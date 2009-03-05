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

        solutionColor = new ColorAttribute(this, "solutionColor");
        solutionColor.setToken("{1.0, 1.0, 1.0, 1.0}");
        
        _icon = new LatticeElementIcon(this, "LatticeElementIcon");
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
      
    private LatticeElementIcon _icon;

    public ColorAttribute solutionColor;
    
    public Parameter isAcceptableSolution;

}
