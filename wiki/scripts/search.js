// Wiki search functionality
var searchInput = document.getElementById('wiki-search');
var nav = document.getElementById('nav');

if (searchInput && nav) {
    var allSections = Array.from(nav.querySelectorAll('li.section'));
    var allPages = Array.from(nav.querySelectorAll('li.page'));
    
    function matches(search, text) {
        if (!search) return true;
        search = search.toLowerCase();
        text = text.toLowerCase();
        return text.includes(search) || text.split(/\s+/).some(function(word) {
            return word.indexOf(search) === 0 || word.includes(search);
        });
    }
    
    function filterNavigation(term) {
        var search = term ? term.trim().toLowerCase() : '';
        
        allSections.forEach(function(section) {
            var pages = Array.from(section.querySelectorAll('li.page'));
            var visibleCount = 0;
            
            pages.forEach(function(page) {
                var link = page.querySelector('a');
                var isVisible = link && matches(search, link.textContent);
                page.style.display = isVisible ? '' : 'none';
                if (isVisible) visibleCount++;
            });
            
            section.style.display = visibleCount > 0 ? '' : 'none';
        });
    }
    
    var debounceTimer;
    searchInput.addEventListener('input', function(e) {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(function() {
            filterNavigation(e.target.value);
        }, 50);
    });
    
    searchInput.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            searchInput.value = '';
            filterNavigation('');
            searchInput.blur();
        }
    });
    
    if (searchInput.value) {
        filterNavigation(searchInput.value);
    }
}
