/* An actor that reads a token from each input channel to
   assemble an ArrayToken.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (zhouye@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ElementsToArray
/**
On each firing, this actor reads exactly one token from each channel
of the input port and assemble the tokens into an ArrayToken. The
ArrayToken is sent to the output port. If there is no input token
at any channel of the input port, the prefire() will return false.
<p>
This actor is polymorphic. It can accept inputs of any type, as long
as the type does not change, and will produce an array with elements
of the corresponding type.
<p>

@author Rachel Zhou
@version $Id$
@since Ptolemy II 3.1
*/

public class ElementsToArray extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be
     *   contained by the proposed container.
     *  @exception NameDuplicationException If the container
     *   already has an actor with this name.
     */
    public ElementsToArray(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        
        // set the output type to be an ArrayType.
        output.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        input.setMultiport(true);
        
        // Set the icon.
        _attachText("_iconDescription", "<svg>\n" +
            "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
            + "style=\"fill:white\"/>\n" +
            "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume one token from each channel of the input port,
     *  assemble those tokens into an ArrayToken, and send the
     *  result to the output.
     *  @exception IllegalActionException If not enough tokens
     *   are available.
     */
    public void fire() throws IllegalActionException {
        int size = input.getWidth();
        Token[] valueArray = new Token[size];
        for (int i = 0; i < size; i++)
            valueArray[i] = input.get(i);

        output.send(0, new ArrayToken(valueArray));
    }

    /** Return true if all channels of the <i>input</i> port have
     *  tokens, false if any channel does not have a token.
     *  @return boolean True if all channels of the input port
     *   have tokens.
     *  @exception IllegalActionException If the hasToken() query
     *   to the input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int)
     */
    public boolean prefire() throws IllegalActionException {
        for (int i = 0; i < input.getWidth(); i++) {
            if (!input.hasToken(i)) {
                return false;
            }
        }
        return true;
    }

    /** Return the type constraint that the type of the elements of
     *  the output array is no less than the type of the input port.
     *  @return A list of inequalities.
     */
    public List typeConstraintList() {
        ArrayType outArrType = (ArrayType)output.getType();
        InequalityTerm elementTerm = outArrType.getElementTypeTerm();
        Inequality ineq = new Inequality(input.getTypeTerm(), elementTerm);

        List result = new LinkedList();
        result.add(ineq);
        return result;
    }
}
