package com.example.apminsightdemo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

import com.bytedance.android.monitor.webview.WebViewMonitorHelper;
import com.bytedance.android.monitor.webview.WebViewMonitorWebChromeClient;
import com.bytedance.android.monitor.webview.WebViewMonitorWebViewClient;

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
        customConfig();
        loadUrl();

    }

    private void configWebViewDebugMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    /**
     * init view config
     */
    private void customConfig() {
        //内部会使用TTLiveWebViewMonitorHelper
        webView.setWebChromeClient(new WebViewMonitorWebChromeClient());
        //内部会使用TTLiveWebViewMonitorHelper
        webView.setWebViewClient(new WebViewMonitorWebViewClient());
    }


    private void loadUrl() {
        final String url = "https://datarangers.com.cn/apminsight/demo/demo/rangers-site-sdk-npm";
        //需要配置监控url
        WebViewMonitorHelper.getInstance().onLoadUrl(webView, url);
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
