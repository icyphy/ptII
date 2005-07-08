/* Interface for attributes that can have their values externally set.

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
package ptolemy.kernel.util;

import java.io.Serializable;

//////////////////////////////////////////////////////////////////////////
//// Settable

/**
 This is an interface for attributes that can have their values
 externally set.  An attribute class that implements this interface has to
 be able to have a value set by a string, via the setExpression()
 method.  A string representation is returned by the getExpression()
 method.  An expression may be an ordinary string with no further
 interpretation, or it may be a string that needs to be evaluated.
 In the latter case, an implementation of this attribute may not
 evaluate the string when the setExpression() method is called.
 It may instead only evaluate the string when the validate() method
 is called.  Often this will not be called until the value of the
 expression is actually needed (this is known as "lazy evaluation").
 Such an implementation will defer notification of listeners and the
 container until the string is evaluated. In a typical use of this
 interface, therefore, it is necessary to be sure that validate()
 is called sometime after setExpression() is called.
 <p>
 In addition, an attribute class that implements this interface
 needs to maintain a list of listeners that are informed whenever
 the value of the attribute changes.  It should inform those
 listeners whenever setExpression() is called.
 <p>
 Among other uses, this interface marks attributes whose value
 can be set via the value attribute of a MoML property element.
 For example, if class XXX implements Settable, then the following
 is valid MoML:
 <pre>
 &lt;property name="xxx" class="XXX" value="yyy"/&gt;
 </pre>
 <p>
 This interface also supports annotations that hint to a user
 interface the level of visibility that an instance should have.
 The visibility is specified as one of the static instances of
 the inner class Visibility, currently NONE, EXPERT, FULL, and NOT_EDITABLE
 NONE indicates that the user should never see the instance,
 and should not be able to set its value through the user interface.
 EXPERT means that only expert users should see the instance.
 FULL means that the instance is always visible, and a user interface
 should always allow it to be set.
 NOT_EDITABLE is similar to FULL, except that the value of the
 expression is visible, but not editable by the user.  This is
 commonly used for feedback from the model.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 */
public interface Settable extends Nameable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be notified when the value of this settable
     *  object changes. An implementation of this method should ignore
     *  the call if the specified listener is already on the list of
     *  listeners.  In other words, it should not be possible for the
     *  same listener to be notified twice of a value update.
     *  @param listener The listener to add.
     *  @see #removeValueListener(ValueListener)
     */
    public void addValueListener(ValueListener listener);

    /** Return the default value of this attribute, if there is
     *  one, or null if there is none.
     *  @return The default value of this attribute, or null
     *   if there is none.
     */
    public String getDefaultExpression();

    /** Get the value of the attribute that has been set by setExpression(),
     *  or null if there is none.
     *  @return The expression.
     *  @see #setExpression(String)
     */
    public String getExpression();

    /** Get the visibility of this Settable, as set by setVisibility().
     *  If setVisibility() has not been called, then implementations of
     *  this interface should return some default, not null, indicating
     *  user-level visibility. The returned value is one of the static
     *  instances of the Visibility inner class.
     *  @return The visibility of this Settable.
     *  @see #setVisibility(Settable.Visibility)
     */
    public Settable.Visibility getVisibility();

    /** Remove a listener from the list of listeners that are
     *  notified when the value of this variable changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     *  @see #addValueListener(ValueListener)
     */
    public void removeValueListener(ValueListener listener);

    /** Set the value of the attribute by giving some expression.
     *  In some implementations, the listeners and the container will
     *  be notified immediately.  However, some implementations may
     *  defer notification until validate() is called.
     *  @param expression The value of the attribute.
     *  @exception IllegalActionException If the expression is invalid.
     *  @see #getExpression()
     */
    public void setExpression(String expression) throws IllegalActionException;

    /** Set the visibility of this Settable.  The argument should be one
     *  of the static public instances of the inner class Visibility.
     *  This is enforced by making it impossible to construct instances
     *  of this inner class outside this interface definition.
     *  If this method is not called, then implementations of
     *  this interface should return some default, not null.
     *  @param visibility The visibility of this Settable.
     *  @see #getVisibility()
     */
    public void setVisibility(Settable.Visibility visibility);

    /** Check the validity of the expression set in setExpression().
     *  Implementations of this method should notify the container
     *  by calling attributeChanged(), unless the container has already
     *  been notified in setExpression().  They should also notify any
     *  registered value listeners if they have not already been notified.
     *  @exception IllegalActionException If the expression is not valid, or
     *   its value is not acceptable to the container or the listeners.
     */
    public void validate() throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Indicator that a user interface should not make an instance visible.
     */
    public static Visibility NONE = new Visibility();

    /** Indicator that a user interface should make an instance visible
     *  only to experts.
     */
    public static Visibility EXPERT = new Visibility();

    /** Indicator that a user interface should make an instance visible.
     */
    public static Visibility FULL = new Visibility();

    /** Indicator that a user interface should make an instance
     *  visible, but not allow editing of the variable.
     */
    public static Visibility NOT_EDITABLE = new Visibility();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Inner class used for the static enumeration of indicators of
     *  visibility.  Instances of this class cannot be constructed outside
     *  the enclosing interface because its constructor is private.
     */
    public static class Visibility implements Serializable {
        // Private constructor prevents construction outside.
        // This constructor should not be called!
        // it is protected to work around a compiler bug in JDK1.2.2
        protected Visibility() {
        }
    }
}
