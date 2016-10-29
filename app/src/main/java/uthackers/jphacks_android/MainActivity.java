package uthackers.jphacks_android;

import android.app.PendingIntent;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.preference.Preference;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity { //implements Runnable { 通信でなんとか
    private static final String ACTION_USB_PERMISSION =
            "uthackers.jphacks_android.USB_PERMISSION";
    Call<ItemContainer> itemCall;
    Call<TokenContainer> tokenCall;
    Call<TokenContainer> connectCall;
    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };
    //   int port = 22222;
    //   volatile Thread runner = null;
    private ItemAdapter mAdapter;
    private ListView mListView;
    //    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String familyToken;
    private String userToken;
    private WebView mWebView;
    //    private ServerSocket mServer;
//    private Socket mSocket;
    private UsbManager mUsbManager;
    private UsbDeviceConnection connection;
    private UsbDevice device;
    private UsbSerialDevice serialPort;
    private final BroadcastReceiver mUsbReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted =
                        intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = mUsbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else {
                detectUsb();
            }
        }

    };
    private SharedPreferences mSharedPreferences;

    public void detectUsb() {
        HashMap<String, UsbDevice> usbDevices = mUsbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();

                PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                        new Intent(ACTION_USB_PERMISSION), 0);
                mUsbManager.requestPermission(device, pi);
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        mWebView.loadUrl("http://recipe.rakuten.co.jp");

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://app.uthackers-app.tk")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        mSharedPreferences = getSharedPreferences("token", MODE_PRIVATE);
        userToken = mSharedPreferences.getString("user_token", "NO_VALID_USER_TOKEN");
        familyToken = mSharedPreferences.getString("family_token", "NO_VALID_FAMILY_TOKEN");

        IFoodAPI mFoodAPI = retrofit.create(IFoodAPI.class);
        itemCall = mFoodAPI.getItemList(userToken);

        ITokenAPI mTokenAPI = retrofit.create(ITokenAPI.class);
        tokenCall = mTokenAPI.getToken(new JSONObject());

        if (familyToken == "NO_VALID_FAMILY_TOKEN") {
            getToken();
        }

        mListView = (ListView) findViewById(R.id.foodlistView);
        mAdapter = new ItemAdapter(this);
        mListView.setAdapter(mAdapter);
        reloadFoods();

        /*
        if (runner == null) {
            runner = new Thread();
            runner.start();
        }
        */

        detectUsb();
    }

        /*
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListner);
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary);
        */


    /*
        private SwipeRefreshLayout.OnRefreshListener mOnRefreshListner = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadFoods();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
        };
    */
    @Override
    protected void onStart() {
        // reloadFoods();
        super.onStart();
    }


    public void reloadFoods() {
        itemCall.enqueue(new Callback<ItemContainer>() {
            @Override
            public void onResponse(Call<ItemContainer> call, Response<ItemContainer> response) {
                if (response.body() == null) {
                    Snackbar.make(mListView, R.string.snackbar_failed, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                ItemContainer mFoodContainer = response.body();
                Log.d("ItemContainer", response.message());
                Log.d("ItemContainer", response.body().toString());
                List<Item> items = mFoodContainer.getItems();

                for (Item f : items) {
                    mAdapter.add(f);
                    Log.d("api_response/itemName", f.getItemName());
                    Log.d("api_response/itemId", f.getItemId().toString());
                }


                try {
                    mWebView.loadUrl("http://recipe.rakuten.co.jp/search/" + URLEncoder.encode(items.get(0).getItemName(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ItemContainer> call, Throwable t) {
                Snackbar.make(mListView, R.string.snackbar_failed, Snackbar.LENGTH_SHORT).show();
                Log.d("retrofit", "failed to load data");
            }
        });

    }

    private void getToken() {
        tokenCall.enqueue(new Callback<TokenContainer>() {
                              @Override
                              public void onResponse(Call<TokenContainer> call, Response<TokenContainer> response) {
                                  if (response.body() == null) {
                                      Snackbar.make(mListView, R.string.snackbar_failed, Snackbar.LENGTH_SHORT).show();
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
                                  } catch (NullPointerException e) {
                                      Snackbar.make(mListView, R.string.snackbar_failed, Snackbar.LENGTH_SHORT).show();
                                      Log.d("retrofit", "failed to load data");
                                  }
                              }

                              @Override
                              public void onFailure(Call<TokenContainer> call, Throwable t) {
                                  Snackbar.make(mListView, R.string.snackbar_failed, Snackbar.LENGTH_SHORT).show();
                                  Log.d("retrofit", "failed to load data");
                              }
                          }

        );
    }


    /* TCP通信してデバイスと連携しようとしていた時の名残
    public void run() {
        try {
            mServer = new ServerSocket(port);
            mSocket = mServer.accept();

            BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            String message;
            while ((message = reader.readLine()) != null) {
                if (message == "SYN") {
                    //TODO なんかかく
                }
            }
        } catch (IOException e) {
            Snackbar.make(mListView, R.string.snackbar_failed, Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    */
}

