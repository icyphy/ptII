
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
@ProposedRating Red (yukewang@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.petrinet.kernel;


import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;

import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
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
    ////                         ports and parameters              ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

/**  Fire has two parts: to modify the marking in the output places and
 *   to modify the marking at the input places. For each output place,
 *   the fire increases the marking by the weight at the arcs.
 *   For each input place, the fire decreases the marking by the weight
 *   at the connected arcs.
 *   Multiple arcs can exist between a place and a transition.
 *   Furthermore, the arcs can be marked as a "Weight" parameter or
 *   not marked. Not-marked arcs are treated as default weight 1.
 *   Loops can exist as well. 
 *
 *  
 *
 **/

public void fire() throws IllegalActionException {

    System.out.print("start to increase the place marking for outputs");
    System.out.println(" width is " + output.getWidth()  );
    Iterator outRelations = output.linkedRelationList().iterator();
    while(outRelations.hasNext())  {
    
        IORelation weights = (IORelation) outRelations.next();
        if (weights != null) {
           Iterator placePorts = weights.linkedDestinationPortList().iterator();
           while(placePorts.hasNext()) {
               IOPort placePort = (IOPort) placePorts.next();
               Place place = (Place) placePort.getContainer();
               int i = place.getMarking();
               Attribute temporaryAttribute = (Attribute )
                          weights.getAttribute("Weight");
               if (temporaryAttribute == null) {
                   place.increaseMarking(1);
	             System.out.print("default value 1");
                   System.out.print(" source place "+ place.getFullName() +  
                           " original tokens " +i);
                   System.out.println(" new token  " + place.getMarking());
               }
               else if (temporaryAttribute instanceof Variable) {
                   Variable tAttribute = (Variable) temporaryAttribute;
                   Token weightToken = (Token) tAttribute.getToken();
                   if (weightToken instanceof ScalarToken) {
                       ScalarToken wToken = (ScalarToken) weightToken;
                       int j = wToken.intValue();
                       place.increaseMarking(j);
                       System.out.print("source place "+ place.getFullName() +
                               " original tokens " +i);
                       System.out.println("  new token  " + place.getMarking());
                   }
               }
               place.setTemporaryMarking(place.getMarking());
           }
       }
       else
           System.out.println("the arc weight is null");
   }

   System.out.print("start to decrease the place marking for input places"  );
   System.out.println("the input width is" + input.getWidth());

   Iterator inRelations = input.linkedRelationList().iterator();
   while(inRelations.hasNext())  {
       IORelation weights = (IORelation) inRelations.next();
       if (weights != null) {
           Iterator placePorts = weights.linkedSourcePortList().iterator();
           while(placePorts.hasNext()) {
               IOPort placePort = (IOPort) placePorts.next();
               Place place = (Place) placePort.getContainer();
               int i = place.getMarking();

               Attribute temporaryAttribute = (Attribute ) 
                       weights.getAttribute("Weight");
               if (temporaryAttribute == null) {
                   place.decreaseMarking(1);
	             System.out.print("default value 1");
                   System.out.print(" source place "+ place.getFullName()+ 
                           " original tokens " +i);
                   System.out.println("  new token  " + place.getMarking());
               }
               else if (temporaryAttribute instanceof Variable) {
                   Variable tAttribute = (Variable) temporaryAttribute;
                   Token weightToken = (Token) tAttribute.getToken();
                   if (weightToken instanceof ScalarToken) {
                       ScalarToken wToken = (ScalarToken) weightToken;
                       int j = wToken.intValue();
                       place.decreaseMarking(j);
                       System.out.print("source place "+ place.getFullName() +
                               " original tokens " +i);
                       System.out.println("  new token  " + place.getMarking());
                   }
               }
               place.setTemporaryMarking(place.getMarking());
           }
       }
       else
           System.out.println("the arc weight is null");
   }
}

/** Prefire is similar with fire. It checks all the input places
 *  to see whether the marking in that place is bigger than
 *  the weight on the arc or not.

 *  We assume the petrinet is specified by arcs connecting places and   
 *  transitions. Arcs can be marked by Weight attribute, or it can be unmarked.
 *  multiple arcs can be between a place and a transition.
 *  Unmarked arcs are treated as weight 1. 
 *
 *  To monitor the multiple links, we use the temporaryMarking varialbe.
 *  TemporaryMarking starts with the same marking as the currentMarking.
 *  Each time a link is seen, the temporaryMarking decreases the value
 *  of the weight on the link. If at the end, the temporaryMarking is
 *  less than 0, then the sum of the weights of all links between 
 *  the place and the transition is bigger than the marking in the place
 *  the transition is not ready to fire.
 **/

public boolean prefire() throws IllegalActionException {

    int k = input.getWidth();
    boolean readyToFire = true;
    System.out.println("--inside the prefire for transition--the width is" + k);
              
    Iterator inRelations = input.linkedRelationList().iterator();
    while(inRelations.hasNext())  {
        IORelation inWeights = (IORelation) inRelations.next();
        if (inWeights != null) {
            Iterator temporaryPlacePorts = 
                     inWeights.linkedSourcePortList().iterator();
            while(temporaryPlacePorts.hasNext()) {
                IOPort temporaryPlacePort = (IOPort) temporaryPlacePorts.next();
                Place temporaryPlace = (Place) 
                        temporaryPlacePort.getContainer();
                int i = temporaryPlace.getMarking();
                temporaryPlace.setTemporaryMarking(i);
            }
        }
    }

    Iterator relations = input.linkedRelationList().iterator();
    while(relations.hasNext())  {
        IORelation weights = (IORelation) relations.next();
        if (weights != null) {
            Iterator placePorts = weights.linkedSourcePortList().iterator();
            while(placePorts.hasNext()) {
                IOPort placePort = (IOPort) placePorts.next();
                Place place = (Place) placePort.getContainer();
                int i = place.getMarking();
                System.out.print("source place "+ place.getFullName() + 
                        " tokens  "+ i + " " + place.getTemporaryMarking());

                Attribute temporaryAttribute = (Attribute) 
                        weights.getAttribute("Weight");
                if (temporaryAttribute == null)  {
	              place.decreaseTemporaryMarking(1);  
                           //unmarked arcs have default value 1
                    System.out.println("  the weight is default value 1");
                }
                else if (temporaryAttribute instanceof Variable) {
                    Variable tAttribute = (Variable) temporaryAttribute;
                    Token weightToken = (Token) tAttribute.getToken();
                    if (weightToken instanceof ScalarToken) {
                        ScalarToken wToken = (ScalarToken) weightToken;
                        int j = wToken.intValue();
                        System.out.println("  the weight is " + j );
                        place.decreaseTemporaryMarking(j);
                    }
                }
                if (place.getTemporaryMarking() <0) {
                    System.out.print("the place has not enough tokens");
                    System.out.println(" Temporarytokens" + 
                                  place.getTemporaryMarking());
                    return false;
                }
            }   
        }
        else
            System.out.println("the arc weight is null");
    }

return readyToFire;

}







    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////



    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}




