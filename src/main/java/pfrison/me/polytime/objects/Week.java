package pfrison.me.polytime.objects;

import pfrison.me.polytime.util.StringWizard;

public class Week {
	public static final int NUMBER_DAYS = 6; //no Sunday
	
	private Day[] days = new Day[NUMBER_DAYS];
	private int week = -1;
	
	public Week() {}
	public Week(Day[] days, int week) {
		if(days.length != 6) throw new IllegalArgumentException("days length should be 6.");
		for(Day day : days) if(day == null) throw new IllegalArgumentException("All String in days should be not null");

		this.setWeek(week);
		this.setDays(days);

		for(int i=0; i<days.length; i++) {
			Day day = days[i];

			if(day.getDay() == null || day.getDay().equals("")) day.setDay(StringWizard.getDayString(week, i));
		}
	}

	public Day[] getDays() {return days;}
	public void setDays(Day[] days) {
		if(days.length != 6) throw new IllegalArgumentException("days length should be 6.");
		if(week == -1) throw new IllegalArgumentException("You must specify the week number first.");
		for(Day day : days) if(day == null) throw new IllegalArgumentException("All String in days should be not null");
		
		this.days = days;
		
		for(int i=0; i<days.length; i++) {
			Day day = days[i];
			
			if(day.getDay() == null || day.getDay().equals("")) day.setDay(StringWizard.getDayString(week, i));
		}
	}

	public int getWeek() {return week;}
	public void setWeek(int week) {this.week = week;}
}
