/* Exception for domain incompatibility in mappings.

 Copyright (c) 2002 The University of Maryland.
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

@ProposedRating Red (shahrooz@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.graph.mapping;

//////////////////////////////////////////////////////////////////////////
//// DomainException
/**
Exception for domain incompatibility in mappings.

@since Ptolemy II 2.0
@author Shahrooz Shahparnia
@version $Id$
*/
public class DomainException extends RuntimeException {

    /** The default constructor without arguments.
     */
    public DomainException() {}

    /** Constructor with a text description as argument.
     *
     *  @param message The text description of the exception.
     */
    public DomainException(String message) {
        super(message);
    }
}

