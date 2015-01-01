/* MappingConstraintReaderWriter provides methods to read or write mapping constraints from or to a csv file.

Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.domains.metroII.kernel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

///////////////////////////////////////////////////////////////////
//// MappingConstraintReaderWriter

/**
 * MappingConstraintReaderWriter provides methods to read or write mapping
 * constraints from or to a csv file.
 *
 * @author Liangpeng Guo
 * @version $Id: MappingConstraintReaderWriter.java 68172 2014-01-22 02:03:40Z
 *          glp $
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MappingConstraintReaderWriter {

    /**
     * Reads mapping constraints from a file. MappingConstraintSolver
     *
     * @param filename
     *            Filename of the mapping constraint file.
     * @return the constraint file in a string.
     * @exception IOException
     *                a failed or interrupted I/O operations has occurred.
     */
    public static String readMappingFile(String filename) throws IOException {
        FileInputStream stream = new FileInputStream(filename);
        DataInputStream in = new DataInputStream(stream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, java.nio.charset.Charset.defaultCharset()));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    /**
     * Returns a list of mapping constraints.
     *
     * @param buffer constraints in a string.
     * @return a list of mapping constraints.
     */
    public static LinkedList<Pair<String, String>> readConstraints(String buffer) {
        LinkedList<Pair<String, String>> constraintList = new LinkedList<Pair<String, String>>();
        String[] constraints = buffer.split("\n");
        for (String line : constraints) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] eventNames = line.split(",");
            assert eventNames.length == 2;
            eventNames[0] = eventNames[0].trim();
            eventNames[1] = eventNames[1].trim();

            constraintList.add(new Pair(eventNames[0], eventNames[1]));
        }
        return constraintList;
    }

    /**
     * Writes content to mapping file.
     *
     * @param file The mapping file to be written.
     * @param content the content to save to the mapping file.
     * @exception IOException If a failed or interrupted I/O operations
     * has occurred.
     */
    public static void writeMappingFile(File file, String content)
            throws IOException {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(file.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }

}
