/* Extract maximum element from an array.

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
*/

package ptolemy.actor.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Typeable;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// MaximumElement
/**
Extract the maximum element from an array.  This actor reads an array
from the <i>input</i> port and sends the largest of its elements to the
<i>output</i> port.  The subset of elements that are checked is 
determined by the <i>indexOffset</i> parameter (or port).  All elements
of the array whose index is less than indexOffset are not included in the
comparison.  It is required that 0 &lt;= <i>indexOffset</i> &lt; <i>N</i>,
where <i>N</i> is the length of the input array, or an exception will be\
thrown by the fire() method.

@author Mark Oliver
@version $ID: MaximumElement.java,v0.1 2003/05/19
@since Ptolemy II 3.0-beta
*/

public class MaximumElement extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MaximumElement(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Create index port
        index = new TypedIOPort(this, "index", false, true);
        index.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The offset into the input array to begin searching for the 
     *  maximum element.  This is an integer that defaults to 0, and
     *  is required to be less than or equal to the length of the 
     *  input array. If the port is left unconnected, then the 
     *  parameter value will be used.
     */
    public TypedIOPort index;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one array from the input port and produce
     *  one of its elements on the output port.  If there is no token
     *  on the input, then no output is produced.
     *  @exception IllegalActionException If the <i>indexOffset</i>
     *  parameter (or port value) is out of range.
     */
    public void fire() throws IllegalActionException {
        int indexValue = 0;
        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken)input.get(0);
            ScalarToken currentMax = (ScalarToken)token.getElement(indexValue);
            ScalarToken temp = null;
            int i;
            for (i = indexValue+1; i < token.length(); i++) {
                temp = (ScalarToken)token.getElement(i);
                if (currentMax.isLessThan(temp).booleanValue() == true) {
                    indexValue = i;
                    currentMax = temp;
                }
            }
            output.send(0, currentMax);
            index.broadcast(new IntToken(indexValue));
        }
    }

    /** Return the type constraints of this actor.
     *  In this class, the constraints are that the type of the input port
     *  is an array type, and the type of the output port is no less than
     *  the type of the elements of the input array.
     *  @return A list of instances of Inequality.
     *  @see ptolemy.actor.TypedAtomicActor#typeConstraintList
     */
    public List typeConstraintList() {
        Type inputType = input.getType();
        if (inputType == BaseType.UNKNOWN) {
            input.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        } else if ( !(inputType instanceof ArrayType)) {
            throw new IllegalStateException("ArrayElement.typeConstraintList: "
                    + "The input type, " + inputType.toString() + " is not an "
                    + "array type.");
        }

        // NOTE: superclass will put in type constraints for
        // the input and output, so we can't invoke the superclass.
        List result = new LinkedList();

        // collect constraints from contained Typeables
        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            Typeable port = (Typeable)ports.next();
            result.addAll(port.typeConstraintList());
        }

        Iterator typeables = attributeList(Typeable.class).iterator();
        while (typeables.hasNext()) {
            Typeable typeable = (Typeable)typeables.next();
            result.addAll(typeable.typeConstraintList());
        }

        // Add type constraint for the input.
        ArrayType inputArrayType = (ArrayType)input.getType();
        InequalityTerm elementTerm = inputArrayType.getElementTypeTerm();
        Inequality inequality = new Inequality(elementTerm,
                output.getTypeTerm());

        result.add(inequality);
        return result;
    }
}