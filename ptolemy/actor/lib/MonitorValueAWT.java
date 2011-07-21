/*  AWT implementation for MonitorValueInterface 
 
 Copyright (c) 2011 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.awt.Rectangle;

import javax.swing.JTextField;

import ptolemy.actor.gui.PortableContainer;
import ptolemy.data.expr.Parameter;

/**
AWT implementation of the MonitorValueInterface 

@author Ishwinder Singh
@version $Id$
@since @since Ptolemy II 8.1
@Pt.ProposedRating Red (ishwinde)
@Pt.AcceptedRating Red (ishwinde)
*/

public class MonitorValueAWT implements MonitorValueInterface {

    /** Initialize the text field for the MonitorValue
     */
    public void init() {
        _textfield = new JTextField();
        _textfield.setBounds(new Rectangle(150, 50));
    }

    /** Place the visual representation of the actor into the specified container.
     *  @param container The container in which to place the object
     */
    public void place(PortableContainer container) {
        if (_textfield == null) {
            init();
        }
        _textfield.setText("MonitorValue");
        container.add(_textfield);
        _textfield.setEditable(false);
    }

    /** Set the text to the value of the parameter
     * @param value The Parameter containing the value
     */
    public void setValue(Parameter value) {
        _textfield.setText(value.toString());

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Text field for displaying the value
    private JTextField _textfield;

}
