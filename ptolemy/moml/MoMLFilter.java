/* A filter for  MoML (modeling markup language)

Copyright (c) 2002-2005 The Regents of the University of California.
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
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public interface MoMLFilter {
    /** Given a container, attribute name and attribute value,
     *  return a new attribute value.  Note that "attribute"
     *  means XML attribute, not Ptolemy II attribute. Also,
     *  the container is the context of the current of XML
     *  element.  So, for example, if you have:
     *  <pre>
     *    &lt;entity name="foo" class="..."&gt;
     *       &lt;property name="x" value="10"/&gt;
     *    &lt;/entity&gt;
     *  </pre>
     *  then this method will be called twice with the container
     *  being the instance "foo". On the first call, the
     *  <i>attributeName</i> will be "name" and the
     *  <i>attributeValue</i> will be "x".  On the second call,
     *  <i>attributeName</i> will be "value" and the
     *  <i>attributeValue</i> will be "10".
     *  To make no change to the attribute value, an implementer
     *  should simply return the same attributeValue.
     *  To cause the MoMLParser to ignore the current element
     *  altogether, an implementer should return null. For
     *  example, to skip a graphical class, create a filter that
     *  looks for <i>attributeName</i> equal to "class" and
     *  <i>attributeValue</i> equal to the class name to skip.
     *  Note that if the <i>attributeValue</i> argument is null,
     *  then returning null is interpreted as no change, rather than
     *  as an indication to skip the element.
     *  To change the value of the attribute, simply return a
     *  a new value for the attribute.
     *  <p>
     *  If modifies the attribute value, then it should call
     *  the static method MoMLParser.setModified(true), which indicates
     *  that the model was modified so that the user can optionally
     *  save the modified model.
     *
     *  @param container  The container for XML element.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @return A new value for the attribute, or the same value
     *   to leave it unchanged, or null to cause the current element
     *   to be ignored (unless the attributeValue argument is null).
     */
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue);

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
     */
    public void filterEndElement(NamedObj container, String elementName)
            throws Exception;

    /** Return a string that describes what the filter does.
     *  @return A description of the filter (ending with a newline).
     */
    public String toString();
}
