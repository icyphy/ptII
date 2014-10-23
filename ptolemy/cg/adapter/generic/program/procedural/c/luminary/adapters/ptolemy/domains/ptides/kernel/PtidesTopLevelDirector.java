/* Code generator adapter class associated with the PidesTopLevelDirector class.

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
package ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.ptides.kernel;

//////////////////////////////////////////////////////////////////
////PtidesTopLevelDirector

/**
 Code generator adapter associated with the PtidesTopLevelDirector class.
 This adapter generates Luminary specific code.

 This director starts a task for each actor. Each task has a specified
 name, stack size, priority and function code to execute. User can introduce
 annotations in an actor to specify these values. In particular, this
 adapter class looks for the "_stackSize" and "_priority" parameters and
 use their values to create the tasks. If these parameters are not specified,
 the code generator uses the default value 80 for stack size, and 0 for
 priority.

 Each task executes a given function which consists of the actor initialization,
 fire and wrapup code.

 @author Jia Zou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (jiazou)
 @Pt.AcceptedRating
 */
public class PtidesTopLevelDirector
extends
ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel.PtidesTopLevelDirector {

    /**
     * Construct the code generator adapter associated with the given
     * PtidesTopLevelDirector.
     * @param ptidesTopLevelDirector The associated
     *  ptolemy.domains.ptides.kernel.PtidesTopLevelDirector
     */
    public PtidesTopLevelDirector(
            ptolemy.domains.ptides.kernel.PtidesDirector ptidesTopLevelDirector) {
        super(ptidesTopLevelDirector);
    }
}
