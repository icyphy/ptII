package org.hlacerti.lib;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import certi.rti1516e.impl.CertiLogicalTime1516E;
import certi.rti1516e.impl.CertiLogicalTimeInterval1516E;
import certi.rti1516e.impl.CertiRtiAmbassador;
import hla.rti.ConcurrentAccessAttempted;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.impl.CertiAttributeHandleSet;
import hla.rti1516e.impl.CertiObjectHandle;
import hla.rti1516e.jlc.HLAASCIIstringImpl;
import hla.rti1516e.jlc.NullFederateAmbassador;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class HlaManager1516e implements HlaManagerDelegate {

    private HlaManager hlaManager;


    ///////////////////////////////////////////////////////////////////
    ////                      privated variables                  ////


    /** Federate Ambassador for the Ptolemy Federate. */
    private PtolemyFederateAmbassadorInner federateAmbassador;

    /** RTI Ambassador for the Ptolemy Federate. */
    private CertiRtiAmbassador rtia;

    /** Marker that this federate is the one that created the federation. */
    private boolean isFederationCreator = false;

    /** Indicates if the Ptolemy Federate is the register of the synchronization
     *  point. */
    private boolean isSynchronizationPointRegister;

    /** RTI factory, set up in preinitialize(). */
    private RtiFactory factory;

    /** The simulation stop time. */
    private Time stopTime;

    /** A reference to the enclosing director. */
    private DEDirector director;

    /** The RTIG subprocess. */
    private CertiRtig certiRtig;

    /** Map class instance name and object instance handle. Those information are set
     *  using discoverObjectInstance() callback and used by the RAV service.
     */
    private HashMap<Integer, String> discoverObjectInstanceMap;

    /**
     * Map <Sender actor + HlaUpdatable> and registerObjectInstance (ROI)
     * handle for an object instance. See HlaPublisher and HlaAttributeUpdater.
     *
     * HashMap for HlaPublisher to remember which actor's ID has
     * been registered (as an object instance) to the Federation.
     */
    private HashMap<String, Integer> registerObjectInstanceMap;

    /**
     * The reserved keyword to filter HlaReflectable actors using joker
     * wildcard.
     */
    private static final String jokerFilter = "joker_";

    /** Indicates if the 'joker' filter is used in the classInstanceName
     * parameter of a HlaReflectable actor.
     */
    private boolean usedJoker;

    /** The HLA reporter instance if enabled. */
    private HlaReporter hlaReporter;

    /** Indicator that we are trying to use a preexisting RTI when we expected to launch our own. */
    private boolean preexistingRTI;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // FIXME: A better design for the following would have _hlaAttributesToPublish
    // be a LinkedHashMap<HlaUpdater,HLAAttribute>, where
    // HLAAttribute is an inner class with the three items
    // provided by the RTI.
    /** Table of HLA attributes (and their HLA information) that are published
     *  by the current {@link HlaManager1516e} to the HLA/CERTI Federation. This
     *  table is indexed by the {@link HlaUpdatable} actors present in the model.
     */
    protected HashMap<String, Object[]> hlaAttributesToPublish;

    /** Table of HLA attributes (and their HLA information) that the current
     *  {@link HlaManager1516e is subscribed to. This table is indexed by the
     *  {@link HlaReflectable} actors present in the model.
     */
    protected HashMap<String, Object[]> hlaAttributesToSubscribeTo;

    /** List of events received from the HLA/CERTI Federation and indexed by the
     *  {@link HlaReflectable} actors present in the model.
     */
    protected HashMap<String, LinkedList<TimedEvent>> fromFederationEvents;

    /** Table of object class handles associate to instance handles received by
     *  discoverObjectInstance and reflectAttributesValues services (from
     *  the RTI).
     */
    protected HashMap<Integer, Integer> objectHandleToClassHandle;

    /** Table of used joker (wildcard) filter. */
    protected HashMap<String, Boolean> usedJokerFilterMap;

    ///////////////////////////////////////////////////////////////////
    ////                  public methods                            ///

    public HlaManager1516e(HlaManager hlaManager){
        this.hlaManager = hlaManager;

        registerObjectInstanceMap = new HashMap<String, Integer>();
        discoverObjectInstanceMap = new HashMap<Integer, String>();

        rtia = null;
        certiRtig = null;
        federateAmbassador = null;

        hlaAttributesToPublish = new HashMap<String, Object[]>();
        hlaAttributesToSubscribeTo = new HashMap<String, Object[]>();
        fromFederationEvents = new HashMap<String, LinkedList<TimedEvent>>();
        objectHandleToClassHandle = new HashMap<Integer, Integer>();

        // Joker wildcard support.
        usedJokerFilterMap = new HashMap<String, Boolean>();
    }


    /** Propose a time to advance to. Given a proposed time, this method
     *  consults with the HLA RTI (runtime infrastructure) and blocks
     *  until the RTI grants a time advance to the proposed time
     *  or a lesser time. This method returns the time to which it is safe
     *  to advance, and this time is always less than or equal to the proposed
     *  time.
     *  <p>
     *  This method implements the TimeRegulator
     *  interface by calling the HLA/CERTI Time Management services for
     *  a time advance request. The time advance phase in HLA is a two-step
     *  process: 1) a federate sends a time advance request service and
     *  2) waits for the time to be granted by the timeAdvanceGrant
     *  (TAG) service. Two services, both with  lookahead &gt; 0 are implemented:
     *  - timeAdvanceRequest() (TAR) for implementing time-stepped Federates;
     *  - nextEventRequest() (NER) for implementing event-based Federates.
     *  While this method is blocked waiting for the TAG, some attribute may be
     *  reflected with a time stamp less than the proposed time. In this case,
     *  the time returned by this method depends on whether TAR or NER is being
     *  used. If NER is being used, the time returned by this method will equal
     *  the value of that time stamp. If TAR is being used, the time returned
     *  always equals the proposedTime. This allows the director to properly
     *  handle that reflected attribute value at the time of its time stamp. If
     *  no attribute is reflected while this method is blocked, the time
     *  returned always equals the proposedTime no matter which service is
     *  used, NER or TAR.
     *  <p>
     *  In fact, this method deals with two timelines: the Ptolemy timeline,
     *  where a time value is represented by an instance of the Time class,
     *  and HLA timeline, where time is represented by a double. When this
     *  method is called with some Time value t, this method will convert this
     *  to a double using HLA time units, which may result in some loss of
     *  precision. The RTI will respond by granting an HLA logical time h,
     *  a double, which is less than or equal to the proposed time.
     *  It will be less than if some other event from some other federate has
     *  occurred with a time less than the proposed time (depending on NER, TAR,
     *  and many other factors). If the granted time matches the proposed time
     *  (in HLA's double representation of time), then this method will return
     *  the exact proposedTime Time object, thereby avoiding the quantization
     *  errors of conversion. In other words, if the HLA grants the proposed time,
     *  this method returns the proposedTime with no quantization error.
     *
     *  @param proposedTime The proposed time in Ptolemy time.
     *  @return The proposed time or a smaller time t', in Ptolemy time.
     *  @exception IllegalActionException If an RTI internal error occurs or
     *   a concurrent access occurs while waiting for a response from the RTI.
     */
    @Override
    public Time proposeTime(Time proposedTime) throws IllegalActionException {
        // CERTI offers also the Null Prime Message Protocol that improves
        // the performance of the distributed simulation, see [10].
        // When compiling set to ON the option CERTI_USE_NULL_PRIME_MESSAGE_PROTOCOL.

        Time currentTime = director.getModelTime();

        String strProposedTime = proposedTime.toString();
        if (hlaManager.getDebugging()) {
            if (hlaManager.get_EventBased()) {
                hlaDebug("   start proposeTime(t(lastFoundEvent)="
                        + strProposedTime + ") " + "t_ptII = "
                        + currentTime.toString() + " doubleValue="
                        + currentTime.getDoubleValue() + "; t_hla = "
                        + ((CertiLogicalTime1516E)federateAmbassador.hlaLogicalTime).getTime());
            } else {
                hlaDebug("     starting proposeTime(" + strProposedTime + ") "
                        + "t_ptII = " + currentTime.toString() + " doubleValue="
                        + currentTime.getDoubleValue() + "; t_hla = "
                        + ((CertiLogicalTime1516E)federateAmbassador.hlaLogicalTime).getTime());
            }
        }

        // If the proposedTime exceeds the simulation stop time, the simulation
        // must stop. The federate shall not ask to advance to this
        // proposedTime and must return the _stopTime. Notice that no RAV
        // callback with time stamp bigger than the previous received TAG will
        // be evoked.

        if (proposedTime.compareTo(stopTime) > 0) {
            if (hlaManager.getDebugging()) {
                hlaDebug("   proposeTime(" + strProposedTime + ") "
                        + "  > stopTime(" + stopTime
                        + "): returns proposeTime("+ stopTime+ "), skip RTI.");
            }
            return stopTime;
        }

        // If the proposedTime is equal to current time so it has no need to
        // ask for the HLA service then return the currentTime.

        if (currentTime.equals(proposedTime)) {
            // Even if we avoid the multiple calls of the HLA Time management
            // service for optimization, it could be possible to have events
            // from the Federation in the Federate's priority timestamp queue,
            // so we tick() to get these events (if they exist).
            try {
                rtia.evokeMultipleCallbacks(0, MAX_BLOCKING_TIME);// As fast as HLA 1.3 (NER-NER test): 2481 ms
                //rtia.evokeCallback(MAX_BLOCKING_TIME); Simulation is too slow, ~39743ms instead of 2513ms
                //rtia.evokeMultipleCallbacks(MAX_BLOCKING_TIME, 0); // Too slow also, 39660ms
                if (hlaManager.get_EnableHlaReporter()) {
                    if (hlaReporter.getTimeOfTheLastAdvanceRequest() > 0) {
                        //_hlaReporter._numberOfTicks.set(_hlaReporter._numberOfTAGs, _hlaReporter._numberOfTicks.get(_hlaReporter._numberOfTAGs) + 1);
                    } else {
                        hlaReporter._numberOfOtherTicks++;
                    }
                }
            } catch (RTIinternalError e) {
                throw new IllegalActionException(hlaManager, e,
                        "RTIinternalError: " + e.getMessage());
            } catch (CallNotAllowedFromWithinCallback callNotAllowedFromWithinCallback) {
                callNotAllowedFromWithinCallback.printStackTrace();
            }

            return currentTime;
        }

        // If the HLA Time Management is required, ask to the RTI the
        // authorization to advance its time by invoking TAR or NER service
        // as chosen by the user in the HlaManager interface.
        if (hlaManager.get_IsTimeRegulator() && hlaManager.get_IsTimeConstrained()) {
            synchronized (this) {
                // Call the corresponding HLA Time Management service (NER or TAR).
                try {
                    if (hlaManager.get_EventBased()) {
                        if (hlaManager.getDebugging()) {
                            hlaDebug("    proposeTime(t(lastFoudEvent)=("
                                    + strProposedTime
                                    + ") - calling _eventsBasedTimeAdvance("
                                    + strProposedTime + ")");
                        }
                        return eventsBasedTimeAdvance(proposedTime);
                    } else {
                        if (hlaManager.getDebugging()) {
                            hlaDebug("    proposeTime(" + strProposedTime
                                    + ") - calling _timeSteppedBasedTimeAdvance("
                                    + strProposedTime + ")");
                        }
                        return timeSteppedBasedTimeAdvance(proposedTime);
                    }
                } catch (NoSuchElementException e) {
                    if (hlaManager.getDebugging()) {
                        hlaDebug("    proposeTime(" + strProposedTime + ") -"
                                + " NoSuchElementException " + " for _rtia");
                    }
                    // If some attribute is reflected with a time stamp smaller
                    // than the proposedTime, then this method returns that
                    // smaller time stamp (if NER is used).
                    return proposedTime;
                }
            }
        }
        return null;
    }

    /** Update the HLA attribute specified by the <i>updater</i> argument
     *  to the value given by the <i>in</i> argument. This method is called
     *  by the updater. The updated attribute is sent to the HLA/CERTI
     *  Federation. The time stamp for the update depends on whether
     *  TAR or NER is being used. See the class documentation.
     *  @param updater The HLA updater actor.
     *  @param in The updated value.
     *  @exception IllegalActionException If a CERTI exception is raised.
     */
    public void updateHlaAttribute(HlaUpdatable updater, Token in)
            throws IllegalActionException {

        // Get current model time.
        Time currentTime = director.getModelTime();

        // The following operations build the different arguments required
        // to use the updateAttributeValues() (UAV) service provided by HLA/CERTI.

        // Retrieve information of the HLA attribute to publish.
        Object[] tObj = hlaAttributesToPublish.get(updater.getFullName());

        // Encode the value to be sent to the CERTI.
        byte[] bAttributeValue = MessageProcessing1516e.encodeHlaValue(in);
        if (hlaManager.getDebugging()) {
            hlaDebug("      start updateHlaAttribute() t_ptII = " + currentTime
                    + "; t_hla = " + ((CertiLogicalTime1516E)federateAmbassador.hlaLogicalTime).getTime());
        }
        AttributeHandleValueMap suppAttributes = null;
        try {
            suppAttributes = rtia.getAttributeHandleValueMapFactory().create(1);
        } catch (FederateNotExecutionMember federateNotExecutionMember) {
            federateNotExecutionMember.printStackTrace();
        } catch (NotConnected notConnected) {
            notConnected.printStackTrace();
        }
        suppAttributes.put(getAttributeHandleFromTab(tObj), bAttributeValue);

        // Create a representation of uav-event timestamp for CERTI.
        // UAV timestamp sent by a HlaUpdatable
        CertiLogicalTime1516E uavTimeStamp = null;

        // Convert Ptolemy time t (Time) to HLA time (double),
        // g(t) = _convertToCertiLogicalTime(t), where t=Ptolemy currentTime
        CertiLogicalTime1516E ptIICurrentTime = convertToCertiLogicalTime(
                currentTime);

        if (hlaManager.get_EventBased()) {
            // When the time management NER is used, the time stamp takes always
            // the value ptIICurrentTime + lookahead since HLA says that a
            // federate must promise that no event will be sent before
            // hlaCurrentTime + lookahead. Notice that when using NER,
            // ptIICurrentTime = hlaCurrentTime.
            uavTimeStamp = new CertiLogicalTime1516E(
                    ptIICurrentTime.getTime() + hlaManager.get_HlaLookAHead());
        } else {
            // If the time management is TAR (_timeStepped case) the value of
            // uavTimeStamp depends whether (Ptolemy) currentTime is inside or
            // outside the forbidden region [hlaCurrentTime, hlaCurrentTime +
            // lookahead]. If it is inside, uavTimeStamp takes the value
            // (hlaCurrentTime + lookahead), otherwise uavTimeStamp takes the
            // value ptIICurrentTime. Notice that when using TAR, the values
            // of hlaCurrentTime and ptIICurrentTime can be different.

            // h : HLA current logical time provided by the RTI
            CertiLogicalTime1516E hlaCurrentTime = (CertiLogicalTime1516E) federateAmbassador.hlaLogicalTime;

            // Calculate the end of the forbidden interval (i.e., earliest value
            // of the uavTimeStamp).
            CertiLogicalTime1516E minimalNextUAVTime = new CertiLogicalTime1516E(
                    hlaCurrentTime.getTime() + hlaManager.get_HlaLookAHead());

            // g(t) <  h + lah
            if (minimalNextUAVTime.compareTo(ptIICurrentTime) > 0) {
                // UAV(h + lah)
                uavTimeStamp = minimalNextUAVTime;
            } else {
                // UAV(g(t))
                uavTimeStamp = ptIICurrentTime;
            }
        }

        // HLA Reporter support.
        if (hlaManager.get_EnableHlaReporter()) {
            hlaReporter.updateUAVsInformation(updater, in, getHlaCurrentTime(),
                    currentTime, director.getMicrostep(), uavTimeStamp);
        }

        // XXX: FIXME: check if we may add the object instance id to the HLA updatable and remove this.
        ObjectInstanceHandle instanceHandle = new CertiObjectHandle(registerObjectInstanceMap.get(updater.getHlaInstanceName()));

        try {
            if (hlaManager.getDebugging()) {
                hlaDebug("      * UAV '" + updater.getHlaAttributeName()
                        + "', uavTimeStamp=" + uavTimeStamp.getTime()
                        + ", value=" + in.toString() + ", HlaPub="
                        + updater.getFullName());
            }
            // Name to pass to the RTI Ambassador for logging.
            HLAASCIIstring tag =  new HLAASCIIstringImpl(updater.getFullName());
            byte[] tagByte = new byte[tag.getEncodedLength()];// EncodingHelpers.encodeString(synchronizationPointName);
            ByteWrapper rfspTagWrapper = new ByteWrapper(tagByte);
            tag.encode(rfspTagWrapper);

            if (hlaManager.getDebugging()) {
                hlaDebug(" tag " + tagByte);
            }
            // Call HLA service UAV
            rtia.updateAttributeValues(instanceHandle, suppAttributes, tagByte, uavTimeStamp);

            if (hlaManager.get_EnableHlaReporter()) {
                hlaReporter.incrNumberOfUAVs();
            }

        } catch (AttributeNotDefined e) {
            throw new IllegalActionException(hlaManager, e,
                    "AttributeNotDefined: " + updater.getHlaAttributeName() + ": " + e.getMessage());
        } catch (AttributeNotOwned e) {
            throw new IllegalActionException(hlaManager, e,
                    "AttributeNotOwned: " + updater.getHlaAttributeName() + ": " + e.getMessage());
        } catch (FederateNotExecutionMember e) {
            throw new IllegalActionException(hlaManager, e,
                    "FederateNotExecutionMember: " + e.getMessage());
        } catch (SaveInProgress e) {
            throw new IllegalActionException(hlaManager, e,
                    "SaveInProgress: " + e.getMessage());
        } catch (RestoreInProgress e) {
            throw new IllegalActionException(hlaManager, e,
                    "RestoreInProgress: " + e.getMessage());
        } catch (RTIinternalError e) {
            throw new IllegalActionException(hlaManager, e,
                    "RTIinternalError: " + e.getMessage());
        } catch (ObjectInstanceNotKnown objectInstanceNotKnown) {
            objectInstanceNotKnown.printStackTrace();
        } catch (InvalidLogicalTime invalidLogicalTime) {
            invalidLogicalTime.printStackTrace();
        } catch (NotConnected notConnected) {
            notConnected.printStackTrace();
        }
    }

    /** Manage the correct termination of the {@link HlaManager1_3}. Call the
     *  HLA services to: unsubscribe to HLA attributes, unpublish HLA attributes,
     *  resign a Federation, and destroy a Federation if the current Federate is
     *  the last participant.
     *  @exception IllegalActionException If the parent class throws it
     *  of if a CERTI exception is raised.
     */
    public void wrapup() throws IllegalActionException {
        if (hlaManager.get_EnableHlaReporter()) {
            hlaDebug(hlaReporter.displayAnalysisValues());
            hlaReporter.calculateRuntime();
            hlaReporter.writeNumberOfHLACalls();
            hlaReporter.writeDelays();
            hlaReporter.writeUAVsInformation();
            hlaReporter.writeRAVsInformation();
            hlaReporter.writeTimes();
        }

        try {
            // Unsubscribe to HLA attributes.
            hlaDebug("wrapup() - Unsubscribing to HLA attributes.");
            for (Object[] obj : hlaAttributesToSubscribeTo.values()) {
                try {
                    rtia.unsubscribeObjectClass((getClassHandleFromTab(obj)));
                } catch (RTIexception e) {
                    throw new IllegalActionException(hlaManager, e,
                            "RTIexception: " + e.getMessage());
                }
                if (hlaManager.getDebugging()) {
                    hlaDebug("wrapup() - unsubscribe "
                            + getPortFromTab(obj).getContainer().getFullName()
                            + "(classHandle = " + getClassHandleFromTab(obj).hashCode()
                            + ")");
                }
            }

            // Unpublish HLA attributes.
            hlaDebug("wrapup() - Unpublishing HLA attributes.");
            for (Object[] obj : hlaAttributesToPublish.values()) {
                try {
                    rtia.unpublishObjectClass((getClassHandleFromTab(obj)));
                } catch (RTIexception e) {
                    throw new IllegalActionException(hlaManager, e,
                            "RTIexception: " + e.getMessage());
                }
                if (hlaManager.getDebugging()) {
                    hlaDebug("wrapup() - unpublish "
                            + getPortFromTab(obj).getContainer().getFullName()
                            + "(classHandle = " + getClassHandleFromTab(obj).hashCode()
                            + ")");
                }
            }
        } finally {
            // Resign HLA/CERTI Federation execution.
            try {
                // _rtia can be null if we are exporting to JNLP.
                if (rtia != null) {
                    hlaDebug("wrapup() - Resigning Federation execution");
                    rtia.resignFederationExecution(
                        ResignAction.DELETE_OBJECTS); // DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES);
                    hlaDebug("wrapup() - Resigned Federation execution");
                }
            } catch (RTIexception e) {
                hlaDebug("wrapup() - RTIexception.");
                throw new IllegalActionException(hlaManager, e,
                        "RTIexception: " + e.getMessage());
            } finally {
                // Destroy the federation execution if this was the federate
                // that created it. This will wait
                // until all federates have resigned the federation
                // or until the model execution is stopped.
                boolean federationIsActive = true;
                try {
                    while (federationIsActive && isFederationCreator) {

                        // Destroy federation execution.
                        try {
                            hlaDebug("wrapup() - Destroying the federation.");
                            rtia.destroyFederationExecution(hlaManager.get_FederationName());
                            federationIsActive = false;
                            hlaDebug("wrapup() - Federation destroyed by this federate.");

                        } catch (FederatesCurrentlyJoined e) {
                            hlaDebug("wrapup() - Federates are still joined to the federation."
                                    + " Wait some time and try again to destroy the federation.");

                            if (director.isStopRequested()) {
                                hlaDebug("wrapup() - Federate was stopped by the user.");
                                break;
                            }
                            // Give the other federates a chance to finish.
                            Thread.yield();
                        } catch (FederationExecutionDoesNotExist e) {
                            // No more federation. Some other federate must have
                            // succeeded in destroying it.
                            hlaDebug("wrapup() - Federation was destroyed by some other federate.");
                            federationIsActive = false;
                        } catch (RTIinternalError e) {
                            throw new IllegalActionException(hlaManager, e,
                                    "RTIinternalError: " + e.getMessage());
                        } catch (NotConnected e) {
                            throw new IllegalActionException(hlaManager, e,
                                    "NotConnected : " + e.getMessage());
                        }
                    }
                } finally {
                    try {
                        // Clean HLA attribute tables.
                        hlaAttributesToPublish.clear();
                        hlaAttributesToSubscribeTo.clear();
                        fromFederationEvents.clear();
                        objectHandleToClassHandle.clear();

                        // Clean HLA object instance id maps.
                        registerObjectInstanceMap.clear();
                        discoverObjectInstanceMap.clear();

                        // Joker wildcard support.
                        usedJokerFilterMap.clear();

                        // HLA Reporter support.
                        hlaReporter = null;

                        // Close the connection socket connection between jcerti (the Java
                        // proxy for the ambassador) and certi.
                        // Sadly, this nondeterministically triggers an IOException:
                        // rtia can be null if we are exporting to JNLP.
                        if (rtia != null) {                        
                            rtia.disconnect();
                        }
                    } catch (CallNotAllowedFromWithinCallback callNotAllowedFromWithinCallback) {
                        callNotAllowedFromWithinCallback.printStackTrace();
                    } catch (FederateIsExecutionMember federateIsExecutionMember) {
                        federateIsExecutionMember.printStackTrace();
                    } catch (RTIinternalError rtIinternalError) {
                        rtIinternalError.printStackTrace();
                    } finally {
                        // Terminate RTIG subprocess.
                        if (certiRtig != null && ((BooleanToken) hlaManager.killRTIG.getToken()).booleanValue()) {
                            // CERTI seems to require some time for destroying the
                            // federation, done above, to settle.
                            try {
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {
                                // Continue to the kill.
                            }
                            hlaDebug("**** Killing the RTIG process (if authorized by the system)");
                            certiRtig.terminateProcess();
                        }
                        hlaDebug("----------------------- End execution.");
                    }
                }
            }
        }
    }
    @Override
    public boolean noNewActors() {
        return false;
    }

    public Object clone(HlaManager newObject) {
        return null;
    }

    /** Initializes the {@link HlaManager1_3} attribute. This method: calls the
     *  _populateHlaAttributeTables() to initialize HLA attributes to publish
     *  or subscribe to; instantiates and initializes the {@link RTIambassador}
     *  and {@link hla.rti.FederateAmbassador} which handle the communication
     *  Federate &lt;-&gt; RTIA &lt;-&gt; RTIG. RTIA and RTIG are both external communicant
     *  processes (see JCERTI); create the HLA/CERTI Federation (if not exists);
     *  allows the Federate to join the Federation; set the Federate time
     *  management policies (regulator and/or contrained); register a
     *  synchronization point (if required); and synchronizes the Federate with
     *  a synchronization point (if declared).
     *  @exception IllegalActionException If the container of the class is not
     *  an Actor or If a CERTI exception is raised and has to be displayed to
     *  the user.
     */
    public void initialize() throws IllegalActionException {
        NamedObj container = hlaManager.getContainer();
        if (!(container instanceof Actor)) {
            throw new IllegalActionException(hlaManager,
                    "HlaManager has to be contained by an Actor");
        }

        // Get the corresponding director associated to the HlaManager attribute.
        director = (DEDirector) ((CompositeActor) hlaManager.getContainer())
                .getDirector();

        // Initialize HLA attribute tables for publication/subscription.
        populateHlaAttributeTables();

        // Check whether this federate registers a synchronization point
        // to synchronize with the start of other federates.
        isSynchronizationPointRegister = false;
        boolean needsToSynchronize = true;
        String synchronizationPointName = hlaManager.synchronizeStartTo.stringValue().trim();
        if (synchronizationPointName.equals(hlaManager.get_FederateName())) {
            isSynchronizationPointRegister = true;
        } else if (synchronizationPointName.equals("")) {
            needsToSynchronize = false;
        }

        // HLA Reporter support.
        if (hlaManager.get_EnableHlaReporter()) {
            // Get model filename.
            String modelName = hlaManager.getContainer().getFullName();
            try {
                // Directory to store reports is created at the root folder of the user.
                hlaReporter = new HlaReporter(hlaManager.hlaReportPath.getValueAsString(),
                        hlaManager.get_FederateName(), hlaManager.get_FederationName(), modelName);
            } catch (IOException e) {
                throw new IllegalActionException(hlaManager, e,
                        "HLA reporter: Failed to create folder or files: "
                                + e.getMessage());
            }

            hlaReporter.initializeReportVariables(hlaManager.get_HlaLookAHead(), hlaManager.get_HlaTimeStep(),
                    hlaManager.get_HlaTimeUnitValue(), 0.0, director.getModelStopTime(),
                    hlaManager.get_FederateName(), hlaManager.fedFile.asFile().getPath(), isSynchronizationPointRegister,
                    hlaManager.get_TimeStepped(), hlaManager.get_EventBased());

            hlaReporter.initializeAttributesToPublishVariables(
                    hlaAttributesToPublish);
        }

//        //Get a link to the RTI (Connect)
//        federateAmbassador = new PtolemyFederateAmbassadorInner();
//        try {
//            rtia.connect(federateAmbassador, CallbackModel.HLA_IMMEDIATE);
//
//
//        } catch (ConnectionFailed connectionFailed) {
//            connectionFailed.printStackTrace();
//        } catch (InvalidLocalSettingsDesignator invalidLocalSettingsDesignator) {
//            invalidLocalSettingsDesignator.printStackTrace();
//        } catch (UnsupportedCallbackModel unsupportedCallbackModel) {
//            unsupportedCallbackModel.printStackTrace();
//        } catch (AlreadyConnected alreadyConnected) {
//            alreadyConnected.printStackTrace();
//        } catch (CallNotAllowedFromWithinCallback callNotAllowedFromWithinCallback) {
//            callNotAllowedFromWithinCallback.printStackTrace();
//        } catch (RTIinternalError rtIinternalError) {
//            rtIinternalError.printStackTrace();
//        }

        // Create the Federation if one does not already exist.
        isFederationCreator = false;
        try {
            hlaDebug("Creating Federation execution.");
            //File fom = new File(System.getenv("CERTI_FOM_PATH")  + File.separator + hlaManager.fedFileOnRTIG.stringValue());
            //URL fomUrl = findFomFile();
            //System.out.println("Fom url : " + fomUrl);
//          File fom = new File(hlaManager.fedFileOnRTIG.stringValue());
//          rtia.createFederationExecution(hlaManager.get_FederationName(), fomUrl);
            rtia.createFederationExecution(hlaManager.get_FederationName(), hlaManager.fedFileOnRTIG.stringValue());

            isFederationCreator = true;
            hlaDebug("createFederationExecution: FED file on RTIG machine = "
                    + hlaManager.fedFileOnRTIG.stringValue());
        } catch (FederationExecutionAlreadyExists ex) {
            hlaDebug("initialize() - Federation execution already exists. No need to create one.");
            isFederationCreator = false;
        } catch (RTIinternalError e) {
            hlaDebug("RTI internal error.");
            throw new IllegalActionException(hlaManager, e,
                    "RTI internal error.");
        } catch (NotConnected e) {
            hlaDebug("Not Connected error.");
            throw new IllegalActionException(hlaManager, e,
                    "Not Connected error.");
//        } catch (MalformedURLException e) {
//            hlaDebug("Malformed URL error.");
//            throw new IllegalActionException(hlaManager, e,
//                    "Malformed URL error.");
//        } catch (InconsistentFDD e) {
//            hlaDebug("Inconsistent FDD error.");
//            throw new IllegalActionException(hlaManager, e,
//                    "Inconsistent FDD error");
        } catch (CouldNotOpenFDD e) {
            hlaDebug("createFederationExecution: RTIG failed to open FED file: "
                    + hlaManager.fedFileOnRTIG.stringValue());
            String more = "";
            if (preexistingRTI) {
                more = "\nThis federate expected to launch its own RTIG (launchRTIG is true)\n"
                        + "but a pre-existing RTIG was found and we are trying to use that one.\n"
                        + "That pre-existing RTIG cannot find hlaManager FED file.\n"
                        + "Try killing the pre-existing RTIG so that a new one can start.\n"
                        + "Alternatively, adjust the CERTI_FOM_PATH used by the RTIG.";
            }
            throw new IllegalActionException(hlaManager, e,
                    "RTIG could not find FED file: " + hlaManager.fedFileOnRTIG.stringValue() + more);
        } catch (ErrorReadingFDD e) {
            hlaDebug("createFederationExecution: RTIG failed to open FED file: "
                    + hlaManager.fedFileOnRTIG.stringValue());
            throw new IllegalActionException(hlaManager, e,
                    "Error reading FED file.");
        }

        //Join the federation
        try {
            hlaDebug("Joining the federation.");
            rtia.joinFederationExecution(hlaManager.get_FederateName(), "federateType", hlaManager.get_FederationName(), (URL[]) null);
            hlaDebug("initialize() - federation joined");
        } catch(FederateAlreadyExecutionMember e){
            throw new IllegalActionException(hlaManager, e, "FederateAlreadyExecutionMember" + e.getMessage());
        } catch (RTIexception e) {
            throw new IllegalActionException(hlaManager, e, "RTIexception: " + e.getMessage());
        }

        // Initialize the Federate Ambassador
        try {
            hlaDebug("Initializing the RTI Ambassador");
            federateAmbassador.initialize(rtia);
            hlaDebug("RTI Ambassador initialized.");
        } catch (RTIexception e) {
            throw new IllegalActionException(hlaManager, e,
                    "RTIexception: " + e.getMessage());
        } catch (ConcurrentAccessAttempted e) {
            throw new IllegalActionException(hlaManager, e,
                    "ConcurrentAccessAttempted: " + e.getMessage());
        }

        // Initialize HLA time management aspects for a Federate
        // (constrained by and/or participate to the time management).
        initializeTimeAspects();
        hlaDebug("Time Aspects initialized.");
        // Set initial synchronization point.
        if (needsToSynchronize) {
            doInitialSynchronization();
            hlaDebug("Synchronization point initialized.");
        } else {
            hlaDebug("No needs of synchronization point initialization.");
        }
    }

    /** The method populatedHlaValueTables populates the tables
     *  containing information required to publish and to
     *  subscribe to attributes of a class in a HLA Federation.
     *  @exception IllegalActionException If a HLA attribute is declared twice.
     */
    private void populateHlaAttributeTables() throws IllegalActionException {
        CompositeEntity ce = (CompositeEntity) hlaManager.getContainer();

        // HlaUpdatables.
        hlaAttributesToPublish.clear();
        List<HlaUpdatable> _hlaUpdatables = ce.entityList(HlaUpdatable.class);
        for (HlaUpdatable updater : _hlaUpdatables) {
            // FIXME: This is a terrible way to check for name collisions.
            // If there are N updaters, it makes N^2 checks.
            // _hlaUpdatables should be a LinkedHashSet, and before adding
            // any updater to it, check to see whether there is one already contained
            // with the same name. If there is, throw an exception.
            for (HlaUpdatable updaterIndex : _hlaUpdatables) {
                if ((!updater.getFullName().equals(updaterIndex.getFullName())
                        && (updater.getHlaAttributeName()
                        .compareTo(updaterIndex.getHlaAttributeName()) == 0)
                        && (updater.getHlaClassName()
                        .compareTo(updaterIndex.getHlaClassName()) == 0)
                        && (updater.getHlaInstanceName().compareTo(
                        updaterIndex.getHlaInstanceName()) == 0))
                        || (!updater.getFullName().equals(updaterIndex.getFullName())
                        && (!updater.getHlaClassName()
                        .equals(updaterIndex.getHlaClassName()))
                        && (updater.getHlaInstanceName().compareTo(updaterIndex
                        .getHlaInstanceName()) == 0))) {

                    throw new IllegalActionException(updater, "A HlaUpdatable '"
                            + updaterIndex.getFullName()
                            + "' with the same HLA information specified by the "
                            + "HlaUpdatable '" + updater.getFullName()
                            + "' \nis already registered for publication.");
                }
            }

            // Only one input port is allowed per HlaUpdatable actor.
            TypedIOPort tIOPort = updater.getInputPort();

            hlaAttributesToPublish.put(updater.getFullName(),

                    // XXX: FIXME: simply replace Object[] by a HlaUpdatable instance ?

                    // tObj[] object as the following structure:

                    // tObj[0] => input port which receives the token to transform
                    //            as an updated value of a HLA attribute,
                    // tObj[1] => type of the port; it must be equal to the data type of the attribute,
                    // tObj[2] => object class name of the attribute,
                    // tObj[3] => instance class name

                    // tObj[4] => ID of the object class to handle,
                    // tObj[5] => ID of the attribute to handle

                    // tObj[0 .. 3] are extracted from the Ptolemy model.
                    // tObj[3 .. 5] are provided by the RTI (CERTI).
                    new Object[] { tIOPort, tIOPort.getType(),
                            updater.getHlaClassName(),
                            updater.getHlaInstanceName() });
        }

        // HlaReflectables.
        hlaAttributesToSubscribeTo.clear();

        List<HlaReflectable> _hlaReflectables = getHlaReflectables(ce);

        for (HlaReflectable hs : _hlaReflectables) {
            // Note: The HLA attribute name is no more associated to the
            // HlaReflectable actor name. As Ptolemy do not accept two actors
            // of the same name at a same model level the following test is no
            // more required.
            //if (_hlaAttributesToSubscribeTo.get(hs.getFullName()) != null) {
            //    throw new IllegalActionException(this,
            //            "A HLA attribute with the same name is already "
            //                    + "registered for subscription.");
            //}

            // Note: asked by JC on 20171128, the current implementation is not
            // optimized and may slow the model initialization step if there is
            // a lot of actors.
            // The HLA attribute is no more associated to the HlaReflectable
            // actor name but instead to the attribute name parameter. Checks
            // and throws an exception if two actors specify the same HLA
            // attribute from a same HLA object class and a same HLA instance
            // class name.
            for (HlaReflectable hsIndex : _hlaReflectables) {
                if ((!hs.getFullName().equals(hsIndex.getFullName())
                        && (hs.getHlaAttributeName()
                        .compareTo(hsIndex.getHlaAttributeName()) == 0)
                        && (hs.getHlaClassName()
                        .compareTo(hsIndex.getHlaClassName()) == 0)
                        && (hs.getHlaInstanceName().compareTo(
                        hsIndex.getHlaInstanceName()) == 0))
                        || (!hs.getFullName().equals(hsIndex.getFullName())
                        && (!hs.getHlaClassName()
                        .equals(hsIndex.getHlaClassName()))
                        && (hs.getHlaInstanceName().compareTo(hsIndex
                        .getHlaInstanceName()) == 0))) {

                    // FIXME: XXX: Highlight the faulty HlaReflectable actor here, see UCB for API.

                    throw new IllegalActionException(hlaManager, "A HlaReflectable '"
                            + hsIndex.getFullName()
                            + "' with the same HLA information specified by the "
                            + "HlaReflectable '" + hs.getFullName()
                            + "' \nis already registered for subscription.");
                }
            }

            // Only one output port is allowed per HlaReflectable actor.
            TypedIOPort tiop = hs.getOutputPort();

            hlaAttributesToSubscribeTo.put(hs.getFullName(),

                    // XXX: FIXME: simply replace object[] by a HlaReflectable instance ?

                    // tObj[] object as the following structure:

                    // tObj[0] => input port which receives the token to transform
                    //            as an updated value of a HLA attribute,
                    // tObj[1] => type of the port (e.g. of the attribute),
                    // tObj[2] => object class name of the attribute,
                    // tObj[3] => instance class name

                    // tObj[4] => ID of the object class to handle,
                    // tObj[5] => ID of the attribute to handle

                    // tObj[0 .. 3] are extracted from the Ptolemy model.
                    // tObj[3 .. 5] are provided by the RTI (CERTI).
                    new Object[] { tiop, tiop.getType(),
                            hs.getHlaClassName(),
                            hs.getHlaInstanceName() });

            // The events list to store updated values of HLA attribute,
            // (received by callbacks) from the RTI, is indexed by the
            // HlaReflectable actors present in the model.
            fromFederationEvents.put(hs.getFullName(),
                    new LinkedList<TimedEvent>());

            // Joker wildcard support.
            usedJoker = false;

            String instanceNameOrJokerName = hs.getHlaInstanceName();

            if (instanceNameOrJokerName.contains(jokerFilter)) {
                usedJoker = true;
                if (hlaManager.getDebugging()) {
                    hlaDebug("HLA actor " + hs.getFullName()
                            + " uses joker wildcard = " + jokerFilter);
                }
            }

            if (usedJoker) {
                if (!instanceNameOrJokerName.contains(jokerFilter)) {
                    throw new IllegalActionException(hlaManager,
                            "Cannot mix class instance name and joker filter in HlaReflectable "
                                    + "please check: " + hs.getFullName());
                } else {
                    // Add a new discovered joker to the joker table.
                    usedJokerFilterMap.put(instanceNameOrJokerName, false);
                }
            }
        }
    }

    public void preinitialize() throws IllegalActionException {
        if (System.getenv("CERTI_FOM_PATH") != null) {
            hlaDebug("preinitialize() - "
                    + "CERTI_FOM_PATH = " + System.getenv("CERTI_FOM_PATH"));
        }

        // First, check whether there is already an RTI running.
        factory = null;
        certiRtig = null;
        rtia = null;
        preexistingRTI = false;
        try {
            factory = RtiFactoryFactory.getRtiFactory();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(hlaManager, e, "Getting RTI factory failed.");
        }
        System.out.println("Value of launch rtig : " + ((BooleanToken) hlaManager.launchRTIG.getToken()).booleanValue());
        try {
            hlaDebug("Creating RTI Ambassador");
            rtia = (CertiRtiAmbassador) factory.getRtiAmbassador();
            federateAmbassador = new PtolemyFederateAmbassadorInner();
            rtia.connect(federateAmbassador, CallbackModel.HLA_IMMEDIATE);
            if (((BooleanToken) hlaManager.launchRTIG.getToken()).booleanValue()) {
                // The model is expecting to launch its own RTIG, but there is
                // already one running. That one is unlikely to know about the
                // FED files used by this federation, so we abort here.
                hlaDebug("****** WARNING: Expected to launch a new RTIG, "
                        + "but one is running already. Will try using that one.");
                preexistingRTI = true;
            }
            hlaDebug("RTI Ambassador created.");
        } catch (Exception e) {
            // If this fails, there is likely no RTI running.
            hlaDebug("preinitialize() - **** No RTI running.");
            // If set to create one, the create one.
            if (((BooleanToken) hlaManager.launchRTIG.getToken()).booleanValue()) {

                // Request to launch a local RTI.
                // Check for a compatible CERTI_HOST environment variable.
                String certiHost = System.getenv("CERTI_HOST");
                if (certiHost != null
                        && !certiHost.equals("localhost")
                        && !certiHost.equals("127.0.0.1")) {
                    throw new IllegalActionException(hlaManager,
                            "The environment variable CERTI_HOST, which has value: "
                                    + certiHost
                                    + ", is neither localhost nor 127.0.0.1."
                                    + " We cannot launch an RTI at that address. "
                                    + "You may want to set launchRTIG to false.");
                }
                if (certiHost == null) {
                    certiHost = "localhost";
                }
                hlaDebug("preinitialize() - Launching CERTI RTI "
                        + " in directory "
                        + System.getProperty("user.dir"));
                // Try to launch the HLA/CERTI RTIG subprocess.
                certiRtig = new CertiRtig(hlaManager, hlaManager.getDebugging());
                certiRtig.initialize(hlaManager.fedFile.asFile().getAbsolutePath());
                certiRtig.exec();
                hlaDebug("RTI launched.");
                // Give the RTIG some time start up.
                // FIXME: Do we need this after JCERTI fixed bug #53878?
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException ex) {
                    // Ignore
                }
                // The environment variable $CERTI_HOME must be set for creating
                // the RTI Ambassador. Quoting [7], "Within libRTI, the class
                // RTIambassador bundles the services provided by the RTI. All
                // requests made by a federate on the RTI take the form of an
                // RTIambassador method call.
                try {
                    hlaDebug("Creating RTI Ambassador");
                    rtia = (CertiRtiAmbassador) factory.getRtiAmbassador();
                    federateAmbassador = new PtolemyFederateAmbassadorInner();
                    rtia.connect(federateAmbassador, CallbackModel.HLA_IMMEDIATE);
                    hlaDebug("RTI Ambassador created.");
                } catch (Exception ex) {
                    throw new IllegalActionException(hlaManager, ex,
                            "Failed to create RTI Ambassador.");
                }
            } else {
                throw new IllegalActionException(hlaManager, "Could not connect to an RTI. "
                        + "Consider setting launchRTIG to true.\n"
                        + "Note that the RTIG relies on a number of environment "
                        + "variables defined in $CERTI_HOME/share/scripts/myCERTI_env.sh.");
            }
        }
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private methods                       ////


    /** The method getHlaReflectables() get all HLA reflectables
     *  actors across the model.
     *  @param ce the composite entity which may contain HlaReflectables
     *  @return the list of HlaReflectables
     */
    private List<HlaReflectable> getHlaReflectables(CompositeEntity ce) {
        // The list of HLA reflectables to return.
        LinkedList<HlaReflectable> hlaReflectables = new LinkedList<HlaReflectable>();

        // List all classes from top level model.
        List<CompositeEntity> entities = ce.entityList();
        for (ComponentEntity classElt : entities) {
            if (classElt instanceof HlaReflectable) {
                hlaReflectables.add((HlaReflectable) classElt);
            } else if (classElt instanceof ptolemy.actor.TypedCompositeActor) {
                hlaReflectables
                        .addAll(getHlaReflectables((CompositeEntity) classElt));
            }
        }

        return hlaReflectables;
    }

    /** Customized debug message for hlaManager
     *  This will send the message to debug listeners if there are any.
     *  Otherwise, it sends the message to standard out.
     *  @param reason The reason to print
     */
    private void hlaDebug(String reason) {
        String dbgHeader = "Federate: " + hlaManager.get_FederateName() + " - Federation: " + hlaManager.get_FederationName() + " - ";
        if (hlaManager.getDebugging()) {
            hlaManager.setDebug(dbgHeader + reason);
        } else {
            System.out.println(dbgHeader + reason);
        }
    }

    private Time timeSteppedBasedTimeAdvance(Time proposedTime)
            throws IllegalActionException {

        // HLA Reporter support.
        if (hlaManager.get_EnableHlaReporter()) {
            hlaReporter.storeTimes("TAR()", proposedTime,
                    director.getModelTime());
        }

        // Header for debug purpose and listener.
        String headMsg = "timeSteppedBasedTimeAdvance("
                + proposedTime.toString() + "): ";

        if (hlaManager.getDebugging()) {
            hlaDebug("\n" + "start " + headMsg + " print proposedTime.toString="
                    + proposedTime.toString());
        }

        // Convert proposedTime to double so it can be compared with HLA time
        CertiLogicalTime1516E certiProposedTime = convertToCertiLogicalTime(
                proposedTime);

        // Read current HLA time (provided by the RTI)
        CertiLogicalTime1516E hlaLogicaltime = (CertiLogicalTime1516E) federateAmbassador.hlaLogicalTime;

        // Set the value of next HLA point in time (when using TAR)
        CertiLogicalTime1516E nextPointInTime = new CertiLogicalTime1516E(
                hlaLogicaltime.getTime() + hlaManager.get_HlaTimeStep());

        // NOTE: Microstep reset problem
        //  To retrieve the old behavior with the microstep reset problem, you may change the line below:
        //  reset    => while (certiProposedTime.isGreaterThan(nextPointInTime)) {
        //  no reset => while (certiProposedTime.isGreaterThanOrEqualTo(nextPointInTime)) {

        if (hlaManager.getDebugging()) {
            hlaDebug("Before While g(t') >= h+TS; g(t')= "
                    + certiProposedTime.getTime() + "; h+TS= "
                    + nextPointInTime.getTime() + " @ " + headMsg);
        }
        // Call as many TAR as needed for allowing Ptolemy time to advance to
        // proposedTime
        while (certiProposedTime.compareTo(nextPointInTime) >= 0) {
            // Wait the time grant from the HLA/CERTI Federation (from the RTI).
            federateAmbassador.timeAdvanceGrant = false;

            try {
                if (hlaManager.get_EnableHlaReporter()) {
                    // Set time of last time advance request.
                    hlaReporter
                            .setTimeOfTheLastAdvanceRequest(System.nanoTime());
                }

                // Call CERTI TAR HLA service.
                rtia.timeAdvanceRequest(nextPointInTime);

                if (hlaManager.get_EnableHlaReporter()) {
                    // Increment counter of TAR calls.
                    hlaReporter.incrNumberOfTARs();
                }

                if (hlaManager.getDebugging()) {
                    hlaDebug("  TAR(" + nextPointInTime.getTime() + ") in "
                            + headMsg);
                }
            } catch (FederateNotExecutionMember | RequestForTimeConstrainedPending
                    | LogicalTimeAlreadyPassed | RequestForTimeRegulationPending
                    | InvalidLogicalTime | InTimeAdvancingState | NotConnected
                    | SaveInProgress | RestoreInProgress | RTIinternalError e) {
                throw new IllegalActionException(hlaManager, e, e.getMessage());
            }

            while (!(federateAmbassador.timeAdvanceGrant)) {
                if (director.isStopRequested()) {
                    // Stop is requested by the user.
                    // Not sure what to do here, but we can't just keep waiting.
                    throw new IllegalActionException(hlaManager,
                            "Stop requested while waiting for a time advance grant from the RTIG.");
                }
                if (hlaManager.getDebugging()) {
                    hlaDebug("      waiting for callbacks in " + headMsg);
                }
                try {
                    // Wait for a callback. This will return immediately if there
                    // is already a callback pending (and before returning, it will
                    // call that callback). If there is no callback pending, it will
                    // wait up to MAX_BLOCKING_TIME and then return even if there is
                    // no callback.
                    // Do not use tick2() here because it can block the director
                    // if no callback is received. Also, do not use tick() because it
                    // results in a busy wait.
                    // NOTE: The second argument, which the API confusingly calls "max"
                    // but is usually less than "min", appears to not be used.
                   rtia.evokeCallback(MAX_BLOCKING_TIME); //rtia.evokeMultipleCallbacks(MAX_BLOCKING_TIME, 0);

                    // HLA Reporter support.
                    if (hlaManager.get_EnableHlaReporter()) {
                        hlaReporter._numberOfTicks2++;
                        hlaReporter._numberOfTicks
                                .set(hlaReporter._numberOfTAGs,
                                        hlaReporter._numberOfTicks
                                                .get(hlaReporter._numberOfTAGs)
                                                + 1);
                    }

                } catch (RTIinternalError e) {
                    throw new IllegalActionException(hlaManager, e, "***** RTI Internal error : " + e.getMessage());
                } catch (CallNotAllowedFromWithinCallback e) {
                    throw new IllegalActionException(hlaManager, e, "Call Not Allowed From Within Callback : " +e.getMessage());
                }

            } //  end while

            // A TAG was received then the HLA current time is updated for this federate
            federateAmbassador.hlaLogicalTime = nextPointInTime;

            // If one or more RAV are received, HLA guarantees that their time
            // stamps will never be smaller than the current HLA time h (neither
            // larger than nextPointInTime). And proposeTime method must return
            // the same proposed time or a smaller time.
            // This guarantees that the received time stamp is smaller. If,
            // because of the time conversion, the newPtolemyTime appears
            // as bigger than the proposedTime, keep proposedTime. Otherwise
            // update it to newPtolemyTime.
            if (federateAmbassador.hasReceivedRAV) {

                // Convert HLA time to Time so it can be compared with Ptolemy time
                Time newPtolemyTime = convertToPtolemyTime(
                        (CertiLogicalTime1516E) federateAmbassador.hlaLogicalTime);

                // In the general case newPtolemyTime is smaller than proposedTime
                // and proposedTime is updated to newPtolemyTime. This value will
                // be the time stamp on the output of the HlaReflectable actor.
                if (newPtolemyTime.compareTo(proposedTime) < 0) {
                    if (hlaManager.getDebugging()) {
                        hlaDebug("    newPtolemyTime= t'=t''=f(h)="
                                + newPtolemyTime.toString()
                                + " @line 10 in algo 4 " + headMsg);
                    }
                    proposedTime = newPtolemyTime;
                }

                // Store reflected attributes RAV as events on HlaReflectable actors.
                // Notice that proposedTime here is a multiple of hlaManager.get_HlaTimeStep().
                putReflectedAttributesOnHlaReflectables(proposedTime);

                // Reinitialize variable
                federateAmbassador.hasReceivedRAV = false;

                // Ptolemy time is updated
                if (hlaManager.getDebugging()) {
                    hlaDebug("Returns proposedTime=" + proposedTime.toString()
                            + "    (if hasReceivedRAV) " + headMsg
                            + "\n");
                }
                return proposedTime;

            } // end if receivedRAV then

            // Update local variables with the new HLA logical time.
            hlaLogicaltime = (CertiLogicalTime1516E) federateAmbassador.hlaLogicalTime;
            nextPointInTime = new CertiLogicalTime1516E(
                    hlaLogicaltime.getTime() + hlaManager.get_HlaTimeStep());

        } // end while:

        if (hlaManager.getDebugging()) {
            hlaDebug("returns proposedTime=" + proposedTime.toString() + "from "
                    + headMsg);
        }

        // All needed TAR were called. Update Ptolemy time to the time the
        // federate asked to advance.
        return proposedTime;
    }

    /** Convert Ptolemy time, which has units of seconds, to HLA logical time
     *  units. Ptolemy time is implemented using Java classe Time and HLA time
     *  uses IEEE-754 double.
     *  @param pt The Ptolemy time.
     *  @return The time in units of HLA logical time.
     */
    private CertiLogicalTime1516E convertToCertiLogicalTime(Time pt) {
        return new CertiLogicalTime1516E(pt.getDoubleValue() * hlaManager.get_HlaTimeUnitValue());
    }

    /** Convert CERTI (HLA) logical time (IEEE-754 double) to Ptolemy time.
     *  @param ct The CERTI (HLA) logical time.
     *  @return the time converted to Ptolemy time.
     *  @exception IllegalActionException If the given double time value does
     *  not match the time resolution.
     */
    private Time convertToPtolemyTime(CertiLogicalTime1516E ct)
            throws IllegalActionException {
        return new Time(director, ct.getTime() / hlaManager.get_HlaTimeUnitValue());
    }

    /** Time advancement method for event-based federates. This method
     *  uses NER RTI service to propose a time t' to advance to
     *  in a HLA simulation. The federate wants to advance to
     *  <i>proposedTime</i> but if a RAV(t'') is received in this time advance
     *  phase, then <i>proposeTime</i> returns t'', the time granted by the RTI.
     *  All RAV received are put in the HlaReflectable actors.
     *  Eventually a new NER(t') will be called until t' will be granted. See [6],
     *  algorithm 3.
     *  A federate has two timelines: the Ptolemy timeline ptolemyTime and the
     *  HLA timeline hlaLogicalTime. When NER is used, they have the same value
     *  in the general case.

     *  @param proposedTime time stamp of the last found event.
     *  @return the granted time from the HLA simulation.
     *  @exception IllegalActionException
     */
    private Time eventsBasedTimeAdvance(Time proposedTime)
            throws IllegalActionException
            /*, InvalidFederationTime,
            FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress,
            FederateNotExecutionMember, SaveInProgress,
            EnableTimeRegulationPending, EnableTimeConstrainedPending,
            RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted,
            SpecifiedSaveLabelDoesNotExist
            */
            {
        // Custom string representation of proposedTime.
        String strProposedTime = proposedTime.toString();

        // HLA Reporter support.
        if (hlaManager.get_EnableHlaReporter()) {
            hlaReporter.storeTimes("NER()", proposedTime,
                    director.getModelTime());
        }

        if (hlaManager.getDebugging()) {
            hlaDebug("eventsBasedTimeAdvance(): strProposedTime"
                    + " proposedTime=" + proposedTime.toString()
                    + " - calling CERTI NER()");
        }

        // The director global time resolution may be used later in the
        // particular case where HLA time advances but Ptolemy times does not.
        Double r = director.getTimeResolution();

        // Read current Ptolemy time and HLA time (provided by the RTI)
        Time ptolemyTime = director.getModelTime();
        CertiLogicalTime1516E hlaLogicaltime = (CertiLogicalTime1516E) federateAmbassador.hlaLogicalTime;

        // Convert proposedTime to double so it can be compared with HLA time
                CertiLogicalTime1516E certiProposedTime = convertToCertiLogicalTime(
                proposedTime);

        // Call the NER service if the time Ptolemy wants to advance to is
        // bigger than current HLA time
        if (certiProposedTime.compareTo(hlaLogicaltime) > 0) {
            // Wait the time grant from the HLA/CERTI Federation (from the RTI).
            federateAmbassador.timeAdvanceGrant = false;

            if (hlaManager.get_EnableHlaReporter()) {
                // Set time of last time advance request.
                hlaReporter.setTimeOfTheLastAdvanceRequest(System.nanoTime());
            }

            // Call CERTI NER HLA service.
            try {
                rtia.nextMessageRequest(certiProposedTime);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (hlaManager.get_EnableHlaReporter()) {
                // Increment counter of NER calls.
                hlaReporter.incrNumberOfNERs();
            }

            while (!(federateAmbassador.timeAdvanceGrant)) {
                if (director.isStopRequested()) {
                    // Stop is requested by the user.
                    // Not sure what to do here, but we can't just keep waiting.
                    throw new IllegalActionException(hlaManager,
                            "Stop requested while waiting for a time advance grant from the RTIG.");
                }
                if (hlaManager.getDebugging()) {
                    hlaDebug("        proposeTime(t(lastFoundEvent)="
                            + strProposedTime + ") - eventsBasedTimeAdvance("
                            + strProposedTime + ") - " + " waiting TAG(" //jc: + certiProposedTime.getTime()
                            + ") by calling tick(MAX_BLOCKING_TIME, 0)");
                }
                // Wait for a callback. This will return immediately if there
                // is already a callback pending (and before returning, it will
                // call that callback). If there is no callback pending, it will
                // wait up to MAX_BLOCKING_TIME and then return even if there is
                // no callback.
                // Do not use tick2() here because it can block the director
                // if no callback is received. Also, do not use tick() because it
                // results in a busy wait.
                // NOTE: The second argument, which the API confusingly calls "max"
                // but is usually less than "min", appears to not be used.
                try {
                    // Both evoke methods seem to give the same simulation speed
                    rtia.evokeMultipleCallbacks(MAX_BLOCKING_TIME, 0); // rtia.evokeCallback(MAX_BLOCKING_TIME);
                } catch (CallNotAllowedFromWithinCallback callNotAllowedFromWithinCallback) {
                    throw new IllegalActionException(hlaManager, callNotAllowedFromWithinCallback, "Call not allowed within callback");
                } catch (RTIinternalError rtIinternalError) {
                    throw new IllegalActionException(hlaManager, rtIinternalError, "RTI internal error");
                }

                // HLA Reporter support.
                if (hlaManager.get_EnableHlaReporter()) {
                    hlaReporter._numberOfTicks2++;
                    hlaReporter._numberOfTicks.set(hlaReporter._numberOfTAGs,
                            hlaReporter._numberOfTicks
                                    .get(hlaReporter._numberOfTAGs) + 1);
                }

            } // end while

            // A TAG was received then the HLA current time is updated for this federate
            federateAmbassador.hlaLogicalTime = federateAmbassador.grantedHlaLogicalTime;

            // If a RAV was received, the time stamp of TAG is the same as the
            // RAV, and in the general case, the proposedTime will return with
            // this time stamp (converted to Time, the Ptolemy time representation).
            if (federateAmbassador.hasReceivedRAV) {
                Time newPtolemyTime = convertToPtolemyTime(
                        (CertiLogicalTime1516E) federateAmbassador.grantedHlaLogicalTime);

                // True in the general case. If several RAV callbacks are received
                // with the same time stamp, the microsteps of the corresponding
                // Ptolemy events are increased in the order of the RAV reception.
                if (newPtolemyTime.compareTo(ptolemyTime) > 0) {
                    proposedTime = newPtolemyTime;
                } else {
                    // However, it could happen that the RAV (and so the TAG)
                    // received corresponds to an HLA time increased by a
                    // value epsilon but, because of the time representation
                    // conversion, it appears as equal to Ptolemy current time t.
                    // In this case, inserting a new event at e(t;HlaUpdater)
                    // can be a problem to the director, since all existing
                    // events e(t;HlaSubs) have already been treated.
                    // In order to advance the Ptolemy time, the time resolution
                    // is added to the current time t, since it is the shortest
                    // value that can be add for advancing the time beyond t.
                    proposedTime = ptolemyTime.add(r);
                }

                // Store reflected attributes RAV as events on HlaReflectable actors.
                putReflectedAttributesOnHlaReflectables(proposedTime);

                federateAmbassador.hasReceivedRAV = false;

            } // end  if receivedRAV then

        } //  end if

        return proposedTime;

    }

    /** Get the current time in HLA (using double) which is advanced after a TAG callback.
     *  @return the HLA current time converted to a Ptolemy time, which is in
     *   units of seconds (and uses Java class Time).
     */

    private Time getHlaCurrentTime() throws IllegalActionException {
        CertiLogicalTime1516E certiCurrentTime = (CertiLogicalTime1516E) federateAmbassador.hlaLogicalTime;
        return convertToPtolemyTime(certiCurrentTime);
    }

    /** This method is called when a time advancement phase is performed. Every
     *  updated HLA attribute received by callbacks (from the RTI) during the
     *  time advancement phase is saved as a {@link TimedEvent} and stored in a
     *  queue. Then, every {@link TimedEvent} is moved from this queue to the
     *  output port of their corresponding {@link HlaReflectable} actors
     *  @exception IllegalActionException If the parent class throws it.
     */
    private void putReflectedAttributesOnHlaReflectables(Time proposedTime)
            throws IllegalActionException {
        // Reflected HLA attributes, i.e., updated values of HLA attributes
        // received by callbacks (from the RTI) from the whole HLA/CERTI
        // Federation, are stored in a queue (see reflectAttributeValues()
        // in PtolemyFederateAmbassadorInner class).

        if (hlaManager.getDebugging()) {
            hlaDebug("       t_ptII = " + director.getModelTime().toString()
                    + "; t_hla = " + ((CertiLogicalTime1516E)federateAmbassador.hlaLogicalTime).getTime()
                    + " in _putReflectedAttributesOnHlaReflectables("
                    + proposedTime.toString() + ")");
        }

        Iterator<Map.Entry<String, LinkedList<TimedEvent>>> it = fromFederationEvents
                .entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, LinkedList<TimedEvent>> elt = it.next();

            // Multiple events can occur at the same time.
            LinkedList<TimedEvent> events = elt.getValue();
            while (events.size() > 0) {

                HlaTimedEvent ravEvent = (HlaTimedEvent) events.get(0);

                // Update received RAV event timestamp (see [6]).
                if (hlaManager.get_TimeStepped()) {
                    // The Ptolemy event corresponding to a RAV has
                    // time stamp equal to HLA current time, e(f(h+TS))
                    ravEvent.timeStamp = getHlaCurrentTime();
                } else {
                    // The Ptolemy event corresponding to a RAV has
                    // time stamp equal to current Ptolemy time. Indeed, no
                    // modification is made to the received time stamp.
                    ravEvent.timeStamp = proposedTime;
                }

                // If any RAV-event received by HlaReflectable actors, RAV(tau),
                // with tau < Ptolemy startTime, they
                // are put in the event queue with timestamp startTime.
                // Usually startTime=0.
                if (ravEvent.timeStamp
                        .compareTo(director.getModelStartTime()) < 0) {
                    ravEvent.timeStamp = director.getModelStartTime();
                }

                // Get the HlaReflectable actor to which the event is destined to.
                String actorName = elt.getKey();

                TypedIOPort tiop = getPortFromTab(
                        hlaAttributesToSubscribeTo.get(actorName));

                HlaReflectable hs = (HlaReflectable) tiop.getContainer();
                hs.putReflectedHlaAttribute(ravEvent);

                if (hlaManager.getDebugging()) {
                    hlaDebug("       _putRAVOnHlaReflectable(" + proposedTime.toString()
                            + " ravEvent.timeStamp=" + ravEvent.timeStamp
                            + ") for '" + hs.getHlaAttributeName()
                            + " in HlaSubs=" + hs.getFullName());
                }

                if (hlaManager.get_EnableHlaReporter()) {
                    hlaReporter.updateFolRAVsTimes(ravEvent.timeStamp);
                }

                // Remove handled event.
                events.removeFirst();
            }
        }

        // At this point we have handled all events for all registered,
        // HlaReflectable actors, so we may clear the receivedRAV boolean.
        federateAmbassador.hasReceivedRAV = false;

        if (hlaManager.getDebugging()) {
            hlaDebug("        _putRAVOnHlaSubs(" + proposedTime.toString()
                    + ") - no more RAVs to deal with");
        }
    }

    /** This method does the initial synchronization among the
     *  federates and registers the synchronization point if this federate's
     *  name matches the synchronizeStartTo parameter.
     *  @exception IllegalActionException If the RTI throws an exception.
     */
    private void doInitialSynchronization() throws IllegalActionException {
        String synchronizationPointName = hlaManager.synchronizeStartTo.stringValue().trim();
        // If the current Federate is the register of a synchronization point,
        // then register the synchronization point.
        hlaDebug("isSynchronizationPointRegister : " + isSynchronizationPointRegister);
        if (isSynchronizationPointRegister) {
            hlaDebug("isSynchronizationPointRegister = true");
            try {
                HLAASCIIstring rfspTag =  new HLAASCIIstringImpl(synchronizationPointName);
                byte[] rfspTagByte = new byte[rfspTag.getEncodedLength()];
                ByteWrapper rfspTagWrapper = new ByteWrapper(rfspTagByte);
                rfspTag.encode(rfspTagWrapper);
                rtia.registerFederationSynchronizationPoint(
                        synchronizationPointName, rfspTagByte);

                // Wait for synchronization point callback.
                while (!(federateAmbassador.synchronizationSuccess)
                        && !(federateAmbassador.synchronizationFailed)) {
                    if (director.isStopRequested()) {
                        // Stop is requested by the user.
                        // Since this is called at the end of initialize(), it is safe
                        // just return. The director will proceed directly to wrapup().
                        return;
                    }
                    // Wait for a callback. This will return immediately if there
                    // is already a callback pending (and before returning, it will
                    // call that callback). If there is no callback pending, it will
                    // wait up to MAX_BLOCKING_TIME and then return even if there is
                    // no callback.
                    // Do not use tick2() here because it can block the director
                    // if no callback is received. Also, do not use tick() because it
                    // results in a busy wait.
                    // NOTE: The second argument, which the API confusingly calls "max"
                    // but is usually less than "min", appears to not be used.
                    
                    // Both evoke methods seem to give the same simulation speed
                    rtia.evokeMultipleCallbacks(MAX_BLOCKING_TIME, 0); // rtia.evokeCallback(MAX_BLOCKING_TIME);

                    logOtherTicks();
                }
            } catch (RTIexception e) {
                throw new IllegalActionException(hlaManager, e,
                        "RTIexception: " + e.getMessage());
            }

            if (federateAmbassador.synchronizationFailed) {
                throw new IllegalActionException(hlaManager,
                        "CERTI: Synchronization failed! ");
            }
        } // End block for synchronization point creation case.

        // The first launched federates wait for synchronization point announcement.
        while (!(federateAmbassador.inPause)) {
            if (director.isStopRequested()) {
                // Stop is requested by the user.
                // Not sure what to do here, but we can't just keep waiting.
                throw new IllegalActionException(hlaManager,
                        "Stop requested while waiting for a time advance grant from the RTIG.");
            }
            try {
                // Wait for a callback. This will return immediately if there
                // is already a callback pending (and before returning, it will
                // call that callback). If there is no callback pending, it will
                // wait up to MAX_BLOCKING_TIME and then return even if there is
                // no callback.
                // Do not use tick2() here because it can block the director
                // if no callback is received. Also, do not use tick() because it
                // results in a busy wait.
                // NOTE: The second argument, which the API confusingly calls "max"
                // but is usually less than "min", appears to not be used.
                
                // Both evoke methods seem to give the same simulation speed 
                rtia.evokeMultipleCallbacks(MAX_BLOCKING_TIME, 0); // rtia.evokeCallback(MAX_BLOCKING_TIME);

                logOtherTicks();
            } catch (RTIexception e) {
                throw new IllegalActionException(hlaManager, e,
                        "RTIexception: " + e.getMessage());
            }
        }
        // Satisfied synchronization point.
        try {
            rtia.synchronizationPointAchieved(synchronizationPointName);
            if (hlaManager.getDebugging()) {
                hlaDebug("_doInitialSynchronization() - initialize() - Synchronisation point "
                        + synchronizationPointName + " satisfied");
            }
        } catch (RTIexception e) {
            throw new IllegalActionException(hlaManager, e,
                    "RTIexception: " + e.getMessage());
        }

        // Wait for federation synchronization.
        while (federateAmbassador.inPause) {
            if (director.isStopRequested()) {
                // Return to finish initialization so that we proceed to wrapup
                // and resign the federation.
                return;
            }
            if (hlaManager.getDebugging()) {
                hlaDebug("_doInitialSynchronization() - initialize() - Waiting for simulation phase");
            }

            try {
                // Wait for a callback. This will return immediately if there
                // is already a callback pending (and before returning, it will
                // call that callback). If there is no callback pending, it will
                // wait up to MAX_BLOCKING_TIME and then return even if there is
                // no callback.
                // Do not use tick2() here because it can block the director
                // if no callback is received. Also, do not use tick() because it
                // results in a busy wait.
                // NOTE: The second argument, which the API confusingly calls "max"
                // but is usually less than "min", appears to not be used.
                
                // Both evoke methods seem to give the same simulation speed
                rtia.evokeMultipleCallbacks(MAX_BLOCKING_TIME, 0); // rtia.evokeCallback(MAX_BLOCKING_TIME);

                logOtherTicks();
            } catch (RTIexception e) {
                throw new IllegalActionException(hlaManager, e,
                        "RTIexception: " + e.getMessage());
            }
        }
    }

    /** This method enables all time regulating aspect for the federate. After
     *  this call the federate has stated to the RTI if it is time regulating
     *  and/or time regulator. In the current implementation all federates are
     *  time regulating and time constrained. This method also enables
     *  asynchronous delivery, a service that instructs the rtia "to begin
     *  delivering received-ordered (RO) messages even while non time-
     *  advancement services is in progress" [1].
     *  @exception IllegalActionException if the RTI throws it.
     */
    private void initializeTimeAspects() throws IllegalActionException {
        // Initialize Federate timing values.
        federateAmbassador.initializeTimeValues(0.0, hlaManager.get_HlaLookAHead());

        // Declare the Federate time constrained (if true).
        if (hlaManager.get_IsTimeConstrained()) {
            try {
                rtia.enableTimeConstrained();
            } catch (RTIexception e) {
                throw new IllegalActionException(hlaManager, e,
                        "RTIexception: " + e.getMessage());
            }
        }

        // Declare the Federate to be a time regulator (if true).
        if (hlaManager.get_IsTimeRegulator()) {
            try {
                rtia.enableTimeRegulation(federateAmbassador.effectiveLookAHead);
            } catch (RTIexception e) {
                throw new IllegalActionException(hlaManager, e,
                        "RTIexception: " + e.getMessage());
            }
        }

        // Wait the response of the RTI towards Federate time policies that has
        // been declared. The only way to get a response is to invoke the tick()
        // method to receive callbacks from the RTI.
        if (hlaManager.get_IsTimeRegulator() && hlaManager.get_IsTimeConstrained()) {

            while (!(federateAmbassador.timeConstrained)) {
                if (director.isStopRequested()) {
                    // Return to finish initialization so that we proceed to wrapup
                    // and resign the federation.
                    return;
                }
                try {
                    // Wait for a callback. This will return immediately if there
                    // is already a callback pending (and before returning, it will
                    // call that callback). If there is no callback pending, it will
                    // wait up to MAX_BLOCKING_TIME and then return even if there is
                    // no callback.
                    // Do not use tick2() here because it can block the director
                    // if no callback is received. Also, do not use tick() because it
                    // results in a busy wait.
                    // NOTE: The second argument, which the API confusingly calls "max"
                    // but is usually less than "min", appears to not be used.
                    
                    // Both evoke methods seem to give the same simulation speed
                    rtia.evokeMultipleCallbacks(MAX_BLOCKING_TIME, 0); // rtia.evokeCallback(MAX_BLOCKING_TIME);

                    // HLA Reporter support.
                    if (hlaManager.get_EnableHlaReporter()) {
                        hlaReporter._numberOfTicks2++;
                        hlaReporter._numberOfOtherTicks++;
                    }
                } catch (RTIexception e) {
                    throw new IllegalActionException(hlaManager, e,
                            "RTIexception: " + e.getMessage());
                } catch (Exception e){
                    throw new IllegalActionException(hlaManager, e,
                            "Exception: " + e.getMessage());
                }
            }

            while (!(federateAmbassador.timeRegulator)) {
                if (director.isStopRequested()) {
                    // Return to finish initialization so that we proceed to wrapup
                    // and resign the federation.
                    return;
                }
                try {
                    // Wait for a callback. This will return immediately if there
                    // is already a callback pending (and before returning, it will
                    // call that callback). If there is no callback pending, it will
                    // wait up to MAX_BLOCKING_TIME and then return even if there is
                    // no callback.
                    // Do not use tick2() here because it can block the director
                    // if no callback is received. Also, do not use tick() because it
                    // results in a busy wait.
                    // NOTE: The second argument, which the API confusingly calls "max"
                    // but is usually less than "min", appears to not be used.
                    // Both evoke methods seem to give the same simulation speed
                    rtia.evokeMultipleCallbacks(MAX_BLOCKING_TIME, 0); // rtia.evokeCallback(MAX_BLOCKING_TIME);

                    // HLA Reporter support.
                    if (hlaManager.get_EnableHlaReporter()) {
                        hlaReporter._numberOfTicks2++;
                        hlaReporter._numberOfOtherTicks++;
                    }
                } catch (RTIexception e) {
                    throw new IllegalActionException(hlaManager, e,
                            "RTIexception: " + e.getMessage());
                }
            }


            if (hlaManager.getDebugging()) {
                hlaDebug("_initializeTimeAspects() - initialize() -"
                        + " Time Management policies:" + " is Constrained = "
                        + federateAmbassador.timeConstrained
                        + " and is Regulator = "
                        + federateAmbassador.timeRegulator);
            }

            // The following service is required to allow the reception of
            // callbacks from the RTI when a Federate is time constrained.
            try {
                rtia.enableAsynchronousDelivery();
            } catch (RTIexception e) {
                throw new IllegalActionException(hlaManager, e,
                        "RTIexception: " + e.getMessage());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private static methods                 ////

    /** Check that the specified label matches the synchronization point
     *  name given in the synchronizeStartTo parameter.
     *  @param synchronizationPointLabel The label.
     *  @return False if the names don't match.
     */
    private boolean checkSynchronizationPointNameMatch(String synchronizationPointLabel) {
        try {
            String mySynchronizationPoint = hlaManager.synchronizeStartTo.stringValue();
            if (!synchronizationPointLabel.equals(mySynchronizationPoint)) {
                // The synchronization point does not match.
                // Having mismatched synchronization point names can cause the model
                // to deadlock, so we print a warning.
                System.err.println("WARNING: Mismatch between synchronization point name "
                        + synchronizationPointLabel
                        + " and the value of the synchronizeStartTo parameter: "
                        + mySynchronizationPoint
                        + ". This can cause a federation to deadlock!");
                return false;
            }
        } catch (IllegalActionException e) {
            // There is something wrong with my synchronization point specification.
            // This will be caught during initialization, so we can safely ignore here.
            return false;
        }
        return true;
    }

    /* Getter functions to ease access to information stored in an object
     * array about HLA attributes to publish or to subscribe to. */

    /** Simple getter function to retrieve the TypedIOPort instance from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the instance of TypedIOPort
     */
    private static TypedIOPort getPortFromTab(Object[] array) {
        return (TypedIOPort) array[0];
    }

    /** Simple getter function to retrieve the Type instance from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the instance of Type
     */
    static private Type getTypeFromTab(Object[] array) {
        return (Type) array[1];
    }

    /** Simple getter function to retrieve the class object name from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the class object name as String
     */
    static private String getHlaClassNameFromTab(Object[] array) {
        return (String) array[2];
    }

    /** Simple getter function to retrieve the class instance name from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the class instance name as String
     */
    static private String getHlaInstanceNameFromTab(Object[] array) {
        return (String) array[3];
    }

    /** Simple getter function to retrieve the class handle from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the class handle as Integer
     */
    static private ObjectClassHandle getClassHandleFromTab(Object[] array) {
        if(array[4] instanceof CertiObjectHandle)
            return (CertiObjectHandle) array[4];
        else if(array[4] instanceof Integer)
            return new CertiObjectHandle((Integer) array[4]);
        return null;
    }

    /** Simple getter function to retrieve the attribute handle from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the attribute handle as Integer
     */
    static private AttributeHandle getAttributeHandleFromTab(Object[] array) {
        return (AttributeHandle) array[5];
    }

    /** Use the HLA reporter class to log an "other tick".
     */
    private void logOtherTicks() {
        // HLA Reporter support.
        if (hlaManager.get_EnableHlaReporter()) {
            hlaReporter._numberOfTicks2++;
            if (hlaReporter.getTimeOfTheLastAdvanceRequest() > 0) {
                hlaReporter._numberOfTicks
                        .set(hlaReporter._numberOfTAGs,
                                hlaReporter._numberOfTicks
                                        .get(hlaReporter._numberOfTAGs)
                                        + 1);
            } else {
                hlaReporter._numberOfOtherTicks++;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         constants                         ////

    /** The maximum amount of time that a blocking call to tick() will
     *  wait for callbacks to occur.
     */
    private static double MAX_BLOCKING_TIME = 1.0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** This class extends the {@link NullFederateAmbassador} class which
     *  implements the basics HLA services provided by the JCERTI bindings.
     *  @author Gilles Lasnier
     */
    private class PtolemyFederateAmbassadorInner
            extends NullFederateAmbassador {

        ///////////////////////////////////////////////////////////////////
        ////                         public variables                  ////

        /** The lookahead value set by the user and used by CERTI to handle
         *  time management and to order time-stamp-ordered (TSO) events.
         */
        public LogicalTimeInterval effectiveLookAHead;

        /** Indicates the granted HLA logical time of the Federate. This value
         *  is set by callback by the RTI.
         */
        public LogicalTime grantedHlaLogicalTime;

        /** Indicates the current HLA logical time of the Federate. */
        public LogicalTime hlaLogicalTime;

        /** Indicates if the Federate is currently synchronized to others. This
         * value is set by callback by the RTI.
         */
        public boolean inPause;

        /** Indicates if an RAV has been received. */
        public boolean hasReceivedRAV;

        /** Indicates if the request of synchronization by the Federate is
         *  validated by the HLA/CERTI Federation. This value is set by callback
         *  by the RTI.
         */
        public boolean synchronizationSuccess;

        /** Indicates if the request of synchronization by the Federate
         *  has failed. This value is set by callback by the RTI.
         */
        public boolean synchronizationFailed;

        /** Indicates if the Federate has received the time advance grant from
         *  the HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public boolean timeAdvanceGrant;

        /** Indicates if the Federate is declared as time constrained in the
         *  HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public boolean timeConstrained;

        /** Indicates if the Federate is declared as time regulator in the
         *  HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public boolean timeRegulator;

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Initialize the {@link PtolemyFederateAmbassadorInner} which handles
         *  the communication from RTI -> to RTIA -> to FEDERATE. The
         *  <i>rtia</i> manages the interaction with the external communicant
         *  process RTIA. This method called the Declaration Management
         *  services provide by HLA/CERTI to publish/subscribe to HLA attributes
         *  in a HLA Federation.
         *  @param rtia
         *  @exception NameNotFound
         *  @exception ObjectClassNotDefined
         *  @exception FederateNotExecutionMember
         *  @exception RTIinternalError
         *  @exception AttributeNotDefined
         *  @exception SaveInProgress
         *  @exception RestoreInProgress
         *  @exception ConcurrentAccessAttempted
         *  All those exceptions above are from the HLA/CERTI implementation.
         *  @exception IllegalActionException
         *  All those exceptions above are from Ptolemy.
         */
        public void initialize(RTIambassador rtia) throws NameNotFound,
                ObjectClassNotDefined, FederateNotExecutionMember,
                RTIinternalError, SaveInProgress, RestoreInProgress,
                ConcurrentAccessAttempted, IllegalActionException {

            // Retrieve model stop time
            stopTime = director.getModelStopTime();

            if (hlaManager.get_EnableHlaReporter()) {
                hlaReporter._numberOfTicks.add(0);
            }

            this.inPause = false;
            this.synchronizationSuccess = false;
            this.synchronizationFailed = false;
            this.timeAdvanceGrant = false;
            this.timeConstrained = false;
            this.timeRegulator = false;
            this.hasReceivedRAV = false;

            // Configure HlaUpdatable actors from model */
            if (!hlaAttributesToPublish.isEmpty()) {
                setupHlaUpdatables(rtia);
            } else {
                if (hlaManager.getDebugging()) {
                    hlaDebug("INNER initialize: _hlaAttributesToPublish is empty");
                }
            }
            // Configure HlaReflectable actors from model */
            if (!hlaAttributesToSubscribeTo.isEmpty()) {
                setupHlaReflectables(rtia);
            } else {
                if (hlaManager.getDebugging()) {
                    hlaDebug("INNER initialize: hlaAttributesToSubscribeTo is empty");
                }
            }

        }

        /** Initialize Federate's timing properties provided by the user.
         *  @param startTime The start time of the Federate logical clock.
         *  @param lookAHead The contract value used by HLA/CERTI to synchronize
         *  the Federates and to order TSO events.
         *  @exception IllegalActionException
         */
        public void initializeTimeValues(Double startTime, Double lookAHead)
                throws IllegalActionException {
            if (hlaManager.get_HlaLookAHead() <= 0) {
                throw new IllegalActionException(null, null, null,
                        "LookAhead field in HLAManager must be greater than 0.");
            }
            hlaLogicalTime = new CertiLogicalTime1516E(startTime);
            grantedHlaLogicalTime = new CertiLogicalTime1516E(0);
            // The hlaLookAHead is already in  HLA logical time units.
            effectiveLookAHead = new CertiLogicalTimeInterval1516E(hlaManager.get_HlaLookAHead());
            if (hlaManager.getDebugging()) {
                hlaDebug("initializeTimeValues() - Effective HLA lookahead is "
                        + ((CertiLogicalTimeInterval1516E)effectiveLookAHead).getInterval());
            }
            timeAdvanceGrant = false;

        }

        // HLA Object Management services (callbacks).

        /** Callback to receive updated value of a HLA attribute from the
         *  whole Federation (delivered by the RTI (CERTI)).
         */
        @Override
        public void reflectAttributeValues(ObjectInstanceHandle theObject,
                                           AttributeHandleValueMap theAttributes,
                                           byte[] userSuppliedTag,
                                           OrderType sentOrdering,
                                           TransportationTypeHandle theTransport,
                                           LogicalTime theTime,
                                           OrderType receivedOrdering,
                                           SupplementalReflectInfo reflectInfo)
                throws FederateInternalError {

            if (hlaManager.getDebugging()) {
                hlaDebug("      t_ptII = " + director.getModelTime()
                        + "; t_hla = " + ((CertiLogicalTime1516E)federateAmbassador.hlaLogicalTime).getTime()
                        + " start reflectAttributeValues(), INNER callback");
            }
            // Get the object class handle corresponding to
            // the received "theObject" handle.
            int classHandle = objectHandleToClassHandle.get(theObject.hashCode());

            String instanceNameOrJokerName = discoverObjectInstanceMap.get(theObject.hashCode());
            Set<AttributeHandle> attributeHandleSet = theAttributes.keySet();
            hlaDebug("attributeHandleSet.size = " + attributeHandleSet.size());

            for(AttributeHandle attributeHandle : attributeHandleSet) {
                Iterator<Map.Entry<String, Object[]>> ot = hlaAttributesToSubscribeTo.entrySet().iterator();
                while (ot.hasNext()) {
                    Map.Entry<String, Object[]> elt = ot.next();
                    Object[] tObj = elt.getValue();
                    Time ts = null;
                    TimedEvent te = null;
                    Object value = null;
                    HlaReflectable hs = (HlaReflectable) getPortFromTab(tObj).getContainer(); //???

                    if (hlaManager.getDebugging()) {
                        String theAttributesString = "";
                        Set<AttributeHandle> attributeKeys = theAttributes.keySet();
                        for(AttributeHandle key : attributeKeys){
                            theAttributesString += "[attributeHandle=" + key.hashCode() + ", value=" + new String(theAttributes.get(key)) + "]";
                        }
                        hlaDebug("INNER callback: reflectAttributeValues():"
                                + " theObject=" + theObject.hashCode()
                                + " attributeHandle=" + attributeHandle.hashCode()
                                + " userSuppliedTag=" + new String(userSuppliedTag)
                                + " theTime=" + ((CertiLogicalTime1516E)theTime).getTime()
                                + " classHandle=" + classHandle
                                + " instanceNameOrJokerName=" + instanceNameOrJokerName
                                + " HlaSusbcriber=" + attributeHandle.hashCode());
                    }

                    // The tuple (attributeHandle, classHandle, classInstanceName)
                    // allows to identify the object attribute (i.e. one of the HlaReflectables)
                    // where the updated value has to be put.

                    try {
//                        hlaDebug("*Comparaison*");
//                        hlaDebug("-----attribute handle : " + attributeHandle.hashCode() + "==" + hs.getAttributeHandle());
//                        hlaDebug("-----class handle : " + classHandle + "==" + hs.getClassHandle());
//                        hlaDebug("-----instanceNameOrJokerName : " + instanceNameOrJokerName);
//                        hlaDebug("-----instance handle : " + hs.getHlaInstanceName() + "==" + instanceNameOrJokerName);
                        if (attributeHandle.hashCode() == hs.getAttributeHandle()
                                && classHandle == hs.getClassHandle() //)
                                && (instanceNameOrJokerName != null
                                && hs.getHlaInstanceName().compareTo(instanceNameOrJokerName) == 0))
                         {
                            hlaDebug("--- Attribute id match");
                            double timeValue = ((CertiLogicalTime1516E) theTime).getTime() / hlaManager.get_HlaTimeUnitValue();
                            ts = new Time(director, timeValue);
                            // Note: Sometimes a received RAV value is different than the UAV value sent.
                            // This could come from the decodeHlaValue and encodeHlaValue CERTI methods.
                            ByteWrapper bw = theAttributes.getValueReference(attributeHandle);
                            byte[] valueByte = bw.array();
                            value = MessageProcessing1516e.decodeHlaValue(getTypeFromTab(tObj), valueByte);
                            te = new HlaTimedEvent(ts,
                                    new Object[] { getTypeFromTab(tObj), value},
                                    theObject.hashCode());


                            fromFederationEvents.get(hs.getFullName()).add(te);

                            if (hlaManager.getDebugging()) {
                                hlaDebug("       *RAV '" + hs.getHlaAttributeName()
                                        + "', timestamp="
                                        + te.timeStamp.toString() + ",value="
                                        + value.toString() + " @ "
                                        + hs.getFullName());
                            }

                            // Notify RAV reception.
                            hasReceivedRAV = true;

                            if (hlaManager.get_EnableHlaReporter()) {
                                hlaReporter.updateRAVsInformation(hs,
                                        (HlaTimedEvent) te,
                                        director.getModelTime(),
                                        hlaAttributesToSubscribeTo, value);
                                hlaReporter.incrNumberOfRAVs();
                            }
                        }
                    }
                    catch (IllegalActionException e) {
                        // Java classic exceptions are encapsulated as FederateInternalError to avoid system prints.
                        //_hlaDebug(
                        //        "INNER callback: reflectAttributeValues(): EXCEPTION IllegalActionException");
                        //e.printStackTrace();

                        throw new FederateInternalError(
                                "INNER callback: reflectAttributeValues(): EXCEPTION IllegalActionException: "
                                        + e.getMessage());
                    }
                }

            }
        }

        /** Callback delivered by the RTI (CERTI) to discover attribute instance
         *  of HLA attribute that the Federate has subscribed to.
         */
        @Override
        public void discoverObjectInstance(ObjectInstanceHandle theObject,
                                           ObjectClassHandle theObjectClass,
                                           String objectName)
                throws FederateInternalError
                {
            // Special attention must be taken when the wildcard "joker_" is
            // used. The wildcard can be used only if all instances are treated
            // the same way, or if it is not important to know their names.
            // Recall that the  order in which the instances will be discovered
            // is not known before the run, but Ptolemy topological sort
            // guarantees that a joker is always binded to the same
            // instance (for a same set of federates). However, if the wildcard
            // of the HlaReflectable is binded to an instance that does not
            // have the attributeName of this actor updated later, then this
            // actor produces no output. See the manual for details.
            String matchingName = null;

            if (usedJoker) {
                String jokerFilter = null;

                // Find a valid non-used joker filter.
                Iterator<Map.Entry<String, Boolean>> it1 = usedJokerFilterMap
                        .entrySet().iterator();

                while (it1.hasNext()) {
                    Map.Entry<String, Boolean> elt = it1.next();
                    // elt.getKey()   => joker filter.
                    // elt.getValue() => joker is already used or not (boolean).
                    if (!elt.getValue().booleanValue()) {
                        jokerFilter = elt.getKey();
                        usedJokerFilterMap.put(jokerFilter, true);
                        if (hlaManager.getDebugging()) {
                            hlaDebug("INNER callback: discoverObjectInstance: found a free joker, break with jokerFilter="
                                    + jokerFilter);
                        }
                        break;
                    }
                }

                if (jokerFilter == null) {
                    if (hlaManager.getDebugging()) {
                        hlaDebug("INNER callback: discoverObjectInstance: no more filter available.\n"
                                + " instanceHandle=" + theObject.hashCode()
                                + " classHandle=" + theObjectClass.hashCode() + " someName="
                                + objectName
                                + " will be ignored during the simulation.");
                    }
                } else {
                    discoverObjectInstanceMap.put(theObject.hashCode(),
                            jokerFilter);
                    if (hlaManager.getDebugging()) {
                        hlaDebug("INNER callback: discoverObjectInstance: instanceHandle="
                                + theObject.hashCode() + " jokerFilter="
                                + jokerFilter + " matchingName="
                                + matchingName);
                    }

                    matchingName = jokerFilter;
                }
            } else {
                // Nominal case, an instance name was defined in the callback discoverObjectInstance.
                if (discoverObjectInstanceMap.containsKey(theObject.hashCode())) {
                    if (hlaManager.getDebugging()) {
                        hlaDebug("INNER callback: discoverObjectInstance: found an instance class already registered: "
                                + objectName);
                    }
                    // Note: this case should not happen with the new implementation from CIELE. But as it is
                    // difficult to test this case, we raise an exception.
                    throw new FederateInternalError(
                            "INNER callback: discoverObjectInstance(): EXCEPTION IllegalActionException: "
                                    + "found an instance class already registered: "
                                    + objectName);

                } else {
                    discoverObjectInstanceMap.put(theObject.hashCode(), objectName);
                    matchingName = objectName;
                }

            }

            // Bind object instance handle to class handle.
            objectHandleToClassHandle.put(theObject.hashCode(), theObjectClass.hashCode());

            // Joker support
            if (matchingName != null) {
                // Get classHandle and attributeHandle for each attribute
                // value to subscribe to. Update the HlaReflectable.
                Iterator<Map.Entry<String, Object[]>> it1 = hlaAttributesToSubscribeTo
                        .entrySet().iterator();

                while (it1.hasNext()) {
                    Map.Entry<String, Object[]> elt = it1.next();
                    // elt.getKey()   => HlaReflectable actor full name.
                    // elt.getValue() => tObj[] array.
                    Object[] tObj = elt.getValue();

                    // Get corresponding HlaReflectable actor.
                    HlaReflectable sub = (HlaReflectable) ((TypedIOPort) tObj[0])
                            .getContainer();
                    // Set the instance handle in the data structure
                    // corresponding to the HlaReflectable actor that matches
                    // the jokerFilter.
                    try {
                        if (sub.getHlaInstanceName()
                                .compareTo(matchingName) == 0) {
                            sub.setInstanceHandle(theObject.hashCode());

                            if (hlaManager.getDebugging()) {
                                hlaDebug("INNER callback: discoverObjectInstance: matchingName="
                                        + matchingName + " hlaSub="
                                        + sub.getFullName());
                            }

                        }
                    } catch (IllegalActionException e) {
                        throw new FederateInternalError(
                                "INNER callback: discoverObjectInstance(): EXCEPTION IllegalActionException: "
                                        + "cannot retrieve HlaReflectable actor class instance name.");
                    }
                }
            }

            if (hlaManager.getDebugging()) {
                hlaDebug("INNER callback:"
                        + " discoverObjectInstance(): the object"
                        + " instanceHandle=" + theObject.hashCode()
                        + " classHandle=" + theObjectClass.hashCode()
                        + " classIntanceOrJokerName=" + objectName);
            }
        }

        // HLA Time Management services (callbacks).

        /** Callback delivered by the RTI (CERTI) to validate that the Federate
         *  is declared as time-regulator in the HLA Federation.
         */
        @Override
        public void timeRegulationEnabled(LogicalTime theFederateTime)
                throws FederateInternalError {
            timeRegulator = true;
            if (hlaManager.getDebugging()) {
                hlaDebug("INNER callback:"
                        + " timeRegulationEnabled(): timeRegulator = "
                        + timeRegulator);
            }
        }

        /** Callback delivered by the RTI (CERTI) to validate that the Federate
         *  is declared as time-constrained in the HLA Federation.
         */
        @Override
        public void timeConstrainedEnabled(LogicalTime theFederateTime)
                throws FederateInternalError {
            timeConstrained = true;
            if (hlaManager.getDebugging()) {
                hlaDebug("INNER callback:"
                        + " timeConstrainedEnabled(): timeConstrained = "
                        + timeConstrained);
            }
        }

        /** Callback (TAG) delivered by the RTI (CERTI) to notify that the
         *  Federate is authorized to advance its time to <i>theTime</i>.
         *  This time is the same or smaller than the time specified
         *  when calling the nextEventRequest() or the timeAdvanceRequest()
         *  services.
         */
        @Override
        public void timeAdvanceGrant(LogicalTime theTime)
                throws FederateInternalError {

            grantedHlaLogicalTime = theTime;
            timeAdvanceGrant = true;

            // HLA Reporter support.
            if (hlaManager.get_EnableHlaReporter()) {
                double delay = (System.nanoTime()
                        - hlaReporter.getTimeOfTheLastAdvanceRequest())
                        / Math.pow(10, 9);

                // Reset time for last advance request (NER or TAG).
                hlaReporter.setTimeOfTheLastAdvanceRequest(Integer.MIN_VALUE);

                // Compute elapsed time spent between latest TAR or NER and this received TAG.
                hlaReporter._TAGDelay.add(delay);

                // As a new TAG has been received add and set is tick() counter to 0.
                hlaReporter._numberOfTicks.add(0);

                // Increment TAG counter.
                hlaReporter._numberOfTAGs++;
            }

            if (hlaManager.getDebugging()) {
                hlaDebug("  TAG(" + ((CertiLogicalTime1516E)grantedHlaLogicalTime).getTime()
                        + " * (HLA time unit=" + hlaManager.get_HlaTimeUnitValue()
                        + ")) received in INNER callback: timeAdvanceGrant()");
            }
        }

        // HLA Federation Management services (callbacks).
        // Synchronization point services.

        /** Callback delivered by the RTI (CERTI) to notify if the synchronization
         *  point registration has failed.
         */
        @Override
        public void synchronizationPointRegistrationFailed(String synchronizationPointLabel,
                                                           SynchronizationPointFailureReason reason)
                throws FederateInternalError  {
            if (!checkSynchronizationPointNameMatch(synchronizationPointLabel)) {
                return;
            }
            synchronizationFailed = true;
            if (hlaManager.getDebugging()) {
                hlaDebug("INNER callback: synchronizationPointRegistrationFailed(): "
                        + "synchronizationFailed = " + synchronizationFailed
                        + "Reason = " + reason);
            }
        }

        /** Callback delivered by the RTI (CERTI) to notify if the synchronization
         *  point registration has succeed.
         */
        @Override
        public void synchronizationPointRegistrationSucceeded(
                String synchronizationPointLabel) throws FederateInternalError {
            if (!checkSynchronizationPointNameMatch(synchronizationPointLabel)) {
                return;
            }
            synchronizationSuccess = true;
            if (hlaManager.getDebugging()) {
                hlaDebug("INNER callback: synchronizationPointRegistrationSucceeded(): "
                        + "synchronizationSuccess = " + synchronizationSuccess);
            }
        }

        /** Callback delivered by the RTI (CERTI) to notify the announcement of
         *  a synchronization point in the HLA Federation.
         */
        @Override
        public void announceSynchronizationPoint(
                String synchronizationPointLabel, byte[] userSuppliedTag)
                throws FederateInternalError {
            hlaDebug("announce synchronisation point");
            if (!checkSynchronizationPointNameMatch(synchronizationPointLabel)) {
                return;
            }
            inPause = true;
            if (hlaManager.getDebugging()) {
                hlaDebug("INNER callback: announceSynchronizationPoint(): inPause = "
                        + inPause);
            }
        }

        /** Callback delivered by the RTI (CERTI) to notify that the Federate is
         *  synchronized to others Federates using the same synchronization point
         *  in the HLA Federation.
         */
        @Override
        public void federationSynchronized(String synchronizationPointLabel, FederateHandleSet failedToSyncSet)
                throws FederateInternalError {
            inPause = false;
            if (!checkSynchronizationPointNameMatch(synchronizationPointLabel)) {
                return;
            }
            if (hlaManager.getDebugging()) {
                hlaDebug("INNER callback: federationSynchronized(): inPause = "
                        + inPause + "\n");
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /** Configure the deployed HLA updatables.
         *  @param rtia
         *  @exception ObjectClassNotDefined
         *  @exception FederateNotExecutionMember
         *  @exception RTIinternalError
         *  @exception SaveInProgress
         *  @exception RestoreInProgress
         *  @exception ConcurrentAccessAttempted
         *  All those exceptions above are from the HLA/CERTI implementation.
         *  @exception IllegalActionException
         *  All those exceptions above are from Ptolemy.
         */
        private void setupHlaUpdatables(RTIambassador rtia)
                throws ObjectClassNotDefined, FederateNotExecutionMember,
                RTIinternalError, SaveInProgress, RestoreInProgress,
                ConcurrentAccessAttempted, IllegalActionException {

            // For each HlaUpdatable actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to publish.

            // 1. Get classHandle and attributeHandle for each attribute
            //    value to publish. Update the HlaUpdatables
            //    table with the information.
            Iterator<Map.Entry<String, Object[]>> it = hlaAttributesToPublish
                    .entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Object[]> elt = it.next();
                // elt.getKey()   => HlaUpdatable actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // Get corresponding HlaUpdatable actor.
                HlaUpdatable pub = (HlaUpdatable) getPortFromTab(tObj)
                        .getContainer();

                if (hlaManager.getDebugging()) {
                    hlaDebug("_setupHlaUpdatables() - HlaUpdatable: "
                            + pub.getFullName());
                }

                // Object class handle and attribute handle are numerical (int)
                // representation, provided by the RTIA, for object classes
                // and object class attributes that appear in the FED file.

                // Retrieve HLA class handle from RTI.
                int classHandle = Integer.MIN_VALUE;

                try {
                    classHandle = rtia.getObjectClassHandle(pub.getHlaClassName()).hashCode();

                    if (hlaManager.getDebugging()) {
                        hlaDebug("_setupHlaUpdatables() "
                                + "objectClassName (in FOM) = "
                                + pub.getHlaClassName() + " - classHandle = "
                                + classHandle);
                    }
                } catch (NameNotFound e) {
                    throw new IllegalActionException(null, e, "NameNotFound: "
                            + e.getMessage()
                            + " is not a HLA class from the FOM (see .fed file).");
                } catch (NotConnected notConnected) {
                    notConnected.printStackTrace();
                }

                // Retrieve HLA attribute handle from RTI.
                //int attributeHandle = Integer.MIN_VALUE;
                AttributeHandle attributeHandle;
                try {
                    attributeHandle = rtia.getAttributeHandle(new CertiObjectHandle(classHandle), pub.getHlaAttributeName());

                    if (hlaManager.getDebugging()) {
                        hlaDebug("_setupHlaUpdatables() " + " attributeHandle = "
                                + attributeHandle.hashCode());
                    }
                } catch (NameNotFound e) {
                    throw new IllegalActionException(null, e, "NameNotFound: "
                            + e.getMessage()
                            + " is not a HLA attribute value from the FOM (see .fed file).");
                } catch (InvalidObjectClassHandle invalidObjectClassHandle) {
                    throw new IllegalActionException(null, invalidObjectClassHandle, "InvalidObjectClassHandle: "
                            + invalidObjectClassHandle.getMessage());
                } catch (NotConnected notConnected) {
                    throw new IllegalActionException(null, notConnected, "NotConnected: "
                            + notConnected.getMessage());
                }

                // Update HLA attribute information (for publication)
                // from HLA services. In this case, the tObj[] object as
                // the following structure:
                // tObj[0] => input port which receives the token to transform
                //            as an updated value of a HLA attribute,
                // tObj[1] => type of the port (e.g. of the attribute),
                // tObj[2] => object class name of the attribute,
                // tObj[3] => instance class name to which this attribute belongs,
                // tObj[4] => object class handle,
                // tObj[5] => attribute handle.

                // tObj[0 .. 3] are extracted from the Ptolemy model.
                // tObj[3 .. 5] are provided by the RTI (CERTI).

                // FIXME: See FIXME below for a better design.

                // All these information are required to publish/unpublish
                // updated value of a HLA attribute.
                elt.setValue(new Object[] { getPortFromTab(tObj),
                        getTypeFromTab(tObj), getHlaClassNameFromTab(tObj),
                        getHlaInstanceNameFromTab(tObj), classHandle,
                        attributeHandle });
            }

            // 2.1 Create a table of HlaUpdatables indexed by their corresponding
            //     classInstanceName (no duplication).
            HashMap<String, LinkedList<String>> classInstanceNameHlaUpdatableTable = new HashMap<String, LinkedList<String>>();

            Iterator<Map.Entry<String, Object[]>> it21 = hlaAttributesToPublish
                    .entrySet().iterator();

            while (it21.hasNext()) {
                Map.Entry<String, Object[]> elt = it21.next();
                // elt.getKey()   => HlaUpdatable actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // Get corresponding HlaUpdatable actor.
                HlaUpdatable pub = (HlaUpdatable) getPortFromTab(tObj)
                        .getContainer();
                String classInstanceName = pub.getHlaInstanceName();

                if (classInstanceNameHlaUpdatableTable
                        .containsKey(classInstanceName)) {
                    classInstanceNameHlaUpdatableTable.get(classInstanceName)
                            .add(elt.getKey());
                } else {
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(elt.getKey());
                    classInstanceNameHlaUpdatableTable.put(classInstanceName,
                            list);
                }
            }

            // 2.2 Create a table of HlaUpdatables indexed by their corresponding
            //     class handle (no duplication).
            HashMap<ObjectClassHandle, LinkedList<String>> classHandleHlaPublisherTable = new HashMap<ObjectClassHandle, LinkedList<String>>();

            Iterator<Map.Entry<String, Object[]>> it22 = hlaAttributesToPublish
                    .entrySet().iterator();

            while (it22.hasNext()) {
                Map.Entry<String, Object[]> elt = it22.next();
                // elt.getKey()   => HlaUpdatable actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                ObjectClassHandle classHandle = (getClassHandleFromTab(tObj));

                if (classHandleHlaPublisherTable.containsKey(classHandle)) {
                    classHandleHlaPublisherTable.get(classHandle)
                            .add(elt.getKey());
                } else {
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(elt.getKey());
                    classHandleHlaPublisherTable.put(classHandle, list);
                }
            }

            // 3. Declare to the Federation the HLA attributes to publish. If
            //    these attributes belongs to the same object class then only
            //    one publishObjectClass() call is performed.
            Iterator<Map.Entry<ObjectClassHandle, LinkedList<String>>> it3 = classHandleHlaPublisherTable
                    .entrySet().iterator();

            while (it3.hasNext()) {
                Map.Entry<ObjectClassHandle, LinkedList<String>> elt = it3.next();
                // elt.getKey()   => HLA class instance name.
                // elt.getValue() => list of HlaUpdatable actor full names.
                LinkedList<String> hlaUpdatableFullnames = elt.getValue();

                // The attribute handle set to declare all attributes to publish
                // for one object class.
                AttributeHandleSet attributesLocal = new CertiAttributeHandleSet(); //factory.createAttributeHandleSet();

                // Fill the attribute handle set with all attribute to publish.
                for (String sPub : hlaUpdatableFullnames) {
                    attributesLocal.add(getAttributeHandleFromTab(hlaAttributesToPublish.get(sPub)));
                }

                // At this point, all HlaUpdatables have been initialized and own their
                // corresponding HLA class handle and HLA attribute handle. Just retrieve
                // the first from the list to get those information.
                Object[] tObj = hlaAttributesToPublish
                        .get(hlaUpdatableFullnames.getFirst());
                ObjectClassHandle classHandle = (getClassHandleFromTab(tObj));

                // Declare to the Federation the HLA attribute(s) to publish.
                try {
                    rtia.publishObjectClassAttributes(classHandle, attributesLocal);

                    if (hlaManager.getDebugging()) {
                        hlaDebug("setupHlaUpdatables() - Publish Object Class: "
                                + " classHandle = " + classHandle.hashCode()
                                + " attributesLocal = "
                                + attributesLocal.toString());
                    }
                } catch (AttributeNotDefined e) {
                    throw new IllegalActionException(null, e,
                            "AttributeNotDefined: " + e.getMessage());
                } catch (NotConnected notConnected) {
                    notConnected.printStackTrace();
                }
            }

            // 4. Register object instances. Only one registerObjectInstance() call is performed
            //    by class instance (name). Finally, update the hash map of class instance name
            //    with the returned object instance handle.
            Iterator<Map.Entry<String, LinkedList<String>>> it4 = classInstanceNameHlaUpdatableTable
                    .entrySet().iterator();

            while (it4.hasNext()) {
                Map.Entry<String, LinkedList<String>> elt = it4.next();
                // elt.getKey()   => HLA class instance name.
                // elt.getValue() => list of HlaUpdatable actor full names.
                LinkedList<String> hlaUpdatableFullnames = elt.getValue();

                // At this point, all HlaUpdatables on the list have been initialized
                // and own their class handle and class instance name. Just retrieve
                // the first from the list to get those information for registering
                // the instance.
                Object[] tObj = hlaAttributesToPublish
                        .get(hlaUpdatableFullnames.getFirst());

                ObjectClassHandle classHandle = (getClassHandleFromTab(tObj));
                String classInstanceName = getHlaInstanceNameFromTab(tObj);

                if (!registerObjectInstanceMap
                        .containsKey(classInstanceName)) {
                    ObjectInstanceHandle instanceHandle;
                    try {
                        instanceHandle = rtia.registerObjectInstance(classHandle, classInstanceName);

                        if (hlaManager.getDebugging()) {
                            hlaDebug("setupHlaUpdatables() - Register Object Instance: "
                                    + " classHandle = " + classHandle.hashCode()
                                    + " classIntanceName = " + classInstanceName
                                    + " instanceHandle = "
                                    + instanceHandle);
                        }

                        registerObjectInstanceMap.put(classInstanceName,
                                instanceHandle.hashCode());
                    } catch (ObjectClassNotPublished e) {
                        throw new IllegalActionException(null, e,
                                "ObjectClassNotPublished: " + e.getMessage());
                    } catch (ObjectInstanceNameInUse objectInstanceNameInUse) {
                        objectInstanceNameInUse.printStackTrace();
                    } catch (ObjectInstanceNameNotReserved objectInstanceNameNotReserved) {
                        objectInstanceNameNotReserved.printStackTrace();
                    } catch (NotConnected notConnected) {
                        notConnected.printStackTrace();
                    }
                } // end if (!registerObjectInstanceMap)
            }
        }


        /** Configure the deployed HLA Reflectable actors.
         *  @param rtia
         *  @exception ObjectClassNotDefined
         *  @exception FederateNotExecutionMember
         *  @exception RTIinternalError
         *  @exception SaveInProgress
         *  @exception RestoreInProgress
         *  @exception ConcurrentAccessAttempted
         *  All those exceptions above are from the HLA/CERTI implementation.
         *  @exception IllegalActionException
         *  All those exceptions above are from Ptolemy.         */
        private void setupHlaReflectables(RTIambassador rtia)
                throws ObjectClassNotDefined, FederateNotExecutionMember,
                RTIinternalError, SaveInProgress, RestoreInProgress,
                ConcurrentAccessAttempted, IllegalActionException {
            // XXX: FIXME: check mixing between tObj[] and HlaSubcriber getter/setter.

            // For each HlaReflectable actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to subscribe to.

            // 1. Get classHandle and attributeHandle for each attribute
            // value to subscribe. Update the HlaReflectables.
            Iterator<Map.Entry<String, Object[]>> it1 = hlaAttributesToSubscribeTo
                    .entrySet().iterator();

            while (it1.hasNext()) {
                Map.Entry<String, Object[]> elt = it1.next();
                // elt.getKey()   => HlaReflectable actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // Get corresponding HlaReflectable actor.
                HlaReflectable sub = (HlaReflectable) ((TypedIOPort) tObj[0])
                        .getContainer();

                if (hlaManager.getDebugging()) {
                    hlaDebug("setupHlaReflectables() - HlaReflectable: "
                            + sub.getFullName());
                }

                // Object class handle and attribute handle are numerical (int)
                // representation, provided by the RTIA, for object classes
                // and object class attributes that appear in the FED file.

                // Retrieve HLA class handle from RTI.
                ObjectClassHandle classHandle = null; // = Integer.MIN_VALUE;

                try {
                    classHandle = rtia.getObjectClassHandle(
                            getHlaClassNameFromTab(tObj));

                    if (hlaManager.getDebugging()) {
                        hlaDebug("setupHlaReflectables() "
                                + "objectClassName (in FOM) = "
                                + getHlaClassNameFromTab(tObj)
                                + " - classHandle = " + classHandle.hashCode());
                    }
                } catch (NameNotFound e) {
                    throw new IllegalActionException(null, e, "NameNotFound: "
                            + e.getMessage()
                            + " is not a HLA class from the FOM (see .fed file).");
                } catch (NotConnected notConnected) {
                    notConnected.printStackTrace();
                }

                // Retrieve HLA attribute handle from RTI.
                AttributeHandle attributeHandle = null; // = Integer.MIN_VALUE;
                try {
                    attributeHandle = rtia.getAttributeHandle(
                            classHandle, sub.getHlaAttributeName());

                    if (hlaManager.getDebugging()) {
                        hlaDebug("setupHlaReflectables() " + " attributeHandle = "
                                + attributeHandle.hashCode());
                    }
                } catch (NameNotFound e) {
                    throw new IllegalActionException(null, e, "NameNotFound: "
                            + e.getMessage()
                            + " is not a HLA attribute value from the FOM (see .fed file).");
                } catch (InvalidObjectClassHandle invalidObjectClassHandle) {
                    invalidObjectClassHandle.printStackTrace();
                } catch (NotConnected notConnected) {
                    notConnected.printStackTrace();
                }

                // Subscribe to HLA attribute information (for subscription)
                // from HLA services. In this case, the tObj[] object has
                // the following structure:
                // tObj[0] => output port: produces an output even whenever the
                //            the attribute of the instance is updated by a
                //            another federation in the federation,
                // tObj[1] => type of the port; it must be equal to the data type of the attribute,
                // tObj[2] => object class name,
                // tObj[3] => instance class name,
                // tObj[4] => object class handle,
                // tObj[5] => attribute handle

                // tObj[0 .. 3] are extracted from the Ptolemy model.
                // tObj[3 .. 5] are provided by the RTI (CERTI).

                // All these information are required to subscribe/unsubscribe
                // HLA attributes.
                elt.setValue(new Object[] { getPortFromTab(tObj),
                        getTypeFromTab(tObj), getHlaClassNameFromTab(tObj),
                        getHlaInstanceNameFromTab(tObj), classHandle,
                        attributeHandle });

                sub.setClassHandle(classHandle.hashCode());
                sub.setAttributeHandle(attributeHandle.hashCode());
            }

            // 2. Create a table of HlaReflectables indexed by their corresponding
            //    class handle (no duplication).
            HashMap<ObjectClassHandle, LinkedList<String>> classHandleHlaReflectableTable = null;
            classHandleHlaReflectableTable = new HashMap<ObjectClassHandle, LinkedList<String>>();

            Iterator<Map.Entry<String, Object[]>> it22 = hlaAttributesToSubscribeTo
                    .entrySet().iterator();

            while (it22.hasNext()) {
                Map.Entry<String, Object[]> elt = it22.next();
                // elt.getKey()   => HlaReflectable actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // The handle of the class to which the HLA attribute belongs to.
                ObjectClassHandle classHandle = (getClassHandleFromTab(tObj));

                if (classHandleHlaReflectableTable.containsKey(classHandle)) {
                    classHandleHlaReflectableTable.get(classHandle)
                            .add(elt.getKey());
                } else {
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(elt.getKey());
                    classHandleHlaReflectableTable.put(classHandle, list);
                }
            }

            // 3. Declare to the Federation the HLA attributes to subscribe to.
            // If these attributes belongs to the same object class then only
            // one subscribeObjectClass() call is performed.
            Iterator<Map.Entry<ObjectClassHandle, LinkedList<String>>> it3 = classHandleHlaReflectableTable
                    .entrySet().iterator();

            while (it3.hasNext()) {
                Map.Entry<ObjectClassHandle, LinkedList<String>> elt = it3.next();
                // elt.getKey()   => HLA class instance name.
                // elt.getValue() => list of HlaReflectable actor full names.
                LinkedList<String> hlaReflectableFullnames = elt.getValue();

                // The attribute handle set to declare all subscribed attributes
                // for one object class.
                factory = RtiFactoryFactory.getRtiFactory();
                AttributeHandleSet attributesLocal = new CertiAttributeHandleSet();//factory.createAttributeHandleSet();

                for (String sSub : hlaReflectableFullnames) {
                        attributesLocal.add(getAttributeHandleFromTab(hlaAttributesToSubscribeTo.get(sSub)));
                }

                // At this point, all HlaReflectable actors have been initialized and own their
                // corresponding HLA class handle and HLA attribute handle. Just retrieve
                // the first from the list to get those information.
                Object[] tObj = hlaAttributesToSubscribeTo
                        .get(hlaReflectableFullnames.getFirst());
                ObjectClassHandle classHandle = (getClassHandleFromTab(tObj));
                try {
                    rtia.subscribeObjectClassAttributes(classHandle, attributesLocal);
                } catch (AttributeNotDefined e) {
                    throw new IllegalActionException(null, e,
                            "AttributeNotDefined: " + e.getMessage());
                } catch (NotConnected notConnected) {
                    notConnected.printStackTrace();
                }

                if (hlaManager.getDebugging()) {
                    String attributesLocalToString = "[";
                    for(AttributeHandle attributeHandle : attributesLocal){
                        attributesLocalToString += attributeHandle.hashCode() + ",";
                    }
                    attributesLocalToString+= "]";
                    hlaDebug("setupHlaReflectables() - Subscribe Object Class Attributes: "
                            + " classHandle = " + classHandle.hashCode()
                            + " attributesLocal = "
                            + attributesLocalToString);
                }
            }
        } // end 'private void setupHlaReflectables(RTIambassador rtia) ...'
    } // end 'private class PtolemyFederateAmbassadorInner extends NullFederateAmbassador { ...'
}