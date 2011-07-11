/* A StringParameter with Linear Temporal Logic formula for RTMaude.

 Copyright (c) 2009-2011 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.data.expr;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// PropertyParameter

/**
 * The PropertyParameter is a StringParameter with RTMaude LTL formula.
 * The contents is translated to related RTMaude model checking command.
 *
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class PropertyParameter extends StringParameter {
    /**
     * Constructs a PropertyParameter object, which contains
     * a string of a temporal logic formula.
     *
     * @param container  The container
     * @param name       The name of the parameter
     * @exception IllegalActionException   If the parameter is not of an
     *   acceptable class for the container
     * @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PropertyParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.StringParameter#stringValue()
     */
    public String stringValue() throws IllegalActionException {
        String timesign;
        String ret = super.stringValue();

        if (ret.matches(".*(with\\s+no\\s+time\\s+limit\\s*|in\\s+time\\s+(<|<=).*)")) {
            timesign = "t";
        } else {
            timesign = "u";
        }

        return "mc {init} |=" + timesign + " " + ret.replace("\\", "\\\\");
    }

}
