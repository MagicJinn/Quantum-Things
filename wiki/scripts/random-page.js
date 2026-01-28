var wikiPages = null;

function getBasePath() {
    var path = window.location.pathname;
    
    // Check if we're on a page inside a category (e.g., /blocks/pagename/)
    if (path.match(/\/(blocks|items|about|other)\/[^/]+\/?/)) {
        return '../../';
    }
    
    return '';
}

function loadPages() {
    if (wikiPages !== null) return Promise.resolve(wikiPages);
    
    var basePath = getBasePath();
    
    return fetch(basePath + 'assets/pages.json')
        .then(function(response) { return response.json(); })
        .then(function(data) {
            wikiPages = data;
            return wikiPages;
        });
}

function goToRandomPage() {
    loadPages().then(function(pages) {
        var randomIndex = Math.floor(Math.random() * pages.length);
        var randomPage = pages[randomIndex];
        var basePath = getBasePath();
        
        window.location.href = basePath + randomPage.category + '/' + randomPage.slug + '/';
    });
}
