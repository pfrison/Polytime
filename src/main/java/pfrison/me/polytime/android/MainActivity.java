package pfrison.me.polytime.android;

import android.animation.Animator;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;

import pfrison.me.polytime.R;
import pfrison.me.polytime.objects.Week;
import pfrison.me.polytime.util.DataWizard;
import pfrison.me.polytime.util.InternetRequests;
import pfrison.me.polytime.util.SaveWeekWizard;
import pfrison.me.polytime.util.TimeTableWizardActivity;

/**
 * This is the root activity. User will load this activity on app start.
 * The first line of code executed is in onCreate(Bundle) method
 */
/*
TODO list :
- Nothing for now, ready to release \o/
 */
public class MainActivity extends AppCompatActivity {
    public static final String GROUP_TP_KEY = "group";
    public static FrameLayout timeTableLayout;
    private MenuItem DLIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //called at the creation of the Activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //set the layout

        //erase all preference when user launch this version for the first time
        //This will be deleted for the next version (current : 1.1.2 , code 4)
        SharedPreferences preftemp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(preftemp.getBoolean("firstRunCode4", true)){
            preftemp.edit().clear().apply();
            preftemp.edit().putBoolean("firstRunCode4", false).apply();
        }

        //buttons to control week selection
        final Button prevButton = (Button) findViewById(R.id.arrowPrevButton);
        final Button currentButton = (Button) findViewById(R.id.arrowCurrentButton);
        final Button nextButton = (Button) findViewById(R.id.arrowNextButton);

        //spinner to control group selection
        Spinner groupSpinner = (Spinner) findViewById(R.id.timeSpinner);
        assert groupSpinner != null;
        //set the Strings to display in the spinner and set animation
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this, R.array.time_spinner, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(timeAdapter);
        //set the user preference for the group selection
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this); //get user preferences
        groupSpinner.setSelection(pref.getInt(GROUP_TP_KEY, 0));
        //set the listener
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                if(pref.getInt(GROUP_TP_KEY, -1) != position){
                    //save the selection
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                    editor.putInt(GROUP_TP_KEY, position);
                    editor.apply();

                    //animate the fade out
                    Animations.animateReturnDefaultFadeOut(timeTableLayout, new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            prevButton.setEnabled(false);
                            currentButton.setEnabled(false);
                            nextButton.setEnabled(false);
                        }
                        @Override
                        public void onAnimationCancel(Animator animation) {}
                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //download time table
                            new DownloadWeekAsync().execute();

                            //animate the fade in
                            Animations.animateReturnDefaultFadeIn(timeTableLayout, Animations.enableButtonsAtEnd(prevButton, currentButton, nextButton));
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        //buttons to control week selection
        //display previous week
        assert prevButton != null;
        //set listener
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animate the fade out
                Animations.animateLeftFadeOut(timeTableLayout, new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        prevButton.setEnabled(false);
                        currentButton.setEnabled(false);
                        nextButton.setEnabled(false);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {}
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //set to the previous week if exist
                        TimeTableWizardActivity.minusLookedWeek(PreferenceManager.getDefaultSharedPreferences(getBaseContext()));
                        //redraw table
                        TimeTableWizardActivity.displayTable(getBaseContext(), SaveWeekWizard.loadWeek(PreferenceManager.getDefaultSharedPreferences(getBaseContext())));

                        //animate the fade in
                        Animations.animateLeftFadeIn(timeTableLayout, Animations.enableButtonsAtEnd(prevButton, currentButton, nextButton));
                    }
                });
            }
        });

        //display current week
        assert currentButton != null;
        //set listener
        currentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animate the fade out
                Animations.animateReturnDefaultFadeOut(timeTableLayout, new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        prevButton.setEnabled(false);
                        currentButton.setEnabled(false);
                        nextButton.setEnabled(false);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {}
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //set to the next closest week
                        TimeTableWizardActivity.setDefaultLookedWeek(PreferenceManager.getDefaultSharedPreferences(getBaseContext()));
                        //redraw table
                        TimeTableWizardActivity.displayTable(getBaseContext(), SaveWeekWizard.loadWeek(PreferenceManager.getDefaultSharedPreferences(getBaseContext())));

                        //animate the fade in
                        Animations.animateReturnDefaultFadeIn(timeTableLayout, Animations.enableButtonsAtEnd(prevButton, currentButton, nextButton));
                    }
                });
            }
        });

        //display next week
        assert nextButton != null;
        //set listener
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animate the fade out
                Animations.animateRightFadeOut(timeTableLayout, new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        prevButton.setEnabled(false);
                        currentButton.setEnabled(false);
                        nextButton.setEnabled(false);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {}
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //set to the next week if exist
                        TimeTableWizardActivity.plusLookedWeek(PreferenceManager.getDefaultSharedPreferences(getBaseContext()));
                        //redraw table
                        TimeTableWizardActivity.displayTable(getBaseContext(), SaveWeekWizard.loadWeek(PreferenceManager.getDefaultSharedPreferences(getBaseContext())));

                        //animate the fade in
                        Animations.animateRightFadeIn(timeTableLayout, Animations.enableButtonsAtEnd(prevButton, currentButton, nextButton));
                    }
                });
            }
        });

        //generate the space to add the table or the loading/error text
        TimeTableWizardActivity.generateSpace(this);
        Animations.animateOpening(MainActivity.timeTableLayout);

        //After the Activity creation, Android will jump to onCreateOptionsMenu(Menu)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //called when creating the option menu
        //create the download state icon toolbar menu
        getMenuInflater().inflate(R.menu.main_activity_toolbar, menu);
        DLIcon = menu.findItem(R.id.dlicon);

        return true;
        //After the OptionMenu creation, Android will jump to onResume()
    }

    @Override
    public void onResume() {
        super.onResume();

        //set the looked week to the closest one
        TimeTableWizardActivity.setDefaultLookedWeek(PreferenceManager.getDefaultSharedPreferences(getBaseContext()));

        //restore a save (if exist) or display a loading text while downloading the table for the first time that the activity is opened
        new DownloadWeekAsync().execute();
    }

    //used to download the table asynchronously while a save is restored.
    class DownloadWeekAsync extends AsyncTask<Void, Void, Void> {
        public DownloadWeekAsync(){}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(DLIcon != null) DLIcon.setIcon(R.drawable.ic_sync_white_24dp);

            //save restored in wait of the updated data
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            //restore save if we have one
            if(SaveWeekWizard.isWeekAvailable(pref, TimeTableWizardActivity.lookedWeek))
                TimeTableWizardActivity.displayTable(getBaseContext(), SaveWeekWizard.loadWeek(pref));
            //tell the user that we downloading the table, if not save is saved
            else TimeTableWizardActivity.displayText(getBaseContext(), getResources().getString(R.string.downloading));
        }

        @Override
        protected Void doInBackground(Void... v) {
            //download
            return DataWizard.downloadWeeks(getBaseContext(), PreferenceManager.getDefaultSharedPreferences(getBaseContext()));
        }

        protected void onPostExecute(Void v) {
            //set the looked week to the closest one
            if(TimeTableWizardActivity.lookedWeek == -1)
                TimeTableWizardActivity.setDefaultLookedWeek(PreferenceManager.getDefaultSharedPreferences(getBaseContext()));

            Week week = SaveWeekWizard.loadWeek(PreferenceManager.getDefaultSharedPreferences(getBaseContext()));
            if(week == null){
                //fails (we can't have both)
                if(DataWizard.readFail){
                    //read fail
                    if(DLIcon != null) DLIcon.setIcon(R.drawable.ic_warning_white_24dp);
                    if(!TimeTableWizardActivity.tableDiplayed){
                        //no table displayed and no save -> tell user no connection
                        TimeTableWizardActivity.displayText(getBaseContext(), getResources().getString(R.string.readFail));
                    }
                }else if(InternetRequests.connectionFail){
                    //connection fail
                    if(DLIcon != null) DLIcon.setIcon(R.drawable.ic_no_connexion_white_24dp);
                    if(!TimeTableWizardActivity.tableDiplayed){
                        //no table displayed and no save -> tell user no connection
                        TimeTableWizardActivity.displayText(getBaseContext(), getResources().getString(R.string.connexionFail));
                    }
                }else{
                    //unknown error
                    if(DLIcon != null) DLIcon.setIcon(R.drawable.ic_warning_white_24dp);
                    if(!TimeTableWizardActivity.tableDiplayed){
                        //no table displayed and no save -> tell user no connection
                        TimeTableWizardActivity.displayText(getBaseContext(), getResources().getString(R.string.unknowFail));
                    }
                }
                return;
            }

            //hide download icon and text
            if(DLIcon != null) DLIcon.setIcon(android.R.color.transparent);

            //apply change in activity
            TimeTableWizardActivity.displayTable(getBaseContext(), week);
        }
    }
}