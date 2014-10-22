/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009-2014 The Regents of the University of California.
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
 */
package ptolemy.verification.kernel.maude;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// RTMPtTerm

/**
 * A Real-Time Maude ptolemy term.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class RTMPtExp extends RTMFragment {

    public RTMPtExp(String exp) throws IllegalActionException {
        this(exp, false);
    }

    public RTMPtExp(ASTPtRootNode root) throws IllegalActionException {
        this(root, false);
    }

    public RTMPtExp(ASTPtRootNode root, boolean isTime)
            throws IllegalActionException {
        super(null);
        RTMExpTranslator rt = new RTMExpTranslator(isTime);
        this.frag = rt.translateParseTree(root);
    }

    public RTMPtExp(String exp, boolean isTime) throws IllegalActionException {
        super(exp);
        RTMExpTranslator rt = new RTMExpTranslator(isTime);
        this.frag = rt.translateExpression(exp);
    }

    public String getValue() throws IllegalActionException {
        String g = this.frag.trim();
        if ((g.startsWith("#r(") || g.startsWith("#f(") || g.startsWith("#b("))
                && g.endsWith(")")) {
            return g.substring(3, g.length() - 1);
        } else {
            throw new IllegalActionException("Not Value!");
        }
    }
}
