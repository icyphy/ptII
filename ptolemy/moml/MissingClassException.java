/* Exception thrown on an attempt to evaluate MoML that
   contains a missing class

 Copyright (c) 2007-2013 The Regents of the University of California.
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

import com.microstar.xml.XmlException;

///////////////////////////////////////////////////////////////////
//// UndefinedConstantOrIdentifer

/**
 Thrown on an attempt to evaluate MoML that
 contains a missing class.

 <p>This exception is used to catch missing classes in
 during cut and paste operations by {@link ptolemy.moml.MoMLParser}.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class MissingClassException extends XmlException {
    /** Construct a new XML parsing exception.
    }
    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument.
     * @param message The error message from the parser.
     * @param missingClassName The name of the missing class.
     * @param systemId The URI of the entity containing the error.
     * @param line The line number where the error appeared.
     * @param column The column number where the error appeared.
     */
    public MissingClassException(String message, String missingClassName,
            String systemId, int line, int column) {
        super(message, systemId, line, column, null);
        _missingClassName = missingClassName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the node name that caused the exception.
     *  @return the name of the unidentified constant or identifier
     *  that caused the exception.
     */
    public String missingClassName() {
        return _missingClassName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The name of the missing class.
     */
    private String _missingClassName;
}
