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
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// VQDecode
/**
@author Steve Neuendorffer
@version $Id$
*/

public class VQDecode extends SDFAtomicActor {
    public VQDecode(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container,name);
        IOPort inputport = (IOPort) newPort("index");
        inputport.setOutput(true);
        setTokenConsumptionRate(inputport, 1);

        IOPort outputport = (IOPort) newPort("imagepart");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 1);

        Parameter p = new Parameter(this, "Codebook", 
                new StringToken("VQcodebook.dat"));
        new Parameter(this, "X Dimension", new IntToken("4"));
        new Parameter(this, "Y Dimension", new IntToken("2"));
    }


    public void fire() throws IllegalActionException {
        IntToken index = (IntToken) ((IOPort) getPort("index")).get(0);
        int cw = index.intValue();

        int nn[][] = new int[xsize][ysize];
        int i,j;
        for(j = 0; j < ysize; j++)
            for(i = 0; i < xsize; i++)
                nn[j][i] = codebook[cw][j][i];

        IntMatrixToken ot = new IntMatrixToken(nn);
        ((IOPort) getPort("index")).send(0,ot);
    }

    public void initialize() throws IllegalActionException {
        File sourcefile = null;
        FileInputStream source = null;

        Parameter p = (Parameter) getAttribute("Codebook");
        String filename = ((StringToken)p.getToken()).stringValue();
        Parameter px = (Parameter) getAttribute("X Dimension");
        xsize = ((IntToken)px.getToken()).intValue();
        Parameter py = (Parameter) getAttribute("Y Dimension");
        ysize = ((IntToken)py.getToken()).intValue();

        try {
            sourcefile = new File(filename);
            if(!sourcefile.exists() || !sourcefile.isFile())
                throw new IllegalActionException("Codebook file " + 
                        filename + " does not exist!");
            if(!sourcefile.canRead()) 
                throw new IllegalActionException("Codebook file " +
                        filename + " is unreadable!");
            source = new FileInputStream(sourcefile);
        
            int i,j;
            for(i=0; i<256; i++) {
                codebook[i] = new byte[ysize][xsize];
                for(j=0; j<ysize; j++)
                    if(source.read(codebook[i][j])!=xsize)
                        throw new IllegalActionException("Error reading " +
                                "codebook file!");
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
    }
    
    private byte codebook[][][] = new byte[256][][];
    private int xsize;
    private int ysize;
}



