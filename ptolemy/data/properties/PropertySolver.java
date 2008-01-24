package ptolemy.data.properties;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.StringUtilities;

public abstract class PropertySolver extends Attribute {

    public PropertySolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _solvers.add(this);
    }

    public Parameter trainingMode;

    /**
     * Resolve the property values for the given top-level entity.
     * @param topLevel The given top level entity.
     */
    public abstract void resolveProperties(CompositeEntity topLevel)
            throws KernelException;

    /**
     * Return the property helper for the given object. 
     * @param object The given object.
     * @return The property helper for the object.
     * @exception IllegalActionException Thrown if the helper cannot
     *  be found or instantiated.
     */
    public abstract PropertyHelper getHelper(Object object)
            throws IllegalActionException;

    public void addPropertyChangedListener(PropertyChangedListener listener) {
        _listeners.add(listener);
    }

    public abstract String getExtendedUseCaseName();

    public abstract String getUseCaseName();

    /** Parse a command-line argument. This method recognized -help
     *  and -version command-line arguments, and prints usage or
     *  version information. No other command-line arguments are
     *  recognized.
     *  @param arg The command-line argument to be parsed.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    public static boolean parseArg(String arg) throws Exception {
        if (arg.equals("-help")) {
            // TODO: _usage()??
            //System.out.println(_usage());

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
        } else if (arg.equals("-version")) {
            System.out
                    .println("Version "
                            + VersionAttribute.CURRENT_VERSION.getExpression()
                            + ", Build $Id$");

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
        }
        // Argument not recognized.
        return false;
    }

    public void removePropertyChangedListener(PropertyChangedListener listener) {
        _listeners.remove(listener);
    }

    public void setTrainingMode(boolean isTraining) {
        trainingMode.setExpression((isTraining) ? "true" : "false");
    }

    /**
     * 
     * @param object
     * @return
     * @exception IllegalActionException
     */
    protected PropertyHelper _getHelper(Object object)
            throws IllegalActionException {
        if (_helperStore.containsKey(object)) {
            return (PropertyHelper) _helperStore.get(object);
        }

        if ((object instanceof IOPort) || (object instanceof Attribute)) {
            return _getHelper(((NamedObj) object).getContainer());
        }

        String packageName = getClass().getPackage().getName() + "."
                + getUseCaseName();

        Class componentClass = object.getClass();

        Class helperClass = null;
        while (helperClass == null) {
            try {

                // FIXME: Is this the right error message?
                if (!componentClass.getName().contains("ptolemy")) {
                    throw new IllegalActionException(
                            "There is no property helper " + " for "
                                    + object.getClass());
                }

                helperClass = Class.forName(componentClass.getName()
                        .replaceFirst("ptolemy", packageName));

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
            helperObject = constructor
                    .newInstance(new Object[] { this, object });

        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Failed to create the helper class for property constraints.");
        }

        if (!(helperObject instanceof PropertyHelper)) {
            throw new IllegalActionException(
                    "Cannot resolve property for this component: " + object
                            + ". Its helper class does not"
                            + " implement PropertyHelper.");
        }
        _helperStore.put(object, helperObject);

        return (PropertyHelper) helperObject;
    }

    private List _listeners = new ArrayList();

    /** A hash map that stores the code generator helpers associated
     *  with the actors.
     */
    protected HashMap _helperStore = new HashMap();

    protected static List _solvers = new ArrayList();

    /**
     * Find a constraint solver that is associated with the given 
     * property lattice name. There can be more than one solvers with
     * the same lattice. This method returns whichever it finds first. 
     * @param latticeName The given name of the property lattice. 
     * @return The property constraint solver associated with the
     *  given lattice name. 
     * @exception IllegalActionException Thrown if no matched solver
     *  is found.
     */
    public static PropertySolver findSolver(String identifier)
            throws IllegalActionException {
        Iterator iterator = _solvers.iterator();
        while (iterator.hasNext()) {
            PropertySolver solver = (PropertySolver) iterator.next();

            if (solver.getUseCaseName().equals(identifier)) {
                return solver;
            }
        }
        throw new IllegalActionException("Cannot find \"" + identifier
                + "\" solver.");
    }

    /** Resolve properties for a model.
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @return The return value of the last subprocess that was run
     *  to compile or run the model.  Return -1 if called  with no arguments.
     *  Return -2 if no CodeGenerator was created.
     *  @exception Exception If any error occurs.
     */
    public static int testProperties(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.data.properties.PropertySolver model.xml "
                    + "[model.xml . . .]\n"
                    + "  The arguments name MoML files containing models");
            return -1;
        }

        PropertySolver solver = null;

        // See MoMLSimpleApplication for similar code
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

        for (int i = 0; i < args.length; i++) {
            if (parseArg(args[i])) {
                continue;
            }
            if (args[i].trim().startsWith("-")) {
                if (i >= (args.length - 1)) {
                    throw new IllegalActionException("Cannot set "
                            + "parameter " + args[i] + " when no value is "
                            + "given.");
                }

                // Save in case this is a parameter name and value.
                //_parameterNames.add(args[i].substring(1));
                //_parameterValues.add(args[i + 1]);
                i++;
                continue;
            }
            // Note: the code below uses explicit try catch blocks
            // so we can provide very clear error messages about what
            // failed to the end user.  The alternative is to wrap the
            // entire body in one try/catch block and say
            // "Code generation failed for foo", which is not clear.
            URL modelURL;

            try {
                modelURL = new File(args[i]).toURI().toURL();
            } catch (Exception ex) {
                throw new Exception("Could not open \"" + args[i] + "\"", ex);
            }

            CompositeActor toplevel = null;

            try {
                try {
                    toplevel = (CompositeActor) parser.parse(null, modelURL);
                } catch (Exception ex) {
                    throw new Exception("Failed to parse \"" + args[i] + "\"",
                            ex);
                }

                // Get all instances of PropertySolver contained in the model.
                List solvers = _solvers;
                //toplevel.attributeList(PropertySolver.class);

                if (solvers.size() == 0) {
                    // Add a codeGenerator
                    throw new IllegalActionException(
                            "The model does not contain a solver.");
                } else {
                    // Get the last CodeGenerator in the list, maybe
                    // it was added last?
                    Iterator iterator = solvers.iterator();
                    while (iterator.hasNext()) {
                        solver = (PropertySolver) iterator.next();
                        //solver._updateParameters(toplevel);
                        try {
                            solver.setTrainingMode(false);
                            solver.resolveProperties(toplevel);
                        } catch (KernelException ex) {
                            throw new Exception(
                                    "Failed to resolve properties for \""
                                            + args[i] + "\"", ex);
                        }
                    }
                }

            } finally {
                // Destroy the top level so that we avoid
                // problems with running the model after generating code
                if (toplevel != null) {
                    toplevel.setContainer(null);
                }
            }
        }
        //if (solver != null) {
        //    return solver.getExecuteCommands().getLastSubprocessReturnCode();
        //}
        //return -2;
        return 0;
    }

}
