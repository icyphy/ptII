package ptolemy.actor.lib.opencv.jna.cxcore;


import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class CvArr extends Structure {
    public CvArr() {
        super();
    };
    public CvArr(Pointer p) {
        super(p);
    };
}
