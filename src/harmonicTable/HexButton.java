package harmonicTable;

import shapes.Hexagon;

public class HexButton extends Hexagon {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8460198731304739999L;

	String note;
	private int buttonNoteNumber;
	private int buttonColor;
	private int noteColor;
	private boolean isActive;

	static final int whiteKey = 255; //color of all white keys
	static final int blackKey = 0; //color of all black keys
	int activeKey = 55;
	int activeNote = 0;
	int Gsharp;//color for G#
	int D; //color for D

	public HexButton(Object p, int startX, int startY, int sideLength, int noteNumber, 
			String note){

		super(p, startX, startY, sideLength);

		if (parent != null){
			Gsharp = parent.color(4,55,139);
			D = parent.color(188,228,246);
			activeKey = parent.color(255, 255,0);
		}

		if (buffer != null){
			Gsharp = buffer.color(4,55,139);
			D = buffer.color(188,228,246);
		}

		this.note = note;
		this.buttonNoteNumber = noteNumber;
		findButtonColor();
		findNoteColor();
		isActive = false;
	}

	public void drawHex(){
		parent.fill(buttonColor);
		parent.pushMatrix();
		parent.translate(getStartX(), getStartY());
		super.drawHex();
		parent.fill(noteColor);
		parent.text(note, c, c);
		parent.popMatrix();
	}

	public void findButtonColor() {
		int color = 255;
		if (note.contains("G#")) {
			color = Gsharp;
		} else if (note.contains("D") && !note.contains("#")) {
			color = D;
		} else if (note.contains("#") || note.contains("b")) {
			color = blackKey;
		}
		buttonColor = color;
	}

	public void findNoteColor(){
		int color = 0;	
		if (note.contains("G#")) {
			color = 255;
		} else if (note.contains("D") && !note.contains("#")) {
			color = 0;
		} else if (note.contains("#") || note.contains("b")) {
			color = 255;
		}
		noteColor = color;
	}

	public void setButtonNoteNumber(int buttonNoteNumber) {
		this.buttonNoteNumber = buttonNoteNumber;
	}

	public int getButtonNoteNumber() {
		return buttonNoteNumber;
	}

	public int getButtonColor() {
		return buttonColor;
	}

	public void setButtonColor(int buttonColor) {
		this.buttonColor = buttonColor;
	}

	public int getNoteColor() {
		return noteColor;
	}

	public void setNoteColor(int noteColor) {
		this.noteColor = noteColor;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
		if (isActive){
			buttonColor = activeKey;
			noteColor = activeNote;
		} else {
			findButtonColor();
			findNoteColor();
		}
	}
}
