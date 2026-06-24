// Wiki search functionality
const searchInput = document.getElementById('wiki-search');
const wikiNav = document.getElementById('nav');

if (searchInput && wikiNav) {
    const allSections = Array.from(wikiNav.querySelectorAll('li.section'));
    const allPages = Array.from(wikiNav.querySelectorAll('li.page'));

    function matches(search, text) {
        if (!search) return true;
        search = search.toLowerCase();
        text = text.toLowerCase();
        return text.includes(search) || text.split(/\s+/).some(function(word) {
            return word.indexOf(search) === 0 || word.includes(search);
        });
    }
    
    function filterNavigation(term) {
        const search = term ? term.trim().toLowerCase() : '';

        allSections.forEach(function(section) {
            const pages = Array.from(section.querySelectorAll('li.page'));
            let visibleCount = 0;

            pages.forEach(function(page) {
                const link = page.querySelector('a');
                const isVisible = link && matches(search, link.textContent);
                page.style.display = isVisible ? '' : 'none';
                if (isVisible) visibleCount++;
            });
            
            section.style.display = visibleCount > 0 ? '' : 'none';
        });
    }

    let debounceTimer;
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
