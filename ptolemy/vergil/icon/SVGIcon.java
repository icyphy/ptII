/* An icon whose description is represented in SVG

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
import ptolemy.vergil.kernel.attributes.FilledShapeAttribute;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.ShapeAttribute;
import diva.util.xml.XmlDocument;
import diva.util.xml.XmlElement;
import diva.util.xml.XmlReader;

//////////////////////////////////////////////////////////////////////////
//// SVGIcon
/**
This class is intended to eventually replace XMLIcon, however,
the current version doesn't work very well, so it isn't used.

@author Edward A. Lee
@version $Id$
*/
public class SVGIcon extends EditorIcon implements ValueListener {

    /** Construct an icon in the specified workspace and name.
     *  This constructor is typically used in conjuction with
     *  setContainerToBe() and createFigure() to create an icon
     *  and generate a figure without having to have write access
     *  to the workspace.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  @see #setContainerToBe(NamedObj)
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     *  @exception IllegalActionException If the specified name contains
     *   a period.
     */
    public SVGIcon(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
        try {
            setName(name);
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(ex);
        }
    }
    
    /** Create a new icon with the given name in the given container.
     *  By default, the icon contains no graphic objects.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     */
    public SVGIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SVGIcon newObject = (SVGIcon)super.clone(workspace);
        newObject._description = null;
        newObject._smallIconDescription = null;
        return newObject;
    }
    
    /** Override the base class to establish this as a listener to
     *  icon descriptions in the container.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        _bindToContainer(container);
    }

    /** Indicate that the container of this icon will eventually
     *  be the specified object. This rather specialized method is
     *  used to create an icon and generate a figure without having
     *  to have write access to the workspace. To use it, use the
     *  constructor that takes a workspace and a name, then call
     *  this method to indicate what the container will be. You
     *  can then call createFigure() or createBackgroundFigure(),
     *  and the appropriate figure for the container specified here
     *  will be used.  Then queue a ChangeRequest that sets the
     *  container to the same specified container. Once the container
     *  has been set by calling setContainer(), then the object
     *  specified to this method is no longer relevant.
     *  @param container The container that will eventually be set.
     *  @see #getContainerOrContainerToBe()
     */
    public void setContainerToBe(NamedObj container) {
        super.setContainerToBe(container);
        _bindToContainer(container);
    }
    
    /** React to the fact that the value of an attribute named
     *  "_iconDescription" contained by the same container has changed
     *  value by redrawing the figure.
     *  @param settable The object that has changed value.
     */
    public void valueChanged(Settable settable) {
        String name = ((Nameable)settable).getName();
        if (name.equals("_iconDescription")
                || name.equals("_smallIconDescription")) {
            _recreateFigure();
            try {
                _updateContents();
            } catch (Exception ex) {
                // Regrettable, but how else to inform of error?
                throw new InternalErrorException(ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Establish this icon as a listener for changes in attributes
     *  named "_iconDescription" and "_smallIconDescription" in the
     *  specified container.
     */
    private void _bindToContainer(NamedObj container) {
        // Get the description.
        ConfigurableAttribute description
                = (ConfigurableAttribute)container.getAttribute(
                "_iconDescription");
        // If the description has changed...
        if (_description != description) {
            if (_description != null) {
                // Remove this as a listener if there
                // was a previous description.
                _description.removeValueListener(this);
            }

            // update the description.
            _description = description;

            if (_description != null) {
                // Listen for changes in value to the icon description.
                _description.addValueListener(this);
            }
        }
        // Get the icon description.
        description
                = (ConfigurableAttribute)container.getAttribute(
                "_smallIconDescription");
        // If the description has changed...
        if (_smallIconDescription != description) {
            if (_smallIconDescription != null) {
                // Remove this as a listener if there
                // was a previous description.
                _smallIconDescription.removeValueListener(this);
            }

            // update the description.
            _smallIconDescription = description;

            if (_smallIconDescription != null) {
                // Listen for changes in value to the icon description.
                _smallIconDescription.addValueListener(this);
            }
        }
        try {
            _updateContents();
        } catch (Exception ex) {
            // Regrettable, but how else to inform of error?
            throw new InternalErrorException(ex);
        }
        // clear the caches
        _recreateFigure();
    }
    
    /** Create a new attribute and insert it in this icon. This method
     *  must be called at a time when this thread can get write access
     *  on the workspace. For example, it is safe to call it from
     *  within a change request, or from within attributeChanged(), or
     *  from within notification of change to a parameter. The first
     *  argument is a string representation of the SVG element type,
     *  the second is a hashtable containing attributes of the object,
     *  and the third is the PC data within the element, if there is
     *  any.  Any attributes that are not recognized will be ignored.
     */
    private void _createAttribute(
            String type, Map attributes, String content) {
        try {
            if (type.equals("rect")) {
                RectangleAttribute attribute
                        = new RectangleAttribute(this, uniqueName("rect"));
                
                _processFilledShapeAttributeAttributes(attribute, attributes);
            } else if (type.equals("circle")) {
                EllipseAttribute attribute
                        = new EllipseAttribute(this, uniqueName("rect"));
    
                // Rename the attributes.
                attributes.put("x", _getAttribute(attributes, "cx", "0.0"));
                attributes.put("y", _getAttribute(attributes, "cy", "0.0"));
                
                double r = _getDouble(attributes, "r", 10.0);
                double width = r * 2.0;
                String widthString = Double.toString(width);
                attributes.put("width", widthString);
                attributes.put("height", widthString);
    
                _processFilledShapeAttributeAttributes(attribute, attributes);
    /* FIXME
            } else if (type.equals("ellipse")) {
                double cx, cy, rx, ry;
                cx = _getDouble(attributes, "cx", 0);
                cy = _getDouble(attributes, "cy", 0);
                rx = _getDouble(attributes, "rx");
                ry = _getDouble(attributes, "ry");
    
                PaintedShape ps = new PaintedShape(new Ellipse2D.Double(
                        cx - rx, cy - ry, 2 * rx, 2 * ry));
                processPaintedShapeAttributes(ps, attributes);
                return ps;
    
            } else if (type.equals("line")) {
                double x1, y1, x2, y2;
                x1 = _getDouble(attributes, "x1", 0);
                y1 = _getDouble(attributes, "y1", 0);
                x2 = _getDouble(attributes, "x2", 0);
                y2 = _getDouble(attributes, "y2", 0);
                Line2D line = new Line2D.Double(x1, y1, x2, y2);
                PaintedPath pp = new PaintedPath(line);
                processPaintedPathAttributes(pp, attributes);
                return pp;
            } else if (type.equals("polyline")) {
                double coords[] =
                    parseCoordString((String)attributes.get("points"));
                Polyline2D poly = new Polyline2D.Double();
                poly.moveTo(coords[0], coords[1]);
                for (int i = 2; i < coords.length; i += 2) {
                    poly.lineTo(coords[i], coords[i+1]);
                }
                PaintedPath pp = new PaintedPath(poly);
                processPaintedPathAttributes(pp, attributes);
                return pp;
            } else if (type.equals("polygon")) {
                double coords[] =
                    parseCoordString((String)attributes.get("points"));
                Polygon2D poly = new Polygon2D.Double();
                poly.moveTo(coords[0], coords[1]);
                for (int i = 2; i < coords.length; i += 2) {
                    poly.lineTo(coords[i], coords[i+1]);
                }
                poly.closePath();
    
                PaintedShape ps = new PaintedShape(poly);
                processPaintedShapeAttributes(ps, attributes);
                return ps;
    
            } else if (type.equals("text")) {
                double x, y;
                x = _getDouble(attributes, "x", 0);
                y = _getDouble(attributes, "y", 0);
                PaintedString string = new PaintedString(content);
                processPaintedStringAttributes(string, attributes);
                string.translate(x, y);
                return string;
            } else if (type.equals("image")) {
                double x, y, width, height;
                x = _getDouble(attributes, "x", 0);
                y = _getDouble(attributes, "y", 0);
                width = _getDouble(attributes, "width");
                height = _getDouble(attributes, "height");
                Rectangle2D bounds = new Rectangle2D.Double(x, y, width, height);
                String link = (String)attributes.get("xlink:href");
                // First try as a system resource.
                URL url = ClassLoader.getSystemResource(link);
                try {
                    if (url == null) {
                        // Web Start needs this.
                        if (_refClass == null) {
                            try {
                                _refClass =
                                    Class.forName("diva.canvas.toolbox.SVGParser");
                            } catch (ClassNotFoundException ex) {
                                throw new RuntimeException("Could not find " +
                                        "diva.canvas.toolbox.SVGParser");
                            }
                        }
                        url = _refClass.getClassLoader().getResource(link);
                    }
    
                    // Try as a regular URL.
                    if (url == null) {
                        url = new URL(link);
                    }
                    Toolkit tk = Toolkit.getDefaultToolkit();
                    Image img = tk.getImage(url);
                    PaintedImage image = new PaintedImage(img, bounds);
                    // Wait until the image has been completely loaded,
                    // unless an error occurred.
                    while (true) {
                        if (tk.prepareImage(img, -1, -1, image)) {
                            // The image was fully prepared, so return the
                            // created image.
                            break;
                        }
                        int bitflags = tk.checkImage(img, -1, -1, image);
                        if ((bitflags &
                                (ImageObserver.ABORT | ImageObserver.ERROR)) != 0) {
                            // There was an error if either flag is set,
                            // so return null.
                            return null;
                        }
                        Thread.yield();
                    }
                    return image;
                } catch (java.net.MalformedURLException ex) {
                    return null;
                }
    */
            }
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }
    
    /** Given the root of an XML tree, populate this icon with
     *  attributes for each graphical element. 
     */
    private void _generateContents(XmlElement root) {
        String name = root.getType();
        if (!name.equals("svg"))
            throw new IllegalArgumentException("Input XML has a root" +
                    "name which is '" + name + "' instead of 'svg':" +
                    root);
        Iterator children = root.elements();
        while (children.hasNext()) {
            XmlElement child = (XmlElement)children.next();
            _createAttribute(
                    child.getType(),
                    child.getAttributeMap(),
                    child.getPCData());
        }
    }

    /** Extract the named attribute from the attribute map and
     *  return the value as a string. If the named attribute is not
     *  present, then return the default.
     *  @param map The attribute map.
     *  @param name The element name.
     *  @param defaultValue The default value.
     *  @return The double specified by this attribute.
     */
    private static String _getAttribute(Map map, String name, String defaultValue) {
        if (map.containsKey(name)) {
            return (String)map.get(name);
        } else {
            return defaultValue;
        }
    }

    /** Extract the named attribute from the attribute map and
     *  return the value as a double. If the named attribute is not
     *  present, then return the default.
     *  @param map The attribute map.
     *  @param name The element name.
     *  @param defaultValue The default value.
     *  @return The double specified by this attribute.
     */
    private static double _getDouble(Map map, String name, double defaultValue) {
        if (map.containsKey(name)) {
            return Double.parseDouble((String)map.get(name));
        } else {
            return defaultValue;
        }
    }
    
    /** Set the attributes of a FilledShapeAttribute from the specified
     *  map of SVG attribute values.
     */
    private static void _processFilledShapeAttributeAttributes (
            FilledShapeAttribute attribute, Map attributes) {

        _processShapeAttributeAttributes(attribute, attributes);
        
        String width = _getAttribute(attributes, "width", "10.0");
        String height = _getAttribute(attributes, "height", "10.0");
        attribute.width.setExpression(width);
        attribute.height.setExpression(height);

        String style = (String) attributes.get("style");
        if (style != null) {
            StringTokenizer t = new StringTokenizer(style, ";");
            while (t.hasMoreTokens()) {
                String string = t.nextToken().trim();
                int index = string.indexOf(":");
                String name = string.substring(0, index);
                String value = string.substring(index + 1);
                /* FIXME: Figure out how to do this. See SVGParser.
                if (name.equals("fill")) {
                    ps.fillPaint = lookupColor(value);
                } else if (name.equals("stroke")) {
                    ps.strokePaint = lookupColor(value);
                } else if (name.equals("stroke-width")) {
                    ps.setLineWidth(Float.parseFloat(value));
                }
                */
            }
        }
    }

    /** Set the attributes of a ShapeAttribute from the specified
     *  map of SVG attribute values.
     */
    private static void _processShapeAttributeAttributes (
            ShapeAttribute attribute, Map attributes) {
        // FIXME: set lineWidth and lineColor.
        
        _processLocation(attribute, attributes);

        String style = (String) attributes.get("style");
        if (style != null) {
            StringTokenizer t = new StringTokenizer(style, ";");
            while (t.hasMoreTokens()) {
                String string = t.nextToken().trim();
                int index = string.indexOf(":");
                String name = string.substring(0, index);
                String value = string.substring(index + 1);
                /* FIXME: Figure out how to do this. See SVGParser.
                if (name.equals("fill")) {
                    ps.fillPaint = lookupColor(value);
                } else if (name.equals("stroke")) {
                    ps.strokePaint = lookupColor(value);
                } else if (name.equals("stroke-width")) {
                    ps.setLineWidth(Float.parseFloat(value));
                }
                */
            }
        }
    }

    /** Set the location of an Attribute from the specified
     *  map of SVG attribute values.
     */
    private static void _processLocation (
            Attribute attribute, Map attributes) {
        double locationValue[] = new double[2];
        locationValue[0] = _getDouble(attributes, "x", 0.0);
        locationValue[1] = _getDouble(attributes, "y", 0.0);           
        try {
            Location location = new Location(attribute, "_location");
            location.setLocation(locationValue);
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Update the contents of the icon based on the SVG data
     *  in the associated "_iconDescription" parameter, if there is one.
     */
    private void _updateContents() throws Exception {
        if (_description == null) {
            return;
        }
        String text = _description.value();
        Reader in = new StringReader(text);
        XmlDocument document = new XmlDocument((URL)null);
        XmlReader reader = new XmlReader();
        reader.parse(document, in);
        XmlElement root = document.getRoot();

        _generateContents(root);
        
        // FIXME: What to do about the _smallIconDescription?
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The description of this icon in XML.
    private ConfigurableAttribute _description;

    // The description of the small version of the icon in XML.
    private ConfigurableAttribute _smallIconDescription;
}
