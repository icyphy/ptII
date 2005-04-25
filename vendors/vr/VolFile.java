/*
 *	%Z%%M% %I% %E% %U%
 *
 * Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package vendors.vr;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.Box;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.DataBufferByte;
import java.awt.color.ColorSpace;
import java.awt.Transparency;
import java.io.*;
import java.net.*;


/**
 * A VolFile is object holds the volume data from a file.
 */
public class VolFile {

    int xDim = 0;
    int dummyint = 0;
    byte dummybyte = 0;
    short dummyshort = 0;
    int yDim = 0;
    int zDim = 0;
    float xSpace = 0;
    float ySpace = 0;
    float zSpace = 0;
    int dataOffset = 0;
    int minVal = 0;
    int maxVal = 0;
    int bytesPerVoxel = 0;
    byte[] id = new byte[64];
    int pos = 0;
    DataInputStream file = null;
    byte[][][] fileData;

    // This windowing stuff is for the 16->8 mapping for the CT data
    // TODO: make this another attribute
    double windowCenter = 880.0;
    double windowWidth = 930.0;
    double windowBase = windowCenter - windowWidth / 2;

    /**
     * Creates a VolFile from a URL.
     * @param voldat  The URL for a .vol file holding the volume
     */
    public VolFile(URL voldat) throws java.io.IOException {
	try {
	     file = new DataInputStream(voldat.openStream());
	} catch(FileNotFoundException fnf) {
	     System.out.println(fnf.getMessage());
	}
	byte[] magicBuffer = new byte[4];
	file.read(magicBuffer, 0, 4);
	pos += 4;
	String magic = new String(magicBuffer, 0, 4);
	if (!magic.equals("vol3")) {
	    System.out.println("file specified is not a .vol file" +
		" header begins with " + magic);
	    System.exit(0);
	}
	xDim = file.readInt();
	pos += 4;
	yDim = file.readInt();
	pos += 4;
	zDim = file.readInt();
	pos += 4;
	xSpace = file.readFloat();
	pos += 4;
	ySpace = file.readFloat();
	pos += 4;
	zSpace = file.readFloat();
	pos += 4;
	dataOffset = file.readInt();
	pos += 4;
	minVal = (int)file.readShort();
	pos += 2;
	maxVal = (int)file.readShort();
	pos += 2;
	bytesPerVoxel = (int)file.readByte();
	pos++ ;
	/* Following is padding to avoid need to seek */
	dummybyte = file.readByte();
	pos ++ ;
	for (int i = 0; i < 13; i++) {
		dummyshort = file.readShort();
	pos += 2;
	}
	file.read(id, 0, 64);
	pos += 64;

	fileData = new byte[zDim][yDim][xDim];
	if (pos != dataOffset) System.out.print("VolFile: Pointer Mismatch");
	if (bytesPerVoxel == 1) {
	    System.out.print("Reading data...");
	    for (int z = 0; z < zDim; z++) {
		for (int y = 0; y < yDim; y++) {
		    int vIndex = (z * xDim * yDim + y * xDim);
		    byte[] dataRow = fileData[z][y];
		    file.readFully(dataRow, 0, xDim);
		}
	    }
	    System.out.println("done");
	} else {
	    byte[] buffer = new byte[xDim * 2];
	    System.out.print("Reading and windowing data");
	    for (int z = 0; z < zDim; z++) {
		for (int y = 0; y < yDim; y++) {
		    int vIndex = (z * xDim * yDim + y * xDim) * 2;
		    byte[] dataRow = fileData[z][y];
		    file.readFully(buffer, 0, xDim * 2);
		    for (int x = 0; x < xDim; x++) {
			int index = x * 2;
			// Map the pair of bytes into a short and then window
			// into a byte
			int low = buffer[index+1];
			if (low < 0) {
			    low += 256;
			}
			int high = buffer[index];
			if (high < 0) {
			    high += 256;
			}
			short fileValue =  (short)((high << 8)  + (low << 0));
			fileValue -= minVal;
			double scaleValue = (fileValue-windowBase)/windowWidth;
			if (scaleValue > 1.0) {
			    scaleValue = 1.0;
			}
			if (scaleValue < 0.0) {
			    scaleValue = 0.0;
			}
			dataRow[x] = (byte)(scaleValue * 255);
		    }
		}
		System.out.print(".");
	    }
	    System.out.println("done");
	}
    }

    /**
     * Returns the ID string for the volume file
     */
    String getId() {
	return new String(id);
    }

    /**
     * Gets the X dimension of the volume
     */
    int getXDim() {
	return xDim;
    }

    /**
     * Gets the Y dimension of the volume
     */
    int getYDim() {
	return yDim;
    }

    /**
     * Gets the Z dimension of the volume
     */
    int getZDim() {
	return zDim;
    }
}
