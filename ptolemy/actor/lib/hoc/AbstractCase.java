/* Abstract base class for a composite actor with several possible refinements.

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.hoc;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// AbstractCase
/**

Abstract base class for a composite actor with several possible refinements.

@author  Joern Janneck and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public abstract class AbstractCase extends TypedCompositeActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AbstractCase(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        _director = new CaseDirector(this, "CaseDirector");

        // The base class forces the class name to be TypedCompositeActor.
        // Override that to get the current class name.
        setClassName(getClass().getName());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the refinement to execute, which must be either an
     *  atomic actor or an opaque composite actor.
     *  @return An actor contained by this actor, or null to indicate
     *   that none should be executed.
     */
    protected abstract Actor _choose();

    /** Override the base class so that the description includes
     *  only ports and contained entities, plus all attributes
     *  except the director.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {

        Iterator attributes = attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            if (attribute != _director) {
                attribute.exportMoML(output, depth);
            }
        }

        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port)ports.next();
            port.exportMoML(output, depth);
        }

        Iterator entities = entityList().iterator();
        while (entities.hasNext()) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            entity.exportMoML(output, depth);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** My director. */
    private Director _director;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Our director.
     */
    private class CaseDirector extends Director {

        /** Construct a director in the given container with the given name.
         *  @param container The container.
         *  @param name The name of this director.
         *  @exception IllegalActionException If the name has a period in it,
         *   or the director is not compatible with the specified container.
         *  @exception NameDuplicationException If the container already
         *   contains an entity with the specified name.
         */
        public CaseDirector(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Invoke fire() on the current choice.
         *  @exception IllegalActionException If the current choice
         *   throws it.
         */
        public void fire() throws IllegalActionException {
            _currentChoice.fire();
        }

        /** Call postfire() on the current choice.
         *  @return The result of postfire() on the current choice.
         *  @exception IllegalActionException If the current choice
         *   throws it.
         */
        public boolean postfire() throws IllegalActionException {
            return _currentChoice.postfire();
        }

        /** Call the choice() method of the enclosing actor to
         *  determine the current choice, and if the current choice
         *  is non-null, then call its prefire() method.
         *  @return False if there is no current choice, or else the
         *   value returned by prefire() on the current choice.
         *  @exception IllegalActionException If the current choice
         *   throws it.
         */
        public boolean prefire() throws IllegalActionException {
            _currentChoice = _choose();
            if (_currentChoice == null) {
                return false;
            }
            return _currentChoice.prefire();
        }

        ///////////////////////////////////////////////////////////////
        ////                     private variable                  ////

        // The current choice.
        private Actor _currentChoice;
    }
}
