package client.gui;

import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import client.WAMClient;
import common.Observer;
import common.WAMBoard;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The GUI frontend for the multiplayer Whack-A-Mole game
 * 
 * Project: project02-0610-hit-or-miss
 * File: WAMGUI.java, Created Apr 9, 2019
 * 
 * @author Giovanni Melchionne <gtm7712@rit.edu>
 * @author Kirsten Auble <kca2085@rit.edu>
 *
 */
public class WAMGUI extends Application implements Observer<WAMBoard> {

	// Fields
	private WAMClient client;
	private Label score;
	private Button[][] buttons;

	private Random random;


	@Override
	public void init() {
		try {
			List<String> args = getParameters().getRaw();

			String server = args.get(0);
			int port = Integer.parseInt(args.get(1));

			client = new WAMClient(server, port);
			client.getBoard().addObserver(this);

			this.score = new Label("Score: ");
			this.score.setStyle("-fx-font: 24 arial;");
			this.score.setPadding(new Insets(10));
			this.buttons = new Button[client.getBoard().getRows()][client.getBoard().getCols()];

			random = new Random();

		} catch (NumberFormatException e) {
			System.err.println(e);
			throw new RuntimeException(e);
		}

	}


	/**
	 * Helper method to create the board so we don't
	 * have to do it all in our start method.
	 */
	public VBox createBoard() {
		int numRow = client.getBoard().getRows();
		int numCol = client.getBoard().getCols();
		VBox result = new VBox();
		GridPane gridpane = new GridPane();
		for (int row = 0; row < numRow; row++) {
			for (int col = 0; col < numCol; col++) {
				final int r = row; // For use in button action
				final int c = col; // For use in button action
				Button button = new Button();
				button.setOnAction(e -> {
					client.whack(r, c);
				});
				Image image = new Image(getClass().getResourceAsStream("/client/gui/assets/empty.png"));
				ImageView view = new ImageView(image);
				view.setFitHeight(128);
				view.setFitWidth(128);
				button.setGraphic(view);
				gridpane.add(button, row, col);
				buttons[row][col] = button;
			}
		}

		result.getChildren().add(gridpane);
		result.getChildren().add(score);
	
		return result;
	}


	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Ouch! Says the Mole");
		VBox pane = createBoard();
		Scene scene = new Scene(pane, 440, 600);
		stage.setWidth(460);
		stage.setHeight(620);
		stage.setResizable(false);
		stage.setMaxWidth(460);
		stage.setMaxHeight(620);

		stage.setOnCloseRequest(e -> {

		});

		stage.setScene(scene);
		stage.show();
	}

	private boolean canMeme = true;


	private void guiUpdate(WAMBoard subject, int row, int col) {
		if (subject.getSpot(row, col)) {

			Image image;

			if (client.getNumMoles() < 13)
				image = new Image(getClass().getResourceAsStream("/client/gui/assets/mole.png"));
			else if (canMeme && random.nextInt(100) < 5) {
				image = new Image(getClass().getResourceAsStream("/client/gui/assets/special.png"));
				try {
					AudioInputStream ais = AudioSystem
							.getAudioInputStream(getClass().getResourceAsStream("/client/gui/assets/g.wav"));
					Clip clip = AudioSystem.getClip();
					clip.open(ais);
					clip.start();
					canMeme = false;
				} catch (Exception e) {
				}
			} else
				image = new Image(getClass().getResourceAsStream("/client/gui/assets/mole.png"));

			ImageView view = new ImageView(image);
			view.setFitHeight(128);
			view.setFitWidth(128);
			buttons[row][col].setGraphic(view);

		} else {
			Image image = new Image(getClass().getResourceAsStream("/client/gui/assets/empty.png"));
			ImageView view = new ImageView(image);
			view.setFitHeight(128);
			view.setFitWidth(128);
			buttons[row][col].setGraphic(view);
		}

		if (subject.getStatus() == "RUN") {
			score.setText("Score: " + subject.getScore());
		} else {
			score.setText(subject.getStatus());
		}
	}


	@Override
	public void update(WAMBoard subject, int row, int col) {
		if (Platform.isFxApplicationThread()) {
			this.guiUpdate(subject, row, col);
		} else {
			Platform.runLater(() -> this.guiUpdate(subject, row, col));
		}
	}


	/**
	 * Eclipse is fun, so here's a main method.
	 * It doesn't do much.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: wrong arguments.");
		} else {
			Application.launch(args);
		}
	}

}
