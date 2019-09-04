package com.example.catatan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity{
    WebView webView;
    SwipeRefreshLayout swipe;
    private ProgressBar bar;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;

    //select whether you want to upload multiple files (set 'true' for yes)
    private boolean multiple_files = false;

    private boolean adaInternet(){
        ConnectivityManager koneksi = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return koneksi.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //checking if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(intent == null || intent.getData() == null){
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        String dataString = intent.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        } else {
                            if(multiple_files) {
                                if (intent.getClipData() != null) {
                                    final int numSelectedFiles = intent.getClipData().getItemCount();
                                    results = new Uri[numSelectedFiles];
                                    for (int i = 0; i < numSelectedFiles; i++) {
                                        results[i] = intent.getClipData().getItemAt(i).getUri();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else{
            if(requestCode == FCR){
                if(null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @SuppressWarnings({"findViewById", "RedundantCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipe=(SwipeRefreshLayout) findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadWeb();
            }
        });
        LoadWeb();
    }
    public void LoadWeb(){

        webView = (WebView) findViewById(R.id.ifView);
        webView.setWebViewClient(new myWebclient());
        bar=(ProgressBar) this.findViewById(R.id.progressBar2);
        assert webView != null;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);

        if(Build.VERSION.SDK_INT >= 21){
            webSettings.setMixedContentMode(0);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT >= 19){
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        webView.setWebViewClient(new Callback());
        webView.setWebViewClient(new myWebclient());
        //webView.loadUrl("file:///android_res/raw/index.html"); //add your test web/page address here
        //webView.loadUrl("https://simpeg.akprind.ac.id");

        //webView.loadUrl("https://demo.getstisla.com/index.html");
        if(adaInternet()){
            // tampilkan peta
            webView.loadUrl("http://portal.rsuii.net/catatan");
        }else{
            // tampilkan pesan
            Intent noinet=new Intent(MainActivity.this, NoinetActivity.class);
            startActivity(noinet);
            finish();
        }



        //DOWNLOAD
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDescription,
                                        String mimetype, long contentLength) {
                /*
                    DownloadManager.Request
                        This class contains all the information necessary to request a new download.
                        The URI is the only required parameter. Note that the default download
                        destination is a shared volume where the system might delete your file
                        if it needs to reclaim space for system use. If this is a problem,
                        use a location on external storage (see setDestinationUri(Uri).
                */
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                /*
                    void allowScanningByMediaScanner ()
                        If the file to be downloaded is to be scanned by MediaScanner, this method
                        should be called before enqueue(Request) is called.
                */
                request.allowScanningByMediaScanner();

                /*
                    DownloadManager.Request setNotificationVisibility (int visibility)
                        Control whether a system notification is posted by the download manager
                        while this download is running or when it is completed. If enabled, the
                        download manager posts notifications about downloads through the system
                        NotificationManager. By default, a notification is shown only
                        when the download is in progress.

                        It can take the following values: VISIBILITY_HIDDEN, VISIBILITY_VISIBLE,
                        VISIBILITY_VISIBLE_NOTIFY_COMPLETED.

                        If set to VISIBILITY_HIDDEN, this requires the permission
                        android.permission.DOWNLOAD_WITHOUT_NOTIFICATION.

                    Parameters
                        visibility int : the visibility setting value
                    Returns
                        DownloadManager.Request this object
                */
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                /*
                    DownloadManager
                        The download manager is a system service that handles long-running HTTP
                        downloads. Clients may request that a URI be downloaded to a particular
                        destination file. The download manager will conduct the download in the
                        background, taking care of HTTP interactions and retrying downloads
                        after failures or across connectivity changes and system reboots.
                */

                /*
                    String guessFileName (String url, String contentDisposition, String mimeType)
                        Guesses canonical filename that a download would have, using the URL
                        and contentDisposition. File extension, if not defined,
                        is added based on the mimetype

                    Parameters
                        url String : Url to the content
                        contentDisposition String : Content-Disposition HTTP header or null
                        mimeType String : Mime-type of the content or null

                    Returns
                        String : suggested filename
                */
                String fileName = URLUtil.guessFileName(url,contentDescription,mimetype);

                /*
                    DownloadManager.Request setDestinationInExternalPublicDir
                    (String dirType, String subPath)

                        Set the local destination for the downloaded file to a path within
                        the public external storage directory (as returned by
                        getExternalStoragePublicDirectory(String)).

                        The downloaded file is not scanned by MediaScanner. But it can be made
                        scannable by calling allowScanningByMediaScanner().

                    Parameters
                        dirType String : the directory type to pass to
                                         getExternalStoragePublicDirectory(String)
                        subPath String : the path within the external directory, including
                                         the destination filename

                    Returns
                        DownloadManager.Request this object

                    Throws
                        IllegalStateException : If the external storage directory cannot be
                                                found or created.
                */
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);

                DownloadManager dManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                /*
                    long enqueue (DownloadManager.Request request)
                        Enqueue a new download. The download will start automatically once the
                        download manager is ready to execute it and connectivity is available.

                    Parameters
                        request DownloadManager.Request : the parameters specifying this download

                    Returns
                        long : an ID for the download, unique across the system. This ID is used
                               to make future calls related to this download.
                */
                dManager.enqueue(request);
            }
        });




//        webView.setDownloadListener(new DownloadListener() {
//            public void onDownloadStart(String url, String userAgent,
//                                        String contentDisposition, String mimetype,
//                                        long contentLength) {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);
//            }
//        });
        webView.setWebChromeClient(new WebChromeClient() {
            /*
             * openFileChooser is not a public Android API and has never been part of the SDK.
             */
            //handling input[type="file"] requests for android API 16+
            @SuppressLint("ObsoleteSdkInt")
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                if (multiple_files && Build.VERSION.SDK_INT >= 18) {
                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
            }

            //handling input[type="file"] requests for android API 21+
            @SuppressLint("InlinedApi")
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (file_permission()) {
                    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

                    //checking for storage permission to write images for upload
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, perms, FCR);

                        //checking for WRITE_EXTERNAL_STORAGE permission
                    } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, FCR);

                        //checking for CAMERA permissions
                    } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, FCR);
                    }
                    if (mUMA != null) {
                        mUMA.onReceiveValue(null);
                    }
                    mUMA = filePathCallback;
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCM);
                        } catch (IOException ex) {
                            Log.e(TAG, "Image file creation failed", ex);
                        }
                        if (photoFile != null) {
                            mCM = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }
                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType("*/*");
                    if (multiple_files) {
                        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    }
                    Intent[] intentArray;
                    if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooserIntent, FCR);
                    return true;
                }else{
                    return false;
                }
            }
        });
    }
    public class myWebclient extends WebViewClient{
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            bar.setVisibility(webView.GONE);
            swipe.setRefreshing(false);
        }

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {

            if(adaInternet()){
                // tampilkan peta
                super.onPageStarted(webView, url, favicon);
            }else{
                // tampilkan pesan
                Intent noinet=new Intent(MainActivity.this, NoinetActivity.class);
                startActivity(noinet);
                finish();
            }
        }
        //SWIPE

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            webView.loadUrl(url);
            return super.shouldOverrideUrlLoading(webView, url);
        }
        @Override
        public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
            try {
                webView.stopLoading();
            } catch (Exception e) {
            }
            try {
                webView.clearView();
            } catch (Exception e) {
            }
            if (webView.canGoBack()) {
                webView.goBack();
            }
//            webView.loadUrl("file:///android_asset/path/to/your/missing-page-template.html");
//            super.onReceivedError(webView, errorCode, description, failingUrl);

            Intent noinet=new Intent(MainActivity.this, NoinetActivity.class);
            startActivity(noinet);
        }

    }
    //callback reporting if error occurs
    public class Callback extends WebViewClient{
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean file_permission(){
        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            return false;
        }else{
            return true;
        }
    }

    //creating new image file here
    private File createImageFile() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    //back/down key handling
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    if(webView.canGoBack()){
                        webView.goBack();
                    }else{
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }



}