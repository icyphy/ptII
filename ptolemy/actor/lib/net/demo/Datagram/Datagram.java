/* An app actor for demonstrating the Datagram actors.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.net.demo.Datagram;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Datagram

/**
 This actor copies, to the system clipboard, the contents of any token
 received at its <i>input</i> port.  It pastes, from the system
 clipboard to the <i>output</i> port, whenever it receives a token at
 the <i>trigger</i>.  If both inputs receive tokens during the same
 firing, the paste is done before the copy.  This ordering insures that
 the contents of the clipboard are not lost in the event of a
 simultaneous copy-paste operation.

 <p> NOTE: This actor has been tested only with an 8-bit character set
 as the Java default character set.  Results are not known for systems
 configured for 16-bit Unicode characters.

 @author Winthrop Williams
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (winthrop)
 @Pt.AcceptedRating Red (winthrop)
 */
public class Datagram extends TypedAtomicActor implements ClipboardOwner {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Datagram(CompositeEntity container, String name)
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

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type StringToken. */
    public TypedIOPort input;

    /** Input port, which has type Token. */
    public TypedIOPort trigger;

    /** Output port, which has type StringToken. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Copy any <i>input</i> token to the clipboard and, if
     *  <i>trigger</i>-ed, paste the clipboard to the
     *  <i>output</i>.  Paste is done before copy when both
     *  inputs are present.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("fire has been called");
        }

        // Paste
        if (trigger.isOutsideConnected() && trigger.hasToken(0)) {
            trigger.get(0);

            Clipboard clipboard = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            Transferable transferable = clipboard.getContents(this);

            try {
                output.broadcast(new StringToken((String) transferable
                        .getTransferData(DataFlavor.stringFlavor)));

                // NullPointerException also possible //
                // Ignore this for now, allowing exception to go uncaught.
            } catch (IOException ex) {
                throw new IllegalActionException(this,
                        " Failed to paste (IO Exception): " + ex);
            } catch (UnsupportedFlavorException ex) {
                throw new IllegalActionException(this,
                        " Failed to paste: (Flavor Exception)" + ex);
            }
        }

        // Copy
        if (input.isOutsideConnected() && input.hasToken(0)) {
            Clipboard clipboard = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            String myString = ((StringToken) input.get(0)).stringValue();
            clipboard.setContents(new StringSelection(myString), this);
        }

        if (_debugging) {
            _debug("fire has completed");
        }
    }

    /** Comply with the ClipboardOwner interface.  It requires a
     *  method exist named <i>lostOwnership</i>.  Specifically,
     *  when setContents() or getContents() is called, the last
     *  argument, known as the "requestor", must be an object which
     *  offers this lostOwnership() method.
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // In case of lost ownership, do nothing.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables
    // No private variables.
}
