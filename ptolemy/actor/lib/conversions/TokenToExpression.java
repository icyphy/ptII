/* An actor that converts tokens into expressions.

 @Copyright (c) 1998-2010 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.conversions;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TokenToExpression

/**
 This actor reads a token from the input port and outputs a string token
 whose value is an expression that can be parsed to yield the input token.
 For example, if the input is itself a string token, the output will be a
 new string token whose value is the value of the input string token surrounded
 by double quotation marks. The input data type is undeclared, so this actor
 can accept any input. If the input known to be absent, this actor outputs
 a string "absent".

 @author  Steve Neuendorffer, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (liuj)
 */
public class TokenToExpression extends Converter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TokenToExpression(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.UNKNOWN);
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output a string token whose value is an expression representing
     *  the value of the input token. If the input known to be absent,
     *  this actor outputs a string "absent".
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            String string = input.get(0).toString();
            output.broadcast(new StringToken(string));
        } else {
            output.broadcast(new StringToken("absent"));
        }
    }
    
    /** Override the base class to declare the input type to be
     *  general if backward type inference is enabled. This will
     *  result in upstream ports resolving to the most general
     *  type rather than the most specific. We don't want the input
     *  type to always be general because code generation works
     *  much better with the most specific types rather than the
     *  most general.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void preinitialize() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor)getContainer();
        if (container.isBackwardTypeInferenceEnabled()) {
            input.setTypeEquals(BaseType.GENERAL);
        }
        super.preinitialize();
    }
}
