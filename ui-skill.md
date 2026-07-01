---
name: minimalist-ui-bootstrap
description: Clean editorial-style interfaces. Warm monochrome palette, typographic contrast, flat bento grids, muted pastels. No gradients, no heavy shadows. Overrides default Bootstrap 5 generic styles.
---

# Protocol: Premium Utilitarian Minimalism UI Architect (Bootstrap Edition)

## 1. Protocol Overview
Name: Premium Utilitarian Minimalism & Editorial UI
Description: An advanced frontend engineering directive for generating highly refined, ultra-minimalist, "document-style" web interfaces analogous to top-tier workspace platforms. This protocol strictly enforces a high-contrast warm monochrome palette, bespoke typographic hierarchies, meticulous structural macro-whitespace, bento-grid layouts, and an ultra-flat component architecture with deliberate muted pastel accents. It actively rejects standard generic SaaS design trends and explicitly overrides default Bootstrap 5 aesthetic behaviors.

## 2. Absolute Negative Constraints (Banned Elements)
The AI must strictly avoid the following generic web development defaults and Bootstrap 5 native utilities:
- DO NOT use the "Inter", "Roboto", or "Open Sans" typefaces.
- DO NOT use generic, thin-line icon libraries like "Lucide", "Feather", or standard "Heroicons". (Bootstrap Icons are permitted ONLY IF they are filled or bold variants).
- DO NOT use Bootstrap's default drop shadows (e.g., `shadow`, `shadow-sm`, `shadow-lg`). Shadows must be practically non-existent. Use `shadow-none` extensively or apply heavily customized, ultra-diffuse, low opacity (< 0.04) shadows.
- DO NOT use Bootstrap's native contextual color classes for backgrounds or large elements (e.g., absolutely NO `bg-primary`, `bg-success`, `bg-danger`, `bg-info`, `bg-warning`).
- DO NOT use gradients, neon colors, or 3D glassmorphism.
- DO NOT use Bootstrap's large border-radius utilities (e.g., NO `rounded-pill` for large containers/buttons, NO `rounded-4`, NO `rounded-5`). Keep borders crisp (`rounded-1` or `rounded-2`).
- DO NOT use emojis anywhere in code, markup, text content, headings, or alt text. Replace with proper icons or clean SVG primitives.
- DO NOT use generic placeholder names like "John Doe", "Acme Corp", or "Lorem Ipsum". Use realistic, contextual content.
- DO NOT use AI copywriting clichés: "Elevate", "Seamless", "Unleash", "Next-Gen", "Game-changer", "Delve". Write plain, specific language.

## 3. Typographic Architecture
The interface must rely on extreme typographic contrast and premium font selection to establish an editorial feel. Override Bootstrap's native typography variables if necessary.
- Primary Sans-Serif (Body, UI, Buttons): Use clean, geometric, or system-native fonts with character. Target: `font-family: 'SF Pro Display', 'Geist Sans', 'Helvetica Neue', 'Switzer', sans-serif`.
- Editorial Serif (Hero Headings & Quotes): Target: `font-family: 'Lyon Text', 'Newsreader', 'Playfair Display', 'Instrument Serif', serif`. Apply tight tracking (`letter-spacing: -0.02em` to `-0.04em`) and tight line-height (`1.1`).
- Monospace (Code, Keystrokes, Meta-data): Target: `font-family: 'Geist Mono', 'SF Mono', 'JetBrains Mono', monospace`.
- Text Colors: Body text must never be absolute black (`#000000`). Never use Bootstrap's default `.text-dark` without modifying it to off-black/charcoal (`#111111` or `#2F3437`). Ensure a generous `line-height` of `1.6` for legibility. Secondary text should be muted gray (`#787774` - override `.text-muted`).

## 4. Color Palette (Warm Monochrome + Spot Pastels)
Color is a scarce resource, utilized only for semantic meaning or subtle accents. You must use custom CSS classes or inline styles to apply these, ignoring Bootstrap's `$theme-colors`.
- Canvas / Background: Pure White `#FFFFFF` or Warm Bone/Off-White `#F7F6F3` / `#FBFBFA`.
- Primary Surface (Cards): `#FFFFFF` or `#F9F9F8`.
- Structural Borders / Dividers: Ultra-light gray `#EAEAEA` or `rgba(0,0,0,0.06)`.
- Accent Colors: Exclusively use highly desaturated, washed-out pastels for tags, inline code backgrounds, or subtle icon backgrounds. Apply these via custom classes (e.g., `.badge-pale-red`).
  - Pale Red: `#FDEBEC` (Text: `#9F2F2D`)
  - Pale Blue: `#E1F3FE` (Text: `#1F6C9F`)
  - Pale Green: `#EDF3EC` (Text: `#346538`)
  - Pale Yellow: `#FBF3DB` (Text: `#956400`)

## 5. Component Specifications
- Bento Box Feature Grids:
  - Utilize Bootstrap's Grid system (`row`, `col-*`) or `d-grid`.
  - Cards (`.card`) must have exactly `border: 1px solid #EAEAEA` (override default card borders).
  - Border-radius must be crisp: `8px` or `12px` maximum.
  - Internal padding must be generous (use Bootstrap's `p-4` or `p-5`).
- Primary Call-To-Action (Buttons):
  - BANNED: `.btn-primary`, `.btn-secondary`, `.btn-success`.
  - Use custom class (e.g., `.btn-charcoal`) with solid background `#111111`, text `#FFFFFF`. 
  - Slight border-radius (`4px` to `6px`). No box-shadow. 
  - Hover state should be a subtle color shift to `#333333` or a micro-scale `transform: scale(0.98)`.
- Tags & Status Badges:
  - Use Bootstrap's `.badge` with `.rounded-pill`, but override backgrounds. Very small typography (`text-xs` / `fs-7`), uppercase with wide tracking (`letter-spacing: 0.05em`).
  - Background must use the defined Muted Pastels.
- Accordions (FAQ):
  - Strip all Bootstrap `.accordion` container boxes, borders, and default background colors. 
  - Separate items only with a `border-bottom: 1px solid #EAEAEA`. Use `.accordion-flush` and override backgrounds to transparent.
  - Remove Bootstrap's default blue focus ring (`box-shadow: none`) on `.accordion-button`.
- Keystroke Micro-UIs:
  - Render shortcuts as physical keys using `<kbd>` tags: `border: 1px solid #EAEAEA`, `border-radius: 4px`, `background: #F7F6F3`, using the Monospace font. Strip Bootstrap's default dark `<kbd>` styling.
- Faux-OS Window Chrome:
  - When mocking up software, wrap it in a minimalist container with a white top bar containing three small, light gray circles (replicating macOS window controls).

## 6. Iconography & Imagery Directives
- System Icons: Use "Phosphor Icons (Bold or Fill weights)" or "Radix UI Icons" for a technical, slightly thicker-stroke aesthetic. Standardize stroke width across all icons.
- Illustrations: Monochromatic, rough continuous-line ink sketches on a white background, featuring a single offset geometric shape filled with a muted pastel color.
- Photography: Use high-quality, desaturated images with a warm tone. Apply subtle overlays (`opacity: 0.04` warm grain) to blend photos into the monochrome palette. Never use oversaturated stock photos. Use reliable placeholders like `https://picsum.photos/seed/{context}/1200/800` when real assets are unavailable.
- Hero & Section Backgrounds: Sections should not feel empty and flat. Use subtle full-width background imagery at very low opacity, soft radial light spots (`radial-gradient` with warm tones at `opacity: 0.03`), or minimal geometric line patterns to add depth without breaking the clean aesthetic.

## 7. Subtle Motion & Micro-Animations
Motion should feel invisible — present but never distracting. The goal is quiet sophistication, not spectacle.
- Scroll Entry: Elements fade in gently as they enter the viewport. Use `translateY(12px)` + `opacity: 0` resolving over `600ms` with `cubic-bezier(0.16, 1, 0.3, 1)`. Use `IntersectionObserver`, never `window.addEventListener('scroll')`.
- Hover States: Cards lift with an ultra-subtle shadow shift (`box-shadow` transitioning from `0 0 0` to `0 2px 8px rgba(0,0,0,0.04)` over `200ms`). Buttons respond with `scale(0.98)` on `:active`.
- Staggered Reveals: Lists and grid items enter with a cascade delay (`animation-delay: calc(var(--index) * 80ms)`). Never mount everything at once.
- Background Ambient Motion: Optional. A single, very slow-moving radial gradient blob (`animation-duration: 20s+`, `opacity: 0.02-0.04`) drifting behind hero sections. Must be applied to a `position: fixed; pointer-events: none` layer. Never on scrolling containers.
- Performance: Animate exclusively via `transform` and `opacity`. No layout-triggering properties (`top`, `left`, `width`, `height`). Use `will-change: transform` sparingly and only on actively animating elements.

## 8. Execution Protocol
When tasked with writing frontend code (HTML, Thymeleaf, Bootstrap 5) or designing a layout:
1. Establish the macro-whitespace first. Use Bootstrap's massive vertical padding utilities between sections (e.g., `py-5`, or custom `py-6`, `my-5`). Avoid cluttered layouts.
2. Constrain the main typography content width using strict `.container` limits or inline max-widths (e.g., `style="max-width: 900px;"`).
3. Apply the custom typographic hierarchy and monochromatic color variables immediately, ignoring Bootstrap's native primary/secondary themes.
4. Ensure every `.card`, divider, and border adheres strictly to the `1px solid #EAEAEA` rule. Use `.border` and customize the border color manually.
5. Add scroll-entry animations to all major content blocks.
6. Ensure sections have visual depth through imagery, ambient gradients, or subtle textures — no empty flat backgrounds.
7. Provide code that reflects this high-end, uncluttered, editorial aesthetic natively, implementing custom CSS blocks (`<style>`) where Bootstrap 5 defaults fail to meet these constraints.

## 9. Iconography Implementation (Thymeleaf)
- The project uses **Phosphor Icons** via Webfont CDN.
- Do NOT generate inline `<svg>` code.
- Insert icons using the `<i>` tag with the `ph-bold` or `ph-fill` weight classes.
- Example: `<i class="ph-bold ph-briefcase fs-5 text-charcoal"></i>`

## 10. Dark Mode & Atmospheric UX Protocol (OPTIONAL)

* **Core Logic:** Strictly utilize Bootstrap 5's `data-bs-theme` attribute on the `<html>` element for global state management. Ensure all color-changing elements use transition: background-color 0.4s ease, color 0.4s ease; to ensure theme switches feel smooth rather than abrupt.
* **Elevation Principle:** ABSOLUTELY PROHIBIT `box-shadow` in Dark Mode. Instead, implement "Tonal Elevation" by varying background surface lightness.
* **Surface Layering:** Use the following atmospheric surface hierarchy:
    * **Base Background:** `#0C1222` (Deep Navy - avoid pure `#000000`)
    * **Surface 1 (Cards/Inputs):** `#151B2E`
    * **Surface 2 (Modals/Dropdowns):** `#1A1F3A`
* **Typography:** Never use pure `#FFFFFF` for text in Dark Mode. Use `#F1F5F9` (Off-white) to prevent halation and reduce eye strain.
*   **Anti-Flash Implementation:** To prevent the "Flash of Wrong Theme" (FOWT), the following script MUST be injected at the very top of the `<head>` section in all layout files:

```html
<script>
    (function() {
        const theme = localStorage.getItem('theme') || 'light';
        document.documentElement.setAttribute('data-bs-theme', theme);
    })();
</script>