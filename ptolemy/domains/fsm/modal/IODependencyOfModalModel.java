/* An instance of IODependencyOfModalModel describes the input-output 
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
import ptolemy.actor.IODependencyOfCompositeActor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// IODependencyOfModalModel
/** An instance of IODependencyOfModalModel describes the input-output 
dependence information of a modal model.

@see ptolemy.actor.IODependencyOfCompositeActor
@author Haiyang Zheng
@version $Id $
@since Ptolemy II 3.1
*/
public class IODependencyOfModalModel extends IODependencyOfCompositeActor {

    /** Construct an IODependency in the given container. 
     *  @param container The container has this IODependency object.
     */
    public IODependencyOfModalModel(Actor container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    protected List _getEntities() {
        LinkedList entities = new LinkedList();
        try {
            Actor[] actors = 
                ((ModalModel)_container).getController().currentState().getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    entities.add(actors[i]);
                }
            }
        } catch (IllegalActionException e) {
           // dealing with the exception 
           // FIXME: how? make this method throw the exception?
           // Similiar things happen in FSMActor getIODependencies method.
        }
        return entities;
    }

}
