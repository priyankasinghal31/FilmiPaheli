// Makes the launcher icons for old phones, the 512 px Play Store icon and the feature graphic.
// Needs Node with the 'playwright' package. Run from the repo root:  node tools/make_store_graphics.js
const { chromium } = require('playwright');
const fs = require('fs');

const TICKET = 'M34,37h40a6,6 0 0 1 6,6v7a4,4 0 0 0 0,8v7a6,6 0 0 1 -6,6h-40a6,6 0 0 1 -6,-6v-7a4,4 0 0 0 0,-8v-7a6,6 0 0 1 6,-6z';
const tile = (x) => `<path fill="#170E2B" d="M${x},49.5a2.5,2.5 0 0 1 2.5,-2.5h6a2.5,2.5 0 0 1 2.5,2.5v10a2.5,2.5 0 0 1 -2.5,2.5h-6a2.5,2.5 0 0 1 -2.5,-2.5z"/><path fill="#F0386B" d="M${x},60h9v2h-9z"/>`;
const ICON = (viewBox) => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${viewBox}" width="100%" height="100%">
  <rect x="0" y="0" width="108" height="108" fill="#170E2B"/>
  <path fill="#FFB224" d="${TICKET}"/>${tile(37)}${tile(51.5)}${tile(66)}</svg>`;

const FEATURE = `<!doctype html><meta charset="utf-8">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Rozha+One&family=DM+Sans:wght@700;800&display=swap">
<style>
 *{box-sizing:border-box;margin:0}
 body{width:1024px;height:500px;overflow:hidden;font-family:'DM Sans',sans-serif;color:#FFF0D2;
  background:radial-gradient(900px 520px at 20% -10%,#3B2272 0%,#170E2B 60%);position:relative}
 .left{position:absolute;left:64px;top:92px;width:520px}
 h1{font-family:'Rozha One','Noto Serif Devanagari',serif;font-weight:400;font-size:92px;line-height:1;color:#FFB224}
 .hi{font-family:'Rozha One','Noto Serif Devanagari',serif;font-size:34px;color:#B9ABDB;margin-top:6px}
 p{font-size:34px;font-weight:800;margin-top:30px;line-height:1.25}
 .chips{display:flex;gap:12px;margin-top:26px}
 .chips span{font-size:20px;font-weight:800;padding:8px 16px;border-radius:99px;background:#2E1B52;border:2px solid #44306F}
 .ticket{position:absolute;right:56px;top:58px;width:330px;height:384px;border-radius:26px;background:#FFF0D2;color:#170E2B;
  box-shadow:0 30px 60px -20px rgba(0,0,0,.7);overflow:hidden;transform:rotate(3deg)}
 .strip{height:122px;background:#21143B;display:flex;align-items:center;justify-content:center;font-size:62px;letter-spacing:10px}
 .body{padding:22px 24px}
 .clue{font-family:'Rozha One',serif;font-size:25px;line-height:1.25}
 .tiles{display:flex;gap:9px;margin-top:22px}
 .tiles i{width:46px;height:58px;border-radius:10px;background:#2E1B52;border-bottom:6px solid #FFB224;display:block}
 .tiles i.on{background:#FFF0D2;border:2px solid #E2D0A8;border-bottom:6px solid #F2DDB2;color:#170E2B;font-style:normal;font-weight:800;font-size:30px;display:flex;align-items:center;justify-content:center}
 .hearts{margin-top:20px;font-size:30px;letter-spacing:6px}
</style>
<div class="left"><h1>Filmi Paheli</h1><div class="hi">फ़िल्मी पहेली</div>
 <p>Guess the Bollywood film from emoji clues</p>
 <div class="chips"><span>500+ films</span><span>Hindi &amp; English</span><span>Daily puzzle</span></div></div>
<div class="ticket"><div class="strip">🏏🌾☀️</div><div class="body">
 <div class="clue">Villagers play cricket against the British to cancel their tax.</div>
 <div class="tiles"><i class="on">L</i><i></i><i></i><i class="on">A</i><i></i><i></i></div>
 <div class="hearts">❤️❤️🖤</div></div></div>`;

(async () => {
  const b = await chromium.launch();
  const shot = async (html, w, h, path, omitBg) => {
    const p = await b.newPage({ viewport: { width: w, height: h } });
    await p.setContent(html, { waitUntil: 'networkidle' });
    await p.screenshot({ path, omitBackground: !!omitBg });
    await p.close();
  };
  // Play Store icon: 512 x 512, full square, slightly zoomed in
  await shot(`<body style="margin:0;width:512px;height:512px">${ICON('12 12 84 84')}</body>`, 512, 512, 'store/icon-512.png');
  // Feature graphic
  await shot(FEATURE, 1024, 500, 'store/feature-graphic-1024x500.png');
  // Launcher icons for Android 7.1 and older (no adaptive icons there)
  const sizes = { mdpi: 48, hdpi: 72, xhdpi: 96, xxhdpi: 144, xxxhdpi: 192 };
  for (const [d, s] of Object.entries(sizes)) {
    const dir = `app/src/main/res/mipmap-${d}`;
    await shot(`<body style="margin:0;background:transparent"><div style="width:${s}px;height:${s}px;border-radius:${s * 0.22}px;overflow:hidden">${ICON('0 0 108 108')}</div></body>`, s, s, `${dir}/ic_launcher.png`, true);
    await shot(`<body style="margin:0;background:transparent"><div style="width:${s}px;height:${s}px;border-radius:50%;overflow:hidden">${ICON('0 0 108 108')}</div></body>`, s, s, `${dir}/ic_launcher_round.png`, true);
  }
  await b.close();
  console.log('done');
})();
