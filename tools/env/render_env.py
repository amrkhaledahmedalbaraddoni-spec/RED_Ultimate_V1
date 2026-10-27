#!/usr/bin/env python3
"""Render the RED runtime environment from a single contract.

The contract (contracts/env.schema.json) is the only place where a variable may
be declared. This tool produces:

  platform/config/.env.example  -- committed template (no real secrets)
  platform/config/.env          -- private, git-ignored, random secrets

Usage:
    python3 tools/env/render_env.py --example
    python3 tools/env/render_env.py --server-ip 192.168.1.50
    python3 tools/env/render_env.py --server-ip 192.168.1.50 --force

Nothing here knows about application code; it only knows the contract.
"""
from __future__ import annotations

import argparse
import json
import os
import secrets
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCHEMA = REPO_ROOT / "contracts" / "env.schema.json"
EXAMPLE = REPO_ROOT / "platform" / "config" / ".env.example"
TARGET = REPO_ROOT / "platform" / "config" / ".env"



def load_schema() -> dict:
    with SCHEMA.open(encoding="utf-8") as handle:
        return json.load(handle)


def placeholder(name: str) -> str:
    return f"CHANGE_ME_{name}"


def render(example: bool, server_ip: str | None) -> str:
    schema = load_schema()
    variables = schema["variables"]
    lines: list[str] = [
        "# ============================================================================",
        "# RED Ultimate — runtime environment",
        "# GENERATED FILE — edit contracts/env.schema.json, then re-run:",
        "#   python3 tools/env/render_env.py --example   (refresh this template)",
        "#   python3 tools/env/render_env.py             (create the private .env)",
        "# ============================================================================",
        "",
    ]

    def emit(header: str, predicate) -> None:
        selected = [v for v in variables if predicate(v)]
        if not selected:
            return
        lines.append(f"# ── {header} ──")
        for var in selected:
            name = var["name"]
            secret = bool(var.get("secret"))
            required = bool(var.get("required"))
            if example:
                value = placeholder(name) if secret or required and not var.get("default") else str(var.get("default") or "")
            else:
                value = secrets.token_urlsafe(48) if secret else str(var.get("default") or "")
                if required and not value:
                    value = "REQUIRED_SET_BEFORE_START"
                if server_ip:
                    if name in {"TURN_PUBLIC_HOST", "MEDIASOUP_ANNOUNCED_IP"}:
                        value = server_ip
                    elif name == "ALLOWED_ORIGINS":
                        http_port = next((v.get("default") for v in variables if v["name"] == "RED_HTTP_PORT"), "80")
                        value = (
                            f"http://localhost:{http_port},http://127.0.0.1:{http_port},"
                            f"http://{server_ip}:{http_port}"
                        )
                    elif name == "GRAFANA_ROOT_URL":
                        value = f"http://{server_ip}/grafana/"
            if secret:
                # A separate comment line: Docker Compose env files have no inline
                # comments, so a trailing '#' would become part of the value.
                minimum = var.get("min_length", 16)
                lines.append(f"# secret - minimum {minimum} characters, generated on first run")
            lines.append(f"{name}={value}")
        lines.append("")

    emit("SECRETS — generated randomly; never commit the generated .env", lambda v: v.get("secret"))
    emit("RUNTIME TOPOLOGY — ports, workers, retention", lambda v: (not v.get("secret")) and v.get("plane") == "runtime")
    emit(
        "SERVICE WIRING — consumed by services/backend",
        lambda v: (not v.get("secret")) and v.get("plane") == "backend",
    )

    android = schema.get("android_build", {}).get("properties", [])
    lines.append("# ── ANDROID BUILD — Gradle properties, not runtime env ──")
    for prop in android:
        lines.append(f"# -P{prop['name']}={prop['default']}")
    lines.append("")

    if not example:
        lines.insert(1, "# This file is private. Never commit, copy into an image, or share it.")
    return "\n".join(lines) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--example", action="store_true", help="refresh the committed template instead of the private file")
    parser.add_argument("--server-ip", default=None, help="LAN IPv4 of this server; fills TURN/announced/origins")
    parser.add_argument("--force", action="store_true", help="overwrite an existing private .env")
    args = parser.parse_args()

    target = EXAMPLE if args.example else TARGET
    if not args.example and target.exists() and not args.force:
        print(f"refusing to overwrite {target.relative_to(REPO_ROOT)} (use --force)", file=sys.stderr)
        return 1

    content = render(example=args.example, server_ip=args.server_ip)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(content, encoding="utf-8")
    if not args.example:
        os.chmod(target, 0o600)
    print(f"wrote {target.relative_to(REPO_ROOT)} ({len(content.splitlines())} lines)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
