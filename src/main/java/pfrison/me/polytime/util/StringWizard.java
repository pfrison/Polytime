package pfrison.me.polytime.util;

import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Calendar;

import pfrison.me.polytime.R;

/**
 * Contain all methods we need to deal with or store strings
 */
public class StringWizard {
	public static String getDayString(int weekNumber, int dayPosition) {
		DateFormat sdf = DateFormat.getDateInstance();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.WEEK_OF_YEAR, weekNumber);

        //On some devices, the set method don't update the weekNumber properly.
        //Calling the get method just after the set method force the weekNumber to update (????)
        //Don't ask me why or how... The observer effect in quantum mechanics is not my domain...
        cal.get(Calendar.WEEK_OF_YEAR);

		cal.set(Calendar.DAY_OF_WEEK, dayPosition + 2);
		return sdf.format(cal.getTime());
	}
	
	public static String getPageString(int TP) {
		switch (TP) {
		case 0:
			return "groupe11.html";
		case 1:
			return "groupe12.html";
		case 2:
			return "groupe23.html";
		case 3:
			return "groupe24.html";
		}
		throw new IllegalArgumentException("TP groupe should be only 1, 2, 3 or 4 (" + String.valueOf(TP) + ").");
	}
	
	public static String ISOtoUTF(String iso) {
		String utf = iso;
		try {
			//é
			utf = utf.replace(new String("é".getBytes("ISO-8859-15")), "é");
			//è
			utf = utf.replace(new String("è".getBytes("ISO-8859-15")), "è");
			//ê
			utf = utf.replace(new String("ê".getBytes("ISO-8859-15")), "ê");
			//ë
			utf = utf.replace(new String("ë".getBytes("ISO-8859-15")), "ë");
			//à
			utf = utf.replace(new String("à".getBytes("ISO-8859-15")), "à");
			//ù
			utf = utf.replace(new String("ù".getBytes("ISO-8859-15")), "ù");
			//ô
			utf = utf.replace(new String("ô".getBytes("ISO-8859-15")), "ô");
			//î
			utf = utf.replace(new String("î".getBytes("ISO-8859-15")), "î");
			//ï
			utf = utf.replace(new String("ï".getBytes("ISO-8859-15")), "ï");
		} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		return utf;
	}

	public static String getDayAbrev(Context context, int day) {
		switch (day){
			case 0:
				return context.getResources().getString(R.string.day1);
			case 1:
				return context.getResources().getString(R.string.day2);
			case 2:
				return context.getResources().getString(R.string.day3);
			case 3:
				return context.getResources().getString(R.string.day4);
			case 4:
				return context.getResources().getString(R.string.day5);
			case 5:
				return context.getResources().getString(R.string.day6);
			case 6:
				return context.getResources().getString(R.string.day7);
		}
        throw new IllegalArgumentException("day should be only between 0 and 6 (" + String.valueOf(day) + ").");
	}

	public static String getHour(Context context, int hour){
		switch (hour){
			case 0:
				return context.getResources().getString(R.string.hour1);
			case 1:
				return context.getResources().getString(R.string.hour2);
			case 2:
				return context.getResources().getString(R.string.hour3);
			case 3:
				return context.getResources().getString(R.string.hour4);
		}
		throw new IllegalArgumentException("day should be only between 0 and 3 (" + String.valueOf(hour) + ").");
	}
}
