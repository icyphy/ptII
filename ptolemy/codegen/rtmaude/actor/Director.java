/* RTMaude Code generator helper class for the Director class.

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
package ptolemy.codegen.rtmaude.actor;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Director

/**
 * Generate RTMaude code for a general Director in DE domain.
 *
 * @see ptolemy.actor.Director
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class Director extends ptolemy.codegen.actor.Director {
    /** Construct the code generator adaptor associated with the given director.
     *  @param director The associated director.
     */
    public Director(ptolemy.actor.Director director) {
        super(director);
    }

    /** Generate the code specified by the given block with given arguments
     * for each actor associated to the director.
     *
     * @param blockName The name of the block.
     * @param args The arguments for the block.
     * @return The code described by the given block.
     * @exception IllegalActionException
     */
    public List<String> getBlockCodeList(String blockName, String... args)
            throws IllegalActionException {
        List<Actor> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList();

        List<String> ret = new LinkedList();

        for (Actor actor : actors) {
            RTMaudeAdaptor helper = (RTMaudeAdaptor) _getHelper((NamedObj) actor);
            ret.addAll(helper.getBlockCodeList(blockName, args));
        }

        return ret;
    }
}
