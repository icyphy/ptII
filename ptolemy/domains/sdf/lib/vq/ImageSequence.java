/*
@Copyright (c) 1998-1999 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY
*/
package ptolemy.domains.sdf.lib.vq;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// ImageSequence
/**
@author Steve Neuendorffer
@version $Id$
*/

public final class ImageSequence extends SDFAtomicActor {
    public ImageSequence(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container,name);
        IOPort outputport = (IOPort) newPort("image");

        outputport.setOutput(true);
        setTokenProductionRate(outputport, 1);

        Parameter p = new Parameter(this, "File Name Template", 
                new StringToken("/users/neuendor/htvq/seq/missa/missa***.qcf"));
        new Parameter(this, "XImageSize", new IntToken("176"));
        new Parameter(this, "YImageSize", new IntToken("144"));
        new Parameter(this, "Start Frame", new IntToken("0"));
        new Parameter(this, "End Frame", new IntToken("30"));
    }

    public void initialize() throws IllegalActionException {
        File sourcefile = null;
        FileInputStream source = null;
        Parameter p = (Parameter) getAttribute("File Name Template");
        String fileroot = ((StringToken)p.getToken()).stringValue();
        p = (Parameter) getAttribute("Start Frame");
        int startframe = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("End Frame");
        int endframe = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("XImageSize");
        xsize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YImageSize");
        ysize = ((IntToken)p.getToken()).intValue();
 
        numframes = endframe-startframe+1;
        frames = new byte[numframes][ysize*xsize];
        frame = new int[ysize*xsize];
        try {
            for(framenumber = 0; 
                framenumber < numframes; 
                framenumber++) {
 
                byte arr[] = fileroot.getBytes();
                int i = framenumber + startframe;
                String tfilename = new String(fileroot);
                int loc = tfilename.lastIndexOf('*');
                while(loc >= 0) {
                    arr[loc] = (byte)('0' + i % 10);
                    i = i / 10;
                    tfilename = new String(arr);
                    loc = tfilename.lastIndexOf('*');
                }
                String filename = new String(arr);
                
                sourcefile = new File(filename);
                if(!sourcefile.exists() || !sourcefile.isFile())
                    throw new IllegalActionException("Image file " + 
                            filename + " does not exist!");
                if(!sourcefile.canRead()) 
                    throw new IllegalActionException("Image file " +
                            filename + " is unreadable!");
                source = new FileInputStream(sourcefile);
  
                int j;
                //              for(j = ysize - 1; j >= 0; j--) { 
                        if(source.read(frames[framenumber],0,ysize*xsize)!=ysize*xsize)
                            throw new IllegalActionException("Error reading " +
                                    "Image file!");
                        //  }
            }
        }
        catch (Exception e) {
            throw new IllegalActionException(e.getMessage());
        }
        finally {
            if(source!=null) {
                try {
                    source.close(); 
                }
                catch (IOException e) {
                }
            }
        }
        framenumber = 0;    
        port_image = (IOPort) getPort("image");
    }

    public void fire() throws IllegalActionException {
        int i, j, n;
        workspace().setReadOnly(true);
       
        System.out.println("frame " + framenumber);
        // This is necessary to convert from bytes to ints
        for(i = 0, n = 0; i < ysize; i++) {
            for(j = 0; j < xsize; j++, n++)
                frame[n] = frames[framenumber][n];
        }

        ImageToken message = new ImageToken(frame, ysize, xsize);
        port_image.send(0, message);

        framenumber++; 
        if(framenumber >= numframes) framenumber = 0;
    }

    public void wrapup() throws IllegalActionException {
        workspace().setReadOnly(false);
    }

    String filetemplate;
    byte frames[][];
    int xsize;
    int ysize;
    int frame[];
    int startframe;
    int endframe;
    int numframes;
    int framenumber;
    IOPort port_image;

}
