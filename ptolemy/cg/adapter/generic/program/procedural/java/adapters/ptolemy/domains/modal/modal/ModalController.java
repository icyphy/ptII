/* Code generator helper for modal controller.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.modal.modal;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ModalController

/**
 Code generator helper for modal controller.

 @author Gang Zhou, with modifications by Shanna-Shaye Forbes
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class ModalController
extends
ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.modal.ModalController {

    /** Construct the code generator helper associated
     *  with the given modal controller.
     *  @param component The associated component.
     */
    public ModalController(ptolemy.domains.modal.modal.ModalController component) {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Public methods                    ////
    /** Generate the variable declaration for the transition flag.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                super.generateVariableDeclaration());

        String name = _myController.getFullName().substring(1);
        String modalName = name.replace("_Controller", "");
        //name = name.replace('.', '_');
        modalName = modalName.replace('.', '_');

        code.append("public static int " + modalName + "__transitionFlag;"
                + _eol);
        return code.toString();
    }
}
