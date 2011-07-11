package ptolemy.actor.lib.opencv.jna.cv;

import com.sun.jna.Structure;

public class CvSize extends Structure {
    public int width;
    public int height;

    public CvSize() {
        super();
    }
    public CvSize(int w, int h) {
        super();
        width = w; height = h;
    }
}
