/* The integrator in the CT domain for test purpose.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ct.kernel.test;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// Integrator

/**
   A wrapper of CTBaseIntegrator. The only purpose of this actor is
   that it is in the ct.lib package.
   @author Jie Liu
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Red (liuj)
   @Pt.AcceptedRating Red (cxh)
   @see ptolemy.domains.ct.kernel.CTBaseIntegrator
*/
public class CTTestIntegrator extends CTBaseIntegrator {
    /** construct the integrator.
     * @see ptolemy.domains.ct.kernel.CTBaseIntegrator
     * @param container The TypedCompositeActor this star belongs to
     * @param name The name.
     * @exception NameDuplicationException If another star already had
     * this name.
     * @exception IllegalActionException If there was an internal problem.
     */
    public CTTestIntegrator(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** call to balance the history.
     *  @exception IllegalActionException If the director has an invalid
     *   time resolution parameter.
     */
    public void balanceHistory() throws IllegalActionException {
        double stepsize = ((CTDirector) getDirector()).getCurrentStepSize();
        _history.rebalance(stepsize);
    }
}
