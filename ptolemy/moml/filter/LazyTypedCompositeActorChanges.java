/* Change TypedCompositeActors into LazyTypedCompositeActors

 Copyright (c) 2008-2014 The Regents of the University of California.
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

import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// LazyTypedCompositeActorChanges

/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter so that all
the TypedCompositeActors except those within actor oriented class
definitions are changed to LazyTypedCompositeActors.

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class LazyTypedCompositeActorChanges extends MoMLFilterSimple {
    /** Possibly replaced TypedCompositeActors with LazyTypedCompositeActors.
     *        If the attributeName is "class", attributeValue is
     *  "ptolemy.actor.TypedCompositeActor" and the container is
     *  not withing an actor oriented class definition, then substitute
     *  in the new class name "ptolemy.actor.LazyTypedCompositeActor".
     *  @param container  The container for this attribute.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the value of the attributeValue argument.
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {

        // Useful for debugging:
        //System.out.println("filterAttributeValue: " + container + "\t"
        //       +  attributeName + "\t" + attributeValue);

        // This method gets called many times by the MoMLParser,
        // so we try to be smart about the number of comparisons
        // and we try to group comparisons together so that we
        // are not making the same comparison more than once.
        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.
        if (attributeName.equals("class")) {
            if (attributeValue.equals("ptolemy.actor.TypedCompositeActor")
                    && container != null) {
                if (container instanceof InstantiableNamedObj
                        && !((InstantiableNamedObj) container)
                        .isWithinClassDefinition()) {
                    // We found a class outside of a class change.
                    MoMLParser.setModified(true);
                    return "ptolemy.actor.LazyTypedCompositeActor";
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

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    @Override
    public String toString() {
        return getClass().getName()
                + ": change TypedCompositeActors that are not within class definitions to"
                + " LazyTypedCompositeActors.";
    }

}
