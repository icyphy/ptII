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
@AcceptedRating Red
@ProposedRating Red
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
import java.net.*;

//////////////////////////////////////////////////////////////////////////
//// HTVQEncode
/**
@author Steve Neuendorffer
@version $Id$
*/

public final class HTVQEncode extends SDFAtomicActor {
    public HTVQEncode(TypedCompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        SDFIOPort outputport = (SDFIOPort) newPort("index");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 3168);
        outputport.setDeclaredType(IntToken.class);
        
        SDFIOPort inputport = (SDFIOPort) newPort("imagepart");
        inputport.setInput(true);
        setTokenConsumptionRate(inputport, 3168);
        inputport.setDeclaredType(IntMatrixToken.class);

        Parameter p = new Parameter(this, "Codebook", 
                new StringToken("/users/neuendor/htvq/usc_hvq_s5.dat"));
	new Parameter(this, "XFramesize", new IntToken("176"));
        new Parameter(this, "YFramesize", new IntToken("144"));
        new Parameter(this, "XPartitionSize", new IntToken("4"));
        new Parameter(this, "YPartitionSize", new IntToken("2"));
    }


    public void fire() throws IllegalActionException {
        int numpartitions = 
            _xframesize * _yframesize / _xpartsize / _ypartsize;

        int j;
        ((SDFIOPort) getPort("imagepart")).getArray(0, _tokens); 

        for(j = 0; j < numpartitions; j++) {
           _codewords[j] = new IntToken(
                _encode(_tokens[j].intArray(), _xpartsize * _ypartsize));
        }

        ((SDFIOPort) getPort("index")).sendArray(0, _codewords);

    }

    public void initialize() throws IllegalActionException {
  
        InputStream source = null;
        
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
            new IntToken[_yframesize * _xframesize / _ypartsize / _xpartsize];
        _tokens =
            new IntMatrixToken[_yframesize * _xframesize / _ypartsize / _xpartsize];

        p = (Parameter) getAttribute("Codebook");
        String filename = ((StringToken)p.getToken()).stringValue();
        try {
            if (filename != null) {
                if(_baseurl != null) {
                    try {
                        // showStatus("Reading data");
                        URL dataurl = new URL(_baseurl, filename);
                        System.out.println("dataurl=" + dataurl);
                        source = dataurl.openStream();
                        //showStatus("Done");
                    } catch (MalformedURLException e) {
                        System.err.println(e.toString());
                    } catch (FileNotFoundException e) {
                        System.err.println("RLEncodingApplet: " +
                                "file not found: " +e);
                    } catch (IOException e) {
                        System.err.println(
                                "RLEncodingApplet: error reading"+
                                " input file: " +e);
                    }
                } else {
                    File sourcefile = new File(filename);
                    if(!sourcefile.exists() || !sourcefile.isFile())
                        throw new IllegalActionException("Image file " + 
                                filename + " does not exist!");
                    if(!sourcefile.canRead()) 
                        throw new IllegalActionException("Image file " +
                                filename + " is unreadable!");
                    source = new FileInputStream(sourcefile);
                }                      
            }
            
            int i, j, y, x, size = 1;
            byte temp[];
            for(i = 0; i<5; i++) {
                size = size * 2;
                temp = new byte[size];
                for(j = 0; j<256; j++) {
                    _codebook[i][j] = new int[size];
                    if(_fullread(source, temp) != size)
                        throw new IllegalActionException("Error reading " +
                                "codebook file!");
                    for(x = 0; x < size; x++)
                        _codebook[i][j][x] = temp[x];
                }
                
                temp = new byte[65536];
                // read in the lookup table.
                if(_fullread(source, temp) != 65536)
                    throw new IllegalActionException("Error reading " +
                            "codebook file!");
                for(x = 0; x < 65536; x++)
                    _lookup_table[i][x] = temp[x] & 255;
            }
        }
        catch (Exception e) {
            throw new IllegalActionException(e.getMessage());
        }
        finally {
            if(source != null) {
                try {
                    source.close(); 
                }
                catch (IOException e) {
                }
            }
        }
    }

    public void setBaseURL(URL baseurl) {
        _baseurl = baseurl;
    }

    int ipbuf_encodep1[][] = new int[8][8];
    int ipbuf_encodep2[][] = new int[8][8];
    
    int _stages(int len) {
        int x = 0;
        if(len < 2) throw new RuntimeException(
                "HTVQEncode: vector length of " + len + 
                "must be greater than 1");
        while(len > 2) { len = len >> 1; x++;}
        return x;
    }

    int _encode(int p[], int len) {
        int[][] p5, p4, p3, p2, p1, p0;
        int numstages;
	int stage = 0;
        int ip;
        int dest_index;

        numstages = _stages(len);
        
	if(numstages>4) 
            throw new RuntimeException(
                    "HTVQEncode: _encode: imagepart too large... exiting");
        p5 = ipbuf_encodep1;
        p4 = ipbuf_encodep2;
        p3 = ipbuf_encodep1;
        p2 = ipbuf_encodep2;
        p1 = ipbuf_encodep1;
        p0 = ipbuf_encodep2;

	switch(numstages) {
        case 4:
            System.arraycopy(p, 0, p5[0], 0, 8);
            System.arraycopy(p, 8, p5[1], 0, 8);
            System.arraycopy(p, 16, p5[2], 0, 8);
            System.arraycopy(p, 24, p5[3], 0, 8);
            break;
        case 3:
            System.arraycopy(p, 0, p4[0], 0, 4);
            System.arraycopy(p, 4, p4[1], 0, 4);
            System.arraycopy(p, 8, p4[2], 0, 4);
            System.arraycopy(p, 12, p4[3], 0, 4);            
            break;
        case 2:            
            p3[0][0] = p[0];
            p3[0][1] = p[1];
            p3[0][2] = p[2];
            p3[0][3] = p[3];
            p3[1][0] = p[4];
            p3[1][1] = p[5];
            p3[1][2] = p[6];
            p3[1][3] = p[7];
            break;
        case 1:
            p2[0][0] = p[0];
            p2[0][1] = p[1];
            p2[1][0] = p[2];
            p2[1][1] = p[3];
            break;
        case 0:
            p1[0][0] = p[0];
            p1[0][1] = p[1];
            break;
	}	
	switch(numstages) {
        case 4:
            //XSIZE = 8, YSIZE = 4
            ip = ((p5[0][0] & 255) << 8) + (p5[0][1] & 255);
            p4[0][0] = _lookup_table[stage][ip];
            ip = ((p5[0][2] & 255) << 8) + (p5[0][3] & 255);
            p4[1][0] = _lookup_table[stage][ip];
            ip = ((p5[0][4] & 255) << 8) + (p5[0][5] & 255);
            p4[2][0] = _lookup_table[stage][ip];
            ip = ((p5[0][6] & 255) << 8) + (p5[0][7] & 255);
            p4[3][0] = _lookup_table[stage][ip];

            ip = ((p5[1][0] & 255) << 8) + (p5[1][1] & 255);
            p4[0][1] = _lookup_table[stage][ip];
            ip = ((p5[1][2] & 255) << 8) + (p5[1][3] & 255);
            p4[1][1] = _lookup_table[stage][ip];
            ip = ((p5[1][4] & 255) << 8) + (p5[1][5] & 255);
            p4[2][1] = _lookup_table[stage][ip];
            ip = ((p5[1][6] & 255) << 8) + (p5[1][7] & 255);
            p4[3][1] = _lookup_table[stage][ip];

            ip = ((p5[2][0] & 255) << 8) + (p5[2][1] & 255);
            p4[0][2] = _lookup_table[stage][ip];
            ip = ((p5[2][2] & 255) << 8) + (p5[2][3] & 255);
            p4[1][2] = _lookup_table[stage][ip];
            ip = ((p5[2][4] & 255) << 8) + (p5[2][5] & 255);
            p4[2][2] = _lookup_table[stage][ip];
            ip = ((p5[2][6] & 255) << 8) + (p5[2][7] & 255);
            p4[3][2] = _lookup_table[stage][ip];

            ip = ((p5[3][0] & 255) << 8) + (p5[3][1] & 255);
             p4[0][3] = _lookup_table[stage][ip];
            ip = ((p5[3][2] & 255) << 8) + (p5[3][2] & 255);
             p4[1][3] = _lookup_table[stage][ip];
            ip = ((p5[3][4] & 255) << 8) + (p5[3][4] & 255);
             p4[2][3] = _lookup_table[stage][ip];
            ip = ((p5[3][6] & 255) << 8) + (p5[3][6] & 255);
             p4[3][3] = _lookup_table[stage][ip];
            stage++;
        case 3:
            //XSIZE = 4, YSIZE = 4
            ip = ((p4[0][1] & 255) << 8) + (p4[0][0] & 255);
            p3[0][0] = _lookup_table[stage][ip];
            ip = ((p4[0][3] & 255) << 8) + (p4[0][2] & 255);
            p3[1][0] = _lookup_table[stage][ip];

            ip = ((p4[1][1] & 255) << 8) + (p4[1][0] & 255);
            p3[0][1] = _lookup_table[stage][ip];
            ip = ((p4[1][3] & 255) << 8) + (p4[1][2] & 255);
            p3[1][1] = _lookup_table[stage][ip];

            ip = ((p4[2][1] & 255) << 8) + (p4[2][0] & 255);
            p3[0][2] = _lookup_table[stage][ip];
            ip = ((p4[2][3] & 255) << 8) + (p4[2][2] & 255);
            p3[1][2] = _lookup_table[stage][ip];

            ip = ((p4[3][1] & 255) << 8) + (p4[3][0] & 255);
             p3[0][3] = _lookup_table[stage][ip];
            ip = ((p4[3][3] & 255) << 8) + (p4[3][2] & 255);
             p3[1][3] = _lookup_table[stage][ip];
            stage++;
        case 2:
            //XSIZE = 4, YSIZE = 2
            ip = ((p3[0][1] & 255) << 8) + (p3[0][0] & 255);
            p2[0][0] = _lookup_table[stage][ip];
            ip = ((p3[0][3] & 255) << 8) + (p3[0][2] & 255);
            p2[1][0] = _lookup_table[stage][ip];
           
            ip = ((p3[1][1] & 255) << 8) + (p3[1][0] & 255);
            p2[0][1] = _lookup_table[stage][ip];
            ip = ((p3[1][3] & 255) << 8) + (p3[1][2] & 255);
            p2[1][1] = _lookup_table[stage][ip];
            stage++;
        case 1:
            //XSIZE = 2, YSIZE = 2
            ip = ((p2[0][1] & 255) << 8) + (p2[0][0] & 255);
            p1[0][0] = _lookup_table[stage][ip];
            ip = ((p2[1][1] & 255) << 8) + (p2[1][0] & 255);
            p1[0][1] = _lookup_table[stage][ip];
            stage++;
            
        case 0:
            //XSIZE = 2, YSIZE = 1
            ip = ((p1[0][1] & 255) << 8) + (p1[0][0] & 255);
            p0[0][0] = _lookup_table[stage][ip];
            stage++;
  	}
        
        return p0[0][0];
    }

    long _distortion(int a[], int b[], int len) {
        long c, d = 0;
        int i;
        for(i = 0;i<len;i++)
        {
                c = ((a[i] & 255) - (b[i] & 255));
                d += c * c;
        }
        return d;
    }
    
    int _fullread(InputStream s, byte b[]) throws IOException {
        int len = 0;
        int remaining = b.length;
        int bytesread = 0;
        while(remaining > 0) {
            bytesread = s.read(b, len, remaining);
            if(bytesread == -1) throw new IOException(
                    "HTVQEncode: _fullread: Unexpected EOF");
            remaining -= bytesread;
            len += bytesread;
        }
        return len;
    }

    private int _codebook[][][] = new int[6][256][];
    private int _lookup_table[][] = new int[6][65536];
    private IntToken _codewords[];
    private IntMatrixToken _tokens[];
    private int _part[];
    private int _xframesize;
    private int _yframesize;
    private int _xpartsize;
    private int _ypartsize;
    private URL _baseurl;
}



