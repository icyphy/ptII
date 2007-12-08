/*

 Copyright (c) 2003-2007 The Regents of the University of California.
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
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// SubclassRule

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class RenameOperation extends Operation {

    public RenameOperation(GTIngredientList owner) {
        this(owner, "");
    }

    public RenameOperation(GTIngredientList owner, String values) {
        super(owner, 1);
        setValues(values);
    }

    public ChangeRequest getChangeRequest(Pattern pattern,
            Replacement replacement, MatchResult matchResult,
            Entity patternEntity, Entity replacementEntity, Entity hostEntity) {
        if (isNameEnabled()) {
            NamedObj parent = hostEntity.getContainer();
            String moml = "<entity name=\"" + hostEntity.getName()
                    + "\"><rename name=\"" + _name + "\"/></entity>";
            return new MoMLChangeRequest(this, parent, moml, null);
        } else {
            return null;
        }
    }

    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    public String getName() {
        return _name;
    }

    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _name;
        default:
            return null;
        }
    }

    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _name);
        return buffer.toString();
    }

    public boolean isNameEnabled() {
        return isEnabled(0);
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _name = (String) value;
            break;
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _name = _decodeStringField(0, fieldIterator);
    }

    public void validate() throws ValidationException {
        if (_name.equals("")) {
            throw new ValidationException("Name must not be empty.");
        }
        if (_name.contains(".")) {
            throw new ValidationException("Name must not have period (\".\") "
                    + "in it.");
        }
    }

    private static final OperationElement[] _ELEMENTS = { new StringOperationElement(
            "name", true) };

    private String _name;
}
