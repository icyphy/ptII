/* MappingEditor is an attribute used to edit the mapping constraints.

 Copyright (c) 2012-2013 The Regents of the University of California.
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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.metroII.gui.MappingEditorGUIFactory;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// MappingEditor

/**
 * MappingEditor is an attribute used to edit the mapping constraints.
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 * 
 */
public class MappingEditor extends Attribute {

    /**
     * Constructs a mapping editor.
     * 
     * @param container
     *            Container of the director.
     * @param name
     *            Name of this attribute.
     * @exception IllegalActionException
     *                If the director is not compatible with the specified
     *                container. May be thrown in a derived class.
     * @exception NameDuplicationException
     *                If the container is not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public MappingEditor(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:11; font-family:SansSerif; fill:white\">"
                + "Double click to\nedit mapping.</text></svg>");

        new MappingEditorGUIFactory(this, "_mappingEditorGUIFactory");
    }

    /**
     * Gets mapping file name.
     */
    public File getMappingFile() throws IllegalActionException {
        CompositeActor actor = (CompositeActor) this.getContainer();
        Director director = (Director) actor.getDirector();

        if (!(director instanceof MetroIIDirector)) {
            throw new IllegalActionException("Need a MetroIIDirector!");
        }

        FileParameter mappingFileName = ((MetroIIDirector) director).mappingFileName;

        if (mappingFileName == null) {
            return null;
        } else {
            StringToken mappingFileNameToken = (StringToken) mappingFileName
                    .getToken();
            if (mappingFileNameToken == null
                    || mappingFileNameToken.stringValue().equals("")) {
                return null;
            } else {
                return mappingFileName.asFile();
            }
        }

    }

    /**
     * Reads mapping constraints.
     * 
     * @throws IllegalActionException
     */
    public String readMapping() throws IllegalActionException {

        String buffer = null;

        File file = getMappingFile();
        if (file != null) {
            String filename = file.getAbsolutePath();
            if (!filename.equals("")) {
                try {
                    buffer = readMappingFile(filename);
                } catch (IOException ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to open mapping file \"" + filename + "\".");
                }
            }
        }

        return buffer;
    }

    /**
     * Reads mapping constraints from a file. MappingConstraintSolver
     * 
     * @param filename
     *            Filename of the mapping constraint file.
     * @exception IOException
     *                a failed or interrupted I/O operations has occurred.
     */
    public String readMappingFile(String filename) throws IOException {
        FileInputStream stream = new FileInputStream(filename);
        DataInputStream in = new DataInputStream(stream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
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
     * Saves content to mapping file.
     * 
     * @param content
     *            the content to save to the mapping file.
     * @throws IllegalActionException
     *             a failed or interrupted I/O operations has occurred.
     */
    public void saveMapping(String content) throws IllegalActionException {
        File file = getMappingFile();
        if (file != null) {
            String filename = file.getAbsolutePath();
            if (!filename.equals("")) {
                try {
                    FileWriter fileWriter = new FileWriter(
                            file.getAbsoluteFile());
                    BufferedWriter bufferedWriter = new BufferedWriter(
                            fileWriter);
                    bufferedWriter.write(content);
                    bufferedWriter.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to save to mapping file \"" + filename
                                    + "\".");
                }
            }
        }

    }

}
