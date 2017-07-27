package ics.specialproject.kean.projectstdeacons.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.SystemRequirementsChecker;

import org.json.JSONException;
import org.json.JSONObject;

import ics.specialproject.kean.projectstdeacons.Adapters.BeaconListViewAdapter;
import ics.specialproject.kean.projectstdeacons.Adapters.LocationListViewAdapter;
import ics.specialproject.kean.projectstdeacons.Application.BeaconRangingApp;
import ics.specialproject.kean.projectstdeacons.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ics.specialproject.kean.projectstdeacons.Activities.SplashActivity.searchMap;
import static ics.specialproject.kean.projectstdeacons.Application.BeaconRangingApp.appBeacons;
import static ics.specialproject.kean.projectstdeacons.Application.BeaconRangingApp.appLocations;
import static ics.specialproject.kean.projectstdeacons.Application.BeaconRangingApp.inDev;
import static ics.specialproject.kean.projectstdeacons.Application.BeaconRangingApp.technique;

public class DevActivity extends AppCompatActivity {
    private BeaconListViewAdapter blvAdapter;
    private LocationListViewAdapter locAdapter;
    private Switch techniqueSwitch;
    private ListView beaconListView;
    private ListView locListView;
    public static Boolean isToAdd = false;
    public static Integer scanCount = 0;
    public static String currentLocation = "";
    public static ToggleButton rangingToggler;
    public static List<String> trustedBeacons =
            Arrays.asList("33345:3091"
                , "61539:34663"
                , "55656:22263");
    public static List<String> tBNames =
            Arrays.asList("Candy Floss"
                    , "Lemon Tart"
                    , "Sweet Beetroot");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev);

        initialize();
        rangingToggler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(!BeaconRangingApp.isPassed && BeaconRangingApp.setAdapter(blvAdapter)) {
                        BeaconRangingApp.isPassed = true;
                        BeaconRangingApp.beaconManager.startRanging(BeaconRangingApp.region);
                        // Adding request to log in request queue
                        BeaconRangingApp.getInstance().addToRequestQueue(startLogs());
                    }
                } else {
                    if(BeaconRangingApp.isPassed){
                        Toast.makeText(DevActivity.this, "Stopped", Toast.LENGTH_LONG).show();
                        if(BeaconRangingApp.destroyAdapter()) {
                            BeaconRangingApp.beaconManager.stopRanging(BeaconRangingApp.region);
                            BeaconRangingApp.isPassed = false;
                        }
                        if(isToAdd) {
                            String reqUrl = getResources().getText(R.string.server_url) +
                                    "/api/locations/specific?name=" + DevActivity.currentLocation +
                                    "&area=" + getResources().getText(R.string.area_id);
                            BeaconRangingApp.getInstance()
                                    .addToRequestQueue(getUpdatedLocation(reqUrl));
                            DevActivity.isToAdd = false;
                            DevActivity.currentLocation = "";
                        }
                    }
                }
            }
        });
        rangingToggler.setChecked(false);

        techniqueSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeaconRangingApp.technique = (techniqueSwitch.isChecked())? 1 : 0;
                Toast.makeText(getApplicationContext(), "Technique: " + BeaconRangingApp.technique,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onPause(){
        inDev = false;
        super.onPause();
    }

    @Override
    public void onBackPressed(){
        inDev = false;
        finish();
    }

    private void initialize(){
        blvAdapter = new BeaconListViewAdapter(DevActivity.this,
                new ArrayList<>(BeaconRangingApp.appBeacons.values()));
        locAdapter = new LocationListViewAdapter(DevActivity.this,
                new ArrayList<>(appLocations.values()));
        techniqueSwitch = (Switch) findViewById(R.id.techniqueSwitch);
        beaconListView = (ListView) findViewById(R.id.beaconListView);
        beaconListView.setAdapter(blvAdapter);
        rangingToggler = (ToggleButton) findViewById(R.id.devRangingToggler);
        locListView = (ListView) findViewById(R.id.locListView);
        locListView.setAdapter(locAdapter);
    }

    public JsonObjectRequest getUpdatedLocation(String reqUrl) {
        // Adding request to request queue
        return new JsonObjectRequest(reqUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    BeaconRangingApp.LocationString loc =
                            appLocations.get(response.getString("name"));

                    String[] points = {"A", "B", "C"};
                    Map<String, String> pointParams;
                    String proxString = "";

                    for(String point: points) {
                        if (response.has("point" + point)) {
                            String truePoint = "point" + point;
                            pointParams = new HashMap<>();
                            JSONObject pointInfo = response.getJSONObject(truePoint);

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
                                    loc.pointA = new JSONObject(pointParams);
                                    break;
                                case "pointB":
                                    loc.pointB = new JSONObject(pointParams);
                                    break;
                                case "pointC":
                                    loc.pointC = new JSONObject(pointParams);
                                    break;
                            }
                        }
                    }
                    appLocations.put(response.getString("name"), loc);

                    // ProxA,ProxB,ProxC : [location]
                    if(!searchMap.containsKey(proxString)) {
                        ArrayList<String> newArr = new ArrayList<>();
                        newArr.add(response.getString("name"));
                        searchMap.put(proxString, newArr);
                    } else {
                        searchMap.get(proxString).add(response.getString("name"));
                    }

                    locAdapter.setLocationList(new ArrayList<>(
                            appLocations.values()
                    ));

                    locAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    public StringRequest startLogs() {
        String reqUrl =  getResources().getText(R.string.server_url) + "/api/logs";
        return new StringRequest(Request.Method.POST,
                reqUrl, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(),
                        "Success!", Toast.LENGTH_LONG);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "Error!", Toast.LENGTH_LONG);
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                String output = (DevActivity.isToAdd)? "Scan Cycle: " + currentLocation
                        + "\n": "Scan Cycle\n";
                params.put("string", output);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
    }
}
