/* An actor that displays input data in a text area on the screen.

@Copyright (c) 1998-1999 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.util.*;
import ptolemy.data.StringToken;
import ptolemy.actor.*;
import java.awt.*;

/** Display the values of the tokens arriving on the input channels
 *  in a text area on the screen.
 *  <p>
 *  The input type is StringToken.  Since any other type of token
 *  can be converted to a StringToken, this imposes no constraints
 *  on the types of the upstream actors.
 *
 *  @author  Yuhong Xiong, Edward A. Lee
 *  @version $Id$
 */
public class Print extends TypedAtomicActor implements Placeable {

    public Print(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(StringToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Input port, which has type StringToken. */
    public TypedIOPort input;

    /** The text area in which the data will be displayed. */
    public TextArea textArea;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Print newobj = (Print)super.clone(ws);
            newobj.input = (TypedIOPort)newobj.getPort("input");
            textArea = null;
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read at most one token from each input channel and display its
     *  string value on the screen.  Each value is terminated
     *  with a newline character.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                StringToken token = (StringToken)input.get(i);
                String value = token.stringValue();
                textArea.append(value + "\n");
            }
        }
    }

    /** Create a text area on the screen, if necessary, or clear the
     *  previously existing text area.
     *  If a panel has not been specified, place the text area into
     *  its own frame.  Otherwise, place it in the specified panel.
     */
    public void initialize() {
        if (textArea == null) {
            setPanel(_panel);
        } else {
            // FIXME: Incredibly, TextArea has no clear method!
            // textArea.clear();
        }
    }

    /** Specify the panel in which the data should be displayed.
     *  An instance of TextArea will be added to that panel.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, an instance of TextArea will be placed in its own frame.
     *  The text area is also placed in its own frame if this method
     *  is called with a null argument.
     *
     *  @param panel The panel into which to place the text area.
     */
    public void setPanel(Panel panel) {
        _panel = panel;
        if (_panel == null) {
            // place the text area in its own frame.
            // FIXME: This probably needs to be a PtolemyFrame, when one
            // exists, so that the close button is dealt with, etc.
            Frame frame = new Frame(getFullName());
            frame.add(textArea);
        } else {
            textArea = new TextArea();
            _panel.add(textArea);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Panel _panel;
}

