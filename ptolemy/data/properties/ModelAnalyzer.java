/*  This actor opens a window to display the specified model and applies its inputs to the model.

 @Copyright (c) 1998-2007 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.data.properties;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.lattice.CalibrationSWConfigEDCCS;
import ptolemy.data.properties.lattice.DataControlConfigEDCCS;
import ptolemy.data.properties.lattice.EventContinuousEDCCS;
import ptolemy.data.properties.lattice.ImpactNoImpactEDCCS;
import ptolemy.data.properties.lattice.ModeSelectEDCCS;
import ptolemy.data.properties.lattice.OnlineOfflineEDCCS;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.SWconfig_Backward_EDCCS;
import ptolemy.data.properties.lattice.SWconfig_Forward_EDCCS;
import ptolemy.data.properties.lattice.StaticDynamicEDCCS;
import ptolemy.data.properties.lattice.StrongWeakNoConnectionEDCCS;
import ptolemy.data.properties.lattice.TypeSystemCCS;
import ptolemy.data.properties.lattice.TypeSystemEDCCS;
import ptolemy.data.properties.lattice.TypeSystemEDCCS_AUXtypes;
import ptolemy.data.properties.token.ExtendedFirstValuePTS;
import ptolemy.data.properties.token.FirstValuePTS;
import ptolemy.data.properties.token.OptimizationEDCPCSolver;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ModelGenerator

/**
This actor opens a window to display the specified model.
If inputs are provided, they are expected to be MoML strings
that are to be applied to the model. This can be used, for
example, to create animations.

@author  Man-Kit Leung
@version $Id$
@since Ptolemy II 6.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
*/
public class ModelAnalyzer extends Transformer {

    public ModelAnalyzer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.data.properties.ModelAnalyzer");

        property = new StringParameter(this, "property");
        action = new StringParameter(this, "action");
        _addChoices();

        showProperty = new Parameter(this, "showProperty");
        showProperty.setTypeEquals(BaseType.BOOLEAN);
        showProperty.setExpression("true");

        highlight = new Parameter(this, "highlight");
        highlight.setTypeEquals(BaseType.BOOLEAN);
        highlight.setExpression("true");

        logConstraint = new Parameter(this, "logConstraint");
        logConstraint.setTypeEquals(BaseType.BOOLEAN);
        logConstraint.setExpression("true");

        overwriteConstraint = new Parameter(this, "overwriteConstraint");
        overwriteConstraint.setTypeEquals(BaseType.BOOLEAN);
        overwriteConstraint.setExpression("false");

        overwriteDependentProperties = new Parameter(this, "overwriteDependentProperties");
        overwriteDependentProperties.setTypeEquals(BaseType.BOOLEAN);
        overwriteDependentProperties.setExpression("false");
        
        errorMessage = new TypedIOPort(this, "errorMessage", false, true);
        errorMessage.setTypeEquals(BaseType.STRING);
        
        input.setTypeEquals(ActorToken.TYPE);
        
        output.setTypeEquals(ActorToken.TYPE);        
    }

    private void _addChoices() throws IllegalActionException, NameDuplicationException {
        _createSolvers(this);

        property.setExpression(solvers[0].getExtendedUseCaseName());

        for (PropertySolver solver : solvers) {
            property.addChoice(solver.getExtendedUseCaseName());
        }
        property.addChoice("Clear All");
        
        PropertySolver._addActions(action);
    }

    /** React to a change in an attribute. 
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        
        super.attributeChanged(attribute);
    }
    
    public Object clone() throws CloneNotSupportedException {
        ModelAnalyzer actor = (ModelAnalyzer) super.clone();
        return actor;
    }

    public void fire() throws IllegalActionException {
        String errorString = "";
        
        ActorToken token = (ActorToken) input.get(0);
        CompositeEntity entity = (CompositeEntity) token.getEntity();
        
        String propertyValue = property.getExpression();
        
        if (propertyValue.equals("Clear All")) {
            try {
                PropertyRemover remover = new PropertyRemover(entity, "ModelAnalyzerClearAll");
                remover.removeProperties(entity);
            } catch (NameDuplicationException e) {
                assert false;
            }            
        } else {
            
            String actionValue = action.getExpression();
            
            try {
                _findSolvers(entity);
                PropertySolver chosenSolver = null;
                for (PropertySolver solver : solvers) {
                    if (propertyValue.equals(
                            solver.getExtendedUseCaseName())) {
                        chosenSolver = solver;
                        break;
                    }
                }

                if (chosenSolver instanceof PropertyConstraintSolver) {
                    ((PropertyConstraintSolver) chosenSolver).logConstraints.setToken(logConstraint.getToken());
                }
    
                // Clear the resolved properties for the chosen solver.
                if (actionValue.equals(PropertySolver.CLEAR)) {
                    chosenSolver.clearDisplay();
                    chosenSolver.clearProperties();
                } else if (actionValue.equals(PropertySolver.VIEW)) {
                    _displayProperties(chosenSolver);
                    
                } else {
                    chosenSolver.setAction(actionValue);
                    chosenSolver.resolveProperties(this);
                    chosenSolver.updateProperties();
                    chosenSolver.checkRegressionTestErrors();
                    
                    _displayProperties(chosenSolver);
                }            
            } catch (PropertyFailedRegressionTestException ex) {
                errorString = KernelException.generateMessage(
                        entity, null, ex, "Failed: Property regression test failed.") + "\n\n";
                
            } catch (KernelException e) {
                throw new IllegalActionException(this, e, "");
            } 
            /*catch (KernelException ex) {
                errorMessage.send(0, new StringToken(KernelException.generateMessage(
                        entity, null, ex, "Failed: Checking/annotating failed while in progress.") + "\n\n"));
            } */
            finally {
//                _removeSolvers(entity);
            }
        }
        errorMessage.send(0, new StringToken(errorString));
        output.send(0, new ActorToken(entity));
    }

    /**
     * @param chosenSolver
     * @throws IllegalActionException
     */
    private void _displayProperties(PropertySolver chosenSolver) 
    throws IllegalActionException {
        
        chosenSolver.clearDisplay();
        
        Token oldValue = chosenSolver._highlighter.showText.getToken();            
        chosenSolver._highlighter.showText.setToken(showProperty.getToken());
        chosenSolver.showProperties();
        chosenSolver._highlighter.showText.setToken(oldValue);

        oldValue = chosenSolver._highlighter.highlight.getToken();            
        chosenSolver._highlighter.highlight.setToken(highlight.getToken());
        chosenSolver.highlightProperties();
        chosenSolver._highlighter.highlight.setToken(oldValue);
    }

    private void _createSolvers(Entity entity) throws IllegalActionException {
        try {
            solvers[0] = new OptimizationEDCPCSolver(entity, "ModelAnalyzer_OptimizationSolver_EDC");
            solvers[1] = new StaticDynamicEDCCS(entity, "ModelAnalyzer_StaticDynamicSolver_EDC");
            solvers[2] = new TypeSystemEDCCS(entity, "ModelAnalyzer_TypeSystemSolver_EDC");
            solvers[3] = new OnlineOfflineEDCCS(entity, "ModelAnalyzer_OnlineOfflineSolver_EDC");
            solvers[4] = new ImpactNoImpactEDCCS(entity, "ModelAnalyzer_ImpactNoImpactSolver_EDC");
            solvers[5] = new ExtendedFirstValuePTS(entity, "ModelAnalyzer_ExtendedFirstValuePTS");
            solvers[6] = new FirstValuePTS(entity, "ModelAnalyzer_FirstValuePTS");
            solvers[7] = new CalibrationSWConfigEDCCS(entity, "ModelAnalyzer_CalibrationSWConfig");
            solvers[8] = new DataControlConfigEDCCS(entity, "ModelAnalyzer_DataControlConfig");
            solvers[9] = new EventContinuousEDCCS(entity, "ModelAnalyzer_EventContinuous");
            solvers[10] = new ModeSelectEDCCS(entity, "ModelAnalyzer_ModeSelect");
            solvers[11] = new StrongWeakNoConnectionEDCCS(entity, "ModelAnalyzer_StrongWeakNoConnection");
            solvers[12] = new TypeSystemEDCCS_AUXtypes(entity, "ModelAnalyzer_TypeSystemAUXtypes");
            solvers[13] = new TypeSystemCCS(entity, "ModelAnalyzer_TypeSystemCCS");
            solvers[14] = new SWconfig_Forward_EDCCS(entity, "ModelAnalyzer_SWconfig_Forward_EDCCS");
            solvers[15] = new SWconfig_Backward_EDCCS(entity, "ModelAnalyzer_SWconfig_Backward_EDCCS");
        } catch (NameDuplicationException e) {
            assert false;
        }
    }
    
    private void _findSolvers(Entity entity) throws IllegalActionException, NameDuplicationException {
        for (PropertySolver solver : (List<PropertySolver>) entity.attributeList(PropertySolver.class)) {
            if (solver instanceof OptimizationEDCPCSolver) {
                solvers[0] = solver;
            } else if (solver instanceof StaticDynamicEDCCS){
                solvers[1] = solver;
            } else if (solver instanceof TypeSystemEDCCS){
                solvers[2] = solver;
            } else if (solver instanceof OnlineOfflineEDCCS){
                solvers[3] = solver;
            } else if (solver instanceof ImpactNoImpactEDCCS){
                solvers[4] = solver;
            } else if (solver instanceof ExtendedFirstValuePTS){
                solvers[5] = solver;
            } else if (solver instanceof FirstValuePTS){
                solvers[6] = solver;
            } else if (solver instanceof CalibrationSWConfigEDCCS){
                solvers[7] = solver;
            } else if (solver instanceof DataControlConfigEDCCS){
                solvers[8] = solver;
            } else if (solver instanceof EventContinuousEDCCS){
                solvers[9] = solver;
            } else if (solver instanceof ModeSelectEDCCS){
                solvers[10] = solver;
            } else if (solver instanceof StrongWeakNoConnectionEDCCS){
                solvers[11] = solver;
            } else if (solver instanceof TypeSystemEDCCS_AUXtypes){
                solvers[12] = solver;
            } else if (solver instanceof TypeSystemCCS){
                solvers[13] = solver;
            } else if (solver instanceof SWconfig_Forward_EDCCS){
                solvers[14] = solver;
            } else if (solver instanceof SWconfig_Backward_EDCCS){
                solvers[15] = solver;
            }
        }
    }

    public Parameter action;
    
    /** Whether to display the annotated property or not.
     */
    public Parameter showProperty;

    public Parameter highlight;

    public Parameter logConstraint;

    public Parameter overwriteConstraint;

    public Parameter overwriteDependentProperties;

    /** The property to analyze.
     */
    public Parameter property;
    
    public TypedIOPort errorMessage;
    
    private PropertySolver[] solvers = new PropertySolver[16];
    
}
