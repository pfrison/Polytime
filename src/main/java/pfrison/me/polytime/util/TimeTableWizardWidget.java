package pfrison.me.polytime.util;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import pfrison.me.polytime.R;
import pfrison.me.polytime.objects.Day;
import pfrison.me.polytime.objects.Lesson;
import pfrison.me.polytime.objects.Week;

/**
 * Contain all methods we need to display text or table in the reserved space in widgets (oriented both horizontally and vertically)
 * All variables and methods are static because we only need to have one for all widget
 */
public class TimeTableWizardWidget {
    public static RemoteViews remoteViews;
    public static Week week;

    public static void displayText(String text){
        if(remoteViews == null) throw new RuntimeException("remoteViews can't be null !");

        //make the table invisible and the textdisplay visible
        remoteViews.setViewVisibility(R.id.widget_linearlayout, View.INVISIBLE);
        remoteViews.setViewVisibility(R.id.widget_textdisplay, View.VISIBLE);

        //set text
        remoteViews.setTextViewText(R.id.widget_textdisplay, text);
    }

    public static void displayTable(Context context) {
        if(remoteViews == null) throw new RuntimeException("remoteViews can't be null !");
        if(week == null) return; //Weeks = null ? -> not downloaded yet.

        //make the table visible and the textdisplay invisible
        remoteViews.setViewVisibility(R.id.widget_linearlayout, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.widget_textdisplay, View.INVISIBLE);

        //find the current day
        Day currentDay = null;
        Calendar cal = Calendar.getInstance();
        if(week.getWeek() != cal.get(Calendar.WEEK_OF_YEAR)){
            //if current week is the next closest week (but no the current one) -> display monday;
            currentDay = week.getDays()[0];
        }else{
            //display the current day
            switch (cal.get(Calendar.DAY_OF_WEEK)){
                case Calendar.MONDAY:
                    currentDay = week.getDays()[0];
                    break;
                case Calendar.TUESDAY:
                    currentDay = week.getDays()[1];
                    break;
                case Calendar.WEDNESDAY:
                    currentDay = week.getDays()[2];
                    break;
                case Calendar.THURSDAY:
                    currentDay = week.getDays()[3];
                    break;
                case Calendar.FRIDAY:
                    currentDay = week.getDays()[4];
                    break;
                case Calendar.SATURDAY:
                    currentDay = week.getDays()[5];
                    break;
                case Calendar.SUNDAY:
                    currentDay = week.getDays()[0];
                    break;
            }
        }
        assert currentDay != null;

        //set background -> highlight current lesson we are in and unhightlight the rest
        switch (getCurrentLesson(context)){
            case 0:
                remoteViews.setInt(R.id.widget_hour1, "setBackgroundResource", R.drawable.time_table_background_current_lesson);
                remoteViews.setInt(R.id.widget_hour2, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour3, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour4, "setBackgroundResource", R.drawable.time_table_border);
                break;
            case 1:
                remoteViews.setInt(R.id.widget_hour1, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour2, "setBackgroundResource", R.drawable.time_table_background_current_lesson);
                remoteViews.setInt(R.id.widget_hour3, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour4, "setBackgroundResource", R.drawable.time_table_border);
                break;
            case 2:
                remoteViews.setInt(R.id.widget_hour1, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour2, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour3, "setBackgroundResource", R.drawable.time_table_background_current_lesson);
                remoteViews.setInt(R.id.widget_hour4, "setBackgroundResource", R.drawable.time_table_border);
                break;
            case 3:
                remoteViews.setInt(R.id.widget_hour1, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour2, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour3, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour4, "setBackgroundResource", R.drawable.time_table_background_current_lesson);
                break;
            default:
                remoteViews.setInt(R.id.widget_hour1, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour2, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour3, "setBackgroundResource", R.drawable.time_table_border);
                remoteViews.setInt(R.id.widget_hour4, "setBackgroundResource", R.drawable.time_table_border);
                break;
        }

        //transform lessons in spannable string
        Spanned[] spanneds = new Spannable[currentDay.getLessons().length];
        for(int i = 0; i < currentDay.getLessons().length; i++){
            Lesson lesson = currentDay.getLessons()[i];
            if(!lesson.getRoom().equals("")){
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) //noinspection deprecation
                    spanneds[i] = Html.fromHtml("<b>" + lesson.getName() + "</b><br>" + "<i>" + lesson.getRoom() + "</i>");
                else spanneds[i] = Html.fromHtml("<b>" + lesson.getName() + "</b><br>" + "<i>" + lesson.getRoom() + "</i>", Html.FROM_HTML_MODE_LEGACY);
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) //noinspection deprecation
                    spanneds[i] = Html.fromHtml("<b>" + lesson.getName() + "</b>");
                else spanneds[i] = Html.fromHtml("<b>" + lesson.getName() + "</b>", Html.FROM_HTML_MODE_LEGACY);
            }
        }

        //set texts
        remoteViews.setTextViewText(R.id.widget_hour1, spanneds[0]);
        remoteViews.setTextViewText(R.id.widget_hour2, spanneds[1]);
        remoteViews.setTextViewText(R.id.widget_hour3, spanneds[2]);
        remoteViews.setTextViewText(R.id.widget_hour4, spanneds[3]);
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
}
