/* An operation to remove a port.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.gt.ingredients.operations;

import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.Replacement;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// PortRemovalOperation

/**
 An operation to remove a port.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PortRemovalOperation extends Operation {

    /** Construct an operation within the given list as its owner. All elements
     *  are enabled and are initialized to empty at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     */
    public PortRemovalOperation(GTIngredientList owner) {
        this(owner, "");
    }

    /** Construct an operation within the given list as its owner and initialize
     *  all the elements with the given values, which are a string encoding of
     *  those elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param values The string encoding of the values of the elements.
     */
    public PortRemovalOperation(GTIngredientList owner, String values) {
        super(owner, 1);
        setValues(values);
    }

    /** Get the change request to update the object in the host model.
     *
     *  @param pattern The pattern of the transformation rule.
     *  @param replacement The replacement of the transformation rule.
     *  @param matchResult The match result.
     *  @param patternObject The object in the pattern, or null.
     *  @param replacementObject The object in the replacement that corresponds
     *   to the object in the pattern.
     *  @param hostObject The object in the host model corresponding to the
     *   object in the replacement.
     *  @return The change request.
     */
    @Override
    public ChangeRequest getChangeRequest(Pattern pattern,
            Replacement replacement, MatchResult matchResult,
            NamedObj patternObject, NamedObj replacementObject,
            NamedObj hostObject) {
        if (isNameEnabled()) {
            String name = _name;
            Port patternPort = ((Entity) patternObject).getPort(_name);
            if (patternPort != null) {
                // If the port is a port in the pattern, find the matched port
                // in the host actor.
                Port hostPort = (Port) matchResult.get(patternPort);
                name = hostPort.getName();
            }
            String moml = "<deletePort name=\"" + name + "\"/>";
            return new MoMLChangeRequest(this, hostObject, moml, null);
        } else {
            return null;
        }
    }

    /** Get the array of elements defined in this GTIngredient.
     *
     *  @return The array of elements.
     */
    @Override
    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    /** Get the port name element.
     *
     *  @return The port name element.
     */
    public String getName() {
        return _name;
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
            return _name;
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
        _encodeStringField(buffer, 0, _name);
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

    /** Return whether the port name element is enabled.
     *
     *  @return true if the port name element is enabled.
     */
    public boolean isNameEnabled() {
        return isEnabled(0);
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
            _name = (String) value;
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
        _name = _decodeStringField(0, fieldIterator);
    }

    /** Validate the enablements and values of all the elements.
     *
     *  @exception ValidationException If some elements are invalid.
     */
    @Override
    public void validate() throws ValidationException {
        if (_name.equals("")) {
            throw new ValidationException("Name must not be empty.");
        }
        if (_name.contains(".")) {
            throw new ValidationException("Name must not have period (\".\") "
                    + "in it.");
        }
    }

    /** The elements.
     */
    private static final OperationElement[] _ELEMENTS = { new StringOperationElement(
            "matcher name or port name", false, true) };

    /** Value of the port name element.
     */
    private String _name;
}
