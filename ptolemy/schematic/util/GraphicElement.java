/* An GraphicElement is an atomic piece of a graphical representation.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.util;

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.*;
import ptolemy.schematic.xml.XMLElement;
import diva.util.java2d.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// GraphicElement
/**
An GraphicElement is an atomic piece of a graphical representation.
i.e. a line, box, textbox, etc.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class GraphicElement extends Object {

    /**
     * Create a new GraphicElement with the given type.
     * By default, the GraphicElement contains no graphic
     * representations.
     * @param attributes a CircularList from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public GraphicElement (String type) {
        _attributes = (LLMap) new LLMap();
        _type = type;
    }

    /**
     * Return an enumeration of all the attributeNames of this graphic element
     */
    public Enumeration attributeNames() {
        return _attributes.keys();
    }
        
    /** 
     * Return the type of this graphic element.  
     * The type is immutably set when the element is created.
     */
    public String getType() {
        return _type;
    }

    /**
     * Return the value of the attribute with the given name.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public String getAttribute (String name) {
        return (String) _attributes.at(name);
    }

    /**
     * Return the color of this element, by turning the color
     * attribute into a brush. If no color attribute exists, then use
     * black.
     */
    public Paint getColor() {
        if(!hasAttribute("color")) {
            return getColorByString("black");
        } else {
            return getColorByString(getAttribute("color"));
        }
    }

    /**
     * Return the value of the content attribute of this graphic element.
     * If no content attribute exists, return an empty string.  This is 
     * primarily useful for textual elements.
     */
    public String getContent() {
        if(!hasAttribute("content")) {
            return new String("");
        } else {
            return getAttribute("content");
        }
    }

    /**
     * Return the fill of this element, by turning the color
     * attribute into a brush. If no fill attribute exists, then use
     * black.
     */
    public Paint getFill() {
        if(!hasAttribute("fill")) {
            return getColorByString("black");
        } else {
            return getColorByString(getAttribute("fill"));
        }
    }

    public Paint getColorByString(String colorName) {
        colorName = colorName.toLowerCase();
        if(colorName.equals("black")) return Color.black;
        if(colorName.equals("blue")) return Color.blue;
        if(colorName.equals("cyan")) return Color.cyan;
        if(colorName.equals("darkgray")) return Color.darkGray;
        if(colorName.equals("gray")) return Color.gray;
        if(colorName.equals("green")) return Color.green;
        if(colorName.equals("lightgray")) return Color.lightGray;
        if(colorName.equals("magenta")) return Color.magenta;
        if(colorName.equals("orange")) return Color.orange;
        if(colorName.equals("pink")) return Color.pink;
        if(colorName.equals("red")) return Color.red;
        if(colorName.equals("white")) return Color.white;
        if(colorName.equals("yellow")) return Color.yellow;
        return Color.black;
    }

    public float getWidth() {
        if(!hasAttribute("width")) return 1;
        DoubleToken token = new DoubleToken(getAttribute("width"));
        return (float)token.doubleValue();
    }

    /**
     * Return the size of the iteration returned by the points method.
     */
    public int numberOfPoints() {
        CircularList doubleList = new CircularList();
        if(!hasAttribute("points")) 
            return 0;
        String pointsAttribute = getAttribute("points");
        StringTokenizer pointsTokens = new StringTokenizer(pointsAttribute);
        return pointsTokens.countTokens();
    }

    /**
     * Return an enumeration of DoubleTokens parsed from the
     * points attribute of this element.  If no points attribute exists,
     * then return an Enumeration with no elements.
     */
    public Enumeration points() {
        CircularList doubleList = new CircularList();
        if(!hasAttribute("points")) 
            return doubleList.elements();
        String pointsAttribute = getAttribute("points");
        StringTokenizer pointsTokens = new StringTokenizer(pointsAttribute);
        while(pointsTokens.hasMoreElements()) {
            String tokenString = (String)pointsTokens.nextElement();
            doubleList.insertLast(new DoubleToken(tokenString));
        }
        return doubleList.elements();
    }
            
    /**
     * Return a painted object that looks like this graphic element
     * [FIXME: cache this]
     * [FIXME: this is ugly..  is there a better way?]
     * [FIXME: not many supported]
     */
    public PaintedObject getPaintedObject() {
        PaintedObject paintedObject;
        if(_type.equals("rect")) {
            int pointsCount = numberOfPoints();
            double pointx, pointy;
            Rectangle2D shape;
            //FIXME if these are illegal, what should we do?
            if(pointsCount < 2) 
                return GraphicElement._errorObject;
            Enumeration allPoints = points();
            pointx = ((DoubleToken) allPoints.nextElement())
                .doubleValue();
            pointy = ((DoubleToken) allPoints.nextElement())
                .doubleValue();
            shape = new Rectangle2D.Double(pointx, pointy, 0, 0);
            while(allPoints.hasMoreElements()) {
                pointx = ((DoubleToken) allPoints.nextElement())
                    .doubleValue();
                if(allPoints.hasMoreElements()) {
                    pointy = ((DoubleToken) allPoints.nextElement())
                        .doubleValue();
                    shape.add(pointx, pointy);
                }
            }
            paintedObject = new PaintedShape(shape, getFill(), getWidth());
        } else if(_type.equals("textline")) {
            paintedObject = new PaintedString(getContent());
            
        } else if(_type.equals("polygon")) {
            int pointsCount = numberOfPoints();
            double pointx, pointy;
            Polygon2D shape;
            if(pointsCount < 2) 
                return GraphicElement._errorObject;
            Enumeration allPoints = points();
            pointx = ((DoubleToken) allPoints.nextElement())
                .doubleValue();
            pointy = ((DoubleToken) allPoints.nextElement())
                .doubleValue();
            shape = new Polygon2D.Double(pointx, pointy);
            while(allPoints.hasMoreElements()) {
                pointx = ((DoubleToken) allPoints.nextElement())
                    .doubleValue();
                if(allPoints.hasMoreElements()) {
                    pointy = ((DoubleToken) allPoints.nextElement())
                        .doubleValue();
                    shape.lineTo(pointx, pointy);
                }
            }
	    paintedObject = new PaintedShape(shape, getFill(), getWidth());
	} else { // if(_type.equals("ellipse")
            //FIXME how do you handle ellipses?
            Rectangle shape = new Rectangle(0, 0, 10, 10);
            paintedObject = new PaintedShape(shape, getFill(), getWidth());
	}
        return paintedObject;
    }
    /**
     * Test if this schematic has the attribute wuth the given name.
     */
    public boolean hasAttribute (String name) {
        return _attributes.includesKey(name);
    }

    /**
     * Remove an attribute from this element
     */
    public void removeAttribute(String name) {
        _attributes.removeAt(name);
    }

   /**
     * Set the attribute with the given name to the given value.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public void setAttribute (String name, String value) {
        _attributes.putAt(name, value);
    }

    /**
     * Return a string this representing Icon.
     */
    public String toString() {
        Enumeration els = attributeNames();
        String str = getType() + "(";
        if(els.hasMoreElements()) {
            String name = (String) els.nextElement();
            str += name + "=" + getAttribute(name);
        }
            
        while(els.hasMoreElements()) {
            String name = (String) els.nextElement();
            str += "," + name + "=" + getAttribute(name);
        }
        return str + ")";
    }

    private static PaintedString _errorObject = new PaintedString("ERROR!");
    
    private LLMap _attributes;
    private String _type;
}

    
