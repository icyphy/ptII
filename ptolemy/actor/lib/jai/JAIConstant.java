package ptolemy.actor.lib.jai;

import java.awt.image.renderable.ParameterBlock;
import java.lang.Double;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Source;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

public class JAIConstant extends Source {

    public JAIConstant(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.OBJECT);
        width = new Parameter(this, "width", new IntToken(0));
        height = new Parameter(this, "height", new IntToken(0));
        bandValues = new Parameter(this, "bandValues", 
                new ArrayToken(_defaultValues));
    }

    public Parameter bandValues;
    public Parameter height;
    public Parameter width;

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == bandValues) {
            Token values[] = ((ArrayToken)bandValues.getToken()).arrayValue();
            _bandValues = new Double[values.length];
            for (int i = 0; i < values.length; i++) {
                _bandValues[i] = 
                    new Double(((DoubleToken)values[i]).doubleValue());
            }
        } else if (attribute == height) {
            _height = ((IntToken)height.getToken()).intValue();
        } else if (attribute == width) {
            _width = ((IntToken)width.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    public void fire() throws IllegalActionException {
        super.fire();
        ParameterBlock parameters = new ParameterBlock();
        parameters.add((float)_width);
        parameters.add((float)_height);
        parameters.add(_bandValues);
        RenderedOp newImage = JAI.create("constant", parameters);
        output.send(0, new JAIImageToken(newImage));        
    }
    
    private DoubleToken _zero = new DoubleToken("0.0F");
    private DoubleToken _defaultValues[] = {_zero};
    private Double _bandValues[];
    private int _height;
    private int _width;
}
