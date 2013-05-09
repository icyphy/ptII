/* This actor implements an output port in a composite resource scheduler.

@Copyright (c) 2011-2013 The Regents of the University of California.
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
package ptolemy.actor.lib.resourceScheduler;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.SetVariable;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/** This actor implements an output port in a composite resource scheduler
 *  (@link CompositeResourceScheduler).
*
*  <p>
*  This composite contains a SetVariable actor which stores values in a 
*  local variable. The CompositeResourceScheduler will check the value of 
*  this parameter. A BooleanToken with value true means that the actor mapped
*  to this port has been scheduled and is ready to be fired. The 
*  CompositeResourceScheduler resets the value to false and notify 
*  the functional model that the associated actor is ready to fire.
*
*  @author Patricia Derler
*  @version $Id$
*  @since Ptolemy II 8.0
*  @Pt.ProposedRating Yellow (derler)
*  @Pt.AcceptedRating Red (derler)
*/
public class ResourceMappingOutputPort extends TypedCompositeActor {

    /** Construct a CQMOutputPort. The contained entities (SetVariable,
     *  Parameter and input port) are created from the XML description
     *  in the library.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ResourceMappingOutputPort(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    } 
    
    /** Initialize actor and clear Parameter value in case it was set
     *  in a previous execution.
     */
    @Override
    public void initialize() throws IllegalActionException { 
        super.initialize();
        
        ((Parameter)getAttribute("Parameter")).setExpression("");
    }
    
    /** Check whether the contained parameter contains a token.
     * @return True if the contained parameter contains a token.
     * @exception IllegalActionException Thrown if token cannot
     * be accessed. 
     */
    public boolean hasToken() throws IllegalActionException {
        Token token = ((Parameter)getAttribute("Parameter")).getToken();
        if (token != null && 
                !(token instanceof BooleanToken)) {
            return true;
        }
        return false;
    }
    
    /** Get token from parameter and remove it from the parameter.
     * @return The token.
     * @exception IllegalActionException Thrown if token cannot
     * be accessed. 
     */
    public Token takeToken() throws IllegalActionException {
        Token token = ((Parameter)getAttribute("Parameter")).getToken();
        ((Parameter)getAttribute("Parameter")).setToken(new BooleanToken(false));
        return token;
    }
    
    /** Get the token from parameter but do not remove it.
     *  @return The token.
     *  @throws IllegalActionException If token cannot be accessed.
     */
    public Token getToken() throws IllegalActionException {
        Token token = ((Parameter)getAttribute("Parameter")).getToken(); 
        return token;
    }
    
    /** Clear parameter value such that it is not saved to the moml xml
     *  description or used in another execution.
     */
    public void wrapup() throws IllegalActionException {
        // TODO Auto-generated method stub
        super.wrapup();
        
        ((Parameter)getAttribute("Parameter")).setExpression("");
    }
    
}
