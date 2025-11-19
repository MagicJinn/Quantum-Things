// Sucks currently, will fix later

// // Dark mode functionality
// (function () {
//     // Check localStorage or default to dark mode ON
//     const darkModeKey = 'wiki-dark-mode';
//     let isDarkMode = localStorage.getItem(darkModeKey);
//     if (isDarkMode === null) {
//         isDarkMode = 'true'; // Default to dark mode ON
//         localStorage.setItem(darkModeKey, 'true');
//     }

//     // Apply dark mode on page load
//     if (isDarkMode === 'true') {
//         document.body.classList.add('dark-mode');
//     }

//     // Create toggle button
//     const toggle = document.createElement('button');
//     toggle.id = 'dark-mode-toggle';
//     toggle.textContent = isDarkMode === 'true' ? 'Light' : 'Dark';
//     toggle.onclick = function () {
//         const isDark = document.body.classList.toggle('dark-mode');
//         localStorage.setItem(darkModeKey, isDark ? 'true' : 'false');
//         toggle.textContent = isDark ? 'Light' : 'Dark';
//     };
//     document.body.appendChild(toggle);
// })();
