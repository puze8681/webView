package com.example.parktaejun.webview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Handler mHandler;
    private boolean mFlag = false;                      // Back Button 2번 클릭으로 Application 종료를 위한 Flag
    private boolean isLoading = false;                  // WebView의 상태 (Loading 중인지)
    private String addr_naver = "http://naver.com";   // HOME으로 지정할 URL

    private Button mLoadButton;
    private Button mEnterButton;
    private Button mBackButton;
    private Button mHomeButton;
    private Button mNextButton;
    private EditText mUrlBar;
    private WebView mWebView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadButton = (Button)findViewById(R.id.btnLoad);
        mEnterButton = (Button)findViewById(R.id.btnEnter);
        mBackButton = (Button)findViewById(R.id.btnBack);
        mHomeButton = (Button)findViewById(R.id.btnHome);
        mNextButton = (Button)findViewById(R.id.btnNext);
        mUrlBar = (EditText)findViewById(R.id.urlBar);
        mWebView = (WebView)findViewById(R.id.webView);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        mUrlBar.setText(addr_naver);
        mWebView.setWebViewClient(new webClient());      // WebViewClient 지정
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);          // WebView에서 JavaScript 실행 가능
        webSettings.setBuiltInZoomControls(true);        // Multi Touch Zoom 기능 가능
        mWebView.loadUrl(addr_naver);

        mLoadButton.setOnClickListener(onclick);
        mEnterButton.setOnClickListener(onclick);
        mBackButton.setOnClickListener(onclick);
        mHomeButton.setOnClickListener(onclick);
        mNextButton.setOnClickListener(onclick);

        mUrlBar.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    mEnterButton.performClick();
                }
                return false;
            }
        });

/**
 * Handler 객체 설정 (Back Button 2번 클릭 시 Application 종료 하도록 추가한 Handler)
 * (블로그 내 Back Button 2번 클릭 시 Application 종료하기 글 참고)
 */
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    mFlag = false;
                }
            }
        };

/**
 * ProcessBar
 */
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setProgress(newProgress);
            }
        });
    }

    private OnClickListener onclick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.btnEnter:
                    String url = null;
                    url = mUrlBar.getText().toString();
                    mWebView.loadUrl(url);         // 해당 URL 로딩
                    break;
                case R.id.btnLoad:
                    if (isLoading == true) {
                        mWebView.stopLoading();  // 로딩 중지
                    } else {
                        mWebView.reload();       // 새로고침
                    }
                    break;
                case R.id.btnBack:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();       // 이전 페이지
                    }
                    break;
                case R.id.btnHome:
                    mWebView.loadUrl(addr_naver);   // 홈 이동
                    break;
                case R.id.btnNext:
                    if (mWebView.canGoForward()) {
                        mWebView.goForward();     // 다음 페이지
                    }
                    break;
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return false;
            } else {
                if (!mFlag) {
                    Toast.makeText(MainActivity.this, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                    mFlag = true;
                    mHandler.sendEmptyMessageDelayed(0, 2000); // 2초 내로 터치하는 경우
                    return false;
                } else {
                    finish();
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * WebViewClient를 상속받아 shouldOverrideUrlLoading method를 구현해야 WebView 내의 Link 연결 프로그램 묻지 않음
     * WebView가 Loading 중일 때만 Progress Bar를 표시하도록 onPageStarted, onPageFinished method 작성
     */
    class webClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mUrlBar.setText(url);
            mLoadButton.setBackgroundResource(R.drawable.stopload);
            mProgressBar.setVisibility(View.VISIBLE);
            isLoading = true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mLoadButton.setBackgroundResource(R.drawable.reload);
            mProgressBar.setVisibility(View.GONE);
            isLoading = false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        /**
         * URL 타입 별로 Url Loading 방식 설정
         */
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
            } else if (url.startsWith("sms:")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(intent);
            } else if (url.startsWith("mailto:")) {
                MailTo mailTo = MailTo.parse(url);
                Intent intent = newEmailIntent(MainActivity.this, mailTo.getTo(), mailTo.getSubject(), mailTo.getBody(), mailTo.getCc());
                startActivity(intent);
            } else if (url.endsWith(".mp3")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "audio/*");
                view.getContext().startActivity(intent);
            } else if (url.endsWith(".mp4") || url.endsWith(".3gp")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "video/*");
                view.getContext().startActivity(intent);
            } else if (url.startsWith("http:") || url.startsWith("https:")) {
                view.loadUrl(url);
            }
            return true;
        }

        private Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
            intent.putExtra(Intent.EXTRA_TEXT, body);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_CC, cc);
            intent.setType("message/rfc822");
            return intent;

        }
    }

    /**
     * 화면 회전 시 WebView 초기화 방지
     * (AndroidManifest.xml의 Activity element에 android:configChanges="orientation|screenSize" 속성 추가)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//// Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mWebView.clearCache(true);                // 캐시 삭제
        Toast.makeText(getApplicationContext(), "캐시가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }
}