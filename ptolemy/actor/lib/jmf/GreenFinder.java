package ptolemy.actor.lib.jmf;

// FIXME: Replace with per-class imports.
import java.awt.*;
import javax.media.*;
import javax.media.Buffer;
import javax.media.control.TrackControl;
import javax.media.Format;
import javax.media.format.*;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.DoubleToken;
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

public class GreenFinder extends TypedAtomicActor {
    public GreenFinder(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        outputX = new TypedIOPort(this, "outputX", false, true);
        outputY = new TypedIOPort(this, "outputY", false, true);
        input.setTypeEquals(BaseType.OBJECT);
        outputX.setTypeEquals(BaseType.DOUBLE);
        outputY.setTypeEquals(BaseType.DOUBLE);


    }

    /* convert a byte into an unsigned int */
    public int bts(byte b) {
        return (int)b & 0xFF;
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        for (int i = 0; i < histSize; i += 1) {
            if (i > yLow && i < yHigh) {
                yClass[i] = 1; }
            else {
                yClass[i] = 0; }
            if (i > uLow && i < uHigh) {
                uClass[i] = 1; }
            else {
                uClass[i] = 0; }
            if (i > vLow && i < vHigh) {
                vClass[i] = 1; }
            else { vClass[i] = 0; }
        }
    }

    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            ObjectToken objectToken = (ObjectToken) input.get(0);
            Buffer in = (Buffer) objectToken.getValue();
            VideoFormat videoFormat = (VideoFormat)in.getFormat();
            YUVFormat yuvFormat = (YUVFormat) videoFormat;
            byte[] data = (byte[])in.getData();
            if (data != null) {
                System.arraycopy(data, yuvFormat.getOffsetY(), YArray, 0, YArray.length);
                System.arraycopy(data, yuvFormat.getOffsetU(), UArray, 0, UArray.length);
                System.arraycopy(data, yuvFormat.getOffsetV(), VArray, 0, VArray.length);

                for (int x = 0; x < frameWidth; x += 1) {
                    for (int y = 0; y < frameHeight; y += 1) {
                        int yComp = getYComponent(x, y);
                        int uComp = getUComponent(x, y);
                        int vComp = getVComponent(x, y);

                        int compInClass = yClass[yComp] & uClass[uComp] & vClass[vComp];
                        if (compInClass==1) {
                            sumX += x;
                            sumY += y;
                            inCount += 1;
                        }
                    }
                }
                if (inCount > 0) {
                    double xLocation = (double) sumX/inCount;
                    double yLocation = (double) (frameHeight - sumY/inCount);
                    outputX.send(0, new DoubleToken(xLocation));
                    outputY.send(0, new DoubleToken(yLocation));
                    if (_debugging) {
                        _debug("just sent " + (int)xLocation + "and " + (int)yLocation);
                    }
                }
                inCount = 0;
                sumX = 0;
                sumY = 0;
            }
        }
    }
    /* Return the int representing the Y band at this pixel*/
    public int getYComponent(int point) {
        return bts(YArray[point]);
    }

    /* Return the int representing the U band at this pixel*/
    public int getUComponent(int point) {
        return bts(UArray[point]);
    }

    /* Return the int representing the V band at this pixel*/
    public int getVComponent(int point) {
        return bts(VArray[point]);
    }

    /* Return the int representing the Y band at this pixel*/
    public int getYComponent(int x, int y) {
        return getYComponent(x + 320 * y);
    }

    /* Return the int representing the U band at this pixel*/
    public int getUComponent(int x, int y) {
        return getUComponent((x >> 1) + (y >> 1) * 160);
    }

    /* Return the int representing the V band at this pixel*/
    public int getVComponent(int x, int y) {
        return getVComponent((x >> 1) + (y >> 1) * 160);
    }


    //FIXME should be parameters and moved to initialize
    public int frameWidth = 320;
    public int frameHeight = 240;

    //public YUVFormat videoFormat = new YUVFormat();

    private byte[] YArray = new byte[frameWidth * frameHeight];
    private byte[] UArray = new byte[frameWidth/2 * frameHeight/2];
    private byte[] VArray = new byte[frameWidth/2 * frameHeight/2];

    public int histSize = 256;
    public int inCount = 0;
    public int sumX = 0;
    public int sumY = 0;

    int[] yClass = new int[histSize];
    int[] uClass = new int[histSize];
    int[] vClass = new int[histSize];

    //FIXME again, following numbers should be parameters
    //the following is for green

    int yLow = 110;
    int yHigh = 210;
    int uLow = 85;
    int uHigh = 110;
    int vLow = 120;
    int vHigh = 130;

    public TypedIOPort input;;
    public TypedIOPort outputX;
    public TypedIOPort outputY;
}


