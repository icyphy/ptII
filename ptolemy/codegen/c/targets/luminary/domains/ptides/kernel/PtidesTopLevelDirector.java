/* Code generator helper class associated with the PidesDirector class.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.codegen.c.targets.luminary.domains.ptides.kernel;

//////////////////////////////////////////////////////////////////
////PtidesDirector

/**
 Code generator helper associated with the PtidesDirector class.
 This helper generates Luminary specific code.

 This director starts a task for each actor. Each task has a specified
 name, stack size, priority and function code to execute. User can introduce
 annotations in an actor to specify these values. In particular, this
 helper class looks for the "_stackSize" and "_priority" parameters and
 use their values to create the tasks. If these parameters are not specified,
 the code generator uses the default value 80 for stack size, and 0 for
 priority.

 Each task executes a given function which consists of the actor initialization,
 fire and wrapup code.

 @author Jia Zou
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (jiazou)
 @Pt.AcceptedRating
 */
public class PtidesTopLevelDirector extends ptolemy.codegen.c.domains.ptides.kernel.PtidesTopLevelDirector {

    /**
     * Construct the code generator helper associated with the given
     * PtidesDirector.
     * @param ptidesDirector The associated
     *  ptolemy.domains.ptides.kernel.PtidesDirector
     */
    public PtidesTopLevelDirector(ptolemy.domains.ptides.kernel.PtidesTopLevelDirector ptidesTopLevelDirector) {
        super(ptidesTopLevelDirector);
    }
}
