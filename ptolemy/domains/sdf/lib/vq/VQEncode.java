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
//// VQEncode
/**
@author Steve Neuendorffer
@version $Id$
*/

public class VQEncode extends SDFAtomicActor {
    public VQEncode(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container,name);
        IOPort outputport = (IOPort) newPort("index");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 1);

        IOPort inputport = (IOPort) newPort("imagepart");
        inputport.setInput(true);
        setTokenConsumptionRate(inputport, 1);

        Parameter p = new Parameter(this, "Codebook", 
                new StringToken("/users/neuendor/htvq/usc_hvq_s5.dat"));
	new Parameter(this, "XFramesize", new IntToken("176"));
        new Parameter(this, "YFramesize", new IntToken("144"));
        new Parameter(this, "XPartitionSize", new IntToken("4"));
        new Parameter(this, "YPartitionSize", new IntToken("2"));
    }


    public void fire() throws IllegalActionException {
        ObjectToken t = (ObjectToken) ((IOPort) getPort("imagepart")).get(0);

        _partitions = (int[][]) t.getValue();
        int numpartitions = 
            _xframesize * _yframesize / _xpartsize / _ypartsize;

        int j;
        for(j = 0; j < numpartitions; j++) {
            _part = _partitions[j];

            int i,nn=0;
            long nnd = 1000000,td;
            for(i = 0; i < 256; i++) {
                td = _distortion(_part, _codebook[2][i], 
                        _xpartsize * _ypartsize);
                if(td < nnd)
                    {nnd = td; nn = i;}
            }
            _codewords[j] = nn;
            //          System.out.println(nn);
        }
        ObjectToken ot = new ObjectToken(_codewords);
        ((IOPort) getPort("index")).send(0,ot);

    }

    public void initialize() throws IllegalActionException {
        File sourcefile = null;
        FileInputStream source = null;
        
        Parameter p; 
	p = (Parameter) getAttribute("XFramesize");
        _xframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YFramesize");
        _yframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("XPartitionSize");
        _xpartsize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YPartitionSize");
        _ypartsize = ((IntToken)p.getToken()).intValue();

        _codewords = 
            new int[_yframesize * _xframesize / _ypartsize / _xpartsize];

        p = (Parameter) getAttribute("Codebook");
        String filename = ((StringToken)p.getToken()).stringValue();
        try {
            sourcefile = new File(filename);
            if(!sourcefile.exists() || !sourcefile.isFile())
                throw new IllegalActionException("Codebook file " + 
                        filename + " does not exist!");
            if(!sourcefile.canRead()) 
                throw new IllegalActionException("Codebook file " +
                        filename + " is unreadable!");
            source = new FileInputStream(sourcefile);

            byte temp[];
            int i, j, y, x, size = 1;
            for(i=0; i<3; i++) {
                size = size * 2;
                temp = new byte[size];
                for(j=0; j<256; j++) {
                    _codebook[i][j] = new int[size];
                    if(source.read(temp)!=size)
                        throw new IllegalActionException("Error reading " +
                                "codebook file!");
                    for(x = 0; x < size; x++)
                        _codebook[i][j][x] = temp[x];
                }
                // skip over the lookup tables.
                source.skip(65536);
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
    
    long _distortion(int a[], int b[], int len) {
        long c,d = 0;
        int i;
        for(i = 0;i<len;i++)
        {
                c = ((a[i] & 255) - (b[i] & 255));
                d += c * c;
        }
        return d;
    }

    private int _codebook[][][] = new int[6][256][];
    private int _codewords[];
    private int _partitions[][];
    private int _part[];
    private int _xframesize;
    private int _yframesize;
    private int _xpartsize;
    private int _ypartsize;
}



