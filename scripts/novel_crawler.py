#!/usr/bin/env python3
"""Configurable public-domain novel crawler.

Example:
  python scripts/novel_crawler.py --config scripts/novel_source_example.json --output out.txt
"""

from __future__ import annotations

import argparse
import html
import json
import re
import time
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path


@dataclass
class Chapter:
    title: str
    url: str


def fetch(url: str, timeout: int = 20) -> str:
    request = urllib.request.Request(url, headers={"User-Agent": "YiYueZhiMingReader/1.0"})
    with urllib.request.urlopen(request, timeout=timeout) as response:
        data = response.read()
        charset = response.headers.get_content_charset() or "utf-8"
        return data.decode(charset, errors="replace")


def clean_html(value: str) -> str:
    value = re.sub(r"(?is)<(script|style).*?</\1>", "", value)
    value = re.sub(r"(?i)<br\s*/?>", "\n", value)
    value = re.sub(r"(?i)</p>|</div>|</section>|</article>", "\n", value)
    value = re.sub(r"<[^>]+>", "", value)
    value = html.unescape(value)
    value = re.sub(r"[ \t]+", " ", value)
    value = re.sub(r"\n{3,}", "\n\n", value)
    return value.strip()


def extract_first(pattern: str, text: str, default: str = "") -> str:
    match = re.search(pattern, text, re.I | re.S)
    return clean_html(match.group(1)) if match else default


def extract_chapters(config: dict, html_text: str) -> list[Chapter]:
    base_url = config["catalog_url"]
    pattern = config.get("chapter_link_regex") or r'<a\s+[^>]*href=["\']([^"\']+)["\'][^>]*>(.*?)</a>'
    chapters: list[Chapter] = []
    for match in re.finditer(pattern, html_text, re.I | re.S):
        href = match.group(1)
        title = clean_html(match.group(2) if len(match.groups()) > 1 else href)
        if not title:
            continue
        if config.get("chapter_title_filter") and not re.search(config["chapter_title_filter"], title, re.I):
            continue
        chapters.append(Chapter(title=title[:80], url=urllib.parse.urljoin(base_url, href)))
    seen: set[str] = set()
    unique: list[Chapter] = []
    for chapter in chapters:
        if chapter.url in seen:
            continue
        seen.add(chapter.url)
        unique.append(chapter)
    return unique


def extract_content(config: dict, html_text: str) -> str:
    pattern = config.get("content_regex")
    if pattern:
        content = extract_first(pattern, html_text)
        if content:
            return content
    paragraphs = re.findall(r"(?is)<p[^>]*>(.*?)</p>", html_text)
    if paragraphs:
        return "\n\n".join(clean_html(p) for p in paragraphs if clean_html(p))
    return clean_html(html_text)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--config", required=True, help="JSON source config path")
    parser.add_argument("--output", required=True, help="UTF-8 TXT output path")
    args = parser.parse_args()

    config = json.loads(Path(args.config).read_text(encoding="utf-8"))
    catalog = fetch(config["catalog_url"])
    book_title = config.get("book_title") or extract_first(config.get("book_title_regex", r"<title[^>]*>(.*?)</title>"), catalog, "在线小说")
    chapters = extract_chapters(config, catalog)
    max_chapters = int(config.get("max_chapters", 80))
    delay = float(config.get("request_delay_seconds", 0.5))

    if not chapters:
        content = extract_content(config, catalog)
        if not content:
            raise SystemExit("No readable content found")
        Path(args.output).write_text(f"{book_title}\n\n{content}\n", encoding="utf-8")
        return

    chunks = [book_title]
    for chapter in chapters[:max_chapters]:
        page = fetch(chapter.url)
        content = extract_content(config, page)
        if content:
            chunks.append(f"{chapter.title}\n\n{content}")
        time.sleep(delay)
    Path(args.output).write_text("\n\n".join(chunks) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
