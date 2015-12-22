import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class FFMPEGStreamGobbler extends Thread {

	InputStream is;
	ConcurrentLinkedQueue<String> q;

	// properties
	public final SimpleDoubleProperty percentProperty;
	public final SimpleStringProperty message;

	// regex for duration and size
	Pattern durationPattern = Pattern
			.compile("[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{2}");

	// line beginning strings
	private String totalDuration = "  Duration: ";
	private String encoding = "size=";
	private double totalDurationMilliSeconds = 0.0;

	// terminate sequence
	boolean cancelRun = false;

	public FFMPEGStreamGobbler(InputStream is) {
		this.is = is;
		this.q = new ConcurrentLinkedQueue<>();
		this.percentProperty = new SimpleDoubleProperty(0.0);
		this.message = new SimpleStringProperty("Encoding...");
	}

	public void run() {

		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				if (cancelRun) {
					return;
				}
				if (line.startsWith(totalDuration)) {
					totalDurationMilliSeconds = extractDuration(line);
				}
				if(line.startsWith(encoding)){
					percentProperty.set(extractDuration(line)/totalDurationMilliSeconds);
				}
			}
		} catch (IOException ioe) {
			q.add(ioe.getMessage());
		}
	}

	// extracts a duration based off the regex defined by durationPattern
	// Returns the number of milliseconds that corresponds to the given duration
	private double extractDuration(String line) {
		Matcher m = durationPattern.matcher(line);
		String found = "";
		if (m.find()) {
			found = m.group(0);
		}
		//format is now HH:MM:SS:%ofSecond
		String[] hms = found.split(":");
		String[] sms = hms[2].split("\\.");
		double totalMilliSeconds = 0;
		totalMilliSeconds += Double.parseDouble(sms[1]) * 10;// Add milliseconds
		totalMilliSeconds += Double.parseDouble(sms[0]) * 1000; // Add seconds
		totalMilliSeconds += Double.parseDouble(hms[1]) * 60 * 1000; // Add
																	// minutes
		totalMilliSeconds += Double.parseDouble(hms[0]) * 60 * 60 * 1000; // Add
																			// hours
		return totalMilliSeconds;
	}
	
	public double getProgress(){
		return percentProperty.get();
	}

	public boolean isEmpty() {
		return q.isEmpty();
	}

	public String poll() {
		return q.poll();
	}

	public void terminate() {
		cancelRun = true;
	}
}
