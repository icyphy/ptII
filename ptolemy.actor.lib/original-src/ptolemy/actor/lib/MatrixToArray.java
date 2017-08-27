/* An actor that disassemble an ArrayToken to a multiport output.

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
package ptolemy.actor.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MatrixToArray

/**
 Convert a matrix to an array. The array will contain the first
 row, followed by the second, etc.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 */
public class MatrixToArray extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be
     *   contained by the proposed container.
     *  @exception NameDuplicationException If the container
     *   already has an actor with this name.
     */
    public MatrixToArray(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set type constraints.
        input.setTypeAtMost(BaseType.MATRIX);
        output.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);

        columnMajor = new Parameter(this, "columnMajor");
        columnMajor.setTypeEquals(BaseType.BOOLEAN);
        columnMajor.setExpression("false");

        // Set the icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If true, then insert the first column into the array first,
     *  followed by the second column, etc. If false, then insert the
     *  first row first, followed by the second, etc.
     *  This is a boolean that defaults to false.
     */
    public Parameter columnMajor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class
     *   contains an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MatrixToArray newObject = (MatrixToArray) super.clone(workspace);
        newObject.input.setTypeAtMost(BaseType.MATRIX);
        newObject.output.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        return newObject;
    }

    /** If there is a token at the input, read the array
     *  from the input port, and construct and send to the
     *  output a matrix containing the values from the array.
     *  @exception IllegalActionException If a runtime
     *   type conflict occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            Token token = input.get(0);
            if (!(token instanceof MatrixToken)) {
                // Not a supported type.
                throw new IllegalActionException(this,
                        "Input is not a matrix: " + token);
            }
            if (((BooleanToken) columnMajor.getToken()).booleanValue()) {
                output.broadcast(((MatrixToken) token).toArrayColumnMajor());
            } else {
                output.broadcast(((MatrixToken) token).toArray());
            }
        }
    }
}
