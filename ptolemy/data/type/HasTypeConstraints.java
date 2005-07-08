/** Interface for objects with types.

 Copyright (c) 2003-2005 The Regents of the University of California.
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

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// HasTypeConstraints

/**
 Interface for objects that have type constraints.  Type constraints
 are represented as inequalities between Typeable objects.

 @author Yuhong Xiong, Xiaojun Liu, Edward A. Lee, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.graph.InequalityTerm
 */
public interface HasTypeConstraints {
    /** Return the type constraints of this object.
     *  The constraints are a list of inequalities.
     *  @return a list of instances of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList();
}
