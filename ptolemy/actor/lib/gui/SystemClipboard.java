/* An actor which copies to, and pastes from, the system clipboard.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (winthrop@robotics.eecs.berkeley.edu)
@AcceptedRating Red (winthrop@robotics.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.gui;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.awt.event.KeyEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;

//////////////////////////////////////////////////////////////////////////
//// SystemClipboard

/**
This actor copies, to the system clipboard, the contents of any token received
at its <i>input</i> port.  It pastes, from the system clipboard, to the
<i>output</i> port, whenever it receives a token at the <i>trigger</i>.
If both inputs receive tokens during the same firing, the paste is done
before the copy.  This ordering insures that the contents of the clipboard
are not lost in the event of a simultaneous copy-paste operation.  This
actor is designed to work with KeystrokeSensor.java.  <p>

@author Winthrop Williams
@version $Id$
*/
public class SystemClipboard extends TypedAtomicActor implements ClipboardOwner {

    public SystemClipboard(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

	// Inputs
        input = new TypedIOPort(this, "input");
        input.setTypeEquals(BaseType.STRING);
        input.setInput(true);

        trigger = new TypedIOPort(this, "trigger");
        trigger.setTypeEquals(BaseType.GENERAL);
        trigger.setInput(true);

        // Output
        output = new TypedIOPort(this, "output");
        output.setTypeEquals(BaseType.STRING);
        output.setOutput(true);
    }

    public TypedIOPort input;

    public TypedIOPort trigger;

    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.
     *  Blah, blah, blah.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) _debug("fire has been called");

	// Paste
	if (trigger.getWidth() > 0 && trigger.hasToken(0)) {
	    trigger.get(0);
	    Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
	    Transferable transferable = clipboard.getContents(this);
	    try{
		output.broadcast(new StringToken( (String)transferable
                        .getTransferData(DataFlavor.stringFlavor) ));
	    // NullPointerException also possible //
		// Ignore this for now, allowing exception to go uncaught.
	    } catch (java.io.IOException ex) {
		throw new IllegalActionException(this,
                        " Failed to paste (IO Exception): " + ex);
	    } catch (java.awt.datatransfer.UnsupportedFlavorException ex) {
		throw new IllegalActionException(this,
                        " Failed to paste: (Flavor Exception)" + ex);
	    }
	}

	// Copy
	if (input.getWidth()>0 && input.hasToken(0)) {
	    Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
	    String myString = ((StringToken)(input.get(0))).stringValue();
	    clipboard.setContents(new StringSelection(myString), this);
	}

	if (_debugging) _debug("fire has completed");
    }

    /** Comply with the ClipboardOwner interface.  It requires a
     *  method exist named <i>lostOwnership</i>.
     *
     *  Without this (and having the actor, or something,
     *  implement ClipboardOwner, I get the following error:
     *
     *  setContents(java.awt.datatransfer.Transferable,
     *              java.awt.datatransfer.ClipboardOwner)
     *           in java.awt.datatransfer.Clipboard
     *           cannot be applied to
     *             (java.awt.datatransfer.StringSelection,
     *              ptolemy.actor.lib.net.Wormhole)
     */
    /*
CLASSPATH="../../../..;c:\cygwin\home\winthrop\8Feb\ptII/lib/diva.jar" "/cygdriv
e/c/jdk1.4/bin/javac" -g -O Wormhole.java
Wormhole.java:137: cannot resolve symbol
symbol  : variable _myFrame
location: class ptolemy.actor.lib.net.Wormhole
            Transferable transferable = clipboard.getContents(_myFrame);
                                                              ^
1 error
make: *** [Wormhole.class] Error 1
bash-2.04$ emacs Wormhole.java

When I have '_myframe' in place of 'this' in getContents( ) call.
Seems "requestor" who calls this must implement the ClipboardOwner interface.
    */

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables

}






