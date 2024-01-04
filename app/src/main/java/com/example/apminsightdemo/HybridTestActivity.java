package com.example.apminsightdemo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.apmplus.hybrid.webview.HybridMonitorManager;

/**
 * @author steven
 * @date 2020/11/11
 */
public class HybridTestActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hybrid_main);
        webView = findViewById(R.id.web_view_root);

        configWebViewDebugMode();

        loadUrl();

    }

    private void configWebViewDebugMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                //js加载
                HybridMonitorManager.getInstance().onProgressChanged(view,newProgress);
            }
        });
    }


    private void loadUrl() {
        final String url = "https://demo-slardar.web.bytedance.net/demo/trigger-event";
        //需要配置监控url
        HybridMonitorManager.getInstance().onLoadUrl(webView,url);
        webView.loadUrl(url);
    }


    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }


}
