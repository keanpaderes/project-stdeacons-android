package ics.specialproject.kean.projectstdeacons.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import ics.specialproject.kean.projectstdeacons.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ImageView backBtnIcon = (ImageView) findViewById(R.id.backBtnIcon);
        backBtnIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create back button.
                finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
