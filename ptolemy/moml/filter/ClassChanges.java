/* Filter for simple class name changes

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.moml.filter;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ClassChanges
/** When this class is registered with the MoMLParser.setMoMLFilter()
method, it will cause MoMLParser to filter so that models from
earlier releases will run in the current release.

<p>This class will filter moml for simple class changes where
the context of the class name to be changed does not matter - all
occurrences of the class name will be changed.  This class
can be though of as a primitive form of sed.

<p> If a class within an actor is what has changed, use (@see
PropertyClassChanges) instead.

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class ClassChanges implements MoMLFilter {

    /** If the attributeName is "class" and attributeValue names a
     *  class that needs to be renamed, then substitute in the new class
     *  name. If the attributeValue names a class that needs to be removed,
     *  then return null, which will cause the MoMLParser to skip the
     *  rest of the element;
     *
     *  @param container  The container for this attribute.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @return the value of the attributeValue argument.
     */
    public String filterAttributeValue(NamedObj container,
            String attributeName, String attributeValue) {

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
            if (_classChanges.containsKey(attributeValue)) {
                // We found a class with a class change.
                MoMLParser.setModified(true);
                return (String)_classChanges.get(attributeValue);
            } else if (_classesToRemove.contains(attributeValue)) {
                // We found a class to remove.
                return null;
            }
        }
        return attributeValue;
    }

    /** Given the elementName, perform any filter operations
     *  that are appropriate for the MOMLParser.endElement() method.
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param elementName The element type name.
     *  @return the filtered element name, or null if
     *  MoMLParser.endElement() should immediately return.
     */
    public String filterEndElement(NamedObj container, String elementName)
            throws Exception {
        return elementName;
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    public String toString() {
        StringBuffer results =
            new StringBuffer(getClass().getName()
                    + ": change any class names that have been "
                    + "renamed and remove obsolete classes.\n"
                    + "Below are original class names followed by "
                    + "the new class names:\n");
        Iterator classNames = _classChanges.keySet().iterator();
        while (classNames.hasNext()) {
            String className = (String)classNames.next();
            results.append("\t" + className + "\t -> "
                    + _classChanges.get(className) + "\n");
        }
        results.append("\nBelow are the classes to remove:\n");
        Iterator classesToRemove = _classesToRemove.iterator();
        while (classesToRemove.hasNext()) {
            String className = (String)classesToRemove.next();
            results.append("\t" + className + "\n");
        }
        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Map of actor names a HashMap of property names to new classes.
    private static HashMap _classChanges;

    static {
        ///////////////////////////////////////////////////////////
        // Actors and attributes that have changed names.
        _classChanges = new HashMap();

        // Location
        _classChanges.put("ptolemy.moml.Location",
                "ptolemy.kernel.util.Location");

        // New in 2.1-devel-2
        _classChanges.put("ptolemy.kernel.util.VersionAttribute",
                "ptolemy.kernel.attributes.VersionAttribute");

        // New in 2.3-devel
        _classChanges.put("ptolemy.actor.lib.comm.SerialComm",
                "ptolemy.actor.lib.io.comm.SerialComm");

        // New in 3.1-devel
        _classChanges.put("ptolemy.domains.fsm.lib.RelationList",
                "ptolemy.domains.fsm.kernel.RelationList");
                
        // Renamed in 3.1-devel
        _classChanges.put("ptolemy.vergil.icon.ImageEditorIcon",
                "ptolemy.vergil.icon.ImageIcon");
                
        // Replaced FileAttribute with FileParameter in 3.2-devel
        _classChanges.put("ptolemy.kernel.attributes.FileAttribute",
                "ptolemy.data.expr.FileParameter");

        // SDFIOPort is obsolete as of 3.2-devel
        _classChanges.put("ptolemy.domains.sdf.kernel.SDFIOPort",
                "ptolemy.actor.TypedIOPort");

        // Moved MultiInstanceComposite
        _classChanges.put("ptolemy.actor.hoc.MultiInstanceComposite",
                "ptolemy.actor.lib.hoc.MultiInstanceComposite");
    }
    
    // Set of class names that are obsolete and should be simply
    // removed.
    private static HashSet _classesToRemove;
    
    static {
        ////////////////////////////////////////////////////////////
        // Classes that are obsolete and should be removed.
        _classesToRemove = new HashSet();
        
        // NotEditableParameter
        _classesToRemove.add("ptolemy.data.expr.NotEditableParameter");
    }
}
