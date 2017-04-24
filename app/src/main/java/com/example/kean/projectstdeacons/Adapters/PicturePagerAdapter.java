package com.example.kean.projectstdeacons.Adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.kean.projectstdeacons.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by kean on 4/22/17.
 */

public class PicturePagerAdapter extends PagerAdapter {
    private ArrayList<String> urls;
    private LayoutInflater inflater;
    private Context context;

    public PicturePagerAdapter(Context context, ArrayList<String> urls) {
        this.context = context;
        this.urls = urls;
        this.inflater = LayoutInflater.from(this.context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        container.removeView((View) object);
    };

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.image_slider, view, false);
        ImageView myImage = (ImageView) myImageLayout
                .findViewById(R.id.image);

        Picasso.with(context).load(urls.get(position)).placeholder(R.drawable.bike).into(myImage);

        view.addView(myImageLayout, 0);
        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
