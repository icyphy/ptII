/* A Petri net place.

 Copyright (c) 2001-2014 The Regents of the University of California.
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

 ***************************************************************

 PetriNet Director Modifications

 Copyright (c) 2010-2014 The University of Florida

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

import java.util.Comparator;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Place

/**
 A Petri net place compatible with the PetriNetDirector. A Petri net
 place is a basic component of the Petri Net model. Another basic
 component is the Transition. A place is connected to transitions. It
 contains an integer as the marking of the place, which represents
 the number of tokens in the place. The operation of the Petri net is
 controlled by the marking and the weights of arcs connecting places
 and transitions.

 The methods here are used to manipulate the integer marking.
 The TemporaryMarking is used for checking whether a transition
 is ready or not.

 @author  Yuke Wang and Edward A. Lee, modified by Zach Ezzell
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Place extends Transformer implements Comparator {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */

    public Place(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        initialMarking = new Parameter(this, "initialMarking");
        initialMarking.setExpression("0");
        initialMarking.setTypeEquals(BaseType.INT);

        input.setMultiport(true);
        input.setTypeEquals(BaseType.GENERAL);
        output.setMultiport(true);

        output.setTypeEquals(BaseType.GENERAL);
    }

    /** The method compares two objects that should be Places.
     *
     * @param o1
     *          The first place.
     *
     * @param o2
     *          The second place.
     *
     * @return
     *          A negative integer, zero, or a positive integer as
     *          the first argument is less than, equal to, or greater
     *          than the second.
     *
     * @exception ClassCastException If either object cannot be casted
     *   in to a Place.
     */
    @Override
    public int compare(Object o1, Object o2) throws ClassCastException {
        Place p1 = (Place) o1;
        Place p2 = (Place) o2;
        return p1.getName().compareToIgnoreCase(p2.getName());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** The number of initial tokens in the place. This is an integer. */
    public Parameter initialMarking;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Decrease the _currentMarking.
     *  @param i the number to be decreased for the marking in the place.
     */
    public void decreaseMarking(int i) {
        _currentMarking = _currentMarking - i;
    }

    /** Decrease the _temporaryMarking by i.
     *  @param i the number to be decreased for the TemporaryMarking
     *   in the place.
     */
    public void decreaseTemporaryMarking(int i) {
        _temporaryMarking = _temporaryMarking - i;
    }

    /** getMarking() is to get the _currentMarking of the place.
     *  @return the currentMarking of the place.
     */
    public int getMarking() {
        return _currentMarking;
    }

    /** getTemporaryMarking() is to get the temporaryMarking of the place.
     *  temporaryMarking is used for checking whether the _currentMarking
     *  of the place is bigger than the sum of all links between the place
     *  and a transition. TemporaryMarking is used in Transition.prefire.
     *  @return the _temporaryMarking of the place.
     *  @see #setTemporaryMarking(int)
     */
    public int getTemporaryMarking() {
        return _temporaryMarking;
    }

    /** Increase the _currentMarking.
     *  @param i the number to be increased for the marking in the place.
     */
    public void increaseMarking(int i) {
        _currentMarking = _currentMarking + i;
    }

    /** Set the current marking equal to the initial marking.
     *  @exception IllegalActionException If the initialMarking parameter
     *   throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _currentMarking = ((IntToken) initialMarking.getToken()).intValue();
        _temporaryMarking = _currentMarking;
    }

    /** Set the _temporaryMarking.
     *  @param i set the TemporaryMarking of the place to i.
     *  @see #getTemporaryMarking()
     */
    public void setTemporaryMarking(int i) {
        _temporaryMarking = i;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Current marking. */
    private int _currentMarking = 0;

    /** Temporary marking. */
    private int _temporaryMarking = 0;
}
