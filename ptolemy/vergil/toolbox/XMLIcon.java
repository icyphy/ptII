/* An icon stored in XML.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import java.util.*;
import java.net.URL;
import java.io.*;
import diva.canvas.Figure;
import diva.canvas.toolbox.*;
import diva.util.xml.*;
import diva.util.java2d.PaintedList;

//////////////////////////////////////////////////////////////////////////
//// XMLIcon
/**
An icon is the graphical representation of a schematic entity.
This icon contains a set of graphic elements.  These graphic elements can
be added manually or created automatically by configuring the icon with
appropriate XML code.  Each graphic element represents a primitive graphical
object that will be used to create a figure or a Swing icon.   If this
icon contains no graphic elements, then the default figure or Swing icon will
be created.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class XMLIcon extends EditorIcon {

    /**
     * Create a new icon with the given name in the given container.
     * By default, the icon contains no graphic
     * representations.
     */
    public XMLIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _paintedList = null;
        _description = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create a background figure based on this icon.  The background figure
     * will be painted with each graphic element that this icon contains.
     */
    public Figure createBackgroundFigure() {
        // Get the description
        // The following code is slightly inefficient because it always adds
        // and removes a listener.  However, the overhead is small compared
        // to the other overhead involved here, so I'm going to keep the code
        // simple.
        NamedObj container = (NamedObj)getContainer();
        if(_description != null) {
            // FIXME  This listener should set _paintedList to null.
            //        _description.removeValueListener(_updateListener);
        }
        SingletonConfigurableAttribute description =
            (SingletonConfigurableAttribute)container.getAttribute(
                    "_iconDescription");
        if(_description != description) {
            _description = description;
            try {
                _updatePaintedList();
            } catch (Exception ex) {
                ex.printStackTrace();
                _paintedList = null;
            }
        }
        if(_description != null) {
            // FIXME
            // _description.addValueListener(_updateListener);
        }

        if(_paintedList == null) {
       	    return _createDefaultBackgroundFigure();
        } else {
            return new PaintedFigure(_paintedList);
        }
    }

    /**
     * Return the painted list
     * contained by this icon.
     */
    public PaintedList paintedList() {
        return _paintedList;
    }

    /**
     * Return a string this representing Icon.
     */
    public String toString() {
        String str = super.toString() + "(";

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
	result += "FIXME";
        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    /** Configure the icon with XML data.  The XML data is given by
     *  either a URL (the <i>source</i> argument), or by text
     *  (the <i>text</i> argument), or both.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If the stream cannot be read or its syntax
     *   is incorrect.
     */
    private void _updatePaintedList() throws Exception {
        // create a new list because the PaintedList we had before
        // was used to create some PaintedFigures already.
        // FIXME: test for 'svg' processing instruction
        if(_description == null) {
            _paintedList = null;
            return;
        }
        String text = _description.value();

   	Reader in = new StringReader(text);
        // FIXME: Do we need a base here?
	XmlDocument document = new XmlDocument((URL)null);
	XmlReader reader = new XmlReader();
	reader.parse(document, in);
	XmlElement root = document.getRoot();

        _paintedList = SVGParser.createPaintedList(root);
    }

    // The list of painted objects contained in this icon.
    private PaintedList _paintedList;

    // The description of this icon in XML.
    private ConfigurableAttribute _description;
}
