import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class YTDLMain extends Application {

	// public static
	public static Parent root;
	public static Stage stage;
	public static TextFlowWriter myWriter;
	public static final String STAGE_TITLE = "YouTubeDownloader Redux";

	public static UserInformation user;
	public static SongInformation song;

	// public static final constants
	// Norwegian and Danish "uh" character
	public static final String OE = "\u00F8";

	// private static
	private static Controller controller;
	private static List<SongInformation> songRecord;
	private static File songRecordFile;

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		Font.loadFont(YTDLMain.class
				.getResourceAsStream("/Resources/Fonts/expressway.ttf"), 12);
		stage = primaryStage;

		root = loadRoot();
		stage = loadStage();
		stage.show();

		// update stage onCloseRequest to call the exit() method
		stage.setOnCloseRequest(e -> exit(0));

		// unmarshall SongRecord.xml (if present) using JAXB
		songRecord = unmarshallSongRecord();
	}

	public Parent loadRoot() throws IOException {
		return FXMLLoader.load(getClass().getResource("FrontEnd.fxml"));
	}

	public Stage loadStage() {
		Stage stage = new Stage();
		Scene scene = new Scene(root);
		stage.setTitle(STAGE_TITLE);
		stage.setScene(scene);
		stage.getIcons().add(new Image("/Resources/Images/YTDLIconPNG.png"));
		stage.setResizable(false);
		return stage;
	}

	/**
	 * Application Wide Controller methods (called within Controller
	 */

	// called when application is exited (via button press, menu item, or stage
	// close)
	public static void exit(int status) {
		saveSongRecord();
		Platform.exit();
		System.exit(status);
	}

	public static void getSong(SongInformation songInfo) {
		// clear textFlow
		myWriter.clear();

		// Make a new user
		YTDLMain.user = new UserInformation(
				"/Volumes/Macintosh_HD/Media/iTunes Library/Automatically Add to iTunes.localized",
				"/users/jkarnuta/desktop");

		// Set song static instance variable
		YTDLMain.song = songInfo;

		// create a GetSongModel object
		GetSongModel model = new GetSongModel(song, user);

		// get a download task from the model
		Task<String> downloadTask = model.downloadSong(myWriter);

		// update controller bindings
		controller.bindProgressBar(downloadTask.progressProperty());
		controller.bindLabelMessageProperty(downloadTask.messageProperty());

		// construct a download message
		Text a = new Text("Downloading ");
		Text b = new Text(song.getSongTitle());
		Text c = new Text(" by: ");
		Text d = new Text(song.getArtist());
		Text newLine = new Text("\n");
		Font boldedFont = Font.font("expressway.ttf", FontWeight.BOLD, 12);
		b.setFont(boldedFont);
		d.setFont(boldedFont);
		myWriter.write(a, b, c, d, newLine);

		// set on succeeded for task
		downloadTask
				.setOnSucceeded(e -> {
					if (downloadTask.getMessage().equals("Already Downloaded")) {
						myWriter.writeError("Already Downloaded MP4");
						controller.unbindLabelMessageProperty();
						controller
								.setLabelRed("YouTube Video File (MP4) Already Downloaded");
					} else if (downloadTask.getMessage().equals("ERROR")) {
						controller.unbindLabelMessageProperty();
						controller.setLabelRed("ERROR");
					} else {
						myWriter.writeSuccess("Download Complete!");
						String videoFileName = downloadTask.getValue();
						YTDLMain.song.setVideoTitle(videoFileName);
						convertVideoToAudio(videoFileName);
					}
				});

		// set task in background thread and start on its way!
		Thread downloadThread = new Thread(downloadTask);
		downloadThread.setDaemon(true);
		downloadThread.start();
	}

	public static void convertVideoToAudio(String filename) {
		// notify UI
		controller.unbindLabelMessageProperty();
		controller.unbindProgressBar();
		controller.setLabelBlue("Extracting Audio");

		// validate filename (sometimes errors have been thrown)
		String tempFileName = "";
		for (char c : filename.toCharArray()) {
			if (c == 65533) {// problem encountered at ytid = 8z02z7m2C2g
				// 65533 is the replacement character
				tempFileName += "\u2013";
			} else {
				tempFileName += c;
			}
		}
		if (!tempFileName.equals(filename)) {
			myWriter.writeError("Replacement Character Found in MP4");
			myWriter.writeInfo("Replacing with En Dash (\u2013)");
			filename = tempFileName;
		}

		// file names
		String absoluteFilePath = user.tempLocation + File.separator + filename;
		String mp3FilePath = convertName(absoluteFilePath, ".mp3");

		if (new File(mp3FilePath).exists()) {
			myWriter.writeError("Mp3 file already exists");
			myWriter.writeInfo("Deleting Old MP3...");
			File f = new File(mp3FilePath);
			boolean deleted = f.delete();
			if (deleted) {
				myWriter.writeSuccess("Deleted Old MP3");
				myWriter.writeInfo("Extracting Audio from MP4...");
			}
		} else {
			myWriter.writeInfo("Extracting Audio from MP4...");
		}

		// variables
		String ffmpegPath = getClassFilePath() + "Resources/ffmpeg";

		// start a thread that contains the encoder task
		extractAudio encoderTask = new extractAudio(ffmpegPath, absoluteFilePath,
				mp3FilePath);

		// Set on succeeded method
		encoderTask.setOnSucceeded(e -> {
			myWriter.writeSuccess("Convert Complete!");
			controller.setLabelGreen("Extraction Complete");
			editTags(mp3FilePath);
			new File(absoluteFilePath).deleteOnExit(); // user might want to
														// view the video before
														// its deleted.
				// we didn't spend all that time downloading for nothing!
			});

		// bind progress to progressBar
		controller.bindProgressBar(encoderTask.progressProperty());

		// Send task on its way in another thread
		new Thread(new Runnable(){
			@Override
			public void run() {
				//start encoderTask
				encoderTask.run();
				//update view with gobbler
				FFMPEGStreamGobbler gob = encoderTask.getGobbler();
				while(!gob.isEmpty()){
					myWriter.writeError(gob.poll());
				}
			}
		}).start();
	}

	private static String convertName(String from, String suffix) {
		int lastIndex = from.lastIndexOf(".");
		return from.substring(0, lastIndex) + suffix;
	}

	public static void editTags(String absoluteFilePath) {
		// use mp3agic. So fast that threading isn't necessary
		Mp3File mp3file;
		try {
			// get file
			mp3file = new Mp3File(absoluteFilePath);
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			myWriter.writeError(e.getMessage());
			return;
		}
		// get ID3v2 tags (set by FFMPEG from mp4 file)
		ID3v2 id3v2Tag;
		if (mp3file.hasId3v2Tag()) {
			id3v2Tag = mp3file.getId3v2Tag();
		} else {
			id3v2Tag = new ID3v23Tag();
			mp3file.setId3v2Tag(id3v2Tag);
		}

		// set ID3v2 tags
		id3v2Tag.setArtist(song.getArtist());
		id3v2Tag.setTitle(song.getSongTitle());
		try {
			// make temp file, delete old one, rename the temp as the old file
			String tempFilePath = absoluteFilePath + "_";
			mp3file.save(tempFilePath);
			new File(absoluteFilePath).delete();
			File tempFile = new File(tempFilePath);
			tempFile.renameTo(new File(absoluteFilePath));
			myWriter.writeSuccess("Wrote ID3 Tags");
			controller.setLabelGreen("Wrote ID3 Tags");
		} catch (NotSupportedException | IOException e) {
			myWriter.writeError(e.getMessage());
		}
		moveFile(absoluteFilePath);
	}

	public static void moveFile(String filePath) {
		// get just the file name
		int filenameIndex = filePath.lastIndexOf(File.separator);
		String filename = filePath.substring(filenameIndex + 1);
		String newFilePath = user.getItunesLocation() + File.separator
				+ filename;

		Path initialFilePath = FileSystems.getDefault().getPath(filePath);
		Path postFilePath = FileSystems.getDefault().getPath(newFilePath);
		try {
			Files.move(initialFilePath, postFilePath,
					StandardCopyOption.REPLACE_EXISTING);
			myWriter.writeSuccess("Successfully Moved File!");
			controller.setLabelGreen("Enjoy Your New Song!!!");
			// Placed in conversion step because this means the program was
			// successful
			updateSongRecord();
		} catch (IOException e) {
			myWriter.writeError("Unable to move file");
			myWriter.writeError(e.getMessage());
		}
	}

	public static String getClassFilePath() {
		String in = YTDLMain.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		in = in.replaceAll("/bin", "");// go up a level
		in = in.replaceAll("%20", " ");// convert url spaces to string spaces
		String app = ".app/Contents/";
		if (in.contains(app)) { // if running in standalone program
			int index = in.indexOf(app);
			in = in.substring(0, index + app.length());// have path end in
														// .app/Contents/
		}
		return in;
	}

	// unmarshalls songRecord from file. If not found, return empty
	// observableList
	private static List<SongInformation> unmarshallSongRecord() {
		// variables
		String songRecordPath = getClassFilePath() + "Resources/SongRecord.xml";

		// make file object
		// if xml doesn't exist, create a new one
		songRecordFile = new File(songRecordPath);

		if (songRecordFile.length() == 0) { // QUICK way to see if empty
			myWriter.writeError("Song Record empty");
			return FXCollections.observableArrayList();
		}

		if (!songRecordFile.exists()) {
			try {
				songRecordFile.createNewFile();
			} catch (IOException e) {
				myWriter.writeError("Unable to create SongRecord.xml");
				myWriter.writeError(e.getMessage());
				e.printStackTrace();
			}
			return FXCollections.observableArrayList();
		}
		// load
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(SongInformationWrapper.class);
			Unmarshaller unm = context.createUnmarshaller();
			SongInformationWrapper wrapper;
			wrapper = (SongInformationWrapper) unm.unmarshal(songRecordFile);
			return wrapper.getSongInformationObjects();

		} catch (JAXBException e) {
			myWriter.writeError("Unable to unmarshall XML file");
			myWriter.writeError(e.getMessage());
			e.printStackTrace();
		}
		myWriter.writeError("SongRecord not found");
		return FXCollections.observableArrayList();
	}

	private static void updateSongRecord() {
		// update
		songRecord.add(YTDLMain.song);
	}

	// updates the SongRecord.xml file stored in the resources folder
	private static void saveSongRecord() {
		try {
			JAXBContext context = JAXBContext
					.newInstance(SongInformationWrapper.class);

			// save
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			SongInformationWrapper wrapper = new SongInformationWrapper();
			wrapper.setSongInformationObjects(songRecord);

			m.marshal(wrapper, songRecordFile);

		} catch (JAXBException e) {
			myWriter.writeError("Unable to save Song XML context");
			myWriter.writeError(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Getter / Setter methods
	 * 
	 * @param toSet
	 */
	// called in controller
	public static void setController(Controller toSet) {
		controller = toSet;
	}

	// called in controller
	public static void setTextFlowWriter(TextFlowWriter toSet) {
		myWriter = toSet;
	}

	// Run when start fails
	public static void main(String[] args) {
		launch(args);
	}

}
