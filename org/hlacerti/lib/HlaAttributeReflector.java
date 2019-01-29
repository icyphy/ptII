/* This actor provides information to subscribe, discover and reflect updated
 * values in the Ptolemy-HLA/CERTI framework.
 
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypeEvent;
import ptolemy.actor.TypeListener;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HlaAttributeReflector

/**
 * This actor produces an output event whenever the specified attribute
 * of the specified instance is updated by another federate in the federation.
 * In HLA, the terminology is that this actor "reflects" the specified attribute
 * value whenever another federate "updates" the attribute value.
 * The time stamp of the output depends on the parameters of the
 * {@see HlaManager}, as explained in the documentation for that class.
 * This actor assumes that there is exactly one HlaManager in the model
 * that contains this actor.
 * <p>
 * The attribute that this actor reflects is specified using three parameters.
 * The <i>className</i> parameter is used to discover an instance of the
 * specified class whose name matches the <i>instanceName</i> parameter.
 * When such an instance is discovered, this actor begins reflecting any
 * updates to the attribute, specified by <i>attributeName</i>, of that
 * instance of that class.  The <i>className</i> and <i>attributeName</i>
 * are required to match a class and attribute specified in the FED file
 * that is specified in the HlaManager.
 * <p>
 * FIXME: Explain the data types.
 * 
 * This actor is associated with a unique HLA attribute of a given object 
 * instance. Reflected values of this HLA attribute are received from the HLA
 * Federation by the {@link HlaManager} attribute deployed in the same model. 
 * The {@link HlaManager} invokes the putReflectedAttribute() to put the 
 * received value in the HlaAttributeReflector actor token queue and to program its 
 * firing time, using the _fireAt() method.
 * 
 *  </p><p>
 * Parameters <i>className</i> and <i>attributeName</i> need to match the
 * name of the class and the name of the attribute defined in the Federate 
 * Object Model (FOM) specified for the Federation and indicated in the FED file.
 * The data type of the output port of this actor must have the same type of the
 * HLA attribute (defined in the FOM, not present in the FED file). 
 * </p><p>
 * The parameter <i>instanceName</i> is chosen by the user. 
 * 
 * </p><p>
 * Entering in more details:
 * This actor provides 3 information: a class name <i>C</i>, an attribute
 * name <i>attr</i> and an instance name <i>i</i>. To each HlaAttributeReflector actor
 * in a Ptolemy model (federate) corresponds a unique {@link HlaPublisher}
 * actor in a (unique) federate with the same information.
 * Let us recall some terms:
 * - FOM: Federation Object Model
 * - FED: Federation Execution Data, contains classes and attributes defined
 *   in the FOM and, for each attribute, if it is timestamped and its QoS 
 * - RTI: Run-Time Infrastructure. The RTI has a Central RTI Component (CRC)
 *   and one or more Local RTI Components (LRC). The LRC has a numerical 
 *   representation (handle) for all object classes and object class attributes
 *   contained in the FED file.
 * 
 * The information supplied in this actor by the user is used in the following
 * way by the {@link HlaManager} attribute (deployed in the same model):
 * 
 * 1. During the initialization phase, the {@link HlaManager}: 
 *  - Subscribes to all the <i>k</i attributes <i>attr-k</i of a class  <i>C</i>
 *    (gathered from <i>k</i HlaAttributeReflector actors) by calling
 *    _rtia.subscribeObjectClassAttributes(classHandle, _attributesLocal), 
 *    where <i>classHandle</i is obtained by calling the HLA service 
 *    rtia.getObjectClassHandle() for  <i>C</i>;
 *    _attributesLocal is the set constructed by calling rtia.getAttributeHandle()
 *    for each <i>attr</i in this Ptolemy federate  model (the set is obtained
 *    from all HlaAttributeReflector actors that has the same class  <i>C</i>); 
 *    - Receives the HLA callback informing the discovering of an instance of
 *    class <i>C</i> named <i>i</i>:
 *    rtia.discoverObjectInstance(objectInstanceId, classHandle, someName), with 
 *    someName = <i>i</i>; objectInstanceId and classHandle are handles provided
 *    by the RTI.
 *    
 * 2. During the simulation loop phase, the {@link HlaManager} receives the RAV
 * callback from the RTI with the new value of an attribute of a class instance. Each 
 * HlaAttributeReflector  actor is related to one RAV callback:
 * rtia.reflectAttributeValues(objectInstanceId, suppAttributes, tag, ravTimeStamp).
 * The RAV callback, with a timestamp t'=<i>ravTimeStamp<\i> is received at the
 * input port of the HlaPublisher actor, during the advance time phase that
 * starts when the federate wants to advanced its time to <i>t<\i> (using NER or
 * TAR time management, see {@link HlaManager} code).
 * The optional parameter <i>tag</i> is not used in the current implementation.
 * </p><p>
 * 
 * 
 *  @author Gilles Lasnier, Contributors: Patricia Derler, David Come
 *  @version $Id: HlaAttributeReflector.java 214 2018-04-01 13:32:02Z j.cardoso $
 *  @since Ptolemy II 11.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaAttributeReflector extends TypedAtomicActor {

    /** Construct a HlaAttributeReflector actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *  by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *  actor with this name.
     */
    public HlaAttributeReflector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // The single output port of the actor.
        output = new TypedIOPort(this, "output", false, true);

        // HLA attribute name.
        attributeName = new Parameter(this, "attributeName");
        attributeName.setDisplayName("Name of the attribute to receive");
        attributeName.setTypeEquals(BaseType.STRING);
        attributeName.setExpression("\"HLAattributName\"");

        // HLA object class in FOM.
        className = new Parameter(this, "className");
        className.setDisplayName("Object class in FOM");
        className.setTypeEquals(BaseType.STRING);
        className.setExpression("\"HLAobjectClass\"");

        // HLA class instance name.
        instanceName = new Parameter(this, "instanceName");
        instanceName.setDisplayName("Name of the HLA class instance");
        instanceName.setTypeEquals(BaseType.STRING);
        instanceName.setExpression("\"HLAinstanceName\"");

        // Propagate parameter changes after parameter instantiations.
        attributeChanged(className);
        attributeChanged(instanceName);
        attributeChanged(attributeName);

        // Basic token types available.
        typeSelector = new StringParameter(this, "typeSelector");
        typeSelector.setDisplayName("type of the parameter");
        typeSelector.addChoice("int");
        typeSelector.addChoice("double");
        typeSelector.addChoice("string");
        typeSelector.addChoice("boolean");

        // Allow the user to change the output port's type directly.
        // Useful for setting a value to typeSelector after reading the MomL file.
        output.addTypeListener(new TypeListener() {
            @Override
            public void typeChanged(TypeEvent event) {
                typeSelector.setExpression(event.getNewType().toString());
            }
        });

        // CERTI message buffer encapsulation.
        useCertiMessageBuffer = new Parameter(this, "useCertiMessageBuffer");
        useCertiMessageBuffer.setTypeEquals(BaseType.BOOLEAN);
        useCertiMessageBuffer.setExpression("false");
        useCertiMessageBuffer.setDisplayName("use CERTI message buffer");
        attributeChanged(useCertiMessageBuffer);

        // Initialize default private values.
        _reflectedAttributeValues = new LinkedList<HlaTimedEvent>();
        _useCertiMessageBuffer = false;

        // Set handle to impossible values <= XXX: FIXME: GiL: true ?
        _attributeHandle = Integer.MIN_VALUE;
        _classHandle = Integer.MIN_VALUE;
        _objectInstanceId = Integer.MIN_VALUE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The HLA attribute name the HlaAttributeReflector is mapped to. */
    public Parameter attributeName;

    /** The object class of the HLA attribute to subscribe to. */
    public Parameter className;

    /** The name of the HLA class instance for this HlaAttributeReflector. */
    public Parameter instanceName;

    /** The output port. */
    public TypedIOPort output;

    /** The type of the output port specified through the user interface. */
    public StringParameter typeSelector;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    public Parameter useCertiMessageBuffer;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the attributeChanged method of the parent. Check if the
     *  user as set all information relative to HLA to subscribe to, for
     *  discovering instances and receive (reflect) updated values.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If one of the parameters
     *  is empty.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == attributeName) {
            String attributeNameValue = ((StringToken) attributeName.getToken())
                    .stringValue();
            if (attributeNameValue.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty attributeName!");
            }
        } else if (attribute == instanceName) {
            String instanceNameValue = ((StringToken) instanceName
                    .getToken()).stringValue();
            if (instanceNameValue.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty instanceName!");
            }
        } else if (attribute == className) {
            String classNameValue = ((StringToken) className.getToken())
                    .stringValue();
            if (classNameValue.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty className!");
            }
        } else if (attribute == useCertiMessageBuffer) {
            _useCertiMessageBuffer = ((BooleanToken) useCertiMessageBuffer
                    .getToken()).booleanValue();
        } else if (attribute == typeSelector) {
            String newPotentialTypeName = typeSelector.stringValue();
            // XXX: FIXME: What is the purpose of this test ?
            if (newPotentialTypeName == null) {
                return;
            }

            Type newPotentialType = BaseType.forName(newPotentialTypeName);
            if (newPotentialType != null
                    && !newPotentialType.equals(output.getType())) {
                output.setTypeEquals(newPotentialType);
            }
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
        // Manage public members.
        HlaAttributeReflector newObject = (HlaAttributeReflector) super.clone(workspace);

        // Manage private members.
        newObject._reflectedAttributeValues = new LinkedList<HlaTimedEvent>();
        newObject._hlaManager = _hlaManager;
        newObject._useCertiMessageBuffer = _useCertiMessageBuffer;

        newObject._attributeHandle = _attributeHandle;
        newObject._classHandle = _classHandle;
        newObject._objectInstanceId = Integer.MIN_VALUE;

        return newObject;
    }

    /** Check if there is one and only one {@link HlaManager} deployed in the
     *  Ptolemy model.
     *  @exception IllegalActionException If there is zero or more than one
     *  {@link HlaManager} per Ptolemy model.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Find the HlaManager by looking into container
        // (recursively if needed).
        //FIXMEjc: the HlaManager must be in the top level model with a DE
        //director.
        CompositeActor ca = (CompositeActor) this.getContainer();
        List<HlaManager> hlaManagers = null;

        while (ca != null) {
            hlaManagers = ca.attributeList(HlaManager.class);
            if (hlaManagers.size() < 1) {
                ca = (CompositeActor) ca.getContainer();
            } else {
                break;
            }
        }

        if (hlaManagers == null || hlaManagers.size() < 1) {
            throw new IllegalActionException(this,
                    "A HlaManager attribute is required to use this actor");
        } else if (hlaManagers.size() > 1) {
            throw new IllegalActionException(this,
                    "Only one HlaManager attribute is allowed per model");
        }
        // Here, we are sure that there is one and only one instance of the
        // HlaManager in the Ptolemy model.
        _hlaManager = hlaManagers.get(0);

    }

    /** Put in the Ptolemy queue the token (with time-stamp t) corresponding to
     *  the RAV (with time-stamp t'=<i>ravTimeStamp<\i> related to the HLA
     *  attribute of an instance (mapped to this actor). The value of t depends
     *  on t' and the time management used (NER or TAR, see {@link HlaManager} code).
     *  @exception IllegalActionException Not thrown here.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();

        Iterator<HlaTimedEvent> it = _reflectedAttributeValues.iterator();

        // Get first event on RAV list.
        it.hasNext();
        TimedEvent te = it.next();

        if (te.timeStamp.compareTo(currentTime) == 0) {
            // Build token with HLA value.
            Token content = _buildToken((Object[]) te.contents);

            // XXX: FIXME: to remove after cleaning ?
            int fromObjectInstanceId = -1;
            if (te instanceof HlaTimedEvent) {
                HlaTimedEvent he = (HlaTimedEvent) te;
                fromObjectInstanceId = he.getHlaObjectInstanceId();
            }

            // Either it is NOT a HlaTimedEvent and we let it go,
            // either it is and it has to match the HLA object instance
            // ID of this HlaAttributeReflector.

            // XXX: FIXME: what to do if this is not a HlaTimedEvent? (-1 case)
            if (fromObjectInstanceId == -1
                    || fromObjectInstanceId == _objectInstanceId) {
                this.outputPortList().get(0).send(0, content);

                if (_debugging) {
                    _debug(this.getDisplayName()
                            + " Called fire() - An updated value"
                            + " of the HLA attribute \"" + getHlaAttributeName()
                            + " from " + fromObjectInstanceId
                            + "\" has been sent at \"" + te.timeStamp + "\" ("
                            + content.toString() + ")");
                }

            }

            it.remove();
        }

        // Refire if token to process at same time
        if (it.hasNext()) {
            TimedEvent tNext = it.next();
            if (tNext.timeStamp.compareTo(currentTime) == 0) {
                // Refiring the actor to handle the other tokens
                // that are still in channels
                getDirector().fireAt(this, currentTime);
            }
        }
    } // End of fire.

    /** Return the HLA attribute handle.
     * @return The HLA attribute handle.
     * @see #setAttributeHandle.
     */
    public int getAttributeHandle() {
        return _attributeHandle;
    }

    /** Return the HLA class handle.
     * @return the HLA class handle.
     * @see #setClassHandle.
     */
    public int getClassHandle() {
        return _classHandle;
    }

    /** Returns the HLA object instance.
     * @return The HLA object instance handle.
     * @see #setObjectInstanceId.
     */
    public int getObjectInstanceId() {
        return _objectInstanceId;
    }

    /** Set the HLA attribute handle.
     * @param attributeHandle The attributeHandle to set.
     * @see #getAttributeHandle.
     */
    public void setAttributeHandle(int attributeHandle) {
        _attributeHandle = attributeHandle;
    }

    /** Set the HLA class handle.
     * @param classHandle The classHandle to set.
     * @see #getClassHandle.
     */
    public void setClassHandle(int classHandle) {
        _classHandle = classHandle;
    }

    /** Set the HLA object instance.
     * @param objectInstanceId The HLA object instance to set.
     * @see #getObjectInstanceHandle.
     */
    public void setObjectInstanceId(int objectInstanceId) {
        _objectInstanceId = objectInstanceId;
    }

    /** Store each updated value of the HLA attribute (mapped to this actor) in
     *  the token queue. Then, program the next firing time of this actor to
     *  send the token at its expected time. This method is called by the
     *  {@link HlaManager} attribute.
     *  @param event The event containing the new value of the HLA
     *  attribute and its time-stamp.
     *  @exception IllegalActionException Not thrown here.
     */
    public void putReflectedHlaAttribute(HlaTimedEvent event)
            throws IllegalActionException {
        // Add the update value to the queue.
        _reflectedAttributeValues.add(event);

        if (_debugging) {
            _debug(this.getFullName()
                    + ": putReflectedHlaAttribute: event timestamp = "
                    + event.timeStamp.toString());
            _debug(this.getFullName()
                    + ": putReflectedHlaAttribute: event value = "
                    + event.contents.toString());
        }

        // Program the next firing time for the received update value.
        _fireAt(event.timeStamp);
    }

    /** Indicate if the HLA subscriber actor uses the CERTI message
     *  buffer API.
     *  @return true if the HLA actor uses the CERTI message and false if
     *  it doesn't.
     */
    public boolean useCertiMessageBuffer() {
        return _useCertiMessageBuffer;
    }

    /** Return the HLA attribute name provided by this HlaAttributeReflector actor.
     * It must match the attribute name defined in the FOM for a class className.
     *  @return the HLA attribute name.
     *  @exception IllegalActionException if a bad token string value is provided.
     */
    public String getHlaAttributeName() throws IllegalActionException {
        String name = "";
        try {
            name = ((StringToken) attributeName.getToken()).stringValue();
        } catch (IllegalActionException illegalActionException) {
            throw new IllegalActionException(this,
                    "Bad attributeName token string value");
        }
        return name;
    }

    /** Return HLA class instance name provided by this HlaAttributeReflector actor.
     * @return The HLA class instance name.
     * @exception IllegalActionException if a bad token string value is provided.
     */
    public String getHlaInstanceName() throws IllegalActionException {
        String name = "";
        try {
            name = ((StringToken) instanceName.getToken()).stringValue();
        } catch (IllegalActionException illegalActionException) {
            throw new IllegalActionException(this,
                    "Bad instanceName token string value");
        }
        return name;
    }

    /** Return the HLA class object name provided by this HlaAttributeReflector actor.
     * It must match the class name defined in the FOM (that has the attribute
     * attributeName of this actor).
     *  @return The HLA class object name.
     *  @exception IllegalActionException if a bad token string value is provided.
     */
    public String getHlaClassName() throws IllegalActionException {
        String name = "";
        try {
            name = ((StringToken) className.getToken()).stringValue();
        } catch (IllegalActionException illegalActionException) {
            throw new IllegalActionException(this,
                    "Bad className token string value");
        }
        return name;
    }

    /** Manage the correct termination of the {@link HlaAttributeReflector}. Reset
     *  HLA handles and object instance ID.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        // Set HLA handles to impossible values
        _attributeHandle = Integer.MIN_VALUE;
        _classHandle = Integer.MIN_VALUE;
        _objectInstanceId = Integer.MIN_VALUE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Build the corresponding typed token from the contents of 
     *  {@link TimedEvent}s stored in the reflectedAttributeValues queue. The
     *  structure of the contents is an array object <i>obj</i> where:
     *  obj[0] is the expected data type and; obj[1] is the object buffer
     *  which contains the typed value.
     * @param obj The array object containing data type indication and buffer.
     * @return value The corresponding typed token.
     * @exception IllegalActionException If the expected data type is not handled
     * Due to previous check this case .
     */
    //FIXMEjc: the last sentence above is incomplete.
    private Token _buildToken(Object[] obj) throws IllegalActionException {
        Token value = null;

        BaseType type = (BaseType) obj[0];
        if (!type.equals(output.getType())) {
            throw new IllegalActionException(this,
                    "The type of the token to build doesn't match the output port type of "
                            + this.getDisplayName());
        }

        if (type.equals(BaseType.BOOLEAN)) {
            value = new BooleanToken((Boolean) obj[1]);
        } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
            Integer valInt = (Integer) obj[1];
            value = new UnsignedByteToken(valInt.byteValue());
        } else if (type.equals(BaseType.DOUBLE)) {
            value = new DoubleToken((Double) obj[1]);
        } else if (type.equals(BaseType.FLOAT)) {
            value = new FloatToken((Float) obj[1]);
        } else if (type.equals(BaseType.INT)) {
            Integer valInt = (Integer) obj[1];
            value = new IntToken(valInt.intValue());
        } else if (type.equals(BaseType.LONG)) {
            value = new LongToken((Long) obj[1]);
        } else if (type.equals(BaseType.SHORT)) {
            value = new ShortToken((Short) obj[1]);
        } else if (type.equals(BaseType.STRING)) {
            value = new StringToken((String) obj[1]);
        } else {
            throw new IllegalActionException(this,
                    "The current type is not supported by this implementation or JCERTI");
            // FIXME: as defined in jcerti.src.hla.rti.jlc.EncodingHelpers.java used in
            // {@link MessageProcessing} ?
            
        }

        return value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of new values for the HLA attribute. */
    private LinkedList<HlaTimedEvent> _reflectedAttributeValues;

    /** A reference to the associated {@link HlaManager}. */
    private HlaManager _hlaManager;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    private boolean _useCertiMessageBuffer;

    /** HLA attribute handle provided by the RTI for the attribute
     *  to subscribe to. */
    private int _attributeHandle;

    /** HLA class handle provided by the RTI for the class object owning
     *  the HLA attribute */
    private int _classHandle;

    /** HLA object instance "ID" provided by the RTI. */
    private int _objectInstanceId;
}
