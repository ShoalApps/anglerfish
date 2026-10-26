// Anglerfish: makes reddit.com feel like an app by removing the prompts web visitors get
// (open in the app, get the app, "use app" bars, cookie banners; optional cookies declined).
(function () {
  if (window.__anglerfish) return;
  window.__anglerfish = true;
  const css = `
    shreddit-experience-tree, xpromo-app-selector, shreddit-async-loader[bundlename="app_selector"],
    faceplate-tracker[noun="open_app"], a[href*="reddit.app.link"], [slot="get-app"], #xpromo-bottom-sheet,
    shreddit-app-selector, .XPromoPopup, .XPromoBottomBar, faceplate-banner[data-testid="xpromo"],
    reddit-cookie-banner { display: none !important; }
    html, body { overscroll-behavior: none; }`;
  const style = document.createElement("style");
  style.textContent = css;
  (document.head || document.documentElement).appendChild(style);
  const dismiss = [/^not now$/i, /^continue$/i, /^reject optional cookies$/i, /^maybe later$/i, /^continue in browser$/i];
  function tidy() {
    for (const el of document.querySelectorAll('button, [role="button"], a')) {
      const t = (el.innerText || "").trim();
      if (!t || t.length > 40) continue;
      const popup = el.closest('[role="dialog"], xpromo-app-selector, shreddit-app-selector, reddit-cookie-banner, [class*="Modal"], [class*="banner"], [class*="Banner"]');
      if (popup && dismiss.some((r) => r.test(t))) el.click();
      if (/^(open app|get app|use app|open in app)$/i.test(t)) el.style.display = "none";
    }
  }
  const start = () => { tidy(); new MutationObserver(tidy).observe(document.documentElement, { childList: true, subtree: true }); };
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", start); else start();
})();
