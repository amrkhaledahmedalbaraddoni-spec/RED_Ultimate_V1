"""A deliberately small YAML subset reader used by the separation guard.

Why not PyYAML? The guard must run in CI, on a bare workstation and inside the
pinned Gradle/Node images without adding a Python dependency. The RED compose
file uses a very small YAML subset (nested maps, block sequences, flow
sequences/maps, block scalars, single-line comments), so a ~150 line reader is
enough — and far easier to audit than a vendored parser.

Unsupported constructs raise YamlSubsetError instead of guessing.
"""
from __future__ import annotations

from typing import Any


class YamlSubsetError(ValueError):
    pass


def _strip_comment(line: str) -> str:
    out = []
    quote: str | None = None
    for index, char in enumerate(line):
        if quote:
            out.append(char)
            if char == quote and (index == 0 or line[index - 1] != "\\"):
                quote = None
            continue
        if char in "\"'":
            quote = char
            out.append(char)
            continue
        if char == "#" and (index == 0 or line[index - 1] in " \t"):
            break
        out.append(char)
    return "".join(out).rstrip()


def _split_top_level(text: str, separator: str) -> list[str]:
    parts: list[str] = []
    depth = 0
    quote: str | None = None
    current: list[str] = []
    for char in text:
        if quote:
            current.append(char)
            if char == quote:
                quote = None
            continue
        if char in "\"'":
            quote = char
            current.append(char)
            continue
        if char in "[{":
            depth += 1
        elif char in "]}":
            depth -= 1
        if char == separator and depth == 0:
            parts.append("".join(current))
            current = []
            continue
        current.append(char)
    parts.append("".join(current))
    return parts


def _scalar(token: str) -> Any:
    token = token.strip()
    if token == "":
        return None
    if token[0] == "[" and token[-1] == "]":
        inner = token[1:-1].strip()
        if not inner:
            return []
        return [_scalar(part) for part in _split_top_level(inner, ",")]
    if token[0] == "{" and token[-1] == "}":
        inner = token[1:-1].strip()
        result: dict[str, Any] = {}
        if not inner:
            return result
        for part in _split_top_level(inner, ","):
            key, _, value = part.partition(":")
            result[str(_scalar(key))] = _scalar(value)
        return result
    if len(token) >= 2 and token[0] == token[-1] and token[0] in "\"'":
        return token[1:-1]
    lowered = token.lower()
    if lowered in {"true", "yes"}:
        return True
    if lowered in {"false", "no"}:
        return False
    if lowered in {"null", "~"}:
        return None
    if token.isdigit() or (token.startswith("-") and token[1:].isdigit()):
        return int(token)
    return token


class _Lines:
    def __init__(self, text: str) -> None:
        self.items: list[tuple[int, str]] = []
        for raw in text.splitlines():
            if "\t" in raw[: len(raw) - len(raw.lstrip())]:
                raise YamlSubsetError("tabs are not allowed for indentation")
            stripped = _strip_comment(raw)
            if not stripped.strip():
                continue
            indent = len(stripped) - len(stripped.lstrip(" "))
            self.items.append((indent, stripped.strip()))

    def __len__(self) -> int:
        return len(self.items)


def _is_sequence(text: str) -> bool:
    return text == "-" or text.startswith("- ")


def _parse_map(lines: _Lines, index: int, indent: int) -> tuple[dict[str, Any], int]:
    result: dict[str, Any] = {}
    while index < len(lines):
        current_indent, text = lines.items[index]
        if current_indent < indent or (current_indent == indent and _is_sequence(text)):
            break
        if current_indent > indent:
            raise YamlSubsetError(f"unexpected indentation near: {text!r}")
        key, separator, rest = text.partition(":")
        if not separator:
            raise YamlSubsetError(f"expected 'key: value' near: {text!r}")
        key = key.strip()
        rest = rest.strip()
        index += 1
        if rest in {"|", ">", "|-", ">-"}:
            collected: list[str] = []
            while index < len(lines) and lines.items[index][0] > current_indent:
                collected.append(lines.items[index][1])
                index += 1
            joined = "\n".join(collected) if rest.startswith("|") else " ".join(collected)
            result[key] = joined
            continue
        if rest.startswith("&"):
            # Anchor declaration: the value is the nested block that follows.
            anchor_name, _, inline = rest[1:].partition(" ")
            if inline.strip():
                result[key] = _scalar(inline)
                continue
            if index < len(lines) and lines.items[index][0] > current_indent:
                child_indent = lines.items[index][0]
                if _is_sequence(lines.items[index][1]):
                    result[key], index = _parse_sequence(lines, index, child_indent)
                else:
                    result[key], index = _parse_map(lines, index, child_indent)
            else:
                result[key] = None
            continue
        if rest:
            result[key] = _scalar(rest)
            continue
        if index < len(lines) and lines.items[index][0] > current_indent:
            child_indent = lines.items[index][0]
            if _is_sequence(lines.items[index][1]):
                result[key], index = _parse_sequence(lines, index, child_indent)
            else:
                result[key], index = _parse_map(lines, index, child_indent)
        else:
            result[key] = None
    return result, index


def _parse_sequence(lines: _Lines, index: int, indent: int) -> tuple[list[Any], int]:
    result: list[Any] = []
    while index < len(lines):
        current_indent, text = lines.items[index]
        if current_indent != indent or not _is_sequence(text):
            break
        rest = text[1:].strip()
        index += 1
        if rest in {"|", ">", "|-", ">-", "|+", ">+"}:
            collected: list[str] = []
            while index < len(lines) and lines.items[index][0] > indent:
                collected.append(lines.items[index][1])
                index += 1
            result.append("\n".join(collected) if rest.startswith("|") else " ".join(collected))
            continue
        if not rest:
            if index < len(lines) and lines.items[index][0] > indent:
                child_indent = lines.items[index][0]
                if _is_sequence(lines.items[index][1]):
                    value, index = _parse_sequence(lines, index, child_indent)
                else:
                    value, index = _parse_map(lines, index, child_indent)
            else:
                value = None
            result.append(value)
            continue
        if (
            ":" in rest
            and not rest.startswith(("[", "{", "\"", "'"))
            and _looks_like_mapping(rest)
        ):
            # "- key: value" starts a mapping item; continue with the sibling keys.
            lines.items[index - 1] = (indent + 2, rest)
            value, index = _parse_map(lines, index - 1, indent + 2)
            result.append(value)
            continue
        result.append(_scalar(rest))
    return result, index


def _looks_like_mapping(text: str) -> bool:
    """True only for '- key: value' / '- key:' items.

    A plain scalar such as '../secrets:/run/secrets:ro' has colons but no
    ':\s', so it must not be mistaken for a mapping item.
    """
    import re

    return bool(re.match(r"^[A-Za-z0-9_.\-]+:(\s|$)", text))


def loads(text: str) -> Any:
    lines = _Lines(text)
    if not len(lines):
        return {}
    value, index = _parse_map(lines, 0, lines.items[0][0])
    if index != len(lines):
        raise YamlSubsetError(f"unparsed content near: {lines.items[index][1]!r}")
    return value
