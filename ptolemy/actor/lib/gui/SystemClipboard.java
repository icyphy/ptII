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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.IOException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Source;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SystemClipboard
/**
When this actor fires, it copies to the system clipboard the contents
of any token received at its <i>input</i> port, and if there is a token
on the <i>trigger</i> input, it also pastes the contents of the system
clipboard to the <i>output</i> port. The paste is done before the copy.

@author Winthrop Williams
@version $Id$
@since Ptolemy II 2.0
*/

public class SystemClipboard extends Source implements ClipboardOwner {

    // NOTE: This actor has been tested only with an 8-bit character
    // set as the Java default character set. Results are not known for
    // systems configured for 16-bit Unicode characters.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SystemClipboard(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Input port.
        input = new TypedIOPort(this, "input");
        input.setTypeEquals(BaseType.STRING);
        input.setInput(true);

        // Output
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type string. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Copy any <i>input</i> token to the clipboard and, if
     *  <i>trigger</i>-ed, paste the clipboard to the
     *  <i>output</i>.  Paste is done before copy when both
     *  inputs are present.
     */
    public void fire() throws IllegalActionException {
        // Do not call super.fire(), because we need to know whether
        // inputs are present.
        // Paste first.
        boolean triggerPresent = false;
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                triggerPresent = true;
                trigger.get(i);
            }
        }
        if (triggerPresent) {
            Clipboard clipboard
                    = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(this);
            try{
                output.broadcast(new StringToken( (String)transferable
                        .getTransferData(DataFlavor.stringFlavor) ));
                // FIXME: NullPointerException also possible
                // Ignore this for now, allowing exception to go uncaught.
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to paste.");
            } catch (UnsupportedFlavorException ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to paste.");
            }
        }

        // Copy next.
        if (input.getWidth() > 0 && input.hasToken(0)) {
            Clipboard clipboard
                    = Toolkit.getDefaultToolkit().getSystemClipboard();
            String myString = ((StringToken)(input.get(0))).stringValue();
            clipboard.setContents(new StringSelection(myString), this);
        }
    }

    /** Comply with the ClipboardOwner interface.  It requires a
     *  method exist named <i>lostOwnership</i>.  Specifically,
     *  when setContents() or getContents() is called, the last
     *  argument, known as the "requestor", must be an object which
     *  offers this lostOwnership() method.
     */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // In case of lost ownership, do nothing.
    }
}
