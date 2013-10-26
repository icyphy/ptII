/* Cellular Automata 2D Convolution Actor.

 Copyright (c) 2010-2013 The University of Florida

 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF FLORIDA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF FLORIDA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF FLORIDA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 FLORIDA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
 */

package ptolemy.domains.ca.lib;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Cellular Automata 2D Convolution Actor.
 *
 * <p>The CA2DConvolution actor should be used in conjunction with the CADirector.
 * To use this actor, it is highly recommended to start with one of the
 * examples.  The examples have the required variables (used by the
 * director) added to the canvas and connected to the correct input and
 * output ports of the CA2DConvolution actor.  The actor should be used to perform
 * convolution operations in discrete two-dimensional (2D) space.  The
 * inputs are: i and j, the x and yLocation of the current space on the
 * 2D grid; the currentValue of the space (i,j) on the grid; and the
 * currentValues of all neighboring cells in the grid.  The output is
 * the newValue at 2D grid position (i,j).</p>
 *
 *  @author  Zach Ezzell, Contributor: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class CA2DConvolution extends TypedCompositeActor {

    /**
     * Construct a new CA2DConvolution.
     *
     * @param entity The container.
     * @param name The name of the CA2DConvolution actor.
     * @exception IllegalActionException If the name has a period in it.
     * @exception NameDuplicationException If the container already
     * contains an entity with the specified name.
     */
    public CA2DConvolution(CompositeEntity entity, String name)
            throws IllegalActionException, NameDuplicationException {
        super(entity, name);
        xLocation = new TypedIOPort(this, "xLocation", true, false);
        xLocation.setTypeEquals(BaseType.INT);
        xLocation.setMultiport(false);

        yLocation = new TypedIOPort(this, "yLocation", true, false);
        yLocation.setTypeEquals(BaseType.INT);
        yLocation.setMultiport(false);

        currentValue = new TypedIOPort(this, "currentValue", true, false);
        currentValue.setTypeEquals(BaseType.DOUBLE);
        currentValue.setMultiport(false);

        newValue = new TypedIOPort(this, "newValue", false, true);
        newValue.setTypeEquals(BaseType.DOUBLE);
        newValue.setMultiport(false);

        neighbor1 = new TypedIOPort(this, "neighbor1", true, false);
        neighbor1.setTypeEquals(BaseType.DOUBLE);
        neighbor1.setMultiport(false);

        neighbor2 = new TypedIOPort(this, "neighbor2", true, false);
        neighbor2.setTypeEquals(BaseType.DOUBLE);
        neighbor2.setMultiport(false);

        neighbor3 = new TypedIOPort(this, "neighbor3", true, false);
        neighbor3.setTypeEquals(BaseType.DOUBLE);
        neighbor3.setMultiport(false);

        neighbor4 = new TypedIOPort(this, "neighbor4", true, false);
        neighbor4.setTypeEquals(BaseType.DOUBLE);
        neighbor4.setMultiport(false);

        neighbor5 = new TypedIOPort(this, "neighbor5", true, false);
        neighbor5.setTypeEquals(BaseType.DOUBLE);
        neighbor5.setMultiport(false);

        neighbor6 = new TypedIOPort(this, "neighbor6", true, false);
        neighbor6.setTypeEquals(BaseType.DOUBLE);
        neighbor6.setMultiport(false);

        neighbor7 = new TypedIOPort(this, "neighbor7", true, false);
        neighbor7.setTypeEquals(BaseType.DOUBLE);
        neighbor7.setMultiport(false);

        neighbor8 = new TypedIOPort(this, "neighbor8", true, false);
        neighbor8.setTypeEquals(BaseType.DOUBLE);
        neighbor8.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     *  This is the xLocation of the current grid space.
     */

    private TypedIOPort xLocation;

    /**
     *  This is the yLocation of the current grid space.
     */
    private TypedIOPort yLocation;

    /**
     *  This is top-left neighbor (i-1,j-1) of the current grid space (i,j).
     */
    private TypedIOPort neighbor1;

    /**
     *  This is top neighbor (i,j-1) of the current grid space (i,j).
     */
    private TypedIOPort neighbor2;

    /**
     *  This is top-right neighbor (i+1,j-1) of the current grid space (i,j).
     */
    private TypedIOPort neighbor3;

    /**
     *  This is left neighbor (i-1,j) of the current grid space (i,j).
     */
    private TypedIOPort neighbor4;

    /**
     *  This is right neighbor (i+1,j) of the current grid space (i,j).
     */
    private TypedIOPort neighbor5;

    /**
     *  This is bottom-left neighbor (i-1,j+1) of the current grid space (i,j).
     */
    private TypedIOPort neighbor6;

    /**
     *  This is bottom neighbor (i,j+1) of the current grid space (i,j).
     */
    private TypedIOPort neighbor7;

    /**
     *  This is bottom-right neighbor (i+1,j+1) of the current grid space (i,j).
     */
    private TypedIOPort neighbor8;

    /**
     *  This is the newValue of the current grid space (i,j).
     */
    private TypedIOPort newValue;

    /**
     *  This is the value of the current grid space (i,j).
     */
    private TypedIOPort currentValue;
}
