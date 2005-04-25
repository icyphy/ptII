/* Actor with several possible refinements.

Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import ptolemy.actor.Actor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// Case

/**
   An actor with several possible refinements.

   @author  Joern Janneck and Edward A. Lee
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (eal)
*/
public class Case extends AbstractCase {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Case(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // FIXME: doc
    public void initialize() throws IllegalActionException {
        super.initialize();
        _entityIterator = this.deepEntityList().iterator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the refinement to execute, which must be either an
     *  atomic actor or an opaque composite actor.
     *  @return An actor contained by this actor, or null to indicate
     *   that none should be executed.
     */
    protected Actor _choose() {
        if (!_entityIterator.hasNext()) {
            _entityIterator = deepEntityList().iterator();

            if (!_entityIterator.hasNext()) {
                return null;
            }
        }

        return (Actor) _entityIterator.next();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Iterator _entityIterator;
}
