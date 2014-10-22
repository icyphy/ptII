/* An icon that renders the value of an attribute of the container.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Random;

import javax.swing.SwingConstants;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;

///////////////////////////////////////////////////////////////////
//// TableIcon

/**
 An icon that displays the value of a variable of the container in a
 table. The attribute is assumed to be an instance of Variable, and its name
 is given by the parameter <i>variableName</i>. Its value must be an
 array of records.  A subset of the fields in the records given by
 the <i>fields</i> parameter is displayed in the icon.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class TableIcon extends DynamicEditorIcon {
    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TableIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        variableName = new StringParameter(this, "variableName");

        boxColor = new ColorAttribute(this, "boxColor");
        boxColor.setExpression("{1.0, 1.0, 1.0, 1.0}");

        Variable UNBOUNDED = new Variable(this, "UNBOUNDED");
        UNBOUNDED.setVisibility(Settable.NONE);
        UNBOUNDED.setExpression("0");

        maxRows = new Parameter(this, "maxRows");
        maxRows.setTypeEquals(BaseType.INT);
        maxRows.setExpression("UNBOUNDED");

        Variable ALL = new Variable(this, "ALL");
        ALL.setVisibility(Settable.NONE);
        Token emptyStringArray = new ArrayToken(BaseType.STRING);
        ALL.setToken(emptyStringArray);

        fields = new Parameter(this, "fields");
        fields.setTypeEquals(new ArrayType(BaseType.STRING));
        fields.setExpression("ALL");

        colorKey = new StringParameter(this, "colorKey");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Color of the box. This defaults to white. */
    public ColorAttribute boxColor;

    /** A column name to use as a color key. If this string is
     *  non-empty, then it specifies a column name that is used
     *  to determine a color for each row. The value in that
     *  row and column determines the color via a hash function,
     *  so that if two rows are identical in that column, then
     *  they are also identical in color.  This is a string that
     *  defaults to empty, indicating that all rows should
     *  be displayed in black.
     */
    public StringParameter colorKey;

    /** The fields to display in the table.
     *  This is an array of strings specifying the field
     *  names to display. It defaults to ALL, which indicates
     *  that all fields should be displayed.
     */
    public Parameter fields;

    /** The maximum number of rows to display. This is an integer, with
     *  default value UNBOUNDED.
     */
    public Parameter maxRows;

    /** The name of the variable in the container
     *  whose value should be displayed in the icon. The variable
     *  value must be an array of records. This is a string that
     *  defaults to the empty string.
     */
    public StringParameter variableName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new background figure.  This overrides the base class
     *  to draw a box around the value display, where the width of the
     *  box depends on the value.
     *  @return A new figure.
     */
    @Override
    public Figure createBackgroundFigure() {
        NamedObj container = getContainer();
        CompositeFigure result = new CompositeFigure();
        if (container != null) {
            try {
                ArrayToken fieldsValue = (ArrayToken) fields.getToken();
                Attribute associatedAttribute = container
                        .getAttribute(variableName.getExpression());
                if (associatedAttribute instanceof Variable) {
                    Token value = ((Variable) associatedAttribute).getToken();
                    if (value instanceof ArrayToken) {
                        // Find the number of rows and columns.
                        int numRows = ((ArrayToken) value).length();
                        int numColumns = fieldsValue.length();
                        if (numColumns == 0) {
                            // All columns should be included.
                            // Make a pass to figure out how many that is.
                            for (int i = 0; i < numRows; i++) {
                                Token row = ((ArrayToken) value).getElement(i);
                                if (row instanceof RecordToken) {
                                    int rowWidth = ((RecordToken) row)
                                            .labelSet().size();
                                    if (rowWidth > numColumns) {
                                        numColumns = rowWidth;
                                    }
                                }
                            }
                        }

                        // Find the width of each column and the height of each row.
                        // All rows are the same height, but column widths can vary.
                        double rowHeight = 0.0;
                        double columnWidth[] = new double[numColumns];
                        for (int i = 1; i < numColumns; i++) {
                            columnWidth[i] = 0.0;
                        }
                        LabelFigure tableElement[][] = new LabelFigure[numRows][numColumns];
                        // Iterate over rows.
                        for (int i = 0; i < numRows; i++) {
                            Token row = ((ArrayToken) value).getElement(i);
                            if (row instanceof RecordToken) {
                                if (fieldsValue.length() == 0) {
                                    // Display all fields.
                                    Iterator labelSet = ((RecordToken) row)
                                            .labelSet().iterator();
                                    int j = 0;
                                    while (labelSet.hasNext()) {
                                        String column = (String) labelSet
                                                .next();
                                        tableElement[i][j] = _labelFigure(
                                                (RecordToken) row, column);
                                        Rectangle2D bounds = tableElement[i][j]
                                                .getBounds();
                                        double width = bounds.getWidth();
                                        if (width > columnWidth[j]) {
                                            columnWidth[j] = width;
                                        }
                                        double height = bounds.getHeight();
                                        if (height > rowHeight) {
                                            rowHeight = height;
                                        }
                                        j++;
                                    }
                                } else {
                                    // Display specified fields.
                                    for (int j = 0; j < fieldsValue.length(); j++) {
                                        if (j >= numColumns) {
                                            break;
                                        }
                                        String column = ((StringToken) fieldsValue
                                                .getElement(j)).stringValue();
                                        tableElement[i][j] = _labelFigure(
                                                (RecordToken) row, column);
                                        Rectangle2D bounds = tableElement[i][j]
                                                .getBounds();
                                        double width = bounds.getWidth();
                                        if (width > columnWidth[j]) {
                                            columnWidth[j] = width;
                                        }
                                        double height = bounds.getHeight();
                                        if (height > rowHeight) {
                                            rowHeight = height;
                                        }
                                    }
                                }
                            }
                        }

                        // Now make a pass to position and add all the figures.
                        double rowPosition = _VERTICAL_PADDING;
                        // Iterate over rows.
                        for (int i = 0; i < numRows; i++) {
                            Token row = ((ArrayToken) value).getElement(i);
                            if (row instanceof RecordToken) {
                                if (fieldsValue.length() == 0) {
                                    // Display all fields.
                                    Iterator labelSet = ((RecordToken) row)
                                            .labelSet().iterator();
                                    int j = 0;
                                    double columnPosition = _HORIZONTAL_PADDING;
                                    while (labelSet.hasNext()) {
                                        /*String column = (String) */labelSet
                                                .next();
                                        tableElement[i][j].translateTo(
                                                columnPosition, rowPosition);
                                        result.add(tableElement[i][j]);
                                        columnPosition += columnWidth[j]
                                                + _HORIZONTAL_PADDING;
                                        j++;
                                    }
                                } else {
                                    // Display specified fields.
                                    double columnPosition = _HORIZONTAL_PADDING;
                                    for (int j = 0; j < fieldsValue.length(); j++) {
                                        // String column = ((StringToken)fieldsValue.getElement(j)).stringValue();
                                        tableElement[i][j].translateTo(
                                                columnPosition, rowPosition);
                                        result.add(tableElement[i][j]);
                                        columnPosition += columnWidth[j]
                                                + _HORIZONTAL_PADDING;
                                    }
                                }
                            }
                            rowPosition += rowHeight + _VERTICAL_PADDING;
                        }
                    }
                }
            } catch (IllegalActionException e) {
                // Stick the error message in the icon.
                result.add(new LabelFigure(e.getMessage()));
            }
        }
        // Now put a box around it all.
        Rectangle2D bounds = result.getBounds();
        // Double the padding below to allow for both sides.
        double width = Math.floor(bounds.getWidth()) + _HORIZONTAL_PADDING * 2;
        double height = Math.floor(bounds.getHeight()) + _VERTICAL_PADDING * 2;
        Figure rectangle = new BasicRectangle(0, 0, width, height,
                boxColor.asColor(), 1);
        CompositeFigure finalResult = new CompositeFigure(rectangle);
        finalResult.add(result);
        return finalResult;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The font used. */
    protected static final Font _labelFont = new Font("SansSerif", Font.PLAIN,
            12);

    /** The amount of padding to use around the edges. */
    protected static final double _HORIZONTAL_PADDING = 5.0;

    /** The amount of padding to use around the edges. */
    protected static final double _VERTICAL_PADDING = 3.0;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a label figure for the specified token.
     *  @param row The row.
     *  @param column The column.
     *  @return A label figure.
     */
    private LabelFigure _labelFigure(RecordToken row, String column) {
        Token token = row.get(column);
        String label = "";
        if (token instanceof StringToken) {
            label = ((StringToken) token).stringValue();
        } else if (token != null) {
            label = token.toString();
        }
        Color color = Color.black;
        try {
            if (!colorKey.stringValue().equals("")) {
                Token colorToken = row.get(colorKey.stringValue());
                if (colorToken != null) {
                    color = _uniqueColor(colorToken.toString());
                }
            }
        } catch (IllegalActionException e) {
            // Ignore. Use black.
        }

        LabelFigure tableElement = new LabelFigure(label, _labelFont, 1.0,
                SwingConstants.NORTH_WEST, color);
        return tableElement;
    }

    private Color _uniqueColor(Object object) {
        // Get a color from the hash code. We will use
        // the low order 24 bits only.
        int hashCode = object.hashCode();
        // Use the code as a seed for a random number generator.
        // FindBugs: [H B BC] Random object created and used only once [DMI_RANDOM_USED_ONLY_ONCE]
        // Actually this is the intend since you want a unique color for
        // each specific object (hence it can't be completely random).
        int code = new Random(hashCode).nextInt();
        float red = (code >> 16 & 0xff) / 256.0f;
        float green = (code >> 8 & 0xff) / 256.0f;
        float blue = (code & 0xff) / 256.0f;
        // Make sure the color is at least as dark as close to a pure red, green, or blue.
        // This means that the magnitude of the r,g,b vector is no greater than 1.0.
        float magnitude = (float) Math.sqrt(red * red + green * green + blue
                * blue);
        if (magnitude < 0.8f) {
            magnitude = 0.8f;
        }

        Color result = new Color(red / magnitude, green / magnitude, blue
                / magnitude);
        return result;
    }
}
