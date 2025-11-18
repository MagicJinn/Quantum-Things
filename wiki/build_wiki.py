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
        self.page_template = (self.templates_dir / "page.html").read_text(encoding='utf-8')
        self.index_template = (self.templates_dir / "index.html").read_text(encoding='utf-8')
        self.index_content_template = (self.templates_dir / "index-content.html").read_text(encoding='utf-8')
        self.category_index_template = (self.templates_dir / "category-index.html").read_text(encoding='utf-8')
    
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
        css_file = self.wiki_dir / "rtwiki_css_main.css"
        if css_file.exists():
            dest = assets_dest / "rtwiki_css_main.css"
            if not dest.exists() or dest.stat().st_mtime < css_file.stat().st_mtime:
                shutil.copy2(css_file, dest)
                print(f"Copied CSS: rtwiki_css_main.css")
        
        # Copy favicon from wiki root
        favicon_file = self.wiki_dir / "rtwiki_favicon.ico"
        if favicon_file.exists():
            dest = assets_dest / "rtwiki_favicon.ico"
            if not dest.exists() or dest.stat().st_mtime < favicon_file.stat().st_mtime:
                shutil.copy2(favicon_file, dest)
                print(f"Copied favicon: rtwiki_favicon.ico")
        
        # Copy images from wiki_md/images/ to wiki_site/images/
        images_source = self.source_dir / "images"
        if images_source.exists():
            for item in images_source.iterdir():
                if item.is_file():
                    dest = self.output_dir / "images" / item.name
                    if not dest.exists() or dest.stat().st_mtime < item.stat().st_mtime:
                        shutil.copy2(item, dest)
    
    def _load_pages(self):
        """Load all page metadata from source directory."""
        pages = {
            'blocks': [],
            'items': [],
            'other': []
        }
        
        for category in ['blocks', 'items', 'other']:
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
        
        for category in ['blocks', 'items', 'other']:
            nav_html += f'<li class="section">\n{category.title()}\n<ul>\n'
            
            for page in self.pages[category]:
                active = ' class="active"' if current_page == page['slug'] and current_page else ''
                # Adjust path based on current location
                if base_path == '':
                    # From root
                    href = f'{category}/{page["slug"]}.html'
                elif base_path == category:
                    # From same category
                    href = f'{page["slug"]}.html'
                else:
                    # From different category
                    href = f'../{category}/{page["slug"]}.html'
                
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
        # Images should point to ../images/ from category pages
        html_content = re.sub(
            r'src="\.\./images/([^"]+)"',
            r'src="../images/\1"',
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
        
        # Build navigation
        nav_html = self._build_navigation(page_info['slug'], category)
        
        # Build full HTML page using template
        html = self.page_template.format(
            title=page_info['title'],
            assets_path="../assets/",
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
        html = self.index_template.format(
            navigation=nav_html,
            content=content_html
        )
        
        return html
    
    def _build_category_index(self, category):
        """Build category index page."""
        nav_html = self._build_navigation(base_path=category)
        
        pages_list = ""
        for page in self.pages[category]:
            pages_list += f'<li><a href="{page["slug"]}.html">{page["title"]}</a></li>\n'
        
        content_html = f"""<h1>{category.title()}</h1>
<p>All {category} in Random Things.</p>

<h2>Pages</h2>
<ul>
{pages_list}
</ul>"""
        
        # Build HTML using template
        html = self.category_index_template.format(
            category_title=category.title(),
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
        for category in ['blocks', 'items', 'other']:
            cat_dir = self.output_dir / category
            cat_dir.mkdir(exist_ok=True)
            
            # Build category index
            print(f"Building {category} index...")
            cat_index_html = self._build_category_index(category)
            (cat_dir / "README.html").write_text(cat_index_html, encoding='utf-8')
            
            # Build individual pages
            for page_info in self.pages[category]:
                print(f"  Building {category}/{page_info['slug']}.html...")
                page_html = self._build_page(category, page_info)
                if page_html:
                    (cat_dir / f"{page_info['slug']}.html").write_text(page_html, encoding='utf-8')
        
        print("-" * 60)
        print(f"Build complete! Site saved to: {self.output_dir.absolute()}")
        print(f"\nTotal pages built:")
        for category in ['blocks', 'items', 'other']:
            print(f"  {category}: {len(self.pages[category])} pages")

def main():
    builder = WikiBuilder()
    builder.build()

if __name__ == "__main__":
    main()

