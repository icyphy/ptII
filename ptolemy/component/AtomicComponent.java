/* A component with functionality given in Java.

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
package ptolemy.component;

import ptolemy.data.TupleToken;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// AtomicComponent

/**
 A component with functionality given in Java. The functionality can
 be given in the {@link #run()} method or by the
 {@link MethodCallPort#call(TupleToken)} method of contained ports
 that are providers.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating yellow (ellen_zh)
 @Pt.AcceptedRating red (cxh)
 */
public class AtomicComponent extends ComponentEntity implements Component {
    /** Construct an entity with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public AtomicComponent(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setContainer(container);
        _addIcon();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the component, which in this base class means
     *  doing nothing and returning immediately.  This is invoked once after
     *  preinitialize() and again whenever the component needs
     *  to be reinitialized.
     *  @exception IllegalActionException If initialization
     *   cannot be completed (not thrown in this base class).
     */
    @Override
    public void initialize() throws IllegalActionException {
    }

    /** Create a new port with the specified name.
     *  The container of the port is set to this entity.
     *  This overrides the base class to create an instance of MethodCallPort.
     *  Derived classes may override this to further constrain the ports.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param name The new port name.
     *  @return The new port
     *  @exception IllegalActionException If the argument is null.
     *  @exception NameDuplicationException If this entity already has a
     *   port with the specified name.
     */
    @Override
    public Port newPort(String name) throws IllegalActionException,
            NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            Port port = new MethodCallPort(this, name);
            return port;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Preinitialize the component, which in this base class means doing
     *  nothing and returning immediately. This is invoked exactly
     *  once per execution of a model, before any other methods
     *  in this interface are invoked.
     *  @exception IllegalActionException If preinitialization
     *   cannot be completed (not thrown in this base class).
     */
    @Override
    public void preinitialize() throws IllegalActionException {
    }

    /** Execute the component, which in this base class means doing
     *  nothing and returning immediately.
     *  This is invoked after preinitialize()
     *  and initialize(), and may be invoked repeatedly.
     *  @exception IllegalActionException If the run cannot be completed
     *   (not thrown in this base class).
     */
    @Override
    public void run() throws IllegalActionException {
    }

    /** Wrap up an execution, which in this base class means doing
     *  nothing and returning immediately. This method is invoked
     *  exactly once per execution of a model. It finalizes
     *  an execution, typically
     *  closing files, displaying final results, etc. If any other
     *  method from this interface is invoked after this, it must
     *  begin with preinitialize().
     *  @exception IllegalActionException If wrapup fails.
     */
    @Override
    public void wrapup() throws IllegalActionException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a port to this entity. This overrides the base class to
     *  throw an exception if the added port is not an instance of
     *  MethodCallPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set
     *  the container of the port to point to this entity.
     *  It assumes that the port is in the same workspace as this
     *  entity, but does not check.  The caller should check.
     *  Derived classes may override this method to further constrain to
     *  a subclass of MethodCallPort.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param port The port to add to this entity.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this entity, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the entity.
     */
    @Override
    protected void _addPort(Port port) throws IllegalActionException,
            NameDuplicationException {
        if (!(port instanceof MethodCallPort)) {
            throw new IllegalActionException(this, port,
                    "Incompatible port class for this entity.");
        }

        super._addPort(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _addIcon() {
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
                + "height=\"40\" style=\"fill:white\"/>\n"
                + "<polygon points=\"-20,-10 20,0 -20,10\" "
                + "style=\"fill:blue\"/>\n" + "</svg>\n");
    }
}
