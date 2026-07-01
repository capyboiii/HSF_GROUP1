/* Dark-mode toggle — follows ui-skill.md §10.
   Uses Bootstrap's data-bs-theme attribute and persists the choice under the
   localStorage key "theme". The anti-flash snippet is inlined at the top of
   <head> in each layout; this file only handles toggling + icon state. */

function toggleTheme() {
  var html = document.documentElement;
  var next = html.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
  html.setAttribute('data-bs-theme', next);
  try { localStorage.setItem('theme', next); } catch (e) { /* ignore */ }
  updateThemeIcons();
}

function updateThemeIcons() {
  var isDark = document.documentElement.getAttribute('data-bs-theme') === 'dark';
  var icons = document.querySelectorAll('[data-theme-icon]');
  for (var i = 0; i < icons.length; i++) {
    icons[i].className = (isDark ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill');
    icons[i].setAttribute('data-theme-icon', '');
  }
}

document.addEventListener('DOMContentLoaded', updateThemeIcons);
