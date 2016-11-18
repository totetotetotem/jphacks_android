package com.fresh_fridge.uthackers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class ItemAdapter extends ArrayAdapter<Item> {
    private LayoutInflater mInflater;

    ItemAdapter(Activity a) {
        super(a, android.R.layout.simple_list_item_1);
        mInflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    @NonNull
    public View getView(final int position, View contentView, @NonNull final ViewGroup parent) {
        final Item item;
        final Button deleteButton;
        final Call<ResultContainer> deleteCall;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://app.uthackers-app.tk")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        IFoodAPI foodAPI = retrofit.create(IFoodAPI.class);

        if (contentView == null) {
            contentView = mInflater.inflate(R.layout.list_item_layout, parent, false);
        }

        item = getItem(position);
        if (item != null) {
            TextView nameOfItem = (TextView) contentView.findViewById(R.id.nameOfItem);
            nameOfItem.setText(item.getItemName());

            TextView expirationDate = (TextView) contentView.findViewById(R.id.ExpirationDate);
            expirationDate.setText(item.getExpireDate());

            deleteCall = foodAPI.postDelete(MainActivity.getUserToken(), new ItemDeleteRequest(new Integer[]{item.getUserItemId()}));

            deleteButton = (Button) contentView.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setTitle("確認");
                    alertDialog.setMessage(item.getItemName() + "を削除しますか？");
                    alertDialog.setPositiveButton("削除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteCall.enqueue(new Callback<ResultContainer>() {
                                @Override
                                public void onResponse(Call<ResultContainer> call, Response<ResultContainer> response) {
                                    if (response.body() == null) {
                                        Log.d("retrofit", "failed to load data");
                                    }
                                    ResultContainer resultContainer = response.body();
                                    if (resultContainer == null || resultContainer.getMeta() == null) {
                                        Log.d("retrofit", "failed to load data");
                                        return;
                                    }
                                    if (resultContainer.getMeta().getStatus() != 200) {
                                        Log.d("retrofit", "failed to load data");
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResultContainer> call, Throwable t) {
                                    Log.d("retrofit", "failed to load data");
                                }
                            });
                            remove(getItem(position));
                        }
                    });
                    alertDialog.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.create().show();
                    // ((ListView) parent).performItemClick(v, position, (long)0);
                }
            });
        }
        return contentView;
    }
}
