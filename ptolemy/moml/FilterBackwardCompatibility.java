/* Filter for backward compatibility

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

package ptolemy.moml;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

import java.util.HashMap;

//////////////////////////////////////////////////////////////////////////
//// FilterBackwardCompatibility
/** When this class is registered with the MoMLParser.setMoMLFilter()
method, it will cause MoMLParser to filter so that models from
earlier releases will run in the current release.

<p>This class will filter for actors that have had port name changes, and
for classes with property where the class name has changed

@author Christopher Hylands, Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class FilterBackwardCompatibility implements MoMLFilter {

    /**  If the attributeName is "class" and attributeValue names a
     *	class that has had its port names changed between releases,
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

        _debug("filterAttributeValue: " + container + "\t"
                +  attributeName + "\t" + attributeValue);

        if (attributeValue == null) {
	    // attributeValue == null is fairly common, so we check for
	    // that first
            return null;
	}
	if (attributeName.equals("name")) {
	    // Save the name of the for later use if we see a "class"
	    _lastNameSeen = attributeValue;
	}
	// The code below is more complicated than perhaps it
	// should be because we do all the backward compatibility
	// changes in one pass.  An alternative would be to have
	// a pass for each time of change, but this would result
	// in a slower system.  In the code below, we try
	// to nest comparisons so as to make as few comparisons as
	// possible, and we try to compare against booleans first
	// so as to avoid more expensive string comparisons.

	if (attributeName.equals("class")) {
	    // Look for lines like:
	    // <entity name="ComplexToCartesian1"
	    //   class="ptolemy.actor.lib.conversions.ComplexToCartesian">

	    if (_actorsWithPortNameChanges.containsKey(attributeValue)) {
		// We found a class with a port name change.
		_currentlyProcessingActorWithPortNameChanges = true;
		_doneProcessingActorWithPortNameChanges  = false;
		_currentActorFullName = container.getFullName()
		    + "." + _lastNameSeen;
		_portMap = (HashMap) _actorsWithPortNameChanges.get(attributeValue);
		_debug("filterAttributeValue: not return saw1 "
                        + _currentActorFullName);
	    } else if (_actorsWithPropertyClassChanges
                    .containsKey(attributeValue)) {
		// We found a class with a property class change.
		_currentlyProcessingActorWithPropertyClassChanges = true;
		_doneProcessingActorWithPortNameChanges  = false;
		_currentActorFullName = container.getFullName()
		    + "." + _lastNameSeen;
		_propertyMap =
		    (HashMap) _actorsWithPropertyClassChanges
		    .get(attributeValue);
		_debug("filterAttributeValue: not return saw2 "
                        + _currentActorFullName);

	    } else if (_currentlyProcessingActorWithPropertyClassChanges
                    && _newClass != null) {
		// We found a property class to change, and now we found the
		// class itself that needs changing.
		_debug("filterAttributeValue: return0 "
                        + _newClass);
		// Only return the new class once, but we might
		// have other properties that need changing
		//_currentlyProcessingActorWithPropertyClassChanges = false;

		String temporaryNewClass = _newClass;
		_newClass = null;
		return temporaryNewClass;
	    } else if ( (_currentlyProcessingActorWithPortNameChanges
                    || _currentlyProcessingActorWithPropertyClassChanges)
                    && container != null
                    && !container.getFullName()
                    .equals(_currentActorFullName)
                    && !container.getFullName()
                    .startsWith(_currentActorFullName)) {

		// We found another class in a different container
		// while handling a class with port name changes, so
		// set _doneProcessingActorWithPortNameChanges so we
		// can handle any port changes later.

		_debug("filterAttributeValue: return3 saw class, resetting: "
                        + container.getFullName() + " "
                        + _currentActorFullName);
		_currentlyProcessingActorWithPortNameChanges = false;
		_currentlyProcessingActorWithPropertyClassChanges = false;
		_doneProcessingActorWithPortNameChanges  = true;
		_currentActorFullName = null;
		_portMap = null;
	    }
	} else if (_currentlyProcessingActorWithPortNameChanges
                && attributeName.equals("name")
                && _portMap != null
                && _portMap.containsKey(attributeValue)) {
	    // We will do the above checks only if we found a
	    // class that had port name changes, but have not
	    // yet found the next class.

	    // Here, we add the port name and the new port name
	    // to a map for later use.

	    String containerName =
		container.getFullName();

	    String newPort = (String)_portMap.get(attributeValue);

	    // Save the container.newPort name for later use.
	    _containerPortMap.put(containerName + "." + attributeValue,
                    containerName + "." + newPort
				  );
	    _debug("filterAttributeValue: return1 ("
                    + containerName + "." + attributeValue + ", "
                    +  containerName + "." + newPort + ") "
                    + newPort);
	    return newPort;
	} else if (_currentlyProcessingActorWithPropertyClassChanges) {
	    if (attributeName.equals("name")) {
		if (_propertyMap.containsKey(attributeValue)) {
		    // We will do the above checks only if we found a
		    // class that had property class changes.
		    _newClass = (String)_propertyMap.get(attributeValue);
		    _debug("filterAttributeValue: not return _newClass = "
                            + _newClass);
		} else {
		    _debug("filterAttributeValue: not return '"
                            + attributeValue + "' did not match");
		    // Saw a name that did not match.
		    // However, we might have other names that
		    // did match, so keep looking
		    //_currentlyProcessingActorWithPropertyClassChanges = false;
		    _newClass = null;
		}
	    }
	} else if (_doneProcessingActorWithPortNameChanges
                && attributeName.equals("port")
                && _containerPortMap.containsKey(container.getFullName()
                        + "." + attributeValue)) {
	    // We are processing actors that have port names.
	    // Now map the old port to the new port.
	    String newPort = (String)_containerPortMap
		.get(container.getFullName() + "." + attributeValue);

	    // Extreme chaos here because sometimes
	    // container.getFullName() will be ".transform_2.transform" and
	    // attributeValue will be "ComplexToCartesian.real"
	    // and sometimes container.getFullName() will be
	    // ".transform_2.transform.ComplexToCartesian"
	    // and attributeValue will be "real"

	    newPort =
		newPort.substring(container.getFullName().length() + 1);

	    _debug("filterAttributeValue: return2 "
                    + newPort);
	    return newPort;
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
    ////                         private methods                   ////

    // FIXME: this should go away
    private void _debug(String printString) {
	//System.out.println(printString);
    }

    // Map of actor names a HashMap of old ports to new ports
    private static HashMap _actorsWithPortNameChanges;

    // Map of actor names a HashMap of property names to new classes.
    private static HashMap _actorsWithPropertyClassChanges;

    // Map of old container.port to new container.newPort;
    private static HashMap _containerPortMap;

    // The the full name of the actor we are currently processing
    private static String _currentActorFullName;

    // Set to true if we are currently processing an actor with parameter
    // class changes, set to false when we are done.
    private static boolean
    _currentlyProcessingActorWithPropertyClassChanges = false;

    // Set to true if we are currently processing an actor with port name
    // changes, set to false when we are done.
    private static boolean
    _currentlyProcessingActorWithPortNameChanges = false;

    // Set to true if we are done processing an actor.
    private static boolean _doneProcessingActorWithPortNameChanges = false;

    // Last "name" value seen, for use if we see a "class".
    private static String _lastNameSeen;

    // The new class name for the property we are working on.
    private static String _newClass;

    // Cache of map from old port names to new port names for
    // the actor we are working on.
    private static HashMap _portMap;

    // Cache of map from old property names to new class names for
    // the actor we are working on.
    private static HashMap _propertyMap;

    static {
	///////////////////////////////////////////////////////////
	// Actors with port name changes.
	_actorsWithPortNameChanges = new HashMap();
	_containerPortMap = new HashMap();

	// ComplexToCartesian: real is now x, imag is now y.
	HashMap cartesianPorts = new HashMap();
	cartesianPorts.put("real", "x");
	cartesianPorts.put("imag", "y");
	_actorsWithPortNameChanges
	    .put("ptolemy.actor.lib.conversions.ComplexToCartesian",
                    cartesianPorts);

	// CartesianToComplex has the same ports as ComplexToCartesian.
	_actorsWithPortNameChanges
	    .put("ptolemy.actor.lib.conversions.CartesianToComplex",
                    cartesianPorts);

        // Sleep
	HashMap sleepPorts = new HashMap();
        sleepPorts.put("delay", "sleepTime");
        _actorsWithPortNameChanges
	    .put("ptolemy.actor.lib.Sleep", sleepPorts);

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
