package pfrison.me.polytime.android;

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
import android.widget.Spinner;

import pfrison.me.polytime.R;
import pfrison.me.polytime.objects.Week;
import pfrison.me.polytime.util.DataWizard;
import pfrison.me.polytime.util.TimeTableWizardActivity;

/**
 * This is the root activity. User will load this activity on app start.
 * The first line of code executed is in onCreate(Bundle) method
 */
public class MainActivity extends AppCompatActivity {
    private MenuItem DLIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //called at the creation of the Activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //set the layout

        //erase all preference when user launch this version for the first time
        //I'm not proud of this, this will be deleted for the next version (current : 1.1.1, code 3)
        SharedPreferences preftemp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(preftemp.getBoolean("firstRunCode3", true)){
            preftemp.edit().clear().apply();
            preftemp.edit().putBoolean("firstRunCode3", false).apply();
        }

        //spinner to control group selection
        Spinner groupSpinner = (Spinner) findViewById(R.id.timeSpinner);
        assert groupSpinner != null;
        //set the Strings to display in the spinner and set animation
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this, R.array.time_spinner, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(timeAdapter);
        //set the user preference for the group selection
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this); //get user preferences
        groupSpinner.setSelection(pref.getInt("group", 0));
        //set the listener
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //save the selection
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                editor.putInt("group", position);
                editor.apply();

                //download time table
                new DownloadWeekAsync().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        //buttons to control week selection
        //display previous week
        Button prevButton = (Button) findViewById(R.id.arrowPrevButton);
        assert prevButton != null;
        //set listener
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set to the previous week if exist
                TimeTableWizardActivity.minusLookedWeek();
                //redraw table
                TimeTableWizardActivity.displayTable(getBaseContext());
            }
        });

        //display current week
        Button currentButton = (Button) findViewById(R.id.arrowCurrentButton);
        assert currentButton != null;
        //set listener
        currentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set to the next closest week
                TimeTableWizardActivity.setDefaultLookedWeek();
                //redraw table
                TimeTableWizardActivity.displayTable(getBaseContext());
            }
        });

        //display next week
        Button nextButton = (Button) findViewById(R.id.arrowNextButton);
        assert nextButton != null;
        //set listener
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set to the next week if exist
                TimeTableWizardActivity.plusLookedWeek();
                //redraw table
                TimeTableWizardActivity.displayTable(getBaseContext());
            }
        });

        //generate the space to add the table or the loading/error text
        TimeTableWizardActivity.generateSpace(this);

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

        //restore a save (if exist, else display a loading text) while downloading the table for the first time that the activity is opened
        new DownloadWeekAsync().execute();
    }

    //used to download the table asynchronously while a save is restored.
    class DownloadWeekAsync extends AsyncTask<Void, Void, Week[]> {
        private int groupTP;

        public DownloadWeekAsync(){
            groupTP = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("group", 0) + 1;
        }

        @Override
        protected Week[] doInBackground(Void... v) {
            //download
            return DataWizard.getWeeks(groupTP, getBaseContext(), PreferenceManager.getDefaultSharedPreferences(getBaseContext()));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(DLIcon != null) DLIcon.setIcon(R.drawable.ic_sync_white_24dp);

            //save restored in wait of the updated data
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            TimeTableWizardActivity.weeks = DataWizard.getSavedWeeks(groupTP, pref);
            //restore save if we have one
            if(TimeTableWizardActivity.weeks != null) TimeTableWizardActivity.displayTable(getBaseContext());
            //tell the user that we downloading the table
            else TimeTableWizardActivity.displayText(getBaseContext(), getResources().getString(R.string.downloading));
        }

        protected void onPostExecute(Week[] week) {
            if(week == null){
                //connection fail
                if(DLIcon != null) DLIcon.setIcon(R.drawable.ic_no_connexion_white_24dp);
                if(TimeTableWizardActivity.weeks == null){
                    //no save
                    TimeTableWizardActivity.displayText(getBaseContext(), getResources().getString(R.string.connexionFail));
                }
                return;
            }

            //hide download icon and text
            if(DLIcon != null) DLIcon.setIcon(android.R.color.transparent);

            //apply change in activity
            TimeTableWizardActivity.weeks = week;
            TimeTableWizardActivity.displayTable(getBaseContext());
        }
    }
}