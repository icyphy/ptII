/* Interface for actors.

 Copyright (c) 1997- The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/

package pt.actor;

import java.util.Enumeration;
import pt.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Actor
/**
An Actor is an executable entity. This interface defines the common
functionality in AtomicActor and CompositeActor.

@author Mudit Goel, Edward A. Lee
@version $Id$
@see pt.actors.CompositeActor
@see pt.actors.IOPort
*/
public interface Actor extends Executable {

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Return the director responsible for the execution of this actor, or
     *  null if there is none.
     *  @return The director that invokes this actor.
     */
    public Director getDirector();

    /** Return an enumeration of the input ports.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration inputPorts();

    /** Return a new receiver of a type compatible with the director.
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException;

    /** Return an enumeration of the output ports.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration outputPorts();
}




