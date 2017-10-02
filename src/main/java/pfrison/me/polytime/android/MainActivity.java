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
import pfrison.me.polytime.util.TimeTableWizard;

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
                new DownloadWeekAsync().execute(position + 1);
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
                TimeTableWizard.minusLookedWeek();
                //redraw table
                TimeTableWizard.displayTable(getBaseContext());
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
                TimeTableWizard.setDefaultLookedWeek();
                //redraw table
                TimeTableWizard.displayTable(getBaseContext());
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
                TimeTableWizard.plusLookedWeek();
                //redraw table
                TimeTableWizard.displayTable(getBaseContext());
            }
        });

        //generate the space to add the table or the loading/error text
        TimeTableWizard.generateSpace(this);

        //After the Activity creation, Android will jump to onCreateOptionsMenu(Menu)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //called when creating the option menu
        //create the download state icon toolbar menu
        getMenuInflater().inflate(R.menu.main_activity_toolbar, menu);
        DLIcon = menu.findItem(R.id.dlicon);

        //restore a save (if exist, else display a loading text) while downloading the table for the first time that the activity is opened
        new DownloadWeekAsync().execute(PreferenceManager.getDefaultSharedPreferences(this).getInt("group", 0) + 1);

        return true;
    }

    //used to download the table asynchronously while a save is restored.
    class DownloadWeekAsync extends AsyncTask<Integer, Void, Week[]> {
        @Override
        protected Week[] doInBackground(Integer... groupeTP) {
            //download
            return DataWizard.getWeeks(groupeTP[0], getBaseContext(), PreferenceManager.getDefaultSharedPreferences(getBaseContext()));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(DLIcon != null) DLIcon.setIcon(R.drawable.ic_sync_white_24dp);

            //save restore in wait of the updated data
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            TimeTableWizard.weeks = DataWizard.getSavedWeeks(pref.getInt("group", 0) + 1, pref);
            //restore save if we have one
            if(TimeTableWizard.weeks != null) TimeTableWizard.displayTable(getBaseContext());
            //tell the user that we downloading the table
            else TimeTableWizard.displayText(getBaseContext(), getResources().getString(R.string.downloading));
        }

        protected void onPostExecute(Week[] week) {
            if(week == null){
                //connection fail
                if(DLIcon != null) DLIcon.setIcon(R.drawable.ic_no_connexion_white_24dp);
                if(TimeTableWizard.weeks == null){
                    //no save
                    TimeTableWizard.displayText(getBaseContext(), getResources().getString(R.string.connexionFail));
                }
                return;
            }

            //hide download icon and text
            if(DLIcon != null) DLIcon.setIcon(android.R.color.transparent);

            //apply change in activity
            TimeTableWizard.weeks = week;
            TimeTableWizard.displayTable(getBaseContext());
        }
    }
}