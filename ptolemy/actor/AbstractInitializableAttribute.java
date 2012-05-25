/* An abstract base class for initializable attributes.

 Copyright (c) 1997-2012 The Regents of the University of California.
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

package ptolemy.actor;

import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// AbstractInitializableAttribute

/** An abstract base class for attributes that are preinitialized,
 *  initialized, and wrapped up during execution of a model.
 *
 *  @author Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Yellow (eal )
 *  @Pt.AcceptedRating Red (eal)
 */
public abstract class AbstractInitializableAttribute extends Attribute implements Initializable {
    
    /** Construct an instance of the attribute.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public AbstractInitializableAttribute(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Add the specified object to the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     */
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
    }
    
    /** Clone the attribute.  This clears the list of initializable objects.
     *  @param workspace The workspace in which to place the cloned attribute.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned variable.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AbstractInitializableAttribute newObject = (AbstractInitializableAttribute) super.clone(workspace);
        newObject._initializables = null;
        return newObject;
    }
    
    /** Do nothing.
     *  @exception IllegalActionException If thrown by a subclass.
     */
    public void initialize() throws IllegalActionException {
    }

    /** Do nothing.
     *  @exception IllegalActionException If thrown by a subclass.
     */
    public void preinitialize() throws IllegalActionException {
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     */
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    /** Set the new container of this parameter. This method overrides
     *  the base class to remove this object from the list of initializables
     *  of the previous container, if any, and to add it to the list of
     *  initializables in the new container.
     *  @param container The new container.
     *  @exception IllegalActionException If the container will not accept
     *   the attribute, or this variable and the container
     *   are not in the same workspace, or the proposed container would
     *   result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this variable.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        NamedObj oldContainer = getContainer();
        if (oldContainer instanceof Initializable) {
            ((Initializable) oldContainer).removeInitializable(this);
        }
        super.setContainer(container);
        if (container instanceof Initializable) {
            ((Initializable) container).addInitializable(this);
        }
    }

    /** Do nothing.
     *  @exception IllegalActionException If thrown by a subclass.
     */
     public void wrapup() throws IllegalActionException {
     }
     
     ///////////////////////////////////////////////////////////////////
     ////                         private variables                 ////
     
     /** List of objects whose (pre)initialize() and wrapup() methods should be
      *  slaved to these.
      */
     private transient List<Initializable> _initializables;       
}