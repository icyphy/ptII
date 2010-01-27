/* A utility class for creating specialized Array types.
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009 The Regents of the University of California.
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
*/
package ptolemy.codegen.c.kernel.type.parameterizedTemplates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
A utility class for creating specialized Array types.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
*/
public class InstantiateFromTemplate {

    /**
     * Generate a set of type-specific Array code files from a template by
     * macro substitution. A type-specific Array file contains functions for
     * the particular Array type (e.g. IntArray, DoubleArray, and etc.)
     * @param args The first argument is taken to be the file path
     * of the template. The second is the directory path to place
     * the generated files.
     * @exception Exception Thrown if an error occurs when reading
     * or writing the files.
     */
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(new File(
                args[0])));

        StringBuffer templateCode = new StringBuffer();
        String line = reader.readLine();
        while (line != null) {
            templateCode.append(line + "\r\n");
            line = reader.readLine();
        }
        reader.close();

        String filename = args[1] + "\\DoubleArray.c";

        replaceMap.put("\\$Type", "Double");
        replaceMap.put("\\$type_q", "%g");
        replaceMap.put("\\$type", "double");
        replaceMap.put("\\$print_size", "22");
        filename = args[1] + "\\DoubleArray.c";
        _replaceAndPrintContent(templateCode, filename);

        replaceMap.put("\\$Type", "Boolean");
        replaceMap.put("\\$type_q", "%b");
        replaceMap.put("\\$type", "boolean");
        replaceMap.put("\\$print_size", "6");
        filename = args[1] + "\\BooleanArray.c";
        _replaceAndPrintContent(templateCode, filename);

        replaceMap.put("\\$Type", "Int");
        replaceMap.put("\\$type_q", "%d");
        replaceMap.put("\\$type", "int");
        replaceMap.put("\\$print_size", "12");
        filename = args[1] + "\\IntArray.c";
        _replaceAndPrintContent(templateCode, filename);

        replaceMap.put("\\$Type", "String");
        replaceMap.put("\\$type_q", "%s");
        replaceMap.put("\\$type", "string");
        replaceMap.put("\\$print_size", "100");
        filename = args[1] + "\\StringArray.c";
        _replaceAndPrintContent(templateCode, filename);
    }

    private static void _replaceAndPrintContent(StringBuffer templateCode,
            String filename) throws IOException {
        String codeString = templateCode.toString();

        for (String key : replaceMap.keySet()) {
            codeString = codeString.replaceAll(key, replaceMap.get(key));
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(filename));
            writer.write(codeString);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     *
     * NOTE: META SUBSTITUTION SYMBOLS
     * $Type: Int, Char, Array, etc.
     * $type_q: %d, %s, etc.
     * $type: int, char, etc.
     * $print_size: 12(int), 22(long), 22(double), 6(boolean)
     */
    private static HashMap<String, String> replaceMap = new HashMap<String, String>();
}
