/* Basic applet that constructs a Ptolemy II model from a MoML file.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;

//////////////////////////////////////////////////////////////////////////
//// MoMLApplet
/** 
@author  Edward A. Lee, Christopher Hylands
@version $Id$
*/

public class FilterOutGraphicalClasses implements MoMLFilter {
        
    /** If the attributeValue is "ptolemy.vergil.icon.AttributeValueIcon",
     *  then return "ptolemy.kernel.util.Attribute", otherwise
     *  return the original value of the attributeValue.
     *  @param container  The container for this attribute, ignored
     *  in this method.
     *  @param attributeName The name of the attribute, ignored
     *  in this method.
     *  @param attributeValue The value of the attribute.
     *  @return the filtered attributeValue;
     */
    public String filterAttributeValue(NamedObj container,
            String attributeName, String attributeValue) {
        if (attributeValue != null
		&& attributeValue
		.equals("ptolemy.vergil.icon.AttributeValueIcon")) {
            return "ptolemy.kernel.util.Attribute";
        } 
        return attributeValue;
    }
}
