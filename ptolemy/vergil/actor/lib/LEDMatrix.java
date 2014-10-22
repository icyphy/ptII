/* An actor to display 10x10 LEDs

 Copyright (c) 2007-2014 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.vergil.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;

///////////////////////////////////////////////////////////////////
//// LEDMatrix

/** An actor that displays an array of LEDs.  The array display only
 one color, red.  This actor has two inputs, row and column which
 are integers that identify the row and column of the LED to possibly be
 illuminated and a control input which determines whether the
 LED is illuminated or not.

 @author Christopher Brooks, Based on MicaLeds byElaine Cheong
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
public class LEDMatrix extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LEDMatrix(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create the input ports.
        column = new TypedIOPort(this, "column", true, false);
        column.setTypeEquals(BaseType.INT);
        row = new TypedIOPort(this, "row", true, false);
        row.setTypeEquals(BaseType.INT);

        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.BOOLEAN);

        // The number of columns
        columns = new Parameter(this, "columns");
        columns.setExpression("10");
        rows = new Parameter(this, "rows");
        rows.setExpression("10");

        // Create the LED Array icon.
        _ledArray_icon = new EditorIcon(this, "_icon");
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The column of the LED to be illuminated.  The columns are 0-based,
     *  to address the first column, the value of this port should be 0.
     *  The token type is integer.
     */
    public TypedIOPort column;

    /** True if the LED is to be illuminated. The token type is Boolean.
     */
    public TypedIOPort control;

    /** The row of the LED to be illuminated.  The columns are 0-based,
     *  to address the first row, the value of this port should be 0.
     *  The token type is integer.
     */
    public TypedIOPort row;

    /** The number of columns.  The number must be a positive integer,
     *  the initial default value is 10.
     */
    public Parameter columns;

    /** The number of rows.  The number must be a positive integer,
     *  the initial default value is 10.
     */
    public Parameter rows;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone this actor into the specified workspace.
     *  Override the base class to ensure that private variables are
     *  reset.
     *  @param workspace The workspace for the cloned object.
     *  @return A new instance of VisualModelReference.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        LEDMatrix newActor = (LEDMatrix) super.clone(workspace);
        try {
            int columnsValue = ((IntToken) newActor.columns.getToken())
                    .intValue();
            int rowsValue = ((IntToken) newActor.rows.getToken()).intValue();
            newActor._leds = new RectangleAttribute[rowsValue][columnsValue];
            Attribute attribute = newActor.getAttribute("_icon");
            if (attribute != null) {
                attribute.setContainer(null);
            }
            newActor._ledArray_icon = new EditorIcon(newActor, "_icon");
        } catch (Throwable ex) {
            throw new CloneNotSupportedException(getFullName()
                    + ": Failed to get rows or columns: " + ex);
        }
        return newActor;
    }

    /** If the argument is <i>rows</i> or <i>columns</i>, then update
     *  the size of the array.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the offsets array is not
     *   nondecreasing and nonnegative.
     */
    // FIXME: get attribute changed working by moving init() to initialize()?
    //     public void attributeChanged(Attribute attribute)
    //             throws IllegalActionException {
    //         if (attribute == rows || attribute == columns) {
    //             try {
    //                 _init();
    //             } catch (NameDuplicationException ex) {
    //                 throw new IllegalActionException(this, ex, "Failed to initialize.");
    //             }
    //         } else {
    //             super.attributeChanged(attribute);
    //         }
    //     }
    /** Read a token from the row and column ports and illuminate that
     *  led until the next fire.
     *  @exception IllegalActionException If the row or column ports
     *  cannot be read.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (row.hasToken(0) && column.hasToken(0) && control.hasToken(0)) {
            int rowValue = ((IntToken) row.get(0)).intValue();
            int columnValue = ((IntToken) column.get(0)).intValue();
            boolean controlValue = ((BooleanToken) control.get(0))
                    .booleanValue();
            if (controlValue) {
                _leds[rowValue][columnValue].fillColor
                        .setToken("{1.0, 0.0, 0.0, 1.0}");
            } else {
                _leds[rowValue][columnValue].fillColor
                        .setToken("{0.0, 0.0, 0.0, 1.0}");
            }
        }
    }

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        int columnsValue = ((IntToken) columns.getToken()).intValue();
        int rowsValue = ((IntToken) rows.getToken()).intValue();
        for (int x = 0; x < rowsValue; x++) {
            for (int y = 0; y < columnsValue; y++) {
                _leds[x][y].fillColor.setToken("{0.0, 0.0, 0.0, 1.0}");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the LED Array. */
    private void _init() throws IllegalActionException,
            NameDuplicationException {

        int columnsValue = ((IntToken) columns.getToken()).intValue();
        int rowsValue = ((IntToken) rows.getToken()).intValue();
        _leds = new RectangleAttribute[rowsValue][columnsValue];
        for (int x = 0; x < rowsValue; x++) {
            for (int y = 0; y < columnsValue; y++) {
                RectangleAttribute rectangle = new RectangleAttribute(
                        _ledArray_icon, "_led_" + x + "_" + y);
                Location location = new Location(rectangle, "_location");
                double[] ledLocationValue = { x * 20, y * 20 };
                location.setLocation(ledLocationValue);
                rectangle.width.setToken("20.0");
                rectangle.height.setToken("39.0");
                rectangle.centered.setToken("true");
                rectangle.fillColor.setToken("{0.0, 0.0, 0.0, 1.0}");
                _leds[x][y] = rectangle;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Graphical icons for LED Array; */
    private RectangleAttribute[][] _leds;

    /** The icon. */
    EditorIcon _ledArray_icon;
}
