package pfrison.me.polytime.util;

import android.app.Activity;
import android.content.Context;
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
import pfrison.me.polytime.objects.Day;
import pfrison.me.polytime.objects.Week;

/**
 * Contain all methods we need to display text or table in the reserved space in MainActivity
 */
public class TimeTableWizard {
    private static FrameLayout timeTableLayout;
    private static int lookedWeek = -1;
    public static Week[] weeks;

    public static void displayText(final Context context, String text){
        if(timeTableLayout == null)
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
        timeTableLayout.addView(textView);
    }

    public static void displayTable(Context context) {
        if (timeTableLayout == null)
            throw new RuntimeException("The activity is not loaded yet and/or you don't " +
                    "called getHeightAfterLoad(Activity) in the \"onCreate(Bundle)\" method.");
        if (weeks == null)
            throw new RuntimeException("weeks can't be null. Please make sure you download the time table first.");

        //if lookedWeek don't exist in week, set to default
        if(!isLookedWeekExist()) setDefaultLookedWeek();

        //find the selected week
        Week week = null;
        for(Week w : weeks){
            if(w.getWeek() == lookedWeek){
                week = w;
                break;
            }
        }
        assert week != null;

        //should we refresh or build the table ? (a table is already present ?)
        View[] childs = new View[timeTableLayout.getChildCount()];
        for(int i = 0; i < timeTableLayout.getChildCount(); i++) {
            childs[i] = timeTableLayout.getChildAt(i);
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
                        if(!week.getDays()[i - 1].getLessons()[j - 1].getRoom().equals(""))
                            textView.setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b><br>" +
                                    "<i>" + week.getDays()[i - 1].getLessons()[j - 1].getRoom() + "</i>"));
                        else textView.setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b>"));
                    }

                    //highlight the current day and the current lesson and unhighlight the rest
                    if(week.getWeek() == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)){
                        //highlight
                        //-1 because sunday is the first day AND -1 because sunday = 1 BUT +1 because of hour display
                        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                        int lesson = getCurrentLesson(context) + 1; //+1 because of day display
                        if(i == day && i != 0 && j != 0){
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
                        }
                    }else{
                        //unhighlight
                        //only lessons (not hour and day display)
                        if(i != 0 && j != 0) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                                textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_border));
                            else
                                textView.setBackground(context.getResources().getDrawable(R.drawable.time_table_border, null));
                        }
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
            timeTableLayout.addView(scrollView);

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
                    //conversion 100dp to pixels
                    int width = (int) (100f * Resources.getSystem().getDisplayMetrics().density);
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
                    textViews[i][j].setLayoutParams(new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT));
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
                        if(!week.getDays()[i - 1].getLessons()[j - 1].getRoom().equals(""))
                            textViews[i][j].setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b><br>" +
                                    "<i>" + week.getDays()[i - 1].getLessons()[j - 1].getRoom() + "</i>"));
                        else textViews[i][j].setText(Html.fromHtml("<b>" + week.getDays()[i - 1].getLessons()[j - 1].getName() + "</b>"));
                    }
                    //set time_table_border for some textView
                    if(i != 0 && j != 0){ //all lessons (not hour and day display)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //noinspection deprecation
                            textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_border));
                        else textViews[i][j].setBackground(context.getResources().getDrawable(R.drawable.time_table_border, null));
                    }
                    //set height
                    if(j != 0){ //all lessons and hour display
                        //set the height so that all the space is occupied
                        textViews[i][j].setHeight((int) ((float) (timeTableLayout.getHeight() - textViews[i][0].getHeight())/4f));
                    }
                    //highlight the current day and the current lesson
                    if(week.getWeek() == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)){
                        //-1 because sunday is the first day AND -1 because sunday = 1 BUT +1 because of hour display
                        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                        int lesson = getCurrentLesson(context) + 1; //+1 because of day display
                        if(i == day && i != 0 && j != 0){
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
                        }
                    }

                    //add them to their verticalLayout
                    verticalLayouts[i].addView(textViews[i][j]);
                }
            }
        }
    }

    public static void generateSpace(Activity mainAct){
        //create timeTableLayout and set LayoutParams
        timeTableLayout = new FrameLayout(mainAct);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.BELOW, R.id.sep2);
        timeTableLayout.setLayoutParams(params);

        //add timeTableLayout to the rootLayout of MainActivity
        RelativeLayout rootLayout = (RelativeLayout) mainAct.findViewById(R.id.rootLayout);
        assert rootLayout != null;
        rootLayout.addView(timeTableLayout);
    }

    private static void emptySpace(){
        if(timeTableLayout == null)
            throw new RuntimeException("The activity is not loaded yet and/or you don't " +
                    "called getHeightAfterLoad(Activity) in the \"onCreate(Bundle)\" method.");
        //search for child and remove them
        for(int i = 0; i < timeTableLayout.getChildCount(); i++) {
            View child = timeTableLayout.getChildAt(i);
            timeTableLayout.removeView(child);
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

    public static void setDefaultLookedWeek(){
        int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        //get the next closest week
        int min = Integer.MAX_VALUE;
        for(Week w : weeks){
            //if exact value min = 0 -> this is the closest
            if(w.getWeek() == currentWeek){
                lookedWeek = currentWeek;
                return;
            }
            //keep track of the next closest week
            if(w.getWeek() - currentWeek >= 0 //we want min >= 0 (aka the next closest week)
                    && w.getWeek() - currentWeek < min){
                min = w.getWeek() - currentWeek;
            }
        }
        lookedWeek = currentWeek + min;
    }

    public static void minusLookedWeek(){
        for(Week week : weeks) {
            if(week.getWeek() == lookedWeek - 1) {
                //available
                lookedWeek--;
                break;
            }
        }
    }

    public static void plusLookedWeek(){
        for(Week week : weeks) {
            if(week.getWeek() == lookedWeek + 1) {
                //available
                lookedWeek++;
                break;
            }
        }
    }

    private static boolean isLookedWeekExist(){
        //not initialized
        if(lookedWeek == -1) return false;

        for(Week w : weeks){
            if(w.getWeek() == lookedWeek){
                return true;
            }
        }
        return false;
    }
}
