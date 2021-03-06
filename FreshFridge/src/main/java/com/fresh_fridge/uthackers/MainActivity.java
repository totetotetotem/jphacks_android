package com.fresh_fridge.uthackers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.physicaloid.lib.Physicaloid;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.Math.abs;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener, SensorEventListener {
    private static final String ACTION_USB_PERMISSION =
            "com.fresh_fridge.uthackers.USB_PERMISSION";
    static private String userToken;
    private Physicaloid mSerial;
    private Handler mHandler = new Handler();
    private Handler reloadHandler = new Handler();
    private Call<ItemContainer> itemCall;
    private Call<TokenContainer> getTokenCall;
    private ItemAdapter mAdapter;
    private String familyToken;
    private WebView mWebView;
    private ListView mListView;
    private SharedPreferences mSharedPreferences;
    private TextToSpeech tts;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private Translate mTranslate;
    private GoogleApiClient client;
    private StringBuilder mText = new StringBuilder();
    private boolean mStop = false;
    private boolean mRunningMainLoop = false;
    private static final int MENU_QR_OPEN = 0;
    private static final int MENU_RELOAD_WEB = 1;
    private static final int MENU_EXPAND_WEBVIEW = 2;
    private static final int MENU_REDUCE_WEBVIEW = 3;
    private static final float SPEECH_PITCH = 1.0f;
    private static final float SPEECH_RATE = 1.0f;
    private boolean webViewExpanding = false;
    private String initialUrl = "";
    private long tmpTime;

    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int len;
            byte[] rbuf = new byte[4096];

            for (; ; ) {
                len = mSerial.read(rbuf);
                rbuf[len] = 0;

                if (len > 0) {
                    setSerialDataToTextView(rbuf, len);
                    mHandler.post(new Runnable() {
                        public void run() {
                            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                            if (!pm.isInteractive()) {
                                wakeFromSleep();
                            }
                            mText.setLength(0);
                        }
                    });

                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mStop) {
                    mRunningMainLoop = false;
                    return;
                }
            }
        }
    };

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if (!mSerial.isOpened()) {
                    openUsbSerial();
                }
                if (!mRunningMainLoop) {
                    mainloop();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                mStop = true;
                detachedUi();
                mSerial.close();
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (!mSerial.isOpened()) {
                        openUsbSerial();
                    }
                }
                if (!mRunningMainLoop) {
                    mainloop();
                }
            }
        }
    };

    public static String getUserToken() {
        return userToken;
    }

    @Override
    public void onInit(int status) {
        if (TextToSpeech.SUCCESS == status) {
            Log.d("TTS", "initialized");
        } else {
            Log.e("TTS", "faile to initialize");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // webView の設定
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("http://recipe.rakuten.co.jp");

        //data load
        mSharedPreferences = getSharedPreferences("token", MODE_PRIVATE);
        userToken = mSharedPreferences.getString("user_token", "NO_VALID_USER_TOKEN");
        familyToken = mSharedPreferences.getString("family_token", "NO_VALID_FAMILY_TOKEN");
        webViewExpanding = mSharedPreferences.getBoolean("webViewExpanding", false);

        //view enable
        mListView = (ListView) findViewById(R.id.foodlistView);
        mAdapter = new ItemAdapter(this);
        mListView.setAdapter(mAdapter);
        if (!webViewExpanding) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mListView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mListView.getLayoutParams();
            mlp.setMargins(20, 20, 20, 20);
            mListView.setLayoutParams(mlp);
            mWebView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mListView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
            mWebView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        }
        //http logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //set up retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://app.uthackers-app.tk")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        final ITokenAPI mTokenAPI = retrofit.create(ITokenAPI.class);
        IFoodAPI mFoodAPI = retrofit.create(IFoodAPI.class);

        getTokenCall = mTokenAPI.getToken(new PostBody());
        itemCall = mFoodAPI.getItemList(userToken);


        if (familyToken.equals("NO_VALID_FAMILY_TOKEN")) {
            getToken();
        }

        // get service
        mSerial = new Physicaloid(this);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        openUsbSerial();

        reloadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reloadFoods();
                reloadHandler.postDelayed(this, 30000);
            }
        }, 10000);

        tts = new TextToSpeech(this, this);
        tts.setSpeechRate(SPEECH_RATE);
        tts.setPitch(SPEECH_PITCH);
        tts.setLanguage(Locale.JAPAN);

        try {
            mTranslate = new Translate.Builder(AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(), null)
                    .setApplicationName(getString(R.string.app_name))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        reloadFoods();
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webViewExpanding) {
            mListView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
            mWebView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        }
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onDestroy() {
        mSerial.close();
        mStop = true;
        unregisterReceiver(mUsbReceiver);
        reloadHandler.removeCallbacksAndMessages(null);
        tts.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_QR_OPEN, Menu.NONE, "Show QR");
        menu.add(Menu.NONE, MENU_RELOAD_WEB, Menu.NONE, "Return to Search");
        menu.add(Menu.NONE, MENU_EXPAND_WEBVIEW, Menu.NONE, "expand recipe");
        menu.add(Menu.NONE, MENU_REDUCE_WEBVIEW, Menu.NONE, "reduce recipe");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        switch (item.getItemId()) {
            case MENU_QR_OPEN:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Intent intent = new Intent(MainActivity.this, com.fresh_fridge.uthackers.QRCodeActivity.class);
                intent.putExtra("familyToken", familyToken);
                startActivity(intent);
                return true;
            case MENU_RELOAD_WEB:
                mWebView.loadUrl(initialUrl);
                return true;
            case MENU_EXPAND_WEBVIEW:
                editor.putBoolean("webViewExpanding", true);
                editor.apply();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            case MENU_REDUCE_WEBVIEW:
                editor.putBoolean("webViewExpanding", false);
                editor.apply();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (abs(event.values[2]) > 4 && Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN).getTimeInMillis() - tmpTime > 15000) {
                //uncomment this if u use accelerometer to speak
                /*
                speakExpirationDate();
                tmpTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN).getTimeInMillis();
                */
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menu2 = menu.findItem(2);
        MenuItem menu3 = menu.findItem(3);
        menu2.setVisible(!webViewExpanding);
        menu3.setVisible(webViewExpanding);
        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //stub
    }

    private void reloadFoods() {
        itemCall.clone().enqueue(new Callback<ItemContainer>() {
            @Override
            public void onResponse(Call<ItemContainer> call, Response<ItemContainer> response) {
                if (response.body() == null) {
                    Toast.makeText(getApplicationContext(), R.string.Toast_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                ItemContainer mFoodContainer = response.body();
                Log.d("ItemContainer", response.message());
                Log.d("ItemContainer", response.body().toString());
                List<Item> items = mFoodContainer.getItems();

                mAdapter.clear();
                for (Item f : items) {
                    mAdapter.add(f);
                    if (f.getItemId() != null) {
                        Log.d("api_response/itemName", f.getItemName());
                        Log.d("api_response/itemId", f.getItemId().toString());
                    }
                }

                if (items.size() > 0) {
                    try {
                        if (mWebView.getOriginalUrl() == null || mWebView.getOriginalUrl().equals(initialUrl)) {
                            initialUrl = "http://recipe.rakuten.co.jp/search/" + URLEncoder.encode(items.get(0).getItemName(), "UTF-8");
                            mWebView.loadUrl(initialUrl);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                Item item = mAdapter.getItem(0);
                Log.d("expiredate", item.getExpireDateFromToday().toString());
            }

            @Override
            public void onFailure(Call<ItemContainer> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.Toast_failed, Toast.LENGTH_SHORT).show();
                Log.d("retrofit", "failed to load data");
            }
        });
    }

    private void mainloop() {
        mStop = false;
        mRunningMainLoop = true;
        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
        new Thread(mLoop).start();
    }

    void setSerialDataToTextView(byte[] rbuf, int len) {
        for (int i = 0; i < len; ++i) {
            mText.append((char) rbuf[i]);
        }
    }

    private void openUsbSerial() {
        if (mSerial == null) {
            Toast.makeText(this, "cannot open", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mSerial.isOpened()) {
            if (!mSerial.open()) {
                Toast.makeText(this, "cannot open", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
            }
        }

        if (!mRunningMainLoop) {
            mainloop();
        }

    }

    protected void onNewIntent(Intent intent) {
        openUsbSerial();
    }

    private void detachedUi() {
        Toast.makeText(this, "disconnect", Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        getTokenCall.enqueue(new Callback<TokenContainer>() {
                                 @Override
                                 public void onResponse(Call<TokenContainer> call, Response<TokenContainer> response) {
                                     if (response.body() == null) {
                                         Toast.makeText(getApplicationContext(), R.string.Toast_failed, Toast.LENGTH_SHORT).show();
                                         Log.d("retrofit", "failed to load data");
                                         return;
                                     }

                                     TokenContainer tokenContainer = response.body();
                                     try {
                                         familyToken = tokenContainer.getFamily().getToken();
                                         userToken = tokenContainer.getUser().getAccessToken();
                                         SharedPreferences.Editor editor = mSharedPreferences.edit();
                                         editor.putString("user_token", userToken);
                                         editor.putString("family_token", familyToken);
                                         editor.apply();
                                         Log.d("user_token", userToken);
                                         Log.d("family_token", familyToken);
                                     } catch (NullPointerException e) {
                                         Toast.makeText(getApplicationContext(), R.string.Toast_failed, Toast.LENGTH_SHORT).show();
                                         Log.d("retrofit", "failed to load data");
                                     }
                                 }

                                 @Override
                                 public void onFailure(Call<TokenContainer> call, Throwable t) {
                                     Toast.makeText(getApplicationContext(), R.string.Toast_failed, Toast.LENGTH_SHORT).show();
                                     Log.d("retrofit", "failed to load data");
                                 }
                             }

        );
    }

    public void wakeFromSleep() {
        PowerManager.WakeLock wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.FULL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE, "disableLock");
        wakeLock.acquire(1000);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        speakExpirationDate();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    private void speakExpirationDate() {
        Item item;
        try {
            item = mAdapter.getItem(0);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }
        if (item != null) {
            final Integer expireDateFromToday = item.getExpireDateFromToday();
            if (expireDateFromToday != null) {
                if (tts.getVoice().getLocale().equals(Locale.JAPAN)) {
                    String speakText = item.getItemName() + "の賞味期限は" + expireDateFromToday.toString() + "日後です";
                    speechText(speakText);
                } else {
                    try {
                        Translate.Translations.List list = mTranslate.new Translations().list(
                                Collections.singletonList(item.getItemName()),
                                "EN");
                        list.setKey(getApplicationContext().getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("api_key"));
                        Log.d("android Context", getApplicationContext().toString());
                        Log.d("android packageManager", getPackageManager().toString());
                        Log.d("android appinfo", getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).toString());
                        Log.d("android packname", getPackageName());
                        Log.d("android metaData", getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.toString());
                        Log.d("androidKey", getApplicationContext().getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("api_key"));
                        AsyncTask<Translate.Translations.List, Void, String> task = new AsyncTask<Translate.Translations.List, Void, String>() {
                            @Override
                            protected String doInBackground(Translate.Translations.List... list) {
                                try {
                                    TranslationsListResponse response = list[0].execute();
                                    String speakText = response.getTranslations().get(0).getTranslatedText();
                                    if (expireDateFromToday == 1) {
                                        speakText = speakText + " expires in a day";
                                    } else {
                                        speakText = speakText + " expires in" + expireDateFromToday.toString() + "days";
                                    }
                                    return speakText;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return "";
                            }

                            protected void onPostExecute(String result) {
                                speechText(result);
                            }
                        };
                        task.execute(list);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void speechText(String text) {
        if (text.length() > 0) {
            if (tts.isSpeaking()) {
                tts.stop();
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speak");
        }
    }
}

