/* This actor implements an output port in a composite quantity manager.

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
package ptolemy.actor.lib.qm;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/** This actor implements an output port in a composite quantity manager
 *  (@link CompositeQM).
*
*  <p>
*  This composite contains a SetVariable actor which stores values in a 
*  local variable. The CompositeQM will check this parameter and take
*  it out of the variable in the fire.
*
*  @author Patricia Derler
*  @version $Id$
*  @since Ptolemy II 8.0
*  @Pt.ProposedRating Yellow (derler)
*  @Pt.AcceptedRating Red (derler)
*/
public class CQMOutputPort extends TypedCompositeActor {

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
    public CQMOutputPort(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    } 
    
    /** Check whether the contained parameter contains a token.
     * @return True if the contained parameter contains a token.
     * @exception IllegalActionException Thrown if token cannot
     * be accessed. 
     */
    public boolean hasToken() throws IllegalActionException {
        return ((Parameter)getAttribute("Parameter")).getToken() != null;
    }
    
    /** Get token from parameter and remove it from the parameter.
     * @return The token.
     * @exception IllegalActionException Thrown if token cannot
     * be accessed. 
     */
    public Token takeToken() throws IllegalActionException {
        Token token = ((Parameter)getAttribute("Parameter")).getToken();
        ((Parameter)getAttribute("Parameter")).reset();
        return token;
    }
    
}
