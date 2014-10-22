/*
 * The base class of a concept attribute.
 *
 * Below is the copyright agreement for the Ptolemy II system.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
 * rights reserved.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.ontologies;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.util.StringUtilities;

/**
 * The base class of a concept attribute. A ConceptAttribute contains the
 * concept value annotated by the solver.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class ConceptAttribute extends AbstractSettableAttribute {

    /**
     * Construct a ConceptAttribute with the specified name and container.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the attribute is not of an
     * acceptable class for the container, or if the name contains a period.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public ConceptAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add the specified ValueListener. Currently this class doesn't support
     * any listeners, so this method does nothing.
     *
     *  @param listener The listener to add.
     *  @see #removeValueListener
     */
    @Override
    public void addValueListener(ValueListener listener) {
        /* This method is required since ConceptAttribute extends AbstractSettableAttribute
         * which implements the ptolemy.kernel.util.Settable interface.
         * But we do not currently support any listeners in this class.
         */
        return;
    }

    /**
     * Write a MoML description of the ConceptAttribute. Nothing is written if
     * the value is null or "".
     * @param output The output stream to write to.
     * @param depth The depth in the hierarchy, to determine indenting.
     * @param name The name to use instead of the current name.
     * @exception IOException If an I/O error occurs.
     * @see ptolemy.kernel.util.NamedObj#exportMoML(Writer, int, String)
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        String value = getExpression();
        String valueTerm = "";

        if (value != null && !value.equals("")) {
            valueTerm = " value=\"" + StringUtilities.escapeForXML(value)
                    + "\"";

            output.write(_getIndentPrefix(depth) + "<" + _elementName
                    + " name=\"" + name + "\" class=\"" + getClassName() + "\""
                    + valueTerm + ">\n");
            _exportMoMLContents(output, depth + 1);
            output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
        }
    }

    /**
     * Get the string name of the Concept contained by the ConceptAttribute
     * or the empty string if there is none.
     *
     * @return The name of the Concept as a string, or the empty string "".
     */
    @Override
    public String getExpression() {
        return _concept == null ? "" : _concept.toString();
    }

    /**
     * Returns the Concept value.
     *
     * @return a Concept object representing the Concept value
     * contained by the ConceptAttribute.
     * @see #setConcept
     */
    public Concept getConcept() {
        return _concept;
    }

    /** Get the visibility of this Settable, as set by setVisibility().
     *  If setVisibility() has not been called, then this method returns
     *  the default Settable.FULL. The returned value is one of the static
     *  instances of the Visibility inner class.
     *  @return The visibility of this Settable.
     *
     *  @see #setVisibility(Settable.Visibility)
     */
    @Override
    public Visibility getVisibility() {
        return _visibility;
    }

    /**
     * Remove the specified ValueListener. Currently this class doesn't support
     * any listeners, so this method does nothing.
     *
     * @param listener The listener to remove.
     * @see #addValueListener
     */
    @Override
    public void removeValueListener(ValueListener listener) {
        /* This method is required since ConceptAttribute extends AbstractSettableAttribute
         * which implements the ptolemy.kernel.util.Settable interface.
         * But we do not currently support any listeners in this class.
         */
        return;
    }

    /**
     * Specifies the Concept value.
     *
     * @param concept a Concept object specifying the Concept value
     * contained by the ConceptAttribute.
     * @see #getConcept
     */
    public void setConcept(Concept concept) {
        _concept = concept;
    }

    /** Set the visibility of this Settable.  The argument should be one
     *  of the static public instances of the inner class Visibility.
     *  This is enforced by making it impossible to construct instances
     *  of this inner class outside this interface definition.

     *  @param visibility The visibility of this Settable.
     *  @see #getVisibility()
     */
    @Override
    public void setVisibility(Visibility visibility) {
        _visibility = visibility;
    }

    /**
     * The validate() method must be implemented to implement the
     * {@linkplain ptolemy.kernel.util.Settable Settable}
     * interface but is not relevant
     * for the ConceptAttribute class.
     *
     * @return null
     * @exception IllegalActionException To match the signature of the
     * {@linkplain ptolemy.kernel.util.Settable#validate validate()}
     * method in the {@linkplain ptolemy.kernel.util.Settable Settable}
     * interface, but this is not ever thrown here.
     */
    @Override
    public Collection<?> validate() throws IllegalActionException {
        // not relevant
        return null;
    }

    /** The Concept contained by the ConceptAttribute. */
    protected Concept _concept;

    //    private Visibility _visibility = Settable.NONE;

    /** The visibility property of this attribute. */
    private Visibility _visibility = Settable.FULL;

}
