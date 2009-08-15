package harmonicTable;

import java.util.ArrayList;

import midiReference.ChordReference;
import midiReference.MidiReference;
import processing.core.PApplet;
import processing.core.PFont;
import rwmidi.MidiOutput;
import rwmidi.MidiOutputDevice;
import rwmidi.RWMidi;
import wiiJava.WiiController;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.ScrollList;
import controlP5.Textarea;

public class HarmonicTable extends PApplet {

	/**
	 * 
	 */


	private static final long serialVersionUID = -1512978966491270400L;

	public static void main(String[] args) {
		//PApplet.main(new String[] { "--present", "processingMidi.ProcessingMidi" });
		PApplet.main(new String[] { "harmonicTable.HarmonicTable" });
	}

	WiiController wiiController;
	ControlP5 controlP5;
	Textarea aboutInfoBox;
	Textarea versionBox;
	Textarea currentChordBox;
	Textarea chordLockBox;
	static final String aboutInfoString = "HarmonicTable keyboard emulator\n For info visit grantmuller.com";
	static final String versionBoxString = "HarmonicTable 0.3";
	static final int 
	MAINTAB = 1000, 
	SETUPTAB = 1001;
	int currentTab = MAINTAB;

	static int length = 33; //length of one side of the hexagon
	static final int space = 0; //space between hexagons (currently not working)
	static final float a = length/2;
	static final float b = sin(radians(60))*length;  
	static final float c = length; 

	static final int xOffset = parseInt((2*c)+(2*a)); //How far left from 0 to move for each hexagon
	static final float screenWidth = 12*((2*a)+(2*c))+a+(14*space); //based on hexagon sizes
	static final float screenHeight = b*19+(8.5f*space); //based on hexagon sizes

	MidiOutput midiOutput;
	MidiReference midiReference = MidiReference.getMidiReference();
	String startingNote = "C2";
	int previousNote;
	int noteNumber;
	int currentNote;
	boolean releaseNotesToggle = false;

	PFont font;

	ArrayList<HexButton> hexButtons;
	ArrayList<Controller> controllerList;
	private int[] chord = ChordReference.MAJOR.getDegrees();
	private boolean chordLock = false;
	private boolean doRedraw = true;


	public void setup(){
		wiiController = new WiiController();
		size(ceil(screenWidth)+2,ceil(screenHeight)+17);
		smooth();
		textAlign(CENTER);
		font = loadFont("Myriad-Web-48.vlw");
		textFont(font, ceil(length * 0.66f)); //calculate font size based on hexagon size
		controlP5 = new ControlP5(this);

		ScrollList startingNoteList = controlP5.addScrollList("StartingNoteList",0,40,100,500);
		startingNoteList.setLabel("Starting Note");
		startingNoteList.setBackgroundColor(color(4,55,139));
		for (int i = 0; i < 55; i++) {
			Button button = startingNoteList.addItem(midiReference.getNoteName(i,true),9+i);
			button.setId(i);
		}
		startingNoteList.setTab("setup");

		ScrollList midiOutputList = controlP5.addScrollList("MidiOutputList",150,40,250,100);
		midiOutputList.setLabel("Midi Output");
		MidiOutputDevice devices[] = RWMidi.getOutputDevices();
		for (int i = 0; i < devices.length; i++) {
			controlP5.Button button = midiOutputList.addItem(devices[i].getName(),i);
			button.setId(i);
		}
		midiOutputList.setTab("setup");

		aboutInfoBox = controlP5.addTextarea("AboutInfoBox", aboutInfoString, width-160, height-25, 200, 25);
		aboutInfoBox.setTab("setup");
		aboutInfoBox.setColorValue(0);

		versionBox = controlP5.addTextarea("VersionBox", versionBoxString, width-185, 3, 100, 10);

		currentChordBox = controlP5.addTextarea("CurrentChordText", "", width-230, 3, 100, 10);
		chordLockBox = controlP5.addTextarea("ChordLockText", "", width-310, 3, 100, 10);

		controlP5.addToggle("releaseNotesToggle", false, 450f, 40f, 20, 20).setTab("setup");
		controlP5.controller("releaseNotesToggle").setColorLabel(0);
		controlP5.controller("releaseNotesToggle").setLabel("Release Notes");

		controlP5.tab("default").activateEvent(true).setId(MAINTAB);
		controlP5.tab("default").setLabel("main");
		controlP5.tab("setup").activateEvent(true).setId(SETUPTAB);

		midiOutput = RWMidi.getOutputDevices()[0].createOutput();

		//initialize and fill an array of hex buttons
		createHexButtons();
	}

	public void draw(){
		switch(currentTab){
		case MAINTAB: 
			if (doRedraw){
				fill(0);
				strokeWeight(0);
				stroke(color(188,228,246));
				rect(0,0,width,16);
				line(0,17, width, 17);

				strokeWeight(0);
				fill(255);
				rect(0,17,width,height-18);
				strokeWeight(2);
				stroke(148, 149, 150);
				int listSize = hexButtons.size();
				for (int i = 0; i < listSize; i++){
					hexButtons.get(i).drawHex();
				}
				doRedraw = false;
			}
			break;
		case SETUPTAB: 
			background(255); break;
		}
	}

	public void createHexButtons(){
		int rowNumber = 0;
		hexButtons = new ArrayList<HexButton>(); 
		for (int j=2; j <20; j++){
			resetNoteNumber(rowNumber);
			for (int i=0; i<12; i++){

				hexButtons.add(new HexButton(this, space+(i*xOffset), parseInt(height-(j*b)), length, 
						noteNumber, getNoteName(noteNumber)));
				noteNumber++;
			}

			j++;
			rowNumber++;
			resetNoteNumber(rowNumber);

			for (int i=0; i<12; i++){
				hexButtons.add(new HexButton(this, space+parseInt(a+c)+(i*xOffset), parseInt(height-(j*b)), length, 
						noteNumber, getNoteName(noteNumber)));
				noteNumber++;
			}

			rowNumber++;
		}
	}

	public String getNoteName(int noteNumber) {
		return midiReference.getNoteName(noteNumber, true, true);
	}

	public void resetNoteNumber(int rowNumber){
		if (rowNumber == 0){
			noteNumber = midiReference.getNoteNumber(startingNote);
			previousNote = noteNumber;
		} else if (rowNumber % 2 == 0){
			noteNumber = previousNote + 3;
			previousNote = noteNumber;
		} else {
			noteNumber = previousNote + 4;
			previousNote = noteNumber;
		}
	}

	public boolean isPointerInArea(HexButton hexButton){
		return (mouseX >= hexButton.getStartX()+a && mouseX <= hexButton.getStartX()+a+c
				&& mouseY >= hexButton.getStartY() && mouseY <= hexButton.getStartY()+(2*b));
	}

	public void playChord(int buttonNumber, int noteNumber){
		int currentRow = buttonNumber/12;
		for (int j = 1; j < chord.length; j++){
			int localNoteNumber = currentNote + chord[j];
			midiOutput.sendNoteOn(0, localNoteNumber, 100);

			switch(chord[j]){
			case 1: 
			case 2: hexButtons.get(buttonNumber + chord[j]).setActive(true);
			case 3:
				if (currentRow % 2 == 0){
					hexButtons.get(buttonNumber + 11).setActive(true); 
				} else {
					hexButtons.get(buttonNumber + 12).setActive(true); 
				} break;
			case 4: 
				if (currentRow % 2 == 0){
					hexButtons.get(buttonNumber + 12).setActive(true); 
				} else {
					hexButtons.get(buttonNumber + 13).setActive(true); 
				} break;
			case 5: 
				if (currentRow % 2 == 0){
					hexButtons.get(buttonNumber + 13).setActive(true); 
				} else {
					hexButtons.get(buttonNumber + 14).setActive(true); 
				} break;
			case 6: hexButtons.get(buttonNumber + 23).setActive(true); break;
			case 7: hexButtons.get(buttonNumber + 24).setActive(true); break;
			case 8: hexButtons.get(buttonNumber + 25).setActive(true); break;
			case 9: hexButtons.get(buttonNumber + 26).setActive(true); break;
			case 10: 
				if (currentRow % 2 == 0){
					hexButtons.get(buttonNumber + 35).setActive(true); 
				} else {
					hexButtons.get(buttonNumber + 36).setActive(true); 
				} break;		
			case 11: 
				if (currentRow % 2 == 0){
					hexButtons.get(buttonNumber + 36).setActive(true); 
				} else {
					hexButtons.get(buttonNumber + 37).setActive(true); 
				} break;
			case 14: hexButtons.get(buttonNumber + 48).setActive(true); break;
			case 17:
				if (currentRow % 2 == 0){
					hexButtons.get(buttonNumber + 59).setActive(true); 
				} else {
					hexButtons.get(buttonNumber + 60).setActive(true); 
				} break;
			case 21: hexButtons.get(buttonNumber + 48).setActive(true); break;
			}
			
		}
	}

	public void mousePressed(){
		if (currentTab == MAINTAB){
			int listSize = hexButtons.size();
			for (int i = 0; i < listSize; i++){
				HexButton button = (HexButton) hexButtons.get(i);
				if (isPointerInArea(button)){
					currentNote = button.getButtonNoteNumber();
					button.setActive(true);
					midiOutput.sendNoteOn(0, button.getButtonNoteNumber(), 100);
					System.out.println(currentNote);
					System.out.println(i);
					if (keyPressed || chordLock){
						playChord(i, currentNote);
					}
					doRedraw = true;
				}
			}
		}
	}

	public void mouseDragged(){
		if (currentTab == MAINTAB){
			int listSize = hexButtons.size();
			for (int i = 0; i < listSize; i++){
				HexButton button = (HexButton) hexButtons.get(i);
				if (isPointerInArea(button)){
					if (currentNote != button.getButtonNoteNumber()){
						currentNote = button.getButtonNoteNumber();
						button.setActive(true);
						midiOutput.sendNoteOn(0, button.getButtonNoteNumber(), 100);
						if (keyPressed || chordLock){
							playChord(i, currentNote);
						}
					}
					doRedraw = true;
				}
			}
		}
	}

	public void mouseReleased(){
		if (currentTab == MAINTAB){
			int listSize = hexButtons.size();
			for (int i = 0; i < listSize; i++){
				HexButton button = hexButtons.get(i);
				if (button.isActive() && releaseNotesToggle){
					midiOutput.sendNoteOff(0, button.getButtonNoteNumber(), 0);					
				}
				button.setActive(false);
			}
			doRedraw = true;
		}
	}

	public void keyPressed(){
		switch (key){
		//row 1
		case '1': setChord(ChordReference.MAJOR); break;
		case '2': setChord(ChordReference.MINOR); break;
		case '3': setChord(ChordReference.AUGMENTED); break;
		case '4': setChord(ChordReference.DIMINISHED); break;
		//row2
		case 'q': setChord(ChordReference.SEVENTH); break;
		case 'w': setChord(ChordReference.MAJOR_SEVENTH); break;
		case 'e': setChord(ChordReference.MINOR_SEVENTH); break;
		case 'r': setChord(ChordReference.MINOR_MAJOR_SEVENTH); break;
		//row3
		case 'a': setChord(ChordReference.ADD_NINE); break;
		case 's': setChord(ChordReference.ADD_FOURTH); break;
		case 'd': setChord(ChordReference.ELEVENTH); break;
		case 'f': setChord(ChordReference.THIRTEENTH); break;
		//row 4
		case 'z': setChord(ChordReference.MINOR_NINTH); break;
		case 'x': setChord(ChordReference.MINOR_ELEVENTH); break;
		case 'c': setChord(ChordReference.MINOR_THIRTEENTH); break;
		case 'v': setChord(ChordReference.SIXTH); break;
		//chord lock
		case 'b': 
			chordLock = !chordLock; 
			if (chordLock) chordLockBox.setText("CHORD LOCK"); 
			break;
		}
		doRedraw = true;
	}

	public void keyReleased(){
		if (!chordLock){
			currentChordBox.setText("");
			chordLockBox.setText("");
			chord = new int[1];
			chord[0] = 0;
		}
		doRedraw = true;
	}

	public void setChord(ChordReference chordRef){
		chord = chordRef.getDegrees();
		currentChordBox.setText(chordRef.getCommonName());
	}

	void controlEvent(ControlEvent theEvent) {
		if (theEvent.isTab()) {
			//println("tab : "+theEvent.tab().id()+" / "+theEvent.tab().name());
			currentTab =  theEvent.tab().id();
			switch(currentTab){
			case MAINTAB:
				createHexButtons();
				break;
			case SETUPTAB:
			}
			return;
		}

		if (theEvent.controller().parent().name() == "StartingNoteList"){
			startingNote = theEvent.name();
		}

		if (theEvent.controller().parent().name() == "MidiOutputList"){
			midiOutput.closeMidi();
			midiOutput = RWMidi.getOutputDevices()[theEvent.controller().id()].createOutput();
		}
	}
}