/* Class providing additional functions to ptolemyII expression language.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;
import ptolemy.data.StringToken;
import java.io.*;

import java.util.*;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.Token;
import ptolemy.data.MatrixToken;
import ptolemy.data.DoubleMatrixToken;

//////////////////////////////////////////////////////////////////////////
//// UtilityFunctions
/**
Class providing additional functions to ptolemyII expression language.
<p>
Currently this class only contains two methods, env() and readFile(),
and even for these there are only trivial implementations.
<p>
FIXME: finish this class.
@author  Neil Smyth, Christopher Hyland, Bart Kienhuis
@version $Id$
@see PtParser
*/
public class UtilityFunctions {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the referred environment variable. An empty string
     *  is returned if the argument environment variable does not exist.
     * @param envName String representing the name of environment
     *   variable we want to obtain.
     * @return StringToken containing the string referred to by the
     *   environment variable.
     */
    public static StringToken env(String envName) {
        return new StringToken(System.getProperty(envName));
    }

    /** Get the string text contained in the specified file. For
     *  now this just looks in the directory where the parser
     *  is located, but will eventually (hopefully!) be able
     *  to use environment variable, user names etc. in
     *  creating a file path. An empty string
     *  is returned if the specified file could not be located.
     *  FIXME: what do with format of file?, e.g. if file is
     *  spread over many lines should we remove the newlines
     *  and make one long one line string? Also this currently
     *  only looks in the working directory.
     * @param filename The file we want to read the text from.
     * @return StringToken containing the text contained in
     *   the specified file.
     * @exception IllegalActionException If for the given filename
     *   a file cannot be opened.
     * */
    public static StringToken readFile(String filename)             
            throws IllegalActionException {

                // temporary hack, need to work out way to obtain the path.
                String curDir = System.getProperty("user.dir");

                //System.out.println("Directory is " + curDir);
                File fileT = new File(curDir, filename);
                //System.out.println("Trying to open file: " + fileT.toString());
                BufferedReader fin = null;
                String line;
                String result = "";
                String newline = System.getProperty("line.separator");
                try {
                    if (fileT.exists()) {
                        fin = new BufferedReader(new FileReader(fileT));
                        while (true) {
                            try {
                                line = fin.readLine();
                            } catch (IOException e) {
                                break;
                            }

                            if (line == null) break;
                            result += line + newline;
                            //System.out.println("read in line: \"" +
                            //   line + newline + "\"");
                        }
                    }
                } catch (FileNotFoundException e) {
                    // what should we do here?
                    throw new IllegalActionException("File not found:\n" + 
                            e.toString() );
                }
                //System.out.println("Contents of file are: " + result);
                return new StringToken(result);
    }

    /** Read a file that contains a Matrix in Matlab notation. The
     *  file matrix is return by the Matrix parser as a vector of
     *  vectors. On the basis of these vectors, a new double array
     *  element is created and filled with the entries of the
     *  Matrix. The matrix is returned as a DoubleMatrixToken.
     * @param filename The filename.
     * @return A Token contained the matrix as a DoubleMatrixToken.
     * @exception IllegalActionException If for the given filename
     *   a file cannot be opened.
     */
    public static MatrixToken readMatrix(String filename) 
            throws IllegalActionException 
        {

            DoubleMatrixToken returnMatrix = null;
            
            File fileT = new File(filename);
            FileReader fin = null;
                
            // Vector containing the matrix
            Vector k = null;
                
            // Parameters for the Matrix
            int row = -1;
            int column = -1;
                
            // Matlab Matrices always start at 1 instead of 0.
            int posRow = 1;
            int posColumn = 1;
            double[][] mtr = null;
                            
            if (fileT.exists()) {
            
                try {
                    // Open the matrix file
                    fin = new FileReader(fileT);
                } catch (FileNotFoundException e) {
                    throw new IllegalActionException("FIle Not FOUND");
                }
                    

                // Read the file and convert it into a matrix
                mp.ReInit( fin );
                k = mp.readMatrix( );
                    
                if ( column == -1 ) {
                    // The column size of the matrix
                    column = k.size();
                }

                Iterator i = k.iterator();
                while( i.hasNext() ) {
                    Vector l = (Vector) i.next();                    
                    if ( row == -1 ) { 
                                // the row size.
                        row = l.size();                        
                                // create a new matrix definition
                        mtr = new double[column+1][row+1];
                    } else {
                        if ( row != l.size() ) {
                            throw new  IllegalActionException(" The Row" +
                                    " size needs to be the same for all" +
                                    " rows");
                        }
                    }                    
                    Iterator j = l.iterator();
                    while( j.hasNext() ) {
                        Double s = (Double) j.next();
                        mtr[posColumn][posRow++] = s.doubleValue();
                    }
                    posRow=1;
                    posColumn++;
                }

                // Vectors have now become obsolete, data is stored
                // in double[][].
                k.removeAll(k);
                returnMatrix =  new DoubleMatrixToken(mtr);
            } else {
                throw new IllegalActionException("ReadMatrix: File " + 
                        filename + " not Found");
            }
            
            return returnMatrix;      
        }

    /** The Matrix Parser. The Matrix parser is recreated for the standard
        in. However, we use ReInit for the specific matrix files. */
    static MatrixParser mp = new MatrixParser( System.in );
}
