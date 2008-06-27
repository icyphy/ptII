/* An attribute that creates a table to edit an array of records.

 Copyright (c) 2003-2007 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import java.awt.Frame;

import ptolemy.actor.gui.ArrayOfRecordsPane;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// ArrayOfRecordsConfigureFactory

/**
 If this class is contained by an actor, then double clicking on that
 actor will display a table that shows the value of an
 array of tokens contained by a parameter contained by the
 same container as this factory. The name of the parameter
 is given by the  <i>parameterName</i> attribute of this factory.
 It is required that the parameter contain an array of records.

 @author Edward A. Lee
 @version $Id: ArrayOfRecordsConfigureFactory.java 47482 2007-12-06 18:33:55Z cxh $
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (ptolemy)
 */
public class ArrayOfRecordsConfigureFactory extends EditorFactory {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ArrayOfRecordsConfigureFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        parameterName = new StringAttribute(this, "parameterName");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the attribute that is to be edited. */
    public StringAttribute parameterName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a top-level viewer for the specified object with the
     *  specified parent window.
     *  @param object The object to configure, which is required to
     *   contain a parameter with name matching <i>parameterName</i>
     *   and value that is an array of records.
     *  @param parent The parent window, which is required to be an
     *   instance of TableauFrame.
     */
    public void createEditor(NamedObj object, Frame parent) {
        try {
            Parameter attributeToEdit = (Parameter)object
                    .getAttribute(parameterName.getExpression(),
                    Parameter.class);
            Token value = attributeToEdit.getToken();
            if (!(value instanceof ArrayToken)) {
                MessageHandler.error(
                        "Parameter does not contain an array token: "
                        + attributeToEdit.toString());
                return;
            }
            ArrayOfRecordsPane pane = new ArrayOfRecordsPane();
            pane.display((ArrayToken)value);
            new ComponentDialog(parent, object.getFullName(), pane);
        } catch (KernelException ex) {
            MessageHandler.error(
                    "Cannot get specified string attribute to edit.", ex);
            return;
        }
    }
}
