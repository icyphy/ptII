/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
  *
  */
package diva.canvas.toolbox;

import diva.util.java2d.PaintedImage;
import diva.util.java2d.PaintedList;
import diva.util.java2d.PaintedObject;
import diva.util.java2d.PaintedPath;
import diva.util.java2d.PaintedShape;
import diva.util.java2d.PaintedString;
import diva.util.java2d.Polygon2D;
import diva.util.java2d.Polyline2D;
import diva.util.xml.XmlElement;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;


/** A collection of utilities to help parse graphics out of SVG files.
 *  For a description of SVG see <a href="http://www.w3.org/TR/SVG/">the
 *  specification</a>.
 *
 * @version        $Id$
 * @author         John Reekie, Steve Neuendorffer
 */
public class SVGParser {
    /** Return a list of basic color names that are understood.
     *  Note that colors can also be specified using #rrggbb notation,
     *  where rr is the hex representation of the red component, etc.
     *  This method can be used to construct a dialog listing available
     *  colors symbolically by name.
     *  @return An array of symbolic color names.
     */
    public static String[] colorNames() {
        String[] result = {
            "black",
            "blue",
            "cyan",
            "darkgray",
            "gray",
            "green",
            "lightgray",
            "magenta",
            "orange",
            "pink",
            "red",
            "white",
            "yellow"
        };
        return result;
    }

    /** Create a new painted object. The element is parsed from
     * two strings, the first being a representation of the element
     * type, and the second being an XML-style attribute string.
     * Any attributes that are not recognized will be ignored.
     * Legal types and their attributes are:
     *
     * </ul>
     */
    public static PaintedObject createPaintedObject(String type,
            String attributes, String content) {
        Map hm = new HashMap();
        hashAttributes(attributes, hm);
        return createPaintedObject(type, hm, content);
    }

    /** Create a new painted object. The first argument is a string
     * representation of the element type, and the second is a
     * hash-table containing attributes of the object.  Any attributes
     * that are not recognized will be ignored.  See the add(String,
     * String) for a description of legal types and their attributes.
     *
     */
    public static PaintedObject createPaintedObject(String type,
            Map attributes, String content) {
        if (type.equals("rect")) {
            double x;
            double y;
            double width;
            double height;
            x = _getDouble(attributes, "x", 0);
            y = _getDouble(attributes, "y", 0);
            width = _getDouble(attributes, "width");
            height = _getDouble(attributes, "height");

            PaintedShape ps = new PaintedShape(new Rectangle2D.Double(x, y,
                                                       width, height));
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("circle")) {
            double cx;
            double cy;
            double r;
            cx = _getDouble(attributes, "cx", 0);
            cy = _getDouble(attributes, "cy", 0);
            r = _getDouble(attributes, "r");

            PaintedShape ps = new PaintedShape(new Ellipse2D.Double(cx - r,
                                                       cy - r, 2 * r, 2 * r));
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("ellipse")) {
            double cx;
            double cy;
            double rx;
            double ry;
            cx = _getDouble(attributes, "cx", 0);
            cy = _getDouble(attributes, "cy", 0);
            rx = _getDouble(attributes, "rx");
            ry = _getDouble(attributes, "ry");

            PaintedShape ps = new PaintedShape(new Ellipse2D.Double(cx - rx,
                                                       cy - ry, 2 * rx, 2 * ry));
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("line")) {
            double x1;
            double y1;
            double x2;
            double y2;
            x1 = _getDouble(attributes, "x1", 0);
            y1 = _getDouble(attributes, "y1", 0);
            x2 = _getDouble(attributes, "x2", 0);
            y2 = _getDouble(attributes, "y2", 0);

            Line2D line = new Line2D.Double(x1, y1, x2, y2);
            PaintedPath pp = new PaintedPath(line);
            processPaintedPathAttributes(pp, attributes);
            return pp;
        } else if (type.equals("polyline")) {
            double[] coords = parseCoordString((String) attributes.get("points"));
            Polyline2D poly = new Polyline2D.Double();
            poly.moveTo(coords[0], coords[1]);

            for (int i = 2; i < coords.length; i += 2) {
                poly.lineTo(coords[i], coords[i + 1]);
            }

            PaintedPath pp = new PaintedPath(poly);
            processPaintedPathAttributes(pp, attributes);
            return pp;
        } else if (type.equals("polygon")) {
            double[] coords = parseCoordString((String) attributes.get("points"));
            Polygon2D poly = new Polygon2D.Double();
            poly.moveTo(coords[0], coords[1]);

            for (int i = 2; i < coords.length; i += 2) {
                poly.lineTo(coords[i], coords[i + 1]);
            }

            poly.closePath();

            PaintedShape ps = new PaintedShape(poly);
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("text")) {
            double x;
            double y;
            x = _getDouble(attributes, "x", 0);
            y = _getDouble(attributes, "y", 0);

            PaintedString string = new PaintedString(content);
            processPaintedStringAttributes(string, attributes);
            string.translate(x, y);
            return string;
        } else if (type.equals("image")) {
            double x;
            double y;
            double width;
            double height;
            x = _getDouble(attributes, "x", 0);
            y = _getDouble(attributes, "y", 0);
            width = _getDouble(attributes, "width");
            height = _getDouble(attributes, "height");

            Rectangle2D bounds = new Rectangle2D.Double(x, y, width, height);
            String link = (String) attributes.get("xlink:href");

            // First try as a system resource.
            URL url = ClassLoader.getSystemResource(link);

            try {
                if (url == null) {
                    // Web Start needs this.
                    if (_refClass == null) {
                        try {
                            _refClass = Class.forName(
                                    "diva.canvas.toolbox.SVGParser");
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException("Could not find "
                                    + "diva.canvas.toolbox.SVGParser");
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

                    if ((bitflags & (ImageObserver.ABORT | ImageObserver.ERROR)) != 0) {
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
        }

        return null;
    }

    /** Create a new painted object. The first argument is a string
     * representation of the element type, and the second is a
     * hash-table containing attributes of the object.  Any attributes
     * that are not recognized will be ignored.  See the add(String,
     * String) for a description of legal types and their attributes.
     */
    public static PaintedObject createPaintedObject(String type, Map attributes) {
        return createPaintedObject(type, attributes, null);
    }

    /** Given a XML-style attribute string and a hash-table, add
     * each attribute and its value to the table.
     *
     * <P> FIXME: this sucks.
     */
    public static void hashAttributes(String s, Map map) {
        StreamTokenizer t = new StreamTokenizer(new StringReader(s));
        t.whitespaceChars('=', '=');
        t.ordinaryChars('0', '9');
        t.ordinaryChar('.');

        String key = "Unknown";
        String val = "Unknown";

        while (true) {
            int ttype = 0;

            try {
                ttype = t.nextToken();
            } catch (Exception e) {
            }

            if (ttype == StreamTokenizer.TT_EOF) {
                break;
            }

            switch (ttype) {
            case StreamTokenizer.TT_WORD:
                key = t.sval;
                break;
            }

            try {
                ttype = t.nextToken();
            } catch (Exception e) {
            }

            if (ttype == StreamTokenizer.TT_EOF) {
                break;
            }

            switch (ttype) {
            case StreamTokenizer.TT_WORD:
                val = t.sval;
                break;
            }

            ////System.out.println(key + "=" + val);
            map.put(key, val);
        }
    }

    /** Given a string, return a color.
     */
    private static Color lookupColor(String color) {
        String s = color.toLowerCase();

        if (s.equals("black")) {
            return Color.black;
        } else if (s.equals("blue")) {
            return Color.blue;
        } else if (s.equals("cyan")) {
            return Color.cyan;
        } else if (s.equals("darkgray")) {
            return Color.darkGray;
        } else if (s.equals("darkgrey")) {
            return Color.darkGray;
        } else if (s.equals("gray")) {
            return Color.gray;
        } else if (s.equals("grey")) {
            return Color.gray;
        } else if (s.equals("green")) {
            return Color.green;
        } else if (s.equals("lightgray")) {
            return Color.lightGray;
        } else if (s.equals("lightgrey")) {
            return Color.lightGray;
        } else if (s.equals("magenta")) {
            return Color.magenta;
        } else if (s.equals("orange")) {
            return Color.orange;
        } else if (s.equals("pink")) {
            return Color.pink;
        } else if (s.equals("red")) {
            return Color.red;
        } else if (s.equals("white")) {
            return Color.white;
        } else if (s.equals("yellow")) {
            return Color.yellow;
        } else {
            Color c = Color.getColor(s);

            if (c == null) {
                try {
                    c = Color.decode(s);
                } catch (Exception e) {
                }
            }

            if (c == null) {
                c = Color.black;
            }

            return c;
        }
    }

    /** Parse a string of numbers into an array of double.  The doubles
     *  can be delimited by commas and spaces.
     */
    private static double[] parseCoordString(String s) {
        double[] result = new double[4];
        int i = 0;
        StringTokenizer t = new StringTokenizer(s, " ,");

        while (t.hasMoreTokens()) {
            String string = t.nextToken();

            // Ignore consecutive delimiters.
            if (string != "") {
                result[i++] = Double.parseDouble(string);

                if (i == result.length) {
                    double[] temp = new double[2 * result.length];
                    System.arraycopy(result, 0, temp, 0, result.length);
                    result = temp;
                }
            }
        }

        // Yawn! now we have to chop it back to size...
        double[] temp = new double[i];
        System.arraycopy(result, 0, temp, 0, i);
        result = temp;

        // Return it
        return result;
    }

    /** Set the attributes of a PaintedShape from a hash-table
     */
    private static void processPaintedShapeAttributes(PaintedShape ps,
            Map attributes) {
        String style = (String) attributes.get("style");

        if (style != null) {
            StringTokenizer t = new StringTokenizer(style, ";");

            while (t.hasMoreTokens()) {
                String string = t.nextToken().trim();
                int index = string.indexOf(":");
                String name = string.substring(0, index);
                String value = string.substring(index + 1);

                if (name.equals("fill")) {
                    ps.fillPaint = lookupColor(value);
                } else if (name.equals("stroke")) {
                    ps.strokePaint = lookupColor(value);
                } else if (name.equals("stroke-width")) {
                    ps.setLineWidth(Float.parseFloat(value));
                }
            }
        }
    }

    /** Set the attributes of a PaintedPath from a hash-table
     */
    private static void processPaintedPathAttributes(PaintedPath pp,
            Map attributes) {
        String style = (String) attributes.get("style");

        if (style != null) {
            StringTokenizer t = new StringTokenizer(style, ";");

            while (t.hasMoreTokens()) {
                String string = t.nextToken().trim();
                int index = string.indexOf(":");
                String name = string.substring(0, index);
                String value = string.substring(index + 1);

                if (name.equals("stroke")) {
                    pp.strokePaint = lookupColor(value);
                } else if (name.equals("stroke-width")) {
                    pp.setLineWidth(Float.parseFloat(value));
                }
            }
        }
    }

    /** Set the attributes of a PaintedString from a hash-table
     */
    private static void processPaintedStringAttributes(PaintedString pp,
            Map attributes) {
        String style = (String) attributes.get("style");

        if (style != null) {
            StringTokenizer t = new StringTokenizer(style, ";");

            while (t.hasMoreTokens()) {
                String string = t.nextToken().trim();
                int index = string.indexOf(":");
                String name = string.substring(0, index);
                String value = string.substring(index + 1);

                if (name.equals("font-family")) {
                    pp.setFontName(value);
                } else if (name.equals("font-size")) {
                    pp.setSize(Integer.parseInt(value));
                } else if (name.equals("fill")) {
                    pp.setFillPaint(lookupColor(value));
                }
            }
        }
    }

    public static PaintedList createPaintedList(XmlElement root) {
        PaintedList list = new PaintedList();
        String name = root.getType();

        if (!name.equals("svg")) {
            throw new IllegalArgumentException("Input XML has a root"
                    + "name which is '" + name + "' instead of 'svg':" + root);
        }

        Iterator children = root.elements();

        while (children.hasNext()) {
            XmlElement child = (XmlElement) children.next();
            PaintedObject object = createPaintedObject(child.getType(),
                    child.getAttributeMap(), child.getPCData());

            if (object != null) {
                list.add(object);
            }
        }

        return list;
    }

    private static double _getDouble(Map map, String name, double def) {
        if (map.containsKey(name)) {
            return Double.parseDouble((String) map.get(name));
        } else {
            return def;
        }
    }

    private static double _getDouble(Map map, String name) {
        return Double.parseDouble((String) map.get(name));
    }

    // Reference class used to get resources.
    private static Class _refClass = null;
}
