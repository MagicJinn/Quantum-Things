#!/usr/bin/env python3
"""
Build static HTML wiki from Markdown source files.
Preserves the original visual style and navigation.
"""

import os
import re
import json
from pathlib import Path
from bs4 import BeautifulSoup
import markdown
from markdown.extensions import codehilite, tables, fenced_code

class WikiBuilder:
    def __init__(self, wiki_dir=None, output_dir="wiki_site", base_url=None):
        # If wiki_dir is None, use the directory containing this script
        if wiki_dir is None:
            wiki_dir = Path(__file__).parent
        else:
            wiki_dir = Path(wiki_dir)
        
        self.wiki_dir = wiki_dir
        self.source_dir = wiki_dir / "wiki_md"  # Markdown files are in wiki_md/
        self.output_dir = wiki_dir / output_dir  # Output relative to wiki dir
        self.templates_dir = wiki_dir / "templates"
        self.styles_dir = wiki_dir / "styles"
        self.scripts_dir = wiki_dir / "scripts"
        
        # Load configuration
        self.config = self._load_config()
        
        # Use base_url from config if not provided
        if base_url is None:
            base_url = self.config['site']['base_url']
        self.base_url = base_url.rstrip('/')
        
        # Create output directories
        self.output_dir.mkdir(exist_ok=True)
        (self.output_dir / "assets").mkdir(exist_ok=True)
        (self.output_dir / "images").mkdir(exist_ok=True)
        (self.output_dir / "videos").mkdir(exist_ok=True)
        
        # Copy all assets
        self._copy_assets()
        
        # Load templates
        self._load_templates()
        
        # Load page metadata
        self.pages = self._load_pages()
        
        # Configure Markdown
        self.md = markdown.Markdown(
            extensions=[
                'codehilite',
                'tables',
                'fenced_code',
                'toc',
                'meta'
            ],
            extension_configs={
                'codehilite': {
                    'css_class': 'highlight'
                }
            }
        )
    
    def _load_config(self):
        """Load configuration from JSON file."""
        config_file = self.wiki_dir / "config.json"
        with open(config_file, 'r', encoding='utf-8') as f:
            # if the file doesn't exist, we just die, who cares
            return json.load(f)
    
    def _load_templates(self):
        """Load HTML templates from files."""
        self.base_template = (self.templates_dir / "base.html").read_text(encoding='utf-8')
        self.index_content_template = (self.templates_dir / "index-content.html").read_text(encoding='utf-8')
    
    def _copy_file(self, src, dest, name=""):
        """Copy file if it doesn't exist or is outdated."""
        import shutil
        if src.exists() and (not dest.exists() or dest.stat().st_mtime < src.stat().st_mtime):
            shutil.copy2(src, dest)
            if name:
                print(f"Copied: {name}")
    
    def _copy_assets(self):
        """Copy all assets to output directory."""
        import shutil
        assets_dest = self.output_dir / "assets"
        
        # Copy CSS and JS files
        if self.styles_dir.exists():
            for file in self.styles_dir.glob("*.css"):
                self._copy_file(file, assets_dest / file.name, f"CSS: {file.name}")
        
        if self.scripts_dir.exists():
            for file in self.scripts_dir.glob("*.js"):
                self._copy_file(file, assets_dest / file.name, f"JS: {file.name}")
        
        # Copy root assets
        self._copy_file(self.wiki_dir / "favicon.ico", assets_dest / "favicon.ico", "favicon.ico")
        self._copy_file(self.wiki_dir / "og-image.webp", self.output_dir / "og-image.webp", "og-image.webp")
        
        # Copy images and videos
        for media_type in ["images", "videos"]:
            src_dir = self.source_dir / media_type
            if src_dir.exists():
                for item in src_dir.iterdir():
                    if item.is_file():
                        self._copy_file(item, self.output_dir / media_type / item.name, f"{media_type}: {item.name}")
    
    def _load_pages(self):
        """Load all page metadata from source directory."""
        pages = {
            'blocks': [],
            'items': [],
            'other': [],
            'about': []
        }
        
        for category in self.config['categories']:
            cat_dir = self.source_dir / category
            if cat_dir.exists():
                for md_file in cat_dir.glob("*.md"):
                    if md_file.name == "README.md":
                        continue
                    
                    # Read frontmatter
                    content = md_file.read_text(encoding='utf-8')
                    title = md_file.stem.replace('-', ' ').title()
                    category_name = category
                    
                    # Extract from frontmatter
                    if content.startswith('---'):
                        parts = content.split('---', 2)
                        if len(parts) >= 3:
                            for line in parts[1].split('\n'):
                                if ':' in line:
                                    key, value = line.split(':', 1)
                                    key, value = key.strip(), value.strip()
                                    if key == 'title':
                                        title = value
                                    elif key == 'category':
                                        category_name = value
                    
                    pages[category].append({
                        'file': md_file.name,
                        'title': title,
                        'slug': md_file.stem,
                        'category': category_name
                    })
        
        # Sort by title
        for category in pages:
            pages[category].sort(key=lambda x: x['title'])
        
        return pages
    
    def _build_navigation(self, current_page=None, base_path=''):
        """Build the navigation sidebar HTML."""
        nav_html = ['<nav id="nav" role="navigation">', '<ul>']
        
        for category in self.config['categories']:
            nav_html.append(f'<li class="section">{category.title()}<ul>')
            
            for page in self.pages[category]:
                active = ' class="active"' if current_page == page['slug'] else ''
                
                # Build href based on location
                if base_path == '':
                    href = f'{category}/{page["slug"]}/'
                elif base_path == category:
                    href = f'../{page["slug"]}/' if current_page else f'{page["slug"]}/'
                else:
                    href = f'../../{category}/{page["slug"]}/' if current_page else f'../{category}/{page["slug"]}/'
                
                nav_html.append(f'<li class="page"><a href="{href}"{active}>{page["title"]}</a></li>')
            
            nav_html.append('</ul></li>')
        
        nav_html.extend(['</ul>', '</nav>'])
        return '\n'.join(nav_html)
    
    def _convert_markdown_to_html(self, markdown_content):
        """Convert Markdown content to HTML."""
        markdown_content = self._remove_frontmatter(markdown_content)
        html_content = self.md.convert(markdown_content)
        self.md.reset()
        return html_content
    
    def _fix_media_paths(self, html_content):
        """Fix image and video paths in HTML content."""
        html_content = re.sub(r'src="\.\./(images|videos)/([^"]+)"', r'src="../../\1/\2"', html_content)
        return html_content
    
    def _fix_internal_links(self, html_content, category):
        """Fix internal wiki links in HTML content."""
        # Pages are at category/pagename/index.html, so need to go up one level for same-category,
        # or two levels for cross-category links
        
        # Get all valid page slugs for checking same-category links
        valid_slugs = {page['slug'] for page in self.pages[category]}
        
        def replace_cross_category_link(match):
            link_category = match.group(1)
            link_path = match.group(2)
            link_page, anchor = (link_path.split('#', 1) + [''])[:2]
            link_page = re.sub(r'\.(md|html)$', '', link_page)
            anchor = f'#{anchor}' if anchor else ''
            return f'href="../../{link_category}/{link_page}/{anchor}"'
        
        # Match href="../blocks/..." or href="../items/..." etc.
        html_content = re.sub(
            r'href="\.\./(blocks|items|other|about)/([^"]+)"',
            replace_cross_category_link,
            html_content
        )
        
        def replace_same_category_link(match):
            link_page = re.sub(r'\.(md|html)$', '', match.group(1))
            anchor = match.group(2) or ''
            if link_page in valid_slugs:
                return f'href="../{link_page}/{anchor}"'
            return match.group(0)
        
        # Match href="pagename" or href="pagename#anchor" (same category, no ../ prefix)
        # Exclude external links (http://, https://, mailto:, etc.) and already processed links
        # The pattern matches: href=" + (not external) + page_name + optional extension + optional anchor + "
        html_content = re.sub(
            r'href="(?!https?://|mailto:|\.\./|\./|/)([^"/#]+?)(?:\.(?:md|html))?(#[^"]*)?"',
            replace_same_category_link,
            html_content
        )
        
        return html_content
    
    def _extract_text_from_html(self, html_content):
        """Extract plain text from HTML content for meta descriptions."""
        # Use BeautifulSoup to extract text
        soup = BeautifulSoup(html_content, 'html.parser')
        
        # Try to find the description paragraph first (id="desc")
        desc_elem = soup.find(id='desc')
        if desc_elem:
            # Extract text from the description paragraph only
            text = ' '.join(desc_elem.stripped_strings)
        else:
            # Fallback: get all text but exclude logo (id="ih")
            logo_elem = soup.find(id='ih')
            if logo_elem:
                logo_elem.decompose()  # Remove logo from soup
            text = ' '.join(soup.stripped_strings)
        
        return text
    
    def _remove_frontmatter(self, content):
        """Remove frontmatter from markdown content."""
        if content.startswith('---'):
            parts = content.split('---', 2)
            if len(parts) >= 3:
                return parts[2].strip()
        return content
    
    def _extract_meta_description(self, markdown_content, title, category):
        """Extract or generate a meta description from page content."""
        content = self._remove_frontmatter(markdown_content)
        if not content:
            return ""
        
        # Get first paragraph
        for line in content.split('\n'):
            line = line.strip()
            if line and not line.startswith(('#', '```', '![')) and not re.match(r'^\[.+\]\(.+\)$', line):
                # Remove markdown formatting
                line = re.sub(r'\[([^\]]+)\]\([^\)]+\)', r'\1', line)
                line = re.sub(r'\*\*([^\*]+)\*\*', r'\1', line)
                line = re.sub(r'\*([^\*]+)\*', r'\1', line)
                if len(line) > 155:
                    line = line[:155].rsplit(' ', 1)[0] + '...'
                return line
        return ""
    
    def _extract_meta_description_from_html(self, html_content, truncate=True):
        """Extract meta description from HTML content (for homepage)."""
        text = self._extract_text_from_html(html_content)
        if text:
            text = ' '.join(text.split())
            if truncate and len(text) > 155:
                text = text[:155].rsplit(' ', 1)[0] + '...'
        return text
    
    def _generate_meta_keywords(self, title, category):
        """Generate meta keywords for a page."""
        base_keywords = self.config['seo']['base_keywords'].copy()
        category_keywords = self.config['seo']['category_keywords'].get(category, [])
        
        keywords = base_keywords + category_keywords
        keywords.append(title.lower())
        keywords.append(category)
        
        return ', '.join(keywords)
    
    def _generate_canonical_url(self, category=None, slug=None):
        """Generate canonical URL for a page."""
        if category and slug:
            return f"{self.base_url}/{category}/{slug}/"
        elif category:
            return f"{self.base_url}/{category}/"
        else:
            return f"{self.base_url}/"
    
    def _generate_og_image(self):
        """Generate Open Graph image URL."""
        # Use a default OG image - should be 1200x630px for best results
        # The image should be placed in wiki_site/og-image.webp
        # For now, we'll use a consistent absolute URL
        return f"{self.base_url}/og-image.webp"
    
    def _generate_structured_data(self, title, description, category=None, slug=None, page_title=None, website_description=None):
        """Generate JSON-LD structured data for SEO."""
        url = self._generate_canonical_url(category, slug)
        title_suffix = self.config['site']['title_suffix']
        short_title = page_title or title.replace(title_suffix, "")
        website_description = website_description or description
        
        # Base WebSite schema
        website_schema = {
            "@context": "https://schema.org",
            "@type": "WebSite",
            "name": self.config['site']['name'],
            "alternateName": self.config['site']['alternate_name'],
            "url": self.base_url,
            "description": website_description,
            "potentialAction": {
                "@type": "SearchAction",
                "target": {
                    "@type": "EntryPoint",
                    "urlTemplate": f"{self.base_url}/?search={{search_term_string}}"
                },
                "query-input": "required name=search_term_string"
            }
        }
        
        # Article/WebPage schema for individual pages
        if category and slug:
            page_schema = {
                "@context": "https://schema.org",
                "@type": "Article",
                "headline": short_title,
                "description": description,
                "url": url,
                "mainEntityOfPage": {
                    "@type": "WebPage",
                    "@id": url
                },
                "about": {
                    "@type": self.config['structured_data']['game_type'],
                    "name": self.config['site']['game_name'],
                    "applicationCategory": self.config['structured_data']['game_category']
                },
                "isPartOf": {
                    "@type": "WebSite",
                    "name": self.config['site']['name'],
                    "url": self.base_url
                }
            }
            
            # Add breadcrumb
            breadcrumb_schema = {
                "@context": "https://schema.org",
                "@type": "BreadcrumbList",
                "itemListElement": [
                    {
                        "@type": "ListItem",
                        "position": 1,
                        "name": self.config['structured_data']['breadcrumb_home'],
                        "item": self.base_url
                    },
                    {
                        "@type": "ListItem",
                        "position": 2,
                        "name": category.title(),
                        "item": f"{self.base_url}/{category}/"
                    },
                    {
                        "@type": "ListItem",
                        "position": 3,
                        "name": short_title,
                        "item": url
                    }
                ]
            }
            
            return f'<script type="application/ld+json">{json.dumps(website_schema, indent=2)}</script>\n  <script type="application/ld+json">{json.dumps(page_schema, indent=2)}</script>\n  <script type="application/ld+json">{json.dumps(breadcrumb_schema, indent=2)}</script>'
        else:
            # Homepage - just WebSite schema
            return f'<script type="application/ld+json">{json.dumps(website_schema, indent=2)}</script>'
    
    def _build_page(self, category, page_info):
        """Build a single HTML page."""
        md_file = self.source_dir / category / page_info['file']
        if not md_file.exists():
            print(f"    Warning: Markdown file not found: {md_file}")
            return None
        
        # Read Markdown
        markdown_content = md_file.read_text(encoding='utf-8')
        
        # Convert to HTML
        content_html = self._convert_markdown_to_html(markdown_content)
        content_html = self._fix_media_paths(content_html)
        content_html = self._fix_internal_links(content_html, category)
        
        # Build navigation
        nav_html = self._build_navigation(page_info['slug'], category)
        
        # Generate SEO metadata
        title = f"{page_info['title']}{self.config['site']['title_suffix']}"
        og_title = page_info['title']  # Shorter title for OG (without suffix, since og:site_name is set)
        meta_description = self._extract_meta_description(markdown_content, page_info['title'], category)
        meta_keywords = self._generate_meta_keywords(page_info['title'], category)
        canonical_url = self._generate_canonical_url(category, page_info['slug'])
        structured_data = self._generate_structured_data(title, meta_description, category, page_info['slug'], page_info['title'])
        og_image = self._generate_og_image()
        
        # Build full HTML page using template
        html = self.base_template.format(
            title=title,
            og_title=og_title,
            meta_description=meta_description,
            meta_keywords=meta_keywords,
            canonical_url=canonical_url,
            structured_data=structured_data,
            og_image=og_image,
            og_site_name=self.config['site']['name'],
            assets_path="../../assets/",
            extra_css="",
            navigation=nav_html,
            content=content_html
        )
        
        return html
    
    def _build_index(self):
        """Build the main index page."""
        nav_html = self._build_navigation(base_path='')
        
        # Load index content from template
        content_html = self.index_content_template
        
        # Extract meta description from the HTML content itself (truncated for meta tag)
        meta_description = self._extract_meta_description_from_html(content_html, truncate=True)
        
        # Extract full description for structured data (not truncated)
        website_description = self._extract_meta_description_from_html(content_html, truncate=False)
        
        # Generate SEO metadata for homepage
        title = self.config['seo']['homepage']['title']
        og_title = self.config['site']['name']  # Shorter title for OG (since og:site_name is set)
        meta_keywords = self.config['seo']['homepage']['keywords']
        canonical_url = self._generate_canonical_url()
        structured_data = self._generate_structured_data(
            self.config['site']['name'], 
            meta_description,
            website_description=website_description
        )
        og_image = self._generate_og_image()
        
        # Build HTML using template
        html = self.base_template.format(
            title=title,
            og_title=og_title,
            meta_description=meta_description,
            meta_keywords=meta_keywords,
            canonical_url=canonical_url,
            structured_data=structured_data,
            og_image=og_image,
            og_site_name=self.config['site']['name'],
            assets_path="assets/",
            extra_css='<link href="assets/index-header.css" rel="stylesheet" media="print" onload="this.media=\'all\'"/><noscript><link href="assets/index-header.css" rel="stylesheet"/></noscript>',
            navigation=nav_html,
            content=content_html
        )
        
        return html
    
    def build(self):
        """Build the entire wiki site."""
        print("Building wiki site...")
        print("-" * 60)
        
        # Build index
        print("Building index page...")
        index_html = self._build_index()
        (self.output_dir / "index.html").write_text(index_html, encoding='utf-8')
        
        # Build category pages
        for category in self.config['categories']:
            cat_dir = self.output_dir / category
            cat_dir.mkdir(exist_ok=True)
            
            # Build individual pages
            for page_info in self.pages[category]:
                print(f"  Building {category}/{page_info['slug']}/index.html...")
                page_dir = cat_dir / page_info['slug']
                page_dir.mkdir(exist_ok=True)
                page_html = self._build_page(category, page_info)
                if page_html:
                    (page_dir / "index.html").write_text(page_html, encoding='utf-8')
        
        # Generate sitemap
        print("Generating sitemap.xml...")
        self._generate_sitemap()
        
        # Generate robots.txt
        print("Generating robots.txt...")
        self._generate_robots_txt()
        
        print("-" * 60)
        print(f"Build complete! Site saved to: {self.output_dir.absolute()}")
        print(f"\nTotal pages built:")
        for category in self.config['categories']:
            print(f"  {category}: {len(self.pages[category])} pages")
    
    def _generate_sitemap(self):
        """Generate sitemap.xml for search engines."""
        from datetime import datetime
        
        sitemap = ['<?xml version="1.0" encoding="UTF-8"?>']
        sitemap.append('<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">')
        
        # Add homepage
        sitemap.append('  <url>')
        sitemap.append(f'    <loc>{self.base_url}/</loc>')
        sitemap.append(f'    <changefreq>{self.config["sitemap"]["homepage_changefreq"]}</changefreq>')
        sitemap.append(f'    <priority>{self.config["sitemap"]["homepage_priority"]}</priority>')
        sitemap.append('  </url>')
        
        # Add all pages
        for category in self.config['categories']:
            for page_info in self.pages[category]:
                url = self._generate_canonical_url(category, page_info['slug'])
                sitemap.append('  <url>')
                sitemap.append(f'    <loc>{url}</loc>')
                sitemap.append(f'    <changefreq>{self.config["sitemap"]["page_changefreq"]}</changefreq>')
                sitemap.append(f'    <priority>{self.config["sitemap"]["page_priority"]}</priority>')
                sitemap.append('  </url>')
        
        sitemap.append('</urlset>')
        
        sitemap_path = self.output_dir / "sitemap.xml"
        sitemap_path.write_text('\n'.join(sitemap), encoding='utf-8')
    
    def _generate_robots_txt(self):
        """Generate robots.txt file."""
        robots_content = f"""User-agent: *
Allow: /

Sitemap: {self.base_url}/sitemap.xml
"""
        robots_path = self.output_dir / "robots.txt"
        robots_path.write_text(robots_content, encoding='utf-8')

def main():
    builder = WikiBuilder()
    builder.build()

if __name__ == "__main__":
    main()

