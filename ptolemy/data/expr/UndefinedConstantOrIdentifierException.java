/* Exception thrown on an attempt to evaluate an expression that
   contains an unknown constant or identifier

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// UndefinedConstantOrIdentifer

/**
 Thrown on an attempt to evaluate an expression that contains an
 unknown constant or identifier.

 <p>This exception is used to catch missing constants or identifiers
 during cut and paste operations by {@link ptolemy.data.expr.ParseTreeEvaluator#visitLeafNode(ptolemy.data.expr.ASTPtLeafNode)}.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class UndefinedConstantOrIdentifierException extends
IllegalActionException {
    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument.
     *  @param nodeName The name of the missing constant or identifier
     */
    public UndefinedConstantOrIdentifierException(String nodeName) {
        super("The ID " + nodeName + " is undefined.");
        _nodeName = nodeName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the node name that caused the exception.
     *  @return the name of the unidentified constant or identifier
     *  that caused the exception.
     */
    public String nodeName() {
        return _nodeName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The name of the missing constant or identifier.
     */
    private String _nodeName;
}
