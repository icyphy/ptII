/* An Icon is the graphical representation of a schematic entity.

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

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.Configurable;
import ptolemy.moml.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.net.URL;
import java.io.*;
import collections.*;
import ptolemy.schematic.xml.XMLElement;
import diva.canvas.Figure;
import diva.canvas.toolbox.*;

//////////////////////////////////////////////////////////////////////////
//// XMLIcon
/**

An icon is the graphical representation of a schematic entity.
Icons are stored hierarchically in icon libraries.   Every icon has a 
name, along with a graphical representation.

This icon is for those based on XML.  If the icon is never configured, then
it will have a default figure.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class XMLIcon extends Icon implements Configurable {

    /**
     * Create a new icon with the name "Icon" in the given container.
     * By default, the icon contains no graphic
     * representations.
     */
    public XMLIcon (Entity container) 
        throws NameDuplicationException, IllegalActionException {
        super(container, "_icon");
        _graphics = (CircularList) new CircularList();
    }

   /**
     * Add a new graphic element to the icon. 
     */
    public void addGraphicElement (GraphicElement g) 
            throws IllegalActionException {
        _graphics.insertLast(g);
    }

    /** Configure the object with data from the specified input stream.
     *  This method is defined to throw a very general exception to allow
     *  classes that implement the interface to use whatever exceptions
     *  are appropriate.
     *  @param base The base relative to which references within the input
     *   stream are found, or null if this is not known.
     *  @param in InputStream
     *  @exception Exception If the stream cannot be read or its syntax
     *   is incorrect.
     */
    public void configure(URL base, InputStream in) throws Exception {
        // Do nothing for right now.
    }

    /**
     * Test if this icon contains a graphic in the
     * given format.
     */
    public boolean containsGraphicElement (GraphicElement g) {
        return _graphics.includes(g);
    }

    /**
     * Create a figure based on this icon.
     */
    public Figure createFigure() {
        Enumeration graphics = graphicElements();
        PaintedFigure figure = new PaintedFigure();
        while(graphics.hasMoreElements()) {
            GraphicElement element = (GraphicElement) graphics.nextElement();
            figure.add(element.getPaintedObject());
        }
        return figure;
    }

    /**
     * Return an enumeration over the names of the graphics formats
     * supported by this icon.
     *
     * @return Enumeration of String.
     */
    public Enumeration graphicElements() {
        return _graphics.elements();
    }

    /**
     * Remove a graphic element from the icon. Throw an exception if
     * the graphic element is not contained in this icon
     */
    public void removeGraphicElement (GraphicElement g)
            throws IllegalActionException {
        try {
            _graphics.removeOneOf(g);
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("removeGraphicElement:" +
                    "GraphicElement not found in icon.");
        }
    }
    
    /**
     * Return a string this representing Icon.
     */
    public String toString() {
        Enumeration els = graphicElements();
        String str = super.toString() + "(";
        while(els.hasMoreElements()) {
            GraphicElement g = (GraphicElement) els.nextElement();
            str += "\n...." + g.toString();
        }
        return str + ")";
    }

    /** Return a description of the object.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        String result = "";
        if(bracket == 0) 
            result += super._description(detail, indent, 0);
        else 
            result += super._description(detail, indent, 1);
	result += " graphics {\n";
	Enumeration graphicElements = graphicElements();
        while (graphicElements.hasMoreElements()) { 
            GraphicElement p = (GraphicElement) graphicElements.nextElement();
            result +=  _getIndentPrefix(indent + 1) + p.toString() + "\n";
        }
	
        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    private CircularList _graphics;
}

