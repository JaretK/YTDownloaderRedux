import java.io.File;

import javafx.concurrent.Task;

/**
 * Gets the song information from the text fields. Downloads the song from
 * youtube Converts the song into mp3 format Changes the ID3 tags to display
 * song metadata
 * 
 * @author jkarnuta
 *
 */
public class GetSongModel {

	public static final String resourcesLocation = getClassFilePath();
	public static final String YOUTUBEDL_COMMAND = resourcesLocation
			+ "Resources/youtube-dl";

	public static final String YOUTUBE_TEMPLATE = "https://www.youtube.com/watch?v=";

	private final SongInformation song;
	private final UserInformation user;

	public GetSongModel(SongInformation song, UserInformation user) {
		this.song = song;
		this.user = user;
	}

	public Task<String> downloadSong(TextFlowWriter writer) {
		Task<String> downloadTask = new Task<String>() {
			@Override
			protected String call() throws Exception {
				ProcessBuilder pb = new ProcessBuilder();
				pb.directory(new File(user.tempLocation));

				// TODO: Add intelligence to selecting -f option. Some videos
				// might not have a 22 option (unlikely, but MIGHT happen)

				// build command
				// specify that you are selecting a certain file to download
				String fflag = "-f";
				// specify that we want to download the highest quality mp4
				// stream
				String hd720 = "22";
				// Build command
				pb.command(YOUTUBEDL_COMMAND, fflag, hd720, YOUTUBE_TEMPLATE
						+ song.getYtid());
				// Start process
				Process p = pb.start();

				// if any error should occur, set this to false
				boolean cleanRun = true;

				// init gobblers
				InputStreamGobbler inputGobbler = new InputStreamGobbler(
						p.getInputStream());
				StreamGobbler errorGobbler = new StreamGobbler(
						p.getErrorStream());

				// start gobblers
				inputGobbler.start();
				errorGobbler.start();

				// UI notify loop
				while (p.isAlive()) {

					// update progress
					double progress = inputGobbler.getProgressProperty()
							.getValue();
					updateProgress(progress, 1);

					// update messsage with input gobbler's message
					if (cleanRun) {
						updateMessage(inputGobbler.getMessage());
					} else {
						updateMessage("ERROR");
					}
					// dump inputGobbler
					while (!inputGobbler.isEmpty()) {
						writer.writeInfo(inputGobbler.poll());
					}
					// dump errorGobbler
					while (!errorGobbler.isEmpty()) {
						writer.writeError(errorGobbler.poll());
						updateMessage("ERROR");
						cleanRun = false;// error occured
					}

				}
				// terminate gobbers (doesn't matter)
				inputGobbler.terminate();
				errorGobbler.terminate();
				// return value
				return inputGobbler.filename;
			}
		};
		// return task
		return downloadTask;
	}

	public static String getClassFilePath() {
		String in = GetSongModel.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		in = in.replaceAll("/bin", "");
		in = in.replaceAll("%20", " ");
		String app = ".app/Contents/";
		if (in.contains(app)) {
			int index = in.indexOf(app);
			in = in.substring(0, index + app.length());
		}
		return in;
	}

	public SongInformation getSongInformation() {
		return song;
	}

	public UserInformation getUserInformation() {
		return user;
	}
}
