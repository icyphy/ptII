/* A Base class for monotonic function type constraints.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.data.type;

import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// MonotonicFunction

/**
   Actors often need to implement monotonic functions as part of the
   declaration of type constraints.  This base class makes it easy to do
   so.  In most cases, it is simply necessary to implement the getValue()
   and getVariables() abstract methods.

   @author Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Yellow (neuendor)
   @Pt.AcceptedRating Red (neuendor)
*/
public abstract class MonotonicFunction implements InequalityTerm {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return null.
     *  @return null.
     */
    public Object getAssociatedObject() {
        return null;
    }

    /** Return the result of this monotonic function.   Derived
     *  classes should implement this method to return the current value
     *  of the monotonic function.
     *  @return A Type.
     */
    public abstract Object getValue() throws IllegalActionException;

    /** Return the type variables in this inequality term.  Derived
     *  classes should implement this method to return an array of
     *  InequalityTerms that this function depends on.
     *  @return An array of InequalityTerm.
     */
    public abstract InequalityTerm[] getVariables();

    /** Return an additional string describing the current value
     *  of this function.  Subclasses may override this method to
     *  give additional information in the toString() method.   This
     *  method may return null, indicating that no additional information is
     *  desired.
     *  @return null.
     */
    public String getVerboseString() {
        return null;
    }

    /** Throw an Exception. This method cannot be called on a
     *  monotonic function term.
     *  @exception IllegalActionException Always thrown.
     */
    public final void initialize(Object e) throws IllegalActionException {
        throw new IllegalActionException(getClass().getName()
            + ": Cannot initialize a function term.");
    }

    /** Return false.  Monotonic Functions are not settable.
     *  @return False.
     */
    public final boolean isSettable() {
        return false;
    }

    /** Return true.  Monotonic Functions are, by default, always
     *  acceptable.  This method might be overridden by derived classes.
     *  @return True.
     */
    public boolean isValueAcceptable() {
        return true;
    }

    /** Throw an Exception. The value of a function term cannot be set.
     *  @exception IllegalActionException Always thrown.
     */
    public final void setValue(Object e) throws IllegalActionException {
        throw new IllegalActionException(getClass().getName()
            + ": The type is not settable.");
    }

    /** Override the base class to give a description of this term.
     *  @return A description of this term.
     */
    public final String toString() {
        String string = getVerboseString();

        if (string == null) {
            string = "";
        } else {
            string = ", " + string;
        }

        try {
            return "(" + getClass().getName() + ", " + getValue() + string
            + ")";
        } catch (IllegalActionException ex) {
            return "(" + getClass().getName() + ", INVALID" + string + ")";
        }
    }
}
