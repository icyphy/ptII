/* A filter for  MoML (modeling markup language)

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

package ptolemy.moml;

import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// MoMLFilter
/**
This class filters MoML (modeling markup language) identifiers.
It can be used to
<menu>
<li> Remove graphical classes for use in a non-graphical environment
<li> Change the names of actors and ports for backward compatibility.
<menu>
@author Christopher Hylands, Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public interface MoMLFilter {
    /** Given the container, attribute name and attribute value,
     *  return a new attribute value.
     *  Usually, the attribute value returned is the same as
     *  the attribute value passed in.  However, it is possible to
     *  return a different attribute value, which results in renaming
     *  the attribute; or it is possible to return null, which will
     *  cause MoMLParser.attribute() to skip the rest of the current element.
     *
     *  <p>If this method is going to return a different attribute name, then
     *  it should call MoMLParser.setModified(true) which indicates
     *  that the model was modified so that the user can optionally
     *  save the modified model.
     *
     *  @param container  The container for this attribute, ignored
     *  in this method.
     *  @param attributeName The name of the attribute, ignored
     *  in this method.
     *  @param attributeValue The value of the attribute.
     *  @return the filtered attributeValue or null if we are to
     *  skip the current attribute.
     */
    public String filterAttributeValue(NamedObj container,
            String attributeName, String attributeValue);

    /** Given the elementName, perform any filter operations
     *  that are appropriate for the MOMLParser.endElement() method.
     *
     *  <p>If this method is going to return a different attribute name, then
     *  it should call MoMLParser.setModified(true) which indicates
     *  that the model was modified so that the user can optionally
     *  save the modified model.
     *
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param elementName The element type name.
     *  @return the filtered element name, or null if
     *  MoMLParser.endElement() should immediately return.
     */
    public String filterEndElement(NamedObj container, String elementName)
            throws Exception;

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a new line. 
     */
    public String toString();
}
