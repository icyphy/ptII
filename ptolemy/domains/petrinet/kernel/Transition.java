/* Petrinet Transition

 Copyright (c) 2010-2013 The University of Florida

 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF FLORIDA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF FLORIDA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF FLORIDA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 FLORIDA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY

 */

package ptolemy.domains.petrinet.kernel;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//Transition

/**
 * A Transition to be used as part of a Petri Net and in
 * conjunction with the PetriNetDirector.  The transition accepts
 * multiple places as input and output.  A transition fires when
 * one of its input places contains a token.  When a transition
 * fires, it places a token in each of its output places.
 *
 * @author Zach Ezzell
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Transition extends TypedCompositeActor {

    /**
     * Construct a new Transition.
     *
     * @param entity
     *           The CompositeEntity.
     * @param name
     *            The name of the transition.
     *
     * @exception IllegalActionException
     *                If the name has a period in it.
     *
     * @exception NameDuplicationException
     *                If the container already contains an entity with
     *                the specified name.
     */

    public Transition(CompositeEntity entity, String name)
            throws IllegalActionException, NameDuplicationException {
        super(entity, name);
        inputPort = new TypedIOPort(this, "input", true, false);
        inputPort.setTypeEquals(BaseType.GENERAL);
        inputPort.setMultiport(true);

        outputPort = new TypedIOPort(this, "output", false, true);
        outputPort.setTypeEquals(BaseType.GENERAL);
        outputPort.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * The input port for the transition.
     */
    TypedIOPort inputPort;

    /**
     * The output port for the transition.
     */
    TypedIOPort outputPort;

}
