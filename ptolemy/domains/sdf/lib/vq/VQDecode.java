/* This actor decompresses a vector quantized signal.
@Copyright (c) 1998-2000 The Regents of the University of California.
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
//// VQDecode
/**
This actor decompresses a vector quantized signal.   This operation is simply
a table lookup into the codebook.

@see HTVQEncode

@author Steve Neuendorffer
@version $Id$
*/
// FIXME This should be generalized to a Table-lookup actor.
public final class VQDecode extends SDFAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public VQDecode(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        input = (SDFIOPort) newPort("input");
        input.setInput(true);
        input.setTypeEquals(BaseType.INT);

        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.INT_MATRIX);

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

    /** The number of blocks to be decoded during each firing.
     *  The default value is one, which will always work, but using a higher
     *  number (such as the number of blocks in a frame) will speed things up.
     *  This should contain an integer.
     */
    public Parameter blockCount;

    /** The width, in integer pixels, of the block to decode. */
    public Parameter blockWidth;

    /** The width, in integer pixels, of the block to decode. */
    public Parameter blockHeight;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        VQDecode newobj = (VQDecode)(super.clone(ws));
        newobj.input = (SDFIOPort)newobj.getPort("input");
        newobj.output = (SDFIOPort)newobj.getPort("output");
        newobj.codeBook = (Parameter)newobj.getAttribute("codeBook");
        newobj.blockCount = (Parameter)newobj.getAttribute("blockCount");
        newobj.blockWidth = (Parameter)newobj.getAttribute("blockWidth");
        newobj.blockHeight = (Parameter)newobj.getAttribute("blockHeight");
        return newobj;
    }

    /**
     * Fire this actor.
     * Consume a number of tokens on the input port, each representing a
     * VQ codeword.  Index into the appropriate codebook given by the
     * blockWidth and blockHeight parameters to find the decoded vector for
     * each codeword.  Output an IntMatrixToken representing each decoded
     * vector on the output port.
     * @exception IllegalActionException If the input or output are not
     * connected
     * @exception ArrayOutOfBoundsException If the input codewords are
     * not between 0 and 255.
     */
    public void fire() throws IllegalActionException {
        int j;
        int stage = _stages(_blockWidth * _blockHeight);
        input.getArray(0, _codewords);

        for(j = 0; j < _blockCount; j++) {
            _blocks[j] =
                new IntMatrixToken(_codebook[stage][_codewords[j].intValue()],
                        _blockHeight, _blockWidth);
        }

        output.sendArray(0, _blocks);
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

        String filename = ((StringToken)codeBook.getToken()).stringValue();
        try {
            if (filename != null) {
                if(_baseurl != null) {
                    try {
                        URL dataurl = new URL(_baseurl, filename);
                        _debug("VQDecode: codebook = " + dataurl);
                        source = dataurl.openStream();
                    } catch (MalformedURLException e) {
                        System.err.println(e.toString());
                    } catch (FileNotFoundException e) {
                        System.err.println("File not found: " + e);
                    } catch (IOException e) {
			throw new IllegalActionException(
                                "Error reading" +
                                " input file: " + e.getMessage());
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
                    _codebook[i][j] = new int[size];
                    if(_fullread(source, temp) != size)
                        throw new IllegalActionException("Error reading " +
                                "codebook file!");
                    for(x = 0; x < size; x++)
                        _codebook[i][j][x] = temp[x] & 255;
                }

		// skip over the lookup tables.

                temp = new byte[65536];
                // read in the lookup table.
                if(_fullread(source, temp) != 65536)
                    throw new IllegalActionException("Error reading " +
                            "codebook file!");
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

    /**
     * Set the url representing the root classpath from which to load files.
     */
    public void setBaseURL(URL baseurl) {
        _baseurl = baseurl;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

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

    private int _codebook[][][] = new int[6][256][];
    private IntToken _codewords[];
    private IntMatrixToken _blocks[];

    private int _blockCount;
    private int _blockWidth;
    private int _blockHeight;
    private URL _baseurl;
}
