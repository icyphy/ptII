/* Output a substring of the string provided at the input.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.string;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// StringSubstring

/**
 Output a substring of the string provided at the input.  The position of the
 substring within the input string is determined by the <i>start</i> and
 <i>stop</i> port parameters. Following Java convention, the character at
 <i>start</i> is included, but the character at <i>stop</i> is not.
 If the <i>stop</i> is less than <i>start</i>, then
 the substring starts at <i>start</i> and extends to the end of the
 string. The default values for <i>start</i> and <i>stop</i> are
 both 0; this results in an empty string at the output.

 @author Neil E. Turner and Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (net)
 @Pt.AcceptedRating Green (net)
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
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create new parameters and ports.
        // Set default values of the parameters and type constraints.
        start = new PortParameter(this, "start");
        start.setExpression("0");
        start.setTypeEquals(BaseType.INT);
        new SingletonParameter(start.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        stop = new PortParameter(this, "stop");
        stop.setExpression("0");
        stop.setTypeEquals(BaseType.INT);
        new SingletonParameter(stop.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        input.setTypeEquals(BaseType.STRING);
        new SingletonParameter(input, "_showName").setToken(BooleanToken.TRUE);

        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The beginning index of the input string, which is the position of
     *  first character of the desired substring.  Its default value is 0,
     *  type int.
     */
    public PortParameter start;

    /** The ending index of the input string, which is 1 greater than the
     *  position of last letter of the desired substring.  Its default value
     *  is 0, type int.
     */
    public PortParameter stop;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is an input string, find a substring according to the indices
     *  given by the port parameters <i>start</i> and <i>stop</i>
     *  and produce the substring at the output.  If the <i>stop</i>
     *  is -1 , then the substring starts at <i>start</i> and extends to
     *  the end of the string.  In the event that the indices do not exist in
     *  the input string, throw IndexOutOfBoundsException.
     *  @exception IllegalActionException If the superclass throws it, or
     *   if it is thrown reading the input port or writing to the output port.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        start.update();
        stop.update();

        if (input.hasToken(0)) {
            StringToken inputToken = (StringToken) input.get(0);
            String value = inputToken.stringValue();
            int startValue = ((IntToken) start.getToken()).intValue();
            int stopValue = ((IntToken) stop.getToken()).intValue();
            String substringValue;

            
            if (stopValue < startValue) {
                if (value.length() < startValue) {
                    throw new IllegalActionException(this, 
                            "Cannot compute substring of "
                            + "\"" + value + "\""
                            + " starting at "
                            + startValue);
                }
                substringValue = value.substring(startValue);
            } else {
                if (value.length() < stopValue ||
                        startValue < 0) {
                    throw new IllegalActionException(this, 
                            "Cannot compute substring of "
                            + "\"" + value + "\" between "
                            + startValue + " and "
                            + stopValue);
                }
                substringValue = value.substring(startValue, stopValue);
            }

            output.send(0, new StringToken(substringValue));
        }
    }
}
