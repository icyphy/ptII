package ptolemy.actor.lib.opencv.jna.cxcore;

import com.sun.jna.Structure;

public class IplROI extends Structure {
    public int  coi;
    public int  xOffset;
    public int  yOffset;
    public int  width;
    public int  height;
    
    public IplROI() {
        super();
    }
    public static class ByReference extends IplROI implements Structure.ByReference {}
}
