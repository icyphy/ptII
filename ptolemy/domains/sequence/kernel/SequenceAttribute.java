/* SequenceAttribute is a subclass of Parameter with support for integerToken.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

import java.util.Collection;
import java.util.regex.Pattern;

import ptolemy.actor.CompositeActor;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SequenceAttribute

/**
 * The sequence number for actor in the sequence domain.
 * This parameter stores an integer value that is required to be unique within
 * the portion of the model under the control of a single SequenceDirector.
 * Duplicate sequence numbers will trigger an exception.
 * FIXME: Shouldn't these numbers be forced to be unique, like by their
 * position in the XML file?
 * @author Elizabeth Latronico (Bosch)
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 */
public class SequenceAttribute extends Parameter implements Comparable {
    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public SequenceAttribute() {
        super();

    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public SequenceAttribute(Workspace workspace) {
        super(workspace);

    }

    /** Construct an attribute with the given name contained by the specified
     *  container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     *   type is set to Integer for sequence number
     */
    public SequenceAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setTypeEquals(BaseType.INT);

        _attachText("_iconDescription", "<svg>\n" + "<line x=\"-30\" y=\"-2\" "
                + "width=\"60\" height=\"4\" " + "style=\"fill:red\"/>\n"
                + "<rect x=\"15\" y=\"-10\" " + "width=\"4\" height=\"20\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Implement compareTo method to compare sequence numbers
     *  Only the sequence numbers are compared (independent of any process name).
     *
     *  Object may be a SequenceAttribute or a SequenceAttribute
     *  @param object The SequenceAttribute object.
     *  @return 0 if the sequence numbers are the same.
     */
    @Override
    public int compareTo(Object object) {

        try {
            int sequenceNumber1 = this.getSequenceNumber();
            int sequenceNumber2 = 0;

            // Check for either a SequenceAttribute or SequenceAttribute (which is a SequenceAttribute)
            if (object instanceof SequenceAttribute) {
                // If the second object is a SequenceAttribute, use the correct getSequenceNumber()
                // FIXME:  Is this needed, or is it OK just to use (SequenceAttribute) x.getSequenceNumber()?
                // FIXME:  This is bad coding style, because SequenceAtribute should not know about
                // its subclass SequenceAttribute - refactor?
                if (object instanceof ProcessAttribute) {
                    sequenceNumber2 = ((ProcessAttribute) object)
                            .getSequenceNumber();
                } else {
                    sequenceNumber2 = ((SequenceAttribute) object)
                            .getSequenceNumber();
                }

                if (sequenceNumber1 < sequenceNumber2) {
                    return -1;
                } else if (sequenceNumber1 > sequenceNumber2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } catch (IllegalActionException e) {
            throw new IllegalArgumentException(
                    "Invalid SequenceAttribute passed to compareTo method.", e);
        }

        throw new IllegalArgumentException(
                "SequenceAttribute can only be compared to other"
                        + " instances of SequenceAttribute.");
    }

    /** Return true if this SequenceAttribute has the same sequence
     *  number as the given SequenceAttribute.
     *  @param sequenceAttribute The SequenceAttribute object that this
     *  SequenceAttribute object is compared to.
     *  @return True if the two SequenceAttribute objects have the same
     *  sequence number, name and workspace
     */
    @Override
    public boolean equals(Object sequenceAttribute) {
        // See http://www.technofundo.com/tech/java/equalhash.html

        /* FindBugs says that SequenceAttribute "defined
         * compareTo(Object) and uses Object.equals()"
         * http://findbugs.sourceforge.net/bugDescriptions.html#EQ_COMPARETO_USE_OBJECT_EQUALS
         * says: "This class defines a compareTo(...) method but
         * inherits its equals() method from
         * java.lang.Object. Generally, the value of compareTo should
         * return zero if and only if equals returns true. If this is
         * violated, weird and unpredictable failures will occur in
         * classes such as PriorityQueue. In Java 5 the
         * PriorityQueue.remove method uses the compareTo method,
         * while in Java 6 it uses the equals method.
         *
         *  From the JavaDoc for the compareTo method in the
         *  Comparable interface:
         *
         * It is strongly recommended, but not strictly required that
         * (x.compareTo(y)==0) == (x.equals(y)). Generally speaking,
         * any class that implements the Comparable interface and
         * violates this condition should clearly indicate this
         * fact. The recommended language is "Note: this class has a
         * natural ordering that is inconsistent with equals." "
         */
        if (sequenceAttribute == this) {
            return true;
        }
        if (sequenceAttribute == null
                || sequenceAttribute.getClass() != getClass()) {
            return false;
        } else {
            SequenceAttribute attribute = (SequenceAttribute) sequenceAttribute;
            if (compareTo(attribute) == 0
                    && getFullName().equals(attribute.getFullName())
                    && workspace().equals(attribute.workspace())) {
                return true;
            }
        }
        return false;
    }

    /** Construct an attribute with the given name contained by the specified
    /** Returns the sequence number as an int, or 0 if there is none.
     *
     * @return int sequence number
     * @exception IllegalActionException If there is a problem getting the token value.
     */
    public int getSequenceNumber() throws IllegalActionException {
        // FIXME:  0 is actually a valid sequence number - want different default return?
        int seqNumber = 0;

        // Return the attribute token value as an integer
        IntToken token = (IntToken) getToken();
        if (token != null) {
            seqNumber = token.intValue();
        }

        // Check to make sure sequence number is positive or zero.
        if (seqNumber < 0) {
            throw new IllegalActionException(this, "In SequenceAttribute "
                    + getName()
                    + " the sequence number must be a positive integer. "
                    + "It cannot be zero or negative.");
        }

        return seqNumber;
    }

    /** Implement validate method to validate the SequenceAttribute and ProcessAttributes.
     *  @return The current list of value listeners, which are evaluated
     *   as a consequence of this call to validate().
     *  @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public Collection validate() throws IllegalActionException {

        Collection result = null;
        result = super.validate();
        NamedObj container = getContainer();
        String token = "";
        StringBuffer sbf = new StringBuffer();
        if (container != null) {

            // Beth 10/14/08 - added check for no director
            // If there is no director, don't need any of these warnings

            if (((CompositeActor) container.getContainer()).getDirector() != null) {

                if (this.getClass() == SequenceAttribute.class
                        && ((CompositeActor) container.getContainer())
                        .getDirector().getClass() == ProcessDirector.class) {

                    sbf.append("Warning: " + container.getName()
                            + "'s Sequence Attribute will be ignored");
                    System.out.println(sbf);
                }
                if (this.getClass() == ProcessAttribute.class
                        && ((CompositeActor) container.getContainer())
                        .getDirector().getClass() == SequenceDirector.class) {
                    sbf.append("Warning: " + container.getName()
                            + "'s Process Attribute will be ignored");
                    System.out.println(sbf);
                }

                String changedToken = this.getToken().toString();

                if (changedToken != null && !changedToken.equals("")
                        && changedToken.contains("{")) {
                    changedToken = changedToken.replace("\'", "").replace("\"",
                            "");
                    String tokens[] = changedToken.split(",");

                    for (int i = 0; i < tokens.length; i++) {
                        token = tokens[i].replace("{", "").replace("}", "")
                                .trim();

                        // Beth changed 01/19/09
                        // Want to allow any non-whitespace character as a process name
                        if (i == 0 && !Pattern.matches("[^\\s]+", token)) {
                            System.out
                            .println("Warning for actor "
                                    + container.getName()
                                    + ": A process name must have at least one character; please change atrribute: "
                                    + this.getToken().toString());
                        }

                        // Beth changed 01/19/09
                        // The sequence number must be a non-blank number
                        // (we want [\\d]+, not [\\d]*, because [\\d]* would match a zero-length
                        // expression, i.e. an empty string)
                        if (i == 1 && !Pattern.matches("[\\d]+", token)) {
                            System.out
                            .println("Warning for actor "
                                    + container.getName()
                                    + ": A sequence number must be at least one digit; please change atrribute: "
                                    + this.getToken().toString());
                        }

                    }
                }
            }
        }
        return result;
    }

    /** Return the hash code for this SequenceAttribute object.  If two
     *  SequenceAttribute objects contain the same processName,
     *  methodName and have the same sequence number, then they will
     *  have the same hashCode.
     *  @return The hash code for this TimedEvent object.
     */
    @Override
    public int hashCode() {
        // See http://www.technofundo.com/tech/java/equalhash.html
        int hashCode = 32;
        try {
            hashCode = 31 * hashCode + getSequenceNumber();
            // Don't call getFullName(), it calls hashCode()!
            String name = getName();
            if (name != null) {
                hashCode = 31 * hashCode + name.hashCode();
            }
            Workspace workspace = workspace();
            if (workspace != null) {
                hashCode = 31 * hashCode + workspace.hashCode();
            }
        } catch (IllegalActionException ex) {
            return hashCode;
        }
        return hashCode;
    }
}
