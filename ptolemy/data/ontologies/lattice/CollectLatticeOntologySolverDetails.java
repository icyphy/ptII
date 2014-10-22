/* Model attribute that collects the details of the lattice ontology solver resolution
 * on the constraints collected and produces string outputs.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

import java.util.Hashtable;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.tester.lib.Testable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// CollectLatticeOntologySolverDetails

/** Model attribute that collects the details of the lattice ontology solver resolution
 *  on the constraints collected and produces string outputs.
 *  This attribute is meant mainly for testing the LatticeOntologySolver.
 *
 *  @author Charles Shelton, Beth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class CollectLatticeOntologySolverDetails extends Attribute implements
        Testable {

    /** Construct the CollectLatticeOntologySolverDetails attribute
     *  with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CollectLatticeOntologySolverDetails(CompositeEntity container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        solverName = new StringParameter(this, "solverName");
        solverName.setExpression("");

        trainedInitialSolverConstraints = new StringAttribute(this,
                "trainedInitialSolverConstraints");
        trainedInitialSolverConstraints.setExpression("");
        TextStyle trainedStyle = new TextStyle(trainedInitialSolverConstraints,
                "_style");
        trainedStyle.height.setExpression("10");
        trainedStyle.width.setExpression("80");

        trainedResolvedSolverConstraints = new StringAttribute(this,
                "trainedResolvedSolverConstraints");
        trainedResolvedSolverConstraints.setExpression("");
        TextStyle resolvedStyle = new TextStyle(
                trainedResolvedSolverConstraints, "_style");
        resolvedStyle.height.setExpression("10");
        resolvedStyle.width.setExpression("80");

        unacceptableTerms = new StringAttribute(this, "unacceptableTerms");
        unacceptableTerms.setExpression("");
        TextStyle unacceptableTermsStyle = new TextStyle(unacceptableTerms,
                "_style");
        unacceptableTermsStyle.height.setExpression("10");
        unacceptableTermsStyle.width.setExpression("80");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"120\" height=\"40\" "
                + "style=\"fill:orange\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "OntologySolver\nResolution Details</text></svg>");
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////

    /** Name of the LatticeOntologySolver to collect information from. If this
     *  is does not refer to a solver existing in the model, an exception is thrown
     *  when the actor fires.
     */
    public StringParameter solverName;

    /** The string attribute holding the value of the initial solver constraints for
     *  the LatticeOntologySolver.
     */
    public StringAttribute trainedInitialSolverConstraints;

    /** The string attribute holding the value of the resolved solver constraints for
     *  the LatticeOntologySolver.
     */
    public StringAttribute trainedResolvedSolverConstraints;

    /** The string attribute holding the list of terms that resolved to
     *  unacceptable concepts for the LatticeOntologySolver.
     *  For information only; not checked in the test() method.
     */
    public StringAttribute unacceptableTerms;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Test whether the details received from the LatticeOntologySolver
     *  matches the stored values in the actor.
     *
     *  @exception IllegalActionException If the test fails and the results are different.
     */
    @Override
    public void test() throws IllegalActionException {
        _findSolver();
        Hashtable<String, String> initialSolverInfo = _solver
                .getInitialSolverInformation();
        Hashtable<String, String> resolvedSolverInfo = _solver
                .getResolvedSolverInformation();

        String currentInitialSolverConstraintsString = initialSolverInfo
                .get("initialSolverConstraints");
        if (currentInitialSolverConstraintsString == null
                || !currentInitialSolverConstraintsString
                        .equals(trainedInitialSolverConstraints
                                .getValueAsString())) {
            throw new IllegalActionException(
                    this,
                    _solver,
                    "Test failed: The initial solver constraints string collected from the "
                            + _solver.getName()
                            + " LatticeOntologySolver does not match the trained value.\n"
                            + "Trained value:\n"
                            + trainedInitialSolverConstraints
                                    .getValueAsString() + "\nCurrent value:\n"
                            + currentInitialSolverConstraintsString);
        }

        String currentResolvedSolverConstraintsString = resolvedSolverInfo
                .get("resolvedSolverConstraints");
        if (currentResolvedSolverConstraintsString == null
                || !currentResolvedSolverConstraintsString
                        .equals(trainedResolvedSolverConstraints
                                .getValueAsString())) {
            throw new IllegalActionException(
                    this,
                    _solver,
                    "Test failed: The resolved solver constraints string collected from the "
                            + _solver.getName()
                            + " LatticeOntologySolver does not match the trained value.\n"
                            + "Trained value:\n"
                            + trainedResolvedSolverConstraints
                                    .getValueAsString() + "\nCurrent value:\n"
                            + currentResolvedSolverConstraintsString);
        }
    }

    /** Collect the solver details from the LatticeOntologySolver and store
     *  the values received in the actor for future tests.
     *  @exception IllegalActionException If <i>solverName</i> does not refer
     *   to a solver in the model, or if the solver throws it when getting
     *   constraints.
     */
    @Override
    public void train() throws IllegalActionException {
        _findSolver();
        Hashtable<String, String> initialSolverInfo = _solver
                .getInitialSolverInformation();
        Hashtable<String, String> resolvedSolverInfo = _solver
                .getResolvedSolverInformation();

        trainedInitialSolverConstraints.setExpression(initialSolverInfo
                .get("initialSolverConstraints"));
        trainedResolvedSolverConstraints.setExpression(resolvedSolverInfo
                .get("resolvedSolverConstraints"));
        unacceptableTerms.setExpression(_solver.getUnacceptableTermsAsString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the solver referred to by the <i>solverName</i> parameter and
     *  set the variable _solver to point to it.
     *  @exception IllegalActionException If the solver cannot be found in the model.
     */
    private void _findSolver() throws IllegalActionException {
        String solverNameString = solverName.stringValue();
        _solver = (LatticeOntologySolver) ((CompositeEntity) getContainer())
                .getAttribute(solverNameString, LatticeOntologySolver.class);

        if (_solver == null) {
            throw new IllegalActionException(this,
                    "There is no LatticeOntologySolver in the model named "
                            + solverNameString + ".");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The LatticeOntologySolver from which details will be collected. */
    private LatticeOntologySolver _solver;
}
