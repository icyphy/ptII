/* A GraphicElement is an atomic piece of a graphical representation.

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
package ptolemy.vergil.toolbox;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import diva.canvas.toolbox.GraphicsParser;
import diva.util.java2d.PaintedObject;
import diva.util.java2d.PaintedString;
import diva.util.xml.XmlElement;

//////////////////////////////////////////////////////////////////////////
//// GraphicElement

/**
 An GraphicElement is an atomic piece of a graphical representation.
 i.e. a line, box, textbox, etc.

 @author Steve Neuendorffer, John Reekie
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (johnr)
 */
public class GraphicElement {
    /**
     * Create a new GraphicElement with the given type.
     * By default, the GraphicElement contains no attributes and an empty
     * label
     * @param type The type.
     */
    public GraphicElement(String type) {
        _attributes = new HashMap();
        _type = type;
        _label = "";
    }

    /**
     * Return a set of all the attribute names, where each element of
     * the set is a String.
     * @return The set of all attribute names
     */
    public Set attributeNameSet() {
        return _attributes.keySet();
    }

    /**
     * Write the GraphicElement in XML format to the given writer.
     * @param out The writer.
     * @param prefix The prefix, usually a string of spaces.
     * @exception IOException If there is a problem writing the MoML.
     */
    public void exportMoML(Writer out, String prefix) throws IOException {
        XmlElement element = new XmlElement(_type, _attributes);
        element.setPCData(_label);
        element.writeXML(out, prefix);
    }

    /** Return the value of the attribute with the given name.
     *  Throw an exception if there is no attribute with the
     *  given name in this schematic.
     *  @param name The name of the attribute.
     *  @return The value of the attribute with the given name.
     *  @see #setAttribute(String, String)
     */
    public String getAttribute(String name) {
        return (String) _attributes.get(name);
    }

    /**
     * Return the label of this graphic element. This is
     * primarily useful for textual elements, but may be used for other
     * objects that have a label.
     * @return The label.
     * @see #setLabel(String)
     */
    public String getLabel() {
        return _label;
    }

    /**
     * Return a new painted object that looks like this graphic element.
     * If the attributes are not consistent, or another error occurs, then
     * return a painted string containing "Error!".
     * @return The painted object.
     */
    public PaintedObject getPaintedObject() {
        String type = getType();
        String label = getLabel();
        PaintedObject paintedObject = GraphicsParser.createPaintedObject(type,
                _attributes, label);

        if (paintedObject == null) {
            return GraphicElement._errorObject;
        }

        return paintedObject;
    }

    /**
     * Return the type of this graphic element.
     * The type is immutably set when the element is created.
     * @return The type.
     */
    public String getType() {
        return _type;
    }

    /**
     * Test if this element has an attribute with the given name.
     * @param name The name.
     * @return true if this element contains an attribute with the given
     * name.
     */
    public boolean containsAttribute(String name) {
        return _attributes.containsKey(name);
    }

    /**
     * Remove an attribute from this element.
     * @param name The name of the attribute to remove
     */
    public void removeAttribute(String name) {
        _attributes.remove(name);
    }

    /**
     * Set the attribute with the given name to the given value.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * @see #getAttribute(String)
     */
    public void setAttribute(String name, String value) {
        _attributes.put(name, value);
    }

    /**
     * Set the label for this graphic element.
     * @param name The name.
     * @see #getLabel()
     */
    public void setLabel(String name) {
        _label = name;
    }

    /**
     * Return a string this representing this GraphicElement.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("{");
        result.append(getClass().getName() + " {" + _type + "}"
                + " attributes {");

        Set attributeSet = attributeNameSet();
        Iterator names = attributeSet.iterator();

        while (names.hasNext()) {
            String p = (String) names.next();
            result.append(" {" + p + "=" + getAttribute(p) + "}");
        }

        result.append("} label {" + getLabel() + "}}");

        return result.toString();
    }

    // The painted object that is returned if an error occurs.
    private static final PaintedString _errorObject = new PaintedString(
            "ERROR!");

    // The attributes of this graphic element.
    private Map _attributes;

    // The type of this graphic element.
    private String _type;

    // The label of this graphic element.
    private String _label;
}
