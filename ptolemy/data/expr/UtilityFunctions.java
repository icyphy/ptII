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

//////////////////////////////////////////////////////////////////////////
//// UtilityFunctions
/**
Class providing additional functions to ptolemyII expression language.
<p>
Currently this class only contains two methods, env() and readFile(),
and even for these there are only trivial implementations.
<p>
FIXME: finish this class.
@author  Neil Smyth
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
     * */
    public static StringToken readFile(String filename) {
        // temporary hack, need to work out way to obtain the path.
        String curDir = System.getProperty("user.dir");
        //System.out.println("Directory is " + curDir);
        File fileT = new File(curDir, filename);
        //System.out.println("Trying to open file: " + fileT.toString());
        BufferedReader fin = null;
        String line;
        String result = "";
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
                    result += line;
                    //System.out.println("read in line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            // what should we do here?
        }
        //System.out.println("Contents of file are: " + result);
        return new StringToken(result);
    }
}
