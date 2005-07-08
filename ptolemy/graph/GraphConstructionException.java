/* Exception of modifying graph in wrong ways.

 Copyright (c) 2002-2005 The University of Maryland.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.graph;

//////////////////////////////////////////////////////////////////////////
//// GraphConstructionException

/**
 The exception of modifying graph elements in wrong ways. The
 modification is due to the change of topology or improper weight
 association. Function evaluations incurred by graph reading do not
 cause this exception.

 @author Mingyung Ko, Shuvra S. Bhattacharyya
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (myko)
 @Pt.AcceptedRating Red (ssb)
 */
public class GraphConstructionException extends GraphException {
    /** Constructor with an argument of text description.
     *  @param message The exception message.
     */
    public GraphConstructionException(String message) {
        super(message);
    }
}
