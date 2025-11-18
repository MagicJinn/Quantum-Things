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
        
        # Create output directories
        self.output_dir.mkdir(exist_ok=True)
        (self.output_dir / "assets").mkdir(exist_ok=True)
        (self.output_dir / "images").mkdir(exist_ok=True)
        
        # Copy assets from wiki folder
        self._copy_assets()
        
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
    
    def _get_dark_mode_css(self):
        """Get dark mode CSS styles."""
        return """
<style id="dark-mode-styles">
  body.dark-mode {
    background-color: #251F29 !important;
    color: #CCCCCC !important;
  }
  body.dark-mode #container_content {
    background-color: #251F29 !important;
    color: #CCCCCC !important;
  }
  body.dark-mode #container_content h1,
  body.dark-mode #container_content h2,
  body.dark-mode #container_content h3,
  body.dark-mode #container_content h4,
  body.dark-mode #container_content h5,
  body.dark-mode #container_content h6 {
    color: #CCCCCC !important;
  }
  body.dark-mode #container_content p,
  body.dark-mode #container_content li,
  body.dark-mode #container_content td,
  body.dark-mode #container_content th {
    color: #CCCCCC !important;
  }
  body.dark-mode #container_content a {
    color: #9C53E8 !important;
  }
  body.dark-mode #container_content a:hover {
    color: #F7AE61 !important;
  }
  body.dark-mode #container_navigation {
    background-color: #2A232F !important;
  }
  body.dark-mode #container_navigation a {
    color: #CCCCCC !important;
  }
  body.dark-mode #container_navigation a:hover {
    color: #F7AE61 !important;
  }
  #dark-mode-toggle {
    position: fixed;
    top: 10px;
    right: 10px;
    background-color: #9C53E8;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    z-index: 1000;
    box-shadow: 0 2px 4px rgba(0,0,0,0.3);
  }
  #dark-mode-toggle:hover {
    background-color: #F7AE61;
  }
  body.dark-mode #dark-mode-toggle {
    background-color: #F7AE61;
  }
  body.dark-mode #dark-mode-toggle:hover {
    background-color: #9C53E8;
  }
</style>
"""
    
    def _get_dark_mode_script(self):
        """Get dark mode toggle JavaScript."""
        return """
<script>
  // Dark mode functionality
  (function() {
    // Check localStorage or default to dark mode ON
    const darkModeKey = 'wiki-dark-mode';
    let isDarkMode = localStorage.getItem(darkModeKey);
    if (isDarkMode === null) {
      isDarkMode = 'true'; // Default to dark mode ON
      localStorage.setItem(darkModeKey, 'true');
    }
    
    // Apply dark mode on page load
    if (isDarkMode === 'true') {
      document.body.classList.add('dark-mode');
    }
    
    // Create toggle button
    const toggle = document.createElement('button');
    toggle.id = 'dark-mode-toggle';
    toggle.textContent = isDarkMode === 'true' ? 'Light' : 'Dark';
    toggle.onclick = function() {
      const isDark = document.body.classList.toggle('dark-mode');
      localStorage.setItem(darkModeKey, isDark ? 'true' : 'false');
      toggle.textContent = isDark ? 'Light' : 'Dark';
    };
    document.body.appendChild(toggle);
  })();
</script>
"""
    
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
        
        # Build full HTML page
        html = f"""<!DOCTYPE html>
<html>
<head>
  <title>{page_info['title']} - Random Things</title>
  <meta charset="utf-8"/>
  <link href="../assets/rtwiki_css_main.css" rel="stylesheet"/>
  <link href="../assets/rtwiki_favicon.ico" rel="shortcut icon" type="image/x-icon"/>
  {self._get_dark_mode_css()}
</head>
<body>
  <div id="wrapper">
    <script>
      var observer=new MutationObserver(function(e){{e.forEach(function(e){{if(e.addedNodes)for(var o=0;o<e.addedNodes.length;o++){{var t=e.addedNodes[o];if("container_navigation"===t.id){{var r=sessionStorage.getItem("scroll");r&&(t.scrollTop=r,sessionStorage.removeItem("scroll")),observer.disconnect()}}}}}})}});observer.observe(document.body,{{childList:!0,subtree:!0,attributes:!1,characterData:!1}}),document.body.onclick=function(e){{if(e.target&&e.target.tagName&&"a"===e.target.tagName.toLowerCase()){{var o=document.getElementById("container_navigation");sessionStorage.setItem("scroll",o.scrollTop)}}}};
    </script>
    {self._get_dark_mode_script()}
    <div id="container_navigation">
      <div id="logo">
        <span style="color: #F7AE61;">R</span>andom
        <span style="color: #9C53E8;">T</span>hings
      </div>
      {nav_html}
    </div>
    <div id="container_content">
      {content_html}
    </div>
  </div>
</body>
</html>"""
        
        return html
    
    def _build_index(self):
        """Build the main index page."""
        nav_html = self._build_navigation(base_path='')
        
        content_html = """<div id="main-header-wrapper">
  <div id="container1">
    <div id="container2">
      <div id="container3">
        <p id="ih">Random Things</p>
        <p id="desc">This Wiki contains information about the <a href="https://github.com/MagicJinn/Quantum-Things" target="_blank" rel="noopener">Random Things</a> Minecraft Mod</p>
      </div>
    </div>
  </div>
</div>

<style>
#main-header-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 100px);
  width: 100%;
}
#container1 {
  background-image: linear-gradient(45deg, #F7AE61 33%, #9C53E8 67%);
  padding: 2px;
  display: inline-block;
}
#container2 {
  background: #251f29;
  color: #ccc;
  text-align: center;
  width: auto;
  height: auto;
}
#container3 {
  padding: 20px 40px;
}
#ih {
  font-size: 4em;
  font-weight: bold;
  margin-bottom: 8px;
}
#desc {
  margin-bottom: 0px;
  font-size: 2em;
}
#desc > a:link {
  text-decoration: none;
  color: white;
}
#desc > a:visited {
  text-decoration: none;
  color: white;
}
#desc > a:hover {
  text-decoration: none;
  color: darkorange;
}
body.dark-mode #container2 {
  background: #251f29;
  color: #ccc;
}
</style>"""
        
        html = f"""<!DOCTYPE html>
<html>
<head>
  <title>Random Things Wiki</title>
  <meta charset="utf-8"/>
  <link href="assets/rtwiki_css_main.css" rel="stylesheet"/>
  <link href="assets/rtwiki_favicon.ico" rel="shortcut icon" type="image/x-icon"/>
  {self._get_dark_mode_css()}
</head>
<body>
  <div id="wrapper">
    <script>
      var observer=new MutationObserver(function(e){{e.forEach(function(e){{if(e.addedNodes)for(var o=0;o<e.addedNodes.length;o++){{var t=e.addedNodes[o];if("container_navigation"===t.id){{var r=sessionStorage.getItem("scroll");r&&(t.scrollTop=r,sessionStorage.removeItem("scroll")),observer.disconnect()}}}}}})}});observer.observe(document.body,{{childList:!0,subtree:!0,attributes:!1,characterData:!1}}),document.body.onclick=function(e){{if(e.target&&e.target.tagName&&"a"===e.target.tagName.toLowerCase()){{var o=document.getElementById("container_navigation");sessionStorage.setItem("scroll",o.scrollTop)}}}};
    </script>
    {self._get_dark_mode_script()}
    <div id="container_navigation">
      <div id="logo">
        <span style="color: #F7AE61;">R</span>andom
        <span style="color: #9C53E8;">T</span>hings
      </div>
      {nav_html}
    </div>
    <div id="container_content">
      {content_html}
    </div>
  </div>
</body>
</html>"""
        
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
        
        html = f"""<!DOCTYPE html>
<html>
<head>
  <title>{category.title()} - Random Things Wiki</title>
  <meta charset="utf-8"/>
  <link href="../assets/rtwiki_css_main.css" rel="stylesheet"/>
  <link href="../assets/rtwiki_favicon.ico" rel="shortcut icon" type="image/x-icon"/>
  {self._get_dark_mode_css()}
</head>
<body>
  <div id="wrapper">
    <script>
      var observer=new MutationObserver(function(e){{e.forEach(function(e){{if(e.addedNodes)for(var o=0;o<e.addedNodes.length;o++){{var t=e.addedNodes[o];if("container_navigation"===t.id){{var r=sessionStorage.getItem("scroll");r&&(t.scrollTop=r,sessionStorage.removeItem("scroll")),observer.disconnect()}}}}}})}});observer.observe(document.body,{{childList:!0,subtree:!0,attributes:!1,characterData:!1}}),document.body.onclick=function(e){{if(e.target&&e.target.tagName&&"a"===e.target.tagName.toLowerCase()){{var o=document.getElementById("container_navigation");sessionStorage.setItem("scroll",o.scrollTop)}}}};
    </script>
    {self._get_dark_mode_script()}
    <div id="container_navigation">
      <div id="logo">
        <span style="color: #F7AE61;">R</span>andom
        <span style="color: #9C53E8;">T</span>hings
      </div>
      {nav_html}
    </div>
    <div id="container_content">
      {content_html}
    </div>
  </div>
</body>
</html>"""
        
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

