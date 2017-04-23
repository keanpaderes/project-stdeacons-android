package com.example.kean.projectstdeacons.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kean.projectstdeacons.Application.BeaconRangingApp;
import com.example.kean.projectstdeacons.R;

import java.util.ArrayList;

/**
 * Created by kean on 4/15/17.
 */

public class BeaconListViewAdapter extends BaseAdapter {
    private Context currentContext;
    private LayoutInflater beaconInflater;
    private ArrayList<BeaconRangingApp.BeaconString> foundBeaconList;

    public BeaconListViewAdapter(Context context, ArrayList<BeaconRangingApp.BeaconString> arr){
        currentContext = context;
        foundBeaconList = arr;
        beaconInflater = (LayoutInflater) currentContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setBeaconList(ArrayList<BeaconRangingApp.BeaconString> updated){
        foundBeaconList = updated;
    }

    @Override
    public int getCount() {
        return foundBeaconList.size();
    }

    @Override
    public Object getItem(int position) {
        return foundBeaconList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vHolder;
        if(convertView == null){
            // Get view for row item
            convertView = beaconInflater.inflate(R.layout.list_beacon_item, parent, false);
            vHolder = new ViewHolder();
            vHolder.beaconNameText = (TextView) convertView.findViewById(R.id.beaconNameText);
            vHolder.beaconMajorText = (TextView) convertView.findViewById(R.id.beaconMajorText);
            vHolder.beaconMinorText = (TextView) convertView.findViewById(R.id.beaconMinorText);
            vHolder.beaconProximityText = (TextView) convertView.findViewById(R.id.beaconProximityText);
            convertView.setTag(vHolder);

        } else {
            vHolder = (ViewHolder) convertView.getTag();
        }
        BeaconRangingApp.BeaconString nbs =
                (BeaconRangingApp.BeaconString) getItem(position);
        vHolder.beaconNameText.setText(nbs.beaconName);
        vHolder.beaconMajorText.setText(nbs.beaconMajor);
        vHolder.beaconMinorText.setText(nbs.beaconMinor);
        vHolder.beaconProximityText.setText(nbs.beaconProximity);
        return convertView;
    }

    public static class ViewHolder {
        public TextView beaconNameText;
        public TextView beaconMajorText;
        public TextView beaconMinorText;
        public TextView beaconProximityText;
    }
}
