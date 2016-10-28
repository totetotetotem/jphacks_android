package uthackers.jphacks_android;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by totetotetotem on 2016/10/24.
 */
class ItemAdapter extends ArrayAdapter<Item> {
    private LayoutInflater mInflater;


    ItemAdapter(Activity a) {
        super(a, android.R.layout.simple_list_item_1);
        mInflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    @NonNull
    public View getView(final int position, View contentView, @NonNull ViewGroup parent) {
        if (contentView == null) {
            contentView = mInflater.inflate(R.layout.list_item_layout, parent, false);
        }
        final Item item = getItem(position);
        try {
            TextView nameOfItem = (TextView) contentView.findViewById(R.id.nameOfItem);
            nameOfItem.setText(item.getItemName());
            //           TextView numberOfItem = (TextView) contentView.findViewById(R.id.numberOfItem);
//            numberOfItem.setText(item.getItemId());

            TextView expirationDate = (TextView) contentView.findViewById(R.id.ExpirationDate);
            expirationDate.setText(item.getExpireDate());

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return contentView;
    }

}
