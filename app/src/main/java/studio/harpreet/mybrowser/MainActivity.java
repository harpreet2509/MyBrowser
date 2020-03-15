package studio.harpreet.mybrowser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    WebView mywebview;
    ProgressBar progressbar;

    EditText tooltext;
    String url = "https://www.google.com";

    private java.util.Timer Timers = new Timer();
    private double backpress = 0;
    private TimerTask Timer;

    Toolbar mToolbar;

    String mapbookurl,mapbookid,mapoptionsmenu;

    ArrayList<String> mapurllist = new ArrayList<>();
    ArrayList<String> mapidlist = new ArrayList<>();
    ArrayList<String> mapoptionslist = new ArrayList<>();


    DatabaseHelper mydb;
    ArrayList<HashMap<String,String>> userlist;

    String DownloadImageUrl;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        progressbar = findViewById(R.id.progressbar);

        mydb = new DatabaseHelper(this);

        if (getIntent().getExtras() != null) {
            url = getIntent().getStringExtra("urlkey");
        }

        mywebview = findViewById(R.id.webview);
        mywebview.loadUrl(url);



        tooltext = findViewById(R.id.toolbartext);
        setSupportActionBar(mToolbar);
        setTitle(null);

        WebSettings settings = mywebview.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDisplayZoomControls(false);
        settings.supportZoom();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);


        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        mywebview.clearHistory();
        mywebview.clearCache(true);

        mywebview.setWebChromeClient(new WebChromeClient());

        registerForContextMenu(mywebview);

        mywebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressbar.setVisibility(View.VISIBLE);
                tooltext.setText(mywebview.getUrl());
                invalidateOptionsMenu();
                final String Urls = url;

                if (Urls.contains("mailto:") || Urls.contains("sms:") || Urls.contains("tel:")) {
                    mywebview.stopLoading();
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(Urls));
                    startActivity(i);
                }

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressbar.setVisibility(View.GONE);
                invalidateOptionsMenu();
                tooltext.setText(mywebview.getUrl());
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        backpress = 1;
        Timer = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        backpress = 1;
                        Timer.cancel();
                    }
                });
            }
        };
        Timers.schedule(Timer, (int) (3000));
        backpress = 1;

        tooltext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                ValidateUrl(tooltext.getText().toString());

                return true;
            }
        });


        tooltext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();

                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (tooltext.getRight() - tooltext.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        startActivity(new Intent(MainActivity.this, Srchvw.class));

                        return true;
                    }
                }
                return false;

            }
        });

        mywebview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String useragent, String contentdisposition, String mimetype, long contentlength) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        {
                            DowloadDialog(url,useragent,contentdisposition,mimetype);
                        }
                        else
                        {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                        }

                    }
                    else
                    {
                        DowloadDialog(url,useragent,contentdisposition,mimetype);
                    }
            }
        });
    }

    public void DowloadDialog(final String url, final String UserAgent, String contentdisposition, String mimetype)
    {
        final String filename = URLUtil.guessFileName(url,contentdisposition,mimetype);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Downloading...")
                .setMessage("Do you want to Download "+ ' '+" "+filename+" "+' ')
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        String cookie = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("Cookie",cookie);
                        request.addRequestHeader("User-Agent",UserAgent);
                        request.allowScanningByMediaScanner();

                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                        DownloadManager manager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename);

                        manager.enqueue(request);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        final WebView.HitTestResult webviewHittestResult = mywebview.getHitTestResult();

        DownloadImageUrl = webviewHittestResult.getExtra();
        if(webviewHittestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                webviewHittestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
        {
            if(URLUtil.isNetworkUrl(DownloadImageUrl))
            {
                menu.setHeaderTitle("Download Image from Below");
                menu.add(0,1,0,"Download Image")
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int Permission_all = 1;
                                String Permission[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
                                if(!hasPermission(MainActivity.this,Permission))
                                {
                                    ActivityCompat.requestPermissions(MainActivity.this,Permission,Permission_all);
                                }
                                else
                                {
                                    String filename = "";
                                    String type = null;
                                    String Mimetype = MimeTypeMap.getFileExtensionFromUrl(DownloadImageUrl);
                                    filename = URLUtil.guessFileName(DownloadImageUrl,DownloadImageUrl,Mimetype);
                                    if(Mimetype!=null)
                                    {
                                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(Mimetype);

                                    }
                                    if(type==null)
                                    {
                                        filename = filename.replace(filename.substring(filename.lastIndexOf(".")),".png");
                                        type = "image/*";

                                    }
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageUrl));
                                    request.allowScanningByMediaScanner();

                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename);

                                    DownloadManager manager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                                    manager.enqueue(request);

                                    Toast.makeText(MainActivity.this, "Check Notification", Toast.LENGTH_SHORT).show();

                                }
                                return false;
                            }
                        });
                menu.add(0,2,0,"Copy Image Address").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        String copyimageurl = webviewHittestResult.getExtra();
                        ClipboardManager manager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label",copyimageurl);
                        manager.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                menu.add(0,3,0,"Share").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Picasso.get().load(DownloadImageUrl).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                Intent i = new Intent(Intent.ACTION_SEND);
                                i.setType("image/*");
                                i.putExtra(Intent.EXTRA_STREAM,getloacalBitmaUri(bitmap));
                                startActivity(Intent.createChooser(i,"Share Image"));
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
                        return false;
                    }
                });
            }
        }
        else
        {
            Toast.makeText(this, "Error Downloading", Toast.LENGTH_SHORT).show();
        }
    }

    public Uri getloacalBitmaUri(Bitmap bmp)
    {
        Uri bmpuri = null;
        try{
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"shareimage"+ System.currentTimeMillis()+".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG,90,out);
            out.close();
            bmpuri = FileProvider.getUriForFile(getApplicationContext(),"studio.harpreet.mybrowser.provider",file);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return bmpuri;
    }

    public static boolean hasPermission(Context context, String... permissions)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                context!=null && permissions!=null)
        {
            for(String permission : permissions)
            {
                if(ActivityCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public void addbookmark()
    {
        String title = mywebview.getTitle();
        String url = mywebview.getUrl();

        boolean isInserted = mydb.insertData(title,url);
        if(isInserted)
        {
            Toast.makeText(this, "Bookmarked", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Error adding Bookmark", Toast.LENGTH_SHORT).show();
        }
    }

    private void ValidateUrl(String url) {
        String prefix = "https://www.google.com/search?q=";

        if(!url.startsWith("http://") && !url.startsWith("https://")&&
        !url.endsWith(".com"))
        {
            url=prefix+url;
        }
        if(url.endsWith(".com") || url.endsWith(".as") || url.endsWith(".uk") || url.endsWith(".biz"))
        {
            if(!url.startsWith("http://") && !url.startsWith("https://"))
            {
                url = "https://"+url;
            }
        }
        mywebview.loadUrl(url);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        if(mywebview.canGoForward())
        {
            menu.findItem(R.id.Forward).setEnabled(true);
        }

        userlist = mydb.Showdata();
        for(HashMap msg : userlist)
        {
            HashMap hashmap = (HashMap)msg;
            mapoptionsmenu = (String)(hashmap.get("Url"));
            mapoptionslist.add(mapoptionsmenu);
        }
        if(mapoptionsmenu!=null)
        {
            if(mapoptionslist.contains(tooltext.getText().toString()))
            {
                menu.findItem(R.id.bookmark).setIcon(R.drawable.ic_bookmark_white_24dp);
                menu.findItem(R.id.bookmark).setTitle("Bookmarked");
            }
            else
            {
                menu.findItem(R.id.bookmark).setIcon(R.drawable.ic_bookmark_outline_white_24dp);
            }
        }
        else
        {
            menu.findItem(R.id.bookmark).setIcon(R.drawable.ic_bookmark_outline_white_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        invalidateOptionsMenu();
        if(id==R.id.reload)
        {
            mywebview.reload();
        }
        if(id==R.id.Forward)
        {
            if(mywebview.canGoForward())
            {
                mywebview.goForward();
            }
        }
        if(id==R.id.Bookmarks)
        {
            startActivity(new Intent(MainActivity.this,BookList.class));
        }
        if(id==R.id.bookmark)
        {
            userlist = mydb.Showdata();

            for(HashMap link : userlist)
            {
                HashMap hashmap = (HashMap)link;
                mapbookid = (String)hashmap.get("Id");
                mapbookurl = (String)hashmap.get("Url");
                mapidlist.add(mapbookid);
                mapurllist.add(mapbookurl);
            }
            if(mapbookurl!=null && mapbookid!=null)
            {
                if(mapurllist.contains(tooltext.getText().toString())) {
                    int val = mapurllist.indexOf(tooltext.getText().toString());

                    String val1 = mapidlist.get(val);
                    int parseval1 = Integer.parseInt(val1);

                    Integer delete = mydb.delete(String.valueOf(parseval1));

                    if (delete > 0) {
                        Toast.makeText(this, "Bookmark Removed", Toast.LENGTH_SHORT).show();
                        mapurllist.clear();
                        mapidlist.clear();
                        mapoptionslist.clear();
                        userlist = mydb.Showdata();
                        mydb.alter();
                    } else {
                        Toast.makeText(this, "Error Removing Bookmark", Toast.LENGTH_SHORT).show();
                        mapurllist.clear();
                        mapidlist.clear();
                        mapoptionslist.clear();
                    }
                }
                    else
                    {
                        mapurllist.clear();
                        mapidlist.clear();
                        mapoptionslist.clear();
                        addbookmark();
                        Toast.makeText(this, "Bookmarked", Toast.LENGTH_SHORT).show();
                    }
                }
            else
            {
                mapurllist.clear();
                mapidlist.clear();
                mapoptionslist.clear();
                addbookmark();
                Toast.makeText(this, "Bookmarked", Toast.LENGTH_SHORT).show();
            }
            invalidateOptionsMenu();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mywebview.canGoBack())
        {
            mywebview.goBack();
        }
        else
        {
            if(backpress == 1)
            {
                Toast.makeText(this, "Press Again to Exit", Toast.LENGTH_SHORT).show();
                Timer = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                backpress = 2;
                            }
                        });
                    }
                };
                Timers.schedule(Timer,(int)(0));
            }
            Timer = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            backpress = 1;
                        }
                    });
                }
            };
            Timers.schedule(Timer,(int)(3000));
            if(backpress==2)
            {
                finish();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        invalidateOptionsMenu();
    }
}




