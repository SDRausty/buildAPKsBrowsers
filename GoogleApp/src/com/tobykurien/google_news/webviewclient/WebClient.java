package com.tobykurien.google_news.webviewclient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.tobykurien.google_news.GoogleNewsActivity;
import com.tobykurien.google_news.R;

public class WebClient extends WebViewClient {
   GoogleNewsActivity activity;
   WebView wv;
   View pd;

   public WebClient(GoogleNewsActivity activity, WebView wv, View pd) {
      this.activity = activity;
      this.wv = wv;
      this.pd = pd;
   }

   @Override
   public void onPageFinished(WebView view, String url) {
      if (pd != null) pd.setVisibility(View.GONE);
      activity.updateStopItem(true);

      // Google+ workaround to prevent opening of blank window
      wv.loadUrl("javascript:_window=function(url){ location.href=url; }");

      CookieSyncManager.getInstance().sync();
      super.onPageFinished(view, url);
   }

   @Override
   public void onPageStarted(WebView view, String url, Bitmap favicon) {
      Log.d("GoogleApps", "loading " + url);

      activity.updateStopItem(false);
      if (pd != null) pd.setVisibility(View.VISIBLE);
      super.onPageStarted(view, url, favicon);
   }

   @Override
   public boolean shouldOverrideUrlLoading(WebView view, String url) {
      Uri uri = getLoadUri(Uri.parse(url));
      if (uri.getScheme().equals("http") || !isGoogleSite(uri)) {
         Intent i = new Intent(android.content.Intent.ACTION_VIEW);
         i.setData(uri);
         activity.startActivity(i);
         return true;
      } else if (uri.getScheme().equals("mailto")) {
         Intent i = new Intent(android.content.Intent.ACTION_SEND);
         i.putExtra(android.content.Intent.EXTRA_EMAIL, url);
         i.setType("text/html");
         activity.startActivity(i);
         return true;
      } else if (uri.getScheme().equals("market")) {
         Intent i = new Intent(android.content.Intent.ACTION_VIEW);
         i.setData(uri);
         i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         activity.startActivity(i);
         return true;
      }

      return super.shouldOverrideUrlLoading(view, url);
   }

   @Override
   public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
      // TODO Auto-generated method stub
      super.onReceivedError(view, errorCode, description, failingUrl);
      Toast.makeText(activity, description, Toast.LENGTH_LONG).show();
   }
   
   /**
    * Parse the Uri and return an actual Uri to load. This will handle
    * exceptions, like loading a URL
    * that is passed in the "url" parameter, to bypass click-throughs, etc.
    * 
    * @param uri
    * @return
    */
   protected Uri getLoadUri(Uri uri) {
      if (uri == null) return uri;

      // handle google news links to external sites directly
      if (uri.getQueryParameter("url") != null) { 
         return Uri.parse(uri.getQueryParameter("url")); 
      }

      return uri;
   }
   
   /**
    * Returns true if the site is within the Google domains
    * @param uri
    * @return
    */
   protected boolean isGoogleSite(Uri uri) {
      // String url = uri.toString();
      String host = uri.getHost();
      String[] googleSites = activity.getResources().getStringArray(R.array.google_sites);
      for (String sites : googleSites) {
         for (String site : sites.split(" ")) {
            if (host.toLowerCase().endsWith(site.toLowerCase())) { return true; }
         }
      }
      return false;
   }   
}
