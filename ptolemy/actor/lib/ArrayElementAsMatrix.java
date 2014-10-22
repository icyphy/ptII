/* Extract an element from an array by accessing the array as a matrix.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayElementAsMatrix

/**
 Extract an element from an array by accessing the array as a matrix.

 This actor reads an array from the
 <i>input</i> port and sends one of its elements to the <i>output</i>
 port.  The element that is extracted is determined by the
 <i>x</i>, <i>y</i>, <i>xOffset</i> and <i>yOffset</i>  parameters (or port).
 It is required that
 <pre>
 0 &lt;= <i>x</i> * <i>column</i> + <i>y</i> * <i>yOffset</i>  &lt; <i>N</i>
 </pre>
 where <i>N</i> is the length of the input array, or an exception will
 be thrown by the fire() method.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayElementAsMatrix extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayElementAsMatrix(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // set type constraints.
        output.setTypeAtLeast(ArrayType.elementType(input));

        // Set parameters.
        xOffset = new PortParameter(this, "xOffset");
        xOffset.setTypeEquals(BaseType.INT);
        xOffset.setExpression("1");
        new Parameter(xOffset.getPort(), "_showName", BooleanToken.TRUE);
        new StringAttribute(xOffset.getPort(), "_cardinal")
                .setExpression("SOUTH");

        yOffset = new PortParameter(this, "yOffset");
        yOffset.setTypeEquals(BaseType.INT);
        yOffset.setExpression("1");
        new Parameter(yOffset.getPort(), "_showName", BooleanToken.TRUE);
        new StringAttribute(yOffset.getPort(), "_cardinal")
                .setExpression("SOUTH");

        x = new PortParameter(this, "x");
        x.setTypeEquals(BaseType.INT);
        x.setExpression("0");
        new Parameter(x.getPort(), "_showName", BooleanToken.TRUE);
        new StringAttribute(x.getPort(), "_cardinal").setExpression("SOUTH");

        y = new PortParameter(this, "y");
        y.setTypeEquals(BaseType.INT);
        y.setExpression("0");
        new Parameter(y.getPort(), "_showName", BooleanToken.TRUE);
        new StringAttribute(y.getPort(), "_cardinal").setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The xOffset offset into the input array.  This is an integer that
     *  defaults to 1. If the port is left unconnected, then the
     *  parameter value will be used.
     */
    public PortParameter xOffset;

    /** The yOffset offset into the input array.  This is an integer that
     *  defaults to 1. If the port is left unconnected, then the
     *  parameter value will be used.
     */
    public PortParameter yOffset;

    /** The x index into the input array.  This is an integer that
     *  defaults to 0. If the port is left unconnected, then the
     *  parameter value will be used.
     */
    public PortParameter x;

    /** The y index into the input array.  This is an integer that
     *  defaults to 0. If the port is left unconnected, then the
     *  parameter value will be used.
     */
    public PortParameter y;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new instance of ArrayElement.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayElementAsMatrix newObject = (ArrayElementAsMatrix) super
                .clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.input));
        } catch (IllegalActionException e) {
            // Should have been caught before.
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Consume at most one array from the input port and produce
     *  one of its elements on the output port.  If there is no token
     *  on the input, then no output is produced.
     *  @exception IllegalActionException If the <i>index</i> parameter
     *   (or port value) is out of range.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // NOTE: This has be outside the if because we need to ensure
        // that if an index token is provided that it is consumed even
        // if there is no input token.
        xOffset.update();
        yOffset.update();
        x.update();
        y.update();

        int xOffsetValue = ((IntToken) xOffset.getToken()).intValue();
        int yOffsetValue = ((IntToken) yOffset.getToken()).intValue();
        int xValue = ((IntToken) x.getToken()).intValue();
        int yValue = ((IntToken) y.getToken()).intValue();

        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken) input.get(0);

            if (xOffsetValue < 0
                    || yOffsetValue < 0
                    || xValue < 0
                    || yValue < 0
                    || xValue * xOffsetValue + yValue * yOffsetValue >= token
                            .length()) {
                throw new IllegalActionException(this, "xValue (" + xValue
                        + ") * xOffsetValue *(" + xOffsetValue
                        + " ) + yValue (" + yValue + ") * yOffsetValue ("
                        + yOffsetValue + ") = " + xValue * xOffsetValue
                        + yValue * yOffsetValue
                        + " is less than zero or otherwise "
                        + "out of range for the input "
                        + "array, which has length " + token.length());
            }

            output.send(
                    0,
                    token.getElement(xValue * xOffsetValue + yValue
                            * yOffsetValue));
        }
    }
}
