package pfrison.me.polytime.android;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import pfrison.me.polytime.R;
import pfrison.me.polytime.objects.Week;
import pfrison.me.polytime.util.DataWizard;
import pfrison.me.polytime.util.SaveWeekWizard;
import pfrison.me.polytime.util.TimeTableWizardActivity;
import pfrison.me.polytime.util.TimeTableWizardWidget;

public class WidgetVertical extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //set the remote views
        TimeTableWizardWidget.remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_vertical_layout);
        //then refresh the table
        new DownloadWeekAsync(context, appWidgetManager).execute();
    }

    //used to download the table asynchronously while a save is restored.
    class DownloadWeekAsync extends AsyncTask<Void, Void, Void> {
        private final Context context;
        private final AppWidgetManager widgetManager;

        public DownloadWeekAsync(Context context, AppWidgetManager widgetManager){
            this.context = context;
            this.widgetManager = widgetManager;
        }

        @Override
        protected Void doInBackground(Void... v) {
            //download
            return DataWizard.downloadWeeks(context, PreferenceManager.getDefaultSharedPreferences(context));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //save restored in wait of the updated data
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            TimeTableWizardWidget.week = SaveWeekWizard.loadWeek(pref);
            //restore save if we have one
            if(TimeTableWizardWidget.week != null) TimeTableWizardWidget.displayTable(context);
                //tell the user that we downloading the table
            else TimeTableWizardWidget.displayText(context.getResources().getString(R.string.downloading));

            //update widget
            widgetManager.updateAppWidget(new ComponentName(context, WidgetVertical.class), TimeTableWizardWidget.remoteViews);
        }

        protected void onPostExecute(Void v) {
            //set the looked week to the closest one
            TimeTableWizardActivity.setDefaultLookedWeek(PreferenceManager.getDefaultSharedPreferences(context));

            Week week = SaveWeekWizard.loadWeek(PreferenceManager.getDefaultSharedPreferences(context));
            if(week == null){
                //connection fail
                if(TimeTableWizardWidget.week == null){
                    //no save
                    TimeTableWizardWidget.displayText(context.getResources().getString(R.string.connexionFail));
                }
            }else{
                //apply change in activity
                TimeTableWizardWidget.week = week;
                TimeTableWizardWidget.displayTable(context);
            }

            //update widget
            widgetManager.updateAppWidget(new ComponentName(context, WidgetVertical.class), TimeTableWizardWidget.remoteViews);
        }
    }
}
