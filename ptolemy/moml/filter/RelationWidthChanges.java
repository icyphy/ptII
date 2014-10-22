/**
 A filter for backward compatibility with 7.2.devel or earlier models for width inference.

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

import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.IORelation;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// RelationWidthChanges

/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter so that models from
 earlier releases will run in the current release.
 This class will filter for relations that have a width equal to zero
 for Ptolemy versions 7.2.devel or older. The width value will be changed
 from 0 to -1, which is the new default for width inference.
 If the width has not been specified for a models with Ptolemy version
 less than 7.2.devel, the width will be changed to 1. This because 1
 used to be the default value, while now -1 has become the default.

 @author Bert Rodiers
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */
public class RelationWidthChanges extends MoMLFilterSimple {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Filter relations widths and change 0 to -1.
     *  @param container  The container for XML element.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return A new value for the attribute, or the same value
     *   to leave it unchanged, or null to cause the current element
     *   to be ignored (unless the attributeValue argument is null).
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

        if (xmlFile != null) {
            Boolean changedNeeded = _changesNeededForXmlFile.get(xmlFile);
            if (changedNeeded != null && !changedNeeded) {
                return attributeValue;
            }
        }

        if (_currentlyProcessingRelation) {
            if (_currentlyProcessingWidth) {
                if (attributeName.equals("value")) {
                    _currentlyProcessingRelation = false;
                    _currentlyProcessingWidth = false;

                    if (_changedNeeded(container, xmlFile)
                            && attributeValue.equals("0")) {
                        MoMLParser.setModified(true);
                        return Integer.toString(IORelation.WIDTH_TO_INFER);
                    }
                }
            } else {
                if (attributeValue.equals("width")
                        && element.equals("property")) {
                    _currentlyProcessingWidth = true;
                }
            }
        } else if (element.equals("relation") && attributeName.equals("class")) {
            _currentlyProcessingRelation = true;
        }
        return attributeValue;
    }

    /** Make modifications to the specified container, which is
     *  defined in a MoML element with the specified name.
     *  This method is called when an end element in MoML is
     *  encountered. A typical use of this method is to make
     *  some modification to the object (the container) that
     *  was constructed.
     *  <p>
     *  If an implementor makes changes to the specified container,
     *  then it should call MoMLParser.setModified(true) which indicates
     *  that the model was modified so that the user can optionally
     *  save the modified model.
     *
     *  @param container The object defined by the element that this
     *   is the end of.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception If there is a problem modifying the
     *  specified container.
     */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
        if (container instanceof VersionAttribute) {
            VersionAttribute version = (VersionAttribute) container;
            try {
                if (xmlFile != null) {
                    _changesNeededForXmlFile.put(xmlFile, version
                            .isLessThan(new VersionAttribute("7.2.devel")));
                }
            } catch (IllegalActionException e) {
                // We don't expect that this fails.
                throw new IllegalStateException(e);
            }
        } else if (container instanceof IORelation) {
            if (_currentlyProcessingRelation && !_currentlyProcessingWidth) {
                // We have processed a relation, but we did not encounter
                // the width. This means that the default one is being used.
                // The default one was 1, but this has been changed to
                // IORelation.WIDTH_TO_INFER. If we encounter this case with
                // a Ptolemy model before 7.2 we need to update the width of
                // the relation to 1 to not change the user's model.

                if (_changedNeeded(container, xmlFile)) {
                    IORelation relation = (IORelation) container;
                    relation.setWidth(1);
                    relation.width.propagateValue();
                    MoMLParser.setModified(true);
                }
            }
            _currentlyProcessingRelation = false;
            _currentlyProcessingWidth = false;
        }
    }

    /** Return a string that describes what the filter does.
     *  @return A description of the filter (ending with a newline).
     */
    @Override
    public String toString() {
        return Integer.toHexString(hashCode());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return whether changes are necessary.
     * This is only the case for models older than version 7.2.devel
     * @param container The container.
     * @param xmlFile The xmlFile
     * @return True when changes are necessary.
     */
    private boolean _changedNeeded(NamedObj container, String xmlFile) {
        // First Check whether we already have the version
        Boolean changesNeeded = xmlFile != null ? _changesNeededForXmlFile
                .get(xmlFile) : null;
        if (changesNeeded != null && changesNeeded) {
            return _changesNeededForXmlFile.get(xmlFile);
        } else {
            // Retrieve the version number. This is only available on the toplevel.
            NamedObj toplevel = container;
            NamedObj parent = toplevel.getContainer();

            while (parent != null) {
                toplevel = parent;
                parent = toplevel.getContainer();
            }
            Attribute version = toplevel.getAttribute("_createdBy");
            if (version != null) {
                try {
                    return ((VersionAttribute) version)
                            .isLessThan(new VersionAttribute("7.2.devel"));
                } catch (IllegalActionException e) {
                }
            } else {
                // If there is no _createdBy attribute, then this might be a
                // copy and paste, in which case we assume we are copying
                // from the current version.  Note that this might not
                // be always be true, but it is more likely that we are copying
                // from a version recent version than a pre 7.2.devel version.
                // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4804
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**A flag that specifies whether we are currently processing a relation*/
    private boolean _currentlyProcessingRelation = false;

    /**A flag that specifies whether we are currently processing the width of a relation*/
    private boolean _currentlyProcessingWidth = false;

    /**A flag that specifies whether changes might be needed for the model with a certain xmlPath.
     * This is only the case for models older than version 7.2.devel
     */
    private Map<String, Boolean> _changesNeededForXmlFile = new HashMap<String, Boolean>();
}
