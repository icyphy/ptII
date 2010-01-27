/* Actor that collects the details of the lattice ontology solver resolution
 * on the constraints collected and produces string outputs.

 Copyright (c) 2003-2010 The Regents of the University of California.
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

import ptolemy.data.expr.StringParameter;
import ptolemy.domains.tester.lib.Testable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLModelAttribute;

///////////////////////////////////////////////////////////////////
//// GetCausalityInterface

/**
 Actor that collects the details of the lattice ontology solver resolution
 on the constraints collected and produces string outputs.
 This actor is meant mainly for testing the LatticeOntologySolver.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class CollectLatticeOntologySolverDetails extends MoMLModelAttribute implements Testable {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CollectLatticeOntologySolverDetails(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        solverName = new StringParameter(this, "solverName");
        solverName.setExpression("");
        
        trainedInitialSolverStatistics = new StringAttribute(this, "trainedInitialSolverStatistics");
        trainedInitialSolverStatistics.setExpression("");    
        
        trainedInitialSolverConstraints = new StringAttribute(this, "trainedInitialSolverConstraints");
        trainedInitialSolverConstraints.setExpression(""); 
        
        trainedResolvedSolverStatistics = new StringAttribute(this, "trainedResolvedSolverStatistics");
        trainedResolvedSolverStatistics.setExpression("");    
        
        trainedResolvedSolverConstraints = new StringAttribute(this, "trainedResolvedSolverConstraints");
        trainedResolvedSolverConstraints.setExpression(""); 
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////

    /** Name of the LatticeOntologySolver to collect information from. If this
     *  is does not refer to a solver existing in the model, an exception is thrown
     *  when the actor fires.
     */
    public StringParameter solverName;
    
    /** The string attribute holding the value of the initial solver statistics for
     *  the LatticeOntologySolver.
     */
    public StringAttribute trainedInitialSolverStatistics;
    
    /** The string attribute holding the value of the initial solver constraints for
     *  the LatticeOntologySolver.
     */
    public StringAttribute trainedInitialSolverConstraints;
    
    /** The string attribute holding the value of the resolved solver statistics for
     *  the LatticeOntologySolver.
     */
    public StringAttribute trainedResolvedSolverStatistics;
    
    /** The string attribute holding the value of the resolved solver constraints for
     *  the LatticeOntologySolver.
     */
    public StringAttribute trainedResolvedSolverConstraints;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////    
    
    /** Initialize the LatticeOntologySolver and check to see if the solverName refers to a LatticeOntologySolver in the model.
     *  If not, then throw an exception.
     *  
     *  @exception IllegalActionException If the solver cannot be found in the model.
     */
    public void initializeLatticeOntologySolver() throws IllegalActionException {
        String solverNameString = solverName.stringValue();
        _solver = (LatticeOntologySolver) ((CompositeEntity) getContainer()).getAttribute(solverNameString,
                LatticeOntologySolver.class);
        
        if (_solver == null) {
            throw new IllegalActionException(this, "There is no LatticeOntologySolver in the model named " +
                    solverNameString + ".");
        }
    }
    
    
    /** Test whether the details received from the LatticeOntologySolver
     *  matches the stored values in the actor.
     *  
     *  @exception IllegalActionException If the test fails and the results are different.
     */
    public void test() throws IllegalActionException {
        initializeLatticeOntologySolver();        
        Hashtable initialSolverInfo = _solver.getInitialSolverInformation();
        Hashtable resolvedSolverInfo = _solver.getResolvedSolverInformation();
        
        String currentInitialSolverStatsString = (String) initialSolverInfo.get("initialSolverStats");        
        if (currentInitialSolverStatsString == null ||
                !currentInitialSolverStatsString.equals(trainedInitialSolverStatistics.getValueAsString())) {
            throw new IllegalActionException(this, _solver, "Test failed: The initial solver statistics string collected from the " +
                    _solver.getName() + " LatticeOntologySolver does not match the trained value.\n" +
                    "Trained value:\n" + trainedInitialSolverStatistics.getValueAsString() + "\nCurrent value:\n" +
                    currentInitialSolverStatsString);
        }
        
        String currentInitialSolverConstraintsString = (String) initialSolverInfo.get("initialSolverConstraints");        
        if (currentInitialSolverConstraintsString == null ||
                !currentInitialSolverConstraintsString.equals(trainedInitialSolverConstraints.getValueAsString())) {
            throw new IllegalActionException(this, _solver, "Test failed: The initial solver constraints string collected from the " +
                    _solver.getName() + " LatticeOntologySolver does not match the trained value.\n" +
                    "Trained value:\n" + trainedInitialSolverConstraints.getValueAsString() + "\nCurrent value:\n" +
                    currentInitialSolverConstraintsString);
        }
        
        String currentResolvedSolverStatsString = (String) resolvedSolverInfo.get("resolvedSolverStats");        
        if (currentResolvedSolverStatsString == null ||
                !currentResolvedSolverStatsString.equals(trainedResolvedSolverStatistics.getValueAsString())) {
            throw new IllegalActionException(this, _solver, "Test failed: The resolved solver statistics string collected from the " +
                    _solver.getName() + " LatticeOntologySolver does not match the trained value.\n" +
                    "Trained value:\n" + trainedResolvedSolverStatistics.getValueAsString() + "\nCurrent value:\n" +
                    currentResolvedSolverStatsString);
        }
        
        String currentResolvedSolverConstraintsString = (String) resolvedSolverInfo.get("resolvedSolverConstraints");        
        if (currentResolvedSolverConstraintsString == null ||
                !currentResolvedSolverConstraintsString.equals(trainedResolvedSolverConstraints.getValueAsString())) {
            throw new IllegalActionException(this, _solver, "Test failed: The resolved solver constraints string collected from the " +
                    _solver.getName() + " LatticeOntologySolver does not match the trained value.\n" +
                    "Trained value:\n" + trainedResolvedSolverConstraints.getValueAsString() + "\nCurrent value:\n" +
                    currentResolvedSolverConstraintsString);
        }
    }
    
    /** Collect the solver details from the LatticeOntologySolver and store
     *  the values received in the actor for future tests.
     */
    public void train() {
        try {
            initializeLatticeOntologySolver();            
            Hashtable initialSolverInfo = _solver.getInitialSolverInformation();
            Hashtable resolvedSolverInfo = _solver.getResolvedSolverInformation();
            
            trainedInitialSolverStatistics.setExpression((String) initialSolverInfo.get("initialSolverStats"));
            trainedInitialSolverConstraints.setExpression((String) initialSolverInfo.get("initialSolverConstraints"));
            trainedResolvedSolverStatistics.setExpression((String) resolvedSolverInfo.get("resolvedSolverStats"));
            trainedResolvedSolverConstraints.setExpression((String) resolvedSolverInfo.get("resolvedSolverConstraints"));
        } catch (IllegalActionException ex) {
            _debug("Unable to train the CollectLatticeOntologySolverDetails " +
                    getName() + " attribute with values from the LatticeOntologySolver." +
                    " Exception thrown: " + ex);
        }            
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The LatticeOntologySolver from which details will be collected. */
    private LatticeOntologySolver _solver;
}
