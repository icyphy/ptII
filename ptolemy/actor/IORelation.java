/* Relation supporting message passing.

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
package ptolemy.actor;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// IORelation

/**
 This class mediates connections between ports that can send data to
 one another via message passing. One purpose of this relation is to
 ensure that IOPorts are only connected to IOPorts. A second purpose
 is to support the notion of a <i>width</i> to represent something
 like a bus. By default an IORelation is a bus for which the width
 will be inferred, which corresponds to a width equal to Auto. In
 Vergil you can change the width from Auto to a specific value, to
 explicitly specify the width of a relation. Specifying a width
 equal to zero will disable the relation.
 A width equal equal to -1 is equivalent to a width equal to Auto,
 in which case the width will be inferred (if possible) from the
 context. In particular, if this relation is linked on the inside
 to a port with some width, then the width of this relation will
 be inferred to be the enough so that the widths of all inside
 linked relations adds up to the outside width of the port.
 If this IORelation is linked to another
 instance of IORelation, then the width of the two IORelations is
 constrained to be the same.
 <p>
 Instances of IORelation can only be linked to instances of IOPort
 or instances of IORelation.
 Derived classes may further constrain this to subclasses of IOPort
 of IORelation.
 Such derived classes should override the protected methods _checkPort()
 and _checkRelation() to throw an exception.
 <p>
 To link a IOPort to an IORelation, use the link() or
 liberalLink() method in the IOPort class.  To remove a link,
 use the unlink() method. To link (unlink) an IORelation to an IORelation,
 use the link() (unlink()) method of IORelation.
 <p>
 The container for instances of this class can only be instances of
 CompositeActor.  Derived classes may wish to further constrain the
 container to subclasses of ComponentEntity.  To do this, they should
 override the _checkContainer() method.

 @author Edward A. Lee, Jie Liu, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (acataldo)
 */
public class IORelation extends ComponentRelation {
    /** Construct a relation in the default workspace with an empty string
     *  as its name. Add the relation to the directory of the workspace.
     */
    public IORelation() {
        super();
        _init();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the relation to the workspace directory.
     *
     *  @param workspace The workspace that will list the relation.
     */
    public IORelation(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public IORelation(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The width of this relation. This is an integer that defaults
     *  to WIDTH_TO_INFER, which means that the width will be inferred.
     */
    public Parameter width;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  This overrides
     *  the base class so that if the attribute is an instance of
     *  Parameter and the name is "width", then the width of the Relation
     *  is set.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute instanceof Parameter
                && "width".equals(attribute.getName())) {
            IntToken t = (IntToken) ((Parameter) attribute).getToken();

            if (t != null) {
                int width = t.intValue();
                _setWidth(width);
            }
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new relation with no links and no container, but with
     *  the same width as the original.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new ComponentRelation.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        IORelation newObject = (IORelation) super.clone(workspace);
        newObject._inferredWidthVersion = -1;
        newObject._cachedWidth = -2;
        return newObject;
    }

    /** Return the receivers of all input ports linked to this
     *  relation, directly or indirectly through a relation group,
     *  except those in the port
     *  given as an argument. The returned value is an array of
     *  arrays. The first index (the row) specifies the group, where
     *  a group receives the same data from a channel.
     *  Each channel normally receives distinct data. The
     *  second index (the column) specifies the receiver number within
     *  the group of receivers that get copies from the same channel.
     *  <p>
     *  The number of groups (rows) is less than or equal to the
     *  width of the relation, which is always at least one. If
     *  there are no receivers then return null.
     *  <p>
     *  For each channel, there may be any number of receivers in the group.
     *  The individual receivers are selected using the second index of the
     *  returned array of arrays.  If there are no receivers in the group,
     *  then the channel is represented by null.  I.e., if the returned
     *  array of arrays is <i>x</i> and the channel number is <i>c</i>,
     *  then <i>x</i>[<i>c</i>] is null.  Otherwise, it is an array, where
     *  the size of the array is the number of receivers in the group.
     *  <p>
     *  NOTE: This method may have the effect of creating new receivers in the
     *  remote input ports and losing the previous receivers in those ports,
     *  together with any data they may contain.  This occurs only if the
     *  topology has changed since the receivers were created, and that change
     *  resulting in one of those ports not having the right number of
     *  receivers.
     *  <p>
     *  This method read-synchronizes on the workspace.
     *
     *  @see IOPort#getRemoteReceivers
     *  @param except The port to exclude, or null to not
     *   exclude any ports.
     *  @return The receivers associated with this relation.
     * @exception IllegalActionException If throwen while determining the cascade.
     * @exception InvalidStateException
     */
    public Receiver[][] deepReceivers(IOPort except)
            throws /*InvalidStateException,*/IllegalActionException {
        try {
            _workspace.getReadAccess();

            Receiver[][] result = new Receiver[0][0];
            Iterator<?> inputs = linkedDestinationPortList(except).iterator();
            Receiver[][] receivers; //= new Receiver[0][0];

            // NOTE: We have to be careful here to keep track of
            // multiple occurrences of a port in this list.
            // EAL 7/30/00.
            HashMap<IOPort, Integer> seen = new HashMap<IOPort, Integer>();

            while (inputs.hasNext()) {
                IOPort p = (IOPort) inputs.next();

                if (p.isInsideGroupLinked(this) && !p.isOpaque()) {
                    // if p is a transparent port and this relation links
                    // from the inside, then get the Receivers outside p.
                    try {
                        receivers = p.getRemoteReceivers(this);
                    } catch (IllegalActionException ex) {
                        throw new InternalErrorException(this, ex, null);
                    }
                } else {
                    // if p not a transparent port, or this relation is linked
                    // to p from the outside.
                    try {
                        // Note that this may be an inside or outside linked
                        // relation.
                        // NOTE: We have to be careful here to keep track of
                        // multiple occurrences of a port in this list.
                        // EAL 7/30/00.
                        int occurrence = 0;

                        if (seen.containsKey(p)) {
                            occurrence = seen.get(p).intValue();
                            occurrence++;
                        }

                        seen.put(p, Integer.valueOf(occurrence));

                        receivers = p._getReceiversLinkedToGroup(this,
                                occurrence);
                    } catch (IllegalActionException ex) {
                        throw new InternalErrorException(this, ex, null);
                    }
                }

                result = _cascade(result, receivers);
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the width of the IORelation, which is always at least one.
     *  If the width has been set to the value of WIDTH_TO_INFER, then
     *  the relation is a bus with
     *  unspecified width, and the width needs to be inferred from the
     *  way the relation is connected.  This is done by checking the
     *  ports that this relation is linked to from the inside and setting
     *  the width to the maximum of those port widths, minus the widths of
     *  other relations linked to those ports on the inside. Each such port is
     *  allowed to have at most one inside relation with an unspecified
     *  width, or an exception is thrown.  If this inference yields a width
     *  of zero, then return one.
     *
     *  @return The width, which is at least zero.
     *  @exception IllegalActionException If thrown while getting the
     *  user width, determining if the width inference is needed or if
     *  thrown by inferWidths().
     *  @see #setWidth(int)
     */
    public int getWidth() throws IllegalActionException {
        int width = _getUserWidth();
        if (_USE_NEW_WIDTH_INFERENCE_ALGO) {

            // If _width equals to the value of WIDTH_TO_INFER
            // we might need to infer it. Since the width inference
            // is cached we only need to infer it in case
            // needsWidthInference() returns true.
            if (width == WIDTH_TO_INFER) {
                if (needsWidthInference()) {

                    Nameable container = getContainer();

                    if (container instanceof CompositeActor) {
                        ((CompositeActor) container).inferWidths();
                    } else {
                        throw new IllegalActionException(
                                this,
                                "Can't infer the widths "
                                        + "of the relations since no container or container is not a CompositeActor.");
                    }

                    assert _inferredWidthVersion == _workspace.getVersion();
                    assert !needsWidthInference();
                }
                return _inferredWidth;
            }
            return width;
        } else {
            if (width == 0) {
                return _inferWidth();
            }
            return width;
        }
    }

    /** Return true if the relation has a definite width (i.e.,
     *  setWidth() has not been called with a value equal to
     *  WIDTH_TO_INFER.
     *  @return True if the width has been set to a positive value.
     *  @exception IllegalActionException If the expression for the width cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public boolean isWidthFixed() throws IllegalActionException {
        int width = _getUserWidth();
        return width != WIDTH_TO_INFER;
    }

    /** List the input ports that this relation connects to from the
     *  outside, and the output ports that it connects to from
     *  the inside. I.e., list the ports through or to which we
     *  could send data. Two ports are connected if they are
     *  linked to relations in the same relation group.
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @return An enumeration of IOPort objects.
     */
    public List<IOPort> linkedDestinationPortList() {
        return linkedDestinationPortList(null);
    }

    /** List the input ports that this relation connects to from the
     *  outside and the output ports that it connects to from
     *  the inside, except the port given as an argument.
     *  I.e., list the ports through or to which we
     *  could send data. Two ports are connected if they are
     *  linked to relations in the same relation group.
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPortList(ptolemy.kernel.Port)
     *  @param except The port not included in the returned list, or
     *   null to not exclude any ports.
     *  @return A list of IOPort objects.
     */
    public List<IOPort> linkedDestinationPortList(IOPort except) {
        try {
            _workspace.getReadAccess();

            // NOTE: The result could be cached for efficiency, but
            // it would have to be cached in a hashtable indexed by the
            // except argument.  Probably not worth it.
            LinkedList<IOPort> resultPorts = new LinkedList<IOPort>();
            Iterator<?> ports = linkedPortList().iterator();

            while (ports.hasNext()) {
                IOPort p = (IOPort) ports.next();

                if (p != except) {
                    if (p.isInsideGroupLinked(this)) {
                        // Linked from the inside
                        if (p.isOutput()) {
                            resultPorts.addLast(p);
                        }
                    } else {
                        if (p.isInput()) {
                            resultPorts.addLast(p);
                        }
                    }
                }
            }

            return resultPorts;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the input ports that we are linked to from the outside,
     *  and the output ports that we are linked to from the inside.
     *  I.e., enumerate the ports through or to which we could send data.
     *  This method is deprecated and calls linkedDestinationPortList().
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @deprecated Use linkedDestinationPortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Enumeration linkedDestinationPorts() {
        return linkedDestinationPorts(null);
    }

    /** Enumerate the input ports that we are linked to from the
     *  outside, and the output ports that we are linked to from
     *  the inside, except the port given as an argument.
     *  I.e., enumerate the ports through or to which we could send data.
     *  This method is deprecated and calls
     *  linkedDestinationPortList(IOPort).
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts(ptolemy.kernel.Port)
     *  @param except The port not included in the returned Enumeration.
     *  @deprecated Use linkDestinationPortList(IOPort) instead.
     *  @return An enumeration of IOPort objects.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Enumeration linkedDestinationPorts(IOPort except) {
        return Collections.enumeration(linkedDestinationPortList(except));
    }

    /** List the output ports that this relation connects to from the
     *  outside and the input ports that it connects to from
     *  the inside.
     *  I.e., list the ports through or from which we
     *  could receive data. Two ports are connected if they are
     *  linked to relations in the same relation group.
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @return An enumeration of IOPort objects.
     */
    public List<IOPort> linkedSourcePortList() {
        return linkedSourcePortList(null);
    }

    /** List the output ports that this relation connects to from the
     *  outside and the input ports that it connects to from
     *  the inside, except the port given as an argument.
     *  I.e., list the ports through or from which we
     *  could receive data. Two ports are connected if they are
     *  linked to relations in the same relation group.
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPortList(ptolemy.kernel.Port)
     *  @param except The port not included in the returned list.
     *  @return A list of IOPort objects.
     */
    public List<IOPort> linkedSourcePortList(IOPort except) {
        try {
            _workspace.getReadAccess();

            // NOTE: The result could be cached for efficiency, but
            // it would have to be cached in a hashtable indexed by the
            // except argument.  Probably not worth it.
            LinkedList<IOPort> resultPorts = new LinkedList<IOPort>();
            Iterator<?> ports = linkedPortList().iterator();

            while (ports.hasNext()) {
                IOPort p = (IOPort) ports.next();

                if (p != except) {
                    if (p.isInsideGroupLinked(this)) {
                        // Linked from the inside
                        if (p.isInput()) {
                            resultPorts.addLast(p);
                        }
                    } else {
                        if (p.isOutput()) {
                            resultPorts.addLast(p);
                        }
                    }
                }
            }

            return resultPorts;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. enumerate the ports from or through which we might receive
     *  data. This method is deprecated and calls
     *  linkedSourcePortList().
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @deprecated Use linkedSourcePortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Enumeration linkedSourcePorts() {
        return Collections.enumeration(linkedSourcePortList());
    }

    /** Enumerate the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. enumerate the ports from or through which we might receive
     *  This method is deprecated and calls
     *  linkedSourcePortList(IOPort).
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts(ptolemy.kernel.Port)
     *  @param except The port not included in the returned Enumeration.
     *  @deprecated Use linkedSourcePortList(IOPort) instead.
     *  @return An enumeration of IOPort objects.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Enumeration linkedSourcePorts(IOPort except) {
        return Collections.enumeration(linkedSourcePortList(except));
    }

    /**
     * Determine whether for this relation width inference needs to be performed.
     * @return True when width inference needs to be performed.
     *  @exception IllegalActionException If the expression for the width cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public boolean needsWidthInference() throws IllegalActionException {
        // In case we width has been fixed or the version has not changed since the last width
        // inference, we of course don't need to do width inference.
        // In case the version changed it still might happen that we do not have to
        // width inference, since the change might not be related to or has no influence
        // on the width of relations. Hence we check with the director who is aware or changes
        // related to the connectivity.
        int width = _getUserWidth();
        boolean widthInferenceValid = width != WIDTH_TO_INFER
                || _inferredWidthVersion == _workspace.getVersion();
        if (!widthInferenceValid) {
            Nameable container = getContainer();

            if (container instanceof CompositeActor) {
                widthInferenceValid = !((CompositeActor) container)
                        .needsWidthInference();
                if (widthInferenceValid) {
                    _inferredWidthVersion = _workspace.getVersion();
                }
            } else {
                // If we don't have a director or manager we can't determine the inferred width.
                // You could argue it is wrong to set the _inferredWidth = 0, however
                // the user can't run the model anyway and hence needs to add a director
                // to run it, at which time the width inference will be executed again.
                // If there is no manager but a director it means the user has not run
                // the model. Since we don't update the version we make sure that the
                // width is updated when the model is initialized.
                _inferredWidth = 0;
                widthInferenceValid = true;
            }
        }
        return !widthInferenceValid;
    }

    /** Specify the container, adding the relation to the list
     *  of relations in the container.
     *  If this relation already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the list of objects in the workspace. If the argument is null, then
     *  unlink the ports from the relation, remove it from
     *  its container, and add it to the list of objects in the workspace.
     *  If the relation is already contained by the container, do nothing.
     *  <p>
     *  The container must be an
     *  instance of CompositeActor or null, otherwise an exception is thrown.
     *  Derived classes may further constrain the class of the container
     *  to a subclass of CompositeActor.
     *  <p>
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  <p>
     *  This method is write-synchronized on the workspace.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not a
     *   CompositeActor or null, or this entity and the container are not in
     *   the same workspace.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the relations list of the container.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof CompositeActor) && container != null) {
            throw new IllegalActionException(this, container,
                    "IORelation can only be contained by CompositeActor.");
        }

        // Invalidate schedule and type resolution of the old container.
        Nameable oldContainer = getContainer();

        if (oldContainer instanceof CompositeActor) {
            Director director = ((CompositeActor) oldContainer).getDirector();

            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }

        // Invalidate schedule and type resolution of the new container.

        // Either container == null or container instanceof CompositeActor == true
        if (container != null) {
            Director director = ((CompositeActor) container).getDirector();

            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }

        super.setContainer(container);
    }

    /** Set the width of this relation and all relations in its
     *  relation group. The width is the number of
     *  channels that the relation represents.  If the argument
     *  is equal to WIDTH_TO_INFER, then the relation becomes a bus with
     *  unspecified width,
     *  and the width will be inferred from the way the relation is used
     *  (but will never be less than zero).
     *  This method invalidates
     *  the resolved types on the director of the container, if there is
     *  one, and notifies each connected actor that its connections
     *  have changed.
     *  This method write-synchronizes on the workspace.
     *
     *  @param widthValue The width of the relation.
     *  @exception IllegalActionException If the argument is not zero, one,
     *   or equal to WIDTH_TO_INFER and the relation is linked to a
     *   non-multiport. Or when the argument is less than zero and different
     *   from WIDTH_TO_INFER.
     *  @see ptolemy.kernel.util.Workspace#getWriteAccess()
     *  @see #getWidth()
     */
    public void setWidth(int widthValue) throws IllegalActionException {
        width.setToken(new IntToken(widthValue));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should describe the width
     *  of the relation, and whether it has been fixed.
     */
    public static final int CONFIGURATION = 512;

    /** The value of the width we should infer. */
    public static final int WIDTH_TO_INFER = -1;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the specified port cannot be linked to this
     *  relation (is not of class IOPort).
     *  @param port The candidate port to link to.
     *  @exception IllegalActionException If the port is not an IOPort.
     */
    @Override
    protected void _checkPort(Port port) throws IllegalActionException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "IORelation can only link to a IOPort.");
        }
    }

    /** Throw an exception if the specified relation is not an instance
     *  of IORelation or if it does not have the same width as this relation.
     *  @param relation The relation to link to.
     *  @param symmetric If true, the call _checkRelation() on the specified
     *   relation with this as an argument.
     *  @exception IllegalActionException If this relation has no container,
     *   or if this relation is not an acceptable relation for the specified
     *   relation, or if this relation and the specified relation do not
     *   have the same width.
     */
    @Override
    protected void _checkRelation(Relation relation, boolean symmetric)
            throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "IORelation can only link to an IORelation.");
        }

        int otherWidth = ((IORelation) relation)._getUserWidth();
        int width = _getUserWidth();
        if (otherWidth != width) {
            // If one of both widths equals WIDTH_TO_INFER we set both to the other width
            //  (which will be different from WIDTH_TO_INFER).
            if (otherWidth == WIDTH_TO_INFER) {
                ((IORelation) relation).setWidth(width);
            } else if (width == WIDTH_TO_INFER) {
                setWidth(otherWidth);
            } else {
                throw new IllegalActionException(this, relation,
                        "Relations have different widths: " + _getUserWidth()
                        + " != "
                        + ((IORelation) relation)._getUserWidth());
            }
        }

        super._checkRelation(relation, symmetric);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class and in this class.
     *  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  <p>
     *  If the detail argument sets the bit defined by the constant
     *  CONFIGURATION, then append to the description is a field
     *  of the form "configuration {width <i>integer</i> ?fixed?}", where the
     *  word "fixed" is present if the relation has fixed width, and is
     *  absent if the relation is a bus with inferred width (isWidthFixed()
     *  returns false).
     *
     *  This method is read-synchronized on the workspace.
     *
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     *  @exception IllegalActionException If thrown while getting the
     *  description of subcomponents.
     */
    @Override
    protected String _description(int detail, int indent, int bracket)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            String result;

            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }

            if ((detail & CONFIGURATION) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }

                result += "configuration {";
                result += "width " + getWidth();

                if (isWidthFixed()) {
                    result += " fixed";
                }

                result += "}";
            }

            if (bracket == 2) {
                result += "}";
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /**Determines whether width inference should be skipped or not.
     * @return True when width inference needs to be skipped.
     */
    protected boolean _skipWidthInference() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         packaged methods                  ////

    /** Set the inferred width of this relation. The width is the number of
     *  channels that the relation represents.
     *  This method is not synchronized on the workspace.
     *  This packaged method is only meant for the width inference
     *  algorithm. It should not be used in other circumstances.
     *  Precondition: you should only infer the width in case it
            is not set by the user.
     *  @param width The inferred width of the relation.
     *  @exception IllegalActionException If the expression for the width cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    void _setInferredWidth(int width) throws IllegalActionException {
        assert _getUserWidth() == WIDTH_TO_INFER;
        // Precondition: you should only infer the width in case it
        // is not set by the user.
        assert width >= 0;
        _inferredWidthVersion = _workspace.getVersion();
        _inferredWidth = width;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private parameters                 ////

    /** A parameter to be able to set the width to Auto to automatically
     * infer widths. This is an integer that equals WIDTH_TO_INFER.
     */
    private Parameter _auto = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Cascade two Receiver arrays to form a new array. For each row, each
     *  element of the second array is appended behind the elements of the
     *  first array. This method is solely for deepReceivers.
     *  The two input arrays must have the same number of rows.
     * @exception IllegalActionException
     */
    private Receiver[][] _cascade(Receiver[][] array1, Receiver[][] array2)
            throws InvalidStateException, IllegalActionException {
        if (array1 == null || array1.length <= 0) {
            return array2;
        }

        if (array2 == null || array2.length <= 0) {
            return array1;
        }

        int width = getWidth();
        Receiver[][] result = new Receiver[width][0];

        for (int i = 0; i < width; i++) {
            if (array1[i] == null) {
                result[i] = array2[i];
            } else if (array1[i].length <= 0) {
                result[i] = array2[i];
            } else if (array2[i] == null) {
                result[i] = array1[i];
            } else if (array2[i].length <= 0) {
                result[i] = array1[i];
            } else {
                int m1 = array1[i].length;
                int m2 = array2[i].length;
                result[i] = new Receiver[m1 + m2];

                for (int j = 0; j < m1; j++) {
                    result[i][j] = array1[i][j];
                }

                for (int j = m1; j < m1 + m2; j++) {
                    result[i][j] = array2[i][j - m1];
                }
            }
        }

        return result;
    }

    /** Return the width set by the user
     *  @return The width set by the user.
     *  @exception IllegalActionException If the expression for the width cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    private int _getUserWidth() throws IllegalActionException {
        if (_cachedWidth == -2) {
            IntToken t = (IntToken) width.getToken();

            if (t != null) {
                int width = t.intValue();
                _setWidth(width);
            }
        }
        return _cachedWidth;
    }

    /** Determine whether widths are currently being inferred or not.
     *  @return True When widths are currently being inferred.
     *  @exception IllegalActionException If toplevel not a CompositeActor.
     */
    private boolean _inferringWidths() throws IllegalActionException {
        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            return ((CompositeActor) container).inferringWidths();
        }
        return false;
    }

    /** Infer the width of the port from how it is connected.
     *  Throw a runtime exception if this cannot be done (normally,
     *  the methods that construct a topology ensure that it can be
     *  be done).  The returned value is always at least one.
     *  This method is not read-synchronized on the workspace, so the caller
     *  should be.
     *  @return The inferred width.
     * @exception IllegalActionException
     */
    private int _inferWidth() throws IllegalActionException {
        //The old algorithm for width inference
        assert !_USE_NEW_WIDTH_INFERENCE_ALGO;
        long version = _workspace.getVersion();

        if (version != _inferredWidthVersion) {
            _inferredWidth = 1;

            Iterator<?> ports = linkedPortList().iterator();

            // Note that depending on the order of the ports get iterated,
            // the inferred width may be different if different ports have
            // different widths. This is nondeterministic.
            // However, the model behavior is not affected by this because
            // the relation with the smallest width along a path decides
            // the number of signals that can be passed through.
            while (ports.hasNext()) {
                IOPort p = (IOPort) ports.next();

                // Infer the width of this port from the linked connections.
                // Note we assume that this method is only called upon a
                // multiport.
                // To guarantee this method successfully infer widths, we have
                // to check and ensure that there is at most one input relation
                // or one output relation whose width is not fixed (unknown).
                // This requirement is conservative. For example, an output
                // port may have two buses with their widths not fixed.
                // Furthermore, if one of the buses is connected to an input
                // port and its width can be determined from the internal
                // connections associated with that input port, the width of
                // the other bus can also be resolved. However, to support this,
                // a fixed-point iteration has to be performed, but there is
                // no guarantee of existence of a useful fixed-point whose
                // widths are all non-zero. Therefore, we take the conservative
                // approach.
                // To infer the unknown width, we resolve the equation where
                // the sum of the widths of input relations equals the sum of
                // those of output relations.
                int portInsideWidth = 0;
                int portOutsideWidth = 0;
                int difference = 0;

                if (p.isInsideGroupLinked(this)) {
                    // I am linked on the inside...
                    portInsideWidth = p._getInsideWidth(this);
                    portOutsideWidth = p._getOutsideWidth(null);

                    // the same as portOutsideWidth = p.getWidth();
                    difference = portOutsideWidth - portInsideWidth;
                } else if (p.isLinked(this)) {
                    // I am linked on the outside...
                    portInsideWidth = p._getInsideWidth(null);
                    portOutsideWidth = p._getOutsideWidth(this);
                    difference = portInsideWidth - portOutsideWidth;
                }

                if (difference > _inferredWidth) {
                    _inferredWidth = difference;
                }
            }

            _inferredWidthVersion = version;
        }

        return _inferredWidth;
    }

    /** Create an initialize the width parameter. */
    private void _init() {
        try {

            _auto = new Parameter(this, "Auto");
            _auto.setExpression(Integer.toString(WIDTH_TO_INFER));
            _auto.setTypeEquals(BaseType.INT);
            _auto.setVisibility(Settable.NONE);
            _auto.setPersistent(false);

            width = new Parameter(this, "width");
            width.setExpression("Auto");
            width.setTypeEquals(BaseType.INT);

        } catch (KernelException ex) {
            throw new InternalErrorException(ex);
        }
    }

    /** Set the width of this relation and all relations in its
     *  relation group. The width is the number of
     *  channels that the relation represents.  If the argument
     *  is equal to the value of WIDTH_TO_INFER, then the relation becomes
     *  a bus with unspecified width,
     *  and the width will be inferred from the way the relation is used
     *  (but will never be less than zero).
     *  This method invalidates
     *  the resolved types on the director of the container, if there is
     *  one, and notifies each connected actor that its connections
     *  have changed.
     *  This method write-synchronizes on the workspace.
     *
     *  @param width The width of the relation.
     *  @exception IllegalActionException If the argument is not zero, one,
     *   or equal to WIDTH_TO_INFER and the relation is linked to a
     *   non-multiport. Or when the argument is less than zero and different
     *   from WIDTH_TO_INFER.
     *  @see ptolemy.kernel.util.Workspace#getWriteAccess()
     *  @see #getWidth()
     */
    private void _setWidth(int width) throws IllegalActionException {
        if (_USE_NEW_WIDTH_INFERENCE_ALGO) {
            if (width == _cachedWidth) {
                // No change.
                return;
            }
            try {
                _workspace.getWriteAccess();

                // Check legitimacy of the change.
                if (width < 0 && width != WIDTH_TO_INFER) {
                    throw new IllegalActionException(this, "" + width
                            + " is not a valid width for this relation.");
                }

                /* rodiers: I'd rather keep the following exception since it makes the
                 * model more consistent, but some tests seem to use this pattern.
                 *
                // Check for non-multiports on a link: should either be 0, 1 or should be inferred.
                 if (width != 1 && width != 0 && width != WIDTH_TO_INFER) {
                     for (Object object : linkedPortList()) {
                         IOPort p = (IOPort) object;
                         if (!p.isMultiport()) {
                             throw new IllegalActionException(this, p,
                                 "Cannot make bus because the "
                                 + "relation is linked to a non-multiport.");
                         }
                     }
                 }
                 */

                _cachedWidth = width;

                // Set the width of all relations in the relation group.
                Iterator<?> relations = relationGroupList().iterator();

                while (!_suppressWidthPropagation && relations.hasNext()) {
                    IORelation relation = (IORelation) relations.next();

                    if (relation == this) {
                        continue;
                    }

                    // If the relation has a width parameter, set that
                    // value. Otherwise, just set its width directly.
                    // Have to disable back propagation.
                    try {
                        relation._suppressWidthPropagation = true;
                        relation.width.setToken(new IntToken(width));
                    } finally {
                        relation._suppressWidthPropagation = false;
                    }
                }

                // Invalidate schedule and type resolution.
                Nameable container = getContainer();

                if (container instanceof CompositeActor) {
                    ((CompositeActor) container).notifyConnectivityChange();
                    Director director = ((CompositeActor) container)
                            .getDirector();

                    if (director != null) {
                        director.invalidateSchedule();
                        director.invalidateResolvedTypes();

                    }
                }

                // According to the comments this used to happen for this reason:
                //      Do this as a second pass so that it does not
                //      get executed if the change is aborted
                //      above by an exception.
                //      FIXME: Haven't completely dealt with this
                //      possibility since the above changes may have
                //      partially completed.
                // With the new width inference algorithm the code below does not
                // need to be executed for this reason. However some actors
                // do some initialization in the connectionsChanged method that they
                // don't do at other points of time.

                for (Object port : linkedPortList()) {
                    IOPort p = (IOPort) port;
                    Entity portContainer = (Entity) p.getContainer();

                    if (portContainer != null) {
                        portContainer.connectionsChanged(p);
                    }
                }
            } finally {
                // About conditionally executing doneWriting:
                //      When the user updates the width we want to increase
                //      the workspace version to invalidate widths that are being
                //      cached by IOPort.
                //      Parameters however have in some sense a strange behavior,
                //      when the user sets a Parameter (such as width) this typically
                //      happens with setToken, which - in case the Parameter is not
                //      lazy - immediately results in the call attributedChanged, which
                //      will call _setWidth. However when the project is opened, setExpression
                //      is used. In the case the expression in not immediately evaluated, but
                //      only when it is necessary. When you call getToken, the expression is evaluated
                //      which results in the call of attributedChanged, which again results
                //      in the call _setWidth. In this case however the model didn't change this
                //      time, but earlier. Typically this happens after opening the model, of creating
                //      a new relation. This already increased the version of the model, and hence
                //      cached values or refreshed. Triggering of the width then happens when doing
                //      width inference. Then we don't want to increase the version number since it would
                //      invalidate all widths immediately.
                //      Probably this construct works in practise, but it remains a dangerous one...
                if (_inferringWidths()) {
                    _workspace.doneTemporaryWriting();
                } else {
                    _workspace.doneWriting();
                }
            }
        } else {
            if (width == _cachedWidth) {
                // No change.
                return;
            }
            try {
                _workspace.getWriteAccess();

                if (width <= 0) {
                    // Check legitimacy of the change.
                    try {
                        _inferWidth();
                    } catch (InvalidStateException ex) {
                        throw new IllegalActionException(this, ex,
                                "Cannot use unspecified width on this relation "
                                        + "because of its links.");
                    }
                }

                // Check for non-multiports on a link.
                /* This is now allowed.
                 if (width != 1) {
                 Iterator ports = linkedPortList().iterator();

                 while (ports.hasNext()) {
                 IOPort p = (IOPort) ports.next();

                 // Check for non-multiports.
                 if (!p.isMultiport()) {
                 throw new IllegalActionException(this, p,
                 "Cannot make bus because the "
                 + "relation is linked to a non-multiport.");
                 }
                 }
                 }
                 */
                _cachedWidth = width;

                // Set the width of all relations in the relation group.
                Iterator<?> relations = relationGroupList().iterator();

                while (!_suppressWidthPropagation && relations.hasNext()) {
                    IORelation relation = (IORelation) relations.next();

                    if (relation == this) {
                        continue;
                    }

                    // If the relation has a width parameter, set that
                    // value. Otherwise, just set its width directly.
                    // Have to disable back propagation.
                    try {
                        relation._suppressWidthPropagation = true;
                        relation.width.setToken(new IntToken(width));
                    } finally {
                        relation._suppressWidthPropagation = false;
                    }
                }

                // Do this as a second pass so that it does not
                // get executed if the change is aborted
                // above by an exception.
                // FIXME: Haven't completely dealt with this
                // possibility since the above changes may have
                // partially completed.
                Iterator<?> ports = linkedPortList().iterator();

                while (ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();
                    Entity portContainer = (Entity) p.getContainer();

                    if (portContainer != null) {
                        portContainer.connectionsChanged(p);
                    }
                }

                // Invalidate schedule and type resolution.
                Nameable container = getContainer();

                if (container instanceof CompositeActor) {
                    Director director = ((CompositeActor) container)
                            .getDirector();

                    if (director != null) {
                        director.invalidateSchedule();
                        director.invalidateResolvedTypes();
                    }
                }
            } finally {
                _workspace.doneWriting();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         packaged variables                 ////

    /** Indicate whether the new or the old width inference algo should be used
     *  This is a packaged field.
     */
    public static final boolean _USE_NEW_WIDTH_INFERENCE_ALGO = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached inferred width.
    private transient int _inferredWidth = -1;

    // The workspace version for the cached inferred width.
    private transient long _inferredWidthVersion = -1;

    // Suppress propagation of width changes.
    private boolean _suppressWidthPropagation = false;

    // The cached value of the width parameter.
    private int _cachedWidth = -2;

}
