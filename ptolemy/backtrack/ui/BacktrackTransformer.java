/* 

Copyright (c) 2005 The Regents of the University of California.
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
import java.util.Stack;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// ModelTransformer
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class BacktrackTransformer {
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
            
            return topLevel;
        } catch (Exception e) {
            throw new IllegalActionException(e.toString());
        }
    }
    
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
    
                        int lastDot = _classBeforeChange.lastIndexOf('.');
                        if (lastDot == -1) {
                            _iconPropertyName = _classBeforeChange;
                        } else {
                            _iconPropertyName =
                                _classBeforeChange.substring(lastDot + 1);
                        }
                        _iconPropertyName += "Icon";
    
                        _classStack.push(_classBeforeChange);
                        return _classAfterChange;
                    } else {
                        _classBeforeChange = null;
                        _iconPropertyName = null;
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
            } else if (element.equals("property") &&
                    _classAfterChange != null &&
                    container.getClassName().equals(_classAfterChange) &&
                    attributeName.equals("name") &&
                    attributeValue.equals(_iconPropertyName)) {
                String iconFileName =
                    _classBeforeChange.replace('.', '/') + "Icon.xml";

                _classBeforeChange = null;
                _classAfterChange = null;

                URL iconFile = getClass().getClassLoader()
                        .getResource(iconFileName);
                if (iconFile != null) {
                    try {
                        Reader reader =
                            new InputStreamReader(iconFile.openStream());
                        _parse(reader, container);
                        reader.close();
                    } catch (Exception e) {
                        // Ignore.
                    }
                }
                return attributeValue;
            } else {
                return attributeValue;
            }
        }

        public void filterEndElement(NamedObj container, String elementName) {
            if ((elementName.equals("entity") || elementName.equals("property"))
                    && container.getClassName() != null) {
                _classStack.pop();
            }
        }

        public static final String AUTOMATIC_PREFIX =
            "ptolemy.backtrack.automatic";
        public static final String MANUAL_PREFIX =
            "ptolemy.backtrack.manual";
        
        private boolean _classExists(String className) {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        
        private String _newClassName(String oldClassName) {
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

        private String _classAfterChange = null;
        private String _classBeforeChange = null;
        private Stack _classStack = new Stack();
        private String _iconPropertyName = null;
        private MoMLParser _parser = null;
    }
}
