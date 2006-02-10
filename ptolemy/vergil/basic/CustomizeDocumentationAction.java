/* An action for editing documentation.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.toolbox.FigureAction;

//////////////////////////////////////////////////////////////////////////
//// CustomizeDocumentationAction

/**
 This class provides an action for editing instance-specific documentation.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class CustomizeDocumentationAction extends FigureAction {

    /** Construct an instance of this action. */
    public CustomizeDocumentationAction() {
        super("Customize Documentation");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the action by first ensuring that the target has an
     *  instance of DocAttribute, and then opening an edit parameters
     *  dialog on that attribute.
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        final NamedObj target = getTarget();

        // If the object does not contain an attribute of class
        // DocAttribute, then create one.  Then open a dialog to edit
        // the parameters of the first such encountered attribute.
        if (target != null) {
            List docAttributeList = target.attributeList(DocAttribute.class);
            if (docAttributeList.size() == 0) {
                // Create a doc attribute, then edit its parameters.
                String moml = "<property name=\"" + "DocAttribute"
                        + "\" class=\"ptolemy.vergil.basic.DocAttribute\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, target,
                        moml) {
                    protected void _execute() throws Exception {
                        super._execute();
                        List docAttributes = target
                                .attributeList(DocAttribute.class);
                        // There shouldn't be more than one of these, but if there are,
                        // the new one is the last one.
                        DocAttribute attribute = (DocAttribute) docAttributes
                                .get(docAttributes.size() - 1);
                        _editDocAttribute(getFrame(), attribute, target);
                    }
                };
                target.requestChange(request);
            } else {
                DocAttribute attribute = (DocAttribute) docAttributeList.get(0);
                // In case parameters or ports have been added since the
                // DocAttribute was constructed, refresh it.
                attribute.refreshParametersAndPorts();
                _editDocAttribute(getFrame(), attribute, target);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /** Edit the specified documentation attribute.
     *  @param owner The owning frame.
     *  @param attribute The attribute to edit.
     *  @param taget The target whose documentation is being edited.
     */
    private void _editDocAttribute(Frame owner, DocAttribute attribute,
            NamedObj target) {
        new EditParametersDialog(owner, attribute, "Edit Documentation for "
                + target.getName());
    }
}
