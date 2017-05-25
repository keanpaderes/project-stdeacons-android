package ics.specialproject.kean.projectstdeacons.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.estimote.sdk.SystemRequirementsChecker;
import ics.specialproject.kean.projectstdeacons.Adapters.DrawerListViewAdapter;
import ics.specialproject.kean.projectstdeacons.Application.BeaconRangingApp;
import ics.specialproject.kean.projectstdeacons.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ics.specialproject.kean.projectstdeacons.Application.BeaconRangingApp.appLocations;

public class SplashActivity extends AppCompatActivity {
    private static String TAG = SplashActivity.class.getSimpleName();
    private ToggleButton rangingToggler;
    private TextView splashLogoText;
    private ImageView splashLogo;

    private static ArrayList<String> drawerItems = new ArrayList<>();
    //minMaj, loc; the costly solution
    public static HashMap<String, Integer> locationHash = new HashMap<>();
    // ProxA,ProxB,ProxC : [location]
    public static HashMap<String, ArrayList<String>> searchMap = new HashMap<>();
    //all locations
    public static ArrayList<String> locationArrayList = new ArrayList<>();

    private int logoTapCount = 0;
    public static Boolean isRanged = false;
    public static String currentIdentification = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this, R.style.Theme_AppCompat_Dialog_Alert);
        alertDialog.setTitle("Identification");

        alertDialog.setMessage("Enter your name. (Optional)");
        final EditText input = new EditText(SplashActivity.this);
        input.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input); // uncomment this line

        alertDialog.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                SplashActivity.currentIdentification = input.getText().toString();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                SplashActivity.currentIdentification = UUID.randomUUID().toString();
            }
        });

        AlertDialog b = alertDialog.create();
        b.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        init();
        if(isRanged) {
            BeaconRangingApp.beaconManager.startRanging(BeaconRangingApp.region);
        }
    }

    @Override
    protected void onPause(){
        BeaconRangingApp.beaconManager.stopRanging(BeaconRangingApp.region);
        super.onPause();
    }

    @Override
    public void onBackPressed(){
        BeaconRangingApp.beaconManager.stopRanging(BeaconRangingApp.region);
        finish();
    }

    public void init(){
        ImageView infoIcon = (ImageView) findViewById(R.id.infoIcon);
        ImageView listIcon = (ImageView) findViewById(R.id.listIcon);
        splashLogo = (ImageView) findViewById(R.id.splashLogo);
        rangingToggler = (ToggleButton) findViewById(R.id.splashRangingToggler);
        splashLogoText = (TextView) findViewById(R.id.splashLogoTxt);

        splashLogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(logoTapCount == 10) {
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
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.splashDrawerLayout);
                if(drawer.isDrawerOpen(Gravity.START)) {
                    drawer.closeDrawer(Gravity.START);
                } else drawer.openDrawer(Gravity.START);
                return true;
            }
        });

        rangingToggler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    logoTapCount = 0;
                    if(isRanged){
                        //check which beacons in trusted beacons that are found
                        rangingToggler.setVisibility(View.INVISIBLE);
                        splashLogoText.setVisibility(View.VISIBLE);
                        BeaconRangingApp.inDev = true;
                        Intent intent = new Intent(getApplicationContext(), DevActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }
        });
        rangingToggler.setChecked(false);
        ListView leftDrawerList = (ListView) findViewById(R.id.leftAppDrawer);
        if(!SplashActivity.drawerItems.contains("header")) SplashActivity.drawerItems.add("header");
        if(SplashActivity.drawerItems.size() <= 1)
            //Initialize app by getting names and adding locations
            BeaconRangingApp.getInstance().addToRequestQueue(initializeApp());
        else {
            ProgressBar loaded = (ProgressBar) findViewById(R.id.progressBar);
            loaded.setVisibility(View.GONE);
            splashLogo.setVisibility(View.VISIBLE);
            splashLogoText.setVisibility(View.VISIBLE);
        }
        leftDrawerList.setAdapter(new DrawerListViewAdapter(this, SplashActivity.drawerItems));
        leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position != 0){
                // Adding request to request queue
                BeaconRangingApp.getInstance().addToRequestQueue(getArtpiece(position));
            }
        }
    }

    public JsonArrayRequest getArtpiece(int position) {
        String reqUrl = getResources().getText(R.string.server_url) +
                "/api/pieces/specific?id=" + position;

        return new JsonArrayRequest(reqUrl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        try {
                            Intent intent = new Intent(getApplicationContext(),
                                    ArtInfoActivity.class);
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject resp = (JSONObject) response
                                        .get(i);
                                intent.putExtra("subjectName",
                                        resp.getString("subjectName"));
                                intent.putExtra("subjectFormal",
                                        resp.getString("subjectFormal"));
                                intent.putExtra("faveCount",
                                        String.valueOf(resp.getInt("faveCount")));

                            }

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public JsonObjectRequest initializeApp() {
        return new JsonObjectRequest(getResources().getText(R.string.server_url) +
                "/api/areas/init?area=" + getResources().getText(R.string.area_id).toString(), null,
                new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray names = response.getJSONArray("names");
                    JSONArray locations = response.getJSONArray("locations");

                    //Parsing through names
                    Log.d(TAG, names.toString());
                    for (int i = 0; i < names.length(); i++) {
                        JSONObject name = (JSONObject) names.get(i);
                        String temp = name.getString("subjectFormal");
                        if(!SplashActivity.drawerItems.contains(temp)) {
                            SplashActivity.drawerItems.add(temp);
                        }
                    }
                    ProgressBar loaded = (ProgressBar) findViewById(R.id.progressBar);
                    loaded.setVisibility(View.GONE);
                    splashLogo.setVisibility(View.VISIBLE);
                    splashLogoText.setVisibility(View.VISIBLE);

                    //If locations is not empty
                    if(locations.length() > 0) {
                        for (int i = 0; i < locations.length(); i++) {
                            JSONObject location = (JSONObject) locations.get(i);
                            String temp = location.getString("name");
                            //Location is still not stored in the appLocations hash
                            if(!appLocations.keySet().contains(temp)) {
                                BeaconRangingApp.LocationString currL =
                                        new BeaconRangingApp.LocationString();
                                currL.name = temp;

                                /*
                                *   ADD FOR ACCURACY, PROXIMITY
                                *        AND RSSI
                                */
                                String[] points = {"A", "B", "C"};
                                Map<String, String> pointParams;
                                String proxString = "";

                                for(String point: points) {
                                    if (location.has("point" + point)) {
                                        String truePoint = "point"+point;
                                        pointParams = new HashMap<>();
                                        JSONObject pointInfo = location.getJSONObject(truePoint);

                                        if(pointInfo.has("name"))
                                            pointParams.put("name", pointInfo.getString("name"));
                                        if(pointInfo.has("optimizedAccuracy"))
                                            pointParams.put("optimizedAccuracy",
                                                    pointInfo.getString("optimizedAccuracy"));
                                        if(pointInfo.has("accuracyRange"))
                                            pointParams.put("accuracyRange",
                                                    pointInfo.getString("accuracyRange"));
                                        if(pointInfo.has("estimatedProximity"))
                                            pointParams.put("estimatedProximity",
                                                    pointInfo.getString("estimatedProximity"));
                                        if(pointInfo.has("rssiRange"))
                                            pointParams.put("rssiRange",
                                                    pointInfo.getString("rssiRange"));

                                        switch(truePoint) {
                                            case "pointA":
                                                proxString +=
                                                    pointInfo.getString("estimatedProximity") + ",";
                                                currL.pointA = new JSONObject(pointParams);
                                                break;
                                            case "pointB":
                                                proxString +=
                                                    pointInfo.getString("estimatedProximity") + ",";
                                                currL.pointB = new JSONObject(pointParams);
                                                break;
                                            case "pointC":
                                                proxString +=
                                                    pointInfo.getString("estimatedProximity");
                                                currL.pointC = new JSONObject(pointParams);
                                                break;
                                        }
                                    }
                                }
                                appLocations.put(temp, currL);

                                // ProxA,ProxB,ProxC : [location]
//                                if(!searchMap.containsKey(proxString)) {
//                                    Toast.makeText(getApplicationContext(), proxString,
//                                            Toast.LENGTH_LONG).show();
//                                    ArrayList<String> newArr = new ArrayList<>();
//                                    newArr.add(temp);
//                                    searchMap.put(proxString, newArr);
//                                } else {
//                                    searchMap.get(proxString).add(temp);
//                                }

                                locationArrayList.add(temp);
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Locations added.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        //No locations found for area
                        Toast.makeText(getApplicationContext(), "No locations registered.",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
