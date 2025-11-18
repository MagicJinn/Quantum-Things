var observer = new MutationObserver(function (e) {
    e.forEach(function (e) {
        if (e.addedNodes) {
            for (var o = 0; o < e.addedNodes.length; o++) {
                var t = e.addedNodes[o];
                if ("container_navigation" === t.id) {
                    var r = sessionStorage.getItem("scroll");
                    if (r) {
                        t.scrollTop = r;
                        sessionStorage.removeItem("scroll");
                    }
                    observer.disconnect();
                }
            }
        }
    });
});
observer.observe(document.body, {
    childList: !0,
    subtree: !0,
    attributes: !1,
    characterData: !1
});
document.body.onclick = function (e) {
    if (e.target && e.target.tagName && "a" === e.target.tagName.toLowerCase()) {
        var o = document.getElementById("container_navigation");
        if (o) {
            sessionStorage.setItem("scroll", o.scrollTop);
        }
    }
};
