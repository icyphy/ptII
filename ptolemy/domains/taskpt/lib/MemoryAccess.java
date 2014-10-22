/* An actor that allows access to memory.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.domains.taskpt.lib;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.domains.taskpt.kernel.TaskPtDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// MemoryAccess

/** An actor that allows access to a memory of a TaskPtDirector.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 */
public abstract class MemoryAccess extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>ptr</i> port.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException Thrown if the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Thrown if the container already has an
     *   actor with this name.
     */
    public MemoryAccess(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        ptr = new TypedIOPort(this, "ptr", false, false);
        ptr.setMultiport(false);
        // FIXME: There should be a PtrToken type at some time.
        ptr.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The pointer to the data. Depending on the implementing subclass,
     * this is either an input or an output. */
    public TypedIOPort ptr;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the memory that this actor can access. This is the memory
     * controlled by the first director in the hierarchy upwards that is of
     * instance TaskPtDirector.
     *
     * @return The memory that is accessible for this actor.
     */
    protected Memory getMemory() {
        Memory memory = null;
        Director director = null;
        Actor actor = this;
        while (director == null && actor != null) {
            if (actor.getDirector() instanceof TaskPtDirector) {
                director = actor.getDirector();
                memory = ((TaskPtDirector) director).getMemory();
            }
            if (actor.getContainer() instanceof Actor) {
                actor = (Actor) actor.getContainer();
            } else {
                actor = null;
            }
        }
        return memory;
    }

}
