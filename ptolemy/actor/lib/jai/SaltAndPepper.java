package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.DoubleToken;

public class SaltAndPepper extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SaltAndPepper(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);

        probability = new Parameter(this, "probability", new DoubleToken("0.1F"));
    }

    public Parameter probability;

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == probability) {
            _probability = ((DoubleToken)probability.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    public void fire() throws IllegalActionException {
        super.fire();
        
        DoubleMatrixToken doubleMatrixToken = (DoubleMatrixToken) input.get(0);
        double data[][] = doubleMatrixToken.doubleMatrix();
        int width = doubleMatrixToken.getRowCount();
        int height = doubleMatrixToken.getColumnCount();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = Math.random();
                if (value < _probability) {
                    if (value < _probability/2) {
                        data[i][j] = 0.0F;
                    } else {
                        data[i][j] = 255.0F;
                    }
                }
            }
        }
        output.send(0, new DoubleMatrixToken(data)); 
    }
    
    private double _probability;
}
