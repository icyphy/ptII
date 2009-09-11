/*
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2009 The Regents of the University of California. All
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
package ptolemy.data.properties;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;

/**
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class AnalyzerAttribute extends Attribute {

    /**
     * Construct an AnalyzerAttribute with the specified container and name.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the attribute is not of an
     * acceptable class for the container, or if the name contains a period.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
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

        overwriteDependentProperties = new Parameter(container,
                "overwriteDependentProperties");
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

    /**
     * The property to analyze.
     */
    public Parameter property;

    /**
     * Whether to display the annotated property or not.
     */
    public Parameter showProperty;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public String analyze(CompositeEntity entity) throws IllegalActionException {
        String errorString = "";
        String propertyValue = property.getExpression();

        if (propertyValue.equals("Clear All")) {
            try {
                PropertyRemover remover = new PropertyRemover(entity,
                        "ModelAnalyzerClearAll");
                remover.removeProperties(entity);
            } catch (NameDuplicationException e) {
                assert false;
            }
        } else {

            String actionValue = action.getExpression();

            PropertySolver chosenSolver = null;
            try {

                URIAttribute attribute = (URIAttribute) entity.getAttribute(
                        "_uri", URIAttribute.class);
                if (attribute == null) {
                    attribute = new URIAttribute(entity, "_uri");
                }
                if (attribute.getURI() == null) {
                    URI uri = getModelURI(getName() + "_" + entity.getName());
                    attribute.setURI(uri);
                }

                List solversInModel = entity
                        .attributeList(PropertySolver.class);
                if (solversInModel.size() > 0) {
                    try {
                        chosenSolver = ((PropertySolver) solversInModel.get(0))
                                .findSolver(propertyValue);
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
                errorString = KernelException.generateMessage(entity, null, ex,
                        "****Failed: Property regression test failed.")
                        + "\n\n";

            } catch (KernelException ex) {
                errorString = KernelException.generateMessage(entity, null, ex,
                        "****Failed: Property regression test failed.")
                        + "\n\n";
            } catch (URISyntaxException ex) {
                errorString = KernelException.generateMessage(entity, null, ex,
                        "****Failed: Property regression test failed.")
                        + "\n\n";
            } catch (Exception ex) {
                errorString = KernelException.generateMessage(entity, null, ex,
                        "****Failed: Property regression test failed.")
                        + "\n\n";
            } finally {
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
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri
                .getPort(), path, uri.getQuery(), uri.getFragment());
    }

    /**
     * Given a dot separated packagename, return a list of all the packages in
     * the given path and below. The list of solver classes is also updated.
     * @param path The dot separated package name, such as
     * "ptolemy.data.properties.configuredSolvers"
     * @return a List of packages that are found.
     */
    public List<Class> getListOfSolverClass(String path) {

        List<Class> solvers = new LinkedList<Class>();

        String directoryPath = "$CLASSPATH/" + path.replace(".", "/");

        try {
            URI directoryURI = new URI(FileUtilities.nameToURL(directoryPath,
                    null, null).toExternalForm().replaceAll(" ", "%20"));
            File directory = null;
            try {
                try {
                    directory = new File(directoryURI);
                } catch (Throwable throwable) {
                    throw new InternalErrorException(this, throwable,
                            "Failed to find directories in the URI: \""
                                    + directoryURI + "\"");
                }
                ClassFileOrDirectoryNameFilter filter = new ClassFileOrDirectoryNameFilter();
                File[] classFiles = directory.listFiles(filter);
                if (classFiles == null) {
                    throw new InternalErrorException(this, null,
                            "Failed to find directories in \"" + directoryPath
                                    + "\"");
                } else {
                    for (File element : classFiles) {
                        String className = element.getName();

                        if (element.isDirectory()) {
                            if (!className.equals("CVS")
                                    && !className.equals(".svn")) {
                                // Search sub-folder.
                                solvers.addAll(getListOfSolverClass(path + "."
                                        + className));
                            }
                        } else {
                            try {
                                // only consider class files
                                if (className.endsWith(".class")) {
                                    solvers.add(Class.forName(path
                                            + "."
                                            + className.substring(0, className
                                                    .length() - 6)));
                                }
                            } catch (ClassNotFoundException e) {
                                assert false;
                            }
                        }
                    }
                }
            } catch (Throwable throwable) {
                try {
                    if (!directoryURI.toString().startsWith("jar:")) {
                        throw throwable;
                    } else {
                        new LinkedList<String>();
                        URL jarURL = directoryURI.toURL();
                        JarURLConnection connection = (JarURLConnection) jarURL
                                .openConnection();
                        String jarEntryName = connection.getEntryName();
                        if (!jarEntryName.endsWith("/")) {
                            jarEntryName = jarEntryName + "/";
                        }
                        JarFile jarFile = connection.getJarFile();
                        Enumeration entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = (JarEntry) entries.nextElement();
                            String name = entry.getName();
                            if (name.startsWith(jarEntryName)
                                    && name.endsWith(".class")
                                    && !entry.isDirectory()) {
                                String className = name.replace("/", ".")
                                        .substring(0, name.length() - 6);
                                try {
                                    solvers.add(Class.forName(className));
                                } catch (ClassNotFoundException ex) {
                                    throw new InternalErrorException(this, ex,
                                            "Can't find class " + className);
                                }
                            }
                        }
                    }
                } catch (Throwable throwable2) {
                    throwable2.printStackTrace();
                    throw new IllegalActionException(this, throwable,
                            "Failed to process " + directoryURI);
                }
            }
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Failed to find classes in \"" + directoryPath + "\"");
        }
        return solvers;
    }

    public PropertySolver instantiateSolver(CompositeEntity entity,
            String className) {

        for (Class solver : _solvers) {
            if (className.equals(solver.getSimpleName())) {
                try {
                    Constructor constructor = solver.getConstructor(
                            NamedObj.class, String.class);

                    PropertySolver solverObject = (PropertySolver) constructor
                            .newInstance(entity, "ModelAnalyzer_"
                                    + solver.getSimpleName());

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

    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
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

        _solvers.addAll(getListOfSolverClass("ptolemy.data.properties.configuredSolvers"));

        if (_solvers.size() > 0) {
            property.setExpression(_solvers.get(0).getSimpleName());
        }

        for (Class solver : _solvers) {
            property.addChoice(solver.getSimpleName());
        }

        property.addChoice("Clear All");

        PropertySolver._addActions(action);
    }

    private void _moveParameter(Parameter parameter)
            throws IllegalActionException, NameDuplicationException {
        if (parameter != null) {
            if (parameter.getContainer() != getContainer()) {
                parameter.setContainer(getContainer());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    private final List<Class> _solvers = new LinkedList<Class>();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     * Look for directories that do are not CVS or .svn.
     */
    static class ClassFileOrDirectoryNameFilter implements FilenameFilter {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /**
         * Return true if the specified file names a directory that is not named
         * "CVS" or ".svn".
         * @param directory the directory in which the potential directory was
         * found.
         * @param name the name of the directory or file.
         * @return true if the file is a directory that contains a file called
         * configuration.xml
         */
        public boolean accept(File directory, String name) {
            try {
                File file = new File(directory, name);

                if (file.isDirectory()
                        && (file.getName().equals("CVS") || file.getName()
                                .equals(".svn"))) {
                    return false;
                }
                if (!file.isDirectory() && file.getName().endsWith(".class")) {
                    return true;
                }
                if (file.isDirectory()) {
                    return true;
                }
            } catch (Exception ex) {
                return false;
            }

            return false;
        }
    }
}
