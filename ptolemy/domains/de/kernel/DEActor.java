/* A base class for DE domain actors.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.actor.*;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.TimedActor;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// DEActor
/** A base class for actors specific to the DE domain.  This class
 *  implements both SequenceActor and TimedActor.
 *  Actors in DE models need not derive from this class.
 *
 *  @author Jie Liu
 *  @version $Id$
 *  @see Actor
 */
public abstract class DEActor extends TypedAtomicActor
    implements SequenceActor, TimedActor {

    /** Constructor an with the specified container and name.
     *  This is protected because there is no reason to create an instance
     *  of this class, but derived classes will want to invoke the
     *  constructor of the superclass.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    protected DEActor(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

}



