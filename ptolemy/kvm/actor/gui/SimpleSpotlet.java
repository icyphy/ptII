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

package ptolemy.kvm.actor.gui;

import com.sun.kjava.*;


//////////////////////////////////////////////////////////////////////////
//// SimpleSpotlet
/**
Ptolemy II Spotlet Baseclass used by simple Ptolemy II applications
for things like testing and such.

Under the KVM on the Palm, applications extend Spotlet and then
create things like TextBoxes for output.

@author Christopher Hylands
@version $Id$
*/
public class SimpleSpotlet extends Spotlet{
    public SimpleSpotlet() {
        _graphics = Graphics.getGraphics();
        textBox = new ScrollTextBox("SimpleSpotlet",5,5,150,130);
        paint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public static void main(String args[]) {
        SimpleSpotlet = new SimpleSpotlet();
	Graphics.getGraphics().clearScreen();
	textBox.paint();
        SimpleSpotlet.register(NO_EVENT_OPTIONS);
        textBox.setText("SimpleSpotlet, SimpleSpotlet, PalmOS");
	SimpleSpotlet.paint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void paint() {
        textBox.paint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // FIXME: these should likely be protected
    public static SimpleSpotlet SimpleSpotlet;
    public static ScrollTextBox textBox;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected Graphics _graphics;
}
