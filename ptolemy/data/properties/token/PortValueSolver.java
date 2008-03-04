package ptolemy.data.properties.token;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.PropertySolver;
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

        listeningMethod = new StringParameter(this, "listeningMethod");
        listeningMethod.setExpression("NONE");
        _listeningMethod = listeningMethod.getExpression();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"115\" height=\"40\" "
                + "style=\"fill:yellow\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "Double click to\nResolve Property.</text></svg>");

        //new PropertySolverGUIFactory(this, "_portValueSolverGUIFactory");

        trainingMode = new Parameter(this, "trainingMode");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
        trainingMode.setExpression("true");

        _addChoices();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Returns the helper that contains property information for
     * the given component.
     * @param component The given component
     * @return The associated property constraint helper.
     */
    public PropertyHelper getHelper(Object object)
            throws IllegalActionException {
        return _getHelper(object);
    }

    public String getUseCaseName() {
        return portValue.getExpression();
    }

    public void resolveProperties(CompositeEntity topLevel)
            throws KernelException {

        // get all ports
        if (topLevel.getContainer() != null) {
            throw new IllegalArgumentException(
                    "TypedCompositeActor.resolveProperties:"
                            + " The specified actor is not the top level container.");
        }

        CompositeActor _compositeActor = (CompositeActor) topLevel;

        Iterator actorIterator = _compositeActor.deepEntityList().iterator();
        while (actorIterator.hasNext()) {
            Actor actor = (Actor) actorIterator.next();

            if (_actorPortValueChanged) {
                _getHelper(actor).reinitialize();
            }

            if (_listeningMethod.contains("Output")) {
                Iterator outportIterator = actor.outputPortList().iterator();
                while (outportIterator.hasNext()) {
                    IOPort outport = (IOPort) outportIterator.next();

                    throw new Error ("FIXME: The next commented out line does not compile.");
                    //outport.addTokenSentListener(listener);
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
            Manager manager = new Manager(topLevel.workspace(),
                    "PortValueManager");
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
            Actor actor = (Actor) actorIterator.next();

            if (_listeningMethod.contains("Output")) {
                Iterator outportIterator = actor.outputPortList().iterator();
                while (outportIterator.hasNext()) {
                    IOPort outport = (IOPort) outportIterator.next();

                    throw new Error("FIXME: The next commented out line does not compile");
                    //outport.removeTokenSentListener(listener);
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

        boolean isTraining = ((BooleanToken) trainingMode.getToken())
                .booleanValue();

        actorIterator = _compositeActor.deepEntityList().iterator();
        while (actorIterator.hasNext()) {
            Actor actor = (Actor) actorIterator.next();
            getHelper(actor).updateProperty(isTraining);
        }
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
            if (!getUseCaseName().equals(portValue.getExpression())) {
                _actorPortValueChanged = true;
            }
        } else if (attribute == listeningMethod) {
            if (!_listeningMethod.equals(listeningMethod.getExpression())) {
                _listeningMethod = listeningMethod.getExpression();
                _actorPortValueChanged = true;
            }
        }
    }

    public String getExtendedUseCaseName() {
        return "token::" + getUseCaseName();
    }

    public StringParameter portValue;
    public StringParameter listeningMethod;

    private void _addChoices() {
        File file = null;

        try {
            file = new File(FileUtilities.nameToURL(
                    "$CLASSPATH/ptolemy/data/properties/token", null, null)
                    .getFile());
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    private FirstTokenSentListener listener = new FirstTokenSentListener(this);

    private boolean _actorPortValueChanged = false;

    private String _listeningMethod = "";
}
