package pfrison.me.polytime.util;

import android.content.SharedPreferences;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;

import pfrison.me.polytime.android.MainActivity;
import pfrison.me.polytime.objects.Day;
import pfrison.me.polytime.objects.Lesson;
import pfrison.me.polytime.objects.Week;

/**
 * Contain all methods we need to save and load data
 */
public class SaveWeekWizard {

    //this should not be changed
    //make sure you delete the content of the previous key before changing
    public static final String SAVE_KEY = "weeksSave"; // + the TP group + the week number

    /* Save pattern : (1 file per week)
     *
     * [W]{
     * wn=int;
     * [D]{
     * dd=str;
     * [L]{
     * ln=str;
     * lr=str;
     * }[L]
     * ...
     * }[D]
     * ...
     * }[W]
     */

    //list of all string to escape
    //relation between string to escape and its code MUST NOT CHANGE from one release to another
    //the shorter the escapes are, the faster the program run
    private static final Escape SEMICOLON = new Escape(";", 0);
    private static final Escape WEEK_START = new Escape("W{", 1);
    private static final Escape WEEK_END = new Escape("}W", 2);
    private static final Escape DAY_START = new Escape("D{", 3);
    private static final Escape DAY_END = new Escape("}D", 4);
    private static final Escape LESSON_START = new Escape("L{", 5);
    private static final Escape LESSON_END = new Escape("}L", 6);
    private static final Escape WEEK_NUMBER = new Escape("wn=", 7);  //week number
    private static final Escape DAY_DATE = new Escape("dd=", 8);     //day date
    private static final Escape LESSON_NAME = new Escape("ln=", 9);  //lesson name
    private static final Escape LESSON_ROOM = new Escape("lr=", 10); //lesson room
    private static final Escape[] escapes = new Escape[]{SEMICOLON, WEEK_START, WEEK_END, DAY_START,
            DAY_END, LESSON_START, LESSON_END, WEEK_NUMBER, DAY_DATE, LESSON_NAME, LESSON_ROOM};


    public static void saveWeeks(Week[] weeks, SharedPreferences pref){
        int groupTP = pref.getInt(MainActivity.GROUP_TP_KEY, 0);
        //one save file for every week
        for(Week week : weeks){
            String save = "";

            save += WEEK_START.str + "\n";
            save += WEEK_NUMBER.str + String.valueOf(week.getWeek()) + SEMICOLON.str + "\n";
            //all day
            for(Day day : week.getDays()){
                save += DAY_START.str + "\n";
                save += DAY_DATE.str + Escape.escapeChars(day.getDay()) + SEMICOLON.str + "\n";
                //all lessons
                for(Lesson lesson : day.getLessons()){
                    save += LESSON_START.str + "\n";
                    save += LESSON_NAME.str + Escape.escapeChars(lesson.getName()) + SEMICOLON.str + "\n";
                    save += LESSON_ROOM.str + Escape.escapeChars(lesson.getRoom()) + SEMICOLON.str + "\n";
                    save += LESSON_END.str + "\n";
                }
                save += DAY_END.str + "\n";
            }
            save += WEEK_END.str + "\n";

            //save to a file like "weeksSave-4-36"
            SharedPreferences.Editor prefedit = pref.edit();
            prefedit.putString(SAVE_KEY + "-" + String.valueOf(groupTP) + "-" + String.valueOf(week.getWeek()), save);
            prefedit.apply();
        }
    }

    public static Week loadWeek(SharedPreferences pref){
        int groupTP = pref.getInt(MainActivity.GROUP_TP_KEY, 0);

        //a save is available ?
        String save = pref.getString(SAVE_KEY + "-" + String.valueOf(groupTP) + "-" + String.valueOf(TimeTableWizardActivity.lookedWeek), null);
        if(save == null) return null; //is save is null -> no week saved yet

        //get all days
        ArrayList<Day> days = new ArrayList<>();
        while (save.contains(DAY_START.str) && save.contains(DAY_END.str)){
            //crop the day
            String dayStr = save.substring(save.indexOf(DAY_START.str), save.indexOf(DAY_END.str) + DAY_END.str.length());
            save = save.substring(save.indexOf(DAY_END.str) + DAY_END.str.length());

            //get date
            String date = Escape.unescapeChars(dayStr.substring(dayStr.indexOf(DAY_DATE.str) + DAY_DATE.str.length(), dayStr.indexOf(SEMICOLON.str)));

            //get all lessons
            ArrayList<Lesson> lessons = new ArrayList<>();
            while (dayStr.contains(LESSON_START.str) && dayStr.contains(LESSON_END.str)){
                //crop the lesson
                String lessonStr = dayStr.substring(dayStr.indexOf(LESSON_START.str), dayStr.indexOf(LESSON_END.str) + LESSON_END.str.length());
                dayStr = dayStr.substring(dayStr.indexOf(LESSON_END.str) + LESSON_END.str.length());

                //get name
                String name = Escape.unescapeChars(lessonStr.substring(lessonStr.indexOf(LESSON_NAME.str) + LESSON_NAME.str.length(), lessonStr.indexOf(SEMICOLON.str)));

                //crop and get room
                lessonStr = lessonStr.substring(lessonStr.indexOf(SEMICOLON.str) + SEMICOLON.str.length());
                String room = Escape.unescapeChars(lessonStr.substring(lessonStr.indexOf(LESSON_ROOM.str) + LESSON_ROOM.str.length(), lessonStr.indexOf(SEMICOLON.str)));

                //add the lesson to the list
                lessons.add(new Lesson(name, room));
            }
            Lesson[] lessonsArray = lessons.toArray(new Lesson[lessons.size()]);

            //add the day to the list
            days.add(new Day(date, lessonsArray));
        }
        Day[] daysArray = days.toArray(new Day[days.size()]);

        //send the week
        return new Week(daysArray, TimeTableWizardActivity.lookedWeek);
    }

    /* ----- string/char escape methods ----- */
    private static class Escape {
        //an object to help shorten the code. It store the char to escape and its code.
        private final String str;
        private final int code;
        private Escape(String str, int code){
            this.str = str; this.code = code;
        }
        private String getEscape(){return "/$" + new DecimalFormat("0000").format(code) + "/";}

        //static methods we actually using
        private static String escapeChars(String str){
            //replace all string by its code
            for(Escape escape : escapes) str = str.replace(escape.str, escape.getEscape());
            return str;
        }
        private static String unescapeChars(String str){
            //replace all escape code by its string
            for(Escape escape : escapes) str = str.replace(escape.getEscape(), escape.str);
            return str;
        }
    }

    public static boolean isWeekAvailable(SharedPreferences pref, int weekNumber){
        int groupTP = pref.getInt(MainActivity.GROUP_TP_KEY, 0);
        //return true if the week is available
        return pref.getString(SaveWeekWizard.SAVE_KEY + "-" + String.valueOf(groupTP) + "-" + String.valueOf(weekNumber), null) != null;
    }
}
