/* A tag that references a ptolemy token type.

 Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.java;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

//////////////////////////////////////////////////////////////////////////
//// TypeTag

/**
 A tag that references a ptolemy token type.  This tag is used
 to store information regarding the resolved ptolemy type of a
 local or field.  Often this information is redundant (for
 instance, in the case of a local defined as a DoubleToken, the
 type can only be BaseType.DOUBLE).  However, in many cases
 the ptolemy type contains more information than java type (for
 instance, in the case of ArrayType).

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TypeTag implements Tag {
    /** Construct a new tag that refers to the given type.
     */
    public TypeTag(ptolemy.data.type.Type type) {
        _type = type;
    }

    /** Return the name of the tag.
     */
    public String getName() {
        return "_CGType";
    }

    /** Return the value of the tag.
     */
    public ptolemy.data.type.Type getType() {
        return _type;
    }

    /** Returns the tag raw data.
     */
    public byte[] getValue() throws AttributeValueException {
        return new byte[0];
    }

    private ptolemy.data.type.Type _type;
}
