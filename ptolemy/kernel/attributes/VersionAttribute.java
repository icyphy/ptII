/* Ptolemy II Version identifiers

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.attributes;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// VersionAttribute
/**
A nonpersistent attribute that identifies the version of an object.
This attribute does not export MoML
The value of the attribute contains a String version-id that represents
the version.
A version-id is a string with substrings separated by one of '.', '-' or '_'.
The substrings may consist of any characters except space.  Version-ids can
be compared against each other other with the compareTo() method.

<p>The JNLP specification at
<a href="http://jcp.org/jsr/detail/056.jsp"><code>http://jcp.org/jsr/detail/056.jsp</code></a>
gives the following syntax for version-ids:
<pre>
version-id ::= string ( separator string ) *
string ::= char ( char ) *
char ::= Any ASCII character except a space, a separator or a
modifier
separator ::= "." | "-" | "_"
</pre>
Valid version-id include "1.3", "1.3.1", "1.3-beta_01".

<p>The JNLP specification includes version-strings, which are used for
matching one or more version-ids in a fashion similar to wildcard
matches within a regular expression.  At this time, this class does
not implement version-strings.

<p>
@see ptolemy.kernel.util.TransientSingletonConfigurableAttribute
@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class VersionAttribute
    extends StringAttribute implements Comparable {

    /** Construct an object in the default workspace with the empty string
     *  as its name. The object is added to the list of objects in the
     *  workspace. Increment the version number of the workspace.
     *  @param expression The initial value of this parameter, set
     *   using setExpression().
     *  @exception IllegalActionException If the value is of the
     *   incorrect format.
     *  @see #setExpression(String)
     */
    public VersionAttribute(String expression) throws IllegalActionException {
        super();
        setExpression(expression);
        setVisibility(Settable.NONE);
    }

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
    public VersionAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _tupleList = new LinkedList();
        setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare the value of this VersionAttribute against the argument
     *  according to the VersionAttribute syntax and padding rules.  For
     *  example:
     *  <p> "1.2.2-005" is greater than "1.2.2.4",
     *  <br> "1.3.1" is an greater than "1.3"
     *  <br> "1.3-beta" is an greater than "1.3-alpha"
     *  <b>
     *  Version-id contain one or more elements. When two version-id's
     *  are compared, they are normalized by padding the shortest
     *  version-id with additional elements containing "0".
     *  During comparison, if both elements can be parsed as Java
     *  <code>int</code>s, then they are compared as integers.  If the
     *  elements cannot be parsed as integers, they are compared as Strings.
     *
     *  @param object The VersionAttribute to compare against.
     *  @return A negative integer, zero, or a positive integer if this
     *   object is less than, equal to, or greater than the specified
     *   object, respectively.
     */
    public int compareTo(Object object) {
        VersionAttribute version = (VersionAttribute) object;
        Iterator versionTuples = version.iterator();
        Iterator tuples;
        if (_tupleList == null) {
            tuples = null;
        } else {
            tuples = _tupleList.iterator();
        }
        while (versionTuples.hasNext()
                || (tuples != null && tuples.hasNext())){
            String versionTuple, tuple;

            // FIXME: deal with * and + in the JNLP Version String spec.

            // Normalize the shortest tuple by padding with 0
            if (versionTuples.hasNext()) {
                versionTuple = (String)versionTuples.next();
            } else {
                versionTuple = "0";
            }
            if (tuples != null && tuples.hasNext()) {
                tuple = (String)tuples.next();
            } else {
                tuple = "0";
            }

            // If both elements can be parsed as Java ints, then
            // compare them as ints.  If not, then compare them
            // as Strings.

            try {
                // Try parsing as ints.
                int tupleInt = Integer.parseInt(tuple);
                int versionInt = Integer.parseInt(versionTuple);
                if (tupleInt < versionInt) {
                    return -1;
                } else if (tupleInt > versionInt) {
                    return 1;
                }
            } catch (NumberFormatException ex) {
                // Compare as Strings.
                int compare = tuple.compareTo(versionTuple);
                if (compare < 0) {
                    return -1;
                } else if (compare > 0) {
                    return 1;
                }
            }
        }
        return 0;
    }

    /** Return true if the specified object is an instance of
     *  VersionAttribute and represents the same version as this one.
     *  @return True if the specified version is the same as this one.
     */
    public boolean equals(Object obj) {
        // If the _tupleList is null, then we are not fully constructed.
        // Defer to the superclass, so that we return true if the argument
        // is the same object as this.
        if (_tupleList == null) {
            return super.equals(obj);
        }
        if (obj instanceof VersionAttribute) {
            return (compareTo(obj) == 0);
        }
        return false;
    }

    /** Return true if this version is less than the specified version.
     *  This method uses compareTo(), but may yield more readable code
     *  in certain circumstances.
     *  @see #compareTo(Object)
     *  @return True if this version is less than the specified version.
     */
    public boolean isLessThan(VersionAttribute version) {
        return (compareTo(version) < 0);
    }

    /** Return an iterator over the elements of the version,
     *  each of which is a String.
     *  @return An iterator over the elements of the version.
     */
    public Iterator iterator() {
        return _tupleList.iterator();
    }

    /** Set the value of the string attribute and notify the container
     *  of the value of this attribute by calling attributeChanged().
     *  Notify any value listeners of this attribute.
     *  @param expression The version string, consisting of
     *   version ID tuples separated by '.', '-' or '_'. For example:
     *   "1.2", "1.2_beta-4".
     *  @exception IllegalActionException If the argument contains a
     *   space, which violates the JNLP Version format specification.
     */
    public void setExpression(String expression)
            throws IllegalActionException {
        super.setExpression(expression);
        if (expression.indexOf(' ') != -1 ) {
            throw new IllegalActionException(this,
                    "Versions cannot contain spaces: '"
                    + expression + "'");
        }
        _tupleList = new LinkedList();
        StringTokenizer tokenizer = new StringTokenizer(expression, ".-_");
        while (tokenizer.hasMoreTokens()) {
            _tupleList.add(tokenizer.nextToken());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////


    /** The VersionAttribute that contains the version of the Ptolemy II
     *  release that is currently running.  This variable may be read
     *  to take action if the assumed version does not match the current
     *  version.  For example,
     *  <p>
     *  <pre>
     *  VersionAttribute assumedVersion = ...;
     *  if (VersionAttribute.CURRENT_VERSION.isLessThan(assumedVersion)) {
     *      throw new IllegalActionException("You need to upgrade!");
     *  }
     *  </pre>
     *  <p>
     *  Similarly, this variable may be used to change the Ptolemy II
     *  functionality depending on the version number:
     *  <p>
     *  <pre>
     *  if (VersionAttribute.CURRENT_VERSION.compareTo(
     *         new VersionAttribute("2.0")) >= 0 ) {
     *      // Perform some operation if the current version is
     *      // Ptolemy II 2.0 or later.
     *  }
     *  </pre>
     */
    public static final VersionAttribute CURRENT_VERSION;

    static {
        try {
            CURRENT_VERSION = new VersionAttribute("4.0-alpha");
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(
                    "Failed to create CURRENT_VERSION: "
                    + KernelException.stackTraceToString(ex));
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A List representation of the version.
    private List _tupleList;
}
