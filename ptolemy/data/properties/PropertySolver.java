package ptolemy.data.properties;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.StaticSchedulingCodeGenerator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

public abstract class PropertySolver extends Attribute {

    public PropertySolver(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
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
     * @throws IllegalActionException Thrown if the helper cannot
     *  be found or instantiated.
     */
    public abstract PropertyHelper getHelper(Object object) 
            throws IllegalActionException;
    
    public void addPropertyChangedListener(PropertyChangedListener listener) {
        _listeners.add(listener);
    }
    
    public void removePropertyChangedListener(PropertyChangedListener listener) {
        _listeners.remove(listener);
    }
    
    private List _listeners = new ArrayList();
    
    private static List _solvers = new ArrayList(); 

    public abstract String getSolverIdentifier();
    
    /**
     * Find a constraint solver that is associated with the given 
     * property lattice name. There can be more than one solvers with
     * the same lattice. This method returns whichever it finds first.
     * Return null if no matched solver is found.
     * @param latticeName The given name of the property lattice. 
     * @return The property constraint solver associated with the
     *  given lattice name. 
     */
    public static PropertySolver nameToSolver(String identifier) {
        Iterator iterator = _solvers.iterator();
        while (iterator.hasNext()) {
            PropertySolver solver = 
                (PropertySolver) iterator.next();
            
            if (solver.getSolverIdentifier().equals(identifier)) {
                return solver;
            }
        }
        return null;
    }
    

    /** Resolve properties for a model.
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @return The return value of the last subprocess that was run
     *  to compile or run the model.  Return -1 if called  with no arguments.
     *  Return -2 if no CodeGenerator was created.
     *  @exception Exception If any error occurs.
     */
    public static int resolveProperties(String[] args) throws Exception {
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
            //try { 

            for (int i = 0; i < args.length; i++) {
                if (CodeGenerator.parseArg(args[i])) {
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
                    modelURL = new File(args[i]).toURL();
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

                    // Get all instances of this class contained in the model
                    List solvers = toplevel.attributeList(PropertySolver.class);
                            //_solvers; 

                    if (solvers.size() == 0) {
                        // Add a codeGenerator
                        throw new IllegalActionException("The model does not contain a solver.");
                    } else {
                        // Get the last CodeGenerator in the list, maybe
                        // it was added last?
                        Iterator iterator = solvers.iterator();
                        while (iterator.hasNext()) {
                            solver = (PropertySolver) iterator.next();
                            //solver._updateParameters(toplevel);
                            try {
                                solver.resolveProperties(toplevel);
                            } catch (KernelException ex) {
                                throw new Exception("Failed to resolve properties for \""
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
        //} catch (Throwable ex) {
        //    MoMLApplication.throwArgsException(ex, args);
        //}
        //return -1;
    }
    
}
