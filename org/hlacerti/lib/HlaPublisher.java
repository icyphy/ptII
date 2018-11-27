/* This actor provides information to publish, register and update values in
 * the Ptolemy-HLA/CERTI framework.

@Copyright (c) 2013-2018 The Regents of the University of California.
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
 * This actor provides updates to shared objects in an HLA (High Level Architecture)
 * federation. The shared objects are are instances of classes defined in a
 * FED file (Federation Execution Data). Each class has attributes, and this
 * actor sends updates to an attribute of an instance of a class when it
 * receives data on its input.  If there are instances of {@link HlaSubscriber}
 * that refer to the same attribute of the same instance, then those will be
 * notified of the update.
 * 
 * This actor requires that there be an instance of {@link HlaManager} in the
 * same model. That instance references the FED file that is required to
 * define the class referred to in this HlaPublisher, and that class is
 * required to have an attribute with a name matching the name given here.
 * 
 * The name of the instance is arbitrary. If an instance with the specified
 * name does not already exist, it will be created during initialization of
 * the model. If two instances of this HlaPublisher actor refer to the
 * same instance name, then they will send updates to the same HLA instance,
 * but they are required to update distinct attributes of that instance.
 * 
 * During initialization, the HlaManager will notify the RTI (Run Time
 * Infrastructure) of the intent to update this particular attribute
 * of this particular instance of the class.
 * 
 * <p>This actor provides 3 information: a class name <i>C</i>, an attribute
 * name <i>attr</i> and an instance name <i>i</i>. To each HlaPublisher actor
 * correspond a unique HlaSubscriber actor in each other federate that wants
 * to receive the updates for the attribute <i>attr</i> of instance named <i>i</i>.
 * Let us recall some terms:
 * - FOM: Federation Object Model
 * - FED: Federation Execution Data, contains classes and attributes defined
 *   in the FOM and, for each attribute, if it is timestamped and its QoS 
 * - RTI: Run-Time Infrastructure. The RTI has a Central RTI Component (CRC)
 *   and one or more Local RTI Components (LRC). The LRC has a numerical 
 *   representation (handle) for all object classes and object class attributes
 *   contained in the FED file.
 * - RTIa: RTI Ambassador interface; Federates can communicate with the RTI
 *   (LRC) through the RTIa.
 * 
 * The information supplied in this actor by the user is used in the following
 * way by the {@link HlaManager} attribute (deployed in the same model):
 * 
 * 1. During the initialization phase, the {@link HlaManager} calls the HLA
 * services for:
 *  - Publishing all the <i>j</i attributes <i>attr-j</i of a class  <i>C</i>
 *    (gathered from <i>j</i HlaPublisher actors) by calling
 *    rtia.publishObjectClass(classHandle, _attributesLocal), where <i>classHandle</i
 *    is provided by calling the service rtia.getObjectClassHandle() for  <i>C</i>;
 *    _attributesLocal is the set constructed by calling rtia.getAttributeHandle()
 *    for each <i>attr-j</i in this Ptolemy federate  model (the set is obtained
 *    from all HlaPublisher actors); 
 *  - Registering an instance of class <i>C</i> named <i>i</i> for this federate, 
 *    informing the LRC that a new object instance exists:
 *    rtia.registerObjectInstance(classHandle, classInstanceName), with 
 *    classInstanceName = <i>i</i>.
 *
 * 2. During the simulation loop phase, the {@link HlaManager} calls the UAV service
 * for updating the value of an attribute of a class instance. Each HlaPublisher
 * actor is related to one UAV call:
 * _rtia.updateAttributeValues(objectInstanceId, suppAttributes, tag, uavTimeStamp).
 * The UAV service is send after a cycle in the {@link HlaManager} that starts
 * with an event e(t) received at the input port of the HlaPublisher actor, and
 * when the federate eventually has advanced its time to <i>uavTimeStamp<\i>=t'.
 * The value of t' depends on the time management (NER or TAR), see {@link
 * HlaManager} code.
 * The optional parameter <i>tag</i> is not used in the current implementation.
 * </p><p>
 * Parameters <i>classObjectName</i> and <i>attributeName</i> need to match the
 * name of the class and the attribute defined in the Federate Object Model
 * (FOM) specified for the Federation and indicated in the FED file.
 * The data type of the input port of this actor must have the same type of the
 * HLA attribute (defined in the FOM, not present in the FED file). 
 * </p><p>
 * The parameter <i>classInstanceName</i> is chosen by the user. Each
 * federate has the ownership of the attributes of an instance of object that
 * it publishes. 
 * </p><p> How to define a FOM? The design of a FOM needs a careful attention.
 * One of the criteria is the reusability of the FOM and federates. A good 
 * and simple choice is design a FOM with a hierarchy such that all 
 * attributes of a (sub-)class are published by a same federate. But this is
 * not mandatory. For example, consider a FOM of a federation with a class C
 * with 3 attributes attr2, attr2 and attr3. The federation has 2 federates, F1
 * and F2. If F1 publishes C.attr1 and C.attr3 and F2 publishes C.attr2, then
 * the name of the instance of class C must be different in both federates. 
 * Federate F1 has 2 HlaPublisher actors: C.attr1.i1 and C.attr3.i1
 * (it publishes two attributes of a same class instance); Federate F2 has one
 * HlaPublisher actor: C.attr3.i2 (it publishes one attribute of a class
 * instance, whose name is different from the the one published by F1).
 * </p><p>
 * See two different federations using the same FOM and simulating the same
 * system: 2 billard balls of class Bille sending their position in a viewer.
 *  </p><p>
 * The parameter <i>useCertiMessageBuffer</i> is chosen by the user. It 
 * indicates if the event is wrapped in a CERTI message buffer,
 * certi.communication.MessageBuffer, see {@link MessageProcessing}. All
 * HlaSubscriber actors with parameters <i>{C, attr, i}</i> must have the same
 * choice for this parameter.
 * </p><p>
 *
 *  @author Gilles Lasnier, Contributors: Patricia Derler, David Come
 *  @version $Id: HlaPublisher.java 214 2018-04-01 13:32:02Z j.cardoso $
 *  @since Ptolemy II 11.0
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

        // The single input port of the actor.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(false);

        // HLA attribute name as defined in the FOM.
        attributeName = new Parameter(this, "attributeName");
        attributeName.setDisplayName("Name of the attribute to publish");
        attributeName.setTypeEquals(BaseType.STRING);
        attributeName.setExpression("\"HLAattributName\"");
        attributeChanged(attributeName);

        // HLA object class as defined in the FOM.
        classObjectName = new Parameter(this, "classObjectName");
        classObjectName.setDisplayName("Object class in FOM");
        classObjectName.setTypeEquals(BaseType.STRING);
        classObjectName.setExpression("\"HLAobjectClass\"");
        attributeChanged(classObjectName);

        // HLA class instance name given by the user.
        classInstanceName = new Parameter(this, "classInstanceName");
        classInstanceName.setDisplayName("Name of the HLA class instance");
        classInstanceName.setTypeEquals(BaseType.STRING);
        classInstanceName.setExpression("\"HLAclassInstanceName\"");
        attributeChanged(classInstanceName);

        // CERTI message buffer encapsulation
        useCertiMessageBuffer = new Parameter(this, "useCertiMessageBuffer");
        useCertiMessageBuffer.setTypeEquals(BaseType.BOOLEAN);
        useCertiMessageBuffer.setExpression("false");
        useCertiMessageBuffer.setDisplayName("use CERTI message buffer");
        attributeChanged(useCertiMessageBuffer);

        // Initialize default private values.
        _hlaManager = null;
        _useCertiMessageBuffer = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The HLA attribute name the HLASubscriber is mapped to. */
    public Parameter attributeName;

    /** The object class of the HLA attribute to publish. */
    public Parameter classObjectName;

    /** The name of the HLA class instance for this HlaSubscriber. */
    public Parameter classInstanceName;

    /** The input port. */
    public TypedIOPort input = null;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    public Parameter useCertiMessageBuffer;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the attributeChanged method of the parent. Check if the
     *  user has set all information related to HLA to publish.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If one of the parameters is
     *  empty.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == attributeName) {
            String value = ((StringToken) attributeName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
        } else if (attribute == classObjectName) {
            String value = ((StringToken) classObjectName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
        }
        if (attribute == classInstanceName) {
            String value = ((StringToken) classInstanceName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
        } else if (attribute == useCertiMessageBuffer) {
            _useCertiMessageBuffer = ((BooleanToken) useCertiMessageBuffer
                    .getToken()).booleanValue();
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
        return newObject;
    }

    /** Retrieve and check if there is one and only one {@link HlaManager}
     *  deployed in the Ptolemy model. The {@link HlaManager} provides the
     *  method to publish the attributes of a class, register an instance of a
     *  class and update the value of a HLA attribute of a class instance to
     *  the HLA/CERTI Federation.
     *  @exception IllegalActionException If there is zero or more than one
     *  {@link HlaManager} per Ptolemy model.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Find the HlaManager by looking into container
        // (recursively if needed).
        // FIXMEjc: it really looks recursively? The HlaManager must be in a DE
        // top level model.
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

    /** All tokens, received in the input port, are transmitted to the
     *  {@link HlaManager} for a publication to the HLA/CERTI Federation.
     */
    @Override
    public void fire() throws IllegalActionException {

        super.fire();

        for (int i = 0; i < input.getWidth(); ++i) {
            if (input.hasToken(i)) {
                Token in = input.get(i);
                _hlaManager.updateHlaAttribute(this, in);
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

    /** Return the HLA attribute name indicated in the HlaPublisher actor
     * that will be used by HLA services (publish and update). The attribute
     * must be defined in the FED file. It belongs to the class classObjectName.
     *  @return the HLA Attribute name.
     *  @exception IllegalActionException if a bad token string value is provided
     */
    public String getAttributeName() throws IllegalActionException {
        String parameter = "";
        try {
            parameter = ((StringToken) attributeName.getToken()).stringValue();
        } catch (IllegalActionException illegalActionException) {
            throw new IllegalActionException(this,
                    "Bad attributeName token string value");
        }
        return parameter;
    }

    /** Return the name of the HLA class instance indicated in the HlaPublisher
     *  actor that will be used by HLA services (register and update). It is
     *  chosen by the user.
     *  @return the HLA class instance name.
     *  @exception IllegalActionException if a bad token string value is provided.
     */
    public String getClassInstanceName() throws IllegalActionException {
        String parameter = "";
        try {
            parameter = ((StringToken) classInstanceName.getToken())
                    .stringValue();
        } catch (IllegalActionException illegalActionException) {
            throw new IllegalActionException(this,
                    "Bad classInstanceName token string value");
        }
        return parameter;
    }

    /** Return the HLA class object name indicated in the HlaPublisher actor.
     * The class must be defined in the FED file and has the attribute
     * attributeName.
     *  @return the HLA object class name.
     *  @exception IllegalActionException if a bad token string value is provided.
     */
    public String getClassObjectName() throws IllegalActionException {
        String parameter = "";
        try {
            parameter = ((StringToken) classObjectName.getToken())
                    .stringValue();
        } catch (IllegalActionException illegalActionException) {
            throw new IllegalActionException(this,
                    "Bad classObjectName token string value");
        }
        return parameter;
    }

    /** Indicate if the HLA publisher actor uses the CERTI message
     *  buffer API.
     *  @return true if the HLA publisher actor uses the CERTI message and
     *  false if it doesn't.
     */
    public boolean useCertiMessageBuffer() {
        // XXX: FIXME: where is the exception management ?
        return _useCertiMessageBuffer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A reference to the associated {@link HlaManager}. */
    private HlaManager _hlaManager;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    private boolean _useCertiMessageBuffer;

}
