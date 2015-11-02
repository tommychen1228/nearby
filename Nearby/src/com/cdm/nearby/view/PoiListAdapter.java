package com.cdm.nearby.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.cdm.nearby.util.DistanceUtil;
import com.cdm.nearby.R;
import com.cdm.nearby.modal.Poi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/15/13
 * Time: 9:56 AM
 */
public class PoiListAdapter extends BaseAdapter{
    private List<Poi> pois = new ArrayList<Poi>();
    private Context context;
    private LayoutInflater layoutInflater;

    public PoiListAdapter(Context context) {
        this.context = context;

        layoutInflater = LayoutInflater.from(context);
    }

    public void addPois(List<Poi> appendPois){
        pois.addAll(appendPois);
    }

    public void addPoi(Poi poi){
        pois.add(poi);
    }

    public void clearPois(){
        pois.clear();
    }

    @Override
    public int getCount() {
        return pois.size();
    }

    @Override
    public Object getItem(int i) {
        return pois.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = layoutInflater.inflate(R.layout.poi_list_item, null);
        }

        Poi poi = pois.get(i);

        TextView nameTextView = (TextView)view.findViewById(R.id.nameTextView);
        TextView addressTextView = (TextView)view.findViewById(R.id.addressTextView);
        TextView distanceTextView = (TextView)view.findViewById(R.id.distanceTextView);

        nameTextView.setText(poi.getName());
        addressTextView.setText(poi.getAddress());
        distanceTextView.setText(DistanceUtil.convert(poi.getDistance()));

        return view;
    }
}
