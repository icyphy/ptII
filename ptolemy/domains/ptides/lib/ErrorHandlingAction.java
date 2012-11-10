package ptolemy.domains.ptides.lib;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.SetVariable;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute; 
import sun.security.x509.AttributeNameEnumeration;

public class ErrorHandlingAction extends SetVariable {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public ErrorHandlingAction(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        action = new Parameter(this, "action");
        action.setTypeEquals(BaseType.STRING);
        action.addChoice("\"" + DropEvent + "\"");
        action.addChoice("\"" + ExecuteEvent + "\"");
        action.addChoice("\"" + FixTimestamp + "\"");
        action.addChoice("\"" + ClearAllEvents + "\"");
        action.addChoice("\"" + ClearEarlierEvents + "\"");
        action.addChoice("\"" + ClearCorruptEvents + "\"");
        action.setExpression("\"" + DropEvent + "\"");
        
        delayed.setExpression("false");
    }

    public Parameter action;
    
    public static String DropEvent = "dropEvent";
    public static String ExecuteEvent = "executeEvent";
    public static String FixTimestamp = "fixTimestamp";
    public static String ClearAllEvents = "clearAllEvents";
    public static String ClearEarlierEvents = "clearEarlierEvents";
    public static String ClearCorruptEvents = "clearCorruptEvents";
    
    public static enum ErrorHandlingActionString{
        DropEvent, ExecuteEvent, FixTimestamp, ClearAllEvents, ClearEarlierEvents, ClearCorruptEvents
    }
    
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == action) {
            String string = ((StringToken)action.getToken()).stringValue();
            variableName.setExpression(string);
            
            if (getContainer() instanceof CompositeActor) {
                CompositeActor container = (CompositeActor)getContainer();
                while (!container.getName().equals("ErrorHandler")) {
                    if (container == container.toplevel()) {
                        // Running cd $PTII/ptolemy/configs/test; make results in this class being
                        // cloned and going into an infinite loop unless we fail here.
                        throw new IllegalActionException(this, "Could not find container named ErrorHandler");
                    }
                    container = (CompositeActor)getContainer();
                }
                if (container.getAttribute(string) == null) {
                    try { 
                        Parameter parameter = new Parameter(container, string);
                        parameter.setExpression("false"); 
                    } catch (NameDuplicationException e) {
                        // caught by checking earlier, cannot
                        // get here.
                    }
                    
                }
            } // else it is in the library.
        }
        super.attributeChanged(attribute);
    }
    
}
