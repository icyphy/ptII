/* A criterion to constrain a port of an actor in the host model.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.actor.gt.ingredients.criteria;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.GTIngredient;
import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.util.PtolemyExpressionString;
import ptolemy.actor.gt.util.RegularExpressionString;
import ptolemy.data.type.Type;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// PortCriterion

/**
 A criterion to constrain a port of an actor in the host model. The port name is
 matched by the portName element, which can be specified with a regular
 expression. The name of the port created for this criterion for the actor (or
 matcher) in the pattern, however, is defined by the matcherName element.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PortCriterion extends Criterion {

    /** Construct a criterion within the given list as its owner. All elements
     *  are enabled and are initialized to empty at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     */
    public PortCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    /** Construct a criterion within the given list as its owner and initialize
     *  all the elements with the given values, which are a string encoding of
     *  those elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param values The string encoding of the values of the elements.
     */
    public PortCriterion(GTIngredientList owner, String values) {
        this(owner, null, null, false, false, false, "");
        setValues(values);
    }

    /** Construct a criterion within the given list as its owner and with the
     *  given value to each of its elements..
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param portName Value of the portName element.
     *  @param portType Value of the portType element.
     *  @param input Value of the input element.
     *  @param output Value of the output element.
     *  @param multiport Value of the multiport element.
     *  @param matcherName Value of the matcherName element.
     */
    public PortCriterion(GTIngredientList owner, String portName,
            String portType, boolean input, boolean output, boolean multiport,
            String matcherName) {
        super(owner, 6);

        NamedObj container = owner.getOwner().getContainer();
        _portName = new RegularExpressionString(portName);
        _portType = new PtolemyExpressionString(container, portType);
        _input = input;
        _output = output;
        _multiport = multiport;
        _matcherName = matcherName;
    }

    /** Return whether this criterion can check the given object.
     *
     *  @param object The object.
     *  @return true if the object can be checked.
     */
    @Override
    public boolean canCheck(NamedObj object) {
        return super.canCheck(object) && object instanceof Port;
    }

    /** Get the array of elements defined in this GTIngredient.
     *
     *  @return The array of elements.
     */
    @Override
    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    /** Get the matcherName element.
     *
     *  @return The matcherName element.
     */
    public String getMatcherName() {
        return _matcherName;
    }

    /** Get the ID of the port created for this criterion.
     *
     *  @param list The list in which the port ID should be unique.
     *  @return The port ID.
     */
    public String getPortID(GTIngredientList list) {
        if (!isMatcherNameEnabled() || _matcherName.equals("")) {
            int position = list.indexOf(this);
            return _getUniqueName(list, "criterion" + (position + 1));
        } else {
            return _getUniqueName(list, _matcherName);
        }
    }

    /** Get the portName element.
     *
     *  @return The portName element.
     */
    public String getPortName() {
        return _portName.get();
    }

    /** Get the portType element.
     *
     *  @return The portType element.
     */
    public String getPortType() {
        return _portType.get();
    }

    /** Get the value of the index-th elements.
     *
     *  @param index The index.
     *  @return The value.
     *  @see #setValue(int, Object)
     */
    @Override
    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _portName;
        case 1:
            return _portType;
        case 2:
            return _input;
        case 3:
            return _output;
        case 4:
            return _multiport;
        case 5:
            return _matcherName;
        default:
            return null;
        }
    }

    /** Get a string that describes the values of all the elements.
     *
     *  @return A string that describes the values of all the elements.
     *  @see #setValues(String)
     */
    @Override
    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _portName.get());
        _encodeStringField(buffer, 1, _portType.get());
        _encodeBooleanField(buffer, 2, _input);
        _encodeBooleanField(buffer, 3, _output);
        _encodeBooleanField(buffer, 4, _multiport);
        _encodeStringField(buffer, 5, _matcherName);
        return buffer.toString();
    }

    /** Check whether this GTIngredient is applicable to the object.
     *
     *  @param object The object.
     *  @return true if this GTIngredient is applicable; false otherwise.
     */
    @Override
    public boolean isApplicable(NamedObj object) {
        return super.isApplicable(object) && object instanceof ComponentEntity
                && !(object instanceof State);
    }

    /** Get the input element.
     *
     *  @return The input element.
     */
    public boolean isInput() {
        return _input;
    }

    /** Return whether the input element is enabled.
     *
     *  @return true if the input element is enabled.
     */
    public boolean isInputEnabled() {
        return isEnabled(2);
    }

    /** Return whether the matcherName element is enabled.
     *
     *  @return true if the matcherName element is enabled.
     */
    public boolean isMatcherNameEnabled() {
        return isEnabled(5);
    }

    /** Get the multiport element.
     *
     *  @return The multiport element.
     */
    public boolean isMultiport() {
        return _multiport;
    }

    /** Return whether the multiport element is enabled.
     *
     *  @return true if the multiport element is enabled.
     */
    public boolean isMultiportEnabled() {
        return isEnabled(4);
    }

    /** Get the output element.
     *
     *  @return The output element.
     */
    public boolean isOutput() {
        return _output;
    }

    /** Return whether the output element is enabled.
     *
     *  @return true if the output element is enabled.
     */
    public boolean isOutputEnabled() {
        return isEnabled(3);
    }

    /** Return whether the portName element is enabled.
     *
     *  @return true if the portName element is enabled.
     */
    public boolean isPortNameEnabled() {
        return isEnabled(0);
    }

    /** Return whether the portType element is enabled.
     *
     *  @return true if the portType element is enabled.
     */
    public boolean isPortTypeEnabled() {
        return isEnabled(1);
    }

    /** Test whether the given object in the host model matches the object in
     *  the pattern that has this criterion.
     *
     *  @param object The object.
     *  @return true if the object matches.
     */
    @Override
    public boolean match(NamedObj object) {
        // Check the input/output/multiport type
        if (object instanceof IOPort) {
            IOPort port = (IOPort) object;
            if (isInputEnabled() && _input != port.isInput()) {
                return false;
            } else if (isOutputEnabled() && _output != port.isOutput()) {
                return false;
            } else if (isMultiportEnabled() && _multiport != port.isMultiport()) {
                return false;
            }
        } else if (object instanceof Port) {
            if (isInputEnabled() || isOutputEnabled() || isMultiportEnabled()) {
                return false;
            }
        } else {
            return false;
        }

        // Check port name
        if (isPortNameEnabled()) {
            Pattern pattern = _portName.getPattern();
            Matcher matcher = pattern.matcher(object.getName());
            if (!matcher.matches()) {
                return false;
            }
        }

        // Check port type
        if (isPortTypeEnabled()) {
            if (object instanceof TypedIOPort) {
                TypedIOPort typedIOPort = (TypedIOPort) object;
                try {
                    Type lhsType = _portType.getToken().getType();
                    Type hostType = typedIOPort.getType();
                    boolean isTypeCompatible = true;
                    if (isInputEnabled() && _input) {
                        isTypeCompatible = isTypeCompatible
                                && hostType.isCompatible(lhsType);
                    }
                    if (isOutputEnabled() && _output) {
                        isTypeCompatible = isTypeCompatible
                                && lhsType.isCompatible(hostType);
                    }
                    if (!isTypeCompatible) {
                        return false;
                    }
                } catch (IllegalActionException e) {
                    return false;
                }
            }
        }

        return true;
    }

    /** Enable or disable the input.
     *  <p>Used as part of the test harness.
     *  @param enabled true if the element is set to be enabled; false if it
     *   is disabled.
     *  @see ptolemy.actor.gt.GTIngredient#setEnabled(int, boolean)
     */
    public void setInputEnabled(boolean enabled) {
        setEnabled(2, enabled);
    }

    /** Enable or disable the matcher name.
     *  <p>Used as part of the test harness.
     *  @param enabled true if the element is set to be enabled; false if it
     *   is disabled.
     *  @see ptolemy.actor.gt.GTIngredient#setEnabled(int, boolean)
     */
    public void setMatcherNameEnabled(boolean enabled) {
        setEnabled(5, enabled);
    }

    /** Enable or disable the multiport.
     *  <p>Used as part of the test harness.
     *  @param enabled true if the element is set to be enabled; false if it
     *   is disabled.
     *  @see ptolemy.actor.gt.GTIngredient#setEnabled(int, boolean)
     */
    public void setMultiportEnabled(boolean enabled) {
        setEnabled(4, enabled);
    }

    /** Enable or disable the output.
     *  <p>Used as part of the test harness.
     *  @param enabled true if the element is set to be enabled; false if it
     *   is disabled.
     *  @see ptolemy.actor.gt.GTIngredient#setEnabled(int, boolean)
     */
    public void setOutputEnabled(boolean enabled) {
        setEnabled(3, enabled);
    }

    /** Enable or disable the port name.
     *  <p>Used as part of the test harness.
     *  @param enabled true if the element is set to be enabled; false if it
     *   is disabled.
     *  @see ptolemy.actor.gt.GTIngredient#setEnabled(int, boolean)
     */
    public void setPortNameEnabled(boolean enabled) {
        setEnabled(0, enabled);
    }

    /** Enable or disable the port type.
     *  <p>Used as part of the test harness.
     *  @param enabled true if the element is set to be enabled; false if it
     *   is disabled.
     *  @see ptolemy.actor.gt.GTIngredient#setEnabled(int, boolean)
     */
    public void setPortTypeEnabled(boolean enabled) {
        setEnabled(1, enabled);
    }

    /** Set the value of the index-th element.
     *
     *  @param index The index.
     *  @param value The value.
     *  @see #getValue(int)
     */
    @Override
    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _portName.set((String) value);
            break;
        case 1:
            _portType.set((String) value);
            break;
        case 2:
            _input = ((Boolean) value).booleanValue();
            break;
        case 3:
            _output = ((Boolean) value).booleanValue();
            break;
        case 4:
            _multiport = ((Boolean) value).booleanValue();
            break;
        case 5:
            _matcherName = (String) value;
            break;
        }
    }

    /** Set the values of all the elements with a string that describes them.
     *
     *  @param values A string that describes the new values of all the
     *   elements.
     *  @see #getValues()
     */
    @Override
    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _portName.set(_decodeStringField(0, fieldIterator));
        _portType.set(_decodeStringField(1, fieldIterator));
        _input = _decodeBooleanField(2, fieldIterator);
        _output = _decodeBooleanField(3, fieldIterator);
        _multiport = _decodeBooleanField(4, fieldIterator);
        _matcherName = _decodeStringField(5, fieldIterator);
    }

    /** Validate the enablements and values of all the elements.
     *
     *  @exception ValidationException If some elements are invalid.
     */
    @Override
    public void validate() throws ValidationException {
        if (isPortNameEnabled()) {
            if (_portName.get().equals("")) {
                throw new ValidationException("Port name must not be empty.");
            }

            try {
                _portName.getPattern();
            } catch (PatternSyntaxException e) {
                throw new ValidationException("Regular expression \""
                        + _portName + "\" cannot be compiled.", e);
            }
        }

        if (isPortTypeEnabled()) {
            if (_portType.get().equals("")) {
                throw new ValidationException("Port type must not be empty.");
            }

            try {
                _portType.getToken().getType();
            } catch (IllegalActionException e) {
                throw new ValidationException("Type expression \"" + _portType
                        + "\" cannot be parsed.", e);
            }
        }
    }

    /** Get a unique name based on the given name within a list.
     *
     *  @param list The list.
     *  @param name The name to be based on.
     *  @return The unique name within the list.
     */
    private String _getUniqueName(GTIngredientList list, String name) {
        int pos = 1;
        String id = null;
        boolean success = false;
        while (!success) {
            success = true;
            id = pos >= 2 ? name + pos : name;
            Iterator<GTIngredient> iterator = list.iterator();
            while (iterator.hasNext()) {
                GTIngredient ingredient = iterator.next();
                if (ingredient == this) {
                    break;
                } else if (ingredient instanceof PortCriterion) {
                    PortCriterion portCriterion = (PortCriterion) ingredient;
                    String portId = portCriterion.getPortID(list);
                    if (portId.equals(id)) {
                        success = false;
                        break;
                    }
                }
            }
        }
        return id;
    }

    /** The elements.
     */
    private static final CriterionElement[] _ELEMENTS = {
            new StringCriterionElement("name", true, true, false),
            new ChoiceCriterionElement("type", true, false, true, true),
            new BooleanCriterionElement("input", true),
            new BooleanCriterionElement("output", true),
            new BooleanCriterionElement("multi", true),
            new StringCriterionElement("matcherName", true, false, false) };

    /** Value of the input element.
     */
    private boolean _input;

    /** Value of the matcherName element.
     */
    private String _matcherName;

    /** Value of the multiport element.
     */
    private boolean _multiport;

    /** Value of the output element.
     */
    private boolean _output;

    /** Value of the portName element.
     */
    private RegularExpressionString _portName;

    /** Value of the portType element.
     */
    private PtolemyExpressionString _portType;

    /* No longer used.
    static {
        ChoiceCriterionElement portTypes = (ChoiceCriterionElement) _ELEMENTS[1];
        portTypes.addChoices(Constants.types().keySet());
        portTypes.addChoice("arrayType(int)");
        portTypes.addChoice("arrayType(int,5)");
        portTypes.addChoice("[double]");
        portTypes.addChoice("{x=double, y=double}");
    }
    */
}
