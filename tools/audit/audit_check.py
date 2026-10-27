#!/usr/bin/env python3
"""Fast layout audit for RED.

This is the human-readable companion to tools/guard/separation_guard.py:
the guard fails the build on a contract violation, this audit prints a short
inventory so an operator can see what the tree looks like right now.

Usage:
    python3 tools/audit/audit_check.py
"""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
PLANES = {
    "platform": "infrastructure",
    "services": "source code",
    "data": "schema / seeds / fixtures",
    "contracts": "contracts",
    "tools": "tooling",
    "docs": "documentation",
}
EXPECTED = {
    "platform/compose/docker-compose.yml": "runtime definition",
    "platform/config/.env.example": "generated environment contract",
    "platform/proxy/nginx.conf": "single HTTP entry",
    "contracts/stack.json": "service contract",
    "contracts/data.json": "data ownership",
    "contracts/env.schema.json": "environment contract",
    "data/sql/migrations": "Flyway migrations",
    "services/backend/build.gradle.kts": "backend build",
    "services/android/build.gradle.kts": "Android build",
    "settings.gradle.kts": "Gradle entry",
}


def count_files(root: Path) -> tuple[int, int]:
    files = 0
    size = 0
    for path in root.rglob("*"):
        if path.is_file() and ".git" not in path.parts:
            files += 1
            size += path.stat().st_size
    return files, size


def main() -> int:
    print("RED layout audit")
    print("=" * 60)
    for plane, role in PLANES.items():
        root = REPO / plane
        if not root.is_dir():
            print(f"[MISSING] {plane:<10} {role}")
            continue
        files, size = count_files(root)
        print(f"[OK]      {plane:<10} {role:<32} {files:>4} files  {size / 1024:>8.1f} KiB")

    print("-" * 60)
    missing = [path for path in EXPECTED if not (REPO / path).exists()]
    for path in EXPECTED:
        mark = "OK " if (REPO / path).exists() else "MISS"
        print(f"[{mark}] {path:<44} {EXPECTED[path]}")

    print("-" * 60)
    guard = REPO / "tools/guard/separation_guard.py"
    if guard.is_file():
        result = subprocess.run([sys.executable, str(guard)], cwd=REPO)
        print(f"separation guard exit code: {result.returncode}")
    else:
        print("separation guard missing")
        return 1
    return 0 if not missing else 1


if __name__ == "__main__":
    raise SystemExit(main())
