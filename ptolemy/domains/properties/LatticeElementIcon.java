package ptolemy.domains.properties;

import java.awt.Color;
import java.awt.Paint;

import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.fsm.StateIcon;

public class LatticeElementIcon extends StateIcon {

    public LatticeElementIcon(NamedObj container, String name)
    throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    protected Paint _getFill() {
        NamedObj container = getContainer();

        if (container instanceof LatticeElement) {
            try {
                LatticeElement element = (LatticeElement) container;

                boolean isAcceptable = ((BooleanToken) element
                        .isAcceptableSolution.getToken()).booleanValue();

                if (!isAcceptable) {
                    return element.solutionColor.asColor().darker();
                } else {
                    return element.solutionColor.asColor();
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }        
        return Color.white;
    }

    protected float _getLineWidth() {
        NamedObj container = getContainer();

        if (container instanceof LatticeElement) {
            try {
                LatticeElement element = (LatticeElement) container;

                boolean isAcceptable = ((BooleanToken) element
                        .isAcceptableSolution.getToken()).booleanValue();

                if (!isAcceptable) {
                    return 3.0f;
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }        
        return 1.0f;
    }
}
