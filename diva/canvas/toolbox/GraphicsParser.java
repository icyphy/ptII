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

import diva.util.java2d.PaintedObject;
import diva.util.java2d.PaintedPath;
import diva.util.java2d.PaintedShape;
import diva.util.java2d.PaintedString;
import diva.util.java2d.Polygon2D;
import diva.util.java2d.Polyline2D;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;


/** A collection of utilities to help parsing graphics from strings
 * and other kinds of external storage.
 *
 * @version        $Id$
 * @author         John Reekie
 * @deprecated  Will be removed in Diva 0.4. Use diva.compat.canvas if
 *              needed, or diva.canvas.toolbox.VectorFigureBuilder.
 */
public class GraphicsParser {
    /** Create a new painted object. The element is parsed from
     * two strings, the first being a representation of the element
     * type, and the second being an XML-style attribute string.
     * Any attributes that are not recognized will be ignored.
     * Legal types and their attributes are:
     *
     * <ul>
     * <li> <b>line</b>
     *      <b>coords</b>=<i>vertex-list</i>
     *      <b>fill</b>=<i>color</i>
     *      <b>width</b>=<i>integer</i>
     * <li> <b>rectangle</b>
     *      <b>coords</b>=<i>x y width height</i>
     *      <b>fill</b>=<i>color</i>
     *      <b>outline</b>=<i>color</i>
     *      <b>width</b>=<i>integer</i>
     * <li> <b>ellipse</b>
     *      <b>coords</b>=<i>x y width height</i>
     *      <b>fill</b>=<i>color</i>
     *      <b>outline</b>=<i>color</i>
     *      <b>width</b>=<i>integer</i>
     * <li> <b>polygon</b>
     *      <b>coords</b>=<i>vertex-list</i>
     *      <b>fill</b>=<i>color</i>
     *      <b>outline</b>=<i>color</i>
     *      <b>width</b>=<i>integer</i>
     * <li> <b>text</b>
     *      <b>coords</b>=<i>x y</i>
     *      <b>fill</b>=<i>color</i>
     *      <b>font</b>=<i>font-name</i>
     *      <b>size</b>=<i>int</i>
     *      <b>style</b>=<b>bold</b>|<b>italic</b>|<b>plain</b>
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
        double[] coords = parseCoordString((String) attributes.get("coords"));

        if (type.equals("rectangle")) {
            PaintedShape ps = new PaintedShape(new Rectangle2D.Double(
                        coords[0], coords[1], coords[2], coords[3]));
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("ellipse")) {
            PaintedShape ps = new PaintedShape(new Ellipse2D.Double(coords[0],
                        coords[1], coords[2], coords[3]));
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("polygon")) {
            Polygon2D poly = new Polygon2D.Double();
            poly.moveTo(coords[0], coords[1]);

            for (int i = 2; i < coords.length; i += 2) {
                poly.lineTo(coords[i], coords[i + 1]);
            }

            poly.closePath();

            PaintedShape ps = new PaintedShape(poly);
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("line")) {
            Shape s;

            if (coords.length == 4) {
                s = new Line2D.Double(coords[0], coords[1], coords[2], coords[3]);
            } else {
                Polyline2D poly = new Polyline2D.Double();
                poly.moveTo(coords[0], coords[1]);

                for (int i = 2; i < coords.length; i += 2) {
                    poly.lineTo(coords[i], coords[i + 1]);
                }

                s = poly;
            }

            PaintedPath pp = new PaintedPath(s);
            processPaintedPathAttributes(pp, attributes);
            return pp;
        } else if (type.equals("text")) {
            PaintedString string = new PaintedString(content);
            processPaintedStringAttributes(string, attributes);
            string.translate(coords[0], coords[1]);
            return string;
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
        double[] coords = parseCoordString((String) attributes.get("coords"));

        if (type.equals("rectangle")) {
            PaintedShape ps = new PaintedShape(new Rectangle2D.Double(
                        coords[0], coords[1], coords[2], coords[3]));
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("ellipse")) {
            PaintedShape ps = new PaintedShape(new Ellipse2D.Double(coords[0],
                        coords[1], coords[2], coords[3]));
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("polygon")) {
            Polygon2D poly = new Polygon2D.Double();
            poly.moveTo(coords[0], coords[1]);

            for (int i = 2; i < coords.length; i += 2) {
                poly.lineTo(coords[i], coords[i + 1]);
            }

            poly.closePath();

            PaintedShape ps = new PaintedShape(poly);
            processPaintedShapeAttributes(ps, attributes);
            return ps;
        } else if (type.equals("line")) {
            Shape s;

            if (coords.length == 4) {
                s = new Line2D.Double(coords[0], coords[1], coords[2], coords[3]);
            } else {
                Polyline2D poly = new Polyline2D.Double();
                poly.moveTo(coords[0], coords[1]);

                for (int i = 2; i < coords.length; i += 2) {
                    poly.lineTo(coords[i], coords[i + 1]);
                }

                s = poly;
            }

            PaintedPath pp = new PaintedPath(s);
            processPaintedPathAttributes(pp, attributes);
            return pp;
        }

        return null;
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
            return Color.black;
        }

        //          Color c = Color.getColor(s);
        //          if (c == null) {
        //              try {
        //                  c = Color.decode(s);
        //              }
        //              catch (Exception e) {}
        //          }
        //          if (c == null) {
        //              c = Color.black;
        //          }
        //          return c;
    }

    /** Parse a string of numbers into an array of double
     */
    private static double[] parseCoordString(String s) {
        double[] result = new double[4];
        int i = 0;
        StringTokenizer t = new StringTokenizer(s);

        while (t.hasMoreTokens()) {
            result[i++] = Double.parseDouble(t.nextToken());

            if (i == result.length) {
                double[] temp = new double[2 * result.length];
                System.arraycopy(result, 0, temp, 0, result.length);
                result = temp;
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
        Iterator i = attributes.keySet().iterator();

        while (i.hasNext()) {
            String key = (String) i.next();
            String val = (String) attributes.get(key);

            ////System.out.println(key + "=" + val);
            if (key.equals("fill")) {
                ////System.out.println(lookupColor(val));
                ps.fillPaint = lookupColor(val);
            } else if (key.equals("outline")) {
                ps.strokePaint = lookupColor(val);
            } else if (key.equals("width")) {
                ps.setLineWidth(Float.parseFloat(val));
            }
        }
    }

    /** Set the attributes of a PaintedPath from a hash-table
     */
    private static void processPaintedPathAttributes(PaintedPath pp,
        Map attributes) {
        Iterator i = attributes.keySet().iterator();

        while (i.hasNext()) {
            String key = (String) i.next();
            String val = (String) attributes.get(key);

            if (key.equals("fill")) {
                pp.strokePaint = lookupColor(val);
            } else if (key.equals("width")) {
                pp.setLineWidth(Float.parseFloat(val));
            }
        }
    }

    /** Set the attributes of a PaintedString from a hash-table
     */
    private static void processPaintedStringAttributes(PaintedString pp,
        Map attributes) {
        Iterator i = attributes.keySet().iterator();

        while (i.hasNext()) {
            String key = (String) i.next();
            String val = (String) attributes.get(key);

            if (key.equals("font")) {
                pp.setFontName(val);
            } else if (key.equals("size")) {
                pp.setSize(Integer.parseInt(val));
            }
        }
    }
}
