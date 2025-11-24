// Save and restore navigation scroll position
var nav = document.getElementById("container_navigation");
if (nav) {
    var savedScroll = sessionStorage.getItem("scroll");
    if (savedScroll) {
        nav.scrollTop = savedScroll;
        sessionStorage.removeItem("scroll");
    }
}

document.body.addEventListener('click', function (e) {
    if (e.target.tagName === 'A') {
        var nav = document.getElementById("container_navigation");
        if (nav) {
            sessionStorage.setItem("scroll", nav.scrollTop);
        }
    }
});
