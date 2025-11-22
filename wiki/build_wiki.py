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
    def __init__(self, wiki_dir=None, output_dir="wiki_site"):
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
        
        # Create output directories
        self.output_dir.mkdir(exist_ok=True)
        (self.output_dir / "assets").mkdir(exist_ok=True)
        (self.output_dir / "images").mkdir(exist_ok=True)
        (self.output_dir / "videos").mkdir(exist_ok=True)
        
        # Copy assets from wiki folder
        self._copy_assets()
        
        # Copy CSS and JS files to assets
        self._copy_styles_and_scripts()
        
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
    
    def _load_templates(self):
        """Load HTML templates from files."""
        self.base_template = (self.templates_dir / "base.html").read_text(encoding='utf-8')
        self.index_content_template = (self.templates_dir / "index-content.html").read_text(encoding='utf-8')
    
    def _copy_styles_and_scripts(self):
        """Copy CSS and JavaScript files to output assets directory."""
        import shutil
        
        assets_dest = self.output_dir / "assets"
        
        # Copy CSS files
        if self.styles_dir.exists():
            for css_file in self.styles_dir.glob("*.css"):
                dest = assets_dest / css_file.name
                if not dest.exists() or dest.stat().st_mtime < css_file.stat().st_mtime:
                    shutil.copy2(css_file, dest)
                    print(f"Copied CSS: {css_file.name}")
        
        # Copy JS files
        if self.scripts_dir.exists():
            for js_file in self.scripts_dir.glob("*.js"):
                dest = assets_dest / js_file.name
                if not dest.exists() or dest.stat().st_mtime < js_file.stat().st_mtime:
                    shutil.copy2(js_file, dest)
                    print(f"Copied JS: {js_file.name}")
    
    def _copy_assets(self):
        """Copy CSS and favicon from wiki folder to output assets."""
        import shutil
        
        assets_dest = self.output_dir / "assets"
        
        # Copy CSS file from wiki root
        css_file = self.wiki_dir / "css_main.css"
        if css_file.exists():
            dest = assets_dest / "css_main.css"
            if not dest.exists() or dest.stat().st_mtime < css_file.stat().st_mtime:
                shutil.copy2(css_file, dest)
                print(f"Copied CSS: css_main.css")
        
        # Copy favicon from wiki root
        favicon_file = self.wiki_dir / "favicon.ico"
        if favicon_file.exists():
            dest = assets_dest / "favicon.ico"
            if not dest.exists() or dest.stat().st_mtime < favicon_file.stat().st_mtime:
                shutil.copy2(favicon_file, dest)
                print(f"Copied favicon: favicon.ico")
        
        # Copy images from wiki_md/images/ to wiki_site/images/
        images_source = self.source_dir / "images"
        if images_source.exists():
            for item in images_source.iterdir():
                if item.is_file():
                    dest = self.output_dir / "images" / item.name
                    if not dest.exists() or dest.stat().st_mtime < item.stat().st_mtime:
                        shutil.copy2(item, dest)
        
        # Copy videos from wiki_md/videos/ to wiki_site/videos/
        videos_source = self.source_dir / "videos"
        if videos_source.exists():
            for item in videos_source.iterdir():
                if item.is_file():
                    dest = self.output_dir / "videos" / item.name
                    if not dest.exists() or dest.stat().st_mtime < item.stat().st_mtime:
                        shutil.copy2(item, dest)
                        print(f"Copied video: {item.name}")
    
    def _load_pages(self):
        """Load all page metadata from source directory."""
        pages = {
            'blocks': [],
            'items': [],
            'other': [],
            'about': []
        }
        
        for category in ['blocks', 'items', 'other', 'about']:
            cat_dir = self.source_dir / category
            if cat_dir.exists():
                for md_file in cat_dir.glob("*.md"):
                    if md_file.name == "README.md":
                        continue
                    
                    # Read frontmatter
                    content = md_file.read_text(encoding='utf-8')
                    title = md_file.stem.replace('-', ' ').title()
                    category_name = category
                    
                    # Try to extract from frontmatter
                    if content.startswith('---'):
                        parts = content.split('---', 2)
                        if len(parts) >= 3:
                            frontmatter = parts[1]
                            for line in frontmatter.split('\n'):
                                if ':' in line:
                                    key, value = line.split(':', 1)
                                    key = key.strip()
                                    value = value.strip()
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
        nav_html = '<nav id="nav" role="navigation">\n<ul>\n'
        
        for category in ['about', 'blocks', 'items', 'other']:
            nav_html += f'<li class="section">\n{category.title()}\n<ul>\n'
            
            for page in self.pages[category]:
                active = ' class="active"' if current_page == page['slug'] and current_page else ''
                # Adjust path based on current location
                if base_path == '':
                    # From root
                    href = f'{category}/{page["slug"]}/'
                elif base_path == category:
                    # From same category
                    # If current_page is set, we're in a page subdirectory, need to go up first
                    if current_page:
                        href = f'../{page["slug"]}/'
                    else:
                        # From category index, can use relative path
                        href = f'{page["slug"]}/'
                else:
                    # From different category
                    # If current_page is set, we're in a page subdirectory, need to go up two levels
                    if current_page:
                        href = f'../../{category}/{page["slug"]}/'
                    else:
                        # From category index, go up one level to root
                        href = f'../{category}/{page["slug"]}/'
                
                nav_html += f'<li class="page">\n'
                nav_html += f'<a href="{href}"{active}>{page["title"]}</a>\n'
                nav_html += f'</li>\n'
            
            nav_html += '</ul>\n</li>\n'
        
        nav_html += '</ul>\n</nav>'
        return nav_html
    
    def _convert_markdown_to_html(self, markdown_content):
        """Convert Markdown content to HTML."""
        # Remove frontmatter if present
        if markdown_content.startswith('---'):
            parts = markdown_content.split('---', 2)
            if len(parts) >= 3:
                markdown_content = parts[2].strip()
        
        # Convert to HTML
        html_content = self.md.convert(markdown_content)
        self.md.reset()
        
        return html_content
    
    def _fix_image_paths(self, html_content, category):
        """Fix image paths in HTML content."""
        # Images should point to ../../images/ from category/pagename/index.html
        html_content = re.sub(
            r'src="\.\./images/([^"]+)"',
            r'src="../../images/\1"',
            html_content
        )
        return html_content
    
    def _fix_video_paths(self, html_content, category):
        """Fix video paths in HTML content."""
        # Videos should point to ../../videos/ from category/pagename/index.html
        html_content = re.sub(
            r'src="\.\./videos/([^"]+)"',
            r'src="../../videos/\1"',
            html_content
        )
        return html_content
    
    def _fix_internal_links(self, html_content, category):
        """Fix internal wiki links in HTML content."""
        # Pages are at category/pagename/index.html, so need to go up one level for same-category,
        # or two levels for cross-category links
        
        # Get all valid page slugs for checking same-category links
        valid_slugs = {page['slug'] for page in self.pages[category]}
        
        # First, handle cross-category links: ../blocks/spectre-coils -> ../../blocks/spectre-coils/
        def replace_cross_category_link(match):
            link_category = match.group(1)
            link_path = match.group(2)
            # Split path and anchor if present
            if '#' in link_path:
                link_page, anchor = link_path.split('#', 1)
                anchor = '#' + anchor
            else:
                link_page = link_path
                anchor = ''
            # Remove trailing .md or .html if present
            link_page = re.sub(r'\.(md|html)$', '', link_page)
            # Convert to proper HTML path: ../../category/pagename/ or ../../category/pagename/#anchor
            if anchor:
                return f'href="../../{link_category}/{link_page}/{anchor}"'
            else:
                return f'href="../../{link_category}/{link_page}/"'
        
        # Match href="../blocks/..." or href="../items/..." etc.
        html_content = re.sub(
            r'href="\.\./(blocks|items|other|about)/([^"]+)"',
            replace_cross_category_link,
            html_content
        )
        
        # Then, handle same-category links: ender-anchor -> ../ender-anchor/
        def replace_same_category_link(match):
            full_match = match.group(0)  # The entire href="..." match
            link_page = match.group(1)
            anchor = match.group(2) if match.group(2) else ''
            # Remove trailing .md or .html if present
            link_page = re.sub(r'\.(md|html)$', '', link_page)
            # Check if it's a valid page in the same category
            if link_page in valid_slugs:
                # Convert to proper HTML path: ../pagename/ or ../pagename/#anchor
                if anchor:
                    return f'href="../{link_page}/{anchor}"'
                else:
                    return f'href="../{link_page}/"'
            # If not a valid page, return unchanged
            return full_match
        
        # Match href="pagename" or href="pagename#anchor" (same category, no ../ prefix)
        # Exclude external links (http://, https://, mailto:, etc.) and already processed links
        # The pattern matches: href=" + (not external) + page_name + optional extension + optional anchor + "
        html_content = re.sub(
            r'href="(?!https?://|mailto:|\.\./|\./|/)([^"/#]+?)(?:\.(?:md|html))?(#[^"]*)?"',
            replace_same_category_link,
            html_content
        )
        
        return html_content
    
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
        content_html = self._fix_image_paths(content_html, category)
        content_html = self._fix_video_paths(content_html, category)
        content_html = self._fix_internal_links(content_html, category)
        
        # Build navigation
        nav_html = self._build_navigation(page_info['slug'], category)
        
        # Build full HTML page using template
        html = self.base_template.format(
            title=f"{page_info['title']} - Quantum Things",
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
        
        # Build HTML using template
        html = self.base_template.format(
            title="Quantum Things Wiki",
            assets_path="assets/",
            extra_css='<link href="assets/index-header.css" rel="stylesheet"/>',
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
        for category in ['about', 'blocks', 'items', 'other']:
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
        
        print("-" * 60)
        print(f"Build complete! Site saved to: {self.output_dir.absolute()}")
        print(f"\nTotal pages built:")
        for category in ['about', 'blocks', 'items', 'other']:
            print(f"  {category}: {len(self.pages[category])} pages")

def main():
    builder = WikiBuilder()
    builder.build()

if __name__ == "__main__":
    main()

