/* Create a list of actors parsed thus far.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.moml.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// NamedObjClassesSeen

/** Create a Set of classes that extend NamedObj that are in the MoML
 parsed thus far.  This filter does not modify the model.

 @author Christopher Brooks, Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class NamedObjClassesSeen extends MoMLFilterSimple {
    /** Create a filter that looks for classes that extend NamedObj.
     *  @param classesToBeIndexed A HashMap, where the key is a fully
     *  qualified dot separated String naming the class; and the key
     *  is a Set where each element is a String that is a relative
     *  path that refresh to the model.
     */

    public NamedObjClassesSeen(HashMap classesToBeIndexed) {
        reset(null);
        _classesToBeIndexed = classesToBeIndexed;
    }

    /** If the attributeName is "class" and the attributeValue extends
     *  NamedObj, then add the attributeValue to the set of classes
     *  we are interested in.
     *  @param container  The container for this attribute.
     *   in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the value of the attributeValue argument.
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        // This method gets called many times by the MoMLParser,
        // so we try to be smart about the number of comparisons
        // and we try to group comparisons together so that we
        // are not making the same comparison more than once.
        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("class")) {
            // If we have not yet seen this class, check it
            if (!_classesSeen.contains(attributeValue)) {
                _classesSeen.add(attributeValue);
                Set models = null;
                if ((models = (Set) _classesToBeIndexed.get(attributeValue)) != null) {
                    Class theClass = null;
                    try {
                        theClass = Class.forName(attributeValue);
                    } catch (Throwable ex) {
                        // Print a message and move on.
                        // FIXME: Use the doclet error handling mechanism
                        System.err.println("Failed to process "
                                + attributeValue + "\n" + ex);
                    }
                    if (theClass != null
                            && _namedObjClass.isAssignableFrom(theClass)) {
                        if (container != null
                                && container instanceof TypedCompositeActor
                                && container.getFullName().indexOf(".", 1) != -1) {
                            // If the container is not a top level, then
                            // link to the inner part
                            String compositePath = _modelPath
                                    + "#"
                                    + container.getFullName().substring(
                                            container.getFullName().indexOf(
                                                    ".", 1) + 1);
                            //                             System.out.println("NamedObjClasssesSeen: ("
                            //                                                + compositePath
                            //                                                + ") container: " + container
                            //                                                + " element: " + element
                            //                                                + " attributeName: " + attributeName
                            //                                                + " attributeValue: " + attributeValue);
                            models.add(compositePath);
                        } else {
                            models.add(_modelPath);
                        }
                    }
                }
            }
        }
        return attributeValue;
    }

    /** In this class, do nothing.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception Not thrown in this base class.
     */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
    }

    /** Return the Set of classes we have seen that extend NamedObj.
     *  @return Classes seen that extend NamedObj
     */
    //    public Set getNamedObjClassesSeen() {
    //        return _namedObjClassesSeen;
    //    }
    /** Reset the filter.
     *  @param modelPath The new model path.
     */
    public void reset(String modelPath) {
        _modelPath = modelPath;
        _classesSeen = new HashSet();
        //_namedObjClassesSeen = new HashSet();
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    @Override
    public String toString() {
        return getClass().getName()
                + ": Create a Set of classes that have been parsed thus far. "
                + "The classes extend NamedObj. "
                + "This filter does not modify the model. ";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Set of classes seen the far.  Each element is a String that
     *  is a dot separated fully qualified class name.
     */
    private Set _classesSeen;

    /**  A HashMap, where the key is a fully
     *  qualified dot separated String naming the class; and the key
     *  is a Set where each element is a String that is a relative
     *  path that refresh to the model.
     */
    private HashMap _classesToBeIndexed;

    /** The relative path to the model we are parsing.
     */
    String _modelPath;

    private static Class _namedObjClass;
    static {
        try {
            _namedObjClass = Class.forName("ptolemy.kernel.util.NamedObj");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
