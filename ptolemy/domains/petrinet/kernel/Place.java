/* A Petri net place.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.petrinet.kernel;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// Place
/**
A Petri net place.

@author  Yuke Wang and Edward A. Lee
@version $Id$
*/
public class Place extends Transformer {

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


        //  We need multiport, but each channel has only one link.
        // however, at this moment, we do not check how many links we have
        // from one place to one transition.
        // yuke



        output.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** The number of initial tokens in the place. This is an integer. */

    public Parameter initialMarking;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read an available input token, and send it to a randomly chosen
     *  output channel.
     *  @exception IllegalActionException If the send method throws it.
     */

    public int getMarking() {
        return _currentMarking;
    }

    public void increaseMarking(int i) {
        _currentMarking = _currentMarking + i;
    }

    public void decreaseMarking(int i) {
        _currentMarking = _currentMarking - i;
    }
    public void printMarking() {
        System.out.println("the current marking is " + _currentMarking);
    }

    /** Set the current marking equal to the initial marking.
     *  @exception IllegalActionException If the initialMarking parameter
     *   throws it.
     */
    public void initialize() throws IllegalActionException {
        _currentMarking = ((IntToken)initialMarking.getToken()).intValue();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    // Current marking.
    private int _currentMarking = 0;




}
