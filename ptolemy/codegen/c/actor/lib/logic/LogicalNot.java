/* A helper class for ptolemy.actor.lib.logic.LogicalNot

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
package ptolemy.codegen.c.actor.lib.logic;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;

//////////////////////////////////////////////////////////////////////////
//// LogicalNot

/**
 A helper class for ptolemy.actor.lib.logic.LogicalNot.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.1
 @see ptolemy.actor.lib.logic.LogicalNot
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class LogicalNot extends CCodeGeneratorHelper {
    /** Constructor method for the LogicFunction helper.
     *  @param actor the associated actor.
     */
    public LogicalNot(ptolemy.actor.lib.logic.LogicalNot actor) {
        super(actor);
    }
}
