import javafx.concurrent.Task;


public class extractAudio extends Task<Void> {

	//location of FFMPEG binary used to convert file
	private final String ffmpegBinaryPath;
	//file to be converted
	private final String absolutePath;
	//location of converted file
	private final String convertedPath;
	
	private FFMPEGStreamGobbler gobbler;
	
	public extractAudio(String ffmpegPath, String absoluteFilePath, String convertedFilePath){
		this.ffmpegBinaryPath = ffmpegPath;
		this.absolutePath = absoluteFilePath;
		this.convertedPath = convertedFilePath;
	}

	
	@Override
	protected Void call() throws Exception {
		ProcessBuilder proc = new ProcessBuilder();
		proc.command(ffmpegBinaryPath, "-i", absolutePath, "-vn",
				"-acodec", "libmp3lame", "-ac", "2", "-ab", "160k",
				"-ar", "48000", convertedPath);
		Process p = proc.start();
		// Custom FFMPEGStreamGobbler
		// FFMPEG outputs all information to error stream (dafuq)
		gobbler = new FFMPEGStreamGobbler(
				p.getErrorStream());
		gobbler.start();
		while (p.isAlive()) {
			updateProgress(gobbler.getProgress(), 1);
		}
		return null;
	}

	public FFMPEGStreamGobbler getGobbler(){
		return gobbler;
	}
	
}
