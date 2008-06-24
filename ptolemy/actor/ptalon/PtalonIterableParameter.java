/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.ptalon;

import java.util.Collection;

import ptolemy.actor.gt.ValueIterator;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**


 @author Thomas Huining Feng
 @version $Id: AtomicActorMatcher.java 49223 2008-04-12 21:54:36Z tfeng $
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PtalonIterableParameter extends PtalonExpressionParameter
implements ValueIterator {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public PtalonIterableParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        initial = new PtalonExpressionParameter(this, "initial");
        initial.addValueListener(this);
        constraint = new PtalonExpressionParameter(this, "constraint");
        constraint.setTypeEquals(BaseType.BOOLEAN);
        constraint.addValueListener(this);
        next = new PtalonExpressionParameter(this, "next");
    }

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (!hasValue() && initial.hasValue()) {
            setToken(initial.getToken());
        }

        if (attribute == constraint) {
            if (!isConstraintSatisfied()) {
                throw new ConstraintViolationException();
            }
        }
    }

    public Token initial() throws IllegalActionException {
        Token initialToken = initial.getToken();
        setToken(initialToken);
        return initialToken;
    }

    public Token next() throws IllegalActionException {
        Token nextToken = next.getToken();
        setToken(nextToken);
        return nextToken;
    }

    public Collection<?> validate() throws IllegalActionException {
        Collection<?> result = super.validate();
        if (!isConstraintSatisfied()) {
            throw new ConstraintViolationException();
        }
        return result;
    }

    public PtalonExpressionParameter constraint;

    public PtalonExpressionParameter initial;

    public PtalonExpressionParameter next;

    public class ConstraintViolationException extends IllegalActionException {

        ConstraintViolationException() {
            super("ConstraintViolationException: Constraint " +
                    constraint.getExpression() + " is not satisfied.");
        }
    }

    private boolean isConstraintSatisfied() throws IllegalActionException {
        if (hasValue() && constraint.hasValue()) {
            return ((BooleanToken) constraint.getToken()).booleanValue();
        } else {
            return true;
        }
    }
}
