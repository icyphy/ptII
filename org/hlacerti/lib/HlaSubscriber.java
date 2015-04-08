/* This actor implements a subscriber in a HLA/CERTI federation.

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.TupleToken;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.BaseType.IntType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.TupleType;
import ptolemy.data.type.Type;
import ptolemy.domains.ptides.kernel.PtidesPlatform.PtidesNetworkModelTimeType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HlaSubcriber

/**
 * <p>This actor implements a subscriber in a HLA/CERTI federation. This
 * subscriber is associated to one HLA attribute. Reflected values of the HLA
 * attribute are received from the HLA/CERTI Federation by the
 * {@link HlaManager} attribute. The {@link HlaManager} invokes the
 * putReflectedAttribute() to put the received value in the subscriber
 * tokens queue and to program its next firing times, using the _fireAt()
 * method.
 * </p><p>
 * The name of this actor is mapped to the name of the HLA attribute in the
 * federation and need to match the Federate Object Model (FOM) specified for
 * the Federation. The data type of the output port has to be the same type of
 * the HLA attribute. The parameter <i>classObjectHandle</i> needs to match the
 * attribute object class describes in the FOM. The parameter
 * <i>useHlaPtidesEvent</i> indicates if we need to handle PTIDES events as
 * RecordToken for HLA events.
 * </p>
 *
 *  @author Gilles Lasnier, Contributors: Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaSubscriber extends TypedAtomicActor {

    /** Construct a HlaSubscriber actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *  by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *  actor with this name.
     */
    public HlaSubscriber(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // The single output port of the actor.
        output = new TypedIOPort(this, "output", false, true);
        
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
        

                
        opaqueIdendifier = new Parameter(this, "opaqueIdentifier");
        opaqueIdendifier.setDisplayName("Opaque identifier of the federate");
        opaqueIdendifier.setTypeEquals(BaseType.STRING);
        opaqueIdendifier.setExpression("\"opaqueIdentifier\"");
        
        parameterName = new Parameter(this, "parameterName");
        parameterName.setDisplayName("Name of the parameter to receive");
        parameterName.setTypeEquals(BaseType.STRING);
        parameterName.setExpression("\"parameterName\"");
        
        attributeChanged(opaqueIdendifier);
        attributeChanged(parameterName);
        
        _reflectedAttributeValues = new LinkedList<TimedEvent>();
        _useHLAPtidesEvent = false;
        _useCertiMessageBuffer = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The object class of the HLA attribute to subscribe to. */
    public Parameter classObjectHandle;

    /** Indicate if the event is for a PTIDES platform. */
    public Parameter useHLAPtidesEvent;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    public Parameter useCertiMessageBuffer;

    /** The output port. */
    public TypedIOPort output = null;

    /**
    * The HLA Parameter the HLASuscriber is interedted into
    */
    public Parameter parameterName;
    
   /** 
     * Meaningless string used for a 1 <-> 1 mapping with the objectID enabling
     * the HLASuscriber to filter not relevant tokens.
     * Thus all HLASuscribers with the same _opaqueIdendifier 
     * will refere to the same object in the federation
     */ 
    public Parameter opaqueIdendifier;
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the attributeChanged method of the parent. Check if the
     *  user as set the object class of the HLA attribute to subscribe to.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If there is zero or more than one
     *  {@link HlaManager} per Ptolemy model.
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

            // If we receive a HLAPtidesEvent then we have to deal with a
            // RecordType.
            // GL: FIXME: PTIDES: this is to avoid an exception when the type
            // is resolved.
            if (_useHLAPtidesEvent) {
                output.setTypeEquals(new RecordType(new String[] { "microstep",
                        "payload", "sourceTimestamp", "timestamp" },
                        new Type[] { BaseType.INT, BaseType.DOUBLE,
                        BaseType.DOUBLE, BaseType.DOUBLE }));
            }
        }
        else if(attribute == parameterName || attribute == opaqueIdendifier){
            try {
                StringToken opaqueName = (StringToken) opaqueIdendifier.getToken();
                StringToken paramName = (StringToken) parameterName.getToken();
                String param = "";
                String opaque = "";
                if(opaqueName == null) {
                    throw new IllegalActionException(this,
                        "opaqueName Cant be null");
                } else {
                    opaque = opaqueName.stringValue();
                }
                if(paramName == null) {
                    throw new IllegalActionException(this,
                        "paramName Cant be null");
                }else {
                    param = paramName.stringValue();
                }
                
                if(!"opaqueIdentifier".equals(opaque) || !"parameterName".equals(param)){
                    setDisplayName(opaque + " " +param);
                }
                
            } catch (IllegalActionException illegalActionException) {}
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
        System.out.println("CLONNING");
        //take care of all public members ?
        HlaSubscriber newObject = (HlaSubscriber) super.clone(workspace);
        
        //take care of private members
        newObject._reflectedAttributeValues = new LinkedList<TimedEvent>();        
        newObject._useCertiMessageBuffer = _useCertiMessageBuffer;
        newObject._useHLAPtidesEvent = _useHLAPtidesEvent;
        
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

        CompositeActor ca = (CompositeActor) this.getContainer();

        List<HlaManager> hlaManagers = ca.attributeList(HlaManager.class);
        if (hlaManagers.size() > 1) {
            throw new IllegalActionException(this,
                    "Only one HlaManager attribute is allowed per model");
        } else if (hlaManagers.size() < 1) {
            throw new IllegalActionException(this,
                    "A HlaManager attribute is required to use this actor");
        }
    }

    /** Send each update value of the HLA attribute (mapped to this actor) as
     *  token when its time.
     *  @exception IllegalActionException Not thrown here.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();

        Iterator<TimedEvent> it = _reflectedAttributeValues.iterator();
        while (it.hasNext()) {
            TimedEvent te = it.next();
            if (te.timeStamp.compareTo(currentTime) == 0) {
                
                Token content = _buildToken((Object[]) te.contents);
                int origin = -1;
                if(te instanceof OriginatedEvent){
                    OriginatedEvent oe = (OriginatedEvent) te;
                    origin = oe.objectID;
                }
                
                //OriginatedEvent can be null if the HLAsuscriber is not used
                //
                Integer potentialIdentifier = _mapping.get(getOpaqueIdentifier());
                
                //either it is NOT OriginatedEvent and we let it go
                //either it is and it has to match the (potential) origin 
                //and check potentialIdentifier before using it (short circuit evaluation) 
                if(origin == -1 || 
                        potentialIdentifier !=null && potentialIdentifier.equals(origin) == true){
                    this.outputPortList().get(0).send(0, content);
                    
                    if (_debugging) {
                        _debug(this.getDisplayName()
                                + " Called fire() - An updated value"
                                + " of the HLA attribute \"" + this.getName() + " from "
                                + origin  
                                + "\" has been sent at \"" + te.timeStamp + "\"");
                    } //end debug
                } //end test fire
            it.remove();
            } //end current time
        } //end of while
    } //end of fire

    /** Store each updated value of the HLA attribute (mapped to this actor) in
     *  the tokens queue. Then, program the next firing time of this actor to
     *  send the token at its expected time. This method is called by the
     *  {@link HlaManager} attribute.
     *  @param event The event containing the updated value of the HLA attribute
     *  and its time-stamp.
     *  @exception IllegalActionException Not thrown here.
     */
    public void putReflectedHlaAttribute(TimedEvent event)
            throws IllegalActionException {
        // Add the update value to the queue.
        _reflectedAttributeValues.add(event);

        // Program the next firing time for the received update value.
        _fireAt(event.timeStamp);
    }

    /** Indicate if the HLA subscriber actor uses the CERTI message
     *  buffer API.
     */
    public boolean useCertiMessageBuffer() throws IllegalActionException {
        return _useCertiMessageBuffer;
    }

    /** Indicate if the HLA subscriber actor delivers events to a PTIDES
     *  platform.
     */
    public boolean useHLAPtidesEvent() throws IllegalActionException {
        return _useHLAPtidesEvent;
    }
  
    /**
     * Return true if the opaque identifier of the current HLASuscriber is already
     * binded to an object instance.
     */
    public boolean isTaken(){
        String name = null;
        try {
            name= ((StringToken) opaqueIdendifier.getToken()).stringValue();
        } catch (IllegalActionException illegalActionException) {
            illegalActionException.printStackTrace();
        }
        return _mapping.get(name) != null;
    }
    /**
     * Return the opaque identifier of the current HLASuscriber.
    */
    public String getOpaqueIdentifier(){
        try {
            return ((StringToken) opaqueIdendifier.getToken()).stringValue();
        } catch (IllegalActionException illegalActionException) {
        }
        return "";
    }
    
    /**
     * Bind the current HLASuscriber's opaque identifier to object's id theObject.
     */
    public void register(int theObject){

        _mapping.put(getOpaqueIdentifier(),theObject);
    }
    /**
     * Return the kind of HLA Attribute the HLASuscriber is handling.
     */
    public String getParameterName(){
        String parameter ="";
        try {
            parameter = ((StringToken) parameterName.getToken()).stringValue();
        } catch (IllegalActionException illegalActionException) {
        }
        return parameter;
    }
    
    
    @Override 
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        
        //we should do a clear but it is written as OPTIONNAL.
        //safe way to deal with it is to create a whole new object.
        //who said java is RAM consuming ?
        _mapping = new HashMap<String,Integer>();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Build the corresponding typed token from the contents of a
     *  {@link TimedEvent}s stored in the reflectedAttributeValues queue. The
     *  structure of the contents is an array object <i>obj</i> where:
     *  obj[0] is the expected data type and; obj[1] is the object buffer
     *  which contains the typed value.
     * @param obj The array object containing data type indication and buffer.
     * @return value The corresponding typed token.
     * @exception IllegalActionException If the expected data type is not handled
     * Due to previous check this case .
     */
    private Token _buildToken(Object[] obj) throws IllegalActionException {
        Token value = null;

        // GL: FIXME: PTIDES
        if (_useHLAPtidesEvent) {
            Token[] values = new Token[] { new DoubleToken((Double) obj[2]),
                    new IntToken((Integer) obj[3]),
                    new DoubleToken((Double) obj[1]),
                    new DoubleToken((Double) obj[4]) };

            RecordToken record = new RecordToken(
                    PtidesNetworkModelTimeType.LABELS, values);

            return record;
        }

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
                    "The current type is not supported by this implementation");
        }

        return value;
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of updated values for the HLA attribute. */
    private LinkedList<TimedEvent> _reflectedAttributeValues;

    /** Indicate if the event is for a PTIDES platform. */
    private boolean _useHLAPtidesEvent;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    private boolean _useCertiMessageBuffer;
        
    /**
     * used to remeber the mapping _opaqueIdendifier <-> HLA Object id
     * Filled up by register when HLAManager discovers an object
    */
    private static HashMap<String,Integer> _mapping = new HashMap<String,Integer>();
}
