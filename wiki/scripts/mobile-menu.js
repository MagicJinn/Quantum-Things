// Mobile menu toggle functionality
(function () {
    var menuToggle = document.getElementById('mobile-menu-toggle');
    var navigation = document.getElementById('container_navigation');
    var overlay = document.getElementById('mobile-overlay');

    if (menuToggle && navigation && overlay) {
        function toggleMenu() {
            navigation.classList.toggle('mobile-open');
            overlay.classList.toggle('mobile-open');
        }

        function closeMenu() {
            navigation.classList.remove('mobile-open');
            overlay.classList.remove('mobile-open');
        }

        menuToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            toggleMenu();
        });

        overlay.addEventListener('click', function () {
            closeMenu();
        });

        // Close menu when clicking on a navigation link
        var navLinks = navigation.querySelectorAll('a');
        navLinks.forEach(function (link) {
            link.addEventListener('click', function () {
                closeMenu();
            });
        });

        // Close menu on escape key
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && navigation.classList.contains('mobile-open')) {
                closeMenu();
            }
        });
    }
})();

