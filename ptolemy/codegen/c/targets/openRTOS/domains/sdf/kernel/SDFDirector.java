/* Code generator helper class associated with the GiottoDirector class.

 Copyright (c) 2005-2010 The Regents of the University of California.
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
package ptolemy.codegen.c.targets.openRTOS.domains.sdf.kernel;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// SDFDirector

/**
 Code generator helper associated with the SDFDirector class. This class
 is also associated with a code generator.

 @author Shanna-Shaye Forbes, Man-Kit Leung, Ben Lickly
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (sssf)
 @Pt.AcceptedRating Red (sssf)
 */
public class SDFDirector extends
        ptolemy.codegen.c.domains.sdf.kernel.SDFDirector {

    /** Construct the code generator helper associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the worst case execution time (WCET) seen by this
     * director.
     * @return The Worst Case Execution Time (WCET).
     * @exception IllegalActionException If there is a problem determining
     * the WCET or a problem accessing the model.
     */
    public double getWCET() throws IllegalActionException {
        // go through all my actors and get their WCET and multiply that by the firing count
        // for now assume that each actor is fired once
        double wcet = 0;
        double actorFrequency = 0;
        double actorWCET = 0;
        int actorCount = 0;
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) _director
                .getContainer()).deepEntityList()) {
            actorCount++;
            Attribute frequency = ((Entity) actor).getAttribute("frequency");
            Attribute WCET = ((Entity) actor).getAttribute("WCET");

            if (actor instanceof CompositeActor) {
                if (_debugging) {
                    _debug("Composite Actor in SDFDirector, if it has a director I need to ask it for it's WCET");
                }
            } else {

                if (frequency == null) {
                    actorFrequency = 1;
                } else {
                    actorFrequency = ((IntToken) ((Variable) frequency)
                            .getToken()).intValue();
                }
                if (WCET == null) {
                    actorWCET = 0.01;
                } else {
                    actorWCET = ((DoubleToken) ((Variable) WCET).getToken())
                            .doubleValue();
                }
            }
            wcet += actorFrequency * actorWCET;

        }
        if (_debugging) {
            _debug("sdf director has wcet of " + wcet);
        }
        return wcet;
    }
}
