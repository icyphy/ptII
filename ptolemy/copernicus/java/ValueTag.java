/* A tag that references an object.

 Copyright (c) 2001-2003 The Regents of the University of California.
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


package ptolemy.copernicus.java;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

import soot.SootField;
//////////////////////////////////////////////////////////////////////////
//// ValueTag
/**
A tag that references an object.  This tag is usually used
to store the constant runtime value of a field, if that
value can be statically determined.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ValueTag implements Tag {

    /** Construct a new tag that refers to the given object.
     */
    public ValueTag(Object object) {
        _object = object;
    }

    /** Return the name of the tag.
     */
    public String getName() {
        return "_CGValue";
    }

    /** Return the value of the tag.
     */
    public Object getObject() {
        return _object;
    }

    /** Returns the tag raw data.
     */
    public byte[] getValue() throws AttributeValueException {
        return new byte[0];
    }

    /** Return the object of the tag of the given field.
     *  If the field does not have a value tag, then return null.
     */
    public static Object getFieldObject(SootField field) {
        ValueTag tag = (ValueTag)field.getTag("_CGValue");
        if (tag == null) {
            return null;
        } else {
            return tag.getObject();
        }
    }

    private Object _object;

}
