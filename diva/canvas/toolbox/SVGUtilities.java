/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.toolbox;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;


/** A collection of utility functions to aid in figures from SVG,
 * the Scalable Vector Graphics language.
 *  For a description of SVG see <a href="http://www.w3.org/TR/SVG/">the
 *  specification</a>.
 *
 * @version        $Revision$
 * @author         John Reekie, Steve Neuendorffer
 */
public class SVGUtilities {

    /** A map containing a mapping from color names to color values
     */
    private final static HashMap colors = new HashMap();

    /** The base set of colors
     */
    static {
        colors.put("black", Color.black);
        colors.put("blue", Color.blue);
        colors.put("cyan", Color.cyan);
        colors.put("darkgray", Color.darkGray);
        colors.put("darkgrey", Color.darkGray);
        colors.put("gray", Color.gray);
        colors.put("grey", Color.gray);
        colors.put("green", Color.green);
        colors.put("lightgray", Color.lightGray);
        colors.put("lightgrey", Color.lightGray);
        colors.put("magenta", Color.magenta);
        colors.put("orange", Color.orange);
        colors.put("pink", Color.pink);
        colors.put("red", Color.red);
        colors.put("white", Color.white);
        colors.put("yellow", Color.yellow);
    }

    /** Get an iterator over the known color names
     */
    public static Iterator colorNames () {
        return colors.keySet().iterator();
    }

    /** Given a string representing a color, return a color that
     * represents it. The string argument can be a hexadecimal or
     * octal number, or a color name. If a color name, then it is first
     * looked up in the default color table. If not found there, then
     * it is looked up in the System properties. If after all that a
     * color is not found, then return black.
     */
    public static Color getColor (String name) {
        Color color = null;
        name = name.toLowerCase();
        char first = name.charAt(0);
        if (first == '#' || first == '0') {
            color = Color.decode(name);
        } else {
            color = (Color) colors.get(name);
            if (color == null) {
                color = Color.getColor(name);
            }
        }
        if (color == null) {
            color = Color.black;
        }
        return color;
    }

    /** Given a style string, parse it into a map of elements. Here is what
     * an example string might look like: "font: ariel; stroke: red; stroke-width: 2".
     * See the SVG specification for more.
     */
    public static Map parseStyleString (String style) {
        Map map = new HashMap();
        int sep = style.indexOf(';');
        String name, value;

        while (sep > 0) {
            int delim = style.indexOf(sep, ':');
            if (delim > 0) {
                name = style.substring(0, sep).trim();
                value = style.substring(sep+1, delim).trim();
                map.put(name, value);
            }
            style = style.substring(delim+1);
            sep = style.indexOf(';');
        }
        return map;
    }

    /** Parse a string of numbers into an array of double.  The doubles
     *  can be delimited by commas and spaces.
     *
     * <P>FIXME this is not correct
     */
    public static double[] parseCoordString (String s) {
        double result[] = new double[4];
        int i = 0;
        StringTokenizer t = new StringTokenizer(s, " ,");
        while (t.hasMoreTokens()) {
            String string = t.nextToken();
            // Ignore consecutive delimiters.
            if (string != "") {
                result[i++] = Double.parseDouble(string);
                if (i == result.length) {
                    double temp[] = new double[2*result.length];
                    System.arraycopy(result, 0, temp, 0, result.length);
                    result = temp;
                }
            }
        }
        // Yawn! now we have to chop it back to size...
        double temp[] = new double[i];
        System.arraycopy(result, 0, temp, 0, i);
        result = temp;

        // Return it
        return result;
    }
}
