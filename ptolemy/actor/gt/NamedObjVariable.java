/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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

						PT_COPYRIGHT_VERSION_2
						COPYRIGHTENDKEY



 */

package ptolemy.actor.gt;

import java.util.List;

import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class NamedObjVariable extends Variable {

    public NamedObjVariable(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        this(container, container.uniqueName(NAME_PREFIX));
    }

    public NamedObjVariable(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setToken(new ObjectToken(container));
        _setTokenWithContainer = true;
        setPersistent(false);
    }

    private boolean _setTokenWithContainer = false;

    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj oldContainer = getContainer();
        super.setContainer(container);
        if (_setTokenWithContainer && container != oldContainer) {
            setToken(new ObjectToken(container));
        }
    }

    public void setToken(Token token) throws IllegalActionException {
        if (token instanceof ObjectToken) {
            if (!getContainer().equals(((ObjectToken) token).getValue())) {
                throw new IllegalActionException("The NamedObj in the token "
                        + "is not equal to the container of this variable.");
            }
            super.setToken(token);
        } else {
            throw new IllegalActionException("Only instances of NamedObjToken "
                    + "are allowed as argument of setToken().");
        }
    }

    public static NamedObjVariable getNamedObjVariable(NamedObj container,
            boolean autoCreate) throws IllegalActionException {
        List<?> attributes = container.attributeList(NamedObjVariable.class);
        if (attributes.isEmpty()) {
            if (autoCreate) {
                try {
                    return new NamedObjVariable(container);
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }
            } else {
                return null;
            }
        } else {
            return (NamedObjVariable) attributes.get(0);
        }
    }

    public static final String NAME_PREFIX = "namedObjVariable";
}
