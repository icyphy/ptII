/* An icon that renders the value of the container.

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

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.swing.SwingConstants;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicEllipse;
import diva.canvas.toolbox.LabelFigure;

//////////////////////////////////////////////////////////////////////////
//// ValueIcon
/**
An icon that displays a bullet, the name, and, if the container is
an instance of settable, its value.

@author Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ValueIcon extends XMLIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     */
    public ValueIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a background figure based on this icon, which is a text
     *  element with the name of the container, a colon, and its value.
     *  @return A figure for this icon.
     */
    public Figure createBackgroundFigure() {
        return createFigure();
    }

    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of LabelFigure that renders the
     *  container name and value, separated by a colon.
     *  @return A new CompositeFigure consisting of the label.
     */
    public Figure createFigure() {
        CompositeFigure background = new CompositeFigure(
                super.createBackgroundFigure());
        Nameable container = getContainer();
        if (container instanceof Settable) {
            String name = container.getName();
            String value = ((Settable)container).getExpression();
            LabelFigure label = new LabelFigure(name + ": " + value,
                    _labelFont, 1.0, SwingConstants.SOUTH_WEST);
            background.add(label);
            return background;
        } else {
            String name = container.getName();
            LabelFigure label = new LabelFigure(name,
                    _labelFont, 1.0, SwingConstants.SOUTH_WEST);
            background.add(label);
            return background;
        }
    }

    /** Write a MoML description of this object, unless the object is
     *  non-persistent.
     *  MoML is an XML modeling markup language.
     *  In this class, the object is identified by the "property"
     *  element, with "name" and "class" (XML) attributes.
     *  The body of the element, between the "&lt;property&gt;"
     *  and "&lt;/property&gt;", is written using
     *  the _exportMoMLContents() protected method, so that derived classes
     *  can override that method alone to alter only how the contents
     *  of this object are described.
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline.  Since the value of this attribute
     *  simply mirrors that of the container, the value field is not
     *  exported.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     *  @see #isPersistent()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {

        if (!isPersistent() || isClassElement()) {
            return;
        }
        output.write(_getIndentPrefix(depth)
                + "<"
                + getMoMLInfo().elementName
                + " name=\""
                + name
                + "\" class=\""
                + getMoMLInfo().className
                + "\">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</"
                + getMoMLInfo().elementName + ">\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new default background figure, which is a bullet.
     *  @return A figure representing a bullet.
     */
    protected Figure _createDefaultBackgroundFigure() {
        Color color = Color.black;
        List colorAttributes = attributeList(ColorAttribute.class);
        if (colorAttributes.size() > 0) {
            ColorAttribute colorAttribute = (ColorAttribute)colorAttributes.get(0);
            color = colorAttribute.asColor();
        }
        return new BasicEllipse(-10, -6, 6, 6, color, 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);
}
