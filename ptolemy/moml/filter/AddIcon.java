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
//// AddIcon
/** When this class is registered with the MoMLParser.setMoMLFilter()
method, it will cause MoMLParser to filter so that models from
earlier releases will run in the current release.

<p>This class will filter for actors that have had port name changes, and
for classes with property where the class name has changed

@author Christopher Hylands, Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class AddIcon implements MoMLFilter {

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
	    if (_currentlyProcessingActorThatMayNeedAnIcon &&
                    attributeValue.equals("_icon")) {
		// We are processing an annotation and it already
		// has _icon
		_currentlyProcessingActorThatMayNeedAnIcon = false;
            }
	}
	//
	// If you change this class, you should run before and after
	// timing tests on large moml files, a good command to run
	// is:
	// $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
	// which will open up a large xml file and then close after 2 seconds.
	// If you place the above command in a file five times, you
	// can get averages with:
	// sh c:/tmp/timeit | awk '{sum += $4; print sum, sum/NR, $0}'

	if (attributeName.equals("class")) {
	    if (_actorsThatShouldHaveIcons
		.containsKey(attributeValue)) {
		// We found a class that needs an _icon
		_currentlyProcessingActorThatMayNeedAnIcon = true;
		if (container != null ) {
		    _currentActorFullName = container.getFullName()
			+ "." + _lastNameSeen;
		} else {
		    _currentActorFullName = "." + _lastNameSeen;
		}
		_iconMoML = (String) _actorsThatShouldHaveIcons
		    .get(attributeValue);
	    } else if ( _currentlyProcessingActorThatMayNeedAnIcon
			&& container != null
			&& !container.getFullName()
			.equals(_currentActorFullName)
			&& !container.getFullName()
			.startsWith(_currentActorFullName)) {
		// We found another class in a different container
		// while handling a class with port name changes, so
		_currentlyProcessingActorThatMayNeedAnIcon = false;
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
	if ( _currentlyProcessingActorThatMayNeedAnIcon
	     && elementName.equals("entity")
                && container != null
                && container.getFullName().equals(_currentActorFullName)) {

	    _currentlyProcessingActorThatMayNeedAnIcon = false;
	    if (_parser == null) {
		_parser = new MoMLParser();
	    }
	    // setContext calls parser.reset()
	    _parser.setContext(container);
	    try {
		// Do not call parse(_iconMoML) here, since that method
		// will fail if we are in an applet because it tries
		// to read user.dir
		NamedObj icon = _parser.parse(null, _iconMoML);
		MoMLParser.setModified(true);
	    } catch (Exception ex) {
		throw new IllegalActionException(null, ex, "Failed to parse\n"
						 + _iconMoML);
	    }
	}
	return elementName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Set of all actors that require updating.
    private static Set _actorsThatRequireUpdating;

    // We don't have a Map of actors that may need _editorFactory
    // because currently only ptolemy.data.expr.Parameters that
    // have a name="_icon" and a valueIcon need to have editorFactory
    // added

    // Map of actors that should have _icon
    private static HashMap _actorsThatShouldHaveIcons;

    // Map of actor names a HashMap of old ports to new ports
    private static HashMap _actorsWithPortNameChanges;

    // Map of actor names a HashMap of property names to new classes.
    private static HashMap _actorsWithPropertyClassChanges;

    // Map of old container.port to new container.newPort;
    private static HashMap _containerPortMap;

    // The the full name of the actor we are currently processing
    private static String _currentActorFullName;

    // Set to true if the current attribute has a _location attribute.
    // This variable is used to determine whether we need to add  a
    // _editorFactory.
    private static boolean _currentAttributeHasLocation = false;

    // Set to true if we are currently processing an actor with parameter
    // class changes, set to false when we are done.
    private static boolean
	_currentlyProcessingActorWithPropertyClassChanges = false;

    // Set to true if we are currently processing an actor with port name
    // changes, set to false when we are done.
    private static boolean
	_currentlyProcessingActorWithPortNameChanges = false;

    // Set to true if we are currently processing an actor that may
    // need _editorFactory added, set to false when we are done.
    private boolean
	_currentlyProcessingActorThatMayNeedAnEditorFactory = false;

    // Set to true if we are currently processing an actor that may
    // need _icon added, set to false when we are done.
    private boolean _currentlyProcessingActorThatMayNeedAnIcon = false;

    // Set to true if we are done processing an actor.
    private static boolean _doneProcessingActorWithPortNameChanges = false;

    // The moml that we should substitute in if we need to add
    // an _icon
    private static String _iconMoML;

    // Last "name" value seen, for use if we see a "class".
    private static String _lastNameSeen;

    // The new class name for the property we are working on.
    private static String _newClass;

    // The parser we use to parse the MoML when we add an _icon.
    private static MoMLParser _parser;

    // Cache of map from old port names to new port names for
    // the actor we are working on.
    private static HashMap _portMap;

    // Cache of map from old property names to new class names for
    // the actor we are working on.
    private static HashMap _propertyMap;

    // Set to true if we are currently processing an actor
    // that requires processing. Set to false once we are
    // done processing that actor.  This is done for performance reasons.
    private static boolean _currentlyProcessingActorThatRequiresUpdating =
	false;

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

	///////////////////////////////////////////////////////////
	// Actors that should have _icon
	_actorsThatShouldHaveIcons = new HashMap();

	// In alphabetic order by actor class name.
	_actorsThatShouldHaveIcons.put("ptolemy.actor.lib.Const",
                "<property name=\"_icon\" "
                + "class=\"ptolemy.vergil.icon.BoxedValueIcon\">\n"
                + "<property name=\"attributeName\" value=\"value\"/>\n"
                + "<property name=\"displayWidth\" value=\"40\"/>\n"
                + "</property>\n");

	String functionIcon =
	    "<property name=\"_icon\" "
            + "class=\"ptolemy.vergil.icon.AttributeValueIcon\">\n"
	    + "<property name=\"attributeName\" value=\"function\"/>\n"
	    + "</property>\n";

	_actorsThatShouldHaveIcons.put("ptolemy.actor.lib.MathFunction",
                functionIcon);

	_actorsThatShouldHaveIcons.put("ptolemy.actor.lib.Scale",
                "<property name=\"_icon\" "
                + "class=\"ptolemy.vergil.icon.AttributeValueIcon\">\n"
                + "<property name=\"attributeName\" value=\"factor\"/>\n"
                + "</property>\n");

	_actorsThatShouldHaveIcons.put("ptolemy.actor.lib.TrigFunction",
                functionIcon);

	///////////////////////////////////////////////////////////
	// We put all the actors that require updating so that we can
	// have only search for actors.
	_actorsThatRequireUpdating = new HashSet();

	// Add the entries from each of the HashMaps to the Set.
	_actorsThatRequireUpdating
	    .addAll(_actorsWithPortNameChanges.keySet());
	_actorsThatRequireUpdating
	    .addAll(_actorsWithPropertyClassChanges.keySet());
	_actorsThatRequireUpdating
	    .addAll(_actorsThatShouldHaveIcons.keySet());
	// ptolemy.data.expr.Parameters may need _editorFactory added.
	_actorsThatRequireUpdating.add("ptolemy.data.expr.Parameter");

    }
}
