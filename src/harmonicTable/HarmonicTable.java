package harmonicTable;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import rwmidi.MidiEvent;
import rwmidi.MidiInput;
import rwmidi.MidiInputDevice;
import rwmidi.MidiOutput;
import rwmidi.MidiOutputDevice;
import rwmidi.Note;
import rwmidi.RWMidi;

import com.grantmuller.midiReference.ChordReference;
import com.grantmuller.midiReference.MidiReference;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.ListBox;
import controlP5.ListBoxItem;
import controlP5.Textarea;

public class HarmonicTable extends PApplet {

	/*
	 * TODO: Make the HexButton Arraylist into a map that is accessible by a known key, 
	 * to help with performance problems. Perhaps use a combo of starting position to be the key
	 * 
	 * TODO: 
	 */
	private static final long serialVersionUID = -1512978966491270400L;

	public static void main(String[] args) {
		PApplet.main(new String[] { "harmonicTable.HarmonicTable" });
	}

	//ImageIcon titlebaricon = new ImageIcon(loadBytes("hticon.gif"));
	ControlP5 controlP5;
	Textarea aboutInfoBox;
	Textarea versionBox;
	Textarea currentChordBox;
	Textarea chordLockBox;
	static final String aboutInfoString = "HarmonicTable keyboard emulator\n For info visit grantmuller.com";
	static final String versionBoxString = "HarmonicTable 0.5";
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
	MidiInput midiInput;
	MidiReference midiReference = MidiReference.getMidiReference();
	int startingNote;
	int previousNote;
	int noteNumber;
	int currentNote;
	boolean releaseNotesToggle = false;
	boolean midiThruToggle = false;
	PFont font;
	ArrayList<HexButton> hexButtons;
	private int[] chord = ChordReference.MAJOR.getDegrees();
	private boolean chordLock = false;
	private boolean doRedraw = true;
	private Integer midiOutputChannel = 0;
	private Integer midiInputChannel = -1;

	public void setup(){
		//frame.setIconImage(titlebaricon.getImage());
		this.frameRate(120);
		size(ceil(screenWidth)+2,ceil(screenHeight)+17);
		smooth();
		textAlign(CENTER);
		controlP5 = new ControlP5(this);
		startingNote = midiReference.getNoteNumber("C2");
		
		ListBox startingNoteList = controlP5.addListBox("StartingNoteList",0,40,100,500);
		startingNoteList.setLabel("Starting Note");
		startingNoteList.setBackgroundColor(color(4,55,139));
		for (int i = 0; i < 55; i++) {
			ListBoxItem button = startingNoteList.addItem(midiReference.getNoteName(i,true), i);
			button.setId(i);
		}
		startingNoteList.setTab("setup");

		ListBox midiOutputList = controlP5.addListBox("MidiOutputList",150,40,250,100);
		midiOutputList.setLabel("Midi Output");
		MidiOutputDevice outputDevices[] = RWMidi.getOutputDevices();
		for (int i = 0; i < outputDevices.length; i++) {
			ListBoxItem button = midiOutputList.addItem(outputDevices[i].getName(),i);
			button.setId(i);
		}
		midiOutputList.setTab("setup");
		controlP5.addNumberbox("MidiOutputChannel", 0, 150, 185, 25, 15)
		.setTab("setup")
		.setMin(1)
		.setMax(16)
		.setCaptionLabel("Midi Output Channel")
		.setColorCaptionLabel(0);


		ListBox midiInputList = controlP5.addListBox("MidiInputList",450,40,250,100);
		midiInputList.setLabel("Midi Input");
		MidiInputDevice inputDevices[] = RWMidi.getInputDevices();
		for (int i = 0; i < inputDevices.length; i++) {
			ListBoxItem button = midiInputList.addItem(inputDevices[i].getName(),i);
			button.setId(i);
		}		
		midiInputList.setTab("setup");
		controlP5.addNumberbox("MidiInputChannel", 0, 450, 185, 25, 15)
		.setTab("setup")
		.setMin(1)
		.setMax(16)
		.setCaptionLabel("Midi Input Channel")
		.setColorCaptionLabel(0);

		aboutInfoBox = controlP5.addTextarea("AboutInfoBox", aboutInfoString, width-160, height-25, 200, 25);
		aboutInfoBox.setTab("setup");
		aboutInfoBox.setColorValue(0);

		versionBox = controlP5.addTextarea("VersionBox", versionBoxString, width-185, 3, 100, 10);

		currentChordBox = controlP5.addTextarea("CurrentChordText", "", width-230, 3, 100, 10);
		chordLockBox = controlP5.addTextarea("ChordLockText", "", width-310, 3, 100, 10);

		controlP5.addToggle("midiThruToggle", false)
		.setPosition(750, 40)
		.setSize(20, 20)
		.setTab("setup")
		.setColorCaptionLabel(0)
		.setCaptionLabel("Midi Thru");

		controlP5.addToggle("releaseNotesToggle", false)
		.setPosition(750, 80)
		.setSize(20, 20)
		.setTab("setup")
		.setColorCaptionLabel(0)
		.setCaptionLabel("Release Notes");

		controlP5.getTab("default").activateEvent(true).setId(MAINTAB);
		controlP5.getTab("default").setLabel("main");
		controlP5.getTab("setup").activateEvent(true).setId(SETUPTAB);

		midiOutput = RWMidi.getOutputDevices()[0].createOutput();
		midiInput = RWMidi.getInputDevices()[0].createInput(this);

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
			noteNumber = startingNote;
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
			int buttonToActivate = 0;
			switch(chord[j]){
			case 1: 
			case 2: buttonToActivate = buttonNumber + chord[j]; 
			case 3:
				if (currentRow % 2 == 0){
					buttonToActivate = buttonNumber + 11; 
				} else {
					buttonToActivate = buttonNumber + 12; 
				} break;
			case 4: 
				if (currentRow % 2 == 0){
					buttonToActivate = buttonNumber + 12; 
				} else {
					buttonToActivate = buttonNumber + 13; 
				} break;
			case 5: 
				if (currentRow % 2 == 0){
					buttonToActivate = buttonNumber + 13; 
				} else {
					buttonToActivate = buttonNumber + 14; 
				} break;
			case 6: buttonToActivate = buttonNumber + 23; break;
			case 7: buttonToActivate = buttonNumber + 24; break;
			case 8: buttonToActivate = buttonNumber + 25; break;
			case 9: buttonToActivate = buttonNumber + 26; break;
			case 10: 
				if (currentRow % 2 == 0){
					buttonToActivate = buttonNumber + 35; 
				} else {
					buttonToActivate = buttonNumber + 36; 
				} break;		
			case 11: 
				if (currentRow % 2 == 0){
					buttonToActivate = buttonNumber + 36; 
				} else {
					buttonToActivate = buttonNumber + 37; 
				} break;
			case 14: buttonToActivate = buttonNumber + 48; break;
			case 17:
				if (currentRow % 2 == 0){
					buttonToActivate = buttonNumber + 59; 
				} else {
					buttonToActivate = buttonNumber + 60; 
				} break;
			case 21: buttonToActivate = buttonNumber + 48;
			}

			if (buttonToActivate != 0
					&& buttonToActivate < hexButtons.size()){
				hexButtons.get(buttonToActivate).setActive(true);
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
					midiOutput.sendNoteOn(midiOutputChannel, currentNote, 100);
					//System.out.println(currentNote);
					//System.out.println(i);
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
						midiOutput.sendNoteOn(midiOutputChannel, currentNote, 100);
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
					midiOutput.sendNoteOff(midiOutputChannel, button.getButtonNoteNumber(), 0);					
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

	public synchronized void MidiInputChannel(int theValue){
		int oldChannel = midiInputChannel;
		midiInputChannel = theValue - 1;
		if (midiInput != null){
			midiInput.unplug(this, "processEvents", oldChannel);
			midiInput.plug(this, "processEvents", midiInputChannel);
		}
	}

	public void MidiOutputChannel(int theValue){
		midiOutputChannel = theValue - 1;	
	}

	public void controlEvent(ControlEvent theEvent) {
		if (theEvent.isGroup()){
			String parentName = theEvent.getName();
			int value = (int) theEvent.getValue();
			if (parentName.equals("StartingNoteList")){
				startingNote = value;
			}

			if (parentName.equals("MidiOutputList")){
				if (midiOutput != null){
					midiOutput.closeMidi();
				}
				midiOutput = RWMidi.getOutputDevices()[value].createOutput();
			}

			if (parentName.equals("MidiInputList")){
				if (midiInput != null){
					midiInput.closeMidi();
				}
				midiInput = RWMidi.getInputDevices()[value].createInput(this);
				midiInput.plug(this, "processEvents", -1);
			}
		} else if (theEvent.isTab()) {
			currentTab =  theEvent.getTab().getId();
			switch(currentTab){
			case MAINTAB:
				createHexButtons();
				break;
			case SETUPTAB:
			}
			return;
		}
	}

	public void processEvents(Note note)
	{
		switch(note.getCommand()){
		case MidiEvent.NOTE_ON:
			int noteNumber = note.getPitch();
			if (currentTab == MAINTAB){
				int listSize = hexButtons.size();
				for (int i = 0; i < listSize; i++){
					HexButton button = (HexButton) hexButtons.get(i);
					currentNote = button.getButtonNoteNumber();
					if (currentNote == noteNumber){
						button.setActive(true);
						doRedraw = true;
						if (midiThruToggle){
							midiOutput.sendNoteOn(note.getChannel(), noteNumber, note.getVelocity());
						}
						return;
					}
				}
			}
			break;
		case MidiEvent.NOTE_OFF:
			//TODO: May need to repair this method
			if (currentTab == MAINTAB){
				int listSize = hexButtons.size();
				for (int i = 0; i < listSize; i++){
					HexButton button = hexButtons.get(i);
					if (button.isActive()){
						if (midiThruToggle){
							midiOutput.sendNoteOff(note.getChannel(), note.getPitch(), note.getVelocity());
						}			
					}
					button.setActive(false);
				}
				doRedraw = true;
			}
			break;
		}
	}
}