/*
@Copyright (c) 1998 The Regents of the University of California.
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

public class ImageSequence extends SDFAtomicActor {
    public ImageSequence(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container,name);
        IOPort outputport = (IOPort) newPort("image");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 1);

        Parameter p = new Parameter(this, "File Name Template", 
                new StringToken("missa***.qcf"));
        new Parameter(this, "X Image Size", new IntToken("176"));
        new Parameter(this, "Y Image Size", new IntToken("144"));
        new Parameter(this, "Start Frame", new IntToken("0"));
        new Parameter(this, "End Frame", new IntToken("10"));
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
        p = (Parameter) getAttribute("Start Frame");
        xsize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("End Frame");
        ysize = ((IntToken)p.getToken()).intValue();
 
        numframes = endframe-startframe+1;
        frames = new byte[numframes][ysize][xsize];
        try {
            for(framenumber = 0; 
                framenumber <= numframes; 
                framenumber++) {
                byte arr[] = fileroot.getBytes();
                int i = framenumber + startframe;
                int loc = fileroot.lastIndexOf('*');
                while(loc >= 0) {
                    arr[loc] = (byte)(i % 10);
                    i = i / 10;
                    loc = fileroot.lastIndexOf('*');
                }
                String filename = new String(arr);
                
                sourcefile = new File(filename);
                if(!sourcefile.exists() || !sourcefile.isFile())
                    throw new IllegalActionException("Codebook file " + 
                            filename + " does not exist!");
                if(!sourcefile.canRead()) 
                    throw new IllegalActionException("Codebook file " +
                            filename + " is unreadable!");
                source = new FileInputStream(sourcefile);
  
                int j;
                byte b[] = new byte[1];
                for(j = 0; j < ysize; j++) 
                    for(i = 0; i < xsize; i++) {
                        if(source.read(b)!=1)
                            throw new IllegalActionException("Error reading " +
                                    "codebook file!");
                        frames[framenumber][j][i] = b[0];
                    }
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
    }

    public void fire() throws IllegalActionException {
        int frame[][] = new int[xsize][ysize];
        int i, j;
        for(j = 0; j < ysize; j++)
            for(i = 0; i < xsize; i++)
                frame[j][i] = (int) frames[framenumber][j][i];
        IntMatrixToken message = new IntMatrixToken(frame);
        ((IOPort) getPort("output")).send(0, message);
    }
    String filetemplate;
    byte frames[][][];
    int xsize;
    int ysize;
    int startframe;
    int endframe;
    int numframes;
    int framenumber;

}
