/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) Mode-2009 The Regents of the University of California.
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
*/
package ptolemy.data.properties;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;

public class AnalyzerAttribute extends Attribute {

    public AnalyzerAttribute(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        property = new StringParameter(container, "property");
        action = new StringParameter(container, "action");
        _addChoices();

        showProperty = new Parameter(container, "showProperty");
        showProperty.setTypeEquals(BaseType.BOOLEAN);
        showProperty.setExpression("true");

        highlight = new Parameter(container, "highlight");
        highlight.setTypeEquals(BaseType.BOOLEAN);
        highlight.setExpression("true");

        logMode = new Parameter(container, "logMode");
        logMode.setTypeEquals(BaseType.BOOLEAN);
        logMode.setExpression("true");

        overwriteConstraint = new Parameter(container, "overwriteConstraint");
        overwriteConstraint.setTypeEquals(BaseType.BOOLEAN);
        overwriteConstraint.setExpression("false");

        overwriteDependentProperties = new Parameter(container, "overwriteDependentProperties");
        overwriteDependentProperties.setTypeEquals(BaseType.BOOLEAN);
        overwriteDependentProperties.setExpression("false");

    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    public Parameter action;

    public Parameter highlight;

    public Parameter logMode;

    public Parameter overwriteConstraint;

    public Parameter overwriteDependentProperties;

    /** The property to analyze.
     */
    public Parameter property;

    /** Whether to display the annotated property or not.
     */
    public Parameter showProperty;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public String analyze(CompositeEntity entity)
    throws IllegalActionException {
        String errorString = "";
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
    
            PropertySolver chosenSolver = null;
            try {
    
                URIAttribute attribute = (URIAttribute)
                entity.getAttribute("_uri", URIAttribute.class);
                if (attribute == null) {
                    attribute = new URIAttribute(entity, "_uri");
                }
                if (attribute.getURI() == null) {
                    URI uri = getModelURI(getName() + "_" + entity.getName());
                    attribute.setURI(uri);
                }
    
                List solversInModel = entity.attributeList(PropertySolver.class);
                if (solversInModel.size() > 0) {
                    try {
                        chosenSolver = ((PropertySolver) solversInModel.get(0)).findSolver(propertyValue);
                    } catch (PropertyResolutionException ex) {
    
                    }
                }
    
                if (chosenSolver == null) {
                    chosenSolver = instantiateSolver(entity, propertyValue);
                }
    
                for (String solverName : chosenSolver.getDependentSolvers()) {
                    try {
                        chosenSolver.findSolver(solverName);
                    } catch (PropertyResolutionException ex) {
                        instantiateSolver(entity, solverName);
                    }
                }
    
                if (chosenSolver instanceof PropertyConstraintSolver) {
                    ((PropertyConstraintSolver) chosenSolver)
                    .setLogMode(logMode.getToken() == BooleanToken.TRUE);
                }
    
                String previousAction = chosenSolver.setAction(actionValue);
                chosenSolver.invokeSolver(getContainer());
                chosenSolver.setAction(previousAction);
    
            } catch (PropertyFailedRegressionTestException ex) {
                errorString = KernelException.generateMessage(
                        entity, null, ex, "****Failed: Property regression test failed.") + "\n\n";
    
            } catch (KernelException ex) {
                errorString = KernelException.generateMessage(
                        entity, null, ex, "****Failed: Property regression test failed.") + "\n\n";
            } catch (URISyntaxException ex) {
                errorString = KernelException.generateMessage(
                        entity, null, ex, "****Failed: Property regression test failed.") + "\n\n";
            } catch (Exception ex) {
                errorString = KernelException.generateMessage(
                        entity, null, ex, "****Failed: Property regression test failed.") + "\n\n";
            }
            finally {
                if (chosenSolver != null) {
                    chosenSolver.resetAll();
                }
                //        _removeSolvers(entity);
            }
        }
        return errorString;
    }

    public URI getModelURI(String modelName) throws URISyntaxException {
        URI uri = URIAttribute.getModelURI(this);
        String path = uri.getPath();
        int pos = path.lastIndexOf('/');
        if (pos >= 0) {
            path = path.substring(0, pos + 1) + modelName + ".xml";
        } else {
            path += "/" + modelName + ".xml";
        }
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                uri.getPort(), path, uri.getQuery(), uri.getFragment());
    }


    public List<Class> getListOfSolverClass(String path)
    throws IllegalActionException {
        List<Class> solvers = new LinkedList<Class>();
        File file = null;

        try {
            file = new File(FileUtilities.nameToURL(
                    "$CLASSPATH/" + path.replace(".", "/"),
                    null, null).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File[] classFiles = file.listFiles();

        for (int i = 0; i < classFiles.length; i++) {
            String className = classFiles[i].getName();

            if (classFiles[i].isDirectory()) {
                if (!className.equals("CVS") && !className.equals(".svn")) {
                    // Search sub-folder.
                    solvers.addAll(getListOfSolverClass(path + "." + className));
                }
            } else {
                try {
                    // only consider class files
                    if (className.contains(".class")) {
                        _solvers.add(Class.forName(path + "." + className.substring(0, className.length() - 6)));
                    }
                } catch (ClassNotFoundException e) {
                    assert false;
                }
            }

        }
        return solvers;
    }

    public PropertySolver instantiateSolver(CompositeEntity entity, String className) {

        for (Class solver : _solvers) {
            if (className.equals(
                    solver.getSimpleName())) {
                try {
                    Constructor constructor =
                        solver.getConstructor(NamedObj.class, String.class);

                    PropertySolver solverObject = (PropertySolver) constructor
                    .newInstance(entity, "ModelAnalyzer_" + solver.getSimpleName());

                    new Location(solverObject, "_location");

                    return solverObject;

                } catch (Exception ex) {
                    assert false;
                }
                break;
            }
        }
        return null;
    }

    public void setContainer(NamedObj container) throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
    
        _moveParameter(action);
        _moveParameter(property);
        _moveParameter(showProperty);
        _moveParameter(highlight);
        _moveParameter(logMode);
        _moveParameter(overwriteConstraint);
        _moveParameter(overwriteDependentProperties);
    
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    private void _addChoices() throws IllegalActionException {
    
        List<Class> solvers =
            getListOfSolverClass("ptolemy.data.properties.configuredSolvers");
    
        if (solvers.size() > 0) {
            property.setExpression(solvers.get(0).getSimpleName());
        }
    
        for (Class solver : solvers) {
            property.addChoice(solver.getSimpleName());
        }
    
        property.addChoice("Clear All");
    
        PropertySolver._addActions(action);
    }

    private void _moveParameter(Parameter parameter) throws IllegalActionException, NameDuplicationException {
        if (parameter != null) {
            if (parameter.getContainer() != getContainer()) {
                parameter.setContainer(getContainer());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    private List<Class> _solvers = new LinkedList<Class>();

}
