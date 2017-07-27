package ics.specialproject.kean.projectstdeacons.Application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ics.specialproject.kean.projectstdeacons.Activities.ArtInfoActivity;
import ics.specialproject.kean.projectstdeacons.Activities.DevActivity;
import ics.specialproject.kean.projectstdeacons.Activities.SplashActivity;
import ics.specialproject.kean.projectstdeacons.Adapters.BeaconListViewAdapter;
import ics.specialproject.kean.projectstdeacons.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ics.specialproject.kean.projectstdeacons.Activities.DevActivity.scanCount;
import static ics.specialproject.kean.projectstdeacons.Activities.DevActivity.tBNames;
import static ics.specialproject.kean.projectstdeacons.Activities.DevActivity.trustedBeacons;

public class BeaconRangingApp extends Application {
    public static final String TAG = BeaconRangingApp.class.getSimpleName();
    public static Integer RANGE_SIZE = 10;
    public static Integer SCAN_SIZE = 30;

    private RequestQueue rq;
    private static BeaconRangingApp appInstance;
    public static BeaconManager beaconManager;
    public static Region region;
    public static Boolean isSet = false;
    public static Boolean isPassed = false;
    public static Boolean inDev = false;
    public static Boolean isLocated = false;
    public static Integer technique = 0;
    public static HashMap<String, BeaconString> appBeacons
            = new HashMap<>();
    public static HashMap<String, LocationString> appLocations
            = new HashMap<>();
    public static BeaconListViewAdapter passedAdapter;
    public static ToggleButton passedToggler;
    public static HashMap<String, HashMap<String, ArrayList<String>>> lastKnownLocation =
            new HashMap<>();

    public static class BeaconString {
        //Create db of this
        public String beaconName;
        public String beaconMajor;
        public String beaconMinor;
        public String beaconPower;
        public String beaconRSSI;
        public String beaconProximity;
        public String beaconAccuracy;
    }

    public static class LocationString {
        public String name;
        public JSONObject pointA;
        public JSONObject pointB;
        public JSONObject pointC;
    }

    private class MutableInt {
        int value = 1; // note that we start at 1 since we're counting
        void increment () { ++value;      }
         int  get ()       { return value; }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        beaconManager = new BeaconManager(getApplicationContext());
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if(!list.isEmpty()) {
                    for(Beacon b : list){
                        String majorMinor = b.getMajor()+":"+b.getMinor();
                        int id = trustedBeacons.indexOf(majorMinor);

                        //Checks if it is trusted
                        if(trustedBeacons.contains(majorMinor)){
                            //If it is not in the hashmap yet
                            if(!appBeacons.keySet().contains(majorMinor))
                                addToCurrentBeaconHash(b, majorMinor, id);
                            else {
                                Log.v("CHK", Utils.computeProximity(b).toString());

                                //b majorMinor id
                                updateBeaconStatus(b, majorMinor, isPassed);

                                if(inDev && isPassed) {
                                    //Send beacon data
                                    if(DevActivity.isToAdd) {
                                        sendBeaconValues(appBeacons.get(majorMinor));
                                    }
                                    //For logs
                                    sendLogs(appBeacons.get(majorMinor));
                                }

                                if(technique == 1) {
                                    if(Utils.computeProximity(b).equals(Utils.Proximity.IMMEDIATE) && !inDev) {
                                        if(!isLocated) {
                                            BeaconRangingApp.isLocated = true;
                                        } else {
                                            ArtInfoActivity.runningActivity.finish();
                                            BeaconRangingApp.isLocated = false;
                                        }
                                        Log.v("CHK", "NEAR");
                                        beaconManager.stopRanging(region);
                                        String reqUrl = getResources().getText(R.string.server_url)
                                                + "/api/pieces/specific?id=" + SplashActivity.locationHash.get(
                                                list.get(0).getMajor()+":"+list.get(0).getMinor()
                                        );
                                        Toast.makeText(getApplicationContext(), reqUrl, Toast.LENGTH_LONG).show();
                                        JsonArrayRequest req = new JsonArrayRequest(reqUrl,
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
//                                                            if(isLocated) ((Activity) getApplicationContext()).finish();
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
                                                Log.d(TAG, "Error: " + error.getMessage());
                                                Toast.makeText(getApplicationContext(),
                                                        error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        // Adding request to request queue
                                        BeaconRangingApp.getInstance().addToRequestQueue(req);
                                    }
                                }
                            }
                        }
                    }

                    if(inDev && isPassed) {
                        if(DevActivity.scanCount > SCAN_SIZE) {
                            if(BeaconRangingApp.destroyAdapter()) {
                                DevActivity.rangingToggler.setChecked(false);
                                BeaconRangingApp.beaconManager.stopRanging(BeaconRangingApp.region);
                                BeaconRangingApp.isPassed = false;
                                if(DevActivity.isToAdd) {
                                    passedToggler.setChecked(false);
                                    if(BeaconRangingApp.destroyToggler()){
                                        DevActivity.isToAdd = false;
                                        DevActivity.currentLocation = "";
                                    }
                                }
                                DevActivity.scanCount = 0;
                            }
                        } else {
                            scanCount++;
                        }
                    }

                    if(!SplashActivity.isRanged && appBeacons.keySet().size() >= 3) {
                        Toast.makeText(getApplicationContext(), "Beacons detected.",
                                Toast.LENGTH_LONG).show();
                        SplashActivity.isRanged = true;
                        isSet = true;
                    }
                    if(!inDev && technique == 0) {
                        if(checkRanging()) {
                            //The app will estimate the proximity
                            String proximity = getEstimatedProximity();
                            Boolean isFound = false;
                            String locString = "";
                            //Optimized
                            Double[] loc = {
                                    getAveragedAccuracy(
                                            lastKnownLocation.get("pointA").get("accuracyArray")),
                                    getAveragedAccuracy(
                                            lastKnownLocation.get("pointB").get("accuracyArray")),
                                    getAveragedAccuracy(
                                            lastKnownLocation.get("pointC").get("accuracyArray"))
                            };

                            //The app will check Accuracy ranges
                            for(String location: SplashActivity.locationArrayList) {
                                LocationString ls = appLocations.get(location);
                                if(ls.pointA != null && ls.pointB != null && ls.pointC != null) {
                                    Toast.makeText(getApplicationContext(),
                                            "At location? " + (checkIfAtLocation(loc,
                                                    generateRanges(ls))),Toast.LENGTH_LONG).show();
                                    if(checkIfAtLocation(loc, generateRanges(ls))) {
                                        Toast.makeText(getApplicationContext(),
                                                "Ranged at " + location + "", Toast.LENGTH_LONG).show();
                                        isFound = true;
                                        locString = ls.name;



                                        beaconManager.stopRanging(region);
                                        String reqUrl = getResources().getText(R.string.server_url)
                                                + "/api/pieces/specific?id=" + (SplashActivity
                                                .locationArrayList.indexOf(location)+1);
                                        JsonArrayRequest req = new JsonArrayRequest(reqUrl,
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
                                                Log.d(TAG, "Error: " + error.getMessage());
                                                Toast.makeText(getApplicationContext(),
                                                        error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        // Adding request to request queue
                                        BeaconRangingApp.getInstance().addToRequestQueue(req);
                                    }


                                    sendRangeLogs(proximity, Arrays.asList(loc), (isFound?
                                            locString: String.valueOf(isFound)));
                                }
                            } // Ga-graduate ka Kean. Tiwala lang

                            resetValues();
                        }
                    }
                }
            }
        });
        beaconManager.setForegroundScanPeriod(7000, 3000);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                Toast.makeText(getApplicationContext(), "Beacon Ranging ready.", Toast.LENGTH_SHORT).show();
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    public void onTerminate(){
        Toast.makeText(getApplicationContext(), "Beacon Ranging stopped.", Toast.LENGTH_SHORT).show();
        beaconManager.disconnect();
        super.onTerminate();
    }

    public static Boolean setToggler(ToggleButton btn){
        passedToggler = btn;
        return true;
    }

    public static Boolean destroyToggler(){
        passedToggler = null;
        return true;
    }

    public static Boolean setAdapter(BeaconListViewAdapter passed){
        passedAdapter = passed;
        return true;
    }

    public static Boolean destroyAdapter(){
        passedAdapter = null;
        return true;
    }

    public static synchronized BeaconRangingApp getInstance() {
        return appInstance;
    }

    public RequestQueue getRequestQueue() {
        if(rq == null) {
            rq = Volley.newRequestQueue(getApplicationContext());
        }
        return rq;
    }

    public <T> void  addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public Double getAveragedAccuracy(ArrayList<String> arr) {
        Double highestTally = reduceHighestTally(tallyCount(arr));
        return getOptimizedAverage(filterArray(arr, highestTally));
    }

    public Double getOptimizedAverage (ArrayList<Double> arr) {
        Double added = 0.0;
        for(Double d:arr) {
            added += d;
        }
        return added / (double) arr.size();
    }

    public ArrayList<Double> filterArray (ArrayList<String> arr, Double highestTally) {
        ArrayList<Double> retArr = new ArrayList<>();

        for(String d:arr){
            Double dbl = Double.valueOf(d);
            if((Math.ceil(dbl) > (highestTally/2)) &&
                    (Math.floor(dbl) < (highestTally*2)))
                retArr.add(dbl);
        }
        return retArr;
    }

    public Double reduceHighestTally(HashMap<Double, MutableInt> map) {
        Double retVal = (Double) map.keySet().toArray()[0];

        for(Double d : map.keySet()) {
            if(map.get(d).get() > map.get(retVal).get())
                retVal = d;
        }

        return retVal;
    }

    public HashMap<Double, MutableInt> tallyCount(ArrayList<String> arr) {
        HashMap<Double, MutableInt> retMap = new HashMap<>();

        for(String doub: arr) {
            Double floored = Math.floor(Double.valueOf(doub));
            if(!retMap.keySet().contains(floored)) {
                retMap.put(floored, new MutableInt());
            } else {
                retMap.get(floored).increment();
            }
        }
        return retMap;
    }

    public String getEstimatedProximity() {
        return proximityEstimation(lastKnownLocation.get("pointA").get("proximity")) +
                "," + proximityEstimation(lastKnownLocation.get("pointB").get("proximity")) +
                "," + proximityEstimation(lastKnownLocation.get("pointC").get("proximity"));
    }

    public String proximityEstimation(ArrayList<String> arr) {
        HashMap<String, MutableInt> tally = new HashMap<>();

        for(String a : arr) {
            MutableInt count = tally.get(a);
            if(count == null)
                tally.put(a, new MutableInt());
            else count.increment();
        }

        String retString = arr.get(0);
        for(String vals : tally.keySet()) {
            if(tally.get(retString).get() < tally.get(vals).get())
                retString = vals;
        }
        return retString;
    }

    public Boolean checkRanging() {
        if(lastKnownLocation.containsKey("pointA") &&
            lastKnownLocation.containsKey("pointB") &&
            lastKnownLocation.containsKey("pointC")) {
                return (lastKnownLocation.get("pointA").get("accuracyArray").size() >= RANGE_SIZE) &&
                        (lastKnownLocation.get("pointB").get("accuracyArray").size() >= RANGE_SIZE) &&
                        (lastKnownLocation.get("pointC").get("accuracyArray").size() >= RANGE_SIZE) &&

                        (lastKnownLocation.get("pointA").get("proximity").size() >= RANGE_SIZE) &&
                        (lastKnownLocation.get("pointB").get("proximity").size() >= RANGE_SIZE) &&
                        (lastKnownLocation.get("pointC").get("proximity").size() >= RANGE_SIZE);
        } else return false;
    }

    public Boolean checkIfAtLocation(Double[] loc, ArrayList<Double[]> ranges) {
        return ((loc[0] >= ranges.get(0)[0] && loc[0] < ranges.get(0)[1]) &&
                (loc[1] >= ranges.get(1)[0] && loc[1] < ranges.get(1)[1]) &&
                (loc[2] >= ranges.get(2)[0] && loc[2] < ranges.get(2)[1]));
    }

    public ArrayList<Double[]> generateRanges(LocationString ls) {
        ArrayList<Double[]> retArray = new ArrayList<>();
        try {
            String[] rangeA =
                    ls.pointA.getString("accuracyRange").split(",");
            String[] rangeB =
                    ls.pointB.getString("accuracyRange").split(",");
            String[] rangeC =
                    ls.pointC.getString("accuracyRange").split(",");

            Double[] rangesA = {Double.valueOf(rangeA[0]), Double.valueOf(rangeA[1])};
            Double[] rangesB = {Double.valueOf(rangeB[0]), Double.valueOf(rangeB[1])};
            Double[] rangesC = {Double.valueOf(rangeC[0]), Double.valueOf(rangeC[1])};

            retArray.add(rangesA);
            retArray.add(rangesB);
            retArray.add(rangesC);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retArray;
    }

    public void resetValues() {
        lastKnownLocation.get("pointA").get("accuracyArray").clear();
        lastKnownLocation.get("pointB").get("accuracyArray").clear();
        lastKnownLocation.get("pointC").get("accuracyArray").clear();

        lastKnownLocation.get("pointA").get("proximity").clear();
        lastKnownLocation.get("pointB").get("proximity").clear();
        lastKnownLocation.get("pointC").get("proximity").clear();
    }

    public void addToCurrentBeaconHash(Beacon b, String majorMinor, Integer id){
        BeaconString currB = new BeaconString();
        currB.beaconName = tBNames.get(id);
        currB.beaconMajor = String.valueOf(b.getMajor());
        currB.beaconMinor = String.valueOf(b.getMinor());
        currB.beaconPower = String.valueOf(b.getMeasuredPower());
        currB.beaconRSSI = String.valueOf(b.getRssi());
        currB.beaconProximity = String.valueOf(Utils.computeProximity(b));
        currB.beaconAccuracy = String.valueOf(Utils.computeAccuracy(b));
        appBeacons.put(majorMinor, currB);

        HashMap<String, ArrayList<String>> values = new HashMap<>();
//        values.put("accuracy", new ArrayList<String>());
        values.put("accuracyArray", new ArrayList<String>());
        values.put("proximity", new ArrayList<String>());
//        values.get("accuracy").add(currB.beaconAccuracy);

        switch(currB.beaconName) {
            case "Candy Floss":
                SplashActivity.locationHash.put(majorMinor, 1);
                lastKnownLocation.put("pointA", values);
                break;
            case "Lemon Tart":
                SplashActivity.locationHash.put(majorMinor, 2);
                lastKnownLocation.put("pointB", values);
                break;
            case "Sweet Beetroot":
                SplashActivity.locationHash.put(majorMinor, 3);
                lastKnownLocation.put("pointC", values);
                break;
        }
    }

    public void updateBeaconStatus(Beacon b, String majorMinor, Boolean isPassed) {
        appBeacons.get(majorMinor).beaconProximity =
                String.valueOf(Utils.computeProximity(b));

        appBeacons.get(majorMinor).beaconAccuracy =
                String.valueOf(Utils.computeAccuracy(b));

        appBeacons.get(majorMinor).beaconPower =
                String.valueOf(b.getMeasuredPower());

        appBeacons.get(majorMinor).beaconRSSI =
                String.valueOf(b.getRssi());

        String accuracy = appBeacons.get(majorMinor).beaconAccuracy,
            proximity = appBeacons.get(majorMinor).beaconProximity;
        switch(appBeacons.get(majorMinor).beaconName) {
            //ADD OPTIMIZATION
            case "Candy Floss":
//                lastKnownLocation.get("pointA").get("accuracy").clear();
//                lastKnownLocation.get("pointA").get("accuracy").add(accuracy);

                if(lastKnownLocation.get("pointA").get("accuracyArray").size() < RANGE_SIZE)
                    lastKnownLocation.get("pointA").get("accuracyArray").add(accuracy);

                if(lastKnownLocation.get("pointA").get("proximity").size() < RANGE_SIZE)
                    lastKnownLocation.get("pointA").get("proximity").add(proximity);
                break;
            case "Lemon Tart":
//                lastKnownLocation.get("pointB").get("accuracy").clear();
//                lastKnownLocation.get("pointB").get("accuracy").add(accuracy);

                if(lastKnownLocation.get("pointB").get("accuracyArray").size() < RANGE_SIZE)
                    lastKnownLocation.get("pointB").get("accuracyArray").add(accuracy);

                if(lastKnownLocation.get("pointB").get("proximity").size() < RANGE_SIZE)
                    lastKnownLocation.get("pointB").get("proximity").add(proximity);
                break;
            case "Sweet Beetroot":
//                lastKnownLocation.get("pointC").get("accuracy").clear();
//                lastKnownLocation.get("pointC").get("accuracy").add(accuracy);

                if(lastKnownLocation.get("pointC").get("accuracyArray").size() < RANGE_SIZE)
                    lastKnownLocation.get("pointC").get("accuracyArray").add(accuracy);

                if(lastKnownLocation.get("pointC").get("proximity").size() < RANGE_SIZE)
                    lastKnownLocation.get("pointC").get("proximity").add(proximity);
                break;
        }

        if(isPassed) {
            //updateListView
            passedAdapter.setBeaconList(new ArrayList<>(
                    appBeacons.values()
            ));

            passedAdapter.notifyDataSetChanged();
        }
    }

    public void sendLogs(BeaconString b) {
        final BeaconString sentBeacon = b;
        String reqUrl = getResources().getText(R.string.server_url)
                + "/api/logs";
        StringRequest postReq = new StringRequest(Request.Method.POST,
                reqUrl, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
//                Toast.makeText(getApplicationContext(),
//                        "Success! ScanCount = " + scanCount,
//                        Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "Error!", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                String output = "";
                output += sentBeacon.beaconName + ","
                        + sentBeacon.beaconMajor+":"+sentBeacon.beaconMinor +","
                        + "Power: " + sentBeacon.beaconPower + ","
                        + "RSSI: " + sentBeacon.beaconRSSI + ","
                        + "Accuracy: " + sentBeacon.beaconAccuracy + ","
                        + "Proximity: " + sentBeacon.beaconProximity + "\n";
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
        // Adding request to request queue
        BeaconRangingApp.getInstance().addToRequestQueue(postReq);
    }

    public void sendRangeLogs(String prox, List<Double> doubleArr, String isFound) {
        final String proximity = prox;
        final String found = isFound;
        String arr = "";

        for(Double d : doubleArr) {
            arr += String.valueOf(d) +
                    (!(d.equals(doubleArr.get(doubleArr.size()-1)))?
                        ",":"");
        }
        final String stringArr = arr;
        String reqUrl = getResources().getText(R.string.server_url)
                + "/api/range_logs";
        StringRequest postReq = new StringRequest(Request.Method.POST,
                reqUrl, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
//                Toast.makeText(getApplicationContext(),
//                        "Success! ScanCount = " + scanCount,
//                        Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "Error!", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                String output = "";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
                String format = simpleDateFormat.format(new Date());
                output += "[" + format + "],"
                        + "uID: " + SplashActivity.currentIdentification
                        + ", Estimated Proximity: " + proximity
                        + ", Estimated Location Values: "
                        + stringArr + ", Loc Found? "
                        + found + "\n";
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
        // Adding request to request queue
        BeaconRangingApp.getInstance().addToRequestQueue(postReq);
    }

    public void sendBeaconValues(BeaconString b){
        final BeaconString sentBeacon = b;

        String reqUrl = getResources().getText(R.string.server_url)
                + "/api/locations/gsather";
        StringRequest putReq = new StringRequest(Request.Method.PUT,
                reqUrl, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
//                Toast.makeText(getApplicationContext(),
//                        "Success!",
//                        Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "Error!", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("name", DevActivity.currentLocation);
                params.put("area", getResources().getText(R.string.area_id).toString());
                params.put("point", sentBeacon.beaconMajor+":"+sentBeacon.beaconMinor);
                params.put("accuracy", sentBeacon.beaconAccuracy);
                params.put("proximity", sentBeacon.beaconProximity);
                params.put("rssi", sentBeacon.beaconRSSI);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        // Adding request to request queue
        BeaconRangingApp.getInstance().addToRequestQueue(putReq);
    }
}
