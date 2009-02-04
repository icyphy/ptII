package ptolemy.domains.properties;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

public class LatticeElement extends State {

    public LatticeElement(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        refinementName.setVisibility(Settable.NONE);

        //isInitialState.setDisplayName("isInitialEvent");
        //isFinalState.setDisplayName("isFinalEvent");
        isInitialState.setVisibility(Settable.NONE);
        isInitialState.setPersistent(false);
        isInitialState.setToken(BooleanToken.FALSE);
        isFinalState.setVisibility(Settable.NONE);
        isFinalState.setPersistent(false);
        
        isAcceptableSolution = new Parameter(this, "isAcceptableSolution", BooleanToken.TRUE);
        
        isAcceptableSolution.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == isAcceptableSolution) {
            Attribute icon = getAttribute("LatticeElementIcon");            
            Attribute color = icon.getAttribute("fill");

            if (((BooleanToken) ((Parameter) attribute)
                    .getToken()).booleanValue()) {
                
                // remove highlight color
                if (color != null) {
                    try {
                        color.setContainer(null);
                    } catch (NameDuplicationException ex) {
                        // This shouldn't happen.
                        assert false;
                    }
                }
            } else {
                // Color the element.
                if (color != null) {
                    ((ColorAttribute) color).setToken(_unacceptableElementColor);
                } else {
                    try {
                        new ColorAttribute(icon, "fill");
                    } catch (NameDuplicationException e) {
                        // This shouldn't happen.
                        assert false;
                    }
                }
            }
        }
        
    }        
    

    public Parameter isAcceptableSolution;

    private String _unacceptableElementColor = "{1.0, 0.0, 0.0, 1.0}";
}
