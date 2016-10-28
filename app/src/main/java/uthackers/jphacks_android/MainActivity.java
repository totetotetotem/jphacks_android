package uthackers.jphacks_android;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    Call<FoodContainer> call;
    private FoodStuffAdapter mAdapter;
    private ListView mListView;
    //    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String familyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://app.uthackers-app.tk")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        familyId = "0";

        IFoodAPI mFoodAPI = retrofit.create(IFoodAPI.class);
        call = mFoodAPI.getFoodInfoWithUrl(familyId);

        mListView = (ListView) findViewById(R.id.foodlistView);

        mAdapter = new FoodStuffAdapter(this);

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
        reloadFoods();
        super.onStart();
    }


    public void reloadFoods() {
        call.enqueue(new Callback<FoodContainer>() {
            @Override
            public void onResponse(Call<FoodContainer> call, Response<FoodContainer> response) {
                FoodContainer mFoodContainer = response.body();
                List<Food> foods = mFoodContainer.getFoods();

                for (Food f : foods) {
                    mAdapter.add(f);
                }
            }

            @Override
            public void onFailure(Call<FoodContainer> call, Throwable t) {
                Snackbar.make(mListView, R.string.snackbar_failed, Snackbar.LENGTH_SHORT).show();
                Log.d("retrofit", "failed to load data");
            }
        });

    }

}

