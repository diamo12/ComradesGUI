package edu.purdue.comradesgui;

public class ChessEngineOptionString extends ChessEngineOption {

	private String strValue;

	public ChessEngineOptionString(String toParse, ChessEngine chessEngine) {
		super(toParse, chessEngine);
	}

	@Override
	protected void parseInputString(String name, String value) { }

	public void setStringValue(String str) {
		this.strValue = str;
		chessEngine.requestCommand("setoption name " + name + " value " + strValue, true);
	}

	public String getStringValue() {
		return strValue;
	}

	public String toString() {

		String out = super.toString();

		out += " | Option String Value: " + strValue;

		return out;
	}
}
