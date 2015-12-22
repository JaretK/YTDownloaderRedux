import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public abstract class AbstractTextFlowWriter {

	private final TextFlow myFlow;

	public AbstractTextFlowWriter(TextFlow tf) {
		myFlow = tf;
	}

	/**
	 * Colors used throughout for styling purposes
	 */

	public static final Color BLUE = Controller.BLUE;
	public static final Color GREEN = Controller.GREEN;
	public static final Color RED = Controller.RED;

	/**
	 * TEXTFLOW METHODS
	 */

	public void clear() {
		Platform.runLater(() -> {
			myFlow.getChildren().clear();
		});
		// Shift TextFlow down a line
		this.writeLine("");
	}
	
	public void write(Text... text){
		for (Text t : text){
			addText(t);
		}
	}

	public void writeLine(String line) {
		Text text = new Text(line + "\n");
		addText(text);
	}

	public void appendText(String toAppend) {
		Text test = new Text(toAppend);
		addText(test);
	}

	public void writeInfo(String infoText) {
		Text text = new Text(infoText + "\n");
		text.setFill(BLUE);
		addText(text);
	}

	public void writeSuccess(String successText) {
		Text text = new Text(successText + "\n");
		text.setFill(GREEN);
		addText(text);
	}

	public void writeError(String errorText) {
		Text text = new Text(errorText + "\n");
		text.setFill(RED);
		text.setStyle("-fx-stroke:Black");
		text.setStyle("-fx-stroke-width:0.1");
		addText(text);
	}

	public void addArray(Text[] textLines, TextFlow tf) {
		for (Text ele : textLines) {
			addText(ele);
		}
	}

	public synchronized void removeLast() {
		Platform.runLater(() -> {
			myFlow.getChildren().remove(myFlow.getChildren().size() - 1);
		});
	}

	private synchronized void addText(Text text) {
		Platform.runLater(() -> {
			myFlow.getChildren().add(text);
		});
	}
	
	public abstract void writeWelcome();
}
