package ics.specialproject.kean.projectstdeacons.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ics.specialproject.kean.projectstdeacons.R;

import java.util.ArrayList;

/**
 * Created by kean on 4/23/17.
 */

public class DrawerListViewAdapter extends BaseAdapter {
    private Context aContext;
    private LayoutInflater aInflater;
    private ArrayList<String> aData;
//    private Integer[] icons = {
//            R.drawable.ic_favorite,
//            R.drawable.syphilis, R.drawable.gw, R.drawable.vd
//    };

    public DrawerListViewAdapter(Context context, ArrayList<String> items) {
        aContext = context;
        aData = items;
        aInflater = (LayoutInflater) aContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return aData.size();
    }

    @Override
    public String getItem(int position) {
        return aData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DrawerItem curr;

        if(convertView == null) {
            if(getItem(position).equals("header"))
                convertView = aInflater.inflate(R.layout.left_drawer_header, parent, false);
            else convertView = aInflater.inflate(R.layout.left_drawer_layout, parent, false);
            curr = new DrawerItem();
            curr.itemIcon = (ImageView) convertView.findViewById(R.id.drawerItemIcon);
            curr.itemInfo = (TextView) convertView.findViewById(R.id.drawerItemInfo);
            convertView.setTag(curr);
        } else {
            curr = (DrawerItem) convertView.getTag();
        }

        if(!getItem(position).equals("header")) {
            curr.itemInfo.setText(getItem(position));
//            curr.itemIcon.setImageDrawable(ContextCompat.getDrawable(aContext, icons[position]));
        }
        return convertView;
    }

    public static class DrawerItem {
        public ImageView itemIcon;
        public TextView itemInfo;
    }
}
