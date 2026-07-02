---
name: premium-utilitarian-bootstrap-thymeleaf
description: Anti-slop frontend skill for Spring Boot + Thymeleaf + Bootstrap 5. Overrides generic Bootstrap templates into a high-end, editorial-style interface using Font Awesome 6. Strict pre-flight checks for SSR viewport stability.
---

# Protocol: Premium Utilitarian Minimalism UI Architect (Spring Boot + Thymeleaf + Bootstrap 5 Edition)

## 0. BRIEF INFERENCE & SSR CONSTRAINTS
Before generating any Thymeleaf markup or Bootstrap CSS blocks, analyze the specific domain of the Spring Boot application (B2B, Admin, Customer Portal, Editorial Portfolio).
- **Anti-Default Discipline:** Do not build the generic, bright-blue Bootstrap 4/5 dashboard. Do not stack raw layout tables inside basic `.card` components. Reach past the LLM default of `Inter + text-primary` gradients.
- **Thymeleaf Semantic Read:** State in one line before generating code:
  *"Reading this as: Thymeleaf view for <target-audience>, driven by Spring Controller context, utilizing <layout-vibe> via custom Bootstrap utilities."*

## 1. Absolute Negative Constraints (Banned Elements)
The agent must strictly avoid the following generic defaults and default Bootstrap 5 visual behaviors:
- **DO NOT** use default system sans fonts like "Inter", "Roboto", or "Open Sans" as headings.
- **DO NOT** use generic thin icon libraries. (Font Awesome must be forced to specific weights: `fa-solid` or `fa-brands`, never mix random line weights).
- **DO NOT** use Bootstrap's default drop shadows (e.g., `shadow`, `shadow-sm`, `shadow-lg`). Drop shadows must be non-existent or heavily diffuse. Use `.shadow-none` by default.
- **DO NOT** use Bootstrap's native contextual color classes for backgrounds or large semantic elements (absolutely NO `.bg-primary`, `.bg-success`, `.bg-danger`, `.bg-info`, `.bg-warning`).
- **DO NOT** use CSS gradients, neon glow borders, or 3D glassmorphism slop.
- **DO NOT** use Bootstrap's extreme border-radius utilities (NO `.rounded-pill` for large layout containers, NO `.rounded-4` or `.rounded-5`). Keep layout corners crisp (`.rounded-1` or `.rounded-2`).
- **DO NOT** use emojis anywhere in Thymeleaf templates, hardcoded strings, or dynamic `th:text` attributes. Replace with proper Font Awesome glyphs or explicit SVGs.
- **DO NOT** use placeholder data like "John Doe" or "Lorem Ipsum". Leverage realistic contextual mock string variables via Thymeleaf literals (e.g., `th:text="${user.name ?: 'Hieu Nguyen'}"`).

## 2. Typographic Architecture & Hierarchy
The UI relies on structural typography contrast to establish an premium, editorial feel. Override Bootstrap's native typography layers inside a global inline `<style>` block if a standalone CSS file is not present.
- **Primary Sans-Serif (Body text, Form Inputs, Buttons):**
  `font-family: 'SF Pro Display', '-apple-system', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;`
- **Editorial Serif (Display Headings, Page Heros, Section Headers):**
  `font-family: 'Georgia', 'Times New Roman', 'Playfair Display', serif;` Apply tight tracking (`letter-spacing: -0.02em;`) and precise `line-height: 1.15;`.
- **Monospace (Data tables, Meta labels, Code fragments):**
  `font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;`
- **Text Calibration:** Body text must never be absolute black (`#000000`). Override `.text-dark` or apply raw styles targeting off-black/charcoal (`#111111` or `#1F2022`). Force standard `line-height: 1.6` on long-form paragraphs. Muted elements must map to `#757575`.

## 3. Color Palette (Warm Monochrome + Spot Pastels)
Color is a scarce resource, utilized only for semantic verification or precise interactions. Ignore Bootstrap's default `$theme-colors`.
- **Canvas / Base Background:** Pure White `#FFFFFF` or Warm Bone/Off-White `#F9F8F6` / `#F4F3EF`.
- **Primary Surface (Cards, Dropdowns):** `#FFFFFF` or pristine `#FAF9F6`.
- **Structural Borders & Layout Dividers:** Ultra-light gray `#EFEFEF` or `rgba(0,0,0,0.06)`.
- **Spot Pastels (Exclusively for Badges, Tags, or Micro-indicators):** Override Bootstrap background badges with desaturated, flat pastel fills:
  - Pale Red (Danger/Alert): BG `#FDF1F2` | Text `#A63A3A`
  - Pale Blue (Info/Process): BG `#E6F4EA` | Text `#2B6CB0`
  - Pale Green (Success/Active): BG `#EBF7EE` | Text `#2F6A3E`
  - Pale Yellow (Pending/Warning): BG `#FEF9E7` | Text `#8F6B00`

## 4. Component Architecture (Bootstrap 5 Overrides)
- **Bento Grid Layouts:**
  - Utilize Bootstrap's grid matrix (`.row`, `.col-*`) or direct CSS Grid layout wrappers (`d-grid`).
  - Cards (`.card`) must have exactly `border: 1px solid #EFEFEF` and a crisp border-radius of `6px` or `8px` maximum.
  - Internal card padding must be generous (prefer padding utilities `.p-4` or `.p-5`).
- **Primary Call-To-Action (Buttons):**
  - **BANNED:** `.btn-primary`, `.btn-secondary`, `.btn-success`.
  - Use custom class utility `.btn-charcoal` with a solid background of `#111111`, color `#FFFFFF`. 
  - Sharp border corners (`border-radius: 4px;`). Zero box-shadow. Hover states should introduce a micro-opacity shift or scale transformation (`transform: scale(0.98);` on active).
- **Accordions & Collapse Menus:**
  - Strip all default Bootstrap styling from panels. Use `.accordion-flush` and clear out borders.
  - Separate collapsing items using an explicit `border-bottom: 1px solid #EFEFEF`.
  - Remove the blue glow ring focus state on header buttons (`box-shadow: none !important;`).
- **Keystroke Micro-UIs:**
  - Format shortcuts or system metadata variables using the HTML `<kbd>` tag inside the template: `border: 1px solid #EFEFEF; border-radius: 4px; background: #FAF9F6; font-size: 0.8rem; font-family: monospace; color: #111111;`.

## 5. Iconography Implementation (Font Awesome 6)
- **CDN Integration Strategy:** The project standardizes on **Font Awesome 6** (Webfont/CSS version). 
- **DO NOT** output heavy, raw inline `<svg>` blocks unless explicitly requested for highly custom brand-marks.
- **Weight Consistency Rule:** Always enforce solid or brand families strictly. Use `fa-solid` or `fa-brands` explicitly with structural sizing hooks (e.g., `fa-sm`, `fa-fw`).
- **Example Implementation:** 
  ```html
  <i class="fa-solid fa-folder fa-fw me-2 text-charcoal"></i>

  Alignment: Always add the fixed-width utility class fa-fw when aligning icons vertically inside menus, lists, or sidebar fragments.

6. Thymeleaf SSR Optimization & Viewport Stability
No Layout Shifting: Never implement full-height sections using h-screen or vh-100. To ensure cross-platform compatibility across mobile web browsers (Safari iOS top-bar jumps), always leverage modern responsive viewport units: min-h-[100dvh] or raw CSS style overrides min-height: 100dvh;.

Dynamic Conditional Rendering: Use semantic Thymeleaf conditions (th:if, th:unless) cleanly to avoid hidden ghost nodes in the final parsed HTML string.

Fragment Componentization: When writing complex repeatable views (like tables, modal shells, or structural navigation), abstract them into reusable files using th:fragment parameters to ensure clean template architecture.

HTML
<!-- Example of premium fragment inclusion -->
<div th:replace="~{fragments/ui :: bento_card(title=${item.title}, desc=${item.description})}"></div>
7. Subtle Motion & Native Micro-Animations
Avoid massive third-party animation frameworks that degrade SSR performance metrics. Use native CSS coupled with lightweight event triggers.

Scroll Entry Animations: Content containers should lift cleanly into perspective as they cross the viewport threshold. Implement an optimized CSS class approach:

CSS
.reveal-on-scroll {
    opacity: 0;
    transform: translateY(16px);
    transition: opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1), transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.reveal-on-scroll.is-visible {
    opacity: 1;
    transform: translateY(0);
}
Use a single non-blocking IntersectionObserver script isolated at the bottom of the master layout file to apply the .is-visible state.

Performance Constraints: Limit animation execution fields exclusively to GPU-accelerated layer properties (transform, opacity). Never trigger properties that invalidate the layout geometry tree (top, margin, height).

8. Execution Protocol for Generating Code
When rendering target Thymeleaf views:

Establish Core Geometry: Layout the macro-whitespace first. Use ample vertical spacing between distinct logical UI blocks via Bootstrap's default margin/padding ecosystem (.py-5, .my-5, or custom inline grid spacing blocks).

Constrain Typography Spans: Never let plain body content stretch full screen across ultra-wide monitors. Wrap standard text grids inside precise structural layouts or apply explicit container limits (style="max-width: 72ch;" or .container with specific caps).

Inject Native Styling Fail-Safes: If custom CSS properties cannot be mapped immediately inside a decoupled stylesheet, inject a targeted <style scoped> or basic <style> node inside the Thymeleaf layout head container to guarantee layout styling overrides execute prior to render time.

9. Dark Mode & Atmospheric Architecture
Core State Engine: Management of global layout state must bind directly to the standard Bootstrap 5 data-bs-theme structural attribute applied on the root <html> node. Ensure smooth adjustments by setting explicit CSS transitions on structural rendering hooks: transition: background-color 0.3s ease, color 0.3s ease;.

Elevation Principle: Absolutely PROHIBIT the integration of legacy box-shadow implementations in dark color states. Instead, shift to Tonal Elevation models where layer hierarchy is implied through incremental shade brightness increments.

Atmospheric Dark Layering Scale:

Base Context Grid (Canvas background): #0A0F1D (Deep Twilight Navy - avoids pure absolute #000000 text clipping)

Surface Level 1 (Cards, Tab Controls, Input Elements): #121829

Surface Level 2 (Modals, Overlays, Dropdowns): #1A2138

Typography Eye-Strain Safeguard: Never leverage pure white #FFFFFF text strings over dark canvas frames. Force off-white standard typography values (#E2E8F0 or #F1F5F9) to remove character blurring and glare.

Anti-Flash Script Block (Mandatory Layout Injection): To fully eliminate the Flash of Wrong Theme (FOWT) anomaly during slow server-side page loads from the Spring application, the following non-blocking script MUST be present directly at the top of the template layout <head> sequence before asset script streams process:

HTML
<script>
    (function() {
        const cachedTheme = localStorage.getItem('theme') || 'light';
        document.documentElement.setAttribute('data-bs-theme', cachedTheme);
    })();
</script>