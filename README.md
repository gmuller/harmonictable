HarmonicTable is a replica of the C-thru aXis built with Java and Processing that is playable with a mouse and keyboard. The end goal of this project is to port the application to a touchscreen.

<a href="http://www.grantmuller.com/wp-content/uploads/harmonictable011.gif"><img class="aligncenter size-full wp-image-368" title="harmonictable011" src="http://www.grantmuller.com/wp-content/uploads/harmonictable011.gif" alt="harmonictable011" width="520" height="565" /></a>

## Documentation

On the main screen any of the keys can be played by mouse
There are two tabs at the top, one for the main table screen, and one for setup. On the setup screen you will find options for the Starting Note, Midi Input and Output, as well as toggles for note release and Midi Thru.

<dl>
	<dt>Starting Note</dt>
	<dd>The note you select in this scroll list will be the note that the matrix starts with. You can think of this like selecting the octave on a small Midi controller, only this allows you to set any note as the lowest note.<dd> 

	<dt>Midi Input and Output</dt>
	<dd>These are the midi input and output selections. By default, Harmonic Table will select the first input and output available on your system. MidiOutput is on channel 0 by default, input will accept any channel unless one is selected using the number box.</dd>

	<dt>Midi Thru</dt>
	<dd>When this option is turned on, any notes arriving at the input port will be mirrored to the output port.</dd>

	<dt>Release Notes</dt>
	<dd>When this option is turned on, note off messages will be send immediately when the mouse or key is released.</dd>

	<dt>Chord Mode</dt>
	<dd>Chord mode allows the user to play chord when clicking or dragging notes around on the screen. To play a chord, hold down a key from the chart below while clicking a note to play the associated chord:</dd>

<table width="30%">
    <th colspan="2" width="100%">Row 1</th>
        <tr width="50%">
            <td width="50%">1</td>
            <td width="50%">Major</td>
        </tr>
        <tr>
            <td>2</td>
            <td>Minor</td>
        </tr>
        <tr>
            <td>3</td>
            <td>Augmented</td>
        </tr>
        <tr>
            <td>4</td>
            <td>Diminished</td>
        </tr>
    <th colspan="2">Row 2</th>
        <tr>
            <td>q</td>
            <td>7th</td>
        </tr>
        <tr>
            <td>w</td>
            <td>Major 7th</td>
        </tr>
        <tr>
            <td>e</td>
            <td>Minor 7th</td>
        </tr>
        <tr>
            <td>r</td>
            <td>Minor Major 7th</td>
        </tr>
    <th colspan="2">Row 3</th>
        <tr>
            <td>a</td>
            <td>Add 9</td>
        </tr>
        <tr>
            <td>s</td>
            <td>Add 4th</td>
        </tr>
        <tr>
            <td>d</td>
            <td>11th</td>
        </tr>
        <tr>
            <td>f</td>
            <td>13th</td>
        </tr>
    <th colspan="2">Row 4</th>
        <tr>
            <td>z</td>
            <td>Minor 9th</td>
        </tr>
        <tr>
            <td>x</td>
            <td>Minor 11th</td>
        </tr>
        <tr>
            <td>c</td>
            <td>Minor 16th</td>
        </tr>
        <tr>
            <td>v</td>
            <td>6th</td>
        </tr>
</table>
<dl>

In addition you can turn on <strong>chord lock</strong> by pressing the 'b' key. When enabled the user need not hold the key of the chord being played down, just press the key once and any note click will play the chord that is locked. At the top right of the menu bar a message displaying both the chord being played and the chord lock status are displayed.

This was built using the <a href="http://www.processing.org">Processing</a> libraries, along with the <a href="http://www.sojamo.de/libraries/controlP5/">ControlP5</a> library and <a href="http://ruinwesen.com/support-files/rwmidi/documentation/RWMidi.html">Ruin & Wesen's rwmidi</a> library for midi functionality.

<a href="http://blog.makezine.com/archive/2009/01/harmonic_table_input_software.html"><img src="http://makezine.com/images/ads/makers/See_Me_on_MAKE.gif" alt="See me on Make!" width="100" height="99" border="0" /></a>

## Download

This is an executable jar, meaning if you have java installed on your machine (version 5 or above), you should be able to double-click it and you&#8217;re off and running.

<a href="http://www.grantmuller.com/HarmonicTable/HarmonicTable-05.jar">Harmonic Table 05</a>

You can check out the source or report an issue <a href="https://github.com/gmuller/harmonictable/issues">here</a>.