/* An icon that renders the value of the container.

 Copyright (c) 1999-2002 The Regents of the University of California.
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

import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import ptolemy.kernel.util.*;
import ptolemy.kernel.util.NamedObj.*;
import ptolemy.kernel.util.Settable.*;

import javax.swing.SwingConstants;
import java.awt.Font;
import java.io.IOException;
import java.io.Writer;

//////////////////////////////////////////////////////////////////////////
//// ValueIcon
/**
An icon that displays the value of the container, which is assumed
to be an instance of Settable.  This attribute also implements Settable,
but when setExpression() is called, it simply delegates to the container.
Similarly, when getExpression() is called, it gets the expression from
the container. Moreover, when this attribute is given a container,
it attempts to give itself the same name as the container, and when its
name is requested, it first tries to ensure that the name matches that
of the container.  This creates the effect that editing the parameters
of the container (which include this attribute), sets the value of the
container (via the setExpression() method of this attribute).

@author Edward A. Lee
@version $Id$
*/
public class ValueIcon extends XMLIcon implements Settable {

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

    /** Add a listener to be notified when the value of this settable
     *  object changes. This implementation delegates to the container,
     *  if there is one.
     *  @param listener The listener to add.
     */
    public void addValueListener(ValueListener listener) {
        Settable container = (Settable)getContainer();
        if (container != null) {
            container.addValueListener(listener);
        }
    }

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
        Settable container = (Settable)getContainer();
        String name = container.getName();
        String value = container.getExpression();
        LabelFigure label = new LabelFigure(name + ": " + value,
                _labelFont, 1.0, SwingConstants.SOUTH_WEST);
        return label;
    }

    /** Write a MoML description of this object.
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
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {

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

    /** Get the value of the attribute that has been set by setExpression(),
     *  or null if there is none. This implementation delegates to the
     *  container, if there is one, and returns null otherwise.
     *  @return The expression.
     */
    public String getExpression() {
        Settable container = (Settable)getContainer();
        if (container != null) {
            return container.getExpression();
        } else {
            return null;
        }
    }

    /** Get the name. This method may have the side effect of changing
     *  the name of this attribute to match that of the container.
     *  @return The name of the object.
     */
    public String getName() {
        Nameable container = getContainer();
        if (container != null && !_settingName) {
            try {
                // Try to set the name of this to match the container.
                // This apparently results in a call to getName(), so we
                // use the variable _settingName to prevent infinite loop.
                _settingName = true;
                setName(container.getName());
            } catch (KernelException ex) {
                // Ignore and leave name as is.
            } finally {
                _settingName = false;
            }
        }
        return super.getName();
    }

    /** Get the visibility of this Settable, as set by setVisibility().
     *  If setVisibility() has not been called, then return FULL.
     *  @return The visibility of this Settable.
     */
    public Settable.Visibility getVisibility() {
        return _visibility;
    }

    /** Remove a listener from the list of listeners that are
     *  notified when the value of this variable changes.  If no such listener
     *  exists, do nothing. This implementation delegates to the container,
     *  if there is one.
     *  @param listener The listener to remove.
     */
    public void removeValueListener(ValueListener listener) {
        Settable container = (Settable)getContainer();
        if (container != null) {
            container.removeValueListener(listener);
        }
    }

    /** Specify the container, which is required to be an instance
     *  of Settable.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute does not
     *   implement Settable, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof Settable)) {
            throw new IllegalActionException(this, container,
                    "Cannot put a ValueAttribute in something that "
                    + "is not Settable.");
        }
        super.setContainer(container);
        try {
            // Try to set the name of this to match the container.
            setName(container.getName());
        } catch (NameDuplicationException ex) {
            // Ignore and leave name as is.
        }
    }

    /** Set the value of the attribute by giving some expression.
     *  This implementation delegates to the container, if there
     *  is one, and returns null otherwise.
     *  @return The expression.
     */
    public void setExpression(String expression) throws IllegalActionException {
        Settable container = (Settable)getContainer();
        if (container != null) {
            container.setExpression(expression);
        }
    }

    /** Set the visibility of this Settable.  The argument should be one
     *  of the static public instances of the inner class Visibility.
     *  If this method is not called, then the visibility will be FULL.
     *  @param visibility The visibility of this Settable.
     */
    public void setVisibility(Settable.Visibility visibility) {
        _visibility = visibility;
    }

    /** Check the validity of the expression set in setExpression().
     *  This implementation delegates to the container, if there is one.
     *  @exception IllegalActionException If the expression is not valid, or
     *   its value is not acceptable to the container or the listeners.
     */
    public void validate() throws IllegalActionException {
        Settable container = (Settable)getContainer();
        if (container != null && !_validating) {
            try {
                // Prevent infinite loop, since container may try to
                // validate this.
                _validating = true;
                container.validate();
            } finally {
                _validating = false;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);

    // Flag to prevent infinite loop setting name.
    private boolean _settingName = false;

    // Flag to prevent infinite loop validating.
    private boolean _validating = false;

    private Settable.Visibility _visibility = FULL;
}
