/* An actor that displays the inputs on the screen.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.util.*;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;
import ptolemy.actor.util.Table;
import java.awt.*;

/** Show the inputs on the screen.  This actor reads tokens from any number
 *  of input channels and displays their string values in a tabular
 *  format with labels specified by a parameter.  The input type
 *  is StringToken.  Since any other type of token can be converted
 *  to a StringToken, this imposes no constraints on the types of the
 *  upstream actors.
 *  <p>
 *  The "labels" parameter gives a comma-separated list of labels
 *  to display next to the input values.  Each label, of course, cannot
 *  contain a comma.  If there are more labels than input channels,
 *  then the last few labels will be ignored.  If there are fewer
 *  labels than input channels, then the missing labels will be automatically
 *  generated.
 *
 *  @author  Edward A. Lee
 *  @version $Id$
 */
public class Show extends TypedAtomicActor implements Placeable {

    public Show(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(StringToken.class);

        // create the parameter and make it a string.
        labels = new Parameter(this, "labels", new StringToken(""));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type StringToken. */
    public TypedIOPort input;

    /** A comma separated list of labels. */
    public Parameter labels;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public port and parameter variables
     *  to the cloned ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Show newobj = (Show)super.clone(ws);
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.labels = (Parameter)newobj.getAttribute("labels");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read at most one token from each input channel and update
     *  the display with its value.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                StringToken token = (StringToken)input.get(i);
                String value = token.stringValue();
                _table.set(getFullName() + "_" + i, value);
            }
        }
    }

    /** Create a display table on the screen, if necessary, or clear the
     *  previously existing table.
     *  If a panel has not been specified, place the table into
     *  its own frame.  Otherwise, place it in the specified panel.
     *  If the panel is itself an instance of Table, then use that
     *  instance.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_table == null) {
            setPanel(_panel);
        }
        // FIXME: This doesn't deal with mutations, even between runs!
        if (!_populated) {
            int width = input.getWidth();
            for (int i = 0; i < width; i++) {
                // FIXME: Parse the parameter "labels"
                _table.line(getFullName() + "_" + i, "Input " + i + ":", "");
            }
        } else {
            int width = input.getWidth();
            for (int i = 0; i < width; i++) {
                _table.set(getFullName() + "_" + i, "");
            }
        }
    }

    /** Specify the panel in which the data should be displayed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the data will be displayed in its own frame.
     *  The data is also placed in its own frame if this method
     *  is called with a null argument.  If the argument is an
     *  instance of Table, then that object is used to display the data.
     *
     *  @param panel The panel into which to display the data.
     */
    public void setPanel(Panel panel) {
        _panel = panel;
        if (_panel == null) {
            if (_table == null) {
                _table = new Table();
                _populated = false;
            }
            // place the data in its own frame.
            // FIXME: This probably needs to be a PtolemyFrame, when one
            // exists, so that the close button is dealt with, etc.
            // FIXME: This does not actually result in a Frame appearing.
            Frame frame = new Frame(getFullName());
            frame.add(_table);
        } else {
            if (_panel instanceof Table) {
                _table = (Table)_panel;
            } else {
                if (_table == null) {
                    _table = new Table();
                    _populated = false;
                }
                _panel.add(_table);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Panel _panel = null;
    private Table _table = null;
    private boolean _populated = false;
}
