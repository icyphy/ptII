/* An event that represents an actor firing.

 Copyright (c) 2000 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// FiringEvent
/**
An interface for events that can be used for debugging.  These events will
generally be subclassed to create events with more meaning (such as 
a FiringEvent).

@author  Steve Neuendorffer
@version $Id$
@see DebugListener
@see FiringEvent
*/
public class FiringEvent implements DebugEvent {
    
    /** 
     * Create a new firing event with the given source, actor, and type.
     */
    public FiringEvent(Director source, Actor actor, FiringEventType type) {
	_director = source;
	_actor = actor;
	_type = type;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the actor that is being activated.
     */
    public Actor getActor() {
	return _actor;
    }

    /** 
     * Return the director that activated this event.
     */
    public NamedObj getSource() {
	return _director;
    }

    /**
     * Return the type of activation that this event represents.
     */
    public FiringEventType getType() {
	return _type;
    }

    public static class FiringEventType {
	private FiringEventType(String name) {
	    _name = name;
	}
	
	public String getName() {
	    return _name;
	}
	private String _name;
    }
    
    public static FiringEventType PREFIRE = 
	new FiringEventType("prefire");
    public static FiringEventType FIRE = 
	new FiringEventType("fire");
    public static FiringEventType POSTFIRE = 
	new FiringEventType("postfire");
    public static FiringEventType POSTPOSTFIRE = 
	new FiringEventType("postpostfire");

    private Actor _actor;
    private NamedObj _director;
    private FiringEventType _type;
}
