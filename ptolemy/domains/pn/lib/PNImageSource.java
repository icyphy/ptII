/* Reads an audio file and divides the audio data into blocks.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
 
                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
*/

package ptolemy.domains.pn.lib;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.media.*;
import java.io.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNAudioSource
/** 
Reads an image file (int the ASCII PBM format) and creates a matrix token

@author Yuhong Xiong, Mudit Goel
@(#)PNAudioSource.java	1.22 09/13/98
*/

public class PNImageSource extends AtomicActor {
    
    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public PNImageSource(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        //_input = new IOPort(this, "input", true, false);
        //_input.ISINPUT = false;
        _output = new IOPort(this, "output", false, true);
        //_output.ISINPUT = false;
	_filename = new Parameter(this, "Image_file", new StringToken());
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void initialize() {
	String filename =((StringToken)_filename.getToken()).stringValue();
	try {
            //_file = new BufferedReader(new FileReader(filename));
            FileInputStream fis = new FileInputStream(filename);
            _file = new DataInputStream(fis);
        } catch (FileNotFoundException e) {
        System.out.println("Error: In PNAudioSource: file \"" +
                filename + "\" not found!");
        }
    }

    /** Reads one block of data from file and writes it to output port.
     *  Assuming data in 16 bit, higher byte first format.
     */
    public void fire() throws IllegalActionException {
        try {
            String dataread = _file.readLine();
            if (!dataread.equals("P1")) {
                System.out.println("First line is not P1. It is "+dataread);
                throw new IllegalActionException("File format of "+
                        ((StringToken)_filename.getToken()).stringValue()+
                        " not recognised");
            } else {
                dataread = _file.readLine();
                while (dataread.startsWith("#")) {
                    dataread = _file.readLine();
                }
                System.out.println("Read "+dataread);
                DataInputStream dim = 
                    new DataInputStream(new StringBufferInputStream(dataread));
                StreamTokenizer datastr = new StreamTokenizer(dim);
                int datarow = 0;
                int datacol = 0;
                if (datastr.nextToken() == StreamTokenizer.TT_NUMBER) {
                    datacol = (int)datastr.nval;
                }
                if (datastr.nextToken() == StreamTokenizer.TT_NUMBER) {
                    datarow = (int)datastr.nval;
                }
                System.out.println("Rows ="+datarow+" and col = "+datacol);
                int[][] image = new int[datarow][datacol];
                datastr = new StreamTokenizer(_file);
                for (int i=0; i<datarow; i++) {
                    for (int j=0; j<datacol; j++) {
                        while (datastr.nextToken() != 
                                StreamTokenizer.TT_NUMBER);
                        image[i][j] = (int)datastr.nval;
                    }
                }

                //Converting it to a format that can use media.Picture
                //Note that it is currently is a hack for BW pbm images alone.
                //int[] display = new int[datarow*datacol];
                //int alpha = 0;
                //for (int i=0; i<datarow; i++) {
                    //for (int j=0; j<datacol; j++) {
                        //int col = image[i][j]*255;
                        //display[i*datacol+j] = 
                              //(alpha << 24) | (col << 16) | (col << 8) | col;
                        //}
                    //}
                //Picture pic = new Picture(datarow, datacol);
                //pic.setImage(display);
                //pic.displayImage();

                //System.out.println("i = "+i+" j ="+j);
                IntMatrixToken dataToken = new IntMatrixToken(image);
                _output.broadcast(dataToken);
                try {
                    _file.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("PNImageSource Can't readInt.");
        }

    
    }

    public boolean postfire() { 
        return false; 
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    //    private FileInputStream _file;
    private Parameter _filename;
    //private BufferedReader _file;
    private DataInputStream _file;
    //private int _blockSize;
    //private byte[] _buffer;
    private IOPort _output;
    private IOPort _input;
}




