/* This actor implements a publisher in a HLA/CERTI federation.

@Copyright (c) 2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package org.hlacerti.lib;

import java.util.HashMap;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HlaPublisher

/**
 * <p>This actor implements a publisher in a HLA/CERTI federation. This
 * publisher is associated to one HLA attribute. Ptolemy's tokens, received in
 * the input port of this actor, are interpreted as an updated value of the
 * HLA attribute. The updated value is published to the whole HLA Federation
 * by the {@link HlaManager} attribute, deployed in a Ptolemy model.
 * </p><p>
 * The name of this actor is mapped to the name of the HLA attribute in the
 * federation and need to match the Federate Object Model (FOM) specified for
 * the Federation. The data type of the input port has to be the same type of
 * the HLA attribute. The parameter <i>classObjectHandle</i> needs to match the
 * attribute object class describes in the FOM. The parameter
 * <i>useHlaPtidesEvent</i> indicates if we need to handle PTIDES events as
 * RecordToken for HLA events.
 * </p>
 *
 *  @author Gilles Lasnier, Contributors: Patricia Derler, David Come
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaPublisher extends TypedAtomicActor {

    /** Construct the HlaPublisher actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *  by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *  actor with this name.
     */
    public HlaPublisher(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // The single output port of the actor.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        
        classObjectHandle = new Parameter(this, "classObjectHandle");
        classObjectHandle.setDisplayName("Object class in FOM");
        classObjectHandle.setTypeEquals(BaseType.STRING);
        classObjectHandle.setExpression("\"myObjectClass\"");
        attributeChanged(classObjectHandle);

        useHLAPtidesEvent = new Parameter(this, "useHLAPtidesEvent");
        useHLAPtidesEvent.setTypeEquals(BaseType.BOOLEAN);
        useHLAPtidesEvent.setExpression("false");
        useHLAPtidesEvent.setDisplayName("use HLA PTIDES event");
        attributeChanged(useHLAPtidesEvent);

        useCertiMessageBuffer = new Parameter(this, "useCertiMessageBuffer");
        useCertiMessageBuffer.setTypeEquals(BaseType.BOOLEAN);
        useCertiMessageBuffer.setExpression("false");
        useCertiMessageBuffer.setDisplayName("use CERTI message buffer");
        attributeChanged(useCertiMessageBuffer);

        _hlaManager = null;
        _useHLAPtidesEvent = false;
        _useCertiMessageBuffer = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The object class of the HLA attribute to publish. */
    public Parameter classObjectHandle;

    /** Indicate if the event is for a PTIDES platform. */
    public Parameter useHLAPtidesEvent;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    public Parameter useCertiMessageBuffer;

    /** The input port. */
    public TypedIOPort input = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the attributeChanged method of the parent. Check if the
     *  user as set the object class of the HLA attribute to subscribe to.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the object class parameter is
     *  empty.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == classObjectHandle) {
            String value = ((StringToken) classObjectHandle.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
        } else if (attribute == useCertiMessageBuffer) {
            _useCertiMessageBuffer = ((BooleanToken) useCertiMessageBuffer
                    .getToken()).booleanValue();
        } else if (attribute == useHLAPtidesEvent) {
            _useHLAPtidesEvent = ((BooleanToken) useHLAPtidesEvent.getToken())
                    .booleanValue();
        }
        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HlaPublisher newObject = (HlaPublisher) super.clone(workspace);
        newObject._hlaManager = _hlaManager;
        newObject._useCertiMessageBuffer = _useCertiMessageBuffer;
        newObject._useHLAPtidesEvent = _useHLAPtidesEvent;
        return newObject;
    }

    /** Retrieve and check if there is one and only one {@link HlaManager}
     *  deployed in the Ptolemy model. The {@link HlaManager} provides the
     *  method to publish an updated value of a HLA attribute to the HLA/CERTI
     *  Federation.
     *  @exception IllegalActionException If there is zero or more than one
     *  {@link HlaManager} per Ptolemy model.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        CompositeActor ca = (CompositeActor) this.getContainer();

        List<HlaManager> hlaManagers = ca.attributeList(HlaManager.class);
        if (hlaManagers.size() > 1) {
            throw new IllegalActionException(this,
                    "Only one HlaManager attribute is allowed per model");
        } else if (hlaManagers.size() < 1) {
            throw new IllegalActionException(this,
                    "A HlaManager attribute is required to use this actor");
        }

        // Here, we are sure that there is one and only one instance of the
        // HlaManager in the Ptolemy model.
        _hlaManager = hlaManagers.get(0);
    }

    /** Each tokens, received in the input port, are transmitted to the
     *  {@link HlaManager} for a publication to the HLA/CERTI Federation.
     */
    @Override
    public void fire() throws IllegalActionException {
       for(int i = 0 ; i < input.getWidth() ; ++i) {
        if (input.hasToken(i)) {
            Token in = input.get(i);
            String name = input.sourcePortList().get(i).getContainer().getDisplayName();
            _hlaManager.updateHlaAttribute(this, in,_registeredObject.get(name));

            if (_debugging) {
                _debug(this.getDisplayName()
                        + " Called fire() - the update value \""
                        + in.toString() + "\" of the HLA Attribute \""
                        + this.getName() + "\" has been sent to \""
                        + _hlaManager.getDisplayName() + "\"");
            }
        }
    }
    
    }

    /** Indicate if the HLA publisher actor uses the CERTI message
     *  buffer API.
     */
    public boolean useCertiMessageBuffer() throws IllegalActionException {
        return _useCertiMessageBuffer;
    }

    /** Indicate if the HLA publisher actor sends events to a PTIDES
     *  platform.
     */
    public boolean useHLAPtidesEvent() throws IllegalActionException {
        return _useHLAPtidesEvent;
    }

    /**
     *  Establish the mapping between id and opaqueIdentifier for all the HLASubscribers
     * @param id : the object's id given by the RTIG
     * @param opaqueIdentifier : the identifier we map to the above id
     */
    public void register(String opaqueIdentifier,Integer id) {        
        _registeredObject.put(opaqueIdentifier, id);
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A reference to the associated {@link HlaManager}. */
    private HlaManager _hlaManager;

    /** Indicate if the event is for a PTIDES platform. */
    private boolean _useHLAPtidesEvent;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    private boolean _useCertiMessageBuffer;
    
    /** Shared HashMap between all HlaPublishers for remembering with
     * what id an actor as been registered (as an object instance)
     * in the federation 
     */
    private static HashMap<String,Integer> _registeredObject = new HashMap();
}
