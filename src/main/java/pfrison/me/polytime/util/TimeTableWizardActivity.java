package pfrison.me.polytime.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

import pfrison.me.polytime.R;
import pfrison.me.polytime.android.Animations;
import pfrison.me.polytime.android.MainActivity;
import pfrison.me.polytime.objects.Day;
import pfrison.me.polytime.objects.Week;

/**
 * Contain all methods we need to display text or table in the reserved space in MainActivity
 * All variables and methods are static because we only have one timetable to display
 */
public class TimeTableWizardActivity {
    public static int lookedWeek = -1;
    public static boolean tableDiplayed = false;

    public static void displayText(Context context, String text){
        if(MainActivity.timeTableLayout == null)
            throw new RuntimeException("The activity is not loaded yet and/or you don't " +
                    "called getHeightAfterLoad(Activity) in the \"onCreate(Bundle)\" method.");
        //empty the place
        emptySpace();

        //create a TextView
        TextView textView = new TextView(context);
        textView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        //add the text
        textView.setText(text);
        //add color to the text
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) //noinspection deprecation
            textView.setTextColor(context.getResources().getColor(android.R.color.black));
        else textView.setTextColor(context.getResources().getColor(android.R.color.black, null));
        //center text
        textView.setGravity(Gravity.CENTER);
        //make it big
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        //make it bold
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        //add the TextView to the place
        MainActivity.timeTableLayout.addView(textView);

        tableDiplayed = false;
    }

    public static void displayTable(Context context, Week week) {
        if (MainActivity.timeTableLayout == null)
            throw new RuntimeException("The Widget is not loaded yet and/or you didn't called generateSpace(Activity).");
        if (week == null) return; //Week = null ? -> not downloaded yet.

        //should we refresh or build the table ? (a table is already present ?)
        View[] childs = new View[MainActivity.timeTableLayout.getChildCount()];
        for(int i = 0; i < MainActivity.timeTableLayout.getChildCount(); i++) {
            childs[i] = MainActivity.timeTableLayout.getChildAt(i);
        }
        if(childs.length != 0 && childs[0] instanceof HorizontalScrollView){
            //a table is already present -> update it

            //get and update all textViews
            HorizontalScrollView scrollView = (HorizontalScrollView) childs[0];
            LinearLayout horizontalLayout = (LinearLayout) scrollView.getChildAt(0);
            LinearLayout[] verticalLayouts = new LinearLayout[horizontalLayout.getChildCount()];
            for(int i = 0; i < horizontalLayout.getChildCount(); i++) {
                verticalLayouts[i] = (LinearLayout) horizontalLayout.getChildAt(i);
                for(int j = 0; j < verticalLayouts[i].getChildCount(); j++){
                    TextView textView = (TextView) verticalLayouts[i].getChildAt(j);

                    //update day displays
                    if(i != 0 && j == 0){
                        textView.setText(String.format("%s %s", StringWizard.getDayAbrev(context, i - 1), week.getDays()[i - 1].getDay()));
                    }
                    //update lessons (not hour and day display)
                    if(i != 0 && j != 0){
                        if(!week.getDays()[i - 1].getLessons()[j - 1].getRoom().equals("")){
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) //noinspection deprecation
                                textView.setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b><br>" +
                                        "<i>" + week.getDays()[i - 1].getLessons()[j - 1].getRoom() + "</i>"));
                            else textView.setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b><br>" +
                                    "<i>" + week.getDays()[i - 1].getLessons()[j - 1].getRoom() + "</i>", Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) //noinspection deprecation
                                textView.setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b>"));
                            else textView.setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b>", Html.FROM_HTML_MODE_LEGACY));
                        }
                    }

                    //highlight the current day and the current lesson and unhighlight the rest
                    if(week.getWeek() == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)){
                        //-1 because sunday is the first day AND -1 because sunday = 1 BUT +1 because of hour display
                        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                        int lesson = getCurrentLesson(context) + 1; //+1 because of day display
                        if(i == day && i != 0 && j != 0){
                            //highlight
                            if(j == lesson){
                                //set a custom background (highlight current lesson we are in)
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                                    textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_background_current_lesson));
                                else textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_background_current_lesson, null));
                            }else{
                                //set a custom background (highlight current day we are in)
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                                    textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_background_current_day));
                                else textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_background_current_day, null));
                            }
                        } else {
                            //unhighlight
                            //only lessons (not hour and day display)
                            if (i != 0 && j != 0) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                                    textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_border));
                                else
                                    textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_border, null));
                            }
                        }
                    } else if (i != 0 && j != 0) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                            textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_border));
                        else
                            textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_border, null));
                    }
                }
            }
        }else{
            //no table was present -> empty the place and create a new table

            //empty the place
            emptySpace();

            //create an scrollView
            HorizontalScrollView scrollView = new HorizontalScrollView(context);
            scrollView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            //add it to the place
            MainActivity.timeTableLayout.addView(scrollView);

            //create a LinearLayout for all LinearLayout for columns
            LinearLayout horizontalLayout = new LinearLayout(context);
            horizontalLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            //make it horizontal
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
            //add it to the scrollView
            scrollView.addView(horizontalLayout);

            //create all LinearLayout for columns
            LinearLayout[] verticalLayouts = new LinearLayout[Week.NUMBER_DAYS + 1];
            for (int i = 0; i < Week.NUMBER_DAYS + 1; i++) { //+1 for the hour display
                verticalLayouts[i] = new LinearLayout(context);
                //first column is the hour display
                if (i == 0) {
                    verticalLayouts[i].setLayoutParams(new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));
                }else{
                    //set the columns width to 105dp
                    //conversion 105dp to pixels
                    int width = (int) (105f * Resources.getSystem().getDisplayMetrics().density);
                    verticalLayouts[i].setLayoutParams(new FrameLayout.LayoutParams(
                            width,
                            FrameLayout.LayoutParams.MATCH_PARENT));
                }
                //make them vertical
                verticalLayouts[i].setOrientation(LinearLayout.VERTICAL);
                //add them to the horizontalLayout
                horizontalLayout.addView(verticalLayouts[i]);
            }

            //create all textView
            TextView[][] textViews = new TextView[Week.NUMBER_DAYS + 1][Day.NUMBER_LESSONS + 1];
            for (int i = 0; i < Week.NUMBER_DAYS + 1; i++) { //+1 for the hour display
                for (int j = 0; j < Day.NUMBER_LESSONS + 1; j++) { //+1 for the day display
                    textViews[i][j] = new TextView(context);
                    //layout params and set height
                    if(j != 0){ //all lessons and hour display
                        //reset layout params so it change the height automatically
                        //set the height so that all the space is occupied and all equal in height
                        textViews[i][j].setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                0,
                                1f)); //this is the layout weight -> tell android that I want every textview to have the same height
                    }else{
                        textViews[i][j].setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                    //set text size
                    textViews[i][j].setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    //set text color
                    if(i != 0 && j != 0){ //all lessons (not hour and day display)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) //noinspection deprecation
                            textViews[i][j].setTextColor(context.getResources().getColor(android.R.color.black));
                        else textViews[i][j].setTextColor(context.getResources().getColor(android.R.color.black, null));
                    }else{ //the rest (hour and day display)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) //noinspection deprecation
                            textViews[i][j].setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
                        else textViews[i][j].setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text, null));
                    }
                    //set centred
                    textViews[i][j].setGravity(Gravity.CENTER);
                    //set text
                    if(i == 0 && j != 0){ //hour display except corner
                        textViews[i][j].setText(StringWizard.getHour(context, j - 1));
                    }if(i != 0 && j == 0){ //day display except corner
                        textViews[i][j].setText(String.format("%s %s", StringWizard.getDayAbrev(context, i - 1), week.getDays()[i - 1].getDay()));
                    }if(i != 0 && j != 0){ //all lessons (not hour and day display)
                        if(!week.getDays()[i - 1].getLessons()[j - 1].getRoom().equals("")){
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) //noinspection deprecation
                                textViews[i][j].setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b><br>" +
                                        "<i>" + week.getDays()[i - 1].getLessons()[j - 1].getRoom() + "</i>"));
                            else textViews[i][j].setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b><br>" +
                                    "<i>" + week.getDays()[i - 1].getLessons()[j - 1].getRoom() + "</i>", Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) //noinspection deprecation
                                textViews[i][j].setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b>"));
                            else textViews[i][j].setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b>", Html.FROM_HTML_MODE_LEGACY));
                        }
                    }
                    //set time_table_border for some textView
                    if(i != 0 && j != 0){ //all lessons (not hour and day display)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                            textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_border));
                        else textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_border, null));
                    }

                    //highlight the current day and the current lesson and unhighlight the rest
                    if(week.getWeek() == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)){
                        //-1 because sunday is the first day AND -1 because sunday = 1 BUT +1 because of hour display
                        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                        int lesson = getCurrentLesson(context) + 1; //+1 because of day display
                        if(i == day && i != 0 && j != 0){
                            //highlight
                            if(j == lesson){
                                //set a custom background (highlight current lesson we are in)
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                                    textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_background_current_lesson));
                                else textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_background_current_lesson, null));
                            }else{
                                //set a custom background (highlight current day we are in)
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                                    textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_background_current_day));
                                else textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_background_current_day, null));
                            }
                        } else {
                            //unhighlight
                            //only lessons (not hour and day display)
                            if (i != 0 && j != 0) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                                    textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_border));
                                else
                                    textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_border, null));
                            }
                        }
                    } else if (i != 0 && j != 0) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                            textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_border));
                        else
                            textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_border, null));
                    }

                    //add them to their verticalLayout
                    verticalLayouts[i].addView(textViews[i][j]);
                }
            }
        }
        tableDiplayed = true;
    }

    public static void generateSpace(Activity mainAct){
        //create timeTableLayout and set LayoutParams
        MainActivity.timeTableLayout = new FrameLayout(mainAct);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.BELOW, R.id.padding);
        MainActivity.timeTableLayout.setLayoutParams(params);

        //add timeTableLayout to the rootLayout of MainActivity
        RelativeLayout rootLayout = mainAct.findViewById(R.id.rootLayout);
        assert rootLayout != null;
        rootLayout.addView(MainActivity.timeTableLayout);
    }

    private static void emptySpace(){
        if(MainActivity.timeTableLayout == null)
            throw new RuntimeException("The activity is not loaded yet and/or you don't " +
                    "called getHeightAfterLoad(Activity) in the \"onCreate(Bundle)\" method.");
        //search for child and remove them
        for(int i = 0; i < MainActivity.timeTableLayout.getChildCount(); i++) {
            View child = MainActivity.timeTableLayout.getChildAt(i);
            MainActivity.timeTableLayout.removeView(child);
        }
    }

    private static int getCurrentLesson(Context context){
        //get current time
        int minute = Calendar.getInstance().get(Calendar.MINUTE) + (60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        //get schedules in resources strings
        String[] schedulesStr = new String[]{
                context.getResources().getString(R.string.hour1),
                context.getResources().getString(R.string.hour2),
                context.getResources().getString(R.string.hour3),
                context.getResources().getString(R.string.hour4)};
        int[] schedules = new int[schedulesStr.length];
        for(int i = 0; i < schedulesStr.length; i++){
            String endSche = schedulesStr[i].split("\n")[1];
            schedules[i] = Integer.parseInt(endSche.split("h")[1]) + (60 * Integer.parseInt(endSche.split("h")[0]));
        }
        //return the value of the current lesson we are in
        if(minute < schedules[0])
            return 0;
        if(minute < schedules[1])
            return 1;
        if(minute < schedules[2])
            return 2;
        if(minute < schedules[3])
            return 3;
        return -1;
    }

    /** To find the nearest week available */
    public static void setDefaultLookedWeek(SharedPreferences pref){
        int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        for(int add = 0; add < 10; add++){
            int added = (currentWeek + add) % 53; //53 = number of weeks in a year (it's a maximum. Real number is 52,1429 weeks per year)
            if(SaveWeekWizard.isWeekAvailable(pref, added)){
                lookedWeek = added;
                return;
            }
        }
    }

    public static void minusLookedWeek(SharedPreferences pref){
        if(SaveWeekWizard.isWeekAvailable(pref, lookedWeek - 1)) lookedWeek--;
    }

    public static void plusLookedWeek(SharedPreferences pref){
        if(SaveWeekWizard.isWeekAvailable(pref, lookedWeek + 1)) lookedWeek++;
    }
}
