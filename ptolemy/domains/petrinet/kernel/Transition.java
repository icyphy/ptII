/* A Petri net transition.

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


import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;

import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Nameable;

import java.util.Random;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// Transition
/**
A Petri net transition

@author  Yuke Wang and Edward A. Lee
@version $Id$
*/
public class Transition extends Transformer {

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
    public Transition(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.GENERAL);
        output.setMultiport(true);
        output.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** fire has two parts: to modify the marking in the output places and
     * to modify the marking at the input places. The method also checks
     * whether there is a weight in the middle of the place-transition
     * connection. The weight is defined as single-input single-output
     * transformer to hold the weight of the arcs.
     */
    public void fire() throws IllegalActionException {
        int k = output.getWidth();
        System.out.println("-- inside the fire for transition -- "
                + "the output width is" + k);

        System.out.println("start to increase the place marking "
                + "for output places");

        //            for(int j = 0; j < output.getWidth(); j++)  {
        Iterator outports = output.connectedPortList().iterator();
        while(outports.hasNext())  {
           //output port should be connected to weights.
            IOPort weightport = (IOPort) outports.next();
            if (weightport!= null) {
                Nameable arcweight = weightport.getContainer();
                if (arcweight instanceof NamedObj)
                    System.out.println("the object is"
                            + arcweight.getFullName());
                if (arcweight instanceof Place
                        || arcweight instanceof Transition)  {
                    System.out.println("wrong connection, it should be "
                            + "weight here" + arcweight.getFullName());
                    return;
                }
                if (arcweight instanceof Weight)  {
                    Weight arc = (Weight ) arcweight;
                    int  tempweight = arc.getWeight();
                    IOPort weightOutPort = arc.output;

                    Iterator placeports =
                        weightOutPort.connectedPortList().iterator();
                    while(placeports.hasNext())  {
                        IOPort   placeport = (IOPort) placeports.next();
                        if (placeport!= null) {
                            Nameable place = placeport.getContainer();
                            if (place instanceof NamedObj)
                                System.out.println("the place is"
                                        + place.getFullName());
                            if (place instanceof Transition
                                    || place instanceof Weight)  {
                                System.out.println("wrong connection, it "
                                        + "should be place here"
                                        + place.getFullName());
                                return;
                            }
                            if (place instanceof Place)  {
                                Place tempplace = (Place ) place;
                                System.out.println("the old place weight is"
                                        + tempplace.getMarking());
                                tempplace.increaseMarking(tempweight);
                                System.out.println("the new place weight is"
                                        + tempplace.getMarking());
                            }
                        }
                    }
                }
            }
        }
        //                 for(int j = 0; j < input.getWidth(); j++)  {

        System.out.println("start to decrease the place marking for "
                + "input places"  );

        Iterator inports = input.connectedPortList().iterator();
        while(inports.hasNext())  {
            IOPort weightport = (IOPort) inports.next();
            if (weightport!= null) {
                Nameable arcweight = weightport.getContainer();
                if (arcweight instanceof NamedObj)
                    System.out.println("the weight is "
                            + arcweight.getFullName());
                if (arcweight instanceof Place
                        || arcweight instanceof Transition)  {
                    System.out.println("wrong connection, it should be "
                            + "weight here" + arcweight.getFullName());
                    return;
                }
                if (arcweight instanceof Weight)  {
                    Weight arc = (Weight )  arcweight;
                    int  tempweight = arc.getWeight();
                    IOPort weightOutPort = arc.input;
                    Iterator placeports =
                        weightOutPort.connectedPortList().iterator();
                    while(placeports.hasNext())  {
                        IOPort   placeport = (IOPort) placeports.next();
                        if (placeport!= null) {
                            Nameable place = placeport.getContainer();
                            if (place instanceof NamedObj)
                                System.out.println("the place is "
                                        + place.getFullName());
                            if (place instanceof Transition
                                    || place instanceof Weight)  {
                                System.out.println("wrong connection, it "
                                        + "should be place here "
                                        + place.getFullName());
                                return;
                            }
                            if (place instanceof Place)  {
                                Place tempplace = (Place ) place;
                                System.out.println("the arc weight is"
                                        + tempweight + " the place token is "
                                        + tempplace.getMarking());
                                tempplace.decreaseMarking(tempweight);
                                System.out.println("the arc weight is "
                                        + tempweight + " the place token is "
                                        + tempplace.getMarking());
                            }
                        }
                    }
                }
            }
        }
    }

    // prefire is similar with fire, it checks all the input places
    // to see whether the marking in that place is bigger than
    // the weight on the arc or not.


    public boolean prefire() throws IllegalActionException {
        int k = input.getWidth();
        boolean readyToFire = true;
        System.out.println("-- inside the prefire for transition -- "
                + "*the width is" + k);

        //          for(int j = 0; j < input.getWidth(); j++)  {
        Iterator inports = input.connectedPortList().iterator();
        while(inports.hasNext())  {
            IOPort weightport = (IOPort) inports.next();
            if (weightport!= null) {
                Nameable arcweight = weightport.getContainer();
                if (arcweight instanceof NamedObj)
                    System.out.println("the weight is "
                            + arcweight.getFullName());
                if (arcweight instanceof Place
                        || arcweight instanceof Transition)  {
                    System.out.println("wrong connection, it should be "
                            + "weight here " + arcweight.getFullName());
                    return false;
                }
                if (arcweight instanceof Weight)  {
                    Weight arc = (Weight )  arcweight;
                    int  tempweight = arc.getWeight();
                    IOPort weightOutPort = arc.input;
                    Iterator placeports =
                        weightOutPort.connectedPortList().iterator();
                    while(placeports.hasNext())  {
                        IOPort   placeport = (IOPort) placeports.next();
                        if (placeport!= null) {
                            Nameable place = placeport.getContainer();
                            if (place instanceof NamedObj)
                                System.out.println("the place is "
                                        + place.getFullName());
                            if (place instanceof Transition
                                    || place instanceof Weight)  {
                                System.out.println("wrong connection, it "
                                        + "should be place here "
                                        + place.getFullName());
                                return false;
                            }
                            if (place instanceof Place)  {
                                Place tempplace = (Place ) place;
                                System.out.println("the arc weight is"
                                        + tempweight + " the place token is"
                                        + tempplace.getMarking());
                                if ( tempweight > tempplace.getMarking() ) {

                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return readyToFire;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    private Token _token = new Token();
}







/*
  if (input.hasToken(j))  {
  Token token = actor.input.get(j);
  System.out.println(actor.getName()
  + " consuming input from channel " + j );
  }


*/

/*      System.out.println("fire within transition ^^^^^^^^^^^^^^^^^^^^^^");

        for(int i = 0; i < input.getWidth(); i++) {

        if (input.hasToken(i)) {
        Token token = input.get(i);

        System.out.println(getName()
        + " consuming input from channel " + i);
        }
        }
        for(int i = 0; i < output.getWidth(); i++) {
        output.send(i, _token);

        System.out.println(getName()
        + " producing output on channel " + i);
        }

*/


/*                System.out.println("------inside the prefire for "
                  + "transition--------");

                  Iterator relations = input.linkedRelationList().iterator();
                  while(relations.hasNext()) {
                  IORelation relation = (IORelation) relations.next();
                  if (relation != null) {

                  Nameable container = relation.getContainer();

                  if (container instanceof NamedObj)
                  System.out.println("the container is"
                       + container.getFullName());
                  }
                  }


*/



