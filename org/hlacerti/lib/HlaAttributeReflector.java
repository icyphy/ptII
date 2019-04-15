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
import ptolemy.kernel.util.InternalErrorException;
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
 * {@link HlaManager}, as explained in the documentation for that class.
 * This actor assumes that there is exactly one HlaManager in the model
 * that contains this actor.
 * <p>
 * The attribute that this actor reflects is specified using three parameters:
 * <i>attributeName</i>, <i>className</i>, and <i>instanceName</i>.
 * These three parameters specify the attribute to which this actor listens
 * for updates.  Those updates could be produced, for example, by an
 * {@link HlaPublisher} whose three parameters match those of this actor.
 * The <i>className</i> and <i>attributeName</i> are required to match
 * a class and attribute specified in the FED file
 * that is specified in the HlaManager. The <i>instanceName</i> is an arbitrary
 * name chosen by the designer for the instance of the class. It specifies
 * the instance of the specified class whose attribute this actor reflects.
 * <p>
 * A federate may not need to know the name of the instances, because all
 * instances are treated the same way, or because it is not important to know
 * their names. In these cases, the wildcard  <i>joker_N</i>, N being any integer,
 * may be used in the parameter <i>instanceName</i>. As the order in which the
 * instances are discovered is not known before the run, the wildcard must be
 * used with caution, see the manual and {@link HlaAManager} code.
 * <p>
 * If there is no instance with the specified <i>instanceName</i>, or no
 * updated attribute for the instance binded to a wildcard, then this actor
 * produces no output. If a matching instance is later created, then this
 * actor will begin producing outputs when that instance's attribute is updated.
 * If the specified class does not have an attribute with the specified
 * <i>attributeName</i>, as defined in the FED file, or there is no class
 * matching <i>className</i> in the FED file, then an exception will be thrown.
 * <p>
 * The <i>attributeType</i> parameter specifies the data type of the attribute
 * to which this actor listens. This parameter has two effects. First, it sets
 * the type of the <i>output</i> port. Second, it specifies how to interpret
 * the bytes that are transported via the HLA runtime infrastructure (RTI).
 * Currently, only a small set of primitive data types are supported.
 * <p>
 * The <i>useCertiMessageBuffer</i> parameter works together with the
 * <i>attributeType</i> parameter to interpret the bits that are transported
 * over the RTI. Specifically, an HLA RTI will transport arbitrary byte sequences
 * regardless of what they represent.  CERTI, the particular RTI that Ptolemy II
 * uses, provides a convenience feature that packs and unpacks the message bytes
 * for a small set of data types.  This feature takes into account the annoyance
 * that the byte order can be different on different platforms (big endian or
 * little endian). If the attribute that this actor is listening to is updated
 * by a "foreign" federate (not implemented in Ptolemy II), then this
 * <i>useCertiMessageBuffer</i> parameter should be set to true to ensure that
 * byte order changes are handled. And in this case, only the small set of data
 * types supported by CERTI can be used.  On the other hand, if the attribute
 * is updated by a Ptolemy II model, and that update does not not specify
 * to use the CERTI message buffer, then this parameter should be false.
 *  
 *  @author Gilles Lasnier, Janette Cardoso, Edward A. Lee. Contributors: Patricia Derler, David Come
 *  @version $Id: HlaAttributeReflector.java 214 2018-04-01 13:32:02Z j.cardoso $
 *  @since Ptolemy II 11.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaAttributeReflector extends TypedAtomicActor implements HlaReflectable {

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
        attributeName = new StringParameter(this, "attributeName");

        // HLA object class in FOM.
        className = new StringParameter(this, "className");

        // HLA class instance name.
        instanceName = new StringParameter(this, "instanceName");

        // Basic token types available.
        attributeType = new StringParameter(this, "attributeType");
        attributeType.setExpression("int");
        attributeType.addChoice("int");
        attributeType.addChoice("double");
        attributeType.addChoice("string");
        attributeType.addChoice("boolean");

        // Allow the user to change the output port's type directly.
        // Useful for setting a value to typeSelector after reading the MomL file.
        output.addTypeListener(new TypeListener() {
            @Override
            public void typeChanged(TypeEvent event) {
                attributeType.setExpression(event.getNewType().toString());
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
        _instanceHandle = Integer.MIN_VALUE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the attribute to which this actor listens for updates.
     *  This defaults to an empty string, but it must be non-empty to run the model.
     */
    public StringParameter attributeName;

    /** The type of the attribute that this actor is listening to.
     *  This will be used to set the type of the output port.
     *  This is a string that defaults to "int".
     *  Currently, the only supported types are "int", "double", "string",
     *  and "boolean".
     */
    public StringParameter attributeType;

    /** The name of the class whose attribute this actor listens for updates.
     *  This defaults to an empty string, but it must be non-empty to run the model.
     */
    public StringParameter className;

    /** The name of the instance of the class to whose attribute this actor
     *  listens for updates. If this name does not need to be known, then a
     *  wildcard can be used.
     *  This defaults to an empty string, but it must be non-empty to run the model.
     */
    public StringParameter instanceName;

    /** The output port through which the new value of each
     *  update to the specified attribute of the specified instance
     *  are sent. The type of this port is given by <i>attributeType</a>.
     */
    public TypedIOPort output;

    /** Indicate whether the attribute value is conveyed through
     *  a CERTI message buffer. This is a boolean that defaults to false.
     *  It should be set to true if the attribute to which this actor
     *  listens is updated by a foreign simulator. It can be false
     *  if the attribute is updated by a federate implemented in Ptolemy II,
     *  and if this corresponding parameter in the actor doing the updating
     *  is also false.
     */
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

        if (attribute == useCertiMessageBuffer) {
            _useCertiMessageBuffer = ((BooleanToken) useCertiMessageBuffer
                    .getToken()).booleanValue();
        } else if (attribute == attributeType) {
            String newPotentialTypeName = attributeType.stringValue();
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

        // Set HLA handles to impossible values
        newObject._attributeHandle = Integer.MIN_VALUE;
        newObject._classHandle = Integer.MIN_VALUE;
        newObject._instanceHandle = Integer.MIN_VALUE;

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
    }

    /** Put in the Ptolemy queue the token (with time-stamp t) corresponding to
     *  the RAV callback, with time-stamp t'=ravTimeStamp, related to the HLA
     *  attribute of an instance (mapped to this actor). The value of t depends
     *  on t' and the time management used (NER or TAR, see {@link HlaManager} code).
     *  @exception IllegalActionException Not thrown here.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();

        Iterator<HlaTimedEvent> it = _reflectedAttributeValues.iterator();

        // If there is no event, then there is nothing to do.
        if (!it.hasNext()) {
            return;
        }
        // Get first event on RAV list.
        HlaTimedEvent te = it.next();

        // If the time of the first event matches current time, produce an output.
        if (te.timeStamp.compareTo(currentTime) == 0) {
            // Build token with HLA value.
            Token content = _buildToken((Object[]) te.contents);
            int fromInstanceHandle = te.getHlaInstanceHandle();

            // If the instance matches what we expect, produce an output.
            if (fromInstanceHandle == _instanceHandle) {
                output.send(0, content);

                if (_debugging) {
                    _debug(this.getDisplayName()
                            + " Called fire() - An updated value"
                            + " of the HLA attribute \"" + getHlaAttributeName()
                            + " from " + fromInstanceHandle
                            + "\" has been sent at \"" + te.timeStamp + "\" ("
                            + content.toString() + ")");
                }
            } else {
                throw new InternalErrorException(this, null,
                        "Unexpected attribute reflected. Instance handle "
                        + fromInstanceHandle
                        + " does not match the handle that this actor is reflecting "
                        + _instanceHandle);
            }
            it.remove();
        } else {
            throw new InternalErrorException(this, null,
                    "Unexpected firing. Current time "
                    + currentTime
                    + " does not match HLA event time "
                    + te.timeStamp);
        }

        // Refire if there is another event with the same time stamp.
        if (it.hasNext()) {
            TimedEvent tNext = it.next();
            if (tNext.timeStamp.compareTo(currentTime) == 0) {
                // Refiring the actor to handle the other tokens
                // that are still in channels
                getDirector().fireAtCurrentTime(this);
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

    /** Returns the HLA object instance handle.
     * @return The HLA object instance handle.
     * @see #setInstanceHandle.
     */
    public int getInstanceHandle() {
        return _instanceHandle;
    }

    /** FIXME: This should probably not be here. See HlaManager. */
    public TypedIOPort getOutputPort() {
        return output;
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

    /** Set the HLA object instance handle only when wildcard (joker_) is used.
     * @param _instanceHandle The HLA object instance handle to set.
     * @see #getInstanceHandle.
     */
    public void setInstanceHandle(int instanceHandle) {
        _instanceHandle = instanceHandle;
    }

    /** Store each updated value of the HLA attribute (mapped to this actor) in
     *  the token queue. Then, program the next firing time of this actor to
     *  send the token at its expected time t. This method is called by the
     *  {@link HlaManager} attribute. The timestamp t can be different from 
     *  the ravTimeStamp if TAR time management is used (see {@link HlaManager}
     *  code). 
     *  @param event The event containing the new value of the HLA
     *  attribute and its time-stamp t.
     *  @exception IllegalActionException Not thrown here.
     */
    public void putReflectedHlaAttribute(HlaTimedEvent event)
            throws IllegalActionException {
        // Add the updated value to the queue.
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

    /** Indicate if the HLA attribute reflector actor uses the CERTI message
     *  buffer API.
     *  @return true if the HLA actor uses the CERTI message buffer and false
     *  if it doesn't.
     */
    public boolean useCertiMessageBuffer() {
        return _useCertiMessageBuffer;
    }

    /** Return the value of the <i>attributeName</i> parameter.
     *  @return The value of the <i>attributeName</i> parameter.
     *  @exception IllegalActionException If the class name is empty.
     */
    public String getHlaAttributeName() throws IllegalActionException {
        String name = ((StringToken) attributeName.getToken()).stringValue();
        if (name.trim().equals("")) {
            throw new IllegalActionException(this,
                    "Cannot have an empty attributeName!");
        }
        return name;
    }

    /** Return the value of the <i>className</i> parameter.
     *  @return The value of the <i>className</i> parameter.
     *  @exception IllegalActionException If the class name is empty.
     */
    public String getHlaClassName() throws IllegalActionException {
        String name = ((StringToken) className.getToken()).stringValue();
        if (name.trim().equals("")) {
            throw new IllegalActionException(this,
                    "Cannot have an empty className!");
        }
        return name;
    }

    /** Return the value of the <i>instanceName</i> parameter.
     *  @return The value of the <i>instanceName</i> parameter.
     *  @exception IllegalActionException If the class name is empty.
     */
    public String getHlaInstanceName() throws IllegalActionException {
        String name = ((StringToken) instanceName.getToken()).stringValue();
        if (name.trim().equals("")) {
            throw new IllegalActionException(this,
                    "Cannot have an empty instanceName!");
        }
        return name;
    }

    /** Manage the correct termination of the {@link HlaAttributeReflector}. Reset
     *  HLA attribute handle, class handle, and instance handle.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        // Set HLA handles to impossible values
        _attributeHandle = Integer.MIN_VALUE;
        _classHandle = Integer.MIN_VALUE;
        _instanceHandle = Integer.MIN_VALUE;
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

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    private boolean _useCertiMessageBuffer;

    /** HLA attribute handle provided by the RTI for the attribute
     *  to subscribe to. */
    private int _attributeHandle;

    /** HLA class handle provided by the RTI for the class object owning
     *  the HLA attribute */
    private int _classHandle;

    /** HLA object instance handle provided by the RTI. */
    private int _instanceHandle;
}
