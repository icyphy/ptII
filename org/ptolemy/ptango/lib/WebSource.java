/* An actor that represents a placeholder for data received from a web page.

 Copyright (c) 1998-2011 The Regents of the University of California.
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

 */
package org.ptolemy.ptango.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.SetVariable;
import ptolemy.data.BooleanToken;
import ptolemy.data.EventToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// WebSource

/** An actor that represents a placeholder for data from a web page.  Connect 
 * this actor to an input port on an HttpService actor to indicate that the 
 * HttpService actor should retrieve data from a web page for that port.
 * The HttpService will copy the data to this actor's value parameter.
 * 
 * This actor extends ptolemy.actor.lib.Const which supplies a trigger input 
 * port.  For this actor, causing it to fire through the trigger port will 
 * return whatever data (if any) has been stored by a previous HttpRequest.
 * In contrast to ptolemy.actor.lib.Const, the value parameter is not settable 
 * by the Ptolemy user.
 *
 * @author Beth Latronico
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ltrnc)
 * @Pt.AcceptedRating Red (ltrnc)
 * @see org.ptolemy.ptango.HttpService
 * @see org.ptolemy.ptango.HttpCompositeServiceProvider
 */
public class WebSource extends SetVariable {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public WebSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
    
        //value.setExpression("1.0");
        //value.setVisibility(Settable.NOT_EDITABLE);
        
        event = new TypedIOPort(this, "event", false, true);
        event.setTypeEquals(BaseType.EVENT);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A token is sent on this output port once per requestFireNow() request.
     */
    public TypedIOPort event = null;
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** In addition to the data token on the "output" port, send a token
     * on the "event" port if requestFireNow() has been called since the 
     * last firing.
     */
    
    public void fire() throws IllegalActionException {
        
        // The HttpService calls setValue() on the WebSource
        // Always send the value regardless of whether delay is true or false
        Attribute variable = getModifiedVariable();
        if (variable instanceof Variable) {
            Token token = ((Variable) variable).getToken();
            output.send(0, token);
        }
       
        if (_generateEvent) {
            event.send(0, new EventToken());
        }
    }
    
    
    /** Request that this actor be fired.  Used by an HttpService to indicate
     *  that data has arrived.  Returns true if the WebSource is able to 
     *  request firing; false otherwise.
     *  
     *  Note - Originally I tried to call 
     *  getDirector().fireAtCurrentTime(WebSource actor) with the WebSource
     *  actor passed in as a parameter directly in the HttpService class, 
     *  but experienced a problem with the PeriodicDirectorHelper, line 93
     *   Actor container = (Actor) _director.getContainer();
     *  This code got the container of the HttpService and not the WebSource,
     *  so it didn't fire the WebSource properly.  
     *  
     * @return True if the WebSource is able to request firing; false otherwise
     */
    
    public boolean requestFiringNow() {
        try {
            _generateEvent = true;
            getDirector().fireAtCurrentTime(this);
        } catch(IllegalActionException e){
            _generateEvent = false;
            return false;
        }
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the value of the associated container's variable.
     *  @param value The new value.
     */
    public void setValue(Token value) throws IllegalActionException {
        ((Variable) getModifiedVariable()).setToken(value);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    
    /** Set to true if an event should be generated at the next fire().
     */
    // FIXME: It's possible that multiple events are received before the 
    // next fire() - what should we do then?  Could make this an integer
    // but we also need a queue to store the data values.  The queue 
    // probably needs to be a bounded queue.
    boolean _generateEvent;
}
