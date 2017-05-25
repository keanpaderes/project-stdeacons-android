package ics.specialproject.kean.projectstdeacons.Activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.ScrollView;
import android.widget.TextView;

import ics.specialproject.kean.projectstdeacons.Adapters.PicturePagerAdapter;
import ics.specialproject.kean.projectstdeacons.R;

import java.util.ArrayList;
import java.util.HashMap;

import me.relex.circleindicator.CircleIndicator;

public class ArtInfoActivity extends AppCompatActivity {
    private static ViewPager mPager;

    private TextView subjectName;
    private TextView sub1Title;
    private TextView sub1Info;
    private TextView sub2Title;
    private TextView sub2Info;
    private TextView sub3Title;
    private TextView sub3Info;


    private static int currentPage;
    private Boolean tgl = false;
    private HashMap subjects;
    private ArrayList<String> picArr = new ArrayList<>();

    private HashMap<String, String[]> initSubjects() {
        HashMap<String, String[]> ret = new HashMap<>();
        String[] syph = {
                getString(R.string.syphilis_information),
                getString(R.string.syphilis_sub1),
                getString(R.string.syphilis_sub2)
        };
        ret.put("syphilis", syph);

        String[] gw = {
                getString(R.string.gw_information),
                getString(R.string.gw_sub1),
                getString(R.string.gw_sub2)
        };
        ret.put("genital_warts", gw);

        String[] vd = {
                getString(R.string.vd_information),
                getString(R.string.vd_sub1),
                getString(R.string.vd_sub2)
        };
        ret.put("vagina_diseases", vd);

        String[] am = {
                getString(R.string.am_information),
                getString(R.string.am_sub1),
                getString(R.string.am_sub2)
        };
        ret.put("artificial_methods", am);

        String[] tm = {
                getString(R.string.tm_information),
                getString(R.string.tm_sub1),
                getString(R.string.tm_sub2)
        };
        ret.put("traditional_methods", tm);

        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Drawable upArrow = ContextCompat.getDrawable(ArtInfoActivity.this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(ContextCompat.getColor(ArtInfoActivity.this, R.color.colorPrimary),
                PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(
                        ArtInfoActivity.this, R.color.colorAccent)));
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        subjects = initSubjects();
    }

    private void init() {
        subjectName = (TextView) findViewById(R.id.subjectName);
        sub1Title = (TextView) findViewById(R.id.sub1Title);
        sub1Info = (TextView) findViewById(R.id.sub1Info);
        sub2Title = (TextView) findViewById(R.id.sub2Title);
        sub2Info = (TextView) findViewById(R.id.sub2Info);
        sub3Title = (TextView) findViewById(R.id.sub3Title);
        sub3Info = (TextView) findViewById(R.id.sub3Info);

        subjectName.setText(getIntent().getExtras().getString("subjectFormal"));
        String[] info =
                (String[]) subjects.get(getIntent().getExtras().getString("subjectName"));
        sub1Title.setText("Description");
        sub1Info.setText(info[0]);

        switch(getIntent().getExtras().getString("subjectName")) {
            case "artificial_methods":
                sub2Title.setText("Female Condoms");
                sub2Info.setText(info[1]);
                sub3Title.setText("Male Condoms");
                sub3Info.setText(info[2]);
                break;
            case "traditional_methods":
                sub2Title.setText("Regular Cycle");
                sub2Info.setText(info[1]);
                sub3Title.setText("Irregular Cycle");
                sub3Info.setText(info[2]);
                break;
            default:
                sub2Title.setText("Symptoms");
                sub2Info.setText(info[1]);
                sub3Title.setText("Treatment");
                sub3Info.setText(info[2]);
                break;
        }


        for(int i=1;i<=4;i++){
            String addStr = getResources().getText(R.string.server_url) + "/images/"
                    + getIntent().getExtras().getString("subjectName")
                    + "/photo" + i +".jpg";
            if(!picArr.contains(addStr)) picArr.add(addStr);
        }

        mPager = (ViewPager) findViewById(R.id.imagePager);
        mPager.setPageMargin(0);
        mPager.setAdapter(new PicturePagerAdapter(ArtInfoActivity.this, picArr));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.imageIndicator);
        indicator.setViewPager(mPager);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.faveBtn);
        final LinearLayout artInfoMain = (LinearLayout) findViewById(R.id.artInfoMain);

//        ScrollView scrollView = (ScrollView) findViewById(R.id.artInfoScrollView);
//        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                int[] ssPosition = new int[2];
//                artInfoMain.getLocationOnScreen(ssPosition);
//                if(ssPosition[1] < scrollY) {
//                    fab.hide();
//                } else fab.show();
//            }
//        });

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
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed(){
        finish();
    }
}
