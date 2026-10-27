#!/usr/bin/env python3
"""RED separation guard — the executable form of the architecture contract.

Runs without third-party dependencies and fails CI when any of the three planes
leaks into another, when the runtime contract drifts from the compose file, or
when a reference (build context, Dockerfile COPY source, bind mount, Gradle
project directory, docs link) no longer resolves after a move.

Usage:
    python3 tools/guard/separation_guard.py
    python3 tools/guard/separation_guard.py --json
    python3 tools/guard/separation_guard.py --verbose

Exit code 0 = the three planes are separated and still integrate.
"""
from __future__ import annotations

import fnmatch
import json
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import yamlish  # noqa: E402

REPO = Path(__file__).resolve().parents[2]
SKIP_DIRS = {".git", "node_modules", "build", "dist", ".gradle", "target", ".idea", "__pycache__", ".venv"}
DOCS_ARCHIVE = "docs/analysis/archive"


@dataclass
class Result:
    name: str
    plane: str
    failures: list[str] = field(default_factory=list)
    notes: list[str] = field(default_factory=list)

    @property
    def ok(self) -> bool:
        return not self.failures


class Guard:
    def __init__(self) -> None:
        self.results: list[Result] = []
        self.contract = json.loads((REPO / "contracts/stack.json").read_text(encoding="utf-8"))
        self.data_contract = json.loads((REPO / "contracts/data.json").read_text(encoding="utf-8"))
        self.rules = json.loads((REPO / "contracts/separation.rules.json").read_text(encoding="utf-8"))
        self.env = json.loads((REPO / "contracts/env.schema.json").read_text(encoding="utf-8"))
        self.compose_path = REPO / "platform/compose/docker-compose.yml"
        self.compose = yamlish.loads(self.compose_path.read_text(encoding="utf-8"))
        self.env_names = [v["name"] for v in self.env["variables"]]
        self.secret_names = {v["name"] for v in self.env["variables"] if v.get("secret")}
        self.required_names = {v["name"] for v in self.env["variables"] if v.get("required")}

    # ── helpers ───────────────────────────────────────────────────────────
    def check(self, name: str, plane: str) -> Result:
        result = Result(name=name, plane=plane)
        self.results.append(result)
        return result

    def walk(self, root: Path):
        for path in sorted(root.rglob("*")):
            if path.is_dir():
                continue
            relative = path.relative_to(REPO)
            if any(part in SKIP_DIRS for part in relative.parts):
                continue
            yield path, str(relative)

    def rel(self, path: Path) -> str:
        return str(path.relative_to(REPO))

    # ── 1. plane purity ───────────────────────────────────────────────────
    def check_planes(self) -> None:
        for plane_name, plane in self.rules["planes"].items():
            result = self.check(f"plane:{plane_name}", plane_name)
            root = REPO / plane["root"]
            if not root.is_dir():
                result.failures.append(f"missing plane root {plane['root']}/")
                continue
            forbid_ext = set(plane.get("forbid_extensions", []))
            forbid_names = set(plane.get("forbid_filenames", []))
            forbid_globs = plane.get("forbid_globs", [])
            allow_names = set(plane.get("allow_filenames", []))
            for path, relative in self.walk(root):
                name = path.name
                suffix = path.suffix
                if name in allow_names:
                    continue
                if suffix in forbid_ext:
                    result.failures.append(f"{relative} — {suffix} belongs to another plane")
                    continue
                if name in forbid_names:
                    result.failures.append(f"{relative} — infrastructure filename inside {plane['root']}/")
                    continue
                for pattern in forbid_globs:
                    if fnmatch.fnmatch(relative, pattern) or fnmatch.fnmatch(name, pattern):
                        result.failures.append(f"{relative} — matches forbidden pattern {pattern!r}")
                        break
            if plane_name == "services":
                allowed = set(plane.get("allow_yml", []))
                for path, relative in self.walk(root):
                    if path.suffix in {".yml", ".yaml"} and relative not in allowed:
                        result.failures.append(f"{relative} — service-local YAML is not contracted")
            result.notes.append(f"{sum(1 for _ in self.walk(root))} files inspected")

    # ── 2. uniqueness: one definition per concern ─────────────────────────
    def check_uniqueness(self) -> None:
        rules = self.rules["uniqueness"]
        result = self.check("uniqueness:single-source", "cross-plane")
        mirror = rules.get("ci_mirror") or {"path": "Dockerfile", "must_equal": "platform/containers/ci.Dockerfile"}
        for path in rules["exactly_one"]:
            if not (REPO / path).exists():
                result.failures.append(f"missing single source of truth: {path}")

        expected = {
            "**/docker-compose*.y*ml": {"platform/compose/docker-compose.yml"},
            "**/compose.y*ml": {"platform/compose/docker-compose.yml"},
            "**/.env.example": {"platform/config/.env.example"},
            "**/nginx.conf": {"platform/proxy/nginx.conf"},
            "**/stack.json": {"contracts/stack.json"},
            "**/env.schema.json": {"contracts/env.schema.json"},
            "**/data.json": {"contracts/data.json"},
        }
        found: dict[str, set[str]] = {pattern: set() for pattern in expected}
        for path, relative in self.walk(REPO):
            for pattern in expected:
                if fnmatch.fnmatch(relative, pattern):
                    found[pattern].add(relative)
        for pattern, allowed in expected.items():
            extra = found[pattern] - allowed
            if extra:
                result.failures.append(f"duplicate definition for {pattern}: {sorted(extra)}")

        # CI compatibility mirror: one logical definition, byte-identical copy.
        mirror_path, mirror_source = REPO / mirror["path"], REPO / mirror["must_equal"]
        if mirror_path.exists() or mirror_source.exists():
            if not (mirror_path.is_file() and mirror_source.is_file()):
                result.failures.append(f"CI mirror incomplete: {mirror['path']} <-> {mirror['must_equal']}")
            elif mirror_path.read_bytes() != mirror_source.read_bytes():
                result.failures.append(
                    f"{mirror['path']} drifted from {mirror['must_equal']}; run tools/ci/sync-ci-dockerfile.sh"
                )

        for forbidden in rules["forbid_paths"]:
            if (REPO / forbidden).exists():
                result.failures.append(f"legacy/duplicate path must stay removed: {forbidden}")

        for path, relative in self.walk(REPO):
            if path.suffix == ".sql" and not relative.startswith(rules["sql_location"]):
                result.failures.append(f"{relative} — SQL must live under {rules['sql_location']}")
            name = path.name
            if "Dockerfile" in name and relative != mirror["path"]:
                if not any(relative.startswith(prefix) for prefix in rules["dockerfiles_location"]):
                    result.failures.append(f"{relative} — Dockerfiles must live in {rules['dockerfiles_location']}")

    # ── 3. compose ⇄ stack contract ───────────────────────────────────────
    def check_stack(self) -> None:
        result = self.check("integration:compose-vs-contract", "platform")
        compose_services = self.compose.get("services") or {}
        contract_services = {service["name"]: service for service in self.contract["services"]}

        missing = sorted(set(contract_services) - set(compose_services))
        extra = sorted(set(compose_services) - set(contract_services))
        if missing:
            result.failures.append(f"contract services not running in compose: {missing}")
        if extra:
            result.failures.append(f"compose services missing from the contract: {extra}")

        compose_dir = self.compose_path.parent
        for name in sorted(set(contract_services) & set(compose_services)):
            service = contract_services[name]
            actual = compose_services[name] or {}

            if service.get("container") and actual.get("container_name") != service["container"]:
                result.failures.append(f"{name}: container_name {actual.get('container_name')!r} != {service['container']!r}")

            declared_ports = sorted(service.get("ports", []))
            actual_ports = sorted(actual.get("ports") or [])
            if declared_ports != actual_ports:
                result.failures.append(f"{name}: ports {actual_ports} != contract {declared_ports}")

            build = service.get("build")
            if build:
                actual_build = actual.get("build") or {}
                if isinstance(actual_build, str):
                    actual_build = {"context": actual_build}
                if actual_build.get("context") != build["context"]:
                    result.failures.append(f"{name}: build context {actual_build.get('context')!r} != {build['context']!r}")
                if actual_build.get("dockerfile") != build["dockerfile"]:
                    result.failures.append(f"{name}: dockerfile {actual_build.get('dockerfile')!r} != {build['dockerfile']!r}")
                context_dir = (compose_dir / build["context"]).resolve()
                dockerfile = (context_dir / build["dockerfile"]).resolve()
                if not context_dir.is_dir():
                    result.failures.append(f"{name}: build context does not exist: {build['context']}")
                if not dockerfile.is_file():
                    result.failures.append(f"{name}: Dockerfile does not exist: {build['dockerfile']}")
                owner = service.get("owner")
                if owner and owner.startswith("services/") and not (REPO / owner).is_dir():
                    result.failures.append(f"{name}: source owner missing: {owner}")
            else:
                if actual.get("image") != service.get("image"):
                    result.failures.append(f"{name}: image {actual.get('image')!r} != {service.get('image')!r}")

            named: set[str] = set()
            bind_sources: set[str] = set()
            for mount in actual.get("volumes") or []:
                if not isinstance(mount, str):
                    continue
                source = mount.split(":", 1)[0]
                if source.startswith((".", "/", "~")):
                    bind_sources.add(source)
                    resolved = (compose_dir / source).resolve()
                    if not resolved.exists():
                        result.failures.append(f"{name}: bind mount source missing: {source}")
                else:
                    named.add(source)
            if sorted(named) != sorted(set(service.get("volumes", []))):
                result.failures.append(f"{name}: named volumes {sorted(named)} != contract {sorted(service.get('volumes', []))}")
            declared_binds = {entry.split(":", 1)[0] for entry in service.get("binds", [])}
            if declared_binds != bind_sources:
                result.failures.append(f"{name}: bind mounts {sorted(bind_sources)} != contract {sorted(declared_binds)}")

            health = service.get("health") or {}
            healthcheck = actual.get("healthcheck") or {}
            if health.get("type") == "http":
                flattened = json.dumps(healthcheck.get("test", []))
                if health["path"] not in flattened:
                    result.failures.append(f"{name}: healthcheck does not probe {health['path']}")
            elif health.get("type") == "command":
                flattened = json.dumps(healthcheck.get("test", []))
                if health.get("detail", "").split()[0] not in flattened:
                    result.failures.append(f"{name}: healthcheck does not run {health.get('detail')!r}")

        compose_volumes = set((self.compose.get("volumes") or {}).keys())
        contract_volumes = set(self.contract["volumes"])
        if compose_volumes != contract_volumes:
            result.failures.append(
                f"named volumes drift: compose-only {sorted(compose_volumes - contract_volumes)}, "
                f"contract-only {sorted(contract_volumes - compose_volumes)}"
            )

        network = (self.compose.get("networks") or {}).get(self.contract["network"]["name"]) or {}
        subnet = (((network.get("ipam") or {}).get("config") or [{}])[0]).get("subnet")
        if subnet != self.contract["network"]["subnet"]:
            result.failures.append(f"network subnet {subnet!r} != contract {self.contract['network']['subnet']!r}")

        # every volume in the data contract's runtime store table must be declared
        stores = self.data_contract.get("runtime_stores", {})
        for store, spec in stores.items():
            if store == "description":
                continue
            if store not in compose_volumes:
                result.failures.append(f"data contract store {store!r} is not a compose volume")
            dump_dir = spec.get("dump_dir")
            if dump_dir and not dump_dir.startswith("data/runtime/"):
                result.failures.append(f"{store}: backup directory must live under data/runtime/")

    # ── 4. environment contract ──────────────────────────────────────────
    def check_env(self) -> None:
        result = self.check("integration:env-contract", "platform")
        compose_text = self.compose_path.read_text(encoding="utf-8")
        referenced = set(re.findall(r"\$\{([A-Z][A-Z0-9_]*)", compose_text))
        undeclared = sorted(referenced - set(self.env_names))
        if undeclared:
            result.failures.append(f"compose uses variables outside the contract: {undeclared}")

        example = REPO / "platform/config/.env.example"
        declared_in_template = set()
        for line in example.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            key, separator, value = line.partition("=")
            if not separator:
                result.failures.append(f".env.example has a malformed line: {line!r}")
                continue
            declared_in_template.add(key.strip())
            if key.strip() in self.secret_names and not value.strip().startswith("CHANGE_ME_"):
                result.failures.append(f".env.example commits a value for secret {key.strip()}")
        if declared_in_template != set(self.env_names):
            result.failures.append(
                "template drift: "
                f"missing {sorted(set(self.env_names) - declared_in_template)}, "
                f"unknown {sorted(declared_in_template - set(self.env_names))}"
            )
        if self.required_names - declared_in_template:
            result.failures.append(f"template omits required variables: {sorted(self.required_names - declared_in_template)}")

        if (REPO / "platform/config/.env").exists():
            result.failures.append("platform/config/.env exists in the workspace; it must never be committed")

        secrets_dir = REPO / "platform/secrets"
        leaked = [rel for _, rel in self.walk(secrets_dir) if Path(rel).name != ".gitkeep"] if secrets_dir.exists() else []
        if leaked:
            result.failures.append(f"identity material committed under platform/secrets: {leaked}")

        for path, relative in self.walk(REPO):
            if path.suffix in {".pem", ".key", ".p12"} and "signing/red-debug.p12" not in relative:
                result.failures.append(f"{relative} — private key material must not be in the repository")
                continue
            if path.stat().st_size > 2_000_000 or path.suffix in {".png", ".jpg", ".jpeg", ".webp", ".jar", ".lock"}:
                continue
            try:
                text = path.read_text(encoding="utf-8", errors="ignore")
            except OSError:
                continue
            # Assembled at runtime so this file never matches its own scan.
            markers = ("-----BEGIN " + "PRIVATE KEY-----", "-----BEGIN RSA " + "PRIVATE KEY-----",
                       "-----BEGIN EC " + "PRIVATE KEY-----")
            if any(marker in text for marker in markers):
                result.failures.append(f"{relative} — embedded private key block")

    # ── 5. build contexts: every Dockerfile COPY source resolves ─────────
    def check_dockerfiles(self) -> None:
        result = self.check("integration:dockerfile-references", "platform")
        compose_dir = self.compose_path.parent
        builds: list[tuple[str, Path, Path]] = []
        for name, service in (self.compose.get("services") or {}).items():
            build = (service or {}).get("build")
            if not build:
                continue
            if isinstance(build, str):
                build = {"context": build, "dockerfile": "Dockerfile"}
            context_dir = (compose_dir / build["context"]).resolve()
            builds.append((name, context_dir, (context_dir / build["dockerfile"]).resolve()))
        # The CI image is built from the repository root and is not part of compose.
        build_contexts = self.contract.get("extra_builds") or []
        for entry in build_contexts:
            context_dir = (REPO / entry["context"]).resolve()
            builds.append((entry["name"], context_dir, (context_dir / entry["dockerfile"]).resolve()))

        for name, context_dir, dockerfile in builds:
            if not dockerfile.is_file():
                result.failures.append(f"{name}: Dockerfile not found at {dockerfile}")
                continue
            for source in self.dockerfile_sources(dockerfile):
                if "$" in source:
                    continue
                candidate = (context_dir / source).resolve()
                if candidate.exists() or any(candidate.parent.glob(candidate.name)):
                    continue
                if list(candidate.parent.glob(candidate.name)) if candidate.parent.exists() else False:
                    continue
                result.failures.append(f"{name}: COPY source not found: {source} (context {self.rel(context_dir) if str(context_dir).startswith(str(REPO)) else context_dir})")
            result.notes.append(f"{name}: {dockerfile.name}")

    @staticmethod
    def dockerfile_sources(dockerfile: Path) -> list[str]:
        sources: list[str] = []
        raw = dockerfile.read_text(encoding="utf-8")
        joined = raw.replace("\\\n", " ")
        for line in joined.splitlines():
            stripped = line.strip()
            if not stripped or stripped.startswith("#"):
                continue
            tokens = stripped.split()
            if tokens[0].upper() not in {"COPY", "ADD"}:
                continue
            if any(token.startswith("--from") for token in tokens):
                continue
            arguments = [token for token in tokens[1:] if not token.startswith("--")]
            if len(arguments) < 2:
                continue
            sources.extend(arguments[:-1])
        return sources

    # ── 6. Gradle / service wiring ───────────────────────────────────────
    def check_gradle(self) -> None:
        result = self.check("integration:gradle-wiring", "services")
        settings = (REPO / "settings.gradle.kts").read_text(encoding="utf-8")
        for match in re.finditer(r'projectDir\s*=\s*file\("([^"]+)"\)', settings):
            target = (REPO / match.group(1)).resolve()
            if not target.is_dir():
                result.failures.append(f"settings.gradle.kts points at a missing directory: {match.group(1)}")
        for retired in (":fast-lint", ":benchmark", ":microbenchmark", ":baseline-profile", ":lintchecks", ":app-android"):
            if retired in settings:
                result.failures.append(f"settings.gradle.kts still includes retired module {retired}")

        root_build = (REPO / "build.gradle.kts").read_text(encoding="utf-8")
        for retired in (":fast-lint", ":benchmark", ":microbenchmark"):
            if f'"{retired}' in root_build:
                result.failures.append(f"build.gradle.kts still depends on retired module {retired}")

        backend_build = (REPO / "services/backend/build.gradle.kts").read_text(encoding="utf-8")
        if "../../data/sql" not in backend_build:
            result.failures.append("services/backend/build.gradle.kts no longer packages the data plane from ../../data/sql")
        backend_settings = (REPO / "services/backend/settings.gradle.kts").read_text(encoding="utf-8")
        if "../shared-proto" not in backend_settings:
            result.failures.append("services/backend/settings.gradle.kts no longer includes ../shared-proto")
        application = (REPO / "services/backend/src/main/resources/application.yml").read_text(encoding="utf-8")
        if "classpath:migrations" not in application:
            result.failures.append("application.yml must read Flyway migrations from the packaged data plane (classpath:migrations)")
        if not (REPO / "services/android/build.gradle.kts").is_file():
            result.failures.append("services/android is not a Gradle Android application module")
        stray_sql = [rel for _, rel in self.walk(REPO / "services") if Path(rel).suffix == ".sql"]
        if stray_sql:
            result.failures.append(f"SQL found inside the source plane: {stray_sql}")

        # the data plane must declare every migration the service expects
        migrations = sorted((REPO / "data/sql/migrations").glob("V*__*.sql"))
        if not migrations:
            result.failures.append("data/sql/migrations is empty")
        versions = sorted(int(re.match(r"V(\d+)__", path.name).group(1)) for path in migrations)
        if versions != list(range(1, len(versions) + 1)):
            result.failures.append(f"migration versions are not contiguous from V1: {versions}")

    # ── 7. documentation links resolve after the move ─────────────────────
    def check_docs(self) -> None:
        result = self.check("docs:links", "docs")
        documents = [REPO / "README.md"] + sorted((REPO / "docs").rglob("*.md"))
        documents += sorted((REPO / "platform").rglob("*.md"))
        documents += sorted((REPO / "services").rglob("*.md"))
        documents += sorted((REPO / "data").rglob("*.md"))
        pattern = re.compile(r"\]\(([^)\s]+)\)")
        for document in documents:
            relative = document.relative_to(REPO)
            if str(relative).startswith(DOCS_ARCHIVE):
                continue
            text = document.read_text(encoding="utf-8", errors="ignore")
            for target in pattern.findall(text):
                if target.startswith(("http://", "https://", "mailto:", "#")):
                    continue
                path = target.split("#", 1)[0]
                if not path:
                    continue
                resolved = (document.parent / path).resolve()
                if not resolved.exists():
                    result.failures.append(f"{relative}: broken link → {target}")

    # ── run ──────────────────────────────────────────────────────────────
    def run(self) -> int:
        for step in (
            self.check_planes,
            self.check_uniqueness,
            self.check_stack,
            self.check_env,
            self.check_dockerfiles,
            self.check_gradle,
            self.check_docs,
        ):
            try:
                step()
            except Exception as error:  # noqa: BLE001 - the guard must explain, not crash
                result = self.check(f"error:{step.__name__}", "unknown")
                result.failures.append(f"{type(error).__name__}: {error}")
        return 0 if all(result.ok for result in self.results) else 1

    def report(self, as_json: bool, verbose: bool) -> None:
        failed = [result for result in self.results if not result.ok]
        if as_json:
            print(json.dumps(
                {
                    "ok": not failed,
                    "checks": [
                        {"name": r.name, "plane": r.plane, "ok": r.ok, "failures": r.failures, "notes": r.notes}
                        for r in self.results
                    ],
                },
                indent=2,
                ensure_ascii=False,
            ))
            return
        width = max(len(result.name) for result in self.results)
        for result in self.results:
            status = "PASS" if result.ok else "FAIL"
            print(f"[{status}] {result.name.ljust(width)}  ({result.plane})")
            if verbose or not result.ok:
                for note in result.notes:
                    print(f"        · {note}")
            for failure in result.failures:
                print(f"        ✗ {failure}")
        print()
        if failed:
            print(f"SEPARATION GUARD: FAIL — {len(failed)} of {len(self.results)} checks violated the contract")
        else:
            print(
                f"SEPARATION GUARD: PASS — {len(self.results)} checks; "
                "platform / services / data are separated and integrated"
            )


def main(argv: list[str]) -> int:
    guard = Guard()
    code = guard.run()
    guard.report(as_json="--json" in argv, verbose="--verbose" in argv)
    return code


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
