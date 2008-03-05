/*

 Copyright (c) 1997-2007 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class NamedObjToken extends FakedRecordToken {

    public NamedObjToken(NamedObj object) throws IllegalActionException {
        _object = object;
    }

    public boolean equals(Object object) {
        return this == object || (object instanceof NamedObjToken
                && ((NamedObjToken) object)._object.equals(_object));
    }

    public Token get(String label) {
        if (_object instanceof GTEntity) {
            Token token = ((GTEntity) _object).getIngredientToken(label);
            if (token != null) {
                return token;
            }
        }

        NamedObj child = GTTools.getChild(_object, label, true, true, true,
                true);
        if (child instanceof ActorScopeExtender) {
            child = GTTools.getChild(_object, label, false, true, true, true);
        }
        try {
            if (child instanceof Variable) {
                return ((Variable) child).getToken();
            } else {
                return NamedObjVariable.getNamedObjVariable(child, true)
                        .getToken();
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }

    public NamedObj getObject() {
        return _object;
    }

    public int hashCode() {
        return _object.hashCode();
    }

    public BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        return _isEqualTo(rightArgument);
    }

    public Set<String> labelSet() {
        long version = _object.workspace().getVersion();
        if (_labelSet == null || version > _version) {
            _labelSet = new HashSet<String>();

            if (_object instanceof GTEntity) {
                Set<String> labelSet = ((GTEntity) _object).labelSet();
                if (labelSet != null) {
                    _labelSet.addAll(labelSet);
                }
            }

            Collection<?> children = GTTools.getChildren(_object, true, true,
                    true, true);
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                if (child instanceof ActorScopeExtender) {
                    continue;
                }
                _labelSet.add(child.getName());
            }
            _version = version;
        }
        return _labelSet;
    }

    public int length() {
        return labelSet().size();
    }

    protected BooleanToken _isEqualTo(Token object)
            throws IllegalActionException {
        return BooleanToken.getInstance(equals(object));
    }

    private Set<String> _labelSet;

    private NamedObj _object;

    private long _version = -1;
}
