/* An abstract base class for initializable attributes.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package ptolemy.data.expr;

import java.util.LinkedHashSet;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Initializable;
import ptolemy.kernel.util.HierarchyListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// AbstractInitializableParameter

/** An abstract base class for parameters that are preinitialized,
 *  initialized, and wrapped up during execution of a model.
 *  This takes care of the rather complex adjustments that must
 *  be made when the hierarchy changes and also provides default
 *  methods.
 *
 *  @author Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (eal )
 *  @Pt.AcceptedRating Red (eal)
 */
public abstract class AbstractInitializableParameter extends Parameter
        implements HierarchyListener, Initializable {

    /** Construct an instance of the attribute.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public AbstractInitializableParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified object to the set of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     */
    @Override
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedHashSet<Initializable>();
        }
        _initializables.add(initializable);
    }

    /** Clone the attribute.  This clears the list of initializable objects.
     *  @param workspace The workspace in which to place the cloned attribute.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned variable.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AbstractInitializableParameter newObject = (AbstractInitializableParameter) super
                .clone(workspace);
        newObject._initializables = null;
        return newObject;
    }

    /** Notify this object that the containment hierarchy above it has
     *  changed. This method does nothing because instead we use
     *  {@link #preinitialize()} to handle re-establishing the connections.
     *  @exception IllegalActionException If the change is not
     *   acceptable.
     */
    @Override
    public void hierarchyChanged() throws IllegalActionException {
        // Make sure we are registered as to be initialized
        // with the container.
        Initializable container = _getInitializableContainer();
        if (container != null) {
            container.addInitializable(this);
        }
    }

    /** Notify this object that the containment hierarchy above it will be
     *  changed, which results in
     *  @exception IllegalActionException If unlinking to a published port fails.
     */
    @Override
    public void hierarchyWillChange() throws IllegalActionException {
        // Unregister to be initialized with the initializable container.
        // We will be re-registered when hierarchyChanged() is called.
        Initializable container = _getInitializableContainer();
        if (container != null) {
            container.removeInitializable(this);
        }
    }

    /** Invoke initialize() on registered initializables.
     *  @exception IllegalActionException If thrown by a subclass.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }
    }

    /** Invoke preinitialize() on registered initializables.
     *  @exception IllegalActionException If thrown by a subclass.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     */
    @Override
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    /** Override the base class to register as an
     *  {@link Initializable}
     *  so that preinitialize() is invoked, and as a
     *  {@link HierarchyListener}, so that we are notified of
     *  changes in the hiearchy above.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        Initializable previousInitializableContainer = _getInitializableContainer();
        NamedObj previousContainer = getContainer();
        super.setContainer(container);
        Initializable newInitializableContainer = _getInitializableContainer();
        if (previousInitializableContainer != newInitializableContainer) {
            if (previousInitializableContainer != null) {
                previousInitializableContainer.removeInitializable(this);
            }
            if (newInitializableContainer != null) {
                newInitializableContainer.addInitializable(this);
            }
        }
        if (previousContainer != container) {
            if (previousContainer != null) {
                previousContainer.removeHierarchyListener(this);
            }
            if (container != null) {
                container.addHierarchyListener(this);
            }
        }
    }

    /** Invoke wrapup() on registered initializables.
     *  @exception IllegalActionException If thrown by a subclass.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the first Initializable encountered above this
     *  in the hierarchy that will be initialized (i.e., it is either
     *  an atomic actor or an opaque composite actor).
     *  @return The first Initializable above this in the hierarchy,
     *   or null if there is none.
     */
    protected Initializable _getInitializableContainer() {
        NamedObj container = getContainer();
        while (container != null) {
            if (container instanceof Initializable) {
                if (container instanceof CompositeActor) {
                    if (((CompositeActor) container).isOpaque()) {
                        return (Initializable) container;
                    }
                } else {
                    return (Initializable) container;
                }
            }
            container = container.getContainer();
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of objects whose (pre)initialize() and wrapup() methods should be
     *  slaved to these.
     */
    private transient Set<Initializable> _initializables;
}
