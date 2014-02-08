/* An action for editing documentation.

 Copyright (c) 2006-2013 The Regents of the University of California.
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

import java.awt.event.ActionEvent;
import java.util.List;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.toolbox.FigureAction;

///////////////////////////////////////////////////////////////////
//// RemoveCustomDocumentationAction

/**
 This class provides an action for removing instance-specific documentation.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
public class RemoveCustomDocumentationAction extends FigureAction {

    /** Construct an instance of this action. */
    public RemoveCustomDocumentationAction() {
        super("Remove Custom Documentation");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the action by issuing a change request to remove the
     *  the first (and only?) DocAttribute contained by the target,
     *  if any.
     *  @param e The action event.
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        final NamedObj target = getTarget();

        // If the object does not contain an attribute of class
        // DocAttribute, then do nothing. Otherwise, remove it.
        if (target != null) {
            List docAttributeList = target.attributeList(DocAttribute.class);
            if (docAttributeList.size() != 0) {
                // Remove the doc attribute.
                String moml = "<deleteProperty name=\""
                        + ((DocAttribute) docAttributeList.get(0)).getName()
                        + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, target,
                        moml);
                target.requestChange(request);
            }
        }
    }
}
