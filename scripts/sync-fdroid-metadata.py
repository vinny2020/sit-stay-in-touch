#!/usr/bin/env python3
"""
sync-fdroid-metadata.py — generate an F-Droid-compatible fastlane metadata
tree from Ticklr's existing Google Play Console metadata.

Source of truth: android/app/src/main/play/listings/<play-locale>/
                  android/app/src/main/play/release-notes/<play-locale>/production.txt
Target:           fastlane/metadata/android/<fdroid-locale>/

F-Droid (and fastlane's Android tooling) expect underscore-named files and a
per-versionCode changelog, which differ from Play Console's layout:

    Play Console                          F-Droid / fastlane
    ---------------------------------     ---------------------------------
    title.txt                             title.txt
    short-description.txt                 short_description.txt
    full-description.txt                  full_description.txt
    release-notes/<locale>/production.txt changelogs/<versionCode>.txt
    listings/en-US/graphics/icon.png      images/icon.png  (en-US only)

Run this after prep-release-notes.py / any Play listing edit, before tagging
a release, so fastlane/metadata/android/ stays in sync with the Play source.
This directory is regenerated from scratch on every run — never hand-edit it.

Usage:
    python3 scripts/sync-fdroid-metadata.py
    python3 scripts/sync-fdroid-metadata.py --check
    python3 scripts/sync-fdroid-metadata.py --version-code 133
"""
from __future__ import annotations

import argparse
import os
import re
import shutil
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# Locale map: Play Console listing locale -> F-Droid/fastlane locale.
#
# Identity for every locale EXCEPT "iw-IL": Play requires the legacy ISO code
# "iw" for Hebrew, but F-Droid/fastlane use the modern "he-IL" code.
# ---------------------------------------------------------------------------
LOCALE_MAP: dict[str, str] = {
    "iw-IL": "he-IL",
}

PLAY_ROOT = Path("android/app/src/main/play")
LISTINGS_DIR = PLAY_ROOT / "listings"
NOTES_DIR = PLAY_ROOT / "release-notes"
GRADLE_FILE = Path("android/app/build.gradle.kts")
ICON_SRC = LISTINGS_DIR / "en-US" / "graphics" / "icon" / "icon.png"

OUTPUT_DIR = Path("fastlane/metadata/android")

SHORT_DESCRIPTION_LIMIT = 80  # F-Droid/fastlane hard cap
CHANGELOG_LIMIT = 500  # matches Play's release-notes cap; keep them in sync


def fail(msg: str, code: int = 1) -> None:
    print(f"✗ {msg}", file=sys.stderr)
    sys.exit(code)


def fdroid_locale(play_locale: str) -> str:
    return LOCALE_MAP.get(play_locale, play_locale)


def find_repo_root() -> Path:
    """Walk up until we find the directory containing the Play metadata tree."""
    cur = Path.cwd().resolve()
    for candidate in [cur, *cur.parents]:
        if (candidate / LISTINGS_DIR).is_dir():
            return candidate
    fail(
        f"Could not find {LISTINGS_DIR} from {cur}. "
        "Run this script from inside the ticklr repo."
    )


def parse_version_code(gradle_path: Path) -> int:
    """Extract versionCode from android/app/build.gradle.kts.

    Prefers a plain integer literal:
        versionCode = 132

    Falls back to the CI-overridable dynamic expression's trailing default:
        versionCode = project.findProperty("versionCode")?.toString()?.toInt() ?: 27
    """
    if not gradle_path.is_file():
        fail(f"Missing {gradle_path}")
    text = gradle_path.read_text(encoding="utf-8")

    m = re.search(r"^\s*versionCode\s*=\s*(\d+)\s*$", text, re.MULTILINE)
    if m:
        return int(m.group(1))

    m = re.search(r"versionCode\s*=.*?\?:\s*(\d+)", text)
    if m:
        return int(m.group(1))

    fail(f"Could not parse versionCode from {gradle_path}")


def read_source(path: Path) -> str:
    """Read a source file, stripping trailing whitespace/newlines.

    This is the value used both for length validation and for the file we
    write out — content is otherwise preserved verbatim.
    """
    if not path.is_file():
        fail(f"Missing source file: {path}")
    return path.read_text(encoding="utf-8").rstrip()


def write_output(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content + "\n", encoding="utf-8")


def discover_play_locales() -> list[str]:
    if not LISTINGS_DIR.is_dir():
        fail(f"Missing {LISTINGS_DIR}")
    locales = sorted(p.name for p in LISTINGS_DIR.iterdir() if p.is_dir())
    missing_notes = [loc for loc in locales if not (NOTES_DIR / loc / "production.txt").is_file()]
    if missing_notes:
        fail(
            "Locale(s) have a Play listing but no release-notes/production.txt: "
            + ", ".join(missing_notes)
        )
    return locales


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Generate F-Droid fastlane metadata from Google Play Console metadata.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="Validate source metadata without writing anything.",
    )
    parser.add_argument(
        "--version-code",
        type=int,
        default=None,
        help="Override the versionCode used for the changelog filename "
        "(default: parsed from android/app/build.gradle.kts).",
    )
    args = parser.parse_args()

    repo_root = find_repo_root()
    os.chdir(repo_root)
    print(f"  Repo root: {repo_root}")

    version_code = args.version_code if args.version_code is not None else parse_version_code(GRADLE_FILE)
    print(f"  versionCode: {version_code}" + (" (override)" if args.version_code is not None else " (parsed)"))

    play_locales = discover_play_locales()
    print(f"  Locales: {len(play_locales)}")
    print(f"  Mode: {'check (no writes)' if args.check else 'generate'}")
    print()

    if not args.check:
        if OUTPUT_DIR.exists():
            shutil.rmtree(OUTPUT_DIR)
        OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    over_short_desc: list[tuple[str, int]] = []
    over_changelog: list[tuple[str, int]] = []
    warnings: list[str] = []
    rows: list[tuple[str, int, int]] = []

    for play_locale in play_locales:
        fd_locale = fdroid_locale(play_locale)
        src_dir = LISTINGS_DIR / play_locale

        title = read_source(src_dir / "title.txt")
        short_description = read_source(src_dir / "short-description.txt")
        full_description = read_source(src_dir / "full-description.txt")
        changelog = read_source(NOTES_DIR / play_locale / "production.txt")

        short_len = len(short_description)
        changelog_len = len(changelog)

        if short_len > SHORT_DESCRIPTION_LIMIT:
            over_short_desc.append((fd_locale, short_len))
        if changelog_len > CHANGELOG_LIMIT:
            over_changelog.append((fd_locale, changelog_len))
        if play_locale == "en-US" and short_description.endswith("."):
            warnings.append(
                "en-US short_description ends with a period — F-Droid lint dislikes this."
            )

        rows.append((fd_locale, short_len, changelog_len))

        if not args.check:
            out_dir = OUTPUT_DIR / fd_locale
            write_output(out_dir / "title.txt", title)
            write_output(out_dir / "short_description.txt", short_description)
            write_output(out_dir / "full_description.txt", full_description)
            write_output(out_dir / "changelogs" / f"{version_code}.txt", changelog)

            if play_locale == "en-US":
                if not ICON_SRC.is_file():
                    fail(f"Missing icon: {ICON_SRC}")
                icon_dst = out_dir / "images" / "icon.png"
                icon_dst.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(ICON_SRC, icon_dst)

    # --- Summary table ---------------------------------------------------
    rows.sort(key=lambda r: r[0])
    print(f"{'Locale':<8} {'short_desc':>10} {'changelog':>10}")
    print("-" * 30)
    for fd_locale, short_len, changelog_len in rows:
        flags = []
        if short_len > SHORT_DESCRIPTION_LIMIT:
            flags.append("SHORT_DESC OVER LIMIT")
        if changelog_len > CHANGELOG_LIMIT:
            flags.append("CHANGELOG OVER LIMIT")
        flag = f"  ⚠ {', '.join(flags)}" if flags else ""
        print(f"{fd_locale:<8} {short_len:>10} {changelog_len:>10}{flag}")
    print()

    if warnings:
        for w in warnings:
            print(f"⚠ {w}")
        print()

    ok = not over_short_desc and not over_changelog

    if not args.check and ok:
        print(f"✓ Wrote metadata for {len(play_locales)} locale(s) to {OUTPUT_DIR}/")
        print()

    if over_short_desc:
        print(
            f"✗ {len(over_short_desc)} locale(s) exceed the "
            f"{SHORT_DESCRIPTION_LIMIT}-char short_description limit:"
        )
        for loc, size in over_short_desc:
            print(f"    {loc}: {size} chars")
        print()

    if over_changelog:
        print(
            f"✗ {len(over_changelog)} locale(s) exceed the "
            f"{CHANGELOG_LIMIT}-char changelog limit:"
        )
        for loc, size in over_changelog:
            print(f"    {loc}: {size} chars")
        print()

    if ok:
        print("OK")
        sys.exit(0)
    else:
        print("FAIL")
        sys.exit(1)


if __name__ == "__main__":
    main()
