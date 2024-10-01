package com.ripenapps.adoreandroid.utils;

import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.http.SslError;

public class MyWebViewClient extends WebViewClient {
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        // Ignore SSL certificate errors
        handler.proceed(); // Ignore certificate errors
    }
}
