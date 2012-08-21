/* Polygon icon.

@Copyright (c) 2008-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
import java.awt.Shape;
import java.util.Collection;
import java.util.StringTokenizer;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.util.java2d.Polygon2D;


/** This icon is described by a polygon which is given by an integer
 *  array.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class PolygonIcon extends EditorIcon implements Settable {

    
    /** Construct an icon in the specified workspace and name.
     *  This constructor is typically used in conjunction with
     *  setContainerToBe() and createFigure() to create an icon
     *  and generate a figure without having to have write access
     *  to the workspace.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  @see #setContainerToBe(NamedObj)
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the specified name contains
     *   a period.
     */
    public PolygonIcon(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name); 
        setVisibility(EXPERT);
    }

    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PolygonIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        setVisibility(EXPERT);
    }
    
    /** Add a value listener. 
     *  @param listener New value listener to be added.
     */
    @Override
    public void addValueListener(ValueListener listener) {
        // do nothing
    }
    
    /** Clone the location into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the base class throws it.
     *  @return A new Location.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PolygonIcon newObject = (PolygonIcon) super.clone(workspace);

        // Copy the location so that the reference in the new object
        // does not refer to the same array.
        // _location can never be null because setLocation() will
        // not handle it.
        int length = _polygonCoordinates.length;
        newObject._polygonCoordinates = new Integer[length];
        System.arraycopy(_polygonCoordinates, 0, newObject._polygonCoordinates, 0, length);

        return newObject;
    }
    
    /** Create a new figure. 
     *  FIXME: does this need to be as elaborate as the function in EditorIcon.
     *  @return A new figure.
     */
    @Override
    public Figure createBackgroundFigure() {
        return createFigure();
    } 
    
    /** Create a new figure that visually represents this icon.
     *  @return A new Figure
     */
    @Override
    public Figure createFigure() { 
        Shape shape;
        Polygon2D.Double polygon = new Polygon2D.Double();  
        for (int i = 0; i < _polygonCoordinates.length; i = i + 2) {
            polygon.lineTo(_polygonCoordinates[i], _polygonCoordinates[i + 1]);  
        }
        polygon.closePath();
        shape = polygon;
        
        Color fill = _fill;
        if (fill == null) {
            fill = Color.black;
        }
        
        return new BasicFigure(shape, fill, (float) 1.5);
    } 
    
    /** There is no default expression for the polygon icon.
     *  FIXME: should we define a default expression? 
     *  @return null;
     */
    @Override
    public String getDefaultExpression() {
        return null;
    }

    /** Get the expression as a string.
     *  @return The string expression.
     */
    @Override
    public String getExpression() {
        if (_expressionSet) { 
            return _expression;
        }

        if ((_polygonCoordinates == null) || (_polygonCoordinates.length == 0)) {
            return "";
        }
 
        StringBuffer result = new StringBuffer("{");

        for (int i = 0; i < (_polygonCoordinates.length - 1); i++) {
            result.append(_polygonCoordinates[i]);
            result.append(", ");
        }

        result.append(_polygonCoordinates[_polygonCoordinates.length - 1] + "}");
        return result.toString();
    }

    /** Return the value as a string. This is the same as the expression
     *  as a string. 
     *  @return The value as a string.
     */
    @Override
    public String getValueAsString() {
        return getExpression();
    }

    /** Return the visibility.
     *  @return The visibility. 
     */
    @Override
    public Visibility getVisibility() { 
        return _visibility;
    }

    /** There are no value listeners defined so do nothing.
     *  @listener The listener to be removed.
     */
    @Override
    public void removeValueListener(ValueListener listener) {
        // do nothing
    }

    /** Set the expression of this icon which is an array of the coordinates. 
     *  @expression The expression.
     */
    @Override
    public void setExpression(String expression) throws IllegalActionException {  
        _expression = expression;
        _expressionSet = true;
    }

    /** Set the visibility of this icon.
     *  @param visiblity The new visibility.
     */
    @Override
    public void setVisibility(Visibility visibility) {
        _visibility = visibility;
    }

    /** Set the polygon coordinates. 
     *  @param coordinates The coordinates.
     */
    public void setPolygonCoordinates(Integer[] coordinates) {
        _polygonCoordinates = coordinates;
    }
    
    /** Validate the coordinates provided as a string. 
     *  @throws IllegalActionException Not thrown here.
     */
    @Override
    public Collection validate() throws IllegalActionException { 
     // If the value has not been set via setExpression(), there is
        // nothing to do.
        if (!_expressionSet) {
            return null;
        }

        Integer[] coordinates = null;

        if (_expression != null) {  
            StringTokenizer tokenizer = new StringTokenizer(_expression,
                    ",[]{}");
            coordinates = new Integer[tokenizer.countTokens()];

            int count = tokenizer.countTokens();

            for (int i = 0; i < count; i++) {
                String next = tokenizer.nextToken().trim();
                coordinates[i] = Integer.parseInt(next);
            }
        } 
        _polygonCoordinates = coordinates; 
        // FIXME: notify?
        return null;
    } 
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The expression given in setExpression().
    private String _expression;

    // Indicator that the expression is the most recent spec for the location.
    private boolean _expressionSet = false;
    
    // The icon fill color. FIXME Should this be settable?
    private Color _fill;
    
    private Integer[] _polygonCoordinates;
    
    private Visibility _visibility;
    
}
