/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2014-2018 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package org.ptolemy.ssm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.image.PGMReader;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * An occupancy grid map.
 * The map input, which can come from an image file read by PGMReader,
 * is a record with width, height, and grid fields. The width and height
 * are positive integers, and the grid is an array of integer values for
 * each position in the map.
 *
 * This actor decorates any entity in the model that implements the
 * MapConstrained interface.
 *
 * @see MapConstrained
 * @see PGMReader
 * @author Ilge Akkaya
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Map extends TypedAtomicActor implements Decorator {

    /** Construct a StateSpaceModel with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Map(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The occupancy grid map. */
    public PortParameter map;

    /** A 2-d array denoting the map origin in x and y directions
     * respectively.
     */
    public Parameter origin;

    /** Map resolution in meters/pixel.
     */
    public Parameter resolution;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the state accordingly.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If
     *   <i>stateVariableNamest</i> cannot be evaluated or cannot be
     *   converted to the output type, or if the superclass throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == map) {
            RecordToken mapToken = (RecordToken) map.getToken();
            _width = ((IntToken) mapToken.get("width")).intValue();
            _height = ((IntToken) mapToken.get("height")).intValue();
            Token[] gridContent = ((ArrayToken) mapToken.get("grid"))
                    .arrayValue();
            _occupancyGrid = new int[_height][_width];
            for (int i = 0; i < _height; i++) {
                for (int j = 0; j < _width; j++) {
                    int index = j + i * _width;
                    _occupancyGrid[i][j] = ((IntToken) gridContent[index])
                            .intValue();
                }
            }
        } else if (attribute == origin) {
            _origin[0] = ((DoubleToken) ((ArrayToken) origin.getToken())
                    .getElement(0)).doubleValue();
            _origin[1] = ((DoubleToken) ((ArrayToken) origin.getToken())
                    .getElement(1)).doubleValue();
        } else if (attribute == resolution) {
            _resolution = ((DoubleToken) resolution.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Map newObject = (Map) super.clone(workspace);
        newObject._origin = new double[2];
        newObject._occupancyGrid = null;
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        map.update();
    }

    /** Return the occupancy grid.
     *  @return the occupancy grid
     */
    public int[][] getOccupancyGrid() {
        int[][] copyOccupancy = new int[_occupancyGrid.length][_occupancyGrid[0].length];
        for (int i = 0; i < copyOccupancy.length; i++) {
            copyOccupancy[i] = Arrays.copyOf(_occupancyGrid[i],
                    _occupancyGrid[i].length);
        }
        return copyOccupancy;
    }

    /** Return the origin.
     *  @return the origin.
     */
    public double[] getOrigin() {
        return Arrays.copyOf(_origin, _origin.length);
    }

    /**
     * Check if queried (x,y) position is within the valid map area.
     * @param xCoord The x coordinate
     * @param yCoord The y coordinate
     * @return True if the position is within the valid map area.
     */
    public boolean withinValidMapArea(double xCoord, double yCoord) {

        int gridX = (int) Math.floor(xCoord / _resolution);
        int gridY = (int) Math.floor(yCoord / _resolution);

        if (gridX >= 0 && gridY >= 0 && gridY < _occupancyGrid.length
                && gridX < _occupancyGrid[0].length) {
            return (_occupancyGrid[gridY][gridX] == VALID_MAP_INTENSITY);
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the class. */
    private void _init()
            throws IllegalActionException, NameDuplicationException {

        resolution = new Parameter(this, "resolution");
        resolution.setExpression("0.05");
        resolution.setTypeEquals(BaseType.DOUBLE);

        origin = new Parameter(this, "origin");
        origin.setExpression("{0.0,0.0}");
        origin.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        map = new PortParameter(this, "map");
        map.setExpression("{width = 2, height=1, grid={0,0}}");
        String[] mapLabels = { "width", "height", "grid" };
        Type[] types = { BaseType.INT, BaseType.INT,
                new ArrayType(BaseType.INT) };
        map.setTypeEquals(new RecordType(mapLabels, types));
        map.setVisibility(Settable.EXPERT);

        _origin = new double[2];

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _width;
    private int _height;
    private int[][] _occupancyGrid;
    private double[] _origin;
    private double _resolution;

    private static final int VALID_MAP_INTENSITY = 255;

    @Override
    public String description() throws IllegalActionException {
        // TODO Auto-generated method stub
        return "A Map aspect that decorates actors, which implement the MapConstrained interface";
    }

    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target)
            throws IllegalActionException {
        if (target instanceof MapConstrained) {
            try {
                return new MapAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    /** Return the decorated objects.
     *  @return the decorated objects.
     */
    @Override
    public List<NamedObj> decoratedObjects() throws IllegalActionException {
        if (workspace().getVersion() == _decoratedObjectsVersion) {
            return _decoratedObjects;
        }
        _decoratedObjectsVersion = workspace().getVersion();
        List<NamedObj> list = new ArrayList();
        CompositeEntity container = (CompositeEntity) getContainer();
        if (container != null) {
            for (Object object : container.deepEntityList()) {
                if (object instanceof MapConstrained) {
                    list.add((NamedObj) object);
                }
            }
            _decoratedObjects = list;
        }
        return list;
    }

    @Override
    public boolean isGlobalDecorator() throws IllegalActionException {
        return false;
    }

    /** Cached list of decorated objects. */
    protected List<NamedObj> _decoratedObjects;

    /** Version for _decoratedObjects. */
    protected long _decoratedObjectsVersion = -1L;

}
