/* Petri net director.

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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.petrinet.kernel;

import ptolemy.actor.lib.Transformer; 
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.domains.petrinet.kernel.Place;

import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;


//import ptolemy.graph.*;
//import ptolemy.kernel.*;
//import ptolemy.kernel.util.*;
//import ptolemy.data.*;

import java.util.Iterator;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// PetriNetDirector
/**
Petri net director.

@author  Yuke Wang and Edward A. Lee
@version $Id$
*/
public class PetriNetDirector extends Director {

    /** Construct a new Petri net director.
     *  @param container The container.
     *  @param name The name of the director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PetriNetDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of PetriNetReceiver.
     *  @return A new PetriNetReceiver.
     */
    public Receiver newReceiver() {
        return new PetriNetReceiver();
    }


    // we will have three kinds of fire, fire one transition,
    // fire one round transitions, and fire till no more
    // transitions can fire. The director should have all the
    // three parameters to choose. The implementation details
    // are minor differences. We have implemented two methods
    // the fire one transition, and fire one round transition.
    // we have not implemented the fire all transitions till
    // no more transitions to fire yet.

    //  
    // second problem is to choose from all the ready transition which one
    // to fire, and when it fires, it changes the state, and
    // we again to choose from many of the ready states
    // until no more transitions can fire.
    // the current method just fire the transition sequentially.



    public void fire() throws IllegalActionException {


        Nameable container = getContainer();
        if (container instanceof NamedObj) 
            System.out.println("firing, the top level in the director container is" + container.getFullName());
  
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
     
            while (actors.hasNext()) {
                Transformer actor = (Transformer) actors.next();

                if (actor instanceof Transition)  {
                    System.out.println("this is " + actor.getName()
                            + " ************************************************************** " );               
                    Transition transition = (Transition) actor;
                    if (transition.prefire()) {
                        System.out.println("ready to fire transition********");     
                        transition.fire();
                    }
                    else 
                        System.out.println("not ready to fire transition********");
                }

            }
        }
    }

 
    // this method is about the same as the above fire method, except that
    // it returns after one fire.
    // we can further extend this to make it fire a specific transition.


    public void fireOnce() throws IllegalActionException {


        Nameable container = getContainer();
        if (container instanceof NamedObj) 
            System.out.println("firing, the top level in the director container is" + container.getFullName());
  
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
     
            while (actors.hasNext()) {
                Transformer actor = (Transformer) actors.next();

                if (actor instanceof Transition)  {

                    System.out.println("this is " + actor.getName()
                            + " ************************************************************** " );
                
                    Transition transition = (Transition) actor;
                    if (transition.prefire()) {
                        System.out.println("ready to fire transition********");     
                        transition.fire();
                        return;          
                    }
                    else 
                        System.out.println("not ready to fire transition********");
                }

            }
        }
    }

   




    ///////////////////////////////////////////////////////////////////
    ////                      

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


     
}
