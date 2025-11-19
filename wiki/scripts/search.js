// Ai wrote this I don't care
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
        
        // Simple and forgiving fuzzy match function
        function fuzzyMatch(searchTerm, text) {
            if (!searchTerm || !text) return { match: false, score: 0 };

            var search = searchTerm.toLowerCase().trim();
            var target = text.toLowerCase();

            // Empty search shows everything
            if (search === '') {
                return { match: true, score: 100 };
            }

            // Exact match (case-insensitive)
            if (target === search) {
                return { match: true, score: 1000 };
            }

            // Starts with search term
            if (target.indexOf(search) === 0) {
                return { match: true, score: 500 };
            }

            // Contains search term as substring
            if (target.indexOf(search) !== -1) {
                return { match: true, score: 400 };
            }

            // Word boundary matches - check if search matches the start of any word
            var words = target.split(/\s+/);
            for (var i = 0; i < words.length; i++) {
                if (words[i].indexOf(search) === 0) {
                    return { match: true, score: 350 };
                }
                if (words[i].indexOf(search) !== -1) {
                    return { match: true, score: 300 };
                }
            }

            // Fuzzy character matching - only within individual words (strict)
            // This handles typos like "skll" -> "skull" but prevents cross-word matching
            for (var wordIdx = 0; wordIdx < words.length; wordIdx++) {
                var word = words[wordIdx];
                if (word.length < search.length) continue; // Skip words shorter than search

                var searchIdx = 0;
                var lastMatchPos = -1;
                // Allow small gaps for typos, but limit based on word length
                var maxGap = Math.min(2, Math.max(1, Math.floor((word.length - search.length) / 2)));

                for (var j = 0; j < word.length && searchIdx < search.length; j++) {
                    if (word[j] === search[searchIdx]) {
                        // Check gap from last match
                        if (lastMatchPos >= 0) {
                            var gap = j - lastMatchPos - 1;
                            if (gap > maxGap) {
                                // Gap too large, this word doesn't match
                                break;
                            }
                        }
                        lastMatchPos = j;
                        searchIdx++;
                    }
                }

                // If all characters found in order within this word, it's a match
                if (searchIdx === search.length) {
                    // Require that the match is reasonably compact
                    var span = lastMatchPos + 1;
                    var compactness = search.length / span;
                    // At least 50% of the matched span should be the search term
                    if (compactness >= 0.5) {
                        return { match: true, score: 150 + (compactness * 50) };
                    }
                }
            }

            // No match found
            return { match: false, score: 0 };
        }

        // Function to filter navigation
        function filterNavigation(searchTerm) {
            var searchLower = searchTerm ? searchTerm.trim() : '';
            
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
                        var pageTitle = link.textContent;
                        var result = fuzzyMatch(searchLower, pageTitle);
                        
                        if (result.match) {
                            page.style.display = '';
                            visiblePages++;
                        } else {
                            page.style.display = 'none';
                        }
                    } else {
                        // No link found, hide the page
                        page.style.display = 'none';
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
        
        // Handle input events with debouncing for better performance
        var debounceTimer;
        searchInput.addEventListener('input', function(e) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(function () {
                filterNavigation(e.target.value);
            }, 50); // Small delay to avoid excessive filtering while typing
        });
        
        // Handle clear (Escape key)
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                searchInput.value = '';
                filterNavigation('');
                searchInput.blur();
            }
        });

        // Initial filter in case there's a value in the input (e.g., from page reload)
        if (searchInput.value) {
            filterNavigation(searchInput.value);
        }
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initSearch);
    } else {
        initSearch();
    }
})();
