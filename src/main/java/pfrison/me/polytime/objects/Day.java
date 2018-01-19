package pfrison.me.polytime.objects;

public class Day {
	public static final int NUMBER_LESSONS = 4;
	
	private Lesson[] lessons = new Lesson[NUMBER_LESSONS];
	private String day = null;

	public Day(String day, Lesson[] lessons) {
		if(lessons.length != 4) throw new IllegalArgumentException("lessons length should be 4.");
		for(Lesson lesson : lessons) if(lesson == null) throw new IllegalArgumentException("All lessons in lessons should be not null");

		this.setDay(day);
		this.setLessons(lessons);
	}
	
	public Lesson[] getLessons() {return lessons;}
	public void setLessons(Lesson[] lessons) {
		if(lessons.length != 4) throw new IllegalArgumentException("lessons length should be 4");
		if(day == null) throw new IllegalArgumentException("You must specify the day number first.");
		for(Lesson lesson : lessons) if(lesson == null) throw new IllegalArgumentException("All lessons in lessons should be not null");
		
		this.lessons = lessons;
	}
	
	public String getDay() {return day;}
	public void setDay(String day) {this.day = day;}
}
