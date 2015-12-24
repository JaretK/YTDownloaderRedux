
public class UserInformation {
	
	public final String itunesLocation;
	public final String tempLocation;
	
	public static final String ITUNES_LOCATION = "/Volumes/Macintosh_HD/Media/iTunes Library/Automatically Add to iTunes.localized";
	public static final String TEMP_LOCATION = "/users/jkarnuta/desktop";
	
	public UserInformation(String itunesLocation, String tempLocation){
		this.itunesLocation = itunesLocation;
		this.tempLocation = tempLocation;
	}
	
	/**
	 * Parameterless constructor gets the system specific temp and itunes locations
	 */
	public UserInformation(){
		this.itunesLocation = ITUNES_LOCATION;
		this.tempLocation = TEMP_LOCATION;
	}
	
	public String getItunesLocation(){
		return this.itunesLocation;
	}
	
	public String getTempLocation(){
		return this.tempLocation;
	}
}
