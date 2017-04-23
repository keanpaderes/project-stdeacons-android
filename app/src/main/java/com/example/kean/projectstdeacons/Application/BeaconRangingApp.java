package com.example.kean.projectstdeacons.Application;

import android.app.Application;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.example.kean.projectstdeacons.Adapters.BeaconListViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.example.kean.projectstdeacons.Activities.DevActivity.tBNames;
import static com.example.kean.projectstdeacons.Activities.DevActivity.trustedBeacons;

/**
 * Created by kean on 4/15/17.
 */

public class BeaconRangingApp extends Application {
    public static BeaconManager beaconManager;
    public static Region region;
    public static Boolean isSet = false;
    public static Boolean isPassed = false;
    public static HashMap<String, BeaconString> currentBeacons
            = new HashMap<>();
    private HashMap<Integer, Boolean> beaconStatus = new HashMap<>();
    public static BeaconListViewAdapter passedAdapter;
    public static HashMap<String, LocationString> locPoints = new HashMap<>();

    public static class BeaconString {
        //Create db of this
        public String beaconName;
        public String beaconMajor;
        public String beaconMinor;
        public String beaconProximity;
    }

    public static class LocationString {
        //Create db of this
        //2 beacon approx
        public String immediate;
        public String nearby;

        /*//3-4 beacon approx
        public String immediate;
        public ArrayList<String> nearbys;
         */
    }

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = new BeaconManager(getApplicationContext());
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if(!list.isEmpty()) {
                    String read = "";
                    String prox = "Proximity: ";

                    for(String majMin : currentBeacons.keySet()){
                        beaconStatus.put(trustedBeacons.indexOf(majMin), false);
                    }

                    for(Beacon b : list){
                        String majorMinor = b.getMajor()+":"+b.getMinor();
                        int id = trustedBeacons.indexOf(majorMinor);
                        read+= tBNames.get(id) + " ";
                        prox+= tBNames.get(id) + "," +
                                String.valueOf(Utils.computeProximity(b)) + " ";
                        //Checks if it is trusted
                        if(trustedBeacons.contains(majorMinor)){
                            //If it is not in the hashmap yet
                            if(!currentBeacons.keySet().contains(majorMinor)) {
                                BeaconString currB = new BeaconString();
                                currB.beaconName = tBNames.get(id);
                                currB.beaconMajor = String.valueOf(b.getMajor());
                                currB.beaconMinor = String.valueOf(b.getMinor());
                                currB.beaconProximity = String.valueOf(Utils.computeProximity(b));

                                currentBeacons.put(majorMinor, currB);
                                beaconStatus.put(id, false);


                                Toast.makeText(getApplicationContext(), "Distance to beacon: " +
                                        Utils.computeProximity(b) + " Beacon name: " +
                                        currB.beaconName, Toast.LENGTH_LONG).show();
                            } else {
                                if(isSet && isPassed) {
                                    String tempProx = String.valueOf(Utils.computeProximity(b));

                                    if(!tempProx.equals(currentBeacons.get(majorMinor).beaconProximity
                                            .equals(String.valueOf(Utils.computeProximity(b))))) {
                                        currentBeacons.get(majorMinor).beaconProximity =
                                                String.valueOf(Utils.computeProximity(b));
                                        //Beacon proximity is changed and is found
                                        beaconStatus.put(id, true);
                                        //updateListView
                                        passedAdapter.setBeaconList(new ArrayList<>(
                                                currentBeacons.values()
                                        ));
                                        passedAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }
                    Toast.makeText(getApplicationContext(), prox, Toast.LENGTH_LONG).show();
                    //Check if all beacons are found
                    String status = "Status: ";
                    for(int id: beaconStatus.keySet()){
                        //if beacon is not found on that scan
                        status += tBNames.get(id) + " " +
                                (beaconStatus.get(id)? "FOUND": "NOT") + " ";
                        if(!beaconStatus.get(id)){
                            currentBeacons.get(trustedBeacons.get(id))
                                    .beaconProximity = "FAR";
                        }
                    }
                    Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();

                }
            }
        });
        beaconManager.setForegroundScanPeriod(7000, 15000);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                Toast.makeText(getApplicationContext(), "Beacon Ranging ready.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTerminate(){
        Toast.makeText(getApplicationContext(), "Beacon Ranging stopped.", Toast.LENGTH_SHORT).show();
        beaconManager.disconnect();
        super.onTerminate();
    }

    public static Boolean setAdapter(BeaconListViewAdapter passed){
        passedAdapter = passed;
        return true;
    }

    public static Boolean destroyAdapter(){
        passedAdapter = null;
        return true;
    }

    public static String convertLocString(LocationString l){
        String ret = "";
        ret += "I: " + currentBeacons.get(l.immediate).beaconName + ",";
        ret += "N: " + currentBeacons.get(l.nearby).beaconName;
        return ret;
    }
}
