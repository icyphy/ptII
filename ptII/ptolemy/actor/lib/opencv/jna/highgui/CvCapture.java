package ptolemy.actor.lib.opencv.jna.highgui;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.PointerByReference;

public class CvCapture extends PointerType {
    public CvCapture(){
        super();
    }
    public CvCapture(Pointer p){
        super(p);
    }
    public PointerByReference getPointerByReference() {
        return new PointerByReference(this.getPointer());
    }
}
