/* Base class Simple Ptolemy II Spotlets on the Palm using KVM

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kvm.kernel.util.test;

import ptolemy.kvm.kernel.util.*;
import ptolemy.kvm.actor.gui.SimpleSpotlet;

import com.sun.kjava.*;


//////////////////////////////////////////////////////////////////////////
//// TestNamedObjSpotlet
/**
@author Christopher Hylands
@version $Id$
*/
public class TestNamedObjSpotlet extends Spotlet {
    public TestNamedObjSpotlet() {
        _graphics = Graphics.getGraphics();
        textBox = new ScrollTextBox("SimpleSpotlet",5,5,150,130);
	try {
	    TestNamedObj testNamedObj = new TestNamedObj();
	    textBox.setText(testNamedObj.getString());
	} catch (RuntimeException e) {
	    textBox.setText("RuntimeException: " + e);
	} catch (IllegalActionException e2) {
	    textBox.setText("IllegalActionException: " + e2);
	}
        paint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public static void main(String args[]) {
        TestNamedObjSpotlet = new TestNamedObjSpotlet();
	Graphics.getGraphics().clearScreen();
	textBox.paint();
        TestNamedObjSpotlet.register(NO_EVENT_OPTIONS);
        //textBox.setText("TestNamedObjSpotlet, TestNamedObjSpotlet, PalmOS");
	TestNamedObjSpotlet.paint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void paint() {
        textBox.paint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // FIXME: these should likely be protected
    public static TestNamedObjSpotlet TestNamedObjSpotlet;
    public static ScrollTextBox textBox;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected Graphics _graphics;
}



