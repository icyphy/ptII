/* Model transformer for backtracking.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

package ptolemy.backtrack.ui;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.filter.MoMLFilterSimple;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// BacktrackTransformer
/**
 Model transformer for backtracking. This class parses the MoML of the given
 model, and applies a renaming filter to the MoML. The actors with their
 backtracking versions will be replaced. Actors without existing backtracking
 versions will remain unchanged. Other entities in the model, such as
 relations between ports, are kept. The resulting model supports backtracking
 for the transformed actors.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class BacktrackTransformer {

    /** Transform a model by replacing the actors with existing backtracking
     *  versions, and return the resulting model.
     *
     *  Each transformed actor has a "_decorate" attribute. It adds a small
     *  backtracking decoration to the actor's icon.
     *
     *  @param model The model to be transformed.
     *  @return The transformed model.
     *  @exception IllegalActionException If the parser fails to parse the
     *  transformed model.
     */
    public static NamedObj transformModel(NamedObj model)
            throws IllegalActionException {
        StringWriter moml = new StringWriter();
        try {
            model.exportMoML(moml, 0);

            MoMLParser parser = new MoMLParser();
            RenameClassMoMLFilter filter = new RenameClassMoMLFilter();
            MoMLParser.addMoMLFilter(filter);
            NamedObj topLevel = parser.parse(null, moml.toString());
            MoMLParser.getMoMLFilters().remove(filter);

            Iterator entitiesToRename = filter.entitiesChanged();
            while (entitiesToRename.hasNext()) {
                NamedObj entity = (NamedObj) entitiesToRename.next();

                // Add a little visual effect to the transformed entity.
                String imageMoML = "<property name=\"_decorate\" "
                        + "class=\"ptolemy.data.expr.FileParameter\" "
                        + "value=\"$CLASSPATH/ptolemy/backtrack/manual/ptolemy/"
                        + "actor/lib/BacktrackIconSmall.gif\">\n"
                        + "</property>";
                parser.setContext(entity);
                try {
                    parser.parse(null, imageMoML);
                    MoMLParser.setModified(true);
                } catch (Exception ex) {
                    throw new IllegalActionException("Unable to parse\n"
                            + imageMoML);
                }

                /* Do not modify the actor names any more.
                 // Add "(B)" to the end of the entity's name.
                 String oldName = entity.getName();
                 try {
                 entity.setName(oldName + " (B)");
                 } catch (NameDuplicationException ex1) {
                 int i = 0;
                 while (true) {
                 try {
                 entity.setName(oldName + " (B" + i + ")");
                 break;
                 } catch (NameDuplicationException ex2) {
                 // Ignore; try another name.
                 }
                 i++;
                 }
                 }*/
            }

            return topLevel;
        } catch (Exception e) {
            throw new IllegalActionException(e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// RenameClassMoMLFilter

    /**
     The MoML filter that renames the actor classes in the model, if there are
     backtracking versions for them. No change is done on the actor classes
     that do not have backtracking versions.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class RenameClassMoMLFilter extends MoMLFilterSimple {

        /** Return the entities that are changed during the last XML parsing.
         *
         *  @return The entities that are changed.
         */
        public Iterator<NamedObj> entitiesChanged() {
            return _entitiesChanged.iterator();
        }

        /** Filter the value of the attribute. If the attribute corresponds to
         *  a Ptolemy actor with a backtracking version, its class name is
         *  changed to the class name of its backtracking version.
         *
         *  @param container The container of the attribute.
         *  @param element The XML element.
         *  @param attributeName The attribute name.
         *  @param attributeValue The attribute value.
         *  @param xmlFile The file currently being parsed.
         */
        public String filterAttributeValue(NamedObj container, String element,
                String attributeName, String attributeValue, String xmlFile) {
            if (attributeValue == null) {
                return null;
            }

            if (attributeName.equals("class")) {
                if (element.equals("entity")) {
                    String classAfterChange = _newClassName(attributeValue);
                    if (classAfterChange != null) {
                        MoMLParser.setModified(true);
                        String classBeforeChange = attributeValue;
                        _classStack.push(classBeforeChange);
                        return classAfterChange;
                    } else {
                        _classStack.push(null);
                        return attributeValue;
                    }
                } else if (element.equals("property")) {
                    if (_classStack.size() > 0 && _classStack.peek() == null) {
                        _classStack.push(null);
                        return attributeValue;
                    }

                    String newClassName = _newClassName(attributeValue);
                    if (newClassName != null) {
                        _classStack.push(attributeValue);
                        return newClassName;
                    } else {
                        _classStack.push(null);
                        return attributeValue;
                    }
                } else {
                    return attributeValue;
                }
            } else {
                return attributeValue;
            }
        }

        /** Further process the XML element when it is closed with an end tag.
         *  If the element corresponds to a Ptolemy actor that has been changed
         *  to its backtracking version, the MoML description of the original
         *  actor's icon is copied to the new model, because the backtracking
         *  version does not have an icon associated with it.
         *
         *  @param container The container of the element.
         *  @param elementName The XML element to be closed.
         *  @param currentCharData The character data, which appears
         *   only in the doc and configure elements
         *  @param xmlFile The file currently being parsed.
         *  @exception IllegalActionException If the MoML of the original
         *   actor's icon cannot be read and inserted into the new model.
         */
        public void filterEndElement(NamedObj container, String elementName,
                StringBuffer currentCharData, String xmlFile)
                throws IllegalActionException {
            if ((elementName.equals("entity") || elementName.equals("property"))
                    && container != null && container.getClassName() != null) {
                if (_classStack.peek() != null
                        && container.getClassName().equals(
                                _newClassName(_classStack.peek()))) {
                    // Copy the original icon to the MoML.
                    _copyIcon(container);

                    // Add "(B)" to the actor's name later.
                    _entitiesChanged.add(container);
                }
                _classStack.pop();
            }
        }

        /** The prefix to the automatically generated backtracking version of
         *  actors.
         */
        public static final String AUTOMATIC_PREFIX = "ptolemy.backtrack.automatic";

        /** The prefix to the manually written backtracking version of actors.
         */
        public static final String MANUAL_PREFIX = "ptolemy.backtrack.manual";

        /** Test whether a class with the given name can be found.
         *
         *  @param className The name of the class.
         *  @return true if the class is found; false, otherwise.
         */
        private static boolean _classExists(String className) {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        /** Copy the icon of the last modified class (of a Ptolemy actor) to the
         *  MoML within the container's context.
         *
         *  @param container The container.
         *  @exception IllegalActionException If the parsing is not successful.
         */
        private void _copyIcon(NamedObj container)
                throws IllegalActionException {
            String iconFileName = (_classStack.peek()).replace('.', '/')
                    + "Icon.xml";

            URL iconFile = getClass().getClassLoader()
                    .getResource(iconFileName);
            if (iconFile != null) {
                try {
                    Reader reader = new InputStreamReader(iconFile.openStream());
                    _parse(reader, iconFileName, container);
                    reader.close();
                } catch (Exception e) {
                    throw new IllegalActionException(e.toString());
                }
            }
        }

        /** Get the new name for class to be changed to its backtracking
         *  version. If the class has a manually written backtracking version,
         *  the name of that backtracking version will be returned; if there is
         *  no manually written backtracking version for it, but there is an
         *  automatically generated backtracking version, the automatically
         *  name of the generated version will be returned; otherwise, null will
         *  be returned.
         *
         *  @param oldClassName The name of the class before change.
         *  @return The new class name, or null.
         */
        private static String _newClassName(String oldClassName) {
            String automaticClass = AUTOMATIC_PREFIX + "." + oldClassName;
            String manualClass = MANUAL_PREFIX + "." + oldClassName;
            if (_classExists(manualClass)) {
                return manualClass;
            } else if (_classExists(automaticClass)) {
                return automaticClass;
            } else {
                return null;
            }
        }

        /** Parse the content in the reader within the context of the container.
         *
         *  @param reader The reader to be read from.
         *  @param container The context of the parsing.
         *  @return The NamedObj returned by the parser.
         *  @exception Exception If the parsing is not successful.
         */
        private NamedObj _parse(Reader reader, String systemID,
                NamedObj container) throws Exception {
            if (_parser == null) {
                _parser = new MoMLParser();
            }

            _parser.setContext(container);
            NamedObj result = _parser.parse(null, systemID, reader);
            MoMLParser.setModified(true);
            return result;
        }

        /** The stack of the name of the classes that have been changed.
         */
        private Stack<String> _classStack = new Stack<String>();

        /** The list of entities changed during the parsing.
         */
        private List<NamedObj> _entitiesChanged = new LinkedList<NamedObj>();

        /** The parser to parse extra content (e.g., icon MoML).
         */
        private MoMLParser _parser = null;
    }
}
