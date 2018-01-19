package pfrison.me.polytime.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

import pfrison.me.polytime.android.MainActivity;
import pfrison.me.polytime.objects.Day;
import pfrison.me.polytime.objects.Lesson;
import pfrison.me.polytime.objects.Week;

/**
 * Contain all methods we need to demand and format data
 */
public class DataWizard {
    public static boolean readFail = false;

    /**
     * Crop the raw server response {@literal <HTML></HTML>} to the second table {@literal <TABLE></TABLE>}
     * @param raw the raw server response containing two {@literal <TABLE></TABLE>} tags
     * @return {@literal <TABLE>the table data here</TABLE>}
     */
	private static String cropTable(String raw) {
		String tableStr = "";

		//line crop
		boolean go = false;
		for(String str : raw.split("\n")) {
			if(go) tableStr += str; //record if go
			if(!go && str.contains("</TABLE>")) go = true; //if not go and we read the end of the first table, start recording for the second table
		}

		//precise crop
		tableStr = tableStr.substring(tableStr.indexOf("<TABLE"), tableStr.indexOf("</TABLE>") + "</TABLE>".length());

		return tableStr;
	}

    /**
     * Crop the table string {@literal <TABLE></TABLE>} to a list of 7 days string {@literal <TR></TR>} in a raw (6 days plus day display per week)
     * @param tableStr the table string containing multiple {@literal <TR></TR>} tags
     * @return {@literal <TR>a day</TR>} (x7)
     */
	private static String[] cropWeek(String tableStr) {
		int numberOfWeeks = (int) (((double) tableStr.split("<TR align=center>").length) / 7d);

		//correction of a bug in the table "langue 2 (voir affichage)" misplaced
		if(tableStr.contains("<TR align=center><TD colspan=1>Langue 2 (voir affichage)<TD><TD><TD><TD></TR>")) {
			numberOfWeeks--;
		}

		String[] weekStrs = new String[numberOfWeeks];
		for(int i=0; i<weekStrs.length; i++) {
			int cropCursor = -1;
			for(int trCount = 0; trCount < Week.NUMBER_DAYS + 2; trCount++) {
				cropCursor = tableStr.indexOf("<TR align=center>", cropCursor + 1);
			}
			weekStrs[i] = tableStr.substring(0, cropCursor);

			//correction of a bug in the table "langue 2 (voir affichage)" misplaced
			if(weekStrs[i].contains("<TR align=center><TD colspan=1>Langue 2 (voir affichage)<TD><TD><TD><TD></TR>")) {
				int length = "<TR align=center><TD colspan=1>Langue 2 (voir affichage)<TD><TD><TD><TD></TR>".length();
				cropCursor = tableStr.indexOf("<TR align=center>", cropCursor + 1);
				weekStrs[i] = tableStr.substring(length, cropCursor);
			}

			tableStr = tableStr.substring(cropCursor);
		}

		//remove <TABLE> tag from weekStrs[0]
		weekStrs[0] = weekStrs[0].substring(weekStrs[0].indexOf("<TR align=center>"));

		return weekStrs;
	}

    /**
     * Crop the week string {@literal <TR></TR>} (x7) to a day string {@literal <TR></TR>}
     * @param weekStr the week string containing 7 {@literal <TR></TR>} tags
     * @return {@literal <TR>a day</TR>}
     */
	private static String[] cropDay(String weekStr) {
		String[] dayStrs = new String[Week.NUMBER_DAYS +1];
		for(int i=0; i<dayStrs.length - 1; i++) {
			dayStrs[i] = weekStr.substring(0, weekStr.indexOf("<TR align=center>", 1));
			weekStr = weekStr.substring(weekStr.indexOf("<TR align=center>", 1));
		}
		dayStrs[dayStrs.length - 1] = weekStr;
		return dayStrs;
	}

    /**
     * Crop the day string {@literal <TR></TR>} to a list of 4 lesson string {@literal <TD></TD>}
     * @param dayStr the day string containing 4 {@literal <TD></TD>} tags
     * @return a list of 4 lessons
     */
	private static Lesson[] cropLesson(String dayStr) {
		Lesson[] lessons = new Lesson[Day.NUMBER_LESSONS];

		//remove TR
		dayStr = dayStr.substring("<TR align=center>".length());
		dayStr = dayStr.substring(0, dayStr.indexOf("</TR>"));
		//remove time
		dayStr = dayStr.substring(dayStr.indexOf("<TD", 1));

		int i = 0;
		while(dayStr.contains("<TD") && i < 4) {
			//no lesson
			if(dayStr.indexOf("<TD>") == 0) {
				lessons[i] = new Lesson("", "");
				//crop to next lesson
				if(dayStr.indexOf("<TD", 1) != -1) dayStr = dayStr.substring(dayStr.indexOf("<TD", 1));
				else break;

				//next lesson
				i++;
			}
			//have a lesson
			else {
				//get lesson name and room
				int endSubstr = dayStr.indexOf("<TD", 1) == -1 ? dayStr.length() : dayStr.indexOf("<TD", 1);
				String nameroom = dayStr.substring(dayStr.indexOf(">") + ">".length(), endSubstr);

				String name, room;
				if(nameroom.contains("(")) {
					name = nameroom.substring(0, nameroom.indexOf("("));
					if(name.endsWith(" ")) name = name.substring(0, name.length() - 1);

					room = nameroom.substring(nameroom.indexOf("(") + "(".length(), nameroom.indexOf(")"));
				}else {
					name = nameroom;
					room = "";
				}

				//add lesson, colspan times
				int colspan = Integer.parseInt(dayStr.substring(dayStr.indexOf("colspan=") + "colspan=".length(), dayStr.indexOf(">")));
				for(int j=0; j<colspan; j++) {
					lessons[i + j] = new Lesson(name, room);
				}

				//crop to next lesson
				if(dayStr.indexOf("<TD", 1) != -1) dayStr = dayStr.substring(dayStr.indexOf("<TD", 1));
				else break;

				//next lesson (colspan)
				i += colspan;
			}
		}

		return lessons;
	}

    /**
     * Used to get (download + format data) weeks for the server
     * @param context the context
     * @param pref the suer preference
     * @return a list of Week
     */
	public static Void downloadWeeks(Context context, SharedPreferences pref){
        readFail = false;
        //download data
		String data = InternetRequests.getRawData(context, pref);
        if(data == null) return null; //ignore if data is null (no connexion)

        try{
            //extract and organize info contained in data
            String[] weekStrs = cropWeek(cropTable(data));
            ArrayList<Week> weeks = new ArrayList<>();
            for (String weekStr : weekStrs) {
                //if no week number is present -> somehow the timetable is bugged, ignore this week
                if (weekStr.contains("<A name=S")) {
                    Week week = new Week();

                    //week number
                    String wnstr = weekStr.substring(weekStr.indexOf("<A name=S") + "<A name=S".length(), weekStr.indexOf(">S"));
                    week.setWeek(Integer.parseInt(wnstr));

                    //days
                    Day[] days = new Day[Week.NUMBER_DAYS];
                    for(int j=0; j<days.length; j++) {
                        //day string
                        String str = StringWizard.getDayString(week.getWeek(), j);
                        //lessons
                        Lesson[] lessons = cropLesson(cropDay(weekStr)[j + 1]);
                        //add the day
                        days[j] = new Day(str, lessons);
                    }
                    week.setDays(days);

                    weeks.add(week);
                }
            }
            //transform ArrayList in primitive array
            Week[] weeksArray = weeks.toArray(new Week[weeks.size()]);

            //save weeks and send the result
            SaveWeekWizard.saveWeeks(weeksArray, pref);
        }catch (Exception e){
            e.printStackTrace();
            readFail = true;
        }
        return null; //because of the "Void" type (and not "void")
    }
}
