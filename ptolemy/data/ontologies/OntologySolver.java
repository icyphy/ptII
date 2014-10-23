/*
 * A extended base abstract class for an ontology solver.
 *
 * Copyright (c) 1998-2014 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
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
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 */
package ptolemy.data.ontologies;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import ptolemy.domains.tester.lib.Testable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// OntologySolver

/** An extended base abstract class for an ontology solver.
 *
 *  @author Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 */
public abstract class OntologySolver extends OntologySolverBase implements
Testable {

    /**
     * Construct an OntologySolver with the specified container and name. If this
     * is the first OntologySolver created in the model, the shared utility
     * object will also be created.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the PropertySolver is not of an
     * acceptable attribute for the container.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public OntologySolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _momlHandler = new OntologyMoMLHandler(this, "OntologyMoMLHandler");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Check whether there are any regression testing errors after resolving
     * properties. If so, throw a new PropertyResolutionException with
     * an error message that includes all the properties that does not match the
     * regression test values.
     *
     * @exception OntologyResolutionException Thrown if there are any
     * errors from running the OntologySolver in the regression test.
     */
    public void checkErrors() throws OntologyResolutionException {

        List<String> errors = _ontologySolverUtilities.removeErrors();
        Collections.sort(errors);

        if (!errors.isEmpty()) {
            // String errorMessage = errors.toString();

            // FIXME:  Replace with proper error logging.  See java.util.logging
            // throw new OntologyResolutionException(this, errorMessage);
        }
    }

    /**
     * Check whether there are any OntologySolver resolution errors after resolving
     * properties. If so, throw an IllegalActionException.
     *
     * @exception IllegalActionException If an exception is thrown by calling checkErrors()
     */
    public void checkResolutionErrors() throws IllegalActionException {
        for (Object propertyable : getAllPropertyables()) {
            _recordUnacceptableSolution(propertyable, getConcept(propertyable));
        }
        checkErrors();
    }

    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        OntologySolver newObject = (OntologySolver) super.clone(workspace);
        newObject._momlHandler = null;
        return newObject;
    }

    /** Construct and configure the contained model with the specified source and
     *  text. This parses the specified MoML text. Also set the container solver
     *  for the contained model to be the ontology solver.
     *  @param base The base URL for relative references, or null if not known.
     *  @param source The URI of a document providing source, which is ignored in this class.
     *  @param text The MoML description.
     *  @exception Exception If the parsing fails.
     */
    @Override
    public void configure(URL base, String source, String text)
            throws Exception {
        OntologySolverBase.cleanConstants();

        super.configure(base, source, text);

        if (!(_model instanceof OntologySolverModel)) {
            throw new IllegalActionException(this, "An OntologySolver can "
                    + "only contain entities of the type OntologySolverModel.");
        }
        ((OntologySolverModel) _model).setContainerSolver(this);
    }

    /**
     * If the value of the highlight parameter is set to true, highlight the
     * given property-able object with the specified color associated with the
     * given property, if there exists any. If the value of the showText
     * parameter is true, show the given property value for the given
     * property-able object. If the property is not null, this looks for the
     * _showInfo parameter in the property-able object. Create a new _showInfo
     * StringParameter if one does not already exist. Set its value to
     * the given property value. If the given property is null, this removes the
     * _showInfo parameter from the property-able object.
     * @exception IllegalActionException Thrown if an error occurs when creating
     * or setting the value for the _showInfo parameter in the property-able
     * object. Thrown if an error occurs when creating or setting the value for
     * the highlightColor attribute in the property-able object.
     */
    public void displayConcepts() throws IllegalActionException {
        _momlHandler.highlightConcepts();
        _momlHandler.showConceptAnnotations();
    }

    /**
     * Return the PropertyMoMLHandler for this OntologySolver.
     *
     * @return The PropertyMoMLHandler for the OntologySolver
     */
    public OntologyMoMLHandler getMoMLHandler() {
        return _momlHandler;
    }

    /** Invoke the solver directly, and display any concepts resolved by this
     *  solver.
     *  @exception IllegalActionException If there is no ontology.
     */
    public void invokeSolver() throws IllegalActionException {
        invokeSolver(true);
    }

    /** Invoke the solver directly, with a choice as to whether or not this
     *  solver should display its resolved concepts.
     *
     *  @param displayProperties  True if the solver should display its
     *          properties; false otherwise (for example, if it is called
     *          from another solver)
     *  @exception IllegalActionException If there is no ontology.
     */
    public void invokeSolver(boolean displayProperties)
            throws IllegalActionException {
        Ontology ontology = getOntology();
        if (ontology == null) {
            throw new IllegalActionException(this,
                    "No ontology has been given.");
        }
        initialize();
        resolveConcepts();
        checkErrors();

        if (displayProperties) {
            displayConcepts();
        }
    }

    /**
     * Return true if the specified property-able object is settable; otherwise
     * false which means that its concept has been set by
     * OntologyAdapter.setEquals().
     * @param object The specified property-able object.
     * @return True if the specified property-able object is settable, otherwise
     * false.
     */
    public boolean isSettable(Object object) {
        return !_nonSettables.contains(object);
    }

    /**
     * Reset the solver. This removes the internal states of the solver (e.g.
     * previously recorded properties, statistics, etc.).  It also removes
     * this solver from the list of ran solvers.
     */
    @Override
    public void reset() {
        super.reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The handler that issues MoML requests and makes model changes.
     */
    protected OntologyMoMLHandler _momlHandler;

    /**
     * The system-specific end-of-line character.
     */
    protected static final String _eol = StringUtilities
            .getProperty("line.separator");

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Record as an error for the given property-able object and its resolved
     * property. If the given property is null, it does nothing. If the given
     * property is unacceptable, an error is recorded for the given
     * property-able object and the property.
     *
     * @param propertyable The model object to which the concept is attached.
     * @param property The concept attached to the model object.
     */
    private void _recordUnacceptableSolution(Object propertyable,
            Concept property) {

        // Check for unacceptable solution.
        if (property != null && !property.isValueAcceptable()) {
            _ontologySolverUtilities.addErrors("Property \"" + property
                    + "\" is not an acceptable solution for " + propertyable
                    + "." + _eol);
        }
    }

}
