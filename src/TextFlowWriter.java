import javafx.scene.text.TextFlow;

/**
 * Extension of the TextFlow class to provide easier writing capabilities
 * 
 * @author jkarnuta
 *
 */
public class TextFlowWriter extends AbstractTextFlowWriter{

	public TextFlowWriter(TextFlow tf) {
		super(tf);
	}

	@Override
	public void writeWelcome() {
		// Static messages
		final String welcome = "Hello "+System.getProperty("user.name")+". Welcome to YTDLRedux.";
		super.writeInfo(welcome);
	}
}
