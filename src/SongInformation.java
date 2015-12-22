import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.text.WordUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "song")
public class SongInformation {

	
	public String songTitle, artist, ytid, videoTitle;
	public boolean mp3;

	public SongInformation() {
	}

	public SongInformation(String songTitle, String artist, String ytid) {
		this.songTitle = formatString(songTitle);
		this.artist = formatString(artist);
		this.ytid = ytid;
		this.videoTitle = "";
		this.mp3 = true;
	}

	// handle proper capitalization
	// use apache commons lang
	private String formatString(String in) {
		String cap = WordUtils.capitalize(in);
		//capitalize words in parentheses
		int indexOpenParen = cap.indexOf('(');
		while (indexOpenParen > -1 && indexOpenParen < cap.length() - 1) {
			String atIndex = "" + cap.charAt(indexOpenParen + 1);
			atIndex = WordUtils.capitalize(atIndex);
			// rejoin
			String before = cap.substring(0, indexOpenParen + 1);
			String after = cap.substring(indexOpenParen + 2);
			cap = before + atIndex + after;
			indexOpenParen = cap.indexOf('(', indexOpenParen + 1);
		}
		return cap;
	}

	
	public String getSongTitle() {
		return this.songTitle;
	}

	public String getArtist() {
		return this.artist;
	}

	public String getYtid() {
		return this.ytid;
	}

	public String getVideoTitle() {
		return this.videoTitle;
	}

	public void setVideoTitle(String title) {
		this.videoTitle = title;
	}

	public boolean getMP3() {
		return this.mp3;
	}
}
