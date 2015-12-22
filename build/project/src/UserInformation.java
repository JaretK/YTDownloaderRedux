
public class UserInformation {
	
	public final String itunesLocation;
	public final String tempLocation;
	
	public UserInformation(String itunesLocation, String tempLocation){
		this.itunesLocation = itunesLocation;
		this.tempLocation = tempLocation;
	}
	
	public String getItunesLocation(){
		return this.itunesLocation;
	}
	
	public String getTempLocation(){
		return this.tempLocation;
	}
}
