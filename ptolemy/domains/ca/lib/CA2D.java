/* 
 CA2D Actor
 
 Copyright (c) 2010 The University of Florida

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
import ptolemy.kernel.util.Workspace;

/**
 * 
 * The CA2D actor should be used in conjunction with the CADirector. 
 * To use this actor, it is highly recommended to start with one of the 
 * examples.  The examples have the required variables (used by the 
 * director) added to the canvas and connected to the correct input and 
 * output ports of the CA2D actor.  The actor should be used to perform 
 * convolution operations in discrete two-dimensional (2D) space.  The 
 * inputs are: i and j, the x and y location of the current space on the 
 * 2D grid; the current value of the space (i,j) on the grid; and the 
 * current values of all neighboring cells in the grid.  The output is 
 * the new value at 2D grid position (i,j).
 * 
 *  @author  Zach Ezzell, Contributor: Christopher Brooks
 *  @version $Id$ 
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class CA2D extends TypedCompositeActor {

    /**
     * Construct a new CA2D.
     * 
     * @param entity The container.
     * @param name The name of the CA2D actor.
     * @exception IllegalActionException If the name has a period in it.
     * @exception NameDuplicationException If the container already
     * contains an entity with the specified name.
     */
    public CA2D(CompositeEntity entity, String name)
            throws IllegalActionException, NameDuplicationException {
        super(entity, name);
        iPort = new TypedIOPort(this, "x location", true, false);
        iPort.setTypeEquals(BaseType.INT);
        iPort.setMultiport(false);

        jPort = new TypedIOPort(this, "y location", true, false);
        jPort.setTypeEquals(BaseType.INT);
        jPort.setMultiport(false);

        valuePort = new TypedIOPort(this, "current value", true, false);
        valuePort.setTypeEquals(BaseType.DOUBLE);
        valuePort.setMultiport(false);

        outputPort = new TypedIOPort(this, "new value", false, true);
        outputPort.setTypeEquals(BaseType.DOUBLE);
        outputPort.setMultiport(false);

        n1Port = new TypedIOPort(this, "neighbor 1", true, false);
        n1Port.setTypeEquals(BaseType.DOUBLE);
        n1Port.setMultiport(false);

        n2Port = new TypedIOPort(this, "neighbor 2", true, false);
        n2Port.setTypeEquals(BaseType.DOUBLE);
        n2Port.setMultiport(false);

        n3Port = new TypedIOPort(this, "neighbor 3", true, false);
        n3Port.setTypeEquals(BaseType.DOUBLE);
        n3Port.setMultiport(false);

        n4Port = new TypedIOPort(this, "neighbor 4", true, false);
        n4Port.setTypeEquals(BaseType.DOUBLE);
        n4Port.setMultiport(false);

        n5Port = new TypedIOPort(this, "neighbor 5", true, false);
        n5Port.setTypeEquals(BaseType.DOUBLE);
        n5Port.setMultiport(false);

        n6Port = new TypedIOPort(this, "neighbor 6", true, false);
        n6Port.setTypeEquals(BaseType.DOUBLE);
        n6Port.setMultiport(false);

        n7Port = new TypedIOPort(this, "neighbor 7", true, false);
        n7Port.setTypeEquals(BaseType.DOUBLE);
        n7Port.setMultiport(false);

        n8Port = new TypedIOPort(this, "neighbor 8", true, false);
        n8Port.setTypeEquals(BaseType.DOUBLE);
        n8Port.setMultiport(false);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    /**
     *  This is the x location of the current grid space.
     */
    
    private TypedIOPort iPort;
    
    /**
     *  This is the y location of the current grid space.
     */
    private TypedIOPort jPort;
    
    /**
     *  This is top-left neighbor (i-1,j-1) of the current grid space (i,j).
     */
    private TypedIOPort n1Port;
    
    /**
     *  This is top neighbor (i,j-1) of the current grid space (i,j).
     */
    private TypedIOPort n2Port;
    
    /**
     *  This is top-right neighbor (i+1,j-1) of the current grid space (i,j).
     */
    private TypedIOPort n3Port;
    
    /**
     *  This is left neighbor (i-1,j) of the current grid space (i,j).
     */
    private TypedIOPort n4Port;
    
    /**
     *  This is right neighbor (i+1,j) of the current grid space (i,j).
     */
    private TypedIOPort n5Port;
    
    /**
     *  This is bottom-left neighbor (i-1,j+1) of the current grid space (i,j).
     */
    private TypedIOPort n6Port;
    
    /**
     *  This is bottom neighbor (i,j+1) of the current grid space (i,j).
     */
    private TypedIOPort n7Port;
    
    /**
     *  This is bottom-right neighbor (i+1,j+1) of the current grid space (i,j).
     */
    private TypedIOPort n8Port;
    
    /**
     *  This is the new value of the current grid space (i,j).
     */
    private TypedIOPort outputPort;
    
    /**
     *  This is the value of the current grid space (i,j).
     */
    private TypedIOPort valuePort;
}
