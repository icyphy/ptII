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
@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
*/
package ptolemy.domains.sdf.lib.vq;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;
import java.io.*;
import java.net.*;

//////////////////////////////////////////////////////////////////////////
//// HTVQEncode
/**
This actor encodes a matrix using Hierarchical Table-Lookup Vector
Quantization.   The matrix must be of dimensions that are amenable to this
method. (i.e. 2x1, 2x2, 4x2, 4x4, etc.) Instead of performing a
full-search vector quantization during execution, all the optimal encoding
vectors are calculated before hand and stored in a lookup table. (This is
known as Table-lookup Vector Quantization).  However, for large vector sizes
the lookup tables are unmanageably large.   This actor approximates a
full search VQ by storing the lookup tables hierarchically.
The encoding is broken up into stages, and at each stage a number of 2x1
table lookup VQs are performed. For example,
starting with a 4x2 vector in the first stage, codebook 0 (which operates
on raw pixels) is used 4 times, resulting in a 2x2 vector of codewords.
In the second stage, codebook 1 is used twice, resulting in a 2x1 vector.
Lastly, a single 2x1 VQ using codebook 2 (which operates on codewords
representing 2x2 vectors) returns a single codeword for the 4x2 vector.
<p>
The input is an IntMatrixToken corresponding to the block to be encoded.
The values in this matrix are assumed to be between 0 and 255.  The output
is an IntToken with value between 0 and 255.  Integers are used here because
of the minimal byte support in Ptolemy or JAVA.
The size of the input matrix should be the same as the parameters blockHeight
and blockWidth.
<p>
The codebook is specified as a binary file that will be read during
initialization.  This file actually contains five sets of codebooks and
lookups tables.  The first set is for 2x1 blocks, the second is for 2x2
blocks, etc.  (Thus the supplied codebook is only sufficient for block sizes
up to 8x4 pixels.) In each set, the codebook precedes the lookup-tables.
The codebook consists of all 256 codevectors, row scanned from top to bottom.
The lookup table consists of 64K entries (one for each pair of codewords from
the previous stage).  Each entry in the lookup table is an 8-bit codeword.
<p>
<pre>
Stage 0: 2x1 blocksize
codebook = 256 blocks x 2 bytes = 512 bytes
lookup tables = 65536 entries x 1 byte = 65536 bytes
Stage 1: 2x2 blocksize
codebook = 256 blocks x 4 bytes = 1024 bytes
lookup tables = 65536 entries x 1 byte = 65536 bytes
Stage 2: 4x2 blocksize
codebook = 256 blocks x 8 bytes = 2048 bytes
lookup tables = 65536 entries x 1 byte = 65536 bytes
Stage 3: 4x4 blocksize
codebook = 256 blocks x 16 bytes = 4096 bytes
lookup tables = 65536 entries x 1 byte = 65536 bytes
Stage 4: 8x4 blocksize
codebook = 256 blocks x 32 bytes = 8192 bytes
lookup tables = 65536 entries x 1 byte = 65536 bytes
</pre>
<br>
The supplied codebook was trained using images from the
USC image archive and is suitable for most general applications.
<br>
For more information here are some interesting references: <br>
A. Gersho and R. M. Gray, <i>Vector Quantization and Signal Compression</i>.
Kluwer Academic Publishers, Boston, 1992.  <br>
P. C. Chang, J. May, R. M. Gray, "Hierarchical Vector Quantizers with
Table Lookup Encoders," <i> International Conference on Acoustics Speech
and Signal Processing</i>, pp. 1452-1455, 1985. <br>
M. Vishwanath and P. Chou, "An Efficient Algorithm for Hierarchical
Compression of Video," <i>International Conference on Image Processing</i>,
vol. 3, pp. 275-279, Nov. 1994. <br>

@author Steve Neuendorffer
@version $Id$
*/

public final class HTVQEncode extends SDFAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public HTVQEncode(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        input = (SDFIOPort) newPort("input");
        input.setInput(true);
        input.setTypeEquals(BaseType.INT_MATRIX);

	output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.INT);

        codeBook = new Parameter(this, "codeBook",
                new StringToken("ptolemy/domains/sdf" +
                        "/lib/vq/data/usc_hvq_s5.dat"));
        blockCount = new Parameter(this, "blockCount", new IntToken("1"));
        _blockCount = ((IntToken)blockCount.getToken()).intValue();
        output.setTokenProductionRate(_blockCount);
        input.setTokenConsumptionRate(_blockCount);
        blockWidth =
            new Parameter(this, "blockWidth", new IntToken("4"));
        blockHeight =
            new Parameter(this, "blockHeight", new IntToken("2"));
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public SDFIOPort input;

    /** The output port. */
    public SDFIOPort output;

    /** A Parameter of type String, giving the location of the codebook data
     *  file relative to the root classpath.
     */
    public Parameter codeBook;

    /** The number of blocks to be encoded during each firing.
     *  The default value is one, which will always work, but using a higher
     *  number (such as the number of blocks in a frame) will speed things up.
     */
    public Parameter blockCount;

    /** The width, in pixels, of the block to encode. */
    public Parameter blockWidth;

    /** The width, in pixels, of the block to encode. */
    public Parameter blockHeight;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            HTVQEncode newobj = (HTVQEncode)(super.clone(ws));
            newobj.input = (SDFIOPort)newobj.getPort("input");
            newobj.output = (SDFIOPort)newobj.getPort("output");
            newobj.codeBook = (Parameter)newobj.getAttribute("codeBook");
            newobj.blockCount = (Parameter)newobj.getAttribute("blockCount");
            newobj.blockWidth = (Parameter)newobj.getAttribute("blockWidth");
            newobj.blockHeight = (Parameter)newobj.getAttribute("blockHeight");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /**
     * Fire this actor.
     * Consume a codeword on the input, and perform Vector Quantization using
     * Hierarchical Table-Lookup Vector Quantization.  Send the computed
     * codeword on the output.
     * @exception IllegalActionException if a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        int j;
        input.getArray(0, _blocks);

        for(j = 0; j < _blockCount; j++) {
            _codewords[j] = new IntToken(
                    _encode(_blocks[j].intArray(),
                            _blockWidth * _blockHeight));
	}

        output.sendArray(0, _codewords);
    }

    /**
     * Initialize this actor.
     * Load the codebooks and lookup tables from the file given by the
     * parameter "codeBook".
     * @exception IllegalActionException If the parameters do not have
     * legal values, or the codebook file cannot be read.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        InputStream source = null;

        _blockCount = ((IntToken)blockCount.getToken()).intValue();
        input.setTokenConsumptionRate(_blockCount);
        output.setTokenProductionRate(_blockCount);

        _blockWidth = ((IntToken)blockWidth.getToken()).intValue();
        _blockHeight = ((IntToken)blockHeight.getToken()).intValue();

        _codewords =  new IntToken[_blockCount];
        _blocks = new IntMatrixToken[_blockCount];

        String filename = ((StringToken)codeBook.getToken()).toString();
        try {
            if (filename != null) {
                if(_baseurl != null) {
                    try {
                        URL dataurl = new URL(_baseurl, filename);
                        _debug("HTVQEncode: codebook = " + dataurl);
                        source = dataurl.openStream();
                    } catch (MalformedURLException e) {
                        System.err.println(e.toString());
                    } catch (FileNotFoundException e) {
                        System.err.println("HTVQEncode: " +
                                "file not found: " +e);
                    } catch (IOException e) {
                        System.err.println(
                                "HTVQEncode: error reading"+
                                " input file: " +e);
                    }
                } else {
                    File sourcefile = new File(filename);
                    if(!sourcefile.exists() || !sourcefile.isFile())
                        throw new IllegalActionException("Codebook file " +
                                filename + " does not exist!");
                    if(!sourcefile.canRead())
                        throw new IllegalActionException("Codebook file " +
                                filename + " is unreadable!");
                    source = new FileInputStream(sourcefile);
                }
            }

            int i, j, y, x, size = 1;
            byte temp[];
            for(i = 0; i < 5; i++) {
                size = size * 2;
                temp = new byte[size];
                for(j = 0; j < 256; j++) {
                    _codeBook[i][j] = new int[size];
                    if(_fullread(source, temp) != size)
                        throw new IllegalActionException("Error reading " +
                                "codebook file!");
                    for(x = 0; x < size; x++)
                        _codeBook[i][j][x] = temp[x] & 255;
                }

                temp = new byte[65536];
                // read in the lookup table.
                if(_fullread(source, temp) != 65536)
                    throw new IllegalActionException("Error reading " +
                            "codebook file!");
                for(x = 0; x < 65536; x++)
                    _lookupTable[i][x] = temp[x] & 255;
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
                    // ARgh...  can't we do anything right?
                }
            }
        }
    }

    /**
     * Set the base URL from which this actor was loaded.  This actor should
     * load any data that it needs relative to this URL.
     */
    public void setBaseURL(URL baseurl) {
        _baseurl = baseurl;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    private int _encode(int p[], int len) {
        int[][] p5, p4, p3, p2, p1, p0;
        int numstages;
	int stage = 0;
        int ip;
        int dest_index;

        numstages = _stages(len);

	if(numstages>4)
            throw new RuntimeException(
                    "imagepart too large... exiting");
        p5 = ipbuf_encodep1;
        p4 = ipbuf_encodep2;
        p3 = ipbuf_encodep1;
        p2 = ipbuf_encodep2;
        p1 = ipbuf_encodep1;
        p0 = ipbuf_encodep2;

	switch(numstages) {
        case 4:
            // System.arraycopy is faster for large vectors
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
            p4[0][0] = _lookupTable[stage][ip];
            ip = ((p5[0][2] & 255) << 8) + (p5[0][3] & 255);
            p4[1][0] = _lookupTable[stage][ip];
            ip = ((p5[0][4] & 255) << 8) + (p5[0][5] & 255);
            p4[2][0] = _lookupTable[stage][ip];
            ip = ((p5[0][6] & 255) << 8) + (p5[0][7] & 255);
            p4[3][0] = _lookupTable[stage][ip];

            ip = ((p5[1][0] & 255) << 8) + (p5[1][1] & 255);
            p4[0][1] = _lookupTable[stage][ip];
            ip = ((p5[1][2] & 255) << 8) + (p5[1][3] & 255);
            p4[1][1] = _lookupTable[stage][ip];
            ip = ((p5[1][4] & 255) << 8) + (p5[1][5] & 255);
            p4[2][1] = _lookupTable[stage][ip];
            ip = ((p5[1][6] & 255) << 8) + (p5[1][7] & 255);
            p4[3][1] = _lookupTable[stage][ip];

            ip = ((p5[2][0] & 255) << 8) + (p5[2][1] & 255);
            p4[0][2] = _lookupTable[stage][ip];
            ip = ((p5[2][2] & 255) << 8) + (p5[2][3] & 255);
            p4[1][2] = _lookupTable[stage][ip];
            ip = ((p5[2][4] & 255) << 8) + (p5[2][5] & 255);
            p4[2][2] = _lookupTable[stage][ip];
            ip = ((p5[2][6] & 255) << 8) + (p5[2][7] & 255);
            p4[3][2] = _lookupTable[stage][ip];

            ip = ((p5[3][0] & 255) << 8) + (p5[3][1] & 255);
            p4[0][3] = _lookupTable[stage][ip];
            ip = ((p5[3][2] & 255) << 8) + (p5[3][2] & 255);
            p4[1][3] = _lookupTable[stage][ip];
            ip = ((p5[3][4] & 255) << 8) + (p5[3][4] & 255);
            p4[2][3] = _lookupTable[stage][ip];
            ip = ((p5[3][6] & 255) << 8) + (p5[3][6] & 255);
            p4[3][3] = _lookupTable[stage][ip];
            stage++;
        case 3:
            //XSIZE = 4, YSIZE = 4
            ip = ((p4[0][1] & 255) << 8) + (p4[0][0] & 255);
            p3[0][0] = _lookupTable[stage][ip];
            ip = ((p4[0][3] & 255) << 8) + (p4[0][2] & 255);
            p3[1][0] = _lookupTable[stage][ip];

            ip = ((p4[1][1] & 255) << 8) + (p4[1][0] & 255);
            p3[0][1] = _lookupTable[stage][ip];
            ip = ((p4[1][3] & 255) << 8) + (p4[1][2] & 255);
            p3[1][1] = _lookupTable[stage][ip];

            ip = ((p4[2][1] & 255) << 8) + (p4[2][0] & 255);
            p3[0][2] = _lookupTable[stage][ip];
            ip = ((p4[2][3] & 255) << 8) + (p4[2][2] & 255);
            p3[1][2] = _lookupTable[stage][ip];

            ip = ((p4[3][1] & 255) << 8) + (p4[3][0] & 255);
            p3[0][3] = _lookupTable[stage][ip];
            ip = ((p4[3][3] & 255) << 8) + (p4[3][2] & 255);
            p3[1][3] = _lookupTable[stage][ip];
            stage++;
        case 2:
            //XSIZE = 4, YSIZE = 2
            ip = ((p3[0][1] & 255) << 8) + (p3[0][0] & 255);
            p2[0][0] = _lookupTable[stage][ip];
            ip = ((p3[0][3] & 255) << 8) + (p3[0][2] & 255);
            p2[1][0] = _lookupTable[stage][ip];

            ip = ((p3[1][1] & 255) << 8) + (p3[1][0] & 255);
            p2[0][1] = _lookupTable[stage][ip];
            ip = ((p3[1][3] & 255) << 8) + (p3[1][2] & 255);
            p2[1][1] = _lookupTable[stage][ip];
            stage++;
        case 1:
            //XSIZE = 2, YSIZE = 2
            ip = ((p2[0][1] & 255) << 8) + (p2[0][0] & 255);
            p1[0][0] = _lookupTable[stage][ip];
            ip = ((p2[1][1] & 255) << 8) + (p2[1][0] & 255);
            p1[0][1] = _lookupTable[stage][ip];
            stage++;

        case 0:
            //XSIZE = 2, YSIZE = 1
            ip = ((p1[0][1] & 255) << 8) + (p1[0][0] & 255);
            p0[0][0] = _lookupTable[stage][ip];
            stage++;
  	}

        return p0[0][0];
    }

    private int _fullread(InputStream s, byte b[]) throws IOException {
        int len = 0;
        int remaining = b.length;
        int bytesread = 0;
        while(remaining > 0) {
            bytesread = s.read(b, len, remaining);
            if(bytesread == -1) throw new IOException(
                    "Unexpected EOF");
            remaining -= bytesread;
            len += bytesread;
        }
        return len;
    }

    /** Given a vector of the given length, compute the codebook stage
     *  appropriate.  Basically, compute log base 2 of len, assuming
     *  len is a power of 2.
     */
    private int _stages(int len) {
        int x = 0;
        if(len < 2) throw new RuntimeException(
                "Vector length of " + len +
                "must be greater than 1");
        while(len > 2) { len = len >> 1; x++;}
        return x;
    }

    private int ipbuf_encodep1[][] = new int[8][8];
    private int ipbuf_encodep2[][] = new int[8][8];
    private int _codeBook[][][] = new int[6][256][];
    private int _lookupTable[][] = new int[6][65536];
    private IntToken _codewords[];
    private IntMatrixToken _blocks[];

    private int _blockCount;
    private int _blockWidth;
    private int _blockHeight;
    private URL _baseurl;
}
