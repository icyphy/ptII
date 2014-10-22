/* A simple interface for Vergil GUI actions performed on the model.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
 2
 */
package ptolemy.vergil.basic;

import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// IGuiAction

/**
A simple interface that allows to define actions for Vergil that
operate on the model. Vergil needs no specific dependencies on
the class implementing the actions but can just call doAction
to trigger it.

@author  Christian Motika
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cmot)
@Pt.AcceptedRating Red (cmot)
 */
public interface IGuiAction {

    /**
     * Perform the action/modification on the current model triggered
     * by a Vergil GUI action.
     *
     * @param model the current Ptolemy model
     */
    public void doAction(NamedObj model);

}
