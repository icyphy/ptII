/* MappingEditor is an attribute used to edit the mapping constraints.

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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.metroII.gui.MappingEditorGUIFactory;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Gets mapping file name.
     * @return The name of the mapping file.
     * @exception IllegalActionException If the director is not a MetroIIDirector.
     */
    public File getMappingFile() throws IllegalActionException {
        CompositeActor actor = (CompositeActor) this.getContainer();
        Director director = actor.getDirector();

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
     * @return The mapping constraints.
     * @exception IllegalActionException If the mapping file cannot be read.
     */
    public String readMapping() throws IllegalActionException {
        String buffer = null;
        File file = getMappingFile();
        if (file != null) {
            String filename = file.getAbsolutePath();
            if (!filename.equals("")) {
                try {
                    buffer = MappingConstraintReaderWriter
                            .readMappingFile(filename);
                } catch (IOException ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to open mapping file \"" + filename + "\".");
                }
            }
        }

        return buffer;
    }

    /**
     * Saves content to mapping file.
     *
     * @param content
     *            the content to save to the mapping file.
     * @exception IllegalActionException
     *             a failed or interrupted I/O operations has occurred.
     */
    public void saveMapping(String content) throws IllegalActionException {
        File file = getMappingFile();
        if (file != null) {
            String filename = file.getAbsolutePath();
            if (!filename.equals("")) {
                try {
                    MappingConstraintReaderWriter.writeMappingFile(file,
                            content);
                } catch (IOException ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to save to mapping file \"" + filename
                            + "\".");
                }
            }
        }
    }

    /**
     * Returns actors names on all hierarchies.
     *
     * @return actors names on all hierarchies.
     * @exception IllegalActionException
     */
    public String actorNames() throws IllegalActionException {
        Nameable container = getContainer();

        LinkedList<String> _completeActorNameList = new LinkedList<String>();

        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();
            LinkedList<Actor> queue = new LinkedList<Actor>();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                queue.add(actor);
            }

            while (!queue.isEmpty()) {
                Actor actor = queue.poll();
                if (actor instanceof CompositeActor) {
                    Iterator<?> actorList = ((CompositeActor) actor)
                            .deepEntityList().iterator();
                    while (actorList.hasNext()) {
                        Actor actorNextLevel = (Actor) actorList.next();
                        queue.add(actorNextLevel);
                    }
                } else {
                    _completeActorNameList.add(MetroIIEventBuilder
                            .trimModelName(actor.getFullName()));
                }
            }
        }

        StringBuilder actorNameText = new StringBuilder();
        for (String actorName : _completeActorNameList) {
            actorNameText.append(actorName + "\n");
        }

        return actorNameText.toString();
    }

}
