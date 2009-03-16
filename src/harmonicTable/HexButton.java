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
	
	public HexButton(Object p, int startX, int startY, int sideLength, int noteNumber, 
			String note, int buttonColor, int noteColor){
		
		super(p, startX, startY, sideLength);
		this.note = note;
		this.buttonNoteNumber = noteNumber;
		this.buttonColor = buttonColor;
		this.noteColor = noteColor;
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
	

	public void setButtonNoteNumber(int buttonNoteNumber) {
		this.buttonNoteNumber = buttonNoteNumber;
	}

	public int getButtonNoteNumber() {
		return buttonNoteNumber;
	}
}
