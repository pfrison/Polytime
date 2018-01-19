package pfrison.me.polytime.objects;

public class Lesson {
	private String name;
	private String room;

	public Lesson(String name, String room) {
		this.setName(name);
		this.setRoom(room);
	}

	public String getName() {return name;}
	public void setName(String name) {this.name = name;}

	public String getRoom() {return room;}
	public void setRoom(String room) {this.room = room;}
}
