/*  The base abstract class for a property solver.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Node;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.Configurer;
import ptolemy.domains.properties.kernel.OntologyAttribute.OntologyComposite;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////PropertySolverBase

/**
The base abstract class for a property solver.

<p>The base class provides the core functionality for property
resolution.  It provides a method to create a PropertyHelper for any
given model component. The model component can be an object of any
Ptolemy class (e.g. ASTPtRootNode, Sink, Entity, and FSMActor). A
model component, in turn, may have one or multiple property-able
objects.

<p>A PropertySolver is associated with strictly one use-case.
PropertySolvers with the same use-case are considered equivalent.
Subclasses need to associate the solver with an unique use-case by
providing implementation for {@link PropertySolverBase#getUseCaseName()},
{@link PropertySolverBase#getExtendedUseCaseName()}.
An use-case may have dependency with other use-cases. The dependencies
are declared when the solver is instantiated. The dependencies needs to
be kept non-circular to avoid non-deterministic resolution result.

<p>Every PropertySolver is linked together by the SharedParameter called
"sharedUtilitiesWrapper", which contains the shared utility object.
This allows every PropertySolver to find other solvers in the model.

<p>Subclasses needs to implement
{@link PropertySolverBase#resolveProperties()}
to specify exactly how to perform the property resolution. For example,
one may gather all the constraints from the PropertyHelpers and feed them
into a constraint solver.


@author Man-Kit Leung
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */
public abstract class PropertySolverBase extends Attribute {


    /*
     * Construct a PropertySolverBase with the specified container and
     * name. If this is the first PropertySolver created in the model,
     * the shared utility object will also be created.
     *
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the PropertySolverBase is
     * not of an acceptable attribute for the container.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public PropertySolverBase(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sharedUtilitiesWrapper = new SharedParameter(this,
                "sharedUtilitiesWrapper", PropertySolver.class);
        sharedUtilitiesWrapper.setPersistent(false);
        sharedUtilitiesWrapper.setVisibility(Settable.NONE);

        // **We can only create a new shared utilities object
        // only once per model.
        if (sharedUtilitiesWrapper.getExpression().length() == 0) {
            sharedUtilitiesWrapper.setToken(new ObjectToken(
                    new SharedUtilities()));
        }

        Collection<SharedParameter> parameters = sharedUtilitiesWrapper
        .sharedParameterSet();
        for (SharedParameter parameter : parameters) {
            parameters = parameter.sharedParameterSet();
        }

        _sharedUtilities = (SharedUtilities) ((ObjectToken) sharedUtilitiesWrapper
                .getToken()).getValue();

    }

    ///////////////////////////////////////////////////////////////////
    ////                    ports and parameters                   ////

    /**
     * The shared parameter that links together every solver in the
     * same model.
     */

    public SharedParameter sharedUtilitiesWrapper;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     * Add the given unique solver (user-case) identifier to the
     * dependency list. A dependent solver is one whose analysis
     * result is required for this solver's resolution. The dependent
     * solvers are run in order before invoking this solver.
     *
     * @param userCaseName The specified user case name.
     */
    public void addDependentUseCase(String userCaseName) {
        _dependentUseCases.add(userCaseName);
    }

    /**
     * Clear the resolved property for the specified object. The
     * object is assumed to be property-able; otherwise, nothing
     * happens.
     *
     * @param object The specified object.
     */
    public void clearResolvedProperty(Object object) {
        _resolvedProperties.remove(object);
    }

    /**
     * Find a solver that is associated with the specified label. A
     * solver can be identified by the use-case name, class name, or
     * its name in the model.  There can be more than one solvers with
     * the label. This method returns whichever it finds first.
     *
     * @param identifier The specified label.
     * @return The property solver associated with the specified label.
     * @exception IllegalActionException Thrown if no matched solver
     * is found.
     */
    public PropertySolver findSolver(String identifier)
    throws PropertyResolutionException {

        for (PropertySolver solver : getAllSolvers(sharedUtilitiesWrapper)) {
            if (solver.getUseCaseName().equals(identifier)) {
                return solver;
            }
            if (solver.getClass().getSimpleName().equals(identifier)) {
                return solver;
            }
            if (solver.getName().equals(identifier)) {
                return solver;
            }
        }

        throw new PropertyResolutionException(this, "Cannot find \""
                + identifier + "\" solver.");
    }

    /**
     * Return the list of all PropertyHelpers associated with this
     * solver.
     *
     * @return The list of PropertyHelpers.
     */
    public List<PropertyHelper> getAllHelpers() throws IllegalActionException {
        NamedObj topLevel = _toplevel();
        List<PropertyHelper> result = new LinkedList<PropertyHelper>();
        List<PropertyHelper> subHelpers = new LinkedList<PropertyHelper>();

        result.add(getHelper(topLevel));
        subHelpers.add(getHelper(topLevel));

        while (!subHelpers.isEmpty()) {
            PropertyHelper helper = subHelpers.remove(0);
            subHelpers.addAll(helper._getSubHelpers());
            result.add(helper);
        }

        return result;
    }

    /*
     * Return the list of all property-able objects obtained from
     * every PropertyHelper.
     *
     * @return The list of all property-able objects.
     * @exception IllegalActionException Thrown if
     * {@link#getAllPropertyables()} throws it.
     */
    public List getAllPropertyables() throws IllegalActionException {
        List result = new LinkedList();

        for (PropertyHelper helper : getAllHelpers()) {
            result.addAll(helper.getPropertyables());
        }
        return result;
    }

    /**
     * Return the list of all solvers that are in the same model. They
     * are linked by the specified SharedParameter.
     *
     * @param sharedParameter The specified SharedParameter links
     * together the solvers.
     * @return A list of PropertySolvers.
     */
    public static List<PropertySolver> getAllSolvers(
            SharedParameter sharedParameter) {
        List<NamedObj> parameters = new ArrayList<NamedObj>(sharedParameter
                .sharedParameterSet());
        List<PropertySolver> solvers = new LinkedList<PropertySolver>();
        for (NamedObj parameter : parameters) {
            Object container = parameter.getContainer();
            if (container instanceof PropertySolver) {
                solvers.add((PropertySolver) container);
            }
        }
        return solvers;
    }

    /**
     * Get the attribute that corresponds to the specified
     * ASTPtRootNode. This assumes that the correspondence is recorded
     * previously through calling
     * {@link ptolemy.data.properties.PropertyHelper#putAttribute(ASTPtRootNode, Attribute)}.
     *
     * @param node The specified ASTPtRootNode.
     * @return The attribute associated with the specified ASTPtRootNode.
     * @exception AssertionError Thrown if the specified node does not
     * have a corresponding attribute.
     */
    public Attribute getAttribute(ASTPtRootNode node) {
        Node root = node;
        Map<ASTPtRootNode, Attribute> attributes = getSharedUtilities()
        .getAttributes();

        while (root.jjtGetParent() != null) {
            if (attributes.containsKey(root)) {
                return attributes.get(root);
            }
            root = root.jjtGetParent();
        }

        if (!attributes.containsKey(root)) {
            throw new AssertionError(node.toString()
                    + " does not have a corresponding attribute.");
        }

        return attributes.get(root);
    }

    /**
     * Return the list of dependent solvers. The list contains the
     * unique name of the solvers.
     *
     * @return The list of dependent solvers.
     */
    public List<String> getDependentSolvers() {
        return _dependentUseCases;
    }

    /**
     * Return the extended use-case name. The extended use-case name is an
     * unique label for a use-case.
     *
     * @return the extended use-case name.
     */
    public abstract String getExtendedUseCaseName();

    /**
     * Return the property helper for the specified component.
     *
     * @param object The specified component.
     * @return The property helper for the component.
     * @exception IllegalActionException Thrown if the helper cannot
     * be found or instantiated.
     */
    public PropertyHelper getHelper(Object object)
    throws IllegalActionException {
        return _getHelper(object);
    }

    /**
     * Return the expression parser.
     *
     * @return The expression parser.
     */
    public static PtParser getParser() {
        if (_parser == null) {
            _parser = new PtParser();
        }
        return _parser;
    }

    /**
     * Return the root ASTPtRootNode associated with the specified
     * attribute.
     *
     * @param attribute The specified attribute.
     * @return The root ASTPtRootNode associated with the specified
     * attribute.
     * @exception IllegalActionException
     */
    public ASTPtRootNode getParseTree(Attribute attribute)
    throws IllegalActionException {
        Map<Attribute, ASTPtRootNode> parseTrees = getSharedUtilities()
        .getParseTrees();

        if (!parseTrees.containsKey(attribute)) {

            String expression = ((Settable) attribute).getExpression().trim();

            if (expression.length() == 0) {
                return null;
            }

            ASTPtRootNode parseTree;
            // if ((attribute instanceof StringAttribute) ||
            // ((attribute instanceof Variable
            // && ((Variable) attribute).isStringMode()))) {
            if ((attribute instanceof Variable)
                    && ((Variable) attribute).isStringMode()) {

                parseTree = getParser().generateStringParseTree(expression);

            } else {
                parseTree = getParser().generateParseTree(expression);
            }

            parseTrees.put(attribute, parseTree);
            getSharedUtilities().putAttribute(parseTree, attribute);
        }
        return parseTrees.get(attribute);
    }

    /**
     * Return the property value associated with the specified object.
     *
     * @param object The specified object.
     *
     * @return The property of the specified object.
     */
    public Property getProperty(Object object) {
        return getResolvedProperty(object);
    }

    /*
     * Return the resolved property for the specified object. This forces
     * resolution to happen if the object's property is not present.
     *
     * @param object The specified object
     *
     * @return The resolved property for the specified object.
     */
    public Property getResolvedProperty(Object object) {
        return getResolvedProperty(object, true);
    }

    /**
     * Return the resolved property for the specified object. The specified
     * resolve flag indicates whether to force resolution to happen.
     * @param object The specified object.
     * @param resolve Whether or not to force resolution.
     * @return The resolved property for the specified object.
     */
    public Property getResolvedProperty(Object object, boolean resolve) {
        Property property = _resolvedProperties.get(object);

        // See if it is already resolved.
        if (property != null) {
            return property;
        }

        // Get from the PropertyAttribute in the model.
        if (object instanceof NamedObj) {
            PropertyAttribute attribute = (PropertyAttribute) ((NamedObj) object)
            .getAttribute(getExtendedUseCaseName());

            if ((attribute != null) && (attribute.getProperty() != null)) {
                return attribute.getProperty();
            }
        }

        // Try resolve the property.
        try {
            if (resolve && !getSharedUtilities().getRanSolvers().contains(this)) {
                resolveProperties();
            }
        } catch (KernelException ex) {
            throw new InternalErrorException(KernelException
                    .stackTraceToString(ex));
        }

        return _resolvedProperties.get(object);
    }

    /**
     * Return the shared utility object.
     *
     * @return The shared utility object.
     */
    public SharedUtilities getSharedUtilities() {
        return _sharedUtilities;
    }

    /**
     * Return the use-case name. The use-case name is not guaranteed to be
     * unique.
     *
     * @return The use-case name.
     */
    public abstract String getUseCaseName();

    /**
     * Mark the property of the specified object as non-settable. The
     * specified object has a fixed assigned property.
     *
     * @param object The specified object.
     */
    public void markAsNonSettable(Object object) {
        _nonSettables.add(object);
    }

    /**
     * Reset the solver.
     */
    public void reset() {
        _resolvedProperties = new HashMap<Object, Property>();
        _nonSettables = new HashSet<Object>();
        _helperStore = new HashMap<Object, PropertyHelper>();
    }

    /**
     * Reset every solver in the model.
     */
    public void resetAll() {
        _parser = null;
        for (PropertySolver solver : getAllSolvers(sharedUtilitiesWrapper)) {
            solver.reset();
        }
        getSharedUtilities().resetAll();
    }

    /**
     * Perform property resolution.
     *
     * @exception KernelException Thrown if sub-class throws it.
     */
    public abstract void resolveProperties() throws KernelException;

    /**
     * Set the resolved property of the specified object.
     * (See {@link #getResolvedProperty(Object)}).
     *
     * @param object The specified object.
     * @param property The specified property.
     */
    public void setResolvedProperty(Object object, Property property) {
        _resolvedProperties.put(object, property);
    }

    ///////////////////////////////////////////////////////////////////
    ////             protected methods                             ////

    /**
     * Return the PropertyHelper for the specified component. This
     * instantiates a new PropertyHelper if it does not already exist
     * for the specified component.
     * @param component The specified component.
     * @return The PropertyHelper for the specified component.
     * @exception IllegalActionException Thrown if the PropertyHelper
     * cannot be instantiated.
     */
    protected PropertyHelper _getHelper(Object component)
    throws IllegalActionException {

        if (_helperStore.containsKey(component)) {
            return _helperStore.get(component);
        }


        if ((component instanceof IOPort) || (component instanceof Attribute)) {
            if (((NamedObj) component).getContainer() == null) {
                System.err.println("component container is null: " + component);
            }
            return _getHelper(((NamedObj) component).getContainer());
        }

        if (getContainer() instanceof OntologyComposite) {
            _compileHelperClasses();
        }

        String packageName = _getPackageName();

        Class componentClass = component.getClass();

        Class helperClass = null;
        while (helperClass == null) {
            try {

                // FIXME: Is this the right error message?
                if (!componentClass.getName().contains("ptolemy")) {
                    throw new IllegalActionException("There is no "
                            + "property helper for " + component.getClass());
                }

                helperClass = Class.forName((componentClass.getName()
                        .replaceFirst("ptolemy", packageName)).replaceFirst(
                                ".configuredSolvers.", "."));

            } catch (ClassNotFoundException e) {
                // If helper class cannot be found, search the helper class
                // for parent class instead.
                componentClass = componentClass.getSuperclass();
            }
        }

        Constructor constructor = null;
        Class solverClass = getClass();
        while (constructor == null && solverClass != null) {
            try {
                constructor = helperClass.getConstructor(new Class[] {
                        solverClass, componentClass });

            } catch (NoSuchMethodException ex) {
                solverClass = solverClass.getSuperclass();
            }
        }

        if (constructor == null) {
            throw new IllegalActionException(
                    "Cannot find constructor method in "
                    + helperClass.getName());
        }

        Object helperObject = null;

        try {
            helperObject = constructor.newInstance(new Object[] { this,
                    component });

        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
            "Failed to create the helper class for property constraints.");
        }

        if (!(helperObject instanceof PropertyHelper)) {
            throw new IllegalActionException(
                    "Cannot resolve property for this component: " + component
                    + ". Its helper class does not"
                    + " implement PropertyHelper.");
        }
        _helperStore.put(component, (PropertyHelper) helperObject);

        return (PropertyHelper) helperObject;
    }





    private void _compileHelperClasses() throws IllegalActionException {

        OntologyComposite container = (OntologyComposite) getContainer();
        for (Entity entity : (List<Entity>) container.entityList()) {
            StringAttribute attribute = (StringAttribute)
            ((Entity) entity).getAttribute(OntologyComposite.RULES);

            String userCode = attribute.getExpression();

            _compileUserCode(entity, userCode);
        }
    }

    private void _compileUserCode(Entity entity, String userCode) throws IllegalActionException {

        String ptRoot = StringUtilities.getProperty("ptolemy.ptII.dir");

        String classname = _getPackageName() + entity.getClass()
        .getName().replaceFirst("ptolemy", "");

        String packageName = _getPackageName() + entity.getClass()
        .getPackage().getName().replaceFirst("ptolemy", "");

        String directoryPath = (ptRoot + "/" + packageName).replace(".", "/");

        try {
            File file;
            File directory = FileUtilities.nameToFile(directoryPath, null);
            directory.mkdirs();
            file = new File(directory, entity.getClass().getSimpleName() + ".java");

            // Set the file to delete on exit
//            directory.deleteOnExit();
//            file.deleteOnExit();

            // Get the file name and extract a class name from it
            String filename = file.getCanonicalPath();

            PrintWriter out = new PrintWriter(new FileOutputStream(file));
            out.println(userCode);
            // Flush and close the stream
            out.flush();
            out.close();

            String[] args = new String[] {
                    "-classpath", ptRoot,
                    //"-d", directoryPath,
                    filename
            };

            int status = com.sun.tools.javac.Main.compile(args);

            switch (status) {
            case 0:  // OK
                // Make the class file temporary as well
                //new File(file.getParent(), classname + ".class").deleteOnExit();
                try {
                    // Try to access the class and run its main method
                    Class.forName(classname);
                } catch (Exception ex) {
                    throw new IllegalActionException(null, ex,
                    "Cannot load the class file for: " + classname);
                }
                break;
            default:
                throw new IllegalActionException(
                        "Cannot compile user code for " + entity.getName());
            }
        } catch (IOException ex) {
            throw new IllegalActionException(
                    null, ex, "Error occurs when compiling the user code for " + entity.getName());
        }
    }

    /**
     * Return the package name that contains the class of this solver.
     *
     * @return The package name.
     */
    protected String _getPackageName() {
        return getClass().getPackage().getName() + "." + getUseCaseName();
    }

    protected NamedObj _toplevel() {
        NamedObj toplevel = toplevel();

        // If the solver is in an OntologyAttribute, we
        // want to analyze the outside model.
        while (toplevel instanceof Configurer) {
            NamedObj configuredObject = ((Configurer)
                    toplevel).getConfiguredObject();

            if (configuredObject == null) {
                return toplevel;
            }
            toplevel = configuredObject.toplevel();
        }
        return toplevel;
    }

    ///////////////////////////////////////////////////////////////////
    ////             protected variables                           ////

    /**
     * The list that keeps track of the dependencies on other
     * use-cases.  Circular dependencies are not allowed but it is up
     * to the user to enforce this requirement. This means that there
     * should not be a case where two solvers' use-cases exist in each
     * other's dependency list.
     */
    protected List<String> _dependentUseCases = new LinkedList<String>();

    /**
     * The HashMap that caches components and their PropertyHelper objects.
     */
    protected HashMap<Object, PropertyHelper> _helperStore = new HashMap<Object, PropertyHelper>();

    /**
     * The set of property-able objects that have non-settable property. A
     * non-settable property results from setting an object with a fixed
     * property through PropertyHelper.setEquals().
     */
    protected HashSet<Object> _nonSettables = new HashSet<Object>();

    /**
     * The HashMap that caches property-able objects and their
     * Property values.  Each mapping is a pair of Object and
     * Property.
     */
    protected HashMap<Object, Property> _resolvedProperties = new HashMap<Object, Property>();

    /*
     * The shared utility object.
     */
    protected SharedUtilities _sharedUtilities;

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /*
     * The expression parser.
     */
    private static PtParser _parser;

}
