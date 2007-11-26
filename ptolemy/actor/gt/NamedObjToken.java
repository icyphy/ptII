/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
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
public class NamedObjToken extends RecordToken {

    public NamedObjToken(NamedObj object) throws IllegalActionException {
        super(new String[0], new Token[0]);

        _object = object;
    }

    public boolean equals(Object object) {
        return this == object || (object instanceof NamedObjToken
                && ((NamedObjToken) object)._object.equals(_object));
    }

    public Token get(String label) {
        NamedObj child =
            GTTools.getChild(_object, label, true, true, true, true);
        if (child instanceof ActorScopeExtender) {
            child = GTTools.getChild(_object, label, false, true, true, true);
        }
        try {
            return NamedObjVariable.getNamedObjVariable(child, true).getToken();
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

    public Set<String> labelSet() {
        long version = _object.workspace().getVersion();
        if (_labelSet == null || version > _version) {
            Collection<?> children =
                GTTools.getChildren(_object, true, true, true, true);
            _labelSet = new HashSet<String>();
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

    public Token one() throws IllegalActionException {
        throw new IllegalActionException(
                "Multiplicative identity (one) does not exist.");
    }

    public Token zero() throws IllegalActionException {
        throw new IllegalActionException(
                "Additive identity (zero) does not exist.");
    }

    protected Token _add(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Add operation is not supported.");
    }

    protected Token _divide(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Divide operation is not supported.");
    }

    protected BooleanToken _isCloseTo(Token rightArgument, double epsilon)
    throws IllegalActionException {
        throw new IllegalActionException(
                "IsCloseTo operation is not supported.");
    }

    protected BooleanToken _isEqualTo(Token token)
    throws IllegalActionException {
        return BooleanToken.getInstance(equals(token));
    }

    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Modulo operation is not supported.");
    }

    protected Token _multiply(Token rightArgument)
    throws IllegalActionException {
        throw new IllegalActionException(
                "Multiply operation is not supported.");
    }

    protected Token _subtract(Token rightArgument)
    throws IllegalActionException {
        throw new IllegalActionException(
                "Subtract operation is not supported.");
    }

    private Set<String> _labelSet;

    private NamedObj _object;

    private long _version = -1;
}
