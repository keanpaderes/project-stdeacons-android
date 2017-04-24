package com.example.kean.projectstdeacons.Activities;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.estimote.sdk.SystemRequirementsChecker;
import com.example.kean.projectstdeacons.Adapters.DrawerListViewAdapter;
import com.example.kean.projectstdeacons.Application.BeaconRangingApp;
import com.example.kean.projectstdeacons.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

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
    private ArrayList<String> drawerItems = new ArrayList<>();
    private AsyncHttpClient client = new AsyncHttpClient();

    private int logoTapCount = 0;
    private Boolean isRanged = false;

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
        splashLogoText = (TextView) findViewById(R.id.splashLogoTxt);

        splashLogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                if(logoTapCount == 5) {
//                    Toast.makeText(SplashActivity.this, "Dev Mode On", Toast.LENGTH_SHORT).show();
//                    //check which beacons in trusted beacons that are found
//                    rangingToggler.setVisibility(View.VISIBLE);
//                    splashLogoText.setVisibility(View.INVISIBLE);
//                } else ++logoTapCount;

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
//                Intent intent = new Intent(getApplicationContext(), ArtInfoActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);

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
        if(!drawerItems.contains("header")) drawerItems.add("header");

        client.get("http://192.168.1.6:8080/api/pieces/names", null, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // called when response HTTP status is "200 OK"
                for(int i = 0; i< response.length(); i++) {
                    try {
                        String temp = response.getJSONObject(i).getString("subjectFormal");
                        if(!drawerItems.contains(temp)) {
                            drawerItems.add(temp);
                        }
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
                ProgressBar loaded = (ProgressBar) findViewById(R.id.progressBar);
                loaded.setVisibility(View.GONE);
                splashLogo.setVisibility(View.VISIBLE);
                splashLogoText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Toast.makeText(SplashActivity.this, responseString, Toast.LENGTH_SHORT).show();
            }
        });
        leftDrawerList.setAdapter(new DrawerListViewAdapter(this, drawerItems));
        leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position != 0){
                String req = "http://192.168.1.6:8080/api/pieces/specific?id=" + position;
                client.get(req, null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        // called when response HTTP status is "200 OK"
                        Intent intent = new Intent(getApplicationContext(), ArtInfoActivity.class);
                        for(int i = 0; i< response.length(); i++) {
                            try {
                                intent.putExtra("subjectName", response.getJSONObject(i).getString("subjectName"));
                                intent.putExtra("subjectFormal", response.getJSONObject(i).getString("subjectFormal"));
                                intent.putExtra("isDisease", String.valueOf(response.getJSONObject(i).getBoolean("isDisease")));
                                intent.putExtra("faveCount", String.valueOf(response.getJSONObject(i).getInt("faveCount")));
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Toast.makeText(SplashActivity.this, responseString, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
