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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;

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
        _createSolvers("ptolemy.data.properties.configuredSolvers");

        if (solvers.size() > 0) {
            property.setExpression(solvers.get(0).getSimpleName());
        }
        
        for (Class solver : solvers) {
            property.addChoice(solver.getSimpleName());
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
                PropertySolver chosenSolver = null;
                for (Class solver : solvers) {
                    if (propertyValue.equals(
                            solver.getSimpleName())) {
                        try {
                            Constructor constructor = 
                                solver.getConstructor(NamedObj.class, String.class);
                            
                            constructor.newInstance(entity, "ModelAnalyzer_" + solver.getSimpleName());
                        } catch (Exception ex) {
                            assert false;
                        }
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

    private void _createSolvers(String path) throws IllegalActionException {
        File file = null;
        
        try {
            file = new File(FileUtilities.nameToURL(
                    "$CLASSPATH/ptolemy/data/properties/configuredSolvers", 
                    null, null).getFile());
        } catch (IOException ex) {
            // Should not happen.
            assert false;
        }

        File[] classFiles = file.listFiles();
        
        for (int i = 0; i < classFiles.length; i++) {
            String className = classFiles[i].getName();
            
            path += className;
            if (classFiles[i].isDirectory() && !className.equals("CVS") && !className.equals(".svn")) {
                // Search sub-folder. 
                _createSolvers(path);
            } else {
                try {
                    solvers.add(Class.forName(path));
                } catch (ClassNotFoundException e) {
                    assert false;
                }
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
    
    private List<Class> solvers = new LinkedList<Class>();
    
}
