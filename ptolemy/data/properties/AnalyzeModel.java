/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.data.properties;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.gt.controller.GTDebugEvent;
import ptolemy.actor.gt.controller.GTEvent;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.FileUtilities;

//////////////////////////////////////////////////////////////////////////
//// AnalyzeModel

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AnalyzeModel extends GTEvent {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public AnalyzeModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        property = new StringParameter(this, "property");
        action = new StringParameter(this, "action");
        _addChoices();

        showProperty = new Parameter(this, "showProperty");
        showProperty.setTypeEquals(BaseType.BOOLEAN);
        showProperty.setExpression("true");

        highlight = new Parameter(this, "highlight");
        highlight.setTypeEquals(BaseType.BOOLEAN);
        highlight.setExpression("true");

        logMode = new Parameter(this, "logMode");
        logMode.setTypeEquals(BaseType.BOOLEAN);
        logMode.setExpression("true");

        overwriteConstraint = new Parameter(this, "overwriteConstraint");
        overwriteConstraint.setTypeEquals(BaseType.BOOLEAN);
        overwriteConstraint.setExpression("false");

        overwriteDependentProperties = new Parameter(this, "overwriteDependentProperties");
        overwriteDependentProperties.setTypeEquals(BaseType.BOOLEAN);
        overwriteDependentProperties.setExpression("false");
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AnalyzeModel newObject = (AnalyzeModel) super.clone(workspace);
        try {
            newObject._solvers = _createSolvers("ptolemy.data.properties.configuredSolvers");
        } catch (IllegalActionException ex) {
            CloneNotSupportedException exception = new CloneNotSupportedException();
            exception.initCause(ex);
            throw exception;
        }
        return newObject;
    }

    public RefiringData fire(ArrayToken arguments)
            throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        _debug(new GTDebugEvent(this, "Start analysis."));

        long start = System.currentTimeMillis();

        CompositeEntity entity = getModelParameter().getModel();

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
                    URI uri = _getModelURI(getName() + "_" + entity.getName());
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
                    chosenSolver = _instantiateSolver(entity, propertyValue);
                }

                for (String solverName : chosenSolver.getDependentSolvers()) {
                    try {
                        chosenSolver.findSolver(solverName);
                    } catch (PropertyResolutionException ex) {
                        _instantiateSolver(entity, solverName);
                    }
                }

                if (chosenSolver instanceof PropertyConstraintSolver) {
                    ((PropertyConstraintSolver) chosenSolver)
                    .setLogMode(logMode.getToken() == BooleanToken.TRUE);
                }

                String previousAction = chosenSolver.setAction(actionValue);
                chosenSolver.invokeSolver(this);
                chosenSolver.setAction(previousAction);

            } catch (PropertyFailedRegressionTestException ex) {
                System.err.println(KernelException.generateMessage(
                        entity, null, ex, "****Failed: Property regression test failed.") + "\n\n");

            } catch (KernelException ex) {
                System.err.println(KernelException.generateMessage(
                        entity, null, ex, "****Failed: Property regression test failed.") + "\n\n");
            } catch (URISyntaxException ex) {
                System.err.println(KernelException.generateMessage(
                        entity, null, ex, "****Failed: Property regression test failed.") + "\n\n");
            } catch (Exception ex) {
                System.err.println(KernelException.generateMessage(
                        entity, null, ex, "****Failed: Property regression test failed.") + "\n\n");
            }
            /*catch (KernelException ex) {
                errorMessage.send(0, new StringToken(KernelException.generateMessage(
                        entity, null, ex, "Failed: Checking/annotating failed while in progress.") + "\n\n"));
            } */
            finally {
                if (chosenSolver != null) {
                    chosenSolver.resetAll();
                }
//                _removeSolvers(entity);
            }
        }

        getModelParameter().setModel(entity);

        long elapsed = System.currentTimeMillis() - start;
        if (data == null) {
            _debug(new GTDebugEvent(this, "Finish analysis (" +
                    (double) elapsed / 1000 + " sec)."));
        } else {
            _debug(new GTDebugEvent(this, "Request refire (" +
                    (double) elapsed / 1000 + " sec)."));
        }

        return data;
    }

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

    private void _addChoices() throws IllegalActionException {
        _createSolvers("ptolemy.data.properties.configuredSolvers");

        if (_solvers.size() > 0) {
            property.setExpression(_solvers.get(0).getSimpleName());
        }

        for (Class solver : _solvers) {
            property.addChoice(solver.getSimpleName());
        }

        property.addChoice("Clear All");

        PropertySolver._addActions(action);
    }

    private List<Class> _createSolvers(String path)
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
                    solvers.addAll(_createSolvers(path + "." + className));
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

    private URI _getModelURI(String modelName) throws URISyntaxException {
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

    private PropertySolver _instantiateSolver(CompositeEntity entity, String className) {

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

    private List<Class> _solvers = new LinkedList<Class>();
}
