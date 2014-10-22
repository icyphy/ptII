/* An actor that outputs strings read from a text file or URL.

 @Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.string;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// StringSplit

/**
 This actor reads an input string and splits it into an array of
 strings. The <i>separator</i> parameter is a regular expression
 that determines where the split should occur. The default behavior
 will split the input at newline characters.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class StringSplit extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringSplit(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        separator = new StringParameter(this, "separator");
        separator.setExpression("\n");

        trimSpaces = new Parameter(this, "trimSpaces");
        trimSpaces.setTypeEquals(BaseType.BOOLEAN);
        trimSpaces.setExpression("true");

        input.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(new ArrayType(BaseType.STRING));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A specification of the separator used to split the string.
     *  The default is "\n", which results in splitting the string
     *  at newline characters.
     */
    public StringParameter separator;

    /** If true, then trim spaces around each resulting string.
     *  This is a boolean that defaults to true.
     */
    public Parameter trimSpaces;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Split the input string and send to the output.
     *  If there is no input, do nothing.
     *  @exception IllegalActionException If there's no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (input.hasToken(0)) {
            String inputValue = ((StringToken) input.get(0)).stringValue();
            String[] result = inputValue.split(separator.stringValue());
            Token[] resultTokens = new Token[result.length];
            for (int i = 0; i < result.length; i++) {
                resultTokens[i] = new StringToken(result[i]);
            }
            output.broadcast(new ArrayToken(resultTokens));
        }
    }
}
