/* Remove classes

 Copyright (c) 1998-2014 The Regents of the University of California.
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
import java.util.Iterator;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// RemoveClasses

/** When this class is registered with the MoMLParser.addMoMLFilter()
 method, it will cause MoMLParser to remove or replace classes.

<p>Note that that in the default, this class has an empty list
of classes to be removed or replaced.  To add classes to be removed,
call put("my.class.to.be.removed", null).

<p>This class is not added to the list of filters in
BackwardCompatibility, it must be added by calling
{#link ptolemy.moml.MoMLParser.addMoMLFilter(MoMLFilter)

 @author  Edward A. Lee, Christopher Hylands
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class RemoveClasses extends MoMLFilterSimple {
    /** Clear the map of graphical classes to be removed.
     */
    public static void clear() {
        _graphicalClasses = new HashMap();
    }

    /** Filter for graphical classes and return new values if
     *  a graphical class is found.
     *  An internal HashMap maps names of graphical entities to
     *  new names.  The HashMap can also map a graphical entity
     *  to null, which means the entity is removed from the model.
     *  All class attributeValues that start with "ptolemy.domains.gr"
     *  are deemed to be graphical elements and null is always returned.
     *  For example, if the attributeValue is "ptolemy.vergil.icon.ValueIcon",
     *  or "ptolemy.vergil.basic.NodeControllerFactory"
     *  then return "ptolemy.kernel.util.Attribute"; if the attributeValue
     *  is "ptolemy.vergil.icon.AttributeValueIcon" or
     *  "ptolemy.vergil.icon.BoxedValueIcon" then return null, which
     *  will cause the MoMLParser to skip the rest of the element;
     *  otherwise return the original value of the attributeValue.
     *
     *  @param container  The container for this attribute, ignored
     *  in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute, ignored
     *   in this method.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the filtered attributeValue.
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        // If the nightly build is failing with messages like:
        // " X connection to foo:0 broken (explicit kill or server shutdown)."
        // Try uncommenting the next lines to see what is being
        // expanding before the error:
        //System.out.println("filterAttributeValue: " + container + "\t"
        //       +  attributeName + "\t" + attributeValue);
        if (attributeValue == null) {
            return null;
        } else if (_graphicalClasses.containsKey(attributeValue)) {
            MoMLParser.setModified(true);
            return (String) _graphicalClasses.get(attributeValue);
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

    /** Remove a class to be filtered.
     *  @param className The name of the class to be filtered
     *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
     *  @see #put(String, String)
     */
    public void remove(String className) {
        // ptolemy.copernicus.kernel.MakefileGenerator
        // so as to filter out the GeneratorAttribute
        _graphicalClasses.remove(className);
    }

    /** Add a class to be filtered for and its replacement if the class
     *  is found.  If the replacement is null, then the rest of the
     *  attribute is skipped.  Note that if you add a class with
     *  this method, then you must remove it with {@link #remove(String)},
     *  calling 'new RemoveClasses' will not remove a class
     *  that was added with this method.
     *  @param className The name of the class to be filtered
     *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
     *  @param replacement The name of the class to be used if
     *  className is found.  If this argument is null then the
     *  rest of the attribute is skipped.
     *  @see #remove(String)
     */
    public void put(String className, String replacement) {
        // ptolemy.copernicus.kernel.KernelMain call this method
        // so as to filter out the GeneratorAttribute
        _graphicalClasses.put(className, replacement);
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    @Override
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Remove or replace classes.\n"
                + "The following actors are affected:\n");
        Iterator classNames = _graphicalClasses.keySet().iterator();

        while (classNames.hasNext()) {
            String oldClassName = (String) classNames.next();
            String newClassName = (String) _graphicalClasses.get(oldClassName);

            if (newClassName == null) {
                results.append(oldClassName + " will be removed\n");
            } else {
                results.append(oldClassName + " will be replaced by "
                        + newClassName + "\n");
            }
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Map of actor names a HashMap of graphical classes to their
     *  non-graphical counterparts, usually either
     *  ptolemy.kernel.util.Attribute or null.
     */
    private static HashMap _graphicalClasses;

    static {
        _graphicalClasses = new HashMap();
    }
}
