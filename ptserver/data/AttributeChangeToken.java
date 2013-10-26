/*
 AttributeChangeToken encapsulates changes made to a settable object.

 Copyright (c) 2011-2013 The Regents of the University of California.
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
package ptserver.data;

import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// AttributeChangeToken

/** Encapsulate changes made to a settable object.
 *
 * @author Peter Foldes
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (pdf)
 * @Pt.AcceptedRating Red (pdf)
 */
public class AttributeChangeToken extends Token {

    /** Create a new instance with targetSettable set to null.
     */
    public AttributeChangeToken() {
        super();
    }

    /** Create a new instance and set the name of the targetActor.
     *  @param targetSettable The full name of the attribute
     */
    public AttributeChangeToken(String targetSettable) {
        setTargetSettableName(targetSettable);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the object is equal to the instance, false otherwise.
     *  The method checks if the object has the same target name and
     *  the same expression.
     *  @param object The reference object with which to compare.
     *  @return True if the object is equal to the instance, false otherwise.
     *  @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
            return false;
        }
        if (isNil() || ((AttributeChangeToken) object).isNil()) {
            return false;
        }

        AttributeChangeToken other = (AttributeChangeToken) object;
        if (_targetSettableName == null) {
            if (other._targetSettableName != null) {
                return false;
            }
        } else if (!_targetSettableName.equals(other._targetSettableName)) {
            return false;
        }

        if (_expression == null) {
            if (other._expression != null) {
                return false;
            }
        } else if (!_expression.equals(other._expression)) {
            return false;
        }

        return true;
    }

    /** The hashCode method generates an identifier based on the stored
     *  expression and the target settable object. Used to check equality.
     *  @return The hashcode of the instance.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (_expression == null ? 0 : _expression.hashCode());
        result = prime
                * result
                + (_targetSettableName == null ? 0 : _targetSettableName
                        .hashCode());
        return result;
    }

    /** Return the expression.
     *  @return The expression.
     *  @see #setExpression(String)
     */
    public String getExpression() {
        return _expression;
    }

    /** Return name of the target settable object that received the changes encapsulated by
     *  the AttributeChangeToken.
     *  @return The name of the target settable object that received the changes encapsulated by
     *  the AttributeChangeToken.
     *  @see #setTargetSettableName(String)
     */
    public String getTargetSettableName() {
        return _targetSettableName;
    }

    /** Set the value of the expression carried.
     *  @param newExpression the changed expression
     *  @see #getExpression()
     */
    public void setExpression(String newExpression) {
        _expression = newExpression;
    }

    /** Set the name of target settable  object that received the changes that the
     *  AttributeChangeToken encapsulates.
     *  @param targetSettableName the name of the target actor
     *  @see #getTargetSettableName()
     */
    public void setTargetSettableName(String targetSettableName) {
        _targetSettableName = targetSettableName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** New expression for the target settable object.
     */
    private String _expression;

    /** Name of the target settable object.
     */
    private String _targetSettableName;
}
