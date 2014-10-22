/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package ptolemy.actor.lib.opencv.jna.cxcore;

import java.util.Arrays;
import java.util.List;

import ptolemy.actor.lib.opencv.jna.cxcore.CxcoreLib.IplTileInfo;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

public class IplImage extends Structure {
    public int nSize;
    public int ID;
    public int nChannels;
    public int alphaChannel;
    public int depth;
    public byte[] colorModel = new byte[4];
    public byte[] channelSeq = new byte[4];
    public int dataOrder;
    public int origin;
    public int align;
    public int width;
    public int height;
    //public IplROI.ByReference roi;
    //public IplImage maskROI;
    public Pointer roi;
    public Pointer maskROI;
    public Pointer imageId;
    public IplTileInfo tileInfo;
    public int imageSize;
    public Pointer imageData;
    public int widthStep;
    public int[] BorderMode = new int[4];
    public int[] BorderConst = new int[4];
    public Pointer imageDataOrigin;

    public IplImage() {
        super();
    }

    public IplImage(Pointer p) {
        super(p);
    }

    public static class ByReference extends IplImage implements
            Structure.ByReference {
    }

    public ByReference getByReference() {
        return new ByReference();
    }

    public PointerByReference getPointerByReference() {
        return new PointerByReference(this.getPointer());
    };

    /** Return the field names in the proper order.
     *  <p>This is new in jna-3.5.0.
     *  @return a list of strings that name the fields in order.
     */
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "nSize", "ID", "nChannels",
                "alphaChannel", "depth", "colorModel", "channelSeq",
                "dataOrder", "origin",
                "align",
                "width",
                "height",
                //public IplROI.ByReference roi
                //public IplImage maskROI
                "roi", "maskROI", "imageId", "tileInfo", "imageSize",
                "imageData", "widthStep", "BorderMode", "BorderConst",
                "ImageDataOrigin" });
    }
}
