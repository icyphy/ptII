/* An instance of FunctionDependencyOfModalModel describes the function 
dependence information of a modal model.

 Copyright (c) 2003 The Regents of the University of California.
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

package ptolemy.domains.fsm.modal;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.FunctionDependencyOfCompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependencyOfModalModel
/** An instance of FunctionDependencyOfModalModel describes the function 
dependence information of a modal model.

@see ptolemy.actor.FunctionDependencyOfCompositeActor
@author Haiyang Zheng
@version $Id $
@since Ptolemy II 3.1
*/
public class FunctionDependencyOfModalModel extends FunctionDependencyOfCompositeActor {

    /** Construct a FunctionDependency in the given container. 
     *  @param container The container has this FunctionDependency object.
     */
    public FunctionDependencyOfModalModel(Actor container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    /** Get a list of refinements of the current state for function 
     *  dependency calculation.  
     *  @return a list of refinements of the current state. 
     */
    protected List _getEntities() {
        LinkedList entities = new LinkedList();
        try {
            Actor[] actors = 
                ((ModalModel)_container).getController().
                    currentState().getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    entities.add(actors[i]);
                }
            }
        } catch (IllegalActionException e) {
            MessageHandler.error("Invalid refinements.", e);
        }
        return entities;
    }

}
