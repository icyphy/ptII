/* Error handling action for timing errors on Ptides Ports.

@Copyright (c) 2008-2012 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.lib;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.lib.SetVariable;
import ptolemy.actor.util.Time;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** Error handling action for timing errors on Ptides Ports.
 * 
 *  This actor extends the SetVariable actor with a set of
 *  string values that represent error handling actions. E.g.
 *  drop event, execute event, fix timestamp, ...
 *  
 *  Upon choosing one specific error handling action, the 
 *  string value is set as the variableName and, if it does
 *  not exist already, a parameter with that name is added
 *  to the container. 
 *  
 *  This actor is used in ErrorHandler actors which are 
 *  CompositeActors with the name ErrorHandler. 
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class ErrorHandlingAction extends SetVariable { 

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     *  @exception NameDuplicationException Thrown if the name is already used.
     *  @exception IllegalActionException Thrown if parameters cannot be created.
     */
    public ErrorHandlingAction(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct an ErrorHandlingAction in the container with 
     *  a specified name.
     *  @param container The container for this actor.
     *  @param name The name of this actor.
     *  @exception NameDuplicationException Thrown if the name is already used.
     *  @exception IllegalActionException Thrown if parameters cannot be created.
     */
    public ErrorHandlingAction(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        _init();
    }

    /** The error handling action. This is a string parameter with
     *  several choices. See ErrorHandlingActionString.
     */
    public Parameter action;
    
    /** Drop the event.
     */
    public static String DropEvent = "dropEvent";
    
    /** Execute the event unmodified.
     */
    public static String ExecuteEvent = "executeEvent";
    
    /** Execute the event but fix the timestamp to a valid timestamp.
     */
    public static String FixTimestamp = "fixTimestamp";
    
    /** Clear all events in the event queue.
     */
    public static String ClearAllEvents = "clearAllEvents";
    
    /** Clear all events in the event queue with earlier timestamps.
     */
    public static String ClearEarlierEvents = "clearEarlierEvents";
    
    /** Clear all events that should have taken the new event into 
     *  account.
     */
    public static String ClearCorruptEvents = "clearCorruptEvents";
    
    /** The possible error handling action strings.
     */
    public static enum ErrorHandlingActionString{
        DropEvent, ExecuteEvent, FixTimestamp, ClearAllEvents, ClearEarlierEvents, ClearCorruptEvents
    }
    
    /** Upon choosing an error handling action, set the variableName
     *  of this actor and make sure the corresponding parameter is
     *  in the container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == action) {
            String string = ((StringToken)action.getToken()).stringValue();
            variableName.setExpression(string);
            
            if (getContainer() instanceof CompositeActor) {
                CompositeActor container = (CompositeActor)getContainer();
                while (!container.getName().equals("ErrorHandler")) {
                    if (container == container.toplevel()) { 
                        return;
                        // do nothing
                    }
                    container = (CompositeActor)container.getContainer();
                }
                if (container.getAttribute(string) == null) {
                    try { 
                        Parameter parameter = new Parameter(container, string);
                        parameter.setExpression("false"); 
                    } catch (NameDuplicationException e) {
                        // caught by checking earlier, cannot
                        // get here.
                    }
                    
                }
            } // else it is in the library.
        }
        super.attributeChanged(attribute);
    }
    
    /** clone() is not supported, call clone(Workspace workspace)
     *  instead.  Usually it is a mistake for an actor to have a
     *  clone() method and call super.clone(), instead the actor
     *  should have a clone(Workspace workspace) method and
     *  call super.clone(workspace).
     *  @exception CloneNotSupportedException Not thrown here.
     */
//    public Object clone(workspace) throws CloneNotSupportedException {
//        ErrorHandlingAction newObject = (ErrorHandlingAction) super.clone(workspace);
//        return newObject;
//    }
    
    /** Initialize parameters.
     * @exception NameDuplicationException If parameter name is already used.
     * @exception IllegalActionException If parameter cannot be created or type cannot 
     * be assigned.
     */
    private void _init() throws IllegalActionException, NameDuplicationException {
        action = new Parameter(this, "action");
        action.setTypeEquals(BaseType.STRING);
        action.addChoice("\"" + DropEvent + "\"");
        action.addChoice("\"" + ExecuteEvent + "\"");
        action.addChoice("\"" + FixTimestamp + "\"");
        action.addChoice("\"" + ClearAllEvents + "\"");
        action.addChoice("\"" + ClearEarlierEvents + "\"");
        action.addChoice("\"" + ClearCorruptEvents + "\"");
        action.setExpression("\"" + DropEvent + "\"");
        
        delayed.setExpression("false");
    }
    
}
