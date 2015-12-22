import java.util.List;

import javafx.collections.FXCollections;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Helper class to wrap a list of SongInformation objects. Used to save data to
 * xml.
 * 
 * @author JaretK
 */

@XmlRootElement(name = "SongInformationObjects")
public class SongInformationWrapper {

	private List<SongInformation> objs;
	
	public SongInformationWrapper(){
		objs = FXCollections.observableArrayList();
	}

	@XmlElement(name="SongInformation")
	public List<SongInformation> getSongInformationObjects() {
		return objs;
	}
	
	public void addSongInformation(SongInformation obj){
		objs.add(obj);
	}

	public void setSongInformationObjects(List<SongInformation> objs) {
		this.objs = objs;
	}
}
