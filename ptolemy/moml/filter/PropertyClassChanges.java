/* Filter for Property class changes

 Copyright (c) 2002 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// PropertyClassChanges
/** When this class is registered with the MoMLParser.setMoMLFilter()
method, it will cause MoMLParser to filter so that models from
earlier releases will run in the current release.

<p>This class will filter for actors that have had port name changes, and
for classes with property where the class name has changed

@author Christopher Hylands, Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class PropertyClassChanges implements MoMLFilter {

    /**  If the attributeName is "class" and attributeValue names a
     *        class that has had its port names changed between releases,
     *  then substitute in the new port names.
     *
     *  @param container  The container for this attribute.
     *  in this method.
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


        if (attributeName.equals("name")) {
            // Save the name of the for later use if we see a "class"
            _lastNameSeen = attributeValue;
            if (_currentlyProcessingActorWithPropertyClassChanges) {
                if (_propertyMap.containsKey(attributeValue)) {
                    // We will do the above checks only if we found a
                    // class that had property class changes.
                    _newClass = (String)_propertyMap.get(attributeValue);
                } else {
                    // Saw a name that did not match.
                    // However, we might have other names that
                    // did match, so keep looking
                    //_currentlyProcessingActorWithPropertyClassChanges = false;
                    _newClass = null;
                }
            }
        }

        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.

        if (attributeName.equals("class")) {
            if (_actorsWithPropertyClassChanges
                    .containsKey(attributeValue)) {
                // We found a class with a property class change.
                _currentlyProcessingActorWithPropertyClassChanges = true;
                _currentActorFullName = container.getFullName()
                    + "." + _lastNameSeen;
                _propertyMap =
                    (HashMap) _actorsWithPropertyClassChanges
                    .get(attributeValue);
            } else if (_currentlyProcessingActorWithPropertyClassChanges
                    && _newClass != null) {

                // We found a property class to change, and now we
                // found the class itself that needs changing.

                // Only return the new class once, but we might
                // have other properties that need changing
                //_currentlyProcessingActorWithPropertyClassChanges = false;

                String temporaryNewClass = _newClass;
		if (!attributeValue.equals(_newClass)) {
		    MoMLParser.setModified(true);
		}
                _newClass = null;
                return temporaryNewClass;
            } else if (  _currentlyProcessingActorWithPropertyClassChanges
                    && container != null
                    && !container.getFullName()
                    .equals(_currentActorFullName)
                    && !container.getFullName()
                    .startsWith(_currentActorFullName)) {
                // We found another class in a different container
                // while handling a class with port name changes
                _currentlyProcessingActorWithPropertyClassChanges = false;
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Map of actor names a HashMap of property names to new classes.
    private static HashMap _actorsWithPropertyClassChanges;

    // The the full name of the actor we are currently processing
    private static String _currentActorFullName;

    // Set to true if we are currently processing an actor with parameter
    // class changes, set to false when we are done.
    private static boolean
    _currentlyProcessingActorWithPropertyClassChanges = false;

    // Last "name" value seen, for use if we see a "class".
    private static String _lastNameSeen;

    // The new class name for the property we are working on.
    private static String _newClass;

    // Cache of map from old property names to new class names for
    // the actor we are working on.
    private static HashMap _propertyMap;

    static {
        ///////////////////////////////////////////////////////////
        // Actors that have properties that have changed class.
        _actorsWithPropertyClassChanges = new HashMap();

        // AudioReader
        HashMap sourceURLClassChanges = new HashMap();
        // Key = property name, Value = new class name
        sourceURLClassChanges.put("sourceURL", "ptolemy.data.expr.Parameter");

        _actorsWithPropertyClassChanges
            .put("ptolemy.actor.lib.javasound.AudioReader",
                    sourceURLClassChanges);

        // ImagePartition
        HashMap inputOutputTypedIOPortClassChanges = new HashMap();
        inputOutputTypedIOPortClassChanges.put("input",
                "ptolemy.actor.TypedIOPort");
        inputOutputTypedIOPortClassChanges.put("output",
                "ptolemy.actor.TypedIOPort");

        _actorsWithPropertyClassChanges
            .put("ptolemy.domains.sdf.lib.vq.ImagePartition",
                    inputOutputTypedIOPortClassChanges);


        // ImageUnpartition
        _actorsWithPropertyClassChanges
            .put("ptolemy.domains.sdf.lib.vq.ImageUnpartition",
                    inputOutputTypedIOPortClassChanges);

        // HTVQEncode
        _actorsWithPropertyClassChanges
            .put("ptolemy.domains.sdf.lib.vq.HTVQEncode",
                    inputOutputTypedIOPortClassChanges);

        // VQDecode
        _actorsWithPropertyClassChanges
            .put("ptolemy.domains.sdf.lib.vq.VQDecode",
                    inputOutputTypedIOPortClassChanges);
    }
}
