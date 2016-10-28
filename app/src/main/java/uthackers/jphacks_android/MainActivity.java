package uthackers.jphacks_android;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    Call<ItemContainer> call;
    private ItemAdapter mAdapter;
    private ListView mListView;
    //    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String familyId;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

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

        familyId = "1";

        IFoodAPI mFoodAPI = retrofit.create(IFoodAPI.class);
        call = mFoodAPI.getFoodInfoWithUrl(familyId);

        mListView = (ListView) findViewById(R.id.foodlistView);
        mAdapter = new ItemAdapter(this);
        mListView.setAdapter(mAdapter);
        reloadFoods();

        /*
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListner);
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary);
        */
    }


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
        call.enqueue(new Callback<ItemContainer>() {
            @Override
            public void onResponse(Call<ItemContainer> call, Response<ItemContainer> response) {
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

}

