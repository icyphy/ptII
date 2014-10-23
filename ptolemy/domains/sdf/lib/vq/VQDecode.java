/* This actor decompresses a vector quantized signal.
 @Copyright (c) 1998-2014 The Regents of the University of California.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.IntegerMatrixMath;
import ptolemy.util.FileUtilities;

///////////////////////////////////////////////////////////////////
//// VQDecode

/**
 This actor decompresses a vector quantized signal.   This operation is simply
 a table lookup into the codebook.

 @see HTVQEncode

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red
 */

// FIXME This should be generalized to a Table-lookup actor.
public class VQDecode extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public VQDecode(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.INT);

        output.setTypeEquals(BaseType.INT_MATRIX);

        codeBook = new Parameter(this, "codeBook", new StringToken(
                "/ptolemy/domains/sdf" + "/lib/vq/data/usc_hvq_s5.dat"));
        codeBook.setTypeEquals(BaseType.STRING);

        blockCount = new Parameter(this, "blockCount", new IntToken("1"));
        blockCount.setTypeEquals(BaseType.INT);

        blockWidth = new Parameter(this, "blockWidth", new IntToken("4"));
        blockWidth.setTypeEquals(BaseType.INT);

        blockHeight = new Parameter(this, "blockHeight", new IntToken("2"));
        blockHeight.setTypeEquals(BaseType.INT);

        input_tokenConsumptionRate = new Parameter(input,
                "tokenConsumptionRate");
        input_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate.setExpression("blockCount");

        output_tokenProductionRate = new Parameter(output,
                "tokenProductionRate");
        output_tokenProductionRate.setTypeEquals(BaseType.INT);
        output_tokenProductionRate.setExpression("blockCount");
    }

    ///////////////////////////////////////////////////////////////////
    ////                      ports and parameters                 ////

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

    /** The input rate. */
    public Parameter input_tokenConsumptionRate;

    /** The output rate. */
    public Parameter output_tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        VQDecode newObject = (VQDecode) super.clone(workspace);
        newObject._codebook = new int[6][256][][];
        return newObject;
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
     * @exception ArrayIndexOutOfBoundsException If the input codewords are
     * not between 0 and 255.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int j;
        int stage = _stages(_blockWidth * _blockHeight);
        _codewords = input.get(0, _blockCount);

        for (j = 0; j < _blockCount; j++) {
            _blocks[j] = new IntMatrixToken(
                    _codebook[stage][((IntToken) _codewords[j]).intValue()]);
        }

        output.send(0, _blocks, _blocks.length);
    }

    /**
     * Initialize this actor.
     * Load the codebooks and lookup tables from the file given by the
     * parameter "codeBook".
     * @exception IllegalActionException If the parameters do not have
     * legal values, or the codebook file cannot be read.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        InputStream source = null;

        _blockCount = ((IntToken) blockCount.getToken()).intValue();
        _blockWidth = ((IntToken) blockWidth.getToken()).intValue();
        _blockHeight = ((IntToken) blockHeight.getToken()).intValue();

        _codewords = new ptolemy.data.Token[_blockCount];
        _blocks = new IntMatrixToken[_blockCount];

        String filename = ((StringToken) codeBook.getToken()).stringValue();

        try {
            if (filename != null) {
                try {
                    URL dataurl = FileUtilities.nameToURL(filename, null,
                            getClass().getClassLoader());
                    _debug("VQDecode: codebook = " + dataurl);
                    source = dataurl.openStream();
                } catch (MalformedURLException e) {
                    System.err.println(e.toString());
                } catch (FileNotFoundException e) {
                    System.err.println("File not found: " + e);
                } catch (IOException e) {
                    throw new IllegalActionException("Error reading"
                            + " input file: " + e.getMessage());
                }
            }

            int i;
            int j;
            int x;
            int size = 1;
            byte[] temp;
            int[] intTemp;
            int rows = 1;
            int columns = 1;

            for (i = 0; i < 5; i++) {
                size = size * 2;

                if (i % 2 == 0) {
                    columns = columns * 2;
                } else {
                    rows = rows * 2;
                }

                temp = new byte[size];
                intTemp = new int[size];

                for (j = 0; j < 256; j++) {
                    if (_fullRead(source, temp) != size) {
                        throw new IllegalActionException("Error reading "
                                + "codebook file!");
                    }

                    for (x = 0; x < size; x++) {
                        intTemp[x] = temp[x] & 255;
                    }

                    _codebook[i][j] = IntegerMatrixMath.toMatrixFromArray(
                            intTemp, rows, columns);
                }

                // skip over the lookup tables.
                temp = new byte[65536];

                // read in the lookup table.
                if (_fullRead(source, temp) != 65536) {
                    throw new IllegalActionException("Error reading "
                            + "codebook file!");
                }
            }
        } catch (Exception e) {
            throw new IllegalActionException(e.getMessage());
        } finally {
            if (source != null) {
                try {
                    source.close();
                } catch (IOException e) {
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private int _fullRead(InputStream s, byte[] b) throws IOException {
        int length = 0;
        int remaining = b.length;
        int bytesRead = 0;

        while (remaining > 0) {
            bytesRead = s.read(b, length, remaining);

            if (bytesRead == -1) {
                throw new IOException("Unexpected EOF");
            }

            remaining -= bytesRead;
            length += bytesRead;
        }

        return length;
    }

    /** Given a vector of the given length, compute the codebook stage
     *  appropriate.  Basically, compute log base 2 of length, assuming
     *  length is a power of 2.
     */
    private int _stages(int length) {
        int x = 0;

        if (length < 2) {
            throw new RuntimeException("Vector length of " + length
                    + "must be greater than 1");
        }

        while (length > 2) {
            length = length >> 1;
                x++;
        }

        return x;
    }

    private int[][][][] _codebook = new int[6][256][][];

    private ptolemy.data.Token[] _codewords;

    private IntMatrixToken[] _blocks;

    private int _blockCount;

    private int _blockWidth;

    private int _blockHeight;
}
