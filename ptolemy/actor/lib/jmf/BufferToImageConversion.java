package ptolemy.actor.lib.jmf;

// FIXME: Replace with per-class imports.
import java.awt.*;
import javax.media.*;
import javax.media.Buffer;
import javax.media.control.TrackControl;
import javax.media.Format;
import javax.media.format.*;

import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import javax.media.util.BufferToImage;
import javax.swing.ImageIcon;
import java.util.Iterator;
import java.util.Vector;


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

