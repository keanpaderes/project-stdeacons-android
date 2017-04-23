package com.example.kean.projectstdeacons.Activities;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.estimote.sdk.SystemRequirementsChecker;
import com.example.kean.projectstdeacons.Adapters.DrawerListViewAdapter;
import com.example.kean.projectstdeacons.Application.BeaconRangingApp;
import com.example.kean.projectstdeacons.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private ToggleButton rangingToggler;
    private TextView splashLogoText;
    private AlphaAnimation fadeIn;
    private DrawerLayout leftDrawerLayout;
    private ListView leftDrawerList;
    private ImageView splashLogo;
    private ImageView infoIcon;
    private ImageView listIcon;

    private CharSequence leftDrawerTitle;
    private CharSequence mTitle;
    private String[] placeholderArr = {
            "header",
            "Disease 1", "Disease 2", "Disease 3", "Disease 4", "Disease 5",
    };

    private int logoTapCount = 0;
    private Boolean isRanged = false;

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(SplashActivity.this, "Clicked: " +
                    placeholderArr[position], Toast.LENGTH_SHORT).show();
            //selectItem(position);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        init();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    public void init(){
        splashLogo = (ImageView) findViewById(R.id.splashLogo);
        infoIcon = (ImageView) findViewById(R.id.infoIcon);
        listIcon = (ImageView) findViewById(R.id.listIcon);
        rangingToggler = (ToggleButton) findViewById(R.id.splashRangingToggler);

        splashLogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(logoTapCount == 5) {
                    Toast.makeText(SplashActivity.this, "Dev Mode On", Toast.LENGTH_SHORT).show();
                    //check which beacons in trusted beacons that are found
                    rangingToggler.setVisibility(View.VISIBLE);
                    splashLogoText.setVisibility(View.INVISIBLE);
                } else ++logoTapCount;

                return true;
            }
        });

        infoIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                return true;
            }
        });

        listIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(getApplicationContext(), ArtInfoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                return true;
            }
        });

        rangingToggler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    BeaconRangingApp.beaconManager.startRanging(BeaconRangingApp.region);
                    if(!isRanged) isRanged = true;
                } else {
                    BeaconRangingApp.beaconManager.stopRanging(BeaconRangingApp.region);
                    if(isRanged){
                        logoTapCount = 0;
                        BeaconRangingApp.isSet = true;
                        Intent intent = new Intent(getApplicationContext(), DevActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }
        });
        rangingToggler.setChecked(false);

        mTitle = leftDrawerTitle = getTitle();
        leftDrawerLayout = (DrawerLayout) findViewById(R.id.splashDrawerLayout);
        leftDrawerList = (ListView) findViewById(R.id.leftAppDrawer);

        ArrayList<String> drawerItems = new ArrayList<>(Arrays.asList(placeholderArr));
        leftDrawerList.setAdapter(new DrawerListViewAdapter(this, drawerItems));
        leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        splashLogoText = (TextView) findViewById(R.id.splashLogoTxt);
        fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
        splashLogoText.setAnimation(fadeIn);
        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashLogoText.setVisibility(View.VISIBLE);
                splashLogoText.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        splashLogoText.startAnimation(fadeIn);
    }
}
