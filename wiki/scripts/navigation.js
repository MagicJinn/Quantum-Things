// Save and restore navigation scroll position
const navContainer = document.getElementById("container_navigation");
if (navContainer) {
    const savedScroll = sessionStorage.getItem("scroll");
    if (savedScroll) {
        navContainer.scrollTop = savedScroll;
        sessionStorage.removeItem("scroll");
    }
}

document.body.addEventListener('click', function (e) {
    if (e.target.tagName === 'A') {
        const container = document.getElementById("container_navigation");
        if (container) {
            sessionStorage.setItem("scroll", container.scrollTop);
        }
    }
});
