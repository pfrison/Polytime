package pfrison.me.polytime.objects;

public class Lesson {
	private String name;
	private String room;
	private String day;
	private int hour = -1; //-1 if blank
	
	public Lesson() {}
	public Lesson(String name, String room) {
		this.setName(name);
		this.setRoom(room);
	}
	public Lesson(String name, String room, String day, int hour) {
		if(hour < 0 || hour > 3) throw new IllegalArgumentException("hour value should be between 0 and 3"); 
		
		this.setName(name);
		this.setRoom(room);
		this.setDay(day);
		this.setHour(hour);
	}

	public String getName() {return name;}
	public void setName(String name) {this.name = name;}

	public String getRoom() {return room;}
	public void setRoom(String room) {this.room = room;}
	
	public String getDay() {return day;}
	public void setDay(String day) {this.day = day;}
	
	public int getHour() {return hour;}
	public void setHour(int hour) {this.hour = hour;}
}
