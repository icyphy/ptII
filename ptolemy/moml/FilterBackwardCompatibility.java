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

@author  Edward A. Lee, Christopher Hylands
@version $Id$
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

        if (attributeValue == null) {
	    // attributeValue == null is fairly common, so we check for
	    // that first
            return null;
	}
	if (attributeName.equals("name")) { 
	    // Save the name of the for later use if we see a "class"
	    _lastNameSeen = attributeValue;
	}
	if (attributeName.equals("class")) { 
	    if (_actors.containsKey(attributeValue)) {
		_currentlyProcessingActor = true;
		_doneProcessingActor = false;
		_currentActorFullName = container.getFullName()
		    + "." + _lastNameSeen;
		_portMap = (HashMap) _actors.get(attributeValue);
	    } else {
		if (_currentlyProcessingActor 
		    && !container.getFullName()
		    .equals(_currentActorFullName)) {

		    _currentlyProcessingActor = false;
		    _doneProcessingActor = true;
		    _currentActorFullName = null;
		    _portMap = null;
		}
	    }
	} else if (_currentlyProcessingActor
		   && attributeName.equals("name")
		   && _portMap.containsKey(attributeValue)) {
	    // We will do the above checks only if we found a
	    // class that had port name changes.

	    // FIXME: get rid of the leading .?
	    String containerName =
		container.getFullName().substring(2);

	    String newPort = (String)_portMap.get(attributeValue);

	    // Save the container.newPort name for later use.
	    _containerPortMap.put(containerName + "." + attributeValue,
				  containerName + "." + newPort
				  );
	    return newPort;
	} else if (_doneProcessingActor
		   && attributeName.equals("port")
		   && _containerPortMap.containsKey(attributeValue)) {
	    return (String)_containerPortMap.get(attributeValue);
	} 
        return attributeValue;
    } 


    // Map of actor names a HashMap of old ports to new ports
    private static HashMap _actors;

    // Map of old container.port to new container.newPort;
    private static HashMap _containerPortMap; 

    // The the full name of the actor we are currently processing
    private static String _currentActorFullName;

    // Set to true if we are currently processing an actor, set
    // to false when we are done.
    private static boolean _currentlyProcessingActor = false; 

    // Set to true if we are done processing an actor.
    private static boolean _doneProcessingActor = false;

    // Last "name" value seen, for use if we see a "class".
    private static String _lastNameSeen; 

    // Cache of map from old port names to new port names for
    // the actor we are working on.

    private static HashMap _portMap;

    static {
	_actors = new HashMap();
	_containerPortMap = new HashMap();

	HashMap ports = new HashMap();
	
	// ComplexToCartesian: real is now x, imag is now y.
	ports.put("real", "x");
	ports.put("imag", "y");
	_actors.put("ptolemy.actor.lib.conversions.ComplexToCartesian", ports);

	// CartesianToComplex has the same ports as ComplexToCartesian.
	_actors.put("ptolemy.actor.lib.conversions.CartesianToComplex", ports);
    }
}
