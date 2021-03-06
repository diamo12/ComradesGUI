package edu.purdue.comradesgui.javafx;

import edu.purdue.comradesgui.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class FXComradesGUI extends Application {

	public ComradesMain comradesMain;

	private FXChessBoard chessBoard;

	private TitledPane playerSetupPane;

	private GridPane gameInfoGrid;

	private ComboBox<ChessPlayer> whitePlayerCombo;
	private ComboBox<ChessPlayer> blackPlayerCombo;
	private CheckBox useInfiniteTimer;
	private CheckBox useTimerCheckBox;
	private CheckBox useTimerDelay;
	private CheckBox useDelayAsBuffer;
	private Button startGameButton;
	private Button stopTurnButton;
	private TextField timerDurationTextField;
	private TextField timerDelayTextField;

	private Label blackTimerLabel;
	private Label whiteTimerLabel;

	private Stage optionsStage;
	
	public FXComradesGUI() {
		comradesMain = new ComradesMain();
	}

	private void updateButtons() {

		if(!comradesMain.getCurrentGame().isGameStarted()) {

			boolean enableStart = true;

			if(whitePlayerCombo.getValue() == null)
				enableStart = false;

			if(blackPlayerCombo.getValue() == null)
				enableStart = false;

			if(useTimerCheckBox.isSelected()) {
				timerDurationTextField.setDisable(false);
				useTimerDelay.setDisable(false);
				if(!gameInfoGrid.getChildren().contains(blackTimerLabel))
					gameInfoGrid.add(blackTimerLabel, 0, 5);
				if(!gameInfoGrid.getChildren().contains(whiteTimerLabel))
					gameInfoGrid.add(whiteTimerLabel, 0, 6);
			}
			else {
				timerDurationTextField.setDisable(true);
				useTimerDelay.setDisable(true);
				gameInfoGrid.getChildren().remove(blackTimerLabel);
				gameInfoGrid.getChildren().remove(whiteTimerLabel);
			}

			if(useTimerDelay.isSelected() && !useTimerDelay.isDisabled()) {
				useDelayAsBuffer.setDisable(false);
				timerDelayTextField.setDisable(false);
			}
			else {
				useDelayAsBuffer.setDisable(true);
				timerDelayTextField.setDisable(true);
			}

			startGameButton.setDisable(!enableStart);
		}
		else {
			playerSetupPane.setDisable(true);
		}
	}

	public void refreshPlayerCombos() {

		ChessPlayer whiteSelect = whitePlayerCombo.getSelectionModel().getSelectedItem();

		if(whiteSelect != null) {
			whitePlayerCombo.getSelectionModel().clearSelection();
			whitePlayerCombo.setValue(whiteSelect);
		}

		ChessPlayer blackSelect = blackPlayerCombo.getSelectionModel().getSelectedItem();

		if(blackSelect  != null) {
			blackPlayerCombo.getSelectionModel().clearSelection();
			blackPlayerCombo.setValue(blackSelect);
		}
	}

	private boolean updatePlayerSelection(ComboBox<ChessPlayer> inCombo, ComboBox<ChessPlayer> otherCombo) {

		boolean success = true;

		ChessGame chessGame = comradesMain.getCurrentGame();

		if(inCombo.getValue() != null) {

			if(otherCombo.getValue() != null) {

				if(inCombo.getValue() == otherCombo.getValue()) {

					ChessPlayer selected = inCombo.getValue();

					if(selected.getPlayerType() == ChessPlayer.PlayerType.ENGINE) {
						inCombo.getSelectionModel().clearSelection();
						String msg = "You selected the same engine to play against itself, a copy of it must be added to continue.";
						Alert promptDuplicate = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK, ButtonType.CANCEL);
						promptDuplicate.setHeaderText("Duplicate Engine?");
						promptDuplicate.setTitle("Duplicate Engine?");
						promptDuplicate.showAndWait();

						if(promptDuplicate.getResult() == ButtonType.OK) {
							ChessEngine engine = (ChessEngine) selected;
							ChessEngine engineCopy = engine.copyEngine();

							comradesMain.addPlayer(engineCopy);
							inCombo.setValue(engineCopy);
						}
						else
							success = false;
					}
					else {
						
						
					}
				}
			}
		}

		if(inCombo == blackPlayerCombo)
			chessGame.setBlackPlayer(inCombo.getValue());
		else
			chessGame.setWhitePlayer(inCombo.getValue());

		return success;
	}

	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("ComradesGUI - FX!");
		primaryStage.setResizable(false);



		MenuBar menuBar = new MenuBar();

		Menu fileMenu = new Menu("File");
		Menu editMenu = new Menu("Edit");

		MenuItem importEngineButton = new MenuItem("Import Engine");
		MenuItem saveAsButton = new MenuItem("Save as");
		MenuItem optionsButton = new MenuItem("Options");
		MenuItem setFENButton = new MenuItem("Set FEN");

		fileMenu.getItems().add(importEngineButton);
		fileMenu.getItems().add(saveAsButton);
		fileMenu.getItems().add(optionsButton);

		editMenu.getItems().add(setFENButton);

		menuBar.getMenus().add(fileMenu);
		menuBar.getMenus().add(editMenu);

		VBox menuBarVBox = new VBox(menuBar);
		Scene scene = new Scene(menuBarVBox, 900, 625);

		HBox horizontalBox = new HBox();
		VBox verticalBox = new VBox();

		TitledPane boardPane = new TitledPane();
		TitledPane gameInfoPane = new TitledPane();

		GridPane mainDisplayGrid = new GridPane();
		GridPane playerSetupGrid = new GridPane();

		Label blackComboLabel = new Label("Black Player:");
		Label whiteComboLabel = new Label("White Player:");
		Label versusText = new Label("vs.");
		Label timerDurationLabel = new Label("Timer Duration:");
		Label timerIncrementLabel = new Label("Timer Delay:");
		Label currentPlayerLabel = new Label("Current Player:");
		Label currentTurnLabel = new Label("Turn Number:");
		Label deadPiecesWhiteLabel = new Label("Dead White:");
		Label deadPiecesBlackLabel = new Label("Dead Black:");
		Text deadPiecesWhiteText = new Text();
		Text deadPiecesBlackText = new Text();
		Text currentPlayerText = new Text();
		Text currentTurnText = new Text();
		Text blackTimerFeed = new Text();
		Text whiteTimerFeed = new Text();

		playerSetupPane = new TitledPane();

		gameInfoGrid = new GridPane();

		chessBoard = new FXChessBoard(500, comradesMain.getCurrentGame());

		whitePlayerCombo = new ComboBox<>();
		blackPlayerCombo = new ComboBox<>();
		useInfiniteTimer = new CheckBox("Infinite Analysis");
		useTimerCheckBox = new CheckBox("Use Timers");
		useTimerDelay = new CheckBox("Enable Delay");
		useDelayAsBuffer = new CheckBox("Use as Buffer?");
		startGameButton = new Button("Start Game!");
		stopTurnButton = new Button("Force Turn");
		timerDurationTextField = new TextField();
		timerDelayTextField = new TextField();
		blackTimerLabel = new Label("Black Clock:");
		whiteTimerLabel = new Label("White Clock:");

		boardPane.setText("Game Board");
		boardPane.setCollapsible(false);
		boardPane.setAlignment(Pos.TOP_LEFT);
		boardPane.setContent(chessBoard);

		playerSetupPane.setMinWidth(300);
		playerSetupPane.setContent(playerSetupGrid);
		playerSetupPane.setText("Player Setup");
		playerSetupPane.setCollapsible(false);
		playerSetupPane.setAlignment(Pos.TOP_LEFT);

		gameInfoPane.setText("Game Info");
		gameInfoPane.setContent(gameInfoGrid);
		gameInfoPane.setCollapsible(false);
		gameInfoPane.setAlignment(Pos.TOP_LEFT);

		mainDisplayGrid.setHgap(8);
		mainDisplayGrid.setVgap(8);
		mainDisplayGrid.setPadding(new Insets(16, 16, 16, 16));
		mainDisplayGrid.setAlignment(Pos.TOP_LEFT);

		playerSetupGrid.setHgap(8);
		playerSetupGrid.setVgap(8);
		playerSetupGrid.setPadding(new Insets(16, 16, 16, 16));
		playerSetupGrid.setAlignment(Pos.TOP_LEFT);

		gameInfoGrid.setHgap(8);
		gameInfoGrid.setVgap(8);
		gameInfoGrid.setMinWidth(250);
		gameInfoGrid.setPadding(new Insets(16, 16, 16, 16));
		gameInfoGrid.setAlignment(Pos.TOP_LEFT);

		horizontalBox.setSpacing(4);
		horizontalBox.setPadding(new Insets(4, 4, 4, 4));

		verticalBox.setSpacing(4);

		whitePlayerCombo.setMaxWidth(200);
		whitePlayerCombo.setPromptText("<none selected>");
		whitePlayerCombo.setItems(comradesMain.getPlayerList());

		blackPlayerCombo.setMaxWidth(200);
		blackPlayerCombo.setItems(comradesMain.getPlayerList());
		blackPlayerCombo.setPromptText("<none selected>");

		whiteComboLabel.setGraphic(whitePlayerCombo);
		whiteComboLabel.setContentDisplay(ContentDisplay.RIGHT);

		blackComboLabel.setGraphic(blackPlayerCombo);
		blackComboLabel.setContentDisplay(ContentDisplay.RIGHT);

		timerDurationLabel.setGraphic(timerDurationTextField);
		timerDurationLabel.setContentDisplay(ContentDisplay.RIGHT);
		timerIncrementLabel.setGraphic(timerDelayTextField);
		timerIncrementLabel.setContentDisplay(ContentDisplay.RIGHT);

		ObservableStringValue blackProp = Bindings.when(comradesMain.getCurrentGame().getBlackTurnProperty()).then("BLACK").otherwise("NONE");
		ObservableStringValue whiteProp = Bindings.when(comradesMain.getCurrentGame().getWhiteTurnProperty()).then("WHITE").otherwise(blackProp);

		currentPlayerText.textProperty().bind(whiteProp);
		currentPlayerLabel.setGraphic(currentPlayerText);
		currentPlayerLabel.setContentDisplay(ContentDisplay.RIGHT);

		currentTurnText.textProperty().bind(comradesMain.getCurrentGame().getTurnCountProperty().asString());
		currentTurnLabel.setGraphic(currentTurnText);
		currentTurnLabel.setContentDisplay(ContentDisplay.RIGHT);

		blackTimerLabel.setGraphic(blackTimerFeed);
		blackTimerLabel.setContentDisplay(ContentDisplay.RIGHT);
		whiteTimerLabel.setGraphic(whiteTimerFeed);
		whiteTimerLabel.setContentDisplay(ContentDisplay.RIGHT);

		deadPiecesBlackText.setFont(Font.font(chessBoard.getBoardFont().getFamily(), 14));
		deadPiecesWhiteText.setFont(Font.font(chessBoard.getBoardFont().getFamily(), 14));

		deadPiecesBlackText.setWrappingWidth(200);
		deadPiecesWhiteText.setWrappingWidth(200);

		deadPiecesBlackLabel.setGraphic(deadPiecesBlackText);
		deadPiecesBlackLabel.setContentDisplay(ContentDisplay.RIGHT);

		deadPiecesWhiteLabel.setGraphic(deadPiecesWhiteText);
		deadPiecesWhiteLabel.setContentDisplay(ContentDisplay.RIGHT);

		comradesMain.getCurrentGame().getDeadWhitePieces().addListener((ListChangeListener<ChessPiece>) pieceChange -> {
			String str = "";
			for(ChessPiece piece : comradesMain.getCurrentGame().getDeadWhitePieces())
				str += chessBoard.getSkinnedPiece(piece.getPieceChar());
			deadPiecesWhiteText.setText(str);
		});


		comradesMain.getCurrentGame().getDeadBlackPieces().addListener((ListChangeListener<ChessPiece>) pieceChange -> {
			String str = "";
			for(ChessPiece piece : comradesMain.getCurrentGame().getDeadBlackPieces())
				str += chessBoard.getSkinnedPiece(piece.getPieceChar());
			deadPiecesBlackText.setText(str);
		});

		importEngineButton.setOnAction((actionEvent) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select engine executable...");
			File file = fileChooser.showOpenDialog(primaryStage);

			if(file != null) {
				ChessEngine engine = new ChessEngine();
				engine.loadFromPath(file.getAbsolutePath());

				if(engine.hasLoadedFromFile())
					comradesMain.addPlayer(engine);
			}
		});

		saveAsButton.setOnAction((actionEvent) -> {

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save PGN...");
			File file = fileChooser.showSaveDialog(primaryStage);

			if(file != null) {

				PrintWriter out = null;
				try {
					out = new PrintWriter(file);
					System.out.println("File '" + file.getName() + "' created.");
				}
				catch (FileNotFoundException e) {
					System.out.println("!! Error: Could not created run file '" + e.getMessage() + "' !!");
				}

				if(out != null) {
					out.println(comradesMain.getCurrentGame().generateStringPGN());
					out.close();
				}
			}
		});

		optionsButton.setOnAction((actionEvent) -> {

			if(optionsStage != null) {
				try {
					optionsStage.show();
					optionsStage.requestFocus();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			else {
				optionsStage = new Stage();
				FXChessOptions optionsPanel = new FXChessOptions(this);
				optionsPanel.start(optionsStage);
			}
		});

		setFENButton.setOnAction((actionEvent) -> {

			TextInputDialog dialog = new TextInputDialog(comradesMain.getCurrentGame().generateStringFEN());
			dialog.setTitle("Set Board FEN");
			dialog.setHeaderText("Set Board FEN");
			dialog.setContentText("FEN:");
			dialog.getDialogPane().setPrefWidth(450);

			Optional<String> result = dialog.showAndWait();

			result.ifPresent((inFEN) -> comradesMain.getCurrentGame().setBoardFromFEN(inFEN));
		});


		whitePlayerCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

			boolean success = updatePlayerSelection(whitePlayerCombo, blackPlayerCombo);
			if(!success)
				whitePlayerCombo.setValue(oldValue);
			else
				updateButtons();
		});

		blackPlayerCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

			boolean success = updatePlayerSelection(blackPlayerCombo, whitePlayerCombo);
			if(!success)
				blackPlayerCombo.setValue(oldValue);
			else
				updateButtons();
		});
		
		useInfiniteTimer.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			
			
			ChessPlayer tempBlackPlayer = comradesMain.getCurrentGame().getBlackPlayer();
			if(tempBlackPlayer != null) {
				//Is the player a chess engine?
				if(tempBlackPlayer instanceof ChessEngine) {
					
					//Tell the computer the player is a chess engine, store that in a variable called whiteEngine
					ChessEngine blackEngine = (ChessEngine) tempBlackPlayer;
				
					//Tell the chess engine to enable/disable infinite analysis
					blackEngine.getGoCommandBuilder().setInfinite(newValue);
				}	
			
			}
			
			
			//Get the current white player from the game 
			ChessPlayer tempWhitePlayer = comradesMain.getCurrentGame().getWhitePlayer();
			
			//Is there a valid player?
			if(tempWhitePlayer != null) {
		
				//Is the player a chess engine?
				if(tempWhitePlayer instanceof ChessEngine) {
					
					//Tell the computer the player is a chess engine, store that in a variable called whiteEngine
					ChessEngine whiteEngine = (ChessEngine) tempWhitePlayer;
				
					//Tell the chess engine to enable/disable infinite analysis
					whiteEngine.getGoCommandBuilder().setInfinite(newValue);
				}	
			}
			
		}));

		useTimerCheckBox.selectedProperty().addListener(((observable, oldValue, newValue) -> {

			if(newValue) {
				blackTimerFeed.fillProperty().bind(Bindings.when(comradesMain.getCurrentGame().getBlackTimer().getBufferCountDownProperty()).then(Color.GREEN).otherwise(Color.DODGERBLUE));
				blackTimerFeed.textProperty().bind(comradesMain.getCurrentGame().getBlackTimer().getTimerDisplayProperty());
				whiteTimerFeed.fillProperty().bind(Bindings.when(comradesMain.getCurrentGame().getWhiteTimer().getBufferCountDownProperty()).then(Color.GREEN).otherwise(Color.DODGERBLUE));
				whiteTimerFeed.textProperty().bind(comradesMain.getCurrentGame().getWhiteTimer().getTimerDisplayProperty());

				timerDurationTextField.textProperty().setValue("" + comradesMain.getCurrentGame().getTimerDuration());
			}
			comradesMain.getCurrentGame().setUseTimers(newValue);
			updateButtons();
		}));

		useTimerDelay.selectedProperty().addListener(((observable, oldValue, newValue) -> {

			if(newValue) {
				timerDelayTextField.textProperty().setValue("" + comradesMain.getCurrentGame().getTimerDelay());
			}
			comradesMain.getCurrentGame().setUseTimerDelay(newValue);
			updateButtons();
		}));

		useDelayAsBuffer.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			comradesMain.getCurrentGame().setUseDelayAsBuffer(newValue);
			updateButtons();
		}));

		timerDurationTextField.textProperty().addListener(((observable, oldValue, newValue)  -> {

			boolean useOld = true;

			if(newValue != null) {
				if(!newValue.isEmpty()) {

					String[] split = newValue.split(":");

					if(newValue.matches("\\d*")) {
						long lVal = Long.valueOf(newValue);

						long hours = TimeUnit.MILLISECONDS.toHours(lVal);
						long minutes = TimeUnit.MILLISECONDS.toMinutes(lVal) % 60;
						long seconds = TimeUnit.MILLISECONDS.toSeconds(lVal) % 60;

						timerDurationTextField.setText(hours + ":" + minutes + ":" + seconds);
						useOld = false;
					}
					else if(split.length == 3) {

						boolean goodFormat = true;

						long hoursVal = -1;
						long minutesVal = -1;
						long secondsVal = -1;

						if(!split[0].matches("\\d*"))
							goodFormat = false;
						else
							hoursVal = TimeUnit.HOURS.toMillis(Long.parseLong(split[0]));

						if(!split[1].matches("\\d*"))
							goodFormat = false;
						else
							minutesVal = TimeUnit.MINUTES.toMillis(Long.parseLong(split[1]));

						if(!split[2].matches("\\d*"))
							goodFormat = false;
						else
							secondsVal = TimeUnit.SECONDS.toMillis(Long.parseLong(split[2]));

						if(goodFormat) {

							long totalValue = hoursVal + minutesVal + secondsVal;

							long minutesNew = TimeUnit.MILLISECONDS.toMinutes(minutesVal);
							long secondsNew = TimeUnit.MILLISECONDS.toSeconds(secondsVal);

							if(secondsNew < 60 && minutesNew < 60)
								comradesMain.getCurrentGame().setTimerDuration(totalValue);
							else
								timerDurationTextField.setText(totalValue + "");
							useOld = false;
						}
					}
				}
			}

			if(useOld) {
				if(oldValue != null)
					timerDurationTextField.setText(oldValue);
			}

			updateButtons();
		}));

		timerDelayTextField.textProperty().addListener(((observable, oldValue, newValue) -> {

			boolean useOld = true;

			if(newValue != null) {
				if(!newValue.isEmpty()) {

					String[] split = newValue.split(":");

					if(newValue.matches("\\d*")) {
						long lVal = Long.valueOf(newValue);

						long hours = TimeUnit.MILLISECONDS.toHours(lVal);
						long minutes = TimeUnit.MILLISECONDS.toMinutes(lVal) % 60;
						long seconds = TimeUnit.MILLISECONDS.toSeconds(lVal) % 60;

						timerDelayTextField.setText(hours + ":" + minutes + ":" + seconds);
						useOld = false;
					}
					else if(split.length == 3) {

						boolean goodFormat = true;

						long hoursVal = -1;
						long minutesVal = -1;
						long secondsVal = -1;

						if(!split[0].matches("\\d*"))
							goodFormat = false;
						else
							hoursVal = TimeUnit.HOURS.toMillis(Long.parseLong(split[0]));

						if(!split[1].matches("\\d*"))
							goodFormat = false;
						else
							minutesVal = TimeUnit.MINUTES.toMillis(Long.parseLong(split[1]));

						if(!split[2].matches("\\d*"))
							goodFormat = false;
						else
							secondsVal = TimeUnit.SECONDS.toMillis(Long.parseLong(split[2]));

						if(goodFormat) {
							long totalValue = hoursVal + minutesVal + secondsVal;

							long minutesNew = TimeUnit.MILLISECONDS.toMinutes(minutesVal);
							long secondsNew = TimeUnit.MILLISECONDS.toSeconds(secondsVal);

							if(secondsNew < 60 && minutesNew < 60)
								comradesMain.getCurrentGame().setTimerDelay(totalValue);
							else
								timerDelayTextField.setText(totalValue + "");
							useOld = false;
						}
					}
				}
			}

			if(useOld) {
				if (oldValue != null)
					timerDelayTextField.setText(oldValue);
			}

			updateButtons();
		}));

		startGameButton.setOnAction((actionEvent) -> {

			ChessGame chessGame = comradesMain.getCurrentGame();

			if(!chessGame.isGameStarted()) {

				chessGame.startGame();

				startGameButton.setText("Started");
				startGameButton.setDisable(true);
			}
			updateButtons();
		});

		stopTurnButton.setOnAction((actionEvent) -> {

			ChessPlayer ply = comradesMain.getCurrentGame().getCurrentTurnsPlayer();
			if(ply instanceof ChessEngine) {
				((ChessEngine) ply).requestCommand("stop", true);
			}
		});

		playerSetupGrid.add(blackComboLabel, 0, 0);
		playerSetupGrid.add(versusText, 0, 1);
		playerSetupGrid.add(whiteComboLabel, 0, 2);
		playerSetupGrid.add(useTimerCheckBox, 0, 3);
		playerSetupGrid.add(timerDurationLabel, 0, 4);
		playerSetupGrid.add(useTimerDelay, 0, 5);
		playerSetupGrid.add(useDelayAsBuffer, 0, 6);
		playerSetupGrid.add(timerIncrementLabel, 0, 7);
		playerSetupGrid.add(startGameButton, 0, 9);
		playerSetupGrid.add(useInfiniteTimer, 0, 8);

		gameInfoGrid.add(currentTurnLabel, 0, 0);
		gameInfoGrid.add(currentPlayerLabel, 0, 1);
		gameInfoGrid.add(stopTurnButton, 0, 2);
		gameInfoGrid.add(deadPiecesBlackLabel, 0, 3);
		gameInfoGrid.add(deadPiecesWhiteLabel, 0, 4);

		verticalBox.getChildren().addAll(playerSetupPane, gameInfoPane);
		horizontalBox.getChildren().addAll(boardPane, verticalBox);

		mainDisplayGrid.add(horizontalBox,0,0);

		menuBarVBox.getChildren().add(mainDisplayGrid);

		chessBoard.getAnimationTimer().start();
		updateButtons();

		primaryStage.setScene(scene);
		primaryStage.show();

		primaryStage.setOnCloseRequest((event) -> System.exit(1));
	}

	public ComradesMain getComradesMain() {
		return comradesMain;
	}

	public FXChessBoard getChessBoard() {
		return chessBoard;
	}
	
	public static void main(String[] args) {
			launch(args);
	}
}
