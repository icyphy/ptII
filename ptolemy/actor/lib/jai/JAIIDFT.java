package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.awt.image.DataBuffer;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.DFTDescriptor;

public class JAIIDFT extends Transformer {
    
    public JAIIDFT(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    public void fire() throws IllegalActionException {
        super.fire();
        _parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        _parameters.addSource(oldImage);
        //_parameters.add(DFTDescriptor.SCALING_NONE);
        //_parameters.add(DFTDescriptor.COMPLEX_TO_REAL);
        RenderedOp newImage = JAI.create("idft", _parameters);
	_parameters = new ParameterBlock();
	_parameters.addSource(newImage);
	_parameters.add(DataBuffer.TYPE_BYTE);
	RenderedOp newerImage = JAI.create("format", _parameters);
        output.send(0, new JAIImageToken(newerImage));
    }
    
    private ParameterBlock _parameters;
}
