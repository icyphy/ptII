/* A subscriber class to the Java Spaces.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jspaces;

import ptolemy.actor.lib.Source;
import ptolemy.data.expr.Parameter;
import ptolemy.data.*;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.*;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// Subscriber
/**
A subscriber to the Java Spaces. This actor read a 

@author Jie Liu, Yuhong Xiong
@version $Id$
*/

public class Subscriber extends Source {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Subscriber(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    	jspaceName = new Parameter(this, "jspaceName", 
                new StringToken("JaveSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);
        entryName = new Parameter(this, "entryName", 
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);
        minSerialNumber = new Parameter(this, "minSerialNumber", 
                new LongToken(0));
        minSerialNumber.setTypeEquals(BaseType.LONG);
        notify = new Parameter(this, "notify", 
                new BooleanToken(false));
        notify.setTypeEquals(BaseType.BOOLEAN);
        blocking = new Parameter(this, "blocking", 
                new BooleanToken(false));
        blocking.setTypeEquals(BaseType.BOOLEAN);
        defaultToken = new Parameter(this, "defaultToken", 
                new DoubleToken(0.0));
        defaultToken.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The Java Space name. The default name is "JavaSpaces" of 
     *  type StringToken.
     */
    public Parameter jspaceName;

    /** The name for the subcribed entry. The default value is
     *  an empty string of type StringToken.
     */
    public Parameter entryName;

    /** The minimum serial number of the subsribed entry. All
     *  entries retrieved by this actor will have serial numbers
     *  no less than this number. The default value is 0 of 
     *  type LongToken. The serial number is usually greater
     *  than or equal to 0. If this number is negative, the 
     *  subscriber always get the entry with the largest 
     *  serial number (i.e. the latest entry).
     */  
    public Parameter minSerialNumber;

    /** A false value indicate the entry be read from the space upon
     *  the fire() method. Otherwise set itself up to be notified 
     *  by the space. The default value is false, of type BooleanToken.
     */
    public Parameter notify;
 
    /** Indicate whether the actor blocks when it can not read
     *  an entry from the space. The default value is false of
     *  type BooleanToken.
     */
    public Parameter blocking;
   
    /** The default initial token. If the actor is nonblocking
     *  and there is no matching entry in the space, then this
     *  token will be output. Default value is 0.0 of type 
     *  DoubleToken. The token and the type will be override
     *  by the first entry read from the space.
     */
    public Parameter defaultToken;
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>output</code>
     *  variable to equal the new port.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
	try {
	    Subscriber newobj = (Subscriber)super.clone(ws);
	    newobj.jspaceName = (Parameter)newobj.getAttribute("jspaceName");
            newobj.entryName = (Parameter)newobj.getAttribute("entryName");
            newobj.minSerialNumber = 
                (Parameter)newobj.getAttribute("minSerialNumber");
            newobj.notify = (Parameter)newobj.getAttribute("notify");
            newobj.blocking = (Parameter)newobj.getAttribute("blocking");
            newobj.defaultToken = 
                (Parameter)newobj.getAttribute("defaultToken");
	    return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Find the JavaSpaces and retrieve the first token. The type of
     *  the output is infered from the type of the token
     */
    public void preinitialize() throws IllegalActionException {
    }

    

    /** Read at most one input token from each channel of the trigger
     *  input and discard it.  If the trigger input is not connected,
     *  then this method does nothing.  Derived classes should be
     *  sure to call super.fire(), or to consume the trigger input
     *  tokens themselves, so that they aren't left unconsumed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private long _currentSerialNumber;
    
}
