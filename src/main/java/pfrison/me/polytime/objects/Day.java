package pfrison.me.polytime.objects;

public class Day {
	public static final int NUMBER_LESSONS = 4;
	
	private Lesson[] lessons = new Lesson[NUMBER_LESSONS];
	private String day = null;

	public Day() {}
	public Day(String day) {
		this.setDay(day);
	}
	public Day(String day, Lesson[] lessons) {
		if(lessons.length != 4) throw new IllegalArgumentException("lessons length should be 4.");
		for(Lesson lesson : lessons) if(lesson == null) throw new IllegalArgumentException("All lessons in lessons should be not null");

		this.setDay(day);
		this.setLessons(lessons);

		for(int i=0; i<lessons.length; i++) {
			Lesson lesson = lessons[i];

			if(lesson.getDay() == null || lesson.getDay().equals("")) lesson.setDay(day);
			if(lesson.getHour() < 0 || lesson.getHour() > 3) lesson.setHour(i);
		}
	}
	
	public Lesson[] getLessons() {return lessons;}
	public void setLessons(Lesson[] lessons) {
		if(lessons.length != 4) throw new IllegalArgumentException("lessons length should be 4");
		if(day == null) throw new IllegalArgumentException("You must specify the day number first.");
		for(Lesson lesson : lessons) if(lesson == null) throw new IllegalArgumentException("All lessons in lessons should be not null");
		
		this.lessons = lessons;
		
		for(int i=0; i<lessons.length; i++) {
			Lesson lesson = lessons[i];
			
			if(lesson.getDay() == null || lesson.getDay().equals("")) lesson.setDay(day);
			if(lesson.getHour() < 0 || lesson.getHour() > 3) lesson.setHour(i);
		}
	}
	
	public String getDay() {return day;}
	public void setDay(String day) {this.day = day;}
}
