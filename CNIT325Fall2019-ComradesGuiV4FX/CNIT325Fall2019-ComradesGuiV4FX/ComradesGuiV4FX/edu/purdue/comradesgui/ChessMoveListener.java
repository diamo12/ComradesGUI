package edu.purdue.comradesgui;

import java.util.EventListener;

public interface ChessMoveListener extends EventListener {

	void moveEvent(ChessPlayer ply, ChessMove move);
}
