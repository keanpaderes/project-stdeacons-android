package ics.specialproject.kean.projectstdeacons.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;

import java.util.ArrayList;

import ics.specialproject.kean.projectstdeacons.Activities.DevActivity;
import ics.specialproject.kean.projectstdeacons.Application.BeaconRangingApp;
import ics.specialproject.kean.projectstdeacons.R;

public class LocationListViewAdapter extends BaseAdapter{
    private Context currentContext;
    private LayoutInflater locationInflater;
    private ArrayList<BeaconRangingApp.LocationString> addedLocationList;

    public LocationListViewAdapter(Context context, ArrayList<BeaconRangingApp.LocationString> arr){
        currentContext = context;
        addedLocationList = arr;
        locationInflater = (LayoutInflater) currentContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setLocationList(ArrayList<BeaconRangingApp.LocationString> updated){
        addedLocationList = updated;
    }

    @Override
    public int getCount() {return addedLocationList.size();}

    @Override
    public Object getItem(int position) {return addedLocationList.get(position);}

    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LocationViewHolder vHolder;
        if(convertView == null){
            // Get view for row item
            convertView = locationInflater.inflate(R.layout.list_location_item, parent, false);
            vHolder = new LocationViewHolder();
            vHolder.locationNameText = (TextView) convertView.findViewById(R.id.locationNameText);
            vHolder.accPointAText = (TextView) convertView.findViewById(R.id.accPointAText);
            vHolder.accPointBText = (TextView) convertView.findViewById(R.id.accPointBText);
            vHolder.accPointCText = (TextView) convertView.findViewById(R.id.accPointCText);
            vHolder.locationRangeToggler = (ToggleButton) convertView.findViewById(R.id.locationRangeToggler);
            convertView.setTag(vHolder);

        } else {
            vHolder = (LocationViewHolder) convertView.getTag();
        }
        BeaconRangingApp.LocationString nls =
                (BeaconRangingApp.LocationString) getItem(position);
        vHolder.locationNameText.setText(nls.name);

        if(nls.pointA != null) {
            if(nls.pointA.has("accuracyRange")) {
                try {
                    vHolder.accPointAText.setText(nls.pointA.getString("accuracyRange"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    vHolder.accPointAText.setText("N\\A");
                }
            } else vHolder.accPointAText.setText("N\\A");
        } else vHolder.accPointAText.setText("N\\A");


        if(nls.pointB != null) {
            if(nls.pointB.has("accuracyRange")) {
                try {
                    vHolder.accPointBText.setText(nls.pointB.getString("accuracyRange"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    vHolder.accPointBText.setText("N\\A");
                }
            } else vHolder.accPointBText.setText("N\\A");
        } else vHolder.accPointBText.setText("N\\A");


        if(nls.pointC != null) {
            if(nls.pointC.has("accuracyRange")) {
                try {
                    vHolder.accPointCText.setText(nls.pointC.getString("accuracyRange"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    vHolder.accPointCText.setText("N\\A");
                }
            } else vHolder.accPointCText.setText("N\\A");
        } else vHolder.accPointCText.setText("N\\A");

        vHolder.locationRangeToggler.setTag(nls);
        vHolder.locationRangeToggler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BeaconRangingApp.LocationString locationString =
                        (BeaconRangingApp.LocationString) buttonView.getTag();

                if(isChecked) {
                    //check ranging toggler, get beacon readings and store it somewhere. then when finished send it all
                    DevActivity.isToAdd = true;
                    if(BeaconRangingApp.setToggler((ToggleButton) buttonView)) {
                        DevActivity.currentLocation = locationString.name;
                        Toast.makeText(currentContext, "Location: " +
                                locationString.name, Toast.LENGTH_LONG).show();
                        DevActivity.rangingToggler.setChecked(true);
                    }
                } else {
                    DevActivity.isToAdd = false;
                    DevActivity.currentLocation = "";
                    DevActivity.rangingToggler.setChecked(false);
                }
            }
        });
        return convertView;
    }

    private static class LocationViewHolder {
        TextView locationNameText;
        TextView accPointAText;
        TextView accPointBText;
        TextView accPointCText;
        ToggleButton locationRangeToggler;
    }
}
