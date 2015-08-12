/* Attribute that requires a particular version of Ptolemy II.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.kernel.attributes;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// RequireVersion

/**
 An attribute that requires a particular version of Ptolemy II.
 When the value of this attribute is set (via setExpression()),
 the value that is set is compared against the version of the
 currently executing Ptolemy II installation.  If the executing
 version is less than the value set, we throw an expression.
 <p>
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class RequireVersion extends VersionAttribute {
    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public RequireVersion(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setExpression(CURRENT_VERSION.getExpression());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the hash code of this object is equal (==) to
     *  the hash code of the argument.
     *  @param object  The specified object that is compared against.
     *  @return True if the specified version is the same as this one.
     */
    @Override
    public boolean equals(Object object) {
        // VersionAttribute.equals() has a bug where if we had a
        // VersionAttribute and a RequireVersion and the RequireVersion
        // is deleted, then only the first VersionAttribute was deleted, so we
        // define equals() and hashCode().  For details, see
        // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3984
        if (object instanceof RequireVersion) {
            return this.hashCode() == object.hashCode();
        }

        return false;
    }

    /** Return a hash code value for attribute. This method returns
     *  the identity hash code for this attribute.  The hashCode()
     *  method of the super class is <b>not</b> called.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /** Set the required version, check it against the currently
     *  executing version, and throw an exception if the executing
     *  version is older.
     *  @param expression The version string, consisting of
     *   version ID tuples separated by '.', '-' or '_'. For example:
     *   "1.2", "1.2_beta-4".
     *  @exception IllegalActionException If the argument contains a
     *   space, which violates the JNLP Version format specification,
     *   and if the specified version is newer than the executing version.
     */
    @Override
    public void setExpression(String expression) throws IllegalActionException {
        super.setExpression(expression);

        if (CURRENT_VERSION.isLessThan(this)) {
            throw new IllegalActionException(this,
                    "Current version of Ptolemy II is "
                            + CURRENT_VERSION.getExpression()
                            + ", but required version is " + expression + ".");
        }
    }
}
