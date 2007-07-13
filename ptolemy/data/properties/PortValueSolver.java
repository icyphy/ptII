package ptolemy.data.properties;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.gui.PortValueSolverGUIFactory;
import ptolemy.data.properties.token.firstValueToken.FirstTokenSentListener;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;

public class PortValueSolver extends PropertySolver {
    
    public PortValueSolver(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        portValue = new StringParameter(this, "portValue");
        portValue.setExpression("staticValueToken");
        _portValue = portValue.getExpression();

        listeningMethod = new StringParameter(this, "listeningMethod");
        listeningMethod.setExpression("NONE");
        _listeningMethod = listeningMethod.getExpression();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"115\" height=\"40\" "
                + "style=\"fill:yellow\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "Double click to\nResolve Property.</text></svg>");

        new PortValueSolverGUIFactory(
                this, "_portValueSolverGUIFactory");

        trainingMode = new Parameter(this, "trainingMode");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
        trainingMode.setExpression("true");
        
        _addChoices();

        _solvers.add(this);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
   
    /**
     * Returns the helper that contains property information for
     * the given component.
     * @param component The given component
     * @return The associated property constraint helper.
     */    
    public PropertyHelper getHelper(NamedObj component)
            throws IllegalActionException {

        return (PropertyHelper) _getHelper(component);
    }

    public PropertyHelper getHelper(Object object) throws IllegalActionException {
        return (PortValueHelper) _getHelper(object);
    }

    public void resolveProperties(CompositeEntity topLevel) throws KernelException {
        
        // get all ports
        if (topLevel.getContainer() != null) {
            throw new IllegalArgumentException(
                "TypedCompositeActor.resolveProperties:" +
                " The specified actor is not the top level container.");
        }
        
        CompositeActor _compositeActor = (CompositeActor) topLevel;

        Iterator actorIterator = _compositeActor.deepEntityList().iterator();
        while (actorIterator.hasNext()) {
            Actor actor = (Actor)actorIterator.next();

            if (_actorPortValueChanged) {
                _getHelper(actor)._reinitialize();
            } 
            
            if (_listeningMethod.contains("Output")) {
                Iterator outportIterator = actor.outputPortList().iterator();            
                while (outportIterator.hasNext()) {
                    IOPort outport = (IOPort)outportIterator.next();
               
                    outport.addTokenSentListener(listener);
                }
            }
            
            if (_listeningMethod.contains("Input")) {
                /* TokenGotListener not implemented             
                Iterator inportIterator = actor.inputPortList().iterator();            
                while (inportIterator.hasNext()) {
                    IOPort inport = (IOPort)inportIterator.next();
               
                    inport.addTokenGotListener(listener);
                }
                */            
            }
        }
        _actorPortValueChanged = false;                
        
        // run model
        if (!_listeningMethod.equals("NONE")) {
            Manager manager = new Manager(topLevel.workspace(), "PortValueManager");
            ((CompositeActor) topLevel).setManager(manager);
            manager.preinitializeAndResolveTypes();
            ((CompositeActor) topLevel).preinitialize();
            ((CompositeActor) topLevel).initialize();
            ((CompositeActor) topLevel).iterate(1);
            ((CompositeActor) topLevel).wrapup();
        }
        
        // removeTokenSentListener(listener);
        actorIterator = _compositeActor.deepEntityList().iterator();
        while (actorIterator.hasNext()) {
            Actor actor = (Actor)actorIterator.next();

            if (_listeningMethod.contains("Output")) {
                Iterator outportIterator = actor.outputPortList().iterator();            
                while (outportIterator.hasNext()) {
                    IOPort outport = (IOPort)outportIterator.next();
           
                    outport.removeTokenSentListener(listener);
                }
            }
        
            if (_listeningMethod.contains("Input")) {
                /* TokenGotListener not implemented             
                Iterator inportIterator = actor.inputPortList().iterator();            
                while (inportIterator.hasNext()) {
                    IOPort inport = (IOPort)inportIterator.next();
           
                    inport.removeTokenGotListener(listener);
                }
            */   
            }
        }

        actorIterator = _compositeActor.deepEntityList().iterator();
        while (actorIterator.hasNext()) {
            Actor actor = (Actor)actorIterator.next();
            getHelper(actor).updateProperty(true);
        }    
    }

    /**
     * 
     * @param solverName
     * @return
     * @throws IllegalActionException
     */
    public static PortValueSolver findSolver(String solverName)
            throws IllegalActionException {

        Iterator iterator = _solvers.iterator();
        while (iterator.hasNext()) {
            PortValueSolver solver = 
                (PortValueSolver) iterator.next();
            
            if (solver.portValue.getExpression().equals(solverName)) {
                return solver;
            }
        }
        throw new IllegalActionException(
                "Cannot find the \"" + solverName + "\" solver.");
    }

    /** React to a change in an attribute. Clear the previous mappings
     *  for the helpers, so new helpers will be created for the new
     *  lattice.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        
        super.attributeChanged(attribute);
        
        if (attribute == portValue) {
            if (!_portValue.equals(portValue.getExpression())) {
                _portValue = portValue.getExpression();
                _actorPortValueChanged = true;            
            }
        } else if (attribute == listeningMethod) {
            if (!_listeningMethod.equals(listeningMethod.getExpression())) {
                _listeningMethod = listeningMethod.getExpression();
                _actorPortValueChanged = true;            
            }
        }
    }

    
    public StringParameter portValue;
    public StringParameter listeningMethod;
    public Parameter trainingMode;
    
    private void _addChoices() {
        File file = null;
        
        try {
            file = new File(FileUtilities.nameToURL(
                    "$CLASSPATH/ptolemy/data/properties/token", 
                    null, null).getFile());
        } catch (IOException ex) {
            // Should not happen.
            assert false;
        }

        File[] lattices = file.listFiles(); 
        for (int i = 0; i < lattices.length; i++) {
            String latticeName = lattices[i].getName();
            if (lattices[i].isDirectory() && !latticeName.equals("CVS")) {
                portValue.addChoice(latticeName);
            }
        }
        
        listeningMethod.addChoice("NONE");
        listeningMethod.addChoice("Input & Output Ports");
        listeningMethod.addChoice("Input Ports");
        listeningMethod.addChoice("Output Ports");
    }

    private PropertyHelper _getHelper(Object object) 
        throws IllegalActionException {
    
        if (_helperStore.containsKey(object)) {
            return (PropertyHelper) _helperStore.get(object);
        }
        
        String packageName = getClass().getPackage().getName()
                                + ".token." + _portValue;
        
        Class componentClass = object.getClass();
        
        Class helperClass = null;
        while (helperClass == null) {
            try {
                
                // FIXME: Is this the right error message?
                if (!componentClass.getName().contains("ptolemy")) {
                    throw new IllegalActionException("There is no property helper "
                            + " for " + object.getClass());
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
        try {
            constructor = helperClass.getConstructor(
                    new Class[] { PortValueSolver.class, componentClass });
            
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(null, e,
                    "Cannot find constructor method in " 
                    + helperClass.getName());
        }
        
        Object helperObject = null;
        
        try {
            helperObject = constructor.newInstance(new Object[] { this, object });
            
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Failed to create the helper class for property constraints.");
        }
        
        if (!(helperObject instanceof PortValueHelper)) {
            throw new IllegalActionException(
                    "Cannot resolve property for this component: "
                    + object + ". Its helper class does not"
                    + " implement PortValueHelper.");
        }        
        
        _helperStore.put(object, helperObject);
                
        return (PropertyHelper) helperObject;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** A hash map that stores the helpers associated
     *  with the actors.
     */    
    private HashMap _helperStore = new HashMap();    
    private FirstTokenSentListener listener = new FirstTokenSentListener(this);
    private static List _solvers = new ArrayList(); 
    private boolean _actorPortValueChanged = false;
    private String _portValue = "";
    private String _listeningMethod = "";

    public String getSolverIdentifier() {
        return portValue.getExpression();
    }
}
