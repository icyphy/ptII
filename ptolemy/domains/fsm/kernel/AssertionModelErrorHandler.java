/* Handle a model error throwed by Assertion failures.

 Copyright (c) 2000-2002 The Regents of the University of California.
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
@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)

*/

package ptolemy.domains.fsm.kernel;

import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.ModelErrorHandler;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// AssertionModelErrorHandler
/**
A model error handler dealing with the model errors thrown by assertion failurs.
A model error is an exception that is passed up the Ptolemy II hierarchy for 
handling until a container with a registered error handler is found.  
If there is no registered error handler, then the error is ignored.  
It is like throwing an exception, except that instead of unraveling 
the calling stack, it travels up the Ptolemy II hierarchy.  
This class handles the error by checking if there is at least one non-preemptive
transition that is enabled. If there is no one existing, the model error (exception)
will be reported to higher level in hierarchy. If there is at least one, the
model error is ignored. 

@see ModalController
@author Haiyang Zheng
@version $Id$
*/
public class AssertionModelErrorHandler implements ModelErrorHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle a model error by checking if there is any enabled non-preemptive
     *  transtions. If there is some one, the model error is ignored. Otherwise,
     *  the model error (exception) will be passed to higher level in hierarchy.
     *  @param context The FSMActor which controls the state transtions.
     *  @param exception An exception that represents the model error.
     *  @return True if the error has been handled, or nothing if the model error
     *  is thrown to higher level.
     *  @exception IllegalActionException The model error exception.
     */
    public boolean handleModelError(
            NamedObj context,
            IllegalActionException exception)
            throws IllegalActionException {
        
        if (!exception.getMessage().trim().startsWith("AssertionModelError")) throw exception;
        
        ((FSMActor) context)._setInputsFromRefinement();  
        State st = ((FSMActor) context).currentState();     
        Transition tr = ((FSMActor) context)._chooseTransition(st.nonpreemptiveTransitionList());        
        
        if (tr == null) {
            //System.out.println("ModelError is not handled but reported to upper level.");
        	throw exception;
        }

        //System.out.println("ModelError is discarded.");
        return true;
    }
}
