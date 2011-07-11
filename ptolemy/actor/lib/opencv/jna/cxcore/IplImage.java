package ptolemy.actor.lib.opencv.jna.cxcore;

import ptolemy.actor.lib.opencv.jna.cxcore.CxcoreLib.IplTileInfo;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

public class IplImage extends Structure {
    public int  nSize;
    public int  ID;
    public int  nChannels;
    public int  alphaChannel;
    public int  depth;
    public byte[] colorModel = new byte[4];
    public byte[] channelSeq = new byte[4];
    public int  dataOrder;
    public int  origin;
    public int  align;
    public int  width;
    public int  height;
    //public IplROI.ByReference roi;
    //public IplImage maskROI;
    public Pointer roi;
    public Pointer maskROI;
    public Pointer imageId;
    public IplTileInfo tileInfo;
    public int  imageSize;
    public Pointer imageData;
    public int  widthStep;
    public int[]  BorderMode  = new int[4];
    public int[]  BorderConst = new int[4];
    public Pointer imageDataOrigin;

    public IplImage() {
        super();
    }
    public IplImage(Pointer p) {
        super(p);
    }
    public static class ByReference extends IplImage implements Structure.ByReference {}

    public ByReference getByReference() {
        return new ByReference();
    }
    public PointerByReference getPointerByReference() {
        return new PointerByReference(this.getPointer());
    };
}
