package ptolemy.actor.lib.jmf;

import javax.media.Buffer;
import javax.media.format.VideoFormat;

import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.awt.Image;
import javax.media.util.BufferToImage;

public class BufferToImageConversion extends Transformer {
    public BufferToImageConversion(CompositeEntity container, String name)
	    throws IllegalActionException, NameDuplicationException {
	super(container, name);
	input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT); }

    public void fire() throws IllegalActionException {
	super.fire();
	if (input.hasToken(0)) {
	    ObjectToken objectToken = (ObjectToken) input.get(0);
	    Buffer in = (Buffer) objectToken.getValue();
	    VideoFormat videoFormat = (VideoFormat) in.getFormat();
	    //the following my be expensive, and we might want to have 
	    //videoFormat be specifed by the user at the beginning, or
	    //let it default to the following.
	    BufferToImage bufferToImage = new BufferToImage(videoFormat);
	    _image = bufferToImage.createImage(in);
	    if (_image != null) {
		output.send(0, new ObjectToken(_image));
	    }
	}
    }
    private Image _image;
    //private VideoFormat videoFormat;
    //private Buffer in;
}

