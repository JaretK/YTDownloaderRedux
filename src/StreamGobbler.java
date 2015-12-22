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

public class StreamGobbler extends Thread {

	InputStream is;
	ConcurrentLinkedQueue<String> q;

	// terminate sequence
	boolean cancelRun = false;

	public StreamGobbler(InputStream is) {
		this.is = is;
		this.q = new ConcurrentLinkedQueue<>();
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
				q.add(line);
			}
		} catch (IOException ioe) {
			q.add(ioe.getMessage());
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
}
