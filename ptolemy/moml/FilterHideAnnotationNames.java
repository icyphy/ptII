/* Filter that adds _hideName to annotations

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
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

import java.util.HashMap;

//////////////////////////////////////////////////////////////////////////
//// FilterHideAnnotationNames
/** When this class is registered with the MoMLParser.addMoMLFilter()
method, it will cause MoMLParser to add a _hideName property
property for any annotations.

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class FilterHideAnnotationNames implements MoMLFilter {

    /** If the attributeName is "name" and attributeValue ends
     *	with "annotation", then
     *  <pre>
     *   <property name="_hideName" class="ptolemy.data.expr.Parameter">
     *   </property>
     *  <pre>
     *  is added if it is not yet present.
     *
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @return the value of the attributeValue argument.
     */
    public String filterAttributeValue(NamedObj container,
            String attributeName, String attributeValue) {

        //_debug("filterAttributeValue: " + container + "\t"
        //        +  attributeName + "\t" + attributeValue);

        if (attributeValue == null) {
	    // attributeValue == null is fairly common, so we check for
	    // that first
            return null;
	}

	if (attributeName.equals("name")) {
	    if (attributeValue.endsWith("annotation1")) {
		// We found a line like
		// <property name="13:0:0:annotation1"
		//          class="ptolemy.kernel.util.Attribute">

		_currentlyProcessingAnnotation = true;
		_currentAnnotationFullName = container.getFullName()
		    + "." + attributeValue;
		//_debug("filterAttributeValue: saw annotation1: "
                //        + _currentAnnotationFullName);

	    } else if (_currentlyProcessingAnnotation &&
                    attributeValue.equals("_hideName")) {
		// We are processing an annotation and it already
		// has _hideName
		_currentlyProcessingAnnotation = false;
		//_debug("filterAttributeValue: saw _hideName");
	    }
	}
	if ( _currentlyProcessingAnnotation
                && container != null
                && !container.getFullName()
                .equals(_currentAnnotationFullName)
                && !_currentAnnotationFullName
                .startsWith(container.getFullName())
                && !container.getFullName()
                .startsWith(_currentAnnotationFullName)) {

	    // We found another class in a different container
	    // while handling an annotation.

	    //_debug("filterAttributeValue: return3 a different container "
            //        + container.getFullName() + " "
            //        + _currentAnnotationFullName);
	    _currentlyProcessingAnnotation = false;
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
	//_debug("filterEndElement: " + container + "\t" + elementName);
	if (!elementName.equals("property")) {
	    return elementName;
	}
	if ( _currentlyProcessingAnnotation
                && container.getFullName()
                .equals(_currentAnnotationFullName)) {
	    _currentlyProcessingAnnotation = false;

	    //<property name="_hideName" class="ptolemy.data.expr.Parameter">
	    //</property>
	    Parameter hideName = new Parameter(container, "_hideName");

	    //_debug("filterEndElement: added " + hideName + "\n"
            //     + hideName.exportMoML());
	}
	return elementName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // FIXME: this should go away
    //private void _debug(String printString) {
	//System.out.println(printString);
    //}

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    // True if we are currently processing an annotation. 
    private boolean _currentlyProcessingAnnotation = false;

    // The the full name of the annotation we are currently processing
    private static String _currentAnnotationFullName;
}

