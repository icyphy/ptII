/* Filter that adds _icon to actors when necessary

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

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

import java.util.HashMap;

//////////////////////////////////////////////////////////////////////////
//// FilterAddIcons
/** When this class is registered with the MoMLParser.addMoMLFilter()
method, it will cause MoMLParser to add a _icon property
for certain actors.

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class FilterAddIcons implements MoMLFilter {

    /** If the attributeName is "class" and attributeValue names a
     *	class that has should have an _icon, then add the appropriate
     *  _icon.
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

	if (attributeName.equals("class")) {
	    // Look for lines like:
	    // <entity name="Const" class="ptolemy.actor.lib.Const">

	    if (_actorsThatShouldHaveIcons.containsKey(attributeValue)) {
		_currentlyProcessingActorThatMayNeedAnIcon = true;
		if (container != null ) {
		    _currentActorFullName = container.getFullName()
			+ "." + _lastNameSeen;
		} else {
		    _currentActorFullName = "." + _lastNameSeen;
		}
		_iconMoML =
		    (String) _actorsThatShouldHaveIcons.get(attributeValue);

		_debug("filterAttributeValue: saw class: "
                        + _currentActorFullName);
	    }
	} else if (attributeName.equals("name")) {
	    // Save the name of the for later use if we see a "class"
	    _lastNameSeen = attributeValue;
	    if (_currentlyProcessingActorThatMayNeedAnIcon &&
                    attributeValue.equals("_icon")) {
		// We are processing an annotation and it already
		// has _icon
		_currentlyProcessingActorThatMayNeedAnIcon = false;
		_debug("filterAttributeValue: saw _icon");
	    }
	}
	if ( _currentlyProcessingActorThatMayNeedAnIcon
                && container != null
                && !container.getFullName()
                .equals(_currentActorFullName)
                && !_currentActorFullName
                .startsWith(container.getFullName())
                && !container.getFullName()
                .startsWith(_currentActorFullName)) {

	    // We found another class in a different container
	    // while handling an class that might need an icon

	    _debug("filterAttributeValue: return3 a different container "
                    + container.getFullName() + " "
                    + _currentActorFullName);
	    _currentlyProcessingActorThatMayNeedAnIcon = false;
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
	_debug("filterEndElement: " + container + "\t" + elementName);
	if (!elementName.equals("entity")) {
	    return elementName;
	}
	_debug("filterEndElement2: "
	       + _currentlyProcessingActorThatMayNeedAnIcon + " " 
	       + _currentActorFullName);
	if ( _currentlyProcessingActorThatMayNeedAnIcon
	     && container != null
	     && container.getFullName().equals(_currentActorFullName)) {
	    _currentlyProcessingActorThatMayNeedAnIcon = false;

	    if (_parser == null) {
		_parser = new MoMLParser();
	    } else {
		_parser.reset();
	    }
	    _parser.setContext(container);
	    try {
		_debug("container.getAttribute(_icon)"
		       + container.getAttribute("_icon"));
		NamedObj icon = _parser.parse(_iconMoML);
		_debug("filterEndElement: added " + icon + "\n"
		       + icon.exportMoML());
	    } catch (Exception ex) {
		throw new IllegalActionException(null, ex, "Failed to parse\n"
						 + _iconMoML);
	    }
	}
	return elementName;
    }

    // FIXME: this should go away
    private void _debug(String printString) {
	//System.out.println(printString);
    }

    // Map of actors that should have _icon
    private static HashMap _actorsThatShouldHaveIcons;

    private boolean _currentlyProcessingActorThatMayNeedAnIcon = false;

    // The the full name of the actor we are currently processing
    private static String _currentActorFullName;

    // Last "name" value seen, for use if we see a "class".
    private static String _lastNameSeen;

    // The moml that we should substitute in.
    private static String _iconMoML;

    private static MoMLParser _parser;

    static {
	///////////////////////////////////////////////////////////
	// Actors that should have _icon
	_actorsThatShouldHaveIcons = new HashMap();

	// In alphabetic order by actor class name.
	_actorsThatShouldHaveIcons.put("ptolemy.actor.lib.Const",
				       "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">\n" 
				       + "<property name=\"attributeName\" value=\"value\"/>\n"
				       + "<property name=\"displayWidth\" value=\"40\"/>\n"
				       + "</property>\n");

	String functionIcon =
	    "<property name=\"_icon\" class=\"ptolemy.vergil.icon.AttributeValueIcon\">\n"
	    + "<property name=\"attributeName\" value=\"function\"/>\n"
	    + "</property>\n";

	_actorsThatShouldHaveIcons.put("ptolemy.actor.lib.MathFunction",
				       functionIcon);

	_actorsThatShouldHaveIcons.put("ptolemy.actor.lib.Scale",
				       "<property name=\"_icon\" class=\"ptolemy.vergil.icon.AttributeValueIcon\">\n"
				       + "<property name=\"attributeName\" value=\"factor\"/>\n"
				       + "</property>\n");

	_actorsThatShouldHaveIcons.put("ptolemy.actor.lib.TrigFunction",
				       functionIcon);
    }
}

