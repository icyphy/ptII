/* An applet that performs run-length encoding on an image and uses diva
for visualization

 Copyright (c) 1997-1999 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
*/

package ptolemy.domains.pn.demo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.lib.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// RLEncodingApplet
/**
An applet that performs run-length encoding on an image and provides
a visualization of the processes in various states. This requires a jdk1.2
copatible browser or requires a jdk1.2 plugin.
@author Mudit Goel
@version $Id$
*/

public class TTTApplet extends PNApplet {

    private TicTacToeDisplay _display;
    private Panel displayPanel;
    /** Initialize the applet.
     */
    public void init() {

        // Process the background parameter.
        super.init();
	setSize(300, 300);

	setLayout(new GridLayout(2, 1, 15, 15));
	add(_createRunControls(3));

        displayPanel = new Panel();
        displayPanel.setSize(300, 300);
	add(displayPanel);
        // Construct the Ptolemy kernel topology
        constructPtolemyModel();
	//_display.setPanel(displayPanel);
	//pack();
	validate();
	return;
    }
	
    /** Construct the Ptolemy system
     */
    public void constructPtolemyModel () {
        try {
	    _display = new TicTacToeDisplay(_toplevel, "display");
	    TTTPlayer play = new TTTPlayer(_toplevel, "player");
	    _toplevel.connect(_display.input, play.output, "QX");
	    _toplevel.connect(play.input, _display.output);
	    System.out.println("Connections made");	    
        }
        catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        return;
    }

    protected void _go() throws IllegalActionException {
	displayPanel.removeAll();
	_display.setPanel(displayPanel);
	super._go();
    }

    /** Stop the simulation of the system
     */
    public void _stop() {
        _manager.terminate();
    }
}












