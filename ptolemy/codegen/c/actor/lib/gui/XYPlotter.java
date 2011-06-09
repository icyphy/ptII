/* A helper class for ptolemy.actor.lib.gui.XYPlotter

 Copyright (c) 2006-2009 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

package ptolemy.codegen.c.actor.lib.gui;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// XYPlotter

/**
 A helper class for ptolemy.actor.lib.gui.XYPlotter.

 @author Gang Zhou, contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class XYPlotter extends PlotterBase {

    /** Constructor method for the XYPlotter helper.
     *  @param actor the associated actor.
     */
    public XYPlotter(ptolemy.actor.lib.gui.XYPlotter actor) {
        super(actor);
    }

    /** Generate fire code.
     *  @return The generated code.
     *  @exception IllegalActionException If the code stream encounters
     *   errors in processing the specified code blocks.
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());
        ptolemy.actor.lib.gui.XYPlotter actor = (ptolemy.actor.lib.gui.XYPlotter) getComponent();
        code.append(generatePlotFireCode(actor.inputX.getWidth()));
        return code.toString();
    }
}
