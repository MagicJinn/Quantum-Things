(function() {
    'use strict';
    
    // Wait for DOM to be ready
    function initSearch() {
        var searchInput = document.getElementById('wiki-search');
        var nav = document.getElementById('nav');
        
        if (!searchInput || !nav) {
            return;
        }
        
        // Store references to all pages and sections for quick access
        var allSections = Array.from(nav.querySelectorAll('li.section'));
        var allPages = Array.from(nav.querySelectorAll('li.page'));
        
        // Function to filter navigation
        function filterNavigation(searchTerm) {
            var searchLower = searchTerm ? searchTerm.toLowerCase().trim() : '';
            
            if (searchLower === '') {
                // Show all items
                allSections.forEach(function(section) {
                    section.style.display = '';
                });
                allPages.forEach(function(page) {
                    page.style.display = '';
                });
                return;
            }
            
            // Filter pages and sections
            allSections.forEach(function(section) {
                var pages = Array.from(section.querySelectorAll('li.page'));
                var visiblePages = 0;
                
                pages.forEach(function(page) {
                    var link = page.querySelector('a');
                    if (link) {
                        var pageText = link.textContent.toLowerCase();
                        
                        // Check if page title matches search
                        if (pageText.includes(searchLower)) {
                            page.style.display = '';
                            visiblePages++;
                        } else {
                            page.style.display = 'none';
                        }
                    }
                });
                
                // Hide section if no pages are visible
                if (visiblePages === 0) {
                    section.style.display = 'none';
                } else {
                    section.style.display = '';
                }
            });
        }
        
        // Handle input events
        searchInput.addEventListener('input', function(e) {
            filterNavigation(e.target.value);
        });
        
        // Handle clear (Escape key)
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                searchInput.value = '';
                filterNavigation('');
                searchInput.blur();
            }
        });
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initSearch);
    } else {
        initSearch();
    }
})();

