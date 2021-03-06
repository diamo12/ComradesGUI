
package edu.purdue.comradesgui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ComradesMain {

	private ChessGame currentGame;
	private ObservableList<ChessPlayer> playerList;

	public ComradesMain() {

		playerList = FXCollections.observableArrayList();
		currentGame = new ChessGame();
		
		ChessHumanPlayer theHuman = new ChessHumanPlayer();
		theHuman.setPlayerName("Human");
		
		playerList.add(theHuman);
	}

	public ObservableList<ChessPlayer> getPlayerList() {
		return playerList;
	}

	public void addPlayer(ChessPlayer ply) {
		playerList.add(ply);
	}

	public ChessGame getCurrentGame() {
		return currentGame;
	}
	
	
}
