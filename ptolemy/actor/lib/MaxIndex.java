/* A polymorphic comparator.

 Copyright (c) 2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// MaxIndex
/**
A polymorphic comparator.
This actor has one input port, which is multiport,
and one output port, which is not.
The types on the ports are undeclared and will be resolved by
the type resolution mechanism. The tokens on each port will be
compared and an IntToken corresponding to the index of the largest
one will be output. Comparison is done by converting all input Tokens
to doubles and comparing the doubles. Therefore, all input Tokens
must be convertible to DoubleToken.

@author Jeff Tsay
@version $Id$
*/

public class MaxIndex extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MaxIndex(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeAtMost(BaseType.DOUBLE);
         input.setMultiport(true);

        // This could change if we add ShortTokens or ByteTokens
        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
        throws CloneNotSupportedException {
        MaxIndex newobj = (MaxIndex)super.clone(ws);
        return newobj;
    }

    /** If there is at least one token on the input ports, compare the
     *  tokens by converting them into doubles, and output the index of
     *  the port that has the Token with the largest double value.
     *  At most one token is read from each channel, so if more than one
     *  token is pending, the rest are left for future firings.
     *  If none of the input channels has a token, do nothing.
     *
     *  @exception IllegalActionException If there is no director,
     *  or if conversion to DoubleToken is not supported by the input tokens.
     */
    public void fire() throws IllegalActionException {
       IntToken t = null;
       double maxValue = Double.NEGATIVE_INFINITY;
       int maxIndex = -1;
       boolean foundFirst = false;
       for (int i = 0; i < input.getWidth(); i++) {
           if (input.hasToken(i)) {
              double val = ((ScalarToken) input.get(i)).doubleValue();
              if (foundFirst) {
                 if (maxValue < val) {
                    maxValue = val;
                    maxIndex = i;
                 }
              } else {
                 maxValue = val;
                 maxIndex = i;
                 foundFirst = true;
              }
           }
       }

       if (foundFirst) {
           output.send(0, new IntToken(maxIndex));
       }
    }
}
