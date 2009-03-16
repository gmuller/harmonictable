package harmonicTable;

import java.util.ArrayList;

import midiReference.ChordReference;
import midiReference.MidiReference;
import processing.core.PApplet;
import processing.core.PFont;
import rwmidi.MidiOutput;
import rwmidi.MidiOutputDevice;
import rwmidi.RWMidi;
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

	static final int whiteKey = 255; //color of all white keys
	static final int blackKey = 0; //color of all black keys
	final int Gsharp = color(4,55,139); //color for G#
	final int D = color(188,228,246); //color for D

	MidiOutput midiOutput;
	MidiReference midiReference = MidiReference.getMidiReference();
	String startingNote = "C2";
	int previousNote;
	int noteNumber;
	int currentNote;
	boolean releaseNotesToggle = false;
	ArrayList<Integer> activeNotes = new ArrayList<Integer>();

	PFont font;

	ArrayList<HexButton> hexButtons;
	ArrayList<Controller> controllerList;
	boolean doRedraw = true;
	private int[] chord = ChordReference.MAJOR.getDegrees();
	private boolean chordLock = false;


	public void setup(){
		size(ceil(screenWidth)+2,ceil(screenHeight)+17);
		smooth();
		textAlign(CENTER);
		font = loadFont("Myriad-Web-48.vlw");
		textFont(font, ceil(length * 0.66f)); //calculate font size based on hexagon size
		controlP5 = new ControlP5(this);

		ScrollList startingNoteList = controlP5.addScrollList("StartingNoteList",0,40,100,500);
		startingNoteList.setLabel("Starting Note");
		startingNoteList.setBackgroundColor(Gsharp);
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

			fill(0);
			strokeWeight(0);
			stroke(D);
			rect(0,0,width,16);
			line(0,17, width, 17);

			if(doRedraw){
				strokeWeight(0);
				fill(255);
				rect(0,17,width,height-18);
				strokeWeight(2);
				stroke(148, 149, 150);
				int listSize = hexButtons.size();
				for (int i = 0; i < listSize; i++){
					hexButtons.get(i).drawHex();
				}
				doRedraw = !doRedraw;
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
						noteNumber, getNoteName(noteNumber), getButtonColor(getNoteName(noteNumber)),
						getNoteColor(getNoteName(noteNumber))));
				noteNumber++;
			}

			j++;
			rowNumber++;
			resetNoteNumber(rowNumber);

			for (int i=0; i<12; i++){
				hexButtons.add(new HexButton(this, space+parseInt(a+c)+(i*xOffset), parseInt(height-(j*b)), length, 
						noteNumber, getNoteName(noteNumber), getButtonColor(getNoteName(noteNumber)),
						getNoteColor(getNoteName(noteNumber))));
				noteNumber++;
			}

			rowNumber++;
		}
	}

	public String getNoteName(int noteNumber) {
		return midiReference.getNoteName(noteNumber, true, true);
	}

	public int getButtonColor(String noteName) {
		int color = 255;
		if (noteName.contains("G#")) {
			color = Gsharp;
		} else if (noteName.contains("D") && !noteName.contains("#")) {
			color = D;
		} else if (noteName.contains("#") || noteName.contains("b")) {
			color = blackKey;
		}
		return color;
	}

	public int getNoteColor(String noteName){
		int color = 0;	
		if (noteName.contains("G#")) {
			color = 255;
		} else if (noteName.contains("D") && !noteName.contains("#")) {
			color = 0;
		} else if (noteName.contains("#") || noteName.contains("b")) {
			color = 255;
		}
		return color;
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

	public void mousePressed(){
		if (currentTab == MAINTAB){
			int listSize = hexButtons.size();
			for (int i = 0; i < listSize; i++){
				HexButton button = (HexButton) hexButtons.get(i);
				if (mouseX >= button.getStartX()+a && mouseX <= button.getStartX()+a+c
						&& mouseY >= button.getStartY() && mouseY <= button.getStartY()+(2*b)){
					currentNote = button.getButtonNoteNumber();
					activeNotes.add(currentNote);
					if (keyPressed || chordLock){
						int[] thisChord = chord;
						for (int j = 0; j < thisChord.length; j++){
							midiOutput.sendNoteOn(0, button.getButtonNoteNumber() + thisChord[j], 100);
							activeNotes.add(currentNote + thisChord[j]);
						}
						return;
					}
					midiOutput.sendNoteOn(0, button.getButtonNoteNumber(), 100);
					activeNotes.add(currentNote);
				}
			}
		}
	}

	public void mouseDragged(){
		if (currentTab == MAINTAB){
			int listSize = hexButtons.size();
			for (int i = 0; i < listSize; i++){
				HexButton button = (HexButton) hexButtons.get(i);
				if (mouseX >= button.getStartX()+a && mouseX <= button.getStartX()+a+c
						&& mouseY >= button.getStartY() && mouseY <= button.getStartY()+(2*b)){
					if (currentNote != button.getButtonNoteNumber()){
						currentNote = button.getButtonNoteNumber();
						if (keyPressed || chordLock){
							int[] thisChord = chord;
							for (int j = 0; j < thisChord.length; j++){
								midiOutput.sendNoteOn(0, currentNote + thisChord[j], 100);
								activeNotes.add(currentNote + thisChord[j]);
							}
							return;
						}
						midiOutput.sendNoteOn(0, button.getButtonNoteNumber(), 100);
						activeNotes.add(currentNote);
					}
				}
			}
		}
	}

	public void mouseReleased(){
		if (currentTab == MAINTAB){
			if (releaseNotesToggle){
				for (int i = activeNotes.size()-1; i >= 0; i--){
					midiOutput.sendNoteOff(0, activeNotes.get(i), 0);
					activeNotes.remove(i);
				}
			}
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
	}

	public void keyReleased(){
		if (!chordLock){
			currentChordBox.setText("");
			chordLockBox.setText("");
			chord = new int[1];
			chord[0] = 0;
		}
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
				doRedraw = true;
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