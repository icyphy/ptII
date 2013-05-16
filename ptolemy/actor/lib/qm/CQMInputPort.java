/* This actor implements an input port in a composite quantity manager.

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

import ptolemy.actor.lib.Const;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;


/** This actor implements an input port in a composite quantity manager
 *  (@link CompositeQM).
*
*  <p>
*  Tokens received are RecordTokens containing two fields: receiver
*  and token.
*
*  @author Patricia Derler
*  @version $Id$
*  @since Ptolemy II 8.0
*  @Pt.ProposedRating Yellow (derler)
*  @Pt.AcceptedRating Red (derler)
*/
public class CQMInputPort extends Const {
    
    /** Construct a constant source with the default type set to the RecordToken
     *  used in the CompositeQM. 
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CQMInputPort(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        Parameter hide = (Parameter) trigger.getAttribute("_hide");
        if (hide == null) {
            hide = new Parameter(trigger, "_hide", new BooleanToken(true));
        } else {
            hide.setToken(new BooleanToken(true));
        }
        _beforeInitialization = true; 
        value.setExpression("{receiver=object, token=general}");
        value.setVisibility(Settable.NONE);
        firingCountLimit.setVisibility(Settable.NONE);
        output.setTypeEquals(new RecordType(
                new String[]{"receiver", "token"}, 
                new Type[]{BaseType.OBJECT, BaseType.GENERAL}));
    }
    
    
    
    /** Do not set a value before initialization.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown here.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException { 
        if (attribute == value && _beforeInitialization) { 
            // Ignore. When saving a model, the constant might contain
            // a concrete RecordToken with a DEReceiver and a Token. 
            // When loading this model again after saving, the DEReceiver
            // might not have been instantiated and therefore creating
            // the RecordToken will cause errors. 
            value.setExpression("{receiver=object, token=general}");
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Initialize the iteration counter.  A derived class must call
     *  this method in its initialize() method or the <i>firingCountLimit</i>
     *  feature will not work.
     *  @exception IllegalActionException If the parent class throws it,
     *   which could occur if, for example, the director will not accept
     *   sequence actors.
     */
    public void initialize() throws IllegalActionException { 
        super.initialize(); 
        value.setExpression("{receiver=object, token=general}");
        _beforeInitialization = false;
    }
    
    private boolean _beforeInitialization;
}
