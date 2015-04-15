/* Filter to convert specific multiports of an actor to a single port.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
import java.util.Map;
import java.util.Map.Entry;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// MultiportToSinglePort

/** A filter to convert specific multiports of specific
 actors to a single ports.

 <p>When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause will cause MoMLParser to filter so that models from
 earlier releases will run in the current release.

 <p>The Autocorrelation actor changed between PtolemyII 2.x and 3.x
 such that the output port is no longer a multiport.

 <pre>
 // Autocorrelation
 _actorsWithMultiPortToSinglePortChanges
 .put("ptolemy.actor.lib.Autocorrelation, "output")

 </pre>

 <p>The _actorsWithMultiPortToSinglePortChanges HashMap contains Strings
 that name classes such as Autocorrelation that have multiports that should
 be single ports.  The HashMap maps classnames to port names.


 <p> Conceptually, how the code works is that when we see a class while
 parsing, we check to see if the class is in
 _actorsWithMultiPortToSinglePortChanges.

 If the class was present in the HashMap, then as we go through the
 code, we look for the named port and remove the multiport declaration

 @author Christopher Hylands Brooks, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class MultiportToSinglePort extends MoMLFilterSimple {
    /** If the attributeName is "class" and attributeValue names a
     *  class that has had its port names changed between releases,
     *  then substitute in the new port names.
     *
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the value of the attributeValue argument.
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        //System.out.println("filterAttributeValue: " + container + "\t"
        //  +  attributeName + "\t" + attributeValue);
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
                if (_portName != null && _portName.equals(attributeValue)) {
                    // We will do the above checks only if we found a
                    // class that had property class changes.
                    //_foundChange = true;
                } else {
                    if (attributeValue.equals("multiport") && container != null
                            && container.getName().equals(_portName)) {
                        // What if the multiport is false?
                        //_foundChange = false;
                        MoMLParser.setModified(true);
                        return null;
                    }

                    // Saw a name that did not match.
                    // However, we might have other names that
                    // did match, so keep looking
                    //_foundChange = false;
                }
            }
        }

        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.
        if (attributeName.equals("class")) {
            if (_actorsWithMultiPortToSinglePortChanges
                    .containsKey(attributeValue)) {
                // We found a class with a property class change.
                _currentlyProcessingActorWithPropertyClassChanges = true;
                if (container == null) {
                    // Under certain circumstances, we can parse something
                    // that has no container
                    _currentActorFullName = "." + _lastNameSeen;
                } else {
                    _currentActorFullName = container.getFullName() + "."
                            + _lastNameSeen;
                }
                _portName = (String) _actorsWithMultiPortToSinglePortChanges
                        .get(attributeValue);

                //             } else if (_currentlyProcessingActorWithPropertyClassChanges
                //                     && _foundChange) {
                //                 // We found a property class to change, and now we
                //                 // found the class itself that needs changing.
                //                 // Only return the new class once, but we might
                //                 // have other properties that need changing
                //                 //_currentlyProcessingActorWithPropertyClassChanges = false;
                //                 if (!attributeValue.equals(_newClass)) {
                //                     MoMLParser.setModified(true);
                //                 }
                //                 _foundChange = false;
                //                 return attributeValue;
            } else if (_currentlyProcessingActorWithPropertyClassChanges
                    && container != null
                    && !container.getFullName().equals(_currentActorFullName)
                    && !container.getFullName().startsWith(
                            _currentActorFullName)) {
                // We found another class in a different container
                // while handling a class with multiport change
                //_foundChange = false;
                _currentlyProcessingActorWithPropertyClassChanges = false;
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
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Update any actor multiports that are now\n"
                + "single ports.\n"
                + "Below are the actors that are affected, along "
                + "with the port name:\n");
        //Iterator actors = _actorsWithMultiPortToSinglePortChanges.keySet()
        //.iterator();

        //while (actors.hasNext()) {
        for (Map.Entry<String,String> actor: _actorsWithMultiPortToSinglePortChanges.entrySet()) {
            //String actor = (String) actors.next();
            results.append("\t"
                    + actor.getKey()
                    + "\n"
                    //+ (String) _actorsWithMultiPortToSinglePortChanges.get(actor));
                    + actor.getValue());
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Map of actor names a HashMap of class names to multiport names
    // that should be single ports.
    private static HashMap<String, String> _actorsWithMultiPortToSinglePortChanges;

    // The the full name of the actor we are currently processing
    private String _currentActorFullName;

    // Set to true if we are currently processing an actor with parameter
    // class changes, set to false when we are done.
    private boolean _currentlyProcessingActorWithPropertyClassChanges = false;

    // Last "name" value seen, for use if we see a "class".
    private String _lastNameSeen;

    // Keep track of whether a change was found.
    //private static boolean _foundChange;

    // The name of the port we are looking for.
    private String _portName;

    static {
        ///////////////////////////////////////////////////////////
        // Actors that have ports that have changed from multi to single
        _actorsWithMultiPortToSinglePortChanges = new HashMap<String, String>();

        // Autocorrelation
        _actorsWithMultiPortToSinglePortChanges.put(
                "ptolemy.domains.sdf.lib.Autocorrelation", "output");

        _actorsWithMultiPortToSinglePortChanges.put(
                "ptolemy.actor.lib.NonStrictTest", "input");

        // In Ptolemy II 3.0.2, SOC_FSM_SR_HDE.xml has a Const with multiport.
        _actorsWithMultiPortToSinglePortChanges.put("ptolemy.actor.lib.Const",
                "output");

        _actorsWithMultiPortToSinglePortChanges.put(
                "ptolemy.actor.lib.gui.MatrixViewer", "input");
    }
}
