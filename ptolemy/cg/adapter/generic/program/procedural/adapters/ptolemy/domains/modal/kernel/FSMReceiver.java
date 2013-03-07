/* Code generator helper for FSMReceiver.

 Copyright (c) 2012 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.kernel;

import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// FSMReceiver

/**
 Code generator helper for FSMReceiver.

 @author Christopher Brooks
 @version $Id: FSMActor.java 64869 2012-10-26 12:54:51Z eal $
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class FSMReceiver extends SDFReceiver {
    /** Construct the code generator helper associated with the given FSMActor.
     *  @param component The associated component.
     *  @exception NameDuplicationException If the container already contains a
     *  a code generator adapter for this particular FSMActor.
     *  @exception IllegalActionException If the NamedProgramCodeGeneratorAdapter throws
     *  an IllegalActionException.
     */
    /** Construct an adapter for an SDF receiver.
     *  @param receiver The SDFReceiver for which an adapter is constructed.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    public FSMReceiver(ptolemy.domains.modal.kernel.FSMReceiver receiver)
            throws IllegalActionException {
        super(null);
        _component = receiver;
    }
}
