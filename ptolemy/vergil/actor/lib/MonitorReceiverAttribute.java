/*  This parameter, when inserted into a port, causes the port to display its unconsumed inputs.

 @Copyright (c) 2007-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.vergil.actor.lib;

import java.util.Collection;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.ValueListener;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;

///////////////////////////////////////////////////////////////////
//// MonitorReceiverAttribute

/**
 This parameter, when inserted into a port, causes the port to display data
 available for the actor to read.
 It assumes that the port is an input port.
 Otherwise, it returns an empty string. It is a singleton, and it's
 name is always _showInfo, regardless of what name argument is specified to the constructor.
 Note that the display is not automatically updated. Use an instance of RepaintController
 in your model to cause the display to be updated, or use MonitorReceiverContents.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see MonitorReceiverContents
 */
public class MonitorReceiverAttribute extends SingletonAttribute implements
        Settable {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MonitorReceiverAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, "_showInfo");

        // The icon.
        EditorIcon _icon = new EditorIcon(this, "_icon");
        RectangleAttribute rectangle = new RectangleAttribute(_icon,
                "rectangle");
        rectangle.width.setExpression("175.0");
        rectangle.height.setExpression("20.0");
        rectangle.fillColor.setExpression("{1.0, 0.7, 0.7, 1.0}");

        Location _location = new Location(rectangle, "_location");
        _location.setExpression("-5.0, -15.0");

        TextAttribute text = new TextAttribute(_icon, "text");
        text.text.setExpression("MonitorReceiverAttribute");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be notified when the value of this settable
     *  object changes. An implementation of this method should ignore
     *  the call if the specified listener is already on the list of
     *  listeners.  In other words, it should not be possible for the
     *  same listener to be notified twice of a value update.
     *  @param listener The listener to add.
     *  @see #removeValueListener(ValueListener)
     */
    @Override
    public void addValueListener(ValueListener listener) {
        // FIXME: Ignore for now
    }

    /** Return the default value of this attribute, if there is
     *  one, or null if there is none.
     *  @return The default value of this attribute, or null
     *   if there is none.
     */
    @Override
    public String getDefaultExpression() {
        return "Queue monitor";
    }

    /** Return a name to present to the user.
     *  @return A name to present to the user.
     */
    @Override
    public String getDisplayName() {
        return "MonitorReceiverAttribute";
    }

    /** Get the expression currently used by this variable. The expression
     *  is either the value set by setExpression(), or a string representation
     *  of the value set by setToken(), or an empty string if no value
     *  has been set.
     *  @return The expression used by this variable.
     *  @see #setExpression(String)
     */
    @Override
    public String getExpression() {
        try {
            NamedObj container = getContainer();
            if (!(container instanceof IOPort)) {
                return "Not an IOPort";
            }
            Receiver[][] receivers = ((IOPort) container).getReceivers();
            if (receivers == null || receivers.length == 0) {
                return "";
            }
            StringBuffer result = new StringBuffer();
            if (((IOPort) container).isMultiport()) {
                result.append("{");
            }
            for (int i = 0; i < receivers.length; i++) {
                if (i > 0) {
                    result.append("}, {");
                }
                for (int j = 0; j < receivers[i].length; j++) {
                    // Normally there will only be one j with value 0.
                    // Just in case, put in a separator.
                    if (j > 0) {
                        result.append(" AND ");
                    }
                    Receiver receiver = receivers[i][j];
                    if (receiver != null) {
                        List<Token> list = receiver.elementList();
                        if (list != null) {
                            result.append(list);
                        } else {
                            result.append("NULL");
                        }
                    } else {
                        result.append("No Receiver");
                    }
                }
            }
            if (((IOPort) container).isMultiport()) {
                result.append("}");
            }
            return result.toString();
        } catch (Exception e) {
            return "ERROR: " + e;
        }
    }

    /** Get the value of the attribute, which is the evaluated expression.
     *  @return The value.
     *  @see #getExpression()
     */
    @Override
    public String getValueAsString() {
        return getExpression();
    }

    /** Get the visibility of this Settable, as set by setVisibility().
     *  If setVisibility() has not been called, then implementations of
     *  this interface should return some default, not null, indicating
     *  user-level visibility. The returned value is one of the static
     *  instances of the Visibility inner class.
     *  @return The visibility of this Settable.
     *  @see #setVisibility(Settable.Visibility)
     */
    @Override
    public Settable.Visibility getVisibility() {
        return Settable.NOT_EDITABLE;
    }

    /** Remove a listener from the list of listeners that are
     *  notified when the value of this variable changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     *  @see #addValueListener(ValueListener)
     */
    @Override
    public void removeValueListener(ValueListener listener) {
        // FIXME: Ignore for now.
    }

    /** Set the value of the attribute by giving some expression.
     *  In some implementations, the listeners and the container will
     *  be notified immediately.  However, some implementations may
     *  defer notification until validate() is called.
     *  @param expression The value of the attribute.
     *  @exception IllegalActionException If the expression is invalid.
     *  @see #getExpression()
     */
    @Override
    public void setExpression(String expression) throws IllegalActionException {
        // Ignore.
        // Do not throw an exception here, as it prevents undo from working.
    }

    /** Set the visibility of this Settable.  The argument should be one
     *  of the static public instances of the inner class Visibility.
     *  This is enforced by making it impossible to construct instances
     *  of this inner class outside this interface definition.
     *  If this method is not called, then implementations of
     *  this interface should return some default, not null.
     *  @param visibility The visibility of this Settable.
     *  @see #getVisibility()
     */
    @Override
    public void setVisibility(Settable.Visibility visibility) {
        // Ignore.
    }

    /** Check the validity of the expression set in setExpression().
     *  Implementations of this method should notify the container
     *  by calling attributeChanged(), unless the container has already
     *  been notified in setExpression().  They should also notify any
     *  registered value listeners if they have not already been notified.
     *  If any other instances of Settable are validated as a side effect,
     *  then an implementation should return a Collection containing those
     *  instances. This can be used by the caller to avoid validating those
     *  again. The list may contain this instance of Settable.
     *  @return A collection of settables that are also validated as a
     *   side effect, or null if there are none.
     *  @exception IllegalActionException If the expression is not valid, or
     *   its value is not acceptable to the container or the listeners.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        // Ignore.
        return null;
    }
}
