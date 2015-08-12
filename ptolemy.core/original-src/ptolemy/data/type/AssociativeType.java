/* The type of structures that store (key, value) pairs.

Copyright (c) 1997-2014 The Regents of the University of California.
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

package ptolemy.data.type;

///////////////////////////////////////////////////////////////////
////AssociativeType

/**
This class represents structures that store (key, value) pairs. It prescribes
a get() method that retrieves the type of the value associated with a given
key.

@author Marten Lohstroh
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (marten)
@Pt.AcceptedRating Red
 */
public abstract class AssociativeType extends StructuredType {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Return the type of the specified label. If this type does not
     *  contain the specified label, return null.
     *  @param label The specified label.
     *  @return a Type.
     */
    public abstract Type get(String label);
}
