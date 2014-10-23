/* An action for editing documentation.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.actor.DocApplicationSpecializer;
import ptolemy.vergil.toolbox.FigureAction;

///////////////////////////////////////////////////////////////////
//// CustomizeDocumentationAction

/**
 An action for editing instance-specific documentation.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
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
    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        final NamedObj target = getTarget();
        boolean done = false;
        // If the object does not contain an attribute of class
        // DocAttribute, then create one.  Then open a dialog to edit
        // the parameters of the first such encountered attribute.
        List docAttributeList = null;

        if (target != null) {
            Parameter docApplicationSpecializerParameter = null;
            try {
                //find the configuration
                List configsList = Configuration.configurations();
                Configuration config = null;
                for (Iterator it = configsList.iterator(); it.hasNext();) {
                    config = (Configuration) it.next();
                    if (config != null) {
                        break;
                    }
                }
                if (config == null) {
                    throw new InternalErrorException(target, null,
                            "Failed to find configuration");
                }

                // Check to see if the configuration has a
                // _docApplicationSpecializer parameter and if it does,
                // let it handle the customization

                docApplicationSpecializerParameter = (Parameter) config
                        .getAttribute("_docApplicationSpecializer",
                                Parameter.class);
            } catch (IllegalActionException iae) {
                // Ignore.  just let the default action happen
                System.out.println("Error getting the documentation "
                        + "specializer: " + iae.getMessage());
            }

            if (docApplicationSpecializerParameter != null) {
                // If there is a docApplicationSpecializer, use it to
                // customize the documentation since it knows about
                // the special doc attribute

                String docApplicationSpecializerClassName = docApplicationSpecializerParameter
                        .getExpression();
                try {
                    Class docApplicationSpecializerClass = Class
                            .forName(docApplicationSpecializerClassName);
                    final DocApplicationSpecializer docApplicationSpecializer = (DocApplicationSpecializer) docApplicationSpecializerClass
                            .newInstance();
                    String docAttributeClassName = docApplicationSpecializer
                            .getDocumentationAttributeClassName();
                    Class docAttributeClass = Class
                            .forName(docAttributeClassName);
                    if (docApplicationSpecializerClass != null
                            && docAttributeClass != null) {
                        docAttributeList = target
                                .attributeList(docAttributeClass);
                    }

                    if (docAttributeList.size() == 0) {
                        docApplicationSpecializer
                        .handleDocumentationAttributeDoesNotExist(
                                getFrame(), target);

                    } else { //edit the existing attribute
                        final Attribute docAttribute = (Attribute) docAttributeList
                                .get(docAttributeList.size() - 1);
                        ChangeRequest request = new ChangeRequest(this,
                                "Customize documentation.") {
                            @Override
                            protected void _execute() throws Exception {
                                //_editDocAttribute(getFrame(), docAttribute, target);
                                docApplicationSpecializer.editDocumentation(
                                        getFrame(), docAttribute, target);
                            }
                        };
                        target.requestChange(request);
                    }
                } catch (Throwable throwable) {
                    System.out
                    .println("Failed to call doc application specializer "
                            + "class \""
                            + docApplicationSpecializerClassName
                            + "\" on class \""
                            + docApplicationSpecializerClassName
                            + "\".");
                }
                done = true;
            }

            if (!done) {
                //assign the docAttributeList the default DocAttribute if it
                //wasn't assigned by the specializer
                docAttributeList = target.attributeList(DocAttribute.class);
                if (docAttributeList.size() == 0) {
                    // Create a doc attribute, then edit its parameters.
                    String moml = "<property name=\""
                            + "DocAttribute"
                            + "\" class=\"ptolemy.vergil.basic.DocAttribute\"/>";
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            target, moml) {
                        @Override
                        protected void _execute() throws Exception {
                            super._execute();
                            List docAttributes = target
                                    .attributeList(DocAttribute.class);

                            // There shouldn't be more than one of
                            // these, but if there are, the new one is
                            // the last one.

                            DocAttribute attribute = (DocAttribute) docAttributes
                                    .get(docAttributes.size() - 1);
                            _editDocAttribute(getFrame(), attribute, target);
                        }
                    };
                    target.requestChange(request);
                } else {

                    // In case there is more than one such attribute,
                    // get the last one.

                    final DocAttribute attribute = (DocAttribute) docAttributeList
                            .get(docAttributeList.size() - 1);

                    // Do the update in a change request because it may
                    // modify the DocAttribute parameter.

                    ChangeRequest request = new ChangeRequest(this,
                            "Customize documentation.") {
                        @Override
                        protected void _execute() throws Exception {

                            // In case parameters or ports have been
                            // added since the DocAttribute was
                            // constructed, refresh it.

                            attribute.refreshParametersAndPorts();
                            _editDocAttribute(getFrame(), attribute, target);
                        }
                    };
                    target.requestChange(request);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Edit the specified documentation attribute.
     *  @param owner The owning frame.
     *  @param attribute The attribute to edit.
     *  @param target The target whose documentation is being edited.
     */
    private void _editDocAttribute(Frame owner, DocAttribute attribute,
            NamedObj target) {
        new EditParametersDialog(owner, attribute, "Edit Documentation for "
                + target.getName());
    }
}
