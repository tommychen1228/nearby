package com.cdm.nearby.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.cdm.nearby.R;
import com.cdm.nearby.modal.Category;
import com.cdm.nearby.modal.Location;
import com.cdm.nearby.modal.LocationManager;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/13/13
 * Time: 10:22 PM
 */
public class CategoryListAdapter extends BaseAdapter {

    private List<Category> categories;
    private Context context;
    private LayoutInflater layoutInflater;
    private LocationManager locationManager;

    CategoryListAdapter(Context ctx, List<Category> data) {
        context = ctx;
        categories = data;

        layoutInflater = LayoutInflater.from(context);
        locationManager = new LocationManager(context);
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int i) {
        return categories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = layoutInflater.inflate(R.layout.category_list_item, null);
        }

        final Category category = categories.get(i);

        TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        nameTextView.setText(category.getName());

        Button nextButton = (Button) view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CategoryListActivity.class);
                intent.putExtra("code", category.getCode());
                context.startActivity(intent);
            }
        });
        if(category.hasChildren()){
            nextButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(View.INVISIBLE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location myLocation = locationManager.getLastLocation();
                if(myLocation == null){
                    Toast.makeText(context, "定位中,请稍等...", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(context, PoiListActivity.class);
                intent.putExtra("code", category.getCode());
                context.startActivity(intent);
            }
        });
        return view;
    }
}
