import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;

public class Controller implements Initializable {

	// FXML variable handles
	@FXML
	public Menu fileMenu, preferencesMenu;

	@FXML
	public MenuItem closeMenuItem, preferencesMenuItem;

	@FXML
	public Button cancelButton, clearButton, getButton;

	@FXML
	public TextFlow textFlow;

	@FXML
	public ScrollPane textFlowContainer;

	@FXML
	public Label songLabel, artistLabel, ytidLabel, infoLabel;

	@FXML
	public TextField songField, artistField, ytidField;

	@FXML
	public ProgressBar progressBar;

	// private dynamic
	private TextFlowWriter myWriter;

	// Colors
	public static final Color GREEN = Color.web("#008A00");
	public static final Color RED = Color.web("#CC1E00");
	public static final Color BLUE = Color.web("#235F9C");

	// Static messages
	public static final String WELCOME = "Welcome. Input Song Information.";
	
	public static final boolean DEBUG = false;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

		// Set YTDLMain controller handle
		myWriter = new TextFlowWriter(this.textFlow);
		YTDLMain.setController(this);
		YTDLMain.setTextFlowWriter(myWriter);

		// initialize TextFlow auto-orientation
		textFlow.getChildren().addListener(
				(ListChangeListener<Node>) ((change) -> {
					textFlow.layout();
					textFlowContainer.layout();
					textFlowContainer.setVvalue(1.0f);
				}));
		textFlow.setPrefWidth(Region.USE_COMPUTED_SIZE);
		textFlow.setPrefHeight(Region.USE_COMPUTED_SIZE);

		// initialize infoLabel
		setLabelGreen(WELCOME);
		
		//welcome user
		myWriter.writeWelcome();
		
		if(DEBUG){
			songField.setText("next to me");
			artistField.setText("OTTO KNOWS");
			ytidField.setText("VD9zmaX4ez8");
		}
	}

	// Methods that alter FXML objects
	public void setLabelGreen(String text) {
		Platform.runLater(() -> {
			infoLabel.setTextFill(GREEN);
			infoLabel.setText(text);
		});
	}
	
	public void setLabelBlue(String text) {
		Platform.runLater(() -> {
			infoLabel.setTextFill(BLUE);
			infoLabel.setText(text);
		});
	}

	public void setLabelRed(String text) {
		Platform.runLater(() -> {
			infoLabel.setTextFill(RED);
			infoLabel.setText(text);
		});
	}
	
	public void bindProgressBar(SimpleDoubleProperty progressProperty){
		Platform.runLater(()->{
			progressBar.progressProperty().bind(progressProperty);
		});
	}
	public void bindProgressBar(ReadOnlyDoubleProperty progressProperty){
		Platform.runLater(()->{
			progressBar.progressProperty().bind(progressProperty);
		});
	}
	public void unbindProgressBar(){
		Platform.runLater(()->{
			progressBar.progressProperty().unbind();
		});
	}
	public void bindLabelMessageProperty(ReadOnlyStringProperty property){
		Platform.runLater(()->{
			infoLabel.setTextFill(BLUE);
			infoLabel.textProperty().bind(property);
		});
	}
	public void unbindLabelMessageProperty(){
		Platform.runLater(()->{
			infoLabel.textProperty().unbind();
		});
	}

	// Methods that have hook within FXML document
	public void exit() {
		YTDLMain.exit(0);
	}

	//Reinitializes GUI
	public void clearFields() {
		songField.setText("");
		artistField.setText("");
		ytidField.setText("");
		myWriter.clear();
		setLabelGreen(WELCOME);
		songField.requestFocus();
	}

	public void getSong() {
		if (fieldsInvalid()) {
			myWriter.writeError("One or more of the fields are invalid");
		}

		else {
			SongInformation song = new SongInformation(songField.getText(),
					artistField.getText(), ytidField.getText());
			YTDLMain.getSong(song);
		}
	}

	private boolean fieldsInvalid() {
		return songField.getText().isEmpty() || artistField.getText().isEmpty()
				|| ytidField.getText().isEmpty();
	}
}
