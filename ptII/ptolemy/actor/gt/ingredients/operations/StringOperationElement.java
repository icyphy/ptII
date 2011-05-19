/* A string element for an operation.

 Copyright (c) 2003-2009 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// StringOperationElement

/**
 A string element for an operation.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class StringOperationElement extends OperationElement {

    /** Construct a string element for an operation.
     *
     *  @param name The name of the element.
     *  @param canDisable Whether the element can be disabled.
     */
    public StringOperationElement(String name, boolean canDisable) {
        this(name, canDisable, false);
    }

    /** Construct a Boolean element for an operation.
     *
     *  @param name The name of the element.
     *  @param canDisable Whether the element can be disabled.
     *  @param acceptPtolemyExpression Whether Ptolemy expression is accepted.
     */
    public StringOperationElement(String name, boolean canDisable,
            boolean acceptPtolemyExpression) {
        super(name, canDisable);
        _acceptPtolemyExpression = acceptPtolemyExpression;
    }

    /** Return whether Ptolemy expression is accepted.
     *
     *  @return true if Ptolemy expression is accepted.
     */
    public boolean acceptPtolemyExpression() {
        return _acceptPtolemyExpression;
    }

    /** Whether Ptolemy expression is accepted.
     */
    private boolean _acceptPtolemyExpression;
}
