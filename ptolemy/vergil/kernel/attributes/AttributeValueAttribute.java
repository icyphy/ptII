/* An attribute that displays the value of an attribute of the container.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.vergil.kernel.attributes;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collection;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.ValueListener;

///////////////////////////////////////////////////////////////////
//// AttributeValueAttribute

/**
 This is a text attribute whose text string shows the
 value of a parameter. <p>

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class AttributeValueAttribute extends AbstractTextAttribute implements
ValueListener, Settable {
    // NOTE: This attribute only implements settable as a workaround
    // to ensure that it gets notified of the start of execution.
    // Unfortunately, most of the code in the Variable class is
    // written to be specific to Variables, making it difficult for
    // this class to properly listen to a variable by name.

    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public AttributeValueAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        attributeName = new StringAttribute(this, "attributeName");
        displayWidth = new Parameter(this, "displayWidth");
        displayWidth.setExpression("6");
        displayWidth.setTypeEquals(BaseType.INT);

        useExpression = new Parameter(this, "useExpression");
        useExpression.setExpression("false");
        useExpression.setTypeEquals(BaseType.BOOLEAN);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the attribute of the container whose value to display. */
    public StringAttribute attributeName;

    /** The number of characters to display. This is an integer, with
     *  default value 6.
     */
    public Parameter displayWidth;

    /** If true, display the expression rather than the value.
     *  This is a boolean that defaults to false.
     */
    public Parameter useExpression;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a changes in the attributes by changing the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == attributeName) {
            _setAttributeName(attributeName.getExpression());
        } else if (attribute == displayWidth) {
            _displayWidth = ((IntToken) displayWidth.getToken()).intValue();
            _icon.setText(_getText());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** React to a change in the value of the associated attribute.
     */
    @Override
    public void valueChanged(Settable settable) {
        _setAttributeName(attributeName.getExpression());
    }

    /** Add a listener to be notified when the value of this settable
     *  object changes. This implementation ignores the argument, so
     *  listeners to this object are not notified of changes in value.
     *  @param listener The listener to add.
     *  @see #removeValueListener(ValueListener)
     */
    @Override
    public void addValueListener(ValueListener listener) {
    }

    /** Return the default value of this attribute, if there is
     *  one, or null if there is none.
     *  @return The default value of this attribute, or null
     *   if there is none.
     */
    @Override
    public String getDefaultExpression() {
        return "";
    }

    /** Return a name to present to the user, which
     *  is the same as the name returned by getName().
     *  @return A name to present to the user.
     */
    @Override
    public String getDisplayName() {
        return getName();
    }

    /** Get the value of the attribute that has been set by setExpression(),
     *  or null if there is none.
     *  @return The expression.  This base class always returns
     *  the empty string "".
     *  @see #setExpression(String)
     */
    @Override
    public String getExpression() {
        return "";
    }

    /** Get the value of the attribute, which is the evaluated expression.
     *  @return The same as getExpression().
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
        return Settable.NONE;
    }

    /** Remove a listener from the list of listeners that are
     *  notified when the value of this variable changes. This
     *  implementation ignores the argument.
     *  @param listener The listener to remove.
     *  @see #addValueListener(ValueListener)
     */
    @Override
    public void removeValueListener(ValueListener listener) {
    }

    /** Set the value of the attribute by giving some expression.
     *  In this implementation, the specified value is ignored.
     *  @param expression The value of the attribute, which is ignored.
     *  @exception IllegalActionException If the expression is invalid.
     *  @see #getExpression()
     */
    @Override
    public void setExpression(String expression) throws IllegalActionException {
    }

    /** Set the visibility of this Settable.  This call does nothing.
     *  @param visibility The visibility of this Settable.
     *  @see #getVisibility()
     */
    @Override
    public void setVisibility(Settable.Visibility visibility) {
    }

    /** Set the attribute name to match the current expression.
     *  @return Null, indicating that no other instances of Settable
     *   are validated.
     *  @exception IllegalActionException If the expression is not valid, or
     *   its value is not acceptable to the container or the listeners.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        _setAttributeName(attributeName.getExpression());
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the attribute name.
     *  @param attributeName The attribute name.
     */
    protected void _setAttributeName(final String attributeName) {
        NamedObj container = getContainer();

        if (container != null) {
            Attribute newAttribute = ModelScope.getScopedVariable(null,
                    container, attributeName);
            if (newAttribute == null) {
                // Either the specified attribute name is invalid,
                // or this is getting invoked in the constructor, and the
                // attribute being referenced has not yet been constructed.
                // To support the latter situation, we try again (just one
                // more time) in a ChangeRequest.
                if (!_deferred) {
                    ChangeRequest request = new ChangeRequest(this,
                            "AttributeValueAttribute") {
                        @Override
                        protected void _execute() {
                            _setAttributeName(attributeName);
                            _deferred = false;
                        }
                    };
                    _deferred = true;
                    container.requestChange(request);
                }
                _attribute = null;
            } else if (_attribute != newAttribute) {
                if (_attribute != null) {
                    _attribute.removeValueListener(this);
                }

                // newAttribute will always be a Settable.
                _attribute = (Settable) newAttribute;
                _attribute.addValueListener(this);
            }
        }

        _icon.setText(_getText());
    }

    /** Return a new string that contains the expression of the
     *  referred to attribute.
     *  @return A new shape.
     */
    protected String _getText() {
        NamedObj container = getContainer();

        try {
            if (container != null) {
                if (_attribute instanceof Variable) {
                    String value;
                    if (!((BooleanToken) useExpression.getToken())
                            .booleanValue()) {
                        Token token = ((Variable) _attribute).getToken();
                        value = "absent";
                        if (token != null) {
                            value = token.toString();
                            // Suppress scientific notation if it's a double.
                            if (token instanceof DoubleToken) {
                                double doubleValue = ((DoubleToken) token)
                                        .doubleValue();
                                NumberFormat format = NumberFormat
                                        .getInstance();
                                format.setGroupingUsed(false);
                                format.setMinimumFractionDigits(1);
                                format.setRoundingMode(RoundingMode.UNNECESSARY);
                                value = format.format(doubleValue);
                                // If the value shown is 0.0, make sure it's actually zero.
                                if (value.equals("0.0") && doubleValue != 0.0) {
                                    value = "0.00000...";
                                }
                            }
                        }
                    } else {
                        value = _attribute.getExpression();
                    }
                    String truncated = value;
                    int width = _displayWidth;

                    if (value.length() > width) {
                        truncated = value.substring(0, width) + "...";
                    }

                    if (truncated.length() == 0) {
                        truncated = " ";
                    }

                    return truncated;
                } else {
                    String value = _attribute.getExpression();
                    String truncated = value;
                    int width = _displayWidth;

                    if (value.length() > width) {
                        truncated = value.substring(0, width) + "...";
                    }

                    if (truncated.length() == 0) {
                        truncated = " ";
                    }

                    return truncated;
                }
            }
        } catch (Throwable throwable) {
            return "???";
        }

        return "???";
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected members                  ////

    /** Most recent value of the rounding parameter. */
    protected int _displayWidth = 0;

    /** The associated attribute. */
    protected Settable _attribute = null;

    ///////////////////////////////////////////////////////////////////
    ////                          private members                  ////

    /** Flag indicating that we have already tried deferring evaluation. */
    private boolean _deferred = false;
}
