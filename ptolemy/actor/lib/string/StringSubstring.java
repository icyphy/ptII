/* Output a substring of the string provided at the input.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (net@eecs.berkeley.edu)
@AcceptedRating Green (net@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.string;


import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// StringSubstring
/**
Output a substring of the string provided at the input.  The position of the
substring within the input string is determined by the <i>startIndex</i> and
<i>stopIndex</i> port parameters. Following Java convention, the character at
<i>startIndex</i> is included, but the character at <i>stopIndex</i> is not.
If the <i>stopIndex</i> is less than <i>startIndex</i>, then
the substring starts at <i>startIndex</i> and extends to the end of the
string. The default values for <i>startIndex</i> and <i>stopIndex</i> are
both 0; this results in an empty string at the output.

@author Neil E. Turner and Edward A. Lee
@version $Id$
*/

public class StringSubstring extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringSubstring(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Create new parameters and ports.
        // Set default values of the parameters and type constraints.
        startIndex = new PortParameter(this, "startIndex");
        startIndex.setExpression("0");
        startIndex.setTypeEquals(BaseType.INT);

        stopIndex = new PortParameter(this, "stopIndex");
        stopIndex.setExpression("0");
        stopIndex.setTypeEquals(BaseType.INT);

        input.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The beginning index of the input string, which is the position of
     *  first character of the desired substring.  Its default value is 0,
     *  type int.
     */
    public PortParameter startIndex;

    /** The ending index of the input string, which is 1 greater than the
     *  position of last letter of the desired substring.  Its default value
     *  is 0, type int.
     */
    public PortParameter stopIndex;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is an input string, find a substring according to the indices
     *  given by the port parameters <i>startIndex</i> and <i>stopIndex</i>
     *  and produce the substring at the output.  If the <i>stopIndex</i>
     *  is -1 , then the substring starts at <i>startIndex</i> and extends to
     *  the end of the string.  In the event that the indices do not exist in
     *  the input string, throw IndexOutOfBoundsException.
     *  @exception IllegalActionException If the superclass throws it, or
     *   if it is thrown reading the input port or writing to the output port.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        startIndex.update();
        stopIndex.update();
        if (input.hasToken(0)) {
            StringToken inputToken = (StringToken)input.get(0);
            String value = inputToken.stringValue();
            int startIndexValue = ((IntToken)startIndex.getToken()).intValue();
            int stopIndexValue = ((IntToken)stopIndex.getToken()).intValue();
            String substringValue;
            if (stopIndexValue == -1) {
                substringValue = value.substring(startIndexValue);
            } else {
                substringValue = value.substring(startIndexValue,
                        stopIndexValue);
            }
            output.send(0, new StringToken(substringValue));
        }
    }
}
