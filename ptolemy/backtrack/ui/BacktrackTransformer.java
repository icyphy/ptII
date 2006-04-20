/* Model transformer for backtracking.

Copyright (c) 2005-2006 The Regents of the University of California.
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
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
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
     *  @throws IllegalActionException If the parser fails to parse the
     *  transformed model.
     */
    public static NamedObj transformModel(NamedObj model)
            throws IllegalActionException {
        StringWriter moml = new StringWriter();
        try {
            model.exportMoML(moml, 0);

            MoMLParser parser = new MoMLParser();
            RenameClassMoMLFilter filter = new RenameClassMoMLFilter();
            parser.addMoMLFilter(filter);
            NamedObj topLevel = parser.parse(null, moml.toString());
            parser.getMoMLFilters().remove(filter);
            
            Iterator entitiesToRename = filter.entitiesToRename();
            while (entitiesToRename.hasNext()) {
                NamedObj entity = (NamedObj)entitiesToRename.next();
                
                // Add a little visual effect to the transformed entity.
                String imageMoML =
                    "<property name=\"_decorate\" " +
                    "class=\"ptolemy.data.expr.FileParameter\" " +
                    "value=\"$CLASSPATH/ptolemy/backtrack/manual/ptolemy/" +
                    "actor/lib/BacktrackIconSmall.gif\">\n" +
                    "</property>";
                parser.setContext(entity);
                try {
                    parser.parse(null, imageMoML);
                    MoMLParser.setModified(true);
                } catch (Exception ex) {
                    throw new IllegalActionException("Unable to parse\n" +
                            imageMoML);
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
    
    //////////////////////////////////////////////////////////////////////////
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
    private static class RenameClassMoMLFilter implements MoMLFilter {

        public String filterAttributeValue(NamedObj container, String element,
                String attributeName, String attributeValue) {
            if (attributeValue == null) {
                return null;
            }

            if (attributeName.equals("class")) {
                if (element.equals("entity")) {
                    _classAfterChange = _newClassName(attributeValue);
                    if (_classAfterChange != null) {
                        MoMLParser.setModified(true);
                        _classBeforeChange = attributeValue;
                        _classStack.push(_classBeforeChange);
                        return _classAfterChange;
                    } else {
                        _classBeforeChange = null;
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

        public void filterEndElement(NamedObj container, String elementName)
                throws IllegalActionException {
            if ((elementName.equals("entity") || elementName.equals("property"))
                    && container != null && container.getClassName() != null) {
                if (_classStack.peek() != null &&
                        container.getClassName().contains((String)_classStack.peek())) {
                    // Copy the original icon to the MoML.
                    _copyIcon(container);
                    
                    // Add "(B)" to the actor's name later.
                    _entitiesToRename.add(container);
                }
                _classStack.pop();
            }
        }
        
        public Iterator entitiesToRename() {
            return _entitiesToRename.iterator();
        }

        public static final String AUTOMATIC_PREFIX =
            "ptolemy.backtrack.automatic";
        
        public static final String MANUAL_PREFIX =
            "ptolemy.backtrack.manual";
        
        private static boolean _classExists(String className) {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        
        private static String _findLastModifiedClassName(Stack classStack) {
            if (classStack.isEmpty()) {
                return null;
            }
            
            String peek = (String)classStack.peek();
            if (peek != null) {
                return peek;
            } else {
                classStack.pop();
                String result = _findLastModifiedClassName(classStack);
                classStack.push(peek);
                return result;
            }
        }
        
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
        
        private NamedObj _parse(Reader reader, NamedObj container)
                throws Exception {
            if (_parser == null) {
                _parser = new MoMLParser();
            }

            _parser.setContext(container);
            NamedObj result = _parser.parse(null, reader);
            MoMLParser.setModified(true);
            return result;
        }
        
        private void _copyIcon(NamedObj container)
                throws IllegalActionException {
            String iconFileName =
                ((String)_classStack.peek()).replace('.', '/') + "Icon.xml";

            URL iconFile = getClass().getClassLoader()
                    .getResource(iconFileName);
            if (iconFile != null) {
                try {
                    Reader reader =
                        new InputStreamReader(iconFile.openStream());
                    _parse(reader, container);
                    reader.close();
                } catch (Exception e) {
                    throw new IllegalActionException(e.toString());
                }
            }
        }

        private String _classAfterChange = null;
        
        private String _classBeforeChange = null;

        private Stack _classStack = new Stack();
        
        private MoMLParser _parser = null;
        
        private MoMLParser _imageParser = null;
        
        private List _entitiesToRename = new LinkedList();
    }
}
