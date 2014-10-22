/*  A Parameter created in a Ptalon file.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.ptalon;

import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 A Parameter created in a Ptalon file.
 @see PtalonActor

 @author Adam Cataldo, Elaine Cheong
 @version $Id$
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PtalonParameter extends Parameter {

    /** Construct a parameter with the given name contained by the
     *  specified entity. The container argument must not be null, or
     *  a NullPointerException will be thrown.  This parameter will
     *  use the workspace of the container for synchronization and
     *  version counts.  If the name argument is null, then the name
     *  is set to the empty string.  The object is not added to the
     *  list of objects in the workspace unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PtalonParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setStringMode(true);
        _hasValue = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Mark this parameter so that subsequent calls to {@link #hasValue()} will
     *  return false until a value is explicitly set with {@link
     *  #setExpression(String)}, {@link #setToken(String)} or {@link
     *  #setToken(Token)}.
     */
    public void clearValue() {
        _hasValue = false;
    }

    /** Return true if this parameter's value has been set.
     *  @return true If this parameter's value has been set.
     */
    public boolean hasValue() {
        return _hasValue;
    }

    /** Set the expression and flag that the value has been set for
     *  this parameter.
     *  @param expr The expression to set.
     */
    @Override
    public void setExpression(String expr) {
        if (expr == null || expr.trim().equals("")) {
            return;
        }
        _hasValue = true;

        super.setExpression(expr);
    }

    /** Set the token and flag that the value has been set for this
     *  parameter.
     *  @param expression The expression for this token
     *  @exception IllegalActionException If the superclass throws one.
     */
    @Override
    public void setToken(String expression) throws IllegalActionException {
        if (expression == null || expression.trim().equals("")) {
            return;
        }
        _hasValue = true;

        super.setToken(expression);
    }

    /** Set the token and flag that the value has been set for this
     *  parameter.
     *  @param token The token to set.
     *  @exception IllegalActionException If the superclass throws one.
     */
    @Override
    public void setToken(Token token) throws IllegalActionException {
        _hasValue = true;

        super.setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private members                     ////

    /** True if this parameter has a value.
     */
    private boolean _hasValue;
}
