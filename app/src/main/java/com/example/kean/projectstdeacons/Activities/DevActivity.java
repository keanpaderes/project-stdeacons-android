package com.example.kean.projectstdeacons.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.estimote.sdk.SystemRequirementsChecker;
import com.example.kean.projectstdeacons.Adapters.BeaconListViewAdapter;
import com.example.kean.projectstdeacons.Application.BeaconRangingApp;
import com.example.kean.projectstdeacons.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.kean.projectstdeacons.Application.BeaconRangingApp.currentBeacons;
import static com.example.kean.projectstdeacons.Application.BeaconRangingApp.locPoints;
import static com.example.kean.projectstdeacons.Application.BeaconRangingApp.LocationString;

public class DevActivity extends AppCompatActivity {
    private Boolean userIsInteracting = false;
    private Boolean isToAdd = false;
    private LocationString newLoc = null;
    private ArrayAdapter<CharSequence> bnsAdapter;
    private ArrayAdapter<String> locAdapter;
    private Spinner beaconNumberSpinner;
    private ListView beaconListView;
    private ListView locListView;
    private TextView locNameTxt;
    private EditText locNameField;
    private TextView locPlaceTxt;
    private TextView locPlacePointTxt;
    private ToggleButton rangingToggler;
    private Button addButton;
    private BeaconListViewAdapter blvAdapter;


    public static int beaconSize = 3; //this should be changeable
    public static List<String> trustedBeacons =
            Arrays.asList("61539:34663"
                , "33345:3091"
                , "55656:22263");
    public static List<String> tBNames =
            Arrays.asList("Lemon Tart"
                    , "Candy Floss"
                    , "Sweet Beetroot");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev);

        initialize();

        bnsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        beaconNumberSpinner.setAdapter(bnsAdapter);
        beaconNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(userIsInteracting){
                    if(!parent.getItemAtPosition(position).toString().equals("Choose...")){
                        Toast.makeText(DevActivity.this, "Selected: " +
                                        parent.getItemAtPosition(position).toString(),
                                Toast.LENGTH_SHORT) .show();
                        beaconListView.setVisibility(View.VISIBLE);
                        locListView.setVisibility(View.VISIBLE);
                        rangingToggler.setVisibility(View.VISIBLE);
                        addButton.setVisibility(View.VISIBLE);
                        beaconNumberSpinner.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        rangingToggler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(!BeaconRangingApp.isPassed && BeaconRangingApp.setAdapter(blvAdapter)) {
                        BeaconRangingApp.isPassed = true;
                        BeaconRangingApp.beaconManager.startRanging(BeaconRangingApp.region);
                    }
                } else {
                    if(BeaconRangingApp.isPassed){
                        Toast.makeText(DevActivity.this, "Stopped", Toast.LENGTH_LONG).show();
                        if(BeaconRangingApp.destroyAdapter()) {
                            BeaconRangingApp.beaconManager.stopRanging(BeaconRangingApp.region);
                            BeaconRangingApp.isPassed = false;
                        }
                    }
                }
            }
        });
        rangingToggler.setChecked(false);

        locListView.setAdapter(locAdapter);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isToAdd) {
                    rangingToggler.setChecked(false);

                    newLoc = setLoc();
                    if(!(newLoc == null)){
                        isToAdd = true;
                        locPlacePointTxt.setText(BeaconRangingApp.convertLocString(newLoc));
                        setLocFieldsVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(DevActivity.this, "Wrong params in location!",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    locPoints.put(locNameField.getText().toString(), newLoc);
                    newLoc = null;
                    locAdapter.clear();
                    locAdapter.addAll(hashmapToString(locPoints));
                    locAdapter.notifyDataSetChanged();

                    isToAdd = false;
                    setLocFieldsVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    private void initialize(){
        beaconNumberSpinner = (Spinner) findViewById(R.id.beaconNumSpnr);
        bnsAdapter = ArrayAdapter.createFromResource(this,
                R.array.beacon_spinner_array, android.R.layout.simple_spinner_item);

        beaconListView = (ListView) findViewById(R.id.beaconListView);
        blvAdapter = new BeaconListViewAdapter(DevActivity.this,
                new ArrayList<>(currentBeacons.values()));
        beaconListView.setAdapter(blvAdapter);
        rangingToggler = (ToggleButton) findViewById(R.id.devRangingToggler);

        addButton = (Button) findViewById(R.id.addLocationBtn);
        locListView = (ListView) findViewById(R.id.locListView);
        locAdapter = new ArrayAdapter<>(DevActivity.this,
                android.R.layout.simple_list_item_1,
                hashmapToString(locPoints));
        locNameTxt = (TextView) findViewById(R.id.locNameTxt);
        locNameField = (EditText) findViewById(R.id.locNameField);
        locNameField.setText("");
        locPlaceTxt = (TextView) findViewById(R.id.locPlaceTxt);
        locPlacePointTxt = (TextView) findViewById(R.id.locPlacePointTxt);
    }

    public LocationString setLoc(){
        //Add loc only if immediate, near combination and other is far
        LocationString retLoc = new LocationString();
        for(String b: currentBeacons.keySet()){
            if(currentBeacons.get(b).beaconProximity.equals("IMMEDIATE")){
                retLoc.immediate = b;
            } else if(currentBeacons.get(b).beaconProximity.equals("NEAR")) {
                retLoc.nearby = b;
            }
        }
        if(!(retLoc.immediate == null && retLoc.nearby == null)){
            return retLoc;
        } else return null;
    }

    public void setLocFieldsVisibility(int visibility) {
        locNameTxt.setVisibility(visibility);
        locNameField.setVisibility(visibility);
        locPlaceTxt.setVisibility(visibility);
        locPlacePointTxt.setVisibility(visibility);
    }

    public ArrayList<String> hashmapToString(HashMap<String, LocationString> hm){
        ArrayList<String> ret = new ArrayList<>();
        if(!hm.keySet().isEmpty()){
            for(String key : hm.keySet()) {
                ret.add(key + ": " +
                        BeaconRangingApp.convertLocString(hm.get(key)));
            }
        }
        return ret;
    }
}
