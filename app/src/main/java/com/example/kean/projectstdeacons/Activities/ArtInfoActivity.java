package com.example.kean.projectstdeacons.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.kean.projectstdeacons.Adapters.PicturePagerAdapter;
import com.example.kean.projectstdeacons.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class ArtInfoActivity extends AppCompatActivity {
    private static ViewPager mPager;
    private static int currentPage;
    private Boolean tgl = false;
    //Change this
    private static final Integer[] TEST =
            {R.drawable.bike, R.drawable.milky, R.drawable.thunder, R.drawable.trafficlight};
    private ArrayList<Integer> testArr = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
    }

    private void init() {
        for(Integer p:TEST){
            testArr.add(p);
        }
        final Drawable upArrow = ContextCompat.getDrawable(ArtInfoActivity.this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(ContextCompat.getColor(ArtInfoActivity.this, R.color.colorPrimary),
                PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(
                        ArtInfoActivity.this, R.color.colorAccent)));
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        mPager = (ViewPager) findViewById(R.id.imagePager);
        mPager.setPageMargin(0);
        mPager.setAdapter(new PicturePagerAdapter(ArtInfoActivity.this, testArr));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.imageIndicator);
        indicator.setViewPager(mPager);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.faveBtn);
        final LinearLayout artInfoMain = (LinearLayout) findViewById(R.id.artInfoMain);

        ScrollView scrollView = (ScrollView) findViewById(R.id.artInfoScrollView);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int[] ssPosition = new int[2];
                artInfoMain.getLocationOnScreen(ssPosition);
                if(ssPosition[1] < scrollY) {
                    fab.hide();
                } else fab.show();
            }
        });

        fab.bringToFront();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //must put sending fave here
                tgl = !tgl;
                if(tgl) {
                    fab.setImageDrawable(ContextCompat.getDrawable(
                            fab.getContext(), R.drawable.ic_favorite));
                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(
                            fab.getContext(), R.drawable.ic_favorite_border));
                }
            }
        });
    }

    @Override
    protected void onResume (){
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed(){
        finish();
    }
}
