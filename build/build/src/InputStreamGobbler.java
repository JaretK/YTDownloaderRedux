import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class InputStreamGobbler extends Thread {

	InputStream is;
	ConcurrentLinkedQueue<String> q;

	// youtube-dl tags for input handling
	final String download = "[download]";
	final String youtube = "[youtube]";
	int numberDownload = 0;
	final Pattern percentFinder = Pattern.compile("(\\[download\\])(.+?)(\\%)");
	final String filenameHeader = "[download] Destination:";

	// properties
	public final SimpleDoubleProperty percentProperty;
	public final SimpleStringProperty message;

	// filename
	public String filename = "";

	// terminate sequence
	boolean cancelRun = false;

	public InputStreamGobbler(InputStream is) {
		this.is = is;
		this.q = new ConcurrentLinkedQueue<>();
		this.percentProperty = new SimpleDoubleProperty(0.0);
		this.message = new SimpleStringProperty("Downloading...");
	}

	public void run() {
		numberDownload = 0;
		boolean success = false;

		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				if (cancelRun) {
					message.set("Canceled");
					return;
				}
				if (line.contains("has already been downloaded")) {
					message.set("Already Downloaded");
					terminate();
					q.add("Already Downloaded");
					return;
				} else if (line.startsWith(youtube)) {
					// can add to q... looks a little messy so I won't
					continue;
				} else if (line.startsWith(download)) {
					// get file name
					if (numberDownload == 0) {
						int indexOfHeader = line.indexOf(filenameHeader);
						filename = line.substring(
								indexOfHeader + filenameHeader.length()).trim();
						numberDownload++;
					} else { // get percent completed
						numberDownload++;
						Matcher m = percentFinder.matcher(line);
						m.find();
						percentProperty
								.setValue(Double.parseDouble(m.group(2)) / 100);
					}
				}
			}
			success = true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			message.set(ioe.getMessage());
			success = false;
		}
		if (success) {
			message.set("Successfully downloaded!");
		}
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

	public ReadOnlyDoubleProperty getProgressProperty() {
		return percentProperty;
	}

	public ReadOnlyStringProperty getMessageProperty() {
		return message;
	}

	public String getMessage() {
		return message.getValue();
	}
}
