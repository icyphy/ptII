/* An actor that prints out the input to a text area.

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

package ptolemy.actor.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.StringToken;
import ptolemy.actor.*;
import java.awt.*;

/** A printer.
 *
 *  @author  Yuhong Xiong
 *  @version $Id$
 */
public class Print extends TypedAtomicActor implements Placeable {

    public Print(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setDeclaredType(StringToken.class);

	textArea = new TextArea();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Input port. */
    public TypedIOPort input;

    /** The plot object. */
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
            newobj.input.setMultiport(true);
            // newobj.input.setDeclaredType(StringToken.class);
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read all available inputs and print them.
     *  The content of each token is printed on a new line.
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

    /** If a panel has not been specified, place the text area into
     *  its won frame.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void initialize() throws IllegalActionException {
        // FIXME: remove "throws" clause.
        if (_panel == null) {
            // place the text area in its own frame.
            Frame frame = new Frame(getFullName());
            frame.add(textArea);
        }
    }

    /** Specify the panel into which the text area should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the text area will be placed in its own frame.
     *
     *  @param panel The panel into which to place the text area.
     */
    public void setPanel(Panel panel) {
        _panel = panel;
	_panel.add(textArea);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Panel _panel;
}

