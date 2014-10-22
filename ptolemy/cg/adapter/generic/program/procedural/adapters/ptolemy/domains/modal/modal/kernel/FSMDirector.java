/* Code generator helper for FSMDirector.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.modal.kernel;

import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////FSMDirector

/**
Code generator helper for FSMDirector.

@author Shanna-Shaye Forbes
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating red (sssf)
@Pt.AcceptedRating red (sssf)
 */
public class FSMDirector
        extends
        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.kernel.FSMDirector {

    /** Construct the code generator adapter associated with the given
     *  FSMDirector.
     *  @param director The associated
     *  ptolemy.domains.modal.kernel.FSMDirector
     */
    public FSMDirector(ptolemy.domains.modal.kernel.FSMDirector director) {
        super(director);
    }

    @Override
    public String generateFireCode() {
        StringBuffer code = new StringBuffer();
        code.append(_eol + "//generate fire code in the fsm director called"
                + _eol);
        return code.toString();
    }

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;
    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

}
