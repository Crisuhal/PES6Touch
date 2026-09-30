package net.optijuegos.pes6touch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;
    private static final String GAME_URL = "https://pes6.optijuegos.net/";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        immersive();
        web = new WebView(this);
        web.setBackgroundColor(0xff000000);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                installControls();
            }
        });
        setContentView(web);
        web.loadUrl(GAME_URL);
    }

    private void installControls() {
        web.evaluateJavascript("(function(){if(window.__pesTouchInstalled)return;window.__pesTouchInstalled=true;" +
            "const css=`#pt-root{position:fixed;inset:0;z-index:2147483647;pointer-events:none;font-family:Arial,sans-serif;touch-action:none;user-select:none;-webkit-user-select:none}.pt-stick{position:absolute;left:5vw;bottom:6vh;width:min(31vw,150px);height:min(31vw,150px);border-radius:50%;background:rgba(30,34,42,.45);border:2px solid rgba(255,255,255,.58);pointer-events:auto;touch-action:none;display:grid;place-items:center}.pt-knob{width:43%;height:43%;border-radius:50%;background:rgba(230,235,245,.78);border:2px solid white;box-shadow:0 2px 8px #0008;transform:translate(0,0)}.pt-cluster{position:absolute;right:5vw;bottom:5vh;width:min(43vw,220px);height:min(39vw,180px);pointer-events:none}.pt-btn{position:absolute;width:min(16vw,68px);height:min(16vw,68px);border-radius:50%;border:2px solid #ffffffaa;background:#173e91bb;color:white;font-size:clamp(17px,4vw,25px);font-weight:bold;display:grid;place-items:center;pointer-events:auto;touch-action:none;box-shadow:0 3px 9px #0008}.pt-x{right:5%;top:35%}.pt-o{right:28%;top:8%;background:#a92528bb}.pt-sq{right:50%;top:35%;background:#7b348dbb}.pt-tri{right:28%;top:62%;background:#24864ebb}.pt-meta{position:absolute;top:2vh;right:3vw;display:flex;gap:10px;pointer-events:none}.pt-small{width:54px;height:35px;border-radius:16px;background:#161a20a8;color:white;border:1px solid #ffffff88;font-size:14px;pointer-events:auto;touch-action:none}.pt-start{position:absolute;top:2vh;left:50%;transform:translateX(-50%);padding:8px 18px;border-radius:18px;background:#161a20ad;color:white;border:1px solid #ffffff88;font-weight:bold;pointer-events:auto;touch-action:none}#pt-hint{position:absolute;left:50%;bottom:3px;transform:translateX(-50%);color:#fff;background:#0009;border-radius:10px;padding:4px 9px;font-size:11px;opacity:.82;pointer-events:none}`;" +
            "const st=document.createElement('style');st.textContent=css;document.head.appendChild(st);const root=document.createElement('div');root.id='pt-root';root.innerHTML='<div class=pt-stick><div class=pt-knob></div></div><div class=pt-cluster><button class=pt-btn pt-x data-key=KeyX>X</button><button class=pt-btn pt-o data-key=KeyD>O</button><button class=pt-btn pt-sq data-key=KeyA>□</button><button class=pt-btn pt-tri data-key=KeyW>△</button></div><div class=pt-meta><button class=pt-small data-key=KeyQ>L1</button><button class=pt-small data-key=KeyE>R1</button><button class=pt-small data-key=KeyZ>L2</button><button class=pt-small data-key=KeyC>R2</button></div><button class=pt-start data-key=Space>START</button><div id=pt-hint>Joystick + botones táctiles · tocá START para comenzar</div>';document.body.appendChild(root);" +
            "const down=new Set();function target(){return document.activeElement||document.querySelector('canvas')||document.body}function send(code,isDown){const key=code==='Space'?' ':code.replace('Key','').toLowerCase();const ev=new KeyboardEvent(isDown?'keydown':'keyup',{key:key,code:code,bubbles:true,cancelable:true});target().dispatchEvent(ev);}function setKey(code,on){if(on&&!down.has(code)){down.add(code);send(code,true)}else if(!on&&down.has(code)){down.delete(code);send(code,false)}}" +
            "root.querySelectorAll('[data-key]').forEach(b=>{b.addEventListener('pointerdown',e=>{e.preventDefault();b.setPointerCapture(e.pointerId);setKey(b.dataset.key,true)});const up=e=>{e.preventDefault();setKey(b.dataset.key,false)};b.addEventListener('pointerup',up);b.addEventListener('pointercancel',up);b.addEventListener('lostpointercapture',up)});" +
            "const stick=root.querySelector('.pt-stick'),knob=root.querySelector('.pt-knob');let pid=null;let active=[];function release(){active.forEach(k=>setKey(k,false));active=[];knob.style.transform='translate(0,0)'}function move(e){const r=stick.getBoundingClientRect(),cx=r.left+r.width/2,cy=r.top+r.height/2,dx=e.clientX-cx,dy=e.clientY-cy,max=r.width*.34,len=Math.hypot(dx,dy),scale=len>max?max/len:1,x=dx*scale,y=dy*scale;knob.style.transform='translate('+x+'px,'+y+'px)';const nx=x/max,ny=y/max,n=[];if(ny<-.28)n.push('ArrowUp');if(ny>.28)n.push('ArrowDown');if(nx<-.28)n.push('ArrowLeft');if(nx>.28)n.push('ArrowRight');active.filter(k=>!n.includes(k)).forEach(k=>setKey(k,false));n.filter(k=>!active.includes(k)).forEach(k=>setKey(k,true));active=n}stick.addEventListener('pointerdown',e=>{e.preventDefault();pid=e.pointerId;stick.setPointerCapture(pid);move(e)});stick.addEventListener('pointermove',e=>{if(e.pointerId===pid)move(e)});stick.addEventListener('pointerup',e=>{if(e.pointerId===pid){release();pid=null}});stick.addEventListener('pointercancel',()=>{release();pid=null});document.addEventListener('visibilitychange',()=>{if(document.hidden){Array.from(down).forEach(k=>setKey(k,false));release()}});})();", null);
    }

    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    @Override public void onWindowFocusChanged(boolean hasFocus) { super.onWindowFocusChanged(hasFocus); if(hasFocus) immersive(); }
    @Override public void onBackPressed() { if(web!=null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() { if(web!=null) web.destroy(); super.onDestroy(); }
}
