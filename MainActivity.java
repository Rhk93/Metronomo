package it.ignazio.metronomo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private static final int SCEGLI_FOTO = 1001;
    private WebView web;
    private ValueCallback<Uri[]> fotoCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lo schermo resta acceso mentre si studia
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);

        web.setWebViewClient(new WebViewClient());

        // Apre la galleria quando l'utente sceglie la foto di sfondo
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (fotoCallback != null) {
                    fotoCallback.onReceiveValue(null);
                }
                fotoCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                try {
                    startActivityForResult(Intent.createChooser(intent, "Scegli una foto"), SCEGLI_FOTO);
                } catch (Exception e) {
                    fotoCallback = null;
                    return false;
                }
                return true;
            }
        });

        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCEGLI_FOTO && fotoCallback != null) {
            Uri[] risultato = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fotoCallback.onReceiveValue(risultato);
            fotoCallback = null;
        }
    }

    @Override
    protected void onPause() {
        // Ferma il metronomo quando si esce dall'app (salvo diversa impostazione)
        if (web != null) {
            web.evaluateJavascript("window.appPausa && appPausa()", null);
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // Se è aperto il pannello impostazioni lo chiude, altrimenti esce
        web.evaluateJavascript("window.appIndietro ? appIndietro() : false", value -> {
            if (!"true".equals(value)) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
        }
        super.onDestroy();
    }
}
