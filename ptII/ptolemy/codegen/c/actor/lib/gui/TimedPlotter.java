//copied from XYPlotter.

/* A helper class for ptolemy.actor.lib.gui.TimedPlotter

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

import java.util.ArrayList;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// TimedPlotter

/**
 A helper class for ptolemy.actor.lib.gui.TimedPlotter.

 @author Shanna-Shaye Forbes
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red
 @Pt.AcceptedRating Red
 */
public class TimedPlotter extends PlotterBase {

    /** Constructor method for the TimedPlotter helper.
     *  @param actor the associated actor.
     */
    public TimedPlotter(ptolemy.actor.lib.gui.TimedPlotter actor) {
        super(actor);
    }

    /** Generate fire code.
     *  @return The generated code.
     *  @exception IllegalActionException If the code stream encounters
     *   errors in processing the specified code blocks.
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        // code.append(super.generateFireCode());
        ptolemy.actor.lib.gui.TimedPlotter actor = (ptolemy.actor.lib.gui.TimedPlotter) getComponent();
        if (actor.getAttribute("_top") != null) {
            code.append(generatePlotFireCode(actor.input.getWidth(), 1));
        } else if (actor.getAttribute("_bottom") != null) {
            code.append(generatePlotFireCode(actor.input.getWidth(), 2));
        } else {
            code.append(generatePlotFireCode(actor.input.getWidth(), 0));
        }
        return code.toString();
    }

    /** Generate plot specific fire code.
     *  @param width The width.
     *  @param id The identifier of the plotBlock to be used, see TimedPlotter.c
     *  @return The generated code.
     *  @exception IllegalActionException If the code stream encounters
     *   errors in processing the specified code blocks.
     */
    public String generatePlotFireCode(int width, int id)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        ArrayList args = new ArrayList();
        for (int i = width - 1; i >= 0; i--) {
            args.clear();
            args.add(Integer.valueOf(i));
            if (id == 1) {
                code.append(_generateBlockCode("plotBlock1", args));
            } else if (id == 2) {
                code.append(_generateBlockCode("plotBlock2", args));
            } else {
                code.append(_generateBlockCode("plotBlock", args));
            }
        }

        return code.toString();
    }

}
