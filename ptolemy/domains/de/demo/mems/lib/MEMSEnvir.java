/* A MEMS actor that represents the physical environment containing
   a MEMS device.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.domains.de.demo.mems.lib;

import ptolemy.domains.de.demo.mems.gui.*;
import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
/// MEMSDevice
/**
A MEMS actor that represents the physical environment containing
   a MEMS device.  It serves mainly two purposes:  
     1) Defines the geographic location (via three dimensional coordinates)
        and the local enviroment conditions of the associated MEMSDevice.
     2) Acts as a "medium" for transferring MEMSMsg among the MEMSDevices.

   Hence, a typical MEMSEnvir does the following things:
     1) Informs the MEMS device of any changes in the local 
     environmental (eg temperature)
     2) Appends local environment values (eg coordinates) to
     outgoing MEMSMsg.
     3) Filters/processes the MEMSMsg by using the appended 
        information in the MEMSMsg.

   Points 2) and 3) of the above is designed to simulate the environmental
   effects on the communication between any pair of MEMSDevices.  For
   example, when Device B recieves a message from Device A, the MEMSEnvir
   of Device B will compare its own coordinate values with Device A's 
   coordinate values contained in the message header, and decide
   whether Device A is within range.  If not, the MEMSEnvir of 
   Device B will discard the message.  Else, it will "unwrap" the
   environmental information from the message and pass the original
   MEMSMsg sent by Device A to Device B.

@author Allen Miu
@version $Id$
*/

public class MEMSEnvir extends MEMSActor {

    /** Constructs a MEMSEnvir Actor with the specified coordinate and
     *  temperature value.  The MEMSEnvir will also use the handle to
     *  the associated MEMS device to obtain its attributes.
     *
     *  @param container The container actor.
     *  @param name      The name of this actor.
     *  @param mems  The MEMSDevice living in this Enviroment
     *  @param x     The x coordinate value of the associated MEMSDevice
     *  @param y     The y coordinate value of the associated MEMSDevice
     *  @param z     The z coordinate value of the associated MEMSDevice
     *  @param temperature  The temperature of the environment
     *
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MEMSEnvir(TypedCompositeActor container, String name, 
            MEMSDevice mems,
            double x, double y, double z, double temperature,
            MEMSPlot plot)
            throws IllegalActionException, NameDuplicationException  {
        super(container, name+mems.getID());

	// jtc: plot object
	this.plot=plot;
	// draw location of memsenvir
	plot.addPoint(x,y);

	_myID = mems.getID();
	_debugHeader = "MEMSEnvir";
        deviceMsgIO = new TypedIOPort(this, "deviceMsgIO", true, true);
        deviceMsgIO.setDeclaredType(ObjectToken.class);
        carrierMsgIO = new DEIOPort(this, "carrierIO", true, true);
        carrierMsgIO.setDeclaredType(ObjectToken.class);
	//	sysIO = new TypedIOPort(this, "sysIO", true, true);
	this.temperature = temperature;
	coord = new Coord(x,y,z);
	_memsAttrib = mems.getAttrib();

	//	pendingMsgs = new Vector();
	_garbledPendingRecvMsg = false;
	_pendingRecvMessage = null;
	_pendingRecvMsgLength = 0;
	_xferTimeRemaining = 0;
	_recvTimeRemaining = 0;
	Debug.log(0, this, "MEMSEnvir instance created");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Schedules the next enviroment value change event and 
     *  processes pending tokens at the msgIO port
     *
     *  Notes:
     *
     *  All initial environmental data sampling (to mimic sensors) 
     *  event scheduling is accomplished via the 
     *  scheduleEvent() method defined in this class.  The due event
     *  will be launched by the fireDueEvents() method, which will
     *  reschedule the events' next due time.
     *
     *  Messages from the deviceMsgIO are ObjectTokens containing MEMSMsg 
     *  objects.  These messages will be concatenated with local environmental
     *  information and will be broadcasted to other MEMSEnvir Actors.
     *
     *  Messages from the carrierMsgIO are ObjectTokens containing 
     *  MEMSEnvirMsg objects.  These messages will be processed by the 
     *  processCarrierMessage() method defined in this class.
     *
     *  @exception CloneNotSupportedException If there is more than one
     *   destination and the output token cannot be cloned.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (!_isProbeSent) {
            _sendProbe(new thermoProbeMsg(temperature));
            _isProbeSent = true;
        }
        curTime = getCurrentTime();

        /* fire due events */
        fireDueEvents();

        /* check for sensor inputs */
        //      try {
        //	ObjectToken sensorToken = (ObjectToken) sysIO.get(0);
        //	processProbe((Probe) sensorToken.getValue());
        //      } catch (NoTokenException e) {
	/* no state value update, ignore */
        //      }

        /* check for message inputs from other MEMSEnvir Actors */
        try {
            ObjectToken msgToken = (ObjectToken) carrierMsgIO.get(0);
            Debug.log(1, this, "message from other MEMSEnvir received");
            processCarrierMessage((MEMSEnvirMsg) msgToken.getValue());
        } catch (NoTokenException e) {
            // No MEMS message received ... do nothing
        }

        /* check for message inputs from the associated MEMSDevice Actor 
         * and send it out to the carrier (ie a "hub" relation) 
         */
        try {
            ObjectToken msgToken = (ObjectToken) deviceMsgIO.get(0);
            Debug.log(1, this, "message from MEMSDevice" + 
                    getID() + " received");
            _transmitMsgToCarrier((MEMSMsg) msgToken.getValue());
        } catch (NoTokenException e) {
            // No MEMS message received ... do nothing
        }

        // to make sure that the fire method gets invoked in every 
        // clock cycle 
        //
        fireAfterDelay(_memsAttrib.getClockPeriod());
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _isProbeSent = false;
    }      
    
    /** Set the coordinate to the specified value.
     */
    public void setCoordinate(double x, double y, double z) {
        coord = new Coord(x,y,z);
    }


    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** All the changeXXXXXX() handlers should be "registered" in this
     *  method by inserting a statement calling the changeXXXXXX() method.
     *  All of the methods will be invoked and it is up to the individual
     *  handler method to decide whether to perform any action based on
     *  the current time.
     *
     *
     *  Presently, the following handler methods have been registered:
     *
     *  changeTemperature()
     */
    protected void fireDueEvents() throws IllegalActionException {
        /* logic to prevent handlers being called twice at the current time */
        if(prevFireTime < curTime) {
            prevFireTime = curTime;
            _processPendingRecvMsg();
            _processPendingMsgXmit();
            /* register handlers below */
            changeTemperature();
        }
    }

    /** Test the if the incoming message is within range.  If so,
     *  send it to the MEMSDevice.  Otherwise, discard.
     *
     *  @param message - message to be processed
     */
    protected void processCarrierMessage(MEMSEnvirMsg message) throws 
            IllegalActionException {
        if (coord.dist(message.coord) <= _memsAttrib.getRange()) {
            /* FIXME: transmit message only when message.counter == 0 */
            //	_transmitMsgToDevice(message.content);
            _registerCarrierMsg(message);
            Debug.log(1, this,  
                    "passed message id=" + message.content.getID() + 
                    " ASID=" + message.content.ASID +
                    " SID=" + message.content.SID +
                    " DID=" + message.content.DID);
        } else {
            Debug.log(1, this,
                    "discarded message id=" + message.content.getID() + 
                    " ASID=" + message.content.ASID +
                    " SID=" + message.content.SID +
                    " DID=" + message.content.DID);
        }
    }

    /** Registers incoming message from other MEMSDevice.  This method
     *  is used to mimic the data transfer delay (size of data/bandwidth)
     *  and thus allows for two or more incoming messages to overlap 
     *  each other.  For example, if a MEMSDevice is receiving 
     *  Message A that is has a data transfer delay of 5.  Three clock
     *  cycles later, Message B reaches the MEMSDevice.  Hence, the
     *  new Message B overlaps the remaining 2 time lengths of data
     *  of Message A.  
     *
     *  When that happens, the message is garbled.  
     *
     *  The MEMSEnvir Actor will handle messages as follows:
     *
     *  1) MEMSEnvir first receives a message and checks that
     *     it is within the receiving range of its associated MEMSDevice.
     *  2) If so, it calls _registerMsg to save the message in the
     *     MEMSEnvir actor.  (Hence, the message is not immediately sent 
     *     to its associated MEMSDevice.)
     *  3) The MEMSEnvir actor will wait for a number of cycles
     *     equal to message.xferTimeCount (or the data transfer delay
     *     in terms of clock cycles).  If, during this time, no other
     *     message reaches within range of the associated MEMSDevice,
     *     the MEMSEnvir actor will forward the saved (pending) 
     *     message to the associated MEMSDevice.
     *  4) If another message arrives before the first one gets
     *     forwarded to the associated MEMSDevice, the _garbledPendingMsg
     *     flag is asserted.  The MEMSEnvir continues to wait until
     *     ALL messages within range have been fully received.  After
     *     MEMSEnvir finishes waiting, it will create a new instance of
     *     garbledMsg of length equal to the total wait time.
     *
     * @param message - message to be registered
     */
    private void _registerCarrierMsg(MEMSEnvirMsg message)
            throws IllegalActionException {
	

        // FIXME: might want to put the messages into the vector
        //        for supporting a special way of data garbling
        //      pendingMsg.add(message);
        /* Data is garbled if transmit and recv happens at the same time. */
      
        /* FIXME: Remove the (_xferTimeRemaining == 0) clause to 
           allow simultaneous reception and transmission */

        /* _PendingRecvMsgLength == 0 implies _recvTimeRemaining == 0 */
        if ((_pendingRecvMsgLength == 0) && (_xferTimeRemaining == 0)) {
            /* assumes message has at least xferTimeCount > 0 */

            /*jtc draw line here*/
            plot.connect(message.coord, this.coord, 2);
	
            //FIXME: since _pendingRecvMessage is already the message, there
            //       is no need for the other variables...
            _pendingRecvMessage = message;
            _pendingRecvMsgLength = message.xferTimeCount;
            _recvTimeRemaining = message.xferTimeCount;
            Debug.log(0, this, "registering new message, pMsgLength = " +
                    _pendingRecvMsgLength + " recvTR = " +
                    _recvTimeRemaining);
            _sendProbe(new messageProbeMsg(message.xferTimeCount));

        } else {
            /*jtc draw line here*/
            plot.connect(message.coord, coord, 4);
            _garbledPendingRecvMsg = true;
            if (message.xferTimeCount > _recvTimeRemaining) {
                _pendingRecvMsgLength += message.xferTimeCount - _recvTimeRemaining;
                _recvTimeRemaining = message.xferTimeCount;
            } 
            Debug.log(0, this, "pending receive message garbled, pMsgLength = " +
                    _pendingRecvMsgLength + " recvTR = " +
                    _recvTimeRemaining);
	
        }
    }

    private void _processPendingRecvMsg() 
            throws IllegalActionException {
        if (_pendingRecvMessage != null) {
            _recvTimeRemaining--;
            if (_recvTimeRemaining == 0) {
                if (_garbledPendingRecvMsg) {
                    _transmitMsgToDevice(new garbledMsg(_pendingRecvMsgLength));
                } else {
                    /* gui */
                    plot.connect(_pendingRecvMessage.coord, coord, 1);	 	
                    _transmitMsgToDevice(_pendingRecvMessage.content);
                }
                _garbledPendingRecvMsg = false;
                _pendingRecvMessage = null;
                _pendingRecvMsgLength = 0;
            }
        }
    }

    private void _processPendingMsgXmit() {
        if (_xferTimeRemaining != 0) {
            _xferTimeRemaining--;
        }
    }


    /*------------------ Change Event Methods --------------------------*/
    /*--- reminder: For every method shown below, there should be a ---*/
    /*--- corresponding entry entered in the fireDueEvents() method ---*/
    /*-----------------------------------------------------------------*/

    protected void changeTemperature() throws IllegalActionException {
        double newTemp;
        newTemp = getNewTemp();
        if (newTemp != temperature) {
            temperature = newTemp;
            _sendProbe(new thermoProbeMsg(temperature));
        }
    }

    protected double getNewTemp() {
        return temperature;
    }
    
    /*----------------- Helper/Misc Methods --------------------------*/

    /** Wraps the relevant environmental information and broadcasts 
     *  MEMSMsg to other MEMSEnvior Actors using the carrierMsgIO port.
     *
     *  Physical coordinates are concatenated to the MEMSMsg.
     *
     *  @param message - message to be transmitted
     */
    protected void _transmitMsgToCarrier(MEMSMsg message) 
            throws IllegalActionException {

        MEMSEnvirMsg outmsg = new MEMSEnvirMsg(coord,message);

        /* Data is garbled if transmit and recv happens at the same time. */           /* Need this to arbitrate the case where the reception and
           transmission occurs at the same time -- the following 
           gives higher priority to transmission than reception, ie, the
           reception always gets garbled if the execution ever reaches here.
           FIXME: Right now, the transmission will garble any 
           pendingRecvMessage, even when the reception has started first.
                                                                                       */
        if (_pendingRecvMessage != null) {
            _garbledPendingRecvMsg = true;
            Debug.log(0, this, "pending receive message garbled by start of a simultaneous transmission");
        }
      
        if(_xferTimeRemaining == 0) {
            _xferTimeRemaining = outmsg.xferTimeCount;
        } else {
            Debug.log(Debug.ERR,this, "Two different messages being transmitted at the same time");
            System.exit(1);
        }

        carrierMsgIO.broadcast(new ObjectToken(outmsg));
      
        Debug.log(1, this, "message from MEMSDevice" + 
		getID() + " sent to envir hub");
    }

    /** Broadcasts MEMSMsg to the associated MEMSDevice Actor using the 
     *  deviceMsgIO port 
     *
     *  @param message - message to be transmitted
     */
    protected void _transmitMsgToDevice(MEMSMsg message) 
            throws IllegalActionException {
      
        deviceMsgIO.broadcast(new ObjectToken(message));
        Debug.log(1, this,
		"message sent to MEMSDevice" + getID());
    }

    /** Broadcasts an ObjectToken containing a Probe object 
     *  to the associated MEMSDevice Actor using the sysIO port 
     *
     *  @param probe - probe to be transmitted
     */
    protected void _sendProbe(ProbeMsg probe) throws IllegalActionException {
        deviceMsgIO.broadcast(new ObjectToken(probe));
        //      sysIO.broadcast(new ObjectToken(probe));
        Debug.log(1, this,
		"probe sent to MEMSDevice" + getID());
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        public variables                   ////
    public TypedIOPort deviceMsgIO;
    public DEIOPort carrierMsgIO;
    public TypedIOPort sysIO;
    public Coord coord;
    public MEMSPlot plot;  // plot object

    /* ----------------- environmental state variables -------------- */
    protected double temperature;

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////
    protected MEMSDeviceAttrib _memsAttrib;
    protected double curTime;
    //    protected Vector PendingRecvMsgs;

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    private boolean _garbledPendingRecvMsg; 
    private MEMSEnvirMsg _pendingRecvMessage;
    private int _pendingRecvMsgLength;
    private int _xferTimeRemaining;
    private int _recvTimeRemaining;

    private boolean _isProbeSent = false;
}
