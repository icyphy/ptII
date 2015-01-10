/*  JavaSE implementation of the TextFieldContainerInterface.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.gui;

import javax.swing.JTextField;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.lib.Sink;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TextFieldContainerJavaSE
/**
JavaSE implementation of the TextFieldContainerInterface.

@author Ishwinder Singh
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (ishwinde)
@Pt.AcceptedRating Red (ishwinde)
 */

public class TextFieldContainerJavaSE implements TextFieldContainerInterface {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initiate. Do nothing here.
     * @param sink Object of the Sink actor.
     * @exception IllegalActionException If the entity cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    @Override
    public void init(Sink sink) throws IllegalActionException,
    NameDuplicationException {
    }

    /** Place the visual representation of the actor into the specified container.
     *  @param container The container in which to place the object
     */
    @Override
    public void place(PortableContainer container) {
        _textfield = new JTextField();
        _textfield.setText("\t\t");
        if (container != null) {
            // If container is null, should we do what DisplayJavaSE.place() does?
            container.add(_textfield);
        }
        _textfield.setEditable(false);
    }

    /** Set the text to the value of the token.
     * @param value The Parameter containing the value
     */
    @Override
    public void setValue(Token value) {
        if (_textfield != null) {
            if (value == null) {
                // Delete the old text.
                _textfield.setText(null);
            } else {
                _textfield.setText(value.toString());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Text field for displaying the value
    private JTextField _textfield;

}
