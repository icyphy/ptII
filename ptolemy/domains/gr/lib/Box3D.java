/* A GR Shape consisting of a polyhedral box

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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/
package ptolemy.domains.gr.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.media.j3d.Node;

import com.sun.j3d.utils.geometry.Box;

//////////////////////////////////////////////////////////////////////////
//// Box3D
/** This actor contains the geometry and appearance specifications for a GR
box.  The output port is used to connect this actor to the Java3D scene
graph. This actor will only have meaning in the GR domain.

    The parameters <i>xLength</i>, <i>yHeight</i>, and <i>zWidth</i> determine
the dimensions of box.

@author C. Fong
@version $Id$
@since Ptolemy II 1.0
*/
public class Box3D extends GRShadedShape {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Box3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        xLength = new Parameter(this, "xLength", new DoubleToken(0.5));
        yHeight = new Parameter(this, "yHeight", new DoubleToken(0.5));
        zWidth = new Parameter(this, "zWidth", new DoubleToken(0.5));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The length of the box in the x-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 0.5
     */
    public Parameter xLength;

    /** The height of the box in the y-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 0.5
     */
    public Parameter yHeight;

    /** The width of the box in the z-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 0.5
     */
    public Parameter zWidth;


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    protected void _createModel() throws IllegalActionException {
        super._createModel();
        _containedNode = new Box((float)_getLength(), (float) _getHeight(),
                (float) _getWidth(), Box.GENERATE_NORMALS, _appearance);
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a Java3D box.
     *  @return the Java3D box.
     */
    protected Node _getNodeObject() {
        return (Node) _containedNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected Box _containedNode;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the value of the height parameter
     *  @return the height of the box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getHeight() throws IllegalActionException  {
        double value = ((DoubleToken) yHeight.getToken()).doubleValue();
        return value / 2.0;
    }

    /** Return the value of the length parameter
     *  @return the length of the box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getLength() throws IllegalActionException {
        double value = ((DoubleToken) xLength.getToken()).doubleValue();
        return value / 2.0;
    }

    /** Return the value of the width parameter
     *  @return the width of the box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getWidth() throws IllegalActionException {
        double value = ((DoubleToken) zWidth.getToken()).doubleValue();
        return value / 2.0;
    }
}
