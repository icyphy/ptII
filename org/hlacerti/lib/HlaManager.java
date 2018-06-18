/* This attribute implements a HLA Manager to cooperate with a HLA/CERTI Federation.

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import certi.rti.impl.CertiLogicalTime;
import certi.rti.impl.CertiLogicalTimeInterval;
import certi.rti.impl.CertiRtiAmbassador;
import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotKnown;
import hla.rti.AttributeNotOwned;
import hla.rti.ConcurrentAccessAttempted;
import hla.rti.CouldNotDiscover;
import hla.rti.CouldNotOpenFED;
import hla.rti.EnableTimeConstrainedPending;
import hla.rti.EnableTimeConstrainedWasNotPending;
import hla.rti.EnableTimeRegulationPending;
import hla.rti.EnableTimeRegulationWasNotPending;
import hla.rti.ErrorReadingFED;
import hla.rti.EventRetractionHandle;
import hla.rti.FederateAmbassador;
import hla.rti.FederateInternalError;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederateOwnsAttributes;
import hla.rti.FederatesCurrentlyJoined;
import hla.rti.FederationExecutionAlreadyExists;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.FederationTimeAlreadyPassed;
import hla.rti.InvalidFederationTime;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;
import hla.rti.NameNotFound;
import hla.rti.ObjectAlreadyRegistered;
import hla.rti.ObjectClassNotDefined;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectClassNotPublished;
import hla.rti.ObjectNotKnown;
import hla.rti.OwnershipAcquisitionPending;
import hla.rti.RTIambassador;
import hla.rti.RTIexception;
import hla.rti.RTIinternalError;
import hla.rti.ReflectedAttributes;
import hla.rti.ResignAction;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SpecifiedSaveLabelDoesNotExist;
import hla.rti.SuppliedAttributes;
import hla.rti.TimeAdvanceAlreadyInProgress;
import hla.rti.TimeAdvanceWasNotInProgress;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RtiFactory;
import hla.rti.jlc.RtiFactoryFactory;
import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TimeRegulator;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HlaManager

/**
 * This class implements a HLA Manager which allows a Ptolemy model to
 * cooperate with a HLA/CERTI Federation. The main goal is to allow a Ptolemy
 * simulation as Federate of a Federation.
 *
 * <p>The High Level Architecture (HLA) [1][2] is a standard for distributed
 * discrete-event simulation. A complex simulation in HLA is called a HLA
 * Federation. A Federation is a collection of Federates (e.g. simpler simulators),
 * each performing a sequence of computations, interconnected by a Run
 * Time Infrastructure (RTI).</p>
 *
 * <p>CERTI is an Open-Source middleware RTI compliant with HLA [NRS09] which
 * manages every part of federation. It also ensures a real-time behavior of
 * a federation execution. CERTI is implemented in C++ and bindings are
 * provided as JCERTI for Java and PyHLA for Python. For more information see:
 * <a href="http://savannah.nongnu.org/projects/certi" target="_top">http://savannah.nongnu.org/projects/certi</a></p>
 *
 * <p>The {@link HlaManager} attribute handles the time synchronization between
 * Ptolemy model time and HLA logical time by implementing the {@link TimeRegulator}
 * interface. It also manages objects that implement interfaces provided by
 * JCERTI relatives to Federation, Declaration, Object and Time management
 * areas in HLA (each management areas provides a set of services).
 * </p>
 * To develop a HLA Federation it is required to specify a Federate Object
 * Model (FOM) which describes the architecture of the Federation (HLA version,
 * name of Federates which belong to, shared HLA attributes) and the interaction
 * between Federates and shared attributes. Data exchanged in a HLA Federation
 * are called HLA attributes and their interaction mechanism is based on the
 * publish/subscribe paradigm. The FOM is specified in a .fed file used by
 * the RTI (e.g. by the RTIG process when using CERTI). More information in [3].
 * <p> <a href="http://savannah.nongnu.org/projects/certi" target="_top">http://savannah.nongnu.org/projects/certi</a></p>
 * <p> To enable a Ptolemy model as a Federate, the {@link HlaManager} has to be
 * deployed and configured (by double-clicking on the attribute).
 * Parameters <i>federateName</i>, <i>federationName</i> have to match the
 * declaration in the FOM (.fed file). <i>fedFile</i> specifies the FOM file and
 * its path.</p>
 *
 * <p>Parameters <i>useNextEventRequest</i>, <i>UseTimeAdvanceRequest</i>,
 * <i>isTimeConstrained</i> and <i>isTimeRegulator</i> are
 * used to configure the HLA time management services of the Federate. A
 * Federate can only specify the use of the <i>nextEventRequest()
 * service</i> or the <i>timeAdvanceRequest()</i> service at a time.
 * <i>istimeConstrained</i> is used to specify time-constrained Federate and
 * <i>istimeRegulator</i> to specify time-regulator Federate. The combination of
 * both parameters is possible and is recommended.</p>
 *
 * <p>Parameters, <i>hlaStepTime</i> and <i>hlaLookAHead</i>
 * are used to specify Hla Timing attributes of a Federate.</p>
 *
 * <p>Parameters <i>requireSynchronization</i>, <i>synchronizationPointName</i>
 * and <i>isCreatorSyncPt</i> are used to configure HLA synchronization point.
 * This mechanism is usually used to synchronize the Federates, during their
 * initialization, to avoid that Federates that only consume some HLA
 * attributes finished their simulation before the other federates have started.
 * <i>isCreatorSyncPt</i> indicates if the Federate is the creator of the
 * synchronization. Only one Federate can create the named synchronization
 * point for the whole HLA Federation.</p>
 *
 * <p>{@link HlaPublisher} and {@link HlaSubscriber} actors are used to
 * respectively publish and subscribe to HLA attributes. The name of those
 * actors and their <i>classObjectHandle</i> parameter have to match the
 * identifier of the shared HLA attributes and their object class that they
 * belong to, specified in the FOM (.fed file).</p>
 *
 * <p>For a correct execution, the <i>CERTI_HOME</i> environment variable has to
 * be set. It could be set in the shell (by running one of the scripts provided
 * by CERTI) where Vergil is executed, or as a parameter of the Ptolemy model
 * or as a parameter of the {@link HlaManager}:</p>
 * <pre>
 * CERTI_HOME="/absolute/path/to/certi/"
 * </pre>
 *
 * <p>Otherwise, the current implementation is not able to find the CERTI
 * environment, the RTIG binary and to perform its execution. See also
 * the {@link CertiRtig} class.</p>
 *
 * <p>NOTE: For a correct behavior CERTI has to be compiled with the option
 * "CERTI_USE_NULL_PRIME_MESSAGE_PROTOCOL"
 * </p>
 *
 * <p><b>References</b>:</p>
 *
 * <p>[1] Dpt. of Defense (DoD) Specifications, "High Level Architecture Interface
 *     Specification, Version 1.3", DOD/DMSO HLA IF 1.3, Tech. Rep., Apr 1998.</p>
 * <p>[2] IEEE, "IEEE standard for modeling and simulation High Level Architecture
 *     (HLA)", IEEE Std 1516-2010, vol. 18, pp. 1-38, 2010.</p>
 * <p>[3] D. of Defense (DoD) Specifications, "High Level Architecture Object Model
 *     Template, Version 1.3", DOD/DMSO OMT 1.3, Tech. Rep., Feb 1998.</p>
 * <p>[4] E. Noulard, J.-Y. Rousselot, and P. Siron, "CERTI, an open source RTI,
 *     why and how ?", Spring Simulation Interoperability Workshop, pp. 23-27,
 *     Mar 2009.</p>
 * <p>[5] Y. Li, J. Cardoso, and P. Siron, "A distributed Simulation Environment for
 *     Cyber-Physical Systems", Sept 2015.</p>
 * <p>[6] J. Cardoso, and P. Siron, "Ptolemy-HLA: a Cyber-Physical System Distributed
 *     Simulation Framework", Festschrift Lee, Internal DISC report, 2017. </p>
 *
 *
 *  @author Gilles Lasnier, Contributors: Patricia Derler, Edward A. Lee, David Come, Yanxuan LI
 *  @version $Id: HlaManager.java 214 2018-04-01 13:32:02Z j.cardoso $
 *  @since Ptolemy II 11.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaManager extends AbstractInitializableAttribute
        implements TimeRegulator {

    /** Construct a HlaManager with a name and a container. The container
     *  argument must not be null, or a NullPointerException will be thrown.
     *  This actor will use the workspace of the container for synchronization
     *  and version counts. If the name argument is null, then the name is set
     *  to the empty string. Increment the version of the workspace.
     *  @param container Container of this attribute.
     *  @param name Name of this attribute.
     *  @exception IllegalActionException If the container is incompatible
     *  with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *  an actor already in the container.
     */
    public HlaManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _registerObjectInstanceMap = new HashMap<String, Integer>();
        _discoverObjectInstanceMap = new HashMap<Integer, String>();

        _rtia = null;
        _certiRtig = null;
        _federateAmbassador = null;

        _hlaAttributesToPublish = new HashMap<String, Object[]>();
        _hlaAttributesToSubscribeTo = new HashMap<String, Object[]>();
        _fromFederationEvents = new HashMap<String, LinkedList<TimedEvent>>();
        _objectIdToClassHandle = new HashMap<Integer, Integer>();

        // Joker wildcard support.
        _usedJokerFilterMap = new HashMap<String, Boolean>();

        _hlaTimeStep = null;
        _hlaLookAHead = null;

        // HLA Federation management parameters.
        federateName = new Parameter(this, "federateName");
        federateName.setDisplayName("Federate's name");
        federateName.setTypeEquals(BaseType.STRING);
        federateName.setExpression("\"HlaManager\"");
        attributeChanged(federateName);

        federationName = new Parameter(this, "federationName");
        federationName.setDisplayName("Federation's name");
        federationName.setTypeEquals(BaseType.STRING);
        federationName.setExpression("\"HLAFederation\"");
        attributeChanged(federationName);

        fedFile = new FileParameter(this, "fedFile");
        fedFile.setDisplayName("Federate Object Model (.fed) file path");
        new Parameter(fedFile, "allowFiles", BooleanToken.TRUE);
        new Parameter(fedFile, "allowDirectories", BooleanToken.FALSE);
        fedFile.setExpression("$CWD/HLAFederation.fed");

        // HLA Time management parameters.
        timeManagementService = new ChoiceParameter(this,
                "timeManagementService", ETimeManagementService.class);
        timeManagementService.setDisplayName("Time Management Service");
        attributeChanged(timeManagementService);

        hlaTimeStep = new Parameter(this, "hlaTimeStep");
        hlaTimeStep.setDisplayName("*** If TAR is used, time step (s)");
        hlaTimeStep.setExpression("0.0");
        hlaTimeStep.setTypeEquals(BaseType.DOUBLE);
        attributeChanged(hlaTimeStep);

        isTimeConstrained = new Parameter(this, "isTimeConstrained");
        isTimeConstrained.setTypeEquals(BaseType.BOOLEAN);
        isTimeConstrained.setExpression("true");
        isTimeConstrained.setDisplayName("isTimeConstrained ?");
        isTimeConstrained.setVisibility(Settable.NOT_EDITABLE);
        attributeChanged(isTimeConstrained);

        isTimeRegulator = new Parameter(this, "isTimeRegulator");
        isTimeRegulator.setTypeEquals(BaseType.BOOLEAN);
        isTimeRegulator.setExpression("true");
        isTimeRegulator.setDisplayName("isTimeRegulator ?");
        isTimeRegulator.setVisibility(Settable.NOT_EDITABLE);
        attributeChanged(isTimeRegulator);

        hlaLookAHead = new Parameter(this, "hlaLookAHead");
        hlaLookAHead.setDisplayName("lookahead (s)");
        hlaLookAHead.setExpression("0.1");
        hlaLookAHead.setTypeEquals(BaseType.DOUBLE);
        attributeChanged(hlaLookAHead);

        // HLA Synchronization parameters.
        requireSynchronization = new Parameter(this, "requireSynchronization");
        requireSynchronization.setTypeEquals(BaseType.BOOLEAN);
        requireSynchronization.setExpression("true");
        requireSynchronization.setDisplayName("Require synchronization ?");
        attributeChanged(requireSynchronization);

        synchronizationPointName = new Parameter(this,
                "synchronizationPointName");
        synchronizationPointName.setDisplayName("Synchronization point name");
        synchronizationPointName.setTypeEquals(BaseType.STRING);
        synchronizationPointName.setExpression("\"Simulating\"");
        attributeChanged(synchronizationPointName);

        isCreator = new Parameter(this, "isCreator");
        isCreator.setTypeEquals(BaseType.BOOLEAN);
        isCreator.setExpression("false");
        isCreator.setDisplayName("Is synchronization point register ?");
        attributeChanged(isCreator);

        hlaTimeUnit = new Parameter(this, "hlaTimeUnit");
        hlaTimeUnit.setTypeEquals(BaseType.DOUBLE);
        hlaTimeUnit.setExpression("1.0");
        hlaTimeUnit.setDisplayName("HLA time unit");
        attributeChanged(hlaTimeUnit);

        // HLA Reporter support.
        enableHlaReporter = new Parameter(this, "enableHlaReporter");
        enableHlaReporter.setTypeEquals(BaseType.BOOLEAN);
        enableHlaReporter.setExpression("false");
        enableHlaReporter.setDisplayName("Generate HLA reports ?");
        attributeChanged(enableHlaReporter);

        hlaReportPath = new FileParameter(this, "hlaReportPath");
        hlaReportPath.setDisplayName("HLA report folder path");
        new Parameter(hlaReportPath, "allowFiles", BooleanToken.FALSE);
        new Parameter(hlaReportPath, "allowDirectories", BooleanToken.TRUE);
        hlaReportPath.setExpression("testsResults");

        // Local or distant simulation support.
        _certiHost = null;
        _distantSimulation = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Name of the Ptolemy Federate. This parameter must contain an
     *  StringToken. */
    public Parameter federateName;

    /** Name of the federation. This parameter must contain an StringToken. */
    public Parameter federationName;

    /** Path and name of the Federate Object Model (FOM) file. This parameter
     *  must contain an StringToken. */
    public FileParameter fedFile;

    /**
     * Double value for representing how much is a unit of time in the simulation.
     * Has an impact on TAR/NER/RAV/UAV.
     */
    public Parameter hlaTimeUnit;

    /** Boolean value, 'true' if the Federate is the creator of the
     *  synchronization point 'false' if not. This parameter must contain
     *  an BooleanToken. */
    public Parameter isCreator;

    /** Boolean value, 'true' if the Federate is declared time constrained
     *  'false' if not. This parameter must contain an BooleanToken. */
    public Parameter isTimeConstrained;

    /** Boolean value, 'true' if the Federate is declared time regulator
     *  'false' if not. This parameter must contain an BooleanToken. */
    public Parameter isTimeRegulator;

    /** Value of the time step of the Federate. This parameter must contain
     *  an DoubleToken. */
    public Parameter hlaTimeStep;

    /** Value of the lookahead of the HLA ptII federate. This parameter
     *  must contain an DoubleToken. */
    public Parameter hlaLookAHead;

    /** Boolean value, 'true' if the Federate is synchronised with other
     *  Federates using a HLA synchronization point, 'false' if not. This
     *  parameter must contain an BooleanToken. */
    public Parameter requireSynchronization;

    /** Name of the synchronization point (if required). This parameter must
     *  contain an StringToken. */
    public Parameter synchronizationPointName;

    /** Choice of time advancement service (NER or exclusive TAR). */
    public ChoiceParameter timeManagementService;

    /** Enumeration which represents both time advancement services NER or TAR. */
    public enum ETimeManagementService {
        /** The Federate uses the Next Event Request (NER) calls to advance time. */
        NextEventRequest,

        /** The Federate uses the Time Advance Request (TAR) calls to advance in time. */
        TimeAdvancementRequest;

        /** Override the toString of enum class.
         *  @return The string associated for each enum value. */
        @Override
        public String toString() {
            switch (this) {
            case NextEventRequest:
                return "Next Event Request (NER)";
            case TimeAdvancementRequest:
                return "Time Advancement Request (TAR)";
            default:
                throw new IllegalArgumentException();
            }
        }
    };

    /** Boolean value, 'true' if the generation of HLA reports is enabled
     *  'false' if not. This parameter must contain an BooleanToken. */
    public Parameter enableHlaReporter;

    /** Path and name of the HLA folder to store reports. This parameter
     *  must contain a String. */
    public FileParameter hlaReportPath;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Checks constraints on the changed attribute (when it is required) and
     *  associates his value to its corresponding local variables.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the attribute is empty or negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == federateName) {
            String value = ((StringToken) federateName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
            _federateName = value;
        } else if (attribute == federationName) {
            String value = ((StringToken) federationName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
            _federationName = value;
        } else if (attribute == timeManagementService) {
            if (timeManagementService
                    .getChosenValue() == ETimeManagementService.NextEventRequest) {
                _timeStepped = false;
                _eventBased = true;
            } else if (timeManagementService
                    .getChosenValue() == ETimeManagementService.TimeAdvancementRequest) {
                _timeStepped = true;
                _eventBased = false;
            }
        } else if (attribute == isTimeConstrained) {
            _isTimeConstrained = ((BooleanToken) isTimeConstrained.getToken())
                    .booleanValue();

        } else if (attribute == isTimeRegulator) {
            _isTimeRegulator = ((BooleanToken) isTimeRegulator.getToken())
                    .booleanValue();

        } else if (attribute == hlaTimeStep) {
            Double value = ((DoubleToken) hlaTimeStep.getToken()).doubleValue();
            if (value < 0) {
                throw new IllegalActionException(this,
                        "Cannot have negative value !");
            }
            _hlaTimeStep = value;
        } else if (attribute == hlaLookAHead) {
            Double value = ((DoubleToken) hlaLookAHead.getToken())
                    .doubleValue();
            if (value < 0) {
                throw new IllegalActionException(this,
                        "Cannot have negative value !");
            }
            _hlaLookAHead = value;
        } else if (attribute == requireSynchronization) {
            _requireSynchronization = ((BooleanToken) requireSynchronization
                    .getToken()).booleanValue();

        } else if (attribute == synchronizationPointName) {
            String value = ((StringToken) synchronizationPointName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
            _synchronizationPointName = value;
        } else if (attribute == isCreator) {
            _isCreator = ((BooleanToken) isCreator.getToken()).booleanValue();

        } else if (attribute == hlaTimeUnit) {
            _hlaTimeUnitValue = ((DoubleToken) hlaTimeUnit.getToken())
                    .doubleValue();
        } else if (attribute == enableHlaReporter) {
            _enableHlaReporter = ((BooleanToken) enableHlaReporter.getToken())
                    .booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HlaManager newObject = (HlaManager) super.clone(workspace);

        newObject._hlaAttributesToPublish = new HashMap<String, Object[]>();
        newObject._hlaAttributesToSubscribeTo = new HashMap<String, Object[]>();

        newObject._rtia = null;

        newObject._federateAmbassador = null;
        newObject._federateName = _federateName;
        newObject._federationName = _federationName;

        newObject._isTimeConstrained = _isTimeConstrained;
        newObject._isTimeRegulator = _isTimeRegulator;

        newObject._requireSynchronization = _requireSynchronization;
        newObject._synchronizationPointName = _synchronizationPointName;
        newObject._isCreator = _isCreator;

        newObject._eventBased = _eventBased;
        newObject._timeStepped = _timeStepped;

        newObject._fromFederationEvents = new HashMap<String, LinkedList<TimedEvent>>();

        newObject._objectIdToClassHandle = new HashMap<Integer, Integer>();
        newObject._registerObjectInstanceMap = new HashMap<String, Integer>();
        newObject._discoverObjectInstanceMap = new HashMap<Integer, String>();

        newObject._hlaTimeUnitValue = _hlaTimeUnitValue;

        newObject._usedJokerFilterMap = new HashMap<String, Boolean>();
        newObject._usedJoker = false;

        newObject._hlaReporter = null;
        newObject._enableHlaReporter = _enableHlaReporter;

        try {
            newObject._hlaTimeStep = ((DoubleToken) hlaTimeStep.getToken())
                    .doubleValue();
            newObject._hlaLookAHead = ((DoubleToken) hlaLookAHead.getToken())
                    .doubleValue();
        } catch (IllegalActionException ex) {
            throw new CloneNotSupportedException("Failed to get a token.");
        }

        // Local or distant simulation support.
        newObject._certiHost = _certiHost;
        newObject._distantSimulation = _distantSimulation;

        return newObject;
    }

    /** Initializes the {@link HlaManager} attribute. This method: calls the
     *  _populateHlaAttributeTables() to initialize HLA attributes to publish
     *  or subscribe to; instantiates and initializes the {@link RTIambassador}
     *  and {@link FederateAmbassador} which handle the communication
     *  Federate &lt;-&gt; RTIA &lt;-&gt; RTIG. RTIA and RTIG are both external communicant
     *  processes (see JCERTI); create the HLA/CERTI Federation (if not exists);
     *  allows the Federate to join the Federation; set the Federate time
     *  management policies (regulator and/or contrained); creates a
     *  synchronisation point (if required); and synchronizes the Federate with
     *  a synchronization point (if declared).
     *  @exception IllegalActionException If the container of the class is not
     *  an Actor or If a CERTI exception is raised and has to be displayed to
     *  the user.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        NamedObj container = getContainer();
        if (!(container instanceof Actor)) {
            throw new IllegalActionException(this,
                    "HlaManager has to be contained by an Actor");
        }

        // Get the corresponding director associated to the HlaManager attribute.
        _director = (DEDirector) ((CompositeActor) this.getContainer())
                .getDirector();

        // Initialize HLA attribute tables for publication/subscription.
        _populateHlaAttributeTables();

        // HLA Reporter support.
        if (_enableHlaReporter) {
            // Get model filename.
            String modelName = _director.getFullName().substring(1,
                    _director.getFullName().lastIndexOf('.'));
            try {
                // Directory to store reports is created at the root folder of the user.
                _hlaReporter = new HlaReporter(hlaReportPath.getValueAsString(),
                        _federateName, _federationName, modelName);
            } catch (IOException e) {
                throw new IllegalActionException(this, e,
                        "HLA reporter: Failed to create folder or files: "
                                + e.getMessage());
            }

            _hlaReporter.initializeReportVariables(_hlaLookAHead, _hlaTimeStep,
                    _hlaTimeUnitValue, _startTime, _director.getModelStopTime(),
                    _federateName, fedFile.asFile().getPath(), _isCreator,
                    _timeStepped, _eventBased);

            _hlaReporter.initializeAttributesToPublishVariables(
                    _hlaAttributesToPublish);
        }

        // Get a link to the RTI.
        RtiFactory factory = null;
        try {
            factory = RtiFactoryFactory.getRtiFactory();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, e,
                    "RTIinternalError: " + e.getMessage());
        }

        try {
            _rtia = (CertiRtiAmbassador) factory.createRtiAmbassador();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, e, "RTIinternalError. "
                    + "If the error is \"Connection to RTIA failed\", "
                    + "then the problem is likely that the rtig "
                    + "binary could not be started by CertiRtig. "
                    + "One way to debug this is to set the various "
                    + "environment variables by sourcing certi/share/scripts/myCERTI_env.sh, "
                    + "then invoking rtig on the .fed file "
                    + "then (re)running the model.");
        }

        // Create the Federation or raise a warning it the Federation already exits.
        try {
            // Local or distant simulation support.
            // Note: determine if we need to change JCERTI API to handle FED file name
            //       instead of URL.
            _rtia.createFederationExecution(_federationName,
                    fedFile.asFile().toURI().toURL());

            _hlaDebugSys("createFederationExecution: FED file URL="
                    + fedFile.asFile().toURI().toURL());

        } catch (FederationExecutionAlreadyExists e) {
            if (_debugging) {
                _hlaDebug("initialize() - WARNING: FederationExecutionAlreadyExists");
            }

        } catch (CouldNotOpenFED e) {
            // XXX: FIXME: only for debug purpose
            try {
                _hlaDebugSys("createFederationExecution: CouldNotOpenFED exception: FED file URL="
                        + fedFile.asFile().toURI().toURL());
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }

            throw new IllegalActionException(this, e,
                    "CouldNotOpenFED: " + e.getMessage());
        } catch (ErrorReadingFED e) {
            throw new IllegalActionException(this, e,
                    "ErrorReadingFED: " + e.getMessage());
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, e,
                    "RTIinternalError: " + e.getMessage());
        } catch (ConcurrentAccessAttempted e) {
            throw new IllegalActionException(this, e,
                    "ConcurrentAccessAttempted: " + e.getMessage());
        } catch (MalformedURLException e) {
            throw new IllegalActionException(this, e,
                    "MalformedURLException: " + e.getMessage());
        }

        _federateAmbassador = new PtolemyFederateAmbassadorInner();

        // Join the Federation.
        try {
            _rtia.joinFederationExecution(_federateName, _federationName,
                    _federateAmbassador);

            if (_debugging) {
                _hlaDebug("initialize() - federation joined");
            }
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e,
                    "RTIexception: " + e.getMessage());
        }

        // Initialize the Federate Ambassador.
        try {
            _federateAmbassador.initialize(_rtia);
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e,
                    "RTIexception: " + e.getMessage());
        }

        // Initialize HLA time management aspects for a Federate
        // (constrained by and/or participate to the time management).
        _initializeTimeAspects();

        // Set initial synchronization point.
        _doInitialSynchronization();
    }

    /** Return always true.
     *
     *  This function is no more used in this implementation of TimeRegulator
     *  interface. It must return true otherwise the proposeTime() will
     *  enter in an infinite loop.
     *  @return true always return true
     */
    @Override
    public boolean noNewActors() {
        return true;
    }

    /** Launch the HLA/CERTI RTIG process as subprocess. The RTIG has to be
     *  launched before the initialization of a Federate.
     *  NOTE: if another HlaManager (e.g. Federate) has already launched a RTIG,
     *  the subprocess creates here is no longer required, then we destroy it.
     *  @exception IllegalActionException If the initialization of the
     *  CertiRtig or the execution of the RTIG as subprocess has failed.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _startTime = System.nanoTime();

        // XXX: FIXME: remove after debug
        if (System.getenv("CERTI_FOM_PATH") != null) {
            _hlaDebugSys("preinitialize() - "
                    + "CERTI_FOM_PATH = " + System.getenv("CERTI_FOM_PATH"));
        }

        // Local or distant simulation support.
        // Based on CERTI_HOST variable set and different to localhost
        // or loopback address "127.0.0.1".
        _certiHost = System.getenv("CERTI_HOST");
        if (_certiHost != null) {
            if (_certiHost.compareTo("localhost") == 0) {
                _distantSimulation = false;
            } else if (_certiHost.compareTo("127.0.0.1") == 0) {
                _distantSimulation = false;
            } else {
                _distantSimulation = true;
            }

            if (_debugging) {
                _hlaDebug("preinitialize() - "
                        + "CERTI_HOST = " + _certiHost
                        + " _distantSimulation = " + _distantSimulation);
            }

            // XXX: FIXME: remove after debug
            _hlaDebugSys("preinitialize() - "
                    + "CERTI_HOST = " + _certiHost
                    + " _distantSimulation = " + _distantSimulation);
        } else {
            // XXX: FIXME: remove after debug
            _hlaDebugSys("preinitialize() - " + "CERTI_HOST = NULL");
        }

        // Try to launch the HLA/CERTI RTIG subprocess.
        if (!_distantSimulation) {
            _certiRtig = new CertiRtig(this, _debugging);
            _certiRtig.initialize(fedFile.asFile().getAbsolutePath());

            _certiRtig.exec();
            if (_debugging) {
                _hlaDebug("preinitialize() - "
                        + "Launch RTIG process");
            }

            if (_certiRtig.isAlreadyLaunched()) {
                _certiRtig.terminateProcess();
                _certiRtig = null;

                if (_debugging) {
                    _hlaDebug("preinitialize() - "
                            + "Destroy RTIG process as another one is already "
                            + "launched");
                }
            }
        }
        else {
            if (_debugging) {
                _hlaDebug("preinitialize() - "
                        + "Distant simulation: CERTI_HOST variable detected, no RTIG launched");
            }
        }
    }

    /** Propose a time to advance to. This method is the one implementing the
     *  TimeRegulator interface and using the HLA/CERTI Time Management services
     *  (if required). Following HLA and CERTI recommendations, if the Time
     *  Management is required then we have the following behavior:
     *  Case 1: If lookahead = 0
     *   -a) if time-stepped Federate, then the timeAdvanceRequestAvailable()
     *       (TARA) service is used;
     *   -b) if event-based Federate, then the nextEventRequestAvailable()
     *       (NERA) service is used
     *  Case 2: If lookahead &gt; 0
     *   -c) if time-stepped Federate, then timeAdvanceRequest() (TAR) is used;
     *   -d) if event-based Federate, then the nextEventRequest() (NER) is used;
     *  Otherwise the proposedTime is returned.
     *  NOTE: For the Ptolemy II - HLA/CERTI cooperation the default (and correct)
     *  behavior is the case 1 and CERTI has to be compiled with the option
     *  "CERTI_USE_NULL_PRIME_MESSAGE_PROTOCOL".
     *  @param proposedTime The proposed time.
     *  @return The proposed time or a smaller time.
     *  @exception IllegalActionException If this attribute is not
     *  contained by an Actor.
     */
    @Override
    public Time proposeTime(Time proposedTime) throws IllegalActionException {
        Time currentTime = _director.getModelTime();

        String strProposedTime = proposedTime.toString();
        if (_debugging) {
            if (_eventBased) {
                _hlaDebug("   start proposeTime(t(lastFoundEvent)="
                        + strProposedTime + ") " + "t_ptII = "
                        + currentTime.toString() + " doubleValue="
                        + currentTime.getDoubleValue() + "; t_hla = "
                        + _federateAmbassador.hlaLogicalTime);
            } else {
                _hlaDebug("     starting proposeTime(" + strProposedTime + ") "
                        + "t_ptII = " + currentTime.toString() + " doubleValue="
                        + currentTime.getDoubleValue() + "; t_hla = "
                        + _federateAmbassador.hlaLogicalTime);
            }
        }

        // If the proposeTime has exceed the simulation stop time
        // so it has no need to ask for the HLA service
        // then return the _stopTime.

        // Test if we have exceed the simulation stop time.
        if (proposedTime.compareTo(_stopTime) > 0) {
            // XXX: FIXME: clarify SKIP RTI
            if (_debugging) {
                _hlaDebug("    proposeTime(" + strProposedTime + ") -"
                        + " proposedTime > stopTime"
                        + " -> SKIP RTI -> returning stopTime");
            }
            return _stopTime;
        }

        // XXX: FIXME: see why this is called again after STOPTIME ?
        // XXX: FIXME: may be to removed since the above if test ?
        // This test is used to avoid exception when the RTIG subprocess is
        // shutdown before the last call of this method.
        if (_rtia == null) {
            if (_debugging) {
                _hlaDebug("    proposeTime(" + strProposedTime
                        + ") - called but the _rtia is null "
                        + " -> SKIP RTI ->  returning proposedTime");
            }
            return proposedTime;
        }

        // If the proposedTime is equal to current time
        // so it has no need to ask for the HLA service
        // then return the currentTime.

        if (currentTime.equals(proposedTime)) {
            // Even if we avoid the multiple calls of the HLA Time management
            // service for optimization, it could be possible to have events
            // from the Federation in the Federate's priority timestamp queue,
            // so we tick() to get these events (if they exist).
            /*   if (_debugging) {
                _hlaDebug("       proposeTime(" + strProposedTime
                       + "): currentTime t_ptII = proposedTime: tick() one time");  //-> returning currentTime");
            }
             */ //jc: for make the reading easier. If needed, we can go back to this print.
            try {
                _rtia.tick();

                if (_enableHlaReporter) {
                    if (_hlaReporter.getTimeOfTheLastAdvanceRequest() > 0) {
                        //_hlaReporter._numberOfTicks.set(_hlaReporter._numberOfTAGs, _hlaReporter._numberOfTicks.get(_hlaReporter._numberOfTAGs) + 1);
                    } else {
                        _hlaReporter._numberOfOtherTicks++;
                    }
                }
            } catch (ConcurrentAccessAttempted e) {
                throw new IllegalActionException(this, e,
                        "ConcurrentAccessAttempted: " + e.getMessage());
            } catch (RTIinternalError e) {
                throw new IllegalActionException(this, e,
                        "RTIinternalError: " + e.getMessage());
            }

            return currentTime;
        }

        // If the HLA Time Management is required, ask to the HLA/CERTI
        // Federation (the RTI) the authorization to advance its time.
        if (_isTimeRegulator && _isTimeConstrained) {
            synchronized (this) {
                // Call the corresponding HLA Time Management service.
                try {
                    if (_eventBased) {
                        if (_debugging) {
                            _hlaDebug("    proposeTime(t(lastFoudEvent)=("
                                    + strProposedTime
                                    + ") - calling _eventsBasedTimeAdvance("
                                    + strProposedTime + ")");
                        }
                        return _eventsBasedTimeAdvance(proposedTime);
                    } else {
                        if (_debugging) {
                            _hlaDebug("    proposeTime(" + strProposedTime
                                    + ") - calling _timeSteppedBasedTimeAdvance("
                                    + strProposedTime + ")");
                        }
                        return _timeSteppedBasedTimeAdvance(proposedTime);
                    }
                } catch (InvalidFederationTime e) {
                    throw new IllegalActionException(this, e,
                            "InvalidFederationTime: " + e.getMessage());
                } catch (FederationTimeAlreadyPassed e) {
                    throw new IllegalActionException(this, e,
                            "FederationTimeAlreadyPassed: " + e.getMessage());
                } catch (TimeAdvanceAlreadyInProgress e) {
                    throw new IllegalActionException(this, e,
                            "TimeAdvanceAlreadyInProgress: " + e.getMessage());
                } catch (EnableTimeRegulationPending e) {
                    throw new IllegalActionException(this, e,
                            "EnableTimeRegulationPending: " + e.getMessage());
                } catch (EnableTimeConstrainedPending e) {
                    throw new IllegalActionException(this, e,
                            "EnableTimeConstrainedPending: " + e.getMessage());
                } catch (FederateNotExecutionMember e) {
                    throw new IllegalActionException(this, e,
                            "FederateNotExecutionMember: " + e.getMessage());
                } catch (SaveInProgress e) {
                    throw new IllegalActionException(this, e,
                            "SaveInProgress: " + e.getMessage());
                } catch (RestoreInProgress e) {
                    throw new IllegalActionException(this, e,
                            "RestoreInProgress: " + e.getMessage());
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, e,
                            "RTIinternalError: " + e.getMessage());
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, e,
                            "ConcurrentAccessAttempted: " + e.getMessage());
                } catch (NoSuchElementException e) {
                    if (_debugging) {
                        _hlaDebug("    proposeTime(" + strProposedTime + ") -"
                                + " NoSuchElementException " + " for _rtia");
                    }
                    // FIXME: XXX: explain properly that we want to do here?
                    return proposedTime;

                } catch (SpecifiedSaveLabelDoesNotExist e) {
                    Logger.getLogger(HlaManager.class.getName())
                            .log(Level.SEVERE, null, e);
                    throw new IllegalActionException(this, e,
                            "SpecifiedSaveLabelDoesNotExist: "
                                    + e.getMessage());
                }
            }
        }

        return null;
    }

    /** Update the HLA attribute <i>attributeName</i> with the containment of
     *  the token <i>in</i>. The updated attribute is sent to the HLA/CERTI
     *  Federation.
     *  @param hp The HLA publisher actor (HLA attribute) to update.
     *  @param in The updated value of the HLA attribute to update.
     *  @param senderName the name of the federate that sent the attribute.
     *  @exception IllegalActionException If a CERTI exception is raised then
     *  displayed it to the user.
     */
    public void updateHlaAttribute(HlaPublisher hp, Token in)
            throws IllegalActionException {

        // Get current model time.
        Time currentTime = _director.getModelTime();

        // The following operations build the different arguments required
        // to use the updateAttributeValues() (UAV) service provided by HLA/CERTI.

        // Retrieve information of the HLA attribute to publish.
        Object[] tObj = _hlaAttributesToPublish.get(hp.getFullName());

        // Encode the value to be sent to the CERTI.
        byte[] bAttributeValue = MessageProcessing.encodeHlaValue(hp, in);
        if (_debugging) {
            _hlaDebug("      start updateHlaAttribute() t_ptII = " + currentTime
                    + "; t_hla = " + _federateAmbassador.hlaLogicalTime);
        }
        SuppliedAttributes suppAttributes = null;
        try {
            suppAttributes = RtiFactoryFactory.getRtiFactory()
                    .createSuppliedAttributes();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, e,
                    "RTIinternalError: " + e.getMessage());
        }
        suppAttributes.add(_getAttributeHandleFromTab(tObj), bAttributeValue);

        // Note: this information is not used in the current implementation.
        byte[] tag = EncodingHelpers.encodeString(hp.getFullName());

        // Create a representation of uav-event timestamp for CERTI.
        // HLA implies to send event in the future when using NER or TAR services with lookahead > 0.
        // Let us recall the lookahead rule: a federate promises that no events will be sent
        // before hlaCurrentTime + lookahead.
        // To avoid CERTI exception when calling UAV service
        // with condition: uav(tau) tau >= hlaCurrentTime + lookahead.

        // Table 2: UAV timestamp sent by a HlaPublisher
        CertiLogicalTime uavTimeStamp = null;

        // NER (_eventBased case)
        if (_eventBased) {
            // In the NER case, we have the equality currentTime = hlaCurrentTime.
            // So, we chose tau <- currentTime + lookahead and we respect the condition
            // above.
            uavTimeStamp = new CertiLogicalTime(
                    currentTime.getDoubleValue() + _hlaLookAHead);
        } else {
            // TAR (_timeStepped case)

            // f() => _convertToPtolemyTime()
            // g() => _convertToCertiLogicalTime()

            // t => currentTime => Ptolemy time => getModelTime()

            // h => hlaCurrentTime => HLA logical time => _federateAmbassador.logicalTimeHLA
            CertiLogicalTime hlaCurrentTime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;

            // g(t)
            CertiLogicalTime ptIICurrentTime = _convertToCertiLogicalTime(
                    currentTime);

            // h + lah => minimal next UAV time
            CertiLogicalTime minimalNextUAVTime = new CertiLogicalTime(
                    hlaCurrentTime.getTime() + _hlaLookAHead);

            // if h + lah > g(t) <=> if g(t) < h +lah
            if (minimalNextUAVTime.isGreaterThan(ptIICurrentTime)) {
                // UAV(h + lah)
                uavTimeStamp = minimalNextUAVTime;
            } else {
                // UAV(g(t))
                uavTimeStamp = ptIICurrentTime;
            }
        }

        // HLA Reporter support.
        if (_enableHlaReporter) {
            _hlaReporter.updateUAVsInformation(hp, in, _getHlaCurrentTime(),
                    currentTime, _director.getMicrostep(), uavTimeStamp);
        }

        // XXX: FIXME: check if we may add the object instance id to the HLA publisher and remove this.
        int objectInstanceId = _registerObjectInstanceMap
                .get(hp.getClassInstanceName());

        try {
            if (_debugging) {
                _hlaDebug("      * UAV '" + hp.getAttributeName()
                        + "', uavTimeStamp=" + uavTimeStamp.getTime()
                        + ", value=" + in.toString() + ", HlaPub="
                        + hp.getFullName());
            }

            _rtia.updateAttributeValues(objectInstanceId, suppAttributes, tag,
                    uavTimeStamp);

            if (_enableHlaReporter) {
                _hlaReporter.incrNumberOfUAVs();
            }

        } catch (ObjectNotKnown e) {
            throw new IllegalActionException(this, e,
                    "ObjectNotKnown: " + e.getMessage());
        } catch (AttributeNotDefined e) {
            throw new IllegalActionException(this, e,
                    "AttributeNotDefined: " + e.getMessage());
        } catch (AttributeNotOwned e) {
            throw new IllegalActionException(this, e,
                    "AttributeNotOwned: " + e.getMessage());
        } catch (InvalidFederationTime e) {
            throw new IllegalActionException(this, e, "InvalidFederationTime: "
                    + e.getMessage() + "    updateHlaAttribute() - sending UAV("
                    + "HLA publisher=" + hp.getFullName() + ",HLA attribute="
                    + hp.getAttributeName() + ",uavTimeStamp="
                    + uavTimeStamp.getTime() + ",value=" + in.toString() + ")"
                    + " ptII_time=" + currentTime.toString() + " certi_time="
                    + _federateAmbassador.hlaLogicalTime);
        } catch (FederateNotExecutionMember e) {
            throw new IllegalActionException(this, e,
                    "FederateNotExecutionMember: " + e.getMessage());
        } catch (SaveInProgress e) {
            throw new IllegalActionException(this, e,
                    "SaveInProgress: " + e.getMessage());
        } catch (RestoreInProgress e) {
            throw new IllegalActionException(this, e,
                    "RestoreInProgress: " + e.getMessage());
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, e,
                    "RTIinternalError: " + e.getMessage());
        } catch (ConcurrentAccessAttempted e) {
            throw new IllegalActionException(this, e,
                    "ConcurrentAccessAttempted: " + e.getMessage());
        }
    }

    /** Manage the correct termination of the {@link HlaManager}. Call the
     *  HLA services to: unsubscribe to HLA attributes, unpublish HLA attributes,
     *  resign a Federation and destroy a Federation if the current Federate is
     *  the last participant.
     *  @exception IllegalActionException If the parent class throws it
     *  of if a CERTI exception is raised then displayed it to the user.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _hlaDebug("wrapup() - ... so termination");
        }

        super.wrapup();

        if (_enableHlaReporter) {
            if (_debugging) {
                _hlaDebug(_hlaReporter.displayAnalysisValues());
            }

            _hlaReporter.calculateRuntime();
            _hlaReporter.writeNumberOfHLACalls();
            _hlaReporter.writeDelays();
            _hlaReporter.writeUAVsInformation();
            _hlaReporter.writeRAVsInformation();
            _hlaReporter.writeTimes();
        }

        // Unsubscribe to HLA attributes.
        for (Object[] obj : _hlaAttributesToSubscribeTo.values()) {
            try {
                _rtia.unsubscribeObjectClass(_getClassHandleFromTab(obj));
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }
            if (_debugging) {
                _hlaDebug("wrapup() - unsubscribe "
                        + _getPortFromTab(obj).getContainer().getFullName()
                        + "(classHandle = " + _getClassHandleFromTab(obj)
                        + ")");
            }
        }

        // Unpublish HLA attributes.
        for (Object[] obj : _hlaAttributesToPublish.values()) {
            try {
                _rtia.unpublishObjectClass(_getClassHandleFromTab(obj));
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }
            if (_debugging) {
                _hlaDebug("wrapup() - unpublish "
                        + _getPortFromTab(obj).getContainer().getFullName()
                        + "(classHandle = " + _getClassHandleFromTab(obj)
                        + ")");
            }
        }

        // Resign HLA/CERTI Federation execution.
        try {
            // _rtia can be null if we are exporting to JNLP.
            if (_rtia != null) {
                _rtia.resignFederationExecution(
                        ResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES);
            }
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e,
                    "RTIexception: " + e.getMessage());
        }
        if (_debugging) {
            _hlaDebug("wrapup() - Resign Federation execution");
        }
        _hlaDebugSys("wrapup() - Resign Federation execution");

        boolean canDestroyRtig = false;
        while (!canDestroyRtig) {

            // Destroy federation execution - nofail.
            try {
                _rtia.destroyFederationExecution(_federationName);

                // If the federation has been destroyed then we can authorize to kill the rtig.
                canDestroyRtig = true;

                if (_debugging) {
                    _hlaDebug("wrapup() - "
                            + "Destroy Federation execution - no fail");
                }

                _hlaDebugSys("wrapup() - Destroy Federation execution - canDestroyRtig="
                        + canDestroyRtig);

            } catch (FederatesCurrentlyJoined e) {
                if (_debugging) {
                    _hlaDebug("wrapup() - WARNING: FederatesCurrentlyJoined");
                }
                _hlaDebugSys("wrapup() - Exception: FederatesCurrentlyJoined");

            } catch (FederationExecutionDoesNotExist e) {
                // No more federation.
                if (_debugging) {
                    _hlaDebug("wrapup() - WARNING: FederationExecutionDoesNotExist");
                }
                canDestroyRtig = true;

                _hlaDebugSys("wrapup() - Exception: FederationExecutionDoesNotExist - canDestroyRtig="
                        + canDestroyRtig);

            } catch (RTIinternalError e) {
                throw new IllegalActionException(this, e,
                        "RTIinternalError: " + e.getMessage());
            } catch (ConcurrentAccessAttempted e) {
                throw new IllegalActionException(this, e,
                        "ConcurrentAccessAttempted: " + e.getMessage());
            }
        }

        // Terminate RTIG subprocess.
        if (_certiRtig != null) {
            if (_debugging) {
                _hlaDebug("wrapup() - "
                        + "Try to destroy RTIG process (if authorized by the system)");
            }
            _hlaDebugSys("Try to destroy RTIG process (if authorized by the system)");
            _certiRtig.terminateProcess();

        }

        // Clean HLA attribute tables.
        _hlaAttributesToPublish.clear();
        _hlaAttributesToSubscribeTo.clear();
        _fromFederationEvents.clear();
        _objectIdToClassHandle.clear();

        // Clean HLA object instance id maps.
        _registerObjectInstanceMap.clear();
        _discoverObjectInstanceMap.clear();

        // Joker wildcard support.
        _usedJokerFilterMap.clear();

        // HLA Reporter support.
        _hlaReporter = null;

        if (_debugging) {
            _hlaDebug("-----------------------");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Table of HLA attributes (and their HLA information) that are published
     *  by the current {@link HlaManager} to the HLA/CERTI Federation. This
     *  table is indexed by the {@link HlaPublisher} actors present in the model.
     */
    protected HashMap<String, Object[]> _hlaAttributesToPublish;

    /** Table of HLA attributes (and their HLA information) that the current
     *  {@link HlaManager} is subscribed to. This table is indexed by the
     *  {@link HlaSubscriber} actors present in the model.
     */
    protected HashMap<String, Object[]> _hlaAttributesToSubscribeTo;

    /** List of events received from the HLA/CERTI Federation and indexed by the
     *  {@link HlaSubscriber} actors present in the model.
     */
    protected HashMap<String, LinkedList<TimedEvent>> _fromFederationEvents;

    /** Table of object class handles associate to object ids received by
     *  discoverObjectInstance and reflectAttributesValues services (e.g. from
     *  the RTI).
     */
    protected HashMap<Integer, Integer> _objectIdToClassHandle;

    /** Table of used joker (wildcard) filter. */
    protected HashMap<String, Boolean> _usedJokerFilterMap;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Convert Ptolemy time to CERTI logical time.
     * @param pt The Ptolemy time.
     * @return the time converted to CERTI (HLA) logical time.
     */
    private CertiLogicalTime _convertToCertiLogicalTime(Time pt) {
        return new CertiLogicalTime(pt.getDoubleValue() * _hlaTimeUnitValue);
    }

    /** Convert CERTI (HLA) logical time to Ptolemy time.
     *  @param ct The CERTI (HLA) logical time.
     *  @return the time converted to Ptolemy time.
     *  @exception IllegalActionException If the given double time value does
     *  not match the time resolution.
     */
    private Time _convertToPtolemyTime(CertiLogicalTime ct)
            throws IllegalActionException {
        return new Time(_director, ct.getTime() / _hlaTimeUnitValue);
    }

    /** Time advancement method for event-based federates. This method
     *  uses NER or NERA RTI services to propose a time to advance to
     *  in a HLA simulation
     *  This method implements the algorithm 3 "NER proposeTime(t')"
     *  from [citeFestscrhiftLeeRapportInterneDisc-2017].
     *  @param proposedTime time stamp of the next Ptolemy event.
     *  @return the granted time from the HLA simulation.
     *  @exception IllegalActionException
     */
    private Time _eventsBasedTimeAdvance(Time proposedTime)
            throws IllegalActionException, InvalidFederationTime,
            FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress,
            FederateNotExecutionMember, SaveInProgress,
            EnableTimeRegulationPending, EnableTimeConstrainedPending,
            RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted,
            SpecifiedSaveLabelDoesNotExist {

        // Custom string representation of proposedTime.
        String strProposedTime = proposedTime.toString();

        // HLA Reporter support.
        if (_enableHlaReporter) {
            _hlaReporter.storeTimes("NER()", proposedTime,
                    _director.getModelTime());
        }

        if (_debugging) {
            _hlaDebug("_eventsBasedTimeAdvance(): strProposedTime"
                    + " proposedTime=" + proposedTime.toString()
                    + " - calling CERTI NER()");
        }

        // Algorithm 3 - NER (naive implementation to be compliant with the algorithm)

        // f() => _convertToPtolemyTime()
        // g() => _convertToCertiLogicalTime()

        // r => director global time resolution
        Double r = _director.getTimeResolution();

        // t => Ptolemy time => getModelTime()
        Time ptolemyTime = _director.getModelTime();

        // t' => proposedTime

        // h => HLA logical time => _federateAmbassador.logicalTimeHLA
        CertiLogicalTime hlaLogicaltime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;

        // g(t') => certiProposedTime
        CertiLogicalTime certiProposedTime = _convertToCertiLogicalTime(
                proposedTime);

        // algo3: 1: if g(t') > h then
        if (certiProposedTime.isGreaterThan(hlaLogicaltime)) {
            // Wait the time grant from the HLA/CERTI Federation (from the RTI).
            _federateAmbassador.timeAdvanceGrant = false;

            if (_enableHlaReporter) {
                // Set time of last time advance request.
                _hlaReporter.setTimeOfTheLastAdvanceRequest(System.nanoTime());
            }

            // algo3: 2: NER(g(t'))
            // Call CERTI NER HLA service.
            _rtia.nextEventRequest(certiProposedTime);

            if (_enableHlaReporter) {
                // Increment counter of NER calls.
                _hlaReporter.incrNumberOfNERs();
            }

            // algo3: 3: while not granted do
            while (!(_federateAmbassador.timeAdvanceGrant)) {
                if (_debugging) {
                    _hlaDebug("        proposeTime(t(lastFoundEvent)="
                            + strProposedTime + ") - _eventsBasedTimeAdvance("
                            + strProposedTime + ") - " + " waiting TAG(" //jc: + certiProposedTime.getTime()
                            + ") by calling tick2()");
                }
                _rtia.tick2(); // algo3: 4: tick()  > Wait TAG(h'')

                // HLA Reporter support.
                if (_enableHlaReporter) {
                    _hlaReporter._numberOfTicks2++;
                    _hlaReporter._numberOfTicks.set(_hlaReporter._numberOfTAGs,
                            _hlaReporter._numberOfTicks
                                    .get(_hlaReporter._numberOfTAGs) + 1);
                }

            } // algo3: 5: end while

            // algo3: 6: h <- h''    => Update HLA time
            _federateAmbassador.hlaLogicalTime = _federateAmbassador.grantedHlaLogicalTime;

            // algo3: 7: if receivedRAV then
            if (_federateAmbassador.hasReceivedRAV) {

                // algo3: 8: t'' <- f(h'')
                Time newPtolemyTime = _convertToPtolemyTime(
                        (CertiLogicalTime) _federateAmbassador.grantedHlaLogicalTime);

                // algo3: 9: if t'' > t then  => True in the general case
                if (newPtolemyTime.compareTo(ptolemyTime) > 0) {
                    // algo3: 10: t' <- t''
                    proposedTime = newPtolemyTime;
                } else { // algo3: 11: else
                    // algo3: 12: t' <- t'' + r

                    // Note: Modification from mail J.Cardoso 24/10/2017.
                    //proposedTime = newPtolemyTime.add(r);
                    proposedTime = ptolemyTime.add(r);

                } // algo3: 13: end if

                // algo3: 14: putRAVonHlaSubs(t')
                // Store reflected attributes RAV as events on HLASubscriber actors.
                _putReflectedAttributesOnHlaSubscribers(proposedTime);

                _federateAmbassador.hasReceivedRAV = false;

            } // algo3: 15: end if => if receivedRAV then

        } // algo3: 16: end if

        return proposedTime;

    }

    /** Get the current time in HLA which is advanced after a TAG callback.
     *  @return the HLA current time converted as Ptolemy time.
     */
    private Time _getHlaCurrentTime() throws IllegalActionException {
        CertiLogicalTime certiCurrentTime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;
        return _convertToPtolemyTime(certiCurrentTime);
    }

    /** The method {@link #_getHlaSubscribers()} get all HLA subscriber
     *  actors across the model.
     *  @param ce the composite entity which may contain HlaSubscribers
     *  @return the list of HlaSubscribers
     */
    private List<HlaSubscriber> _getHlaSubscribers(CompositeEntity ce) {
        // The list of HLA subscribers to return.
        LinkedList<HlaSubscriber> hlaSubscribers = new LinkedList<HlaSubscriber>();

        // List all classes from top level model.
        List<CompositeEntity> entities = ce.entityList();
        for (ComponentEntity classElt : entities) {
            if (classElt instanceof HlaSubscriber) {
                hlaSubscribers.add((HlaSubscriber) classElt);
            } else if (classElt instanceof ptolemy.actor.TypedCompositeActor) {
                hlaSubscribers
                        .addAll(_getHlaSubscribers((CompositeEntity) classElt));
            }
        }

        return hlaSubscribers;
    }

    /** Customized debug message for {@link #HlaManager}.
     *  @param reason The reason to print
     */
    private void _hlaDebug(String reason) {
        String dbgHeader = "Federate: " + _federateName + " - Federation: " + _federationName + " - ";
        _hlaDebug(dbgHeader + reason);
    }

    /** Customized system debug message for {@link #HlaManager}.
     *  @param reason The reason to print
     */
    private void _hlaDebugSys(String reason) {
        String dbgHeader = "Federate: " + _federateName + " - Federation: " + _federationName + " - ";
        _hlaDebugSys(dbgHeader + reason);
    }    

    /** RTI service for time-stepped federate TAR
     *  is used for proposing a time to advance to.
     *  @param proposedTime time stamp of last found event
     *  @return a valid time to advance to
     */
    private Time _timeSteppedBasedTimeAdvance(Time proposedTime)
            throws IllegalActionException {

        // HLA Reporter support.
        if (_enableHlaReporter) {
            _hlaReporter.storeTimes("TAR()", proposedTime,
                    _director.getModelTime());
        }

        // Header for debug purpose and listener.
        String headMsg = "_timeSteppedBasedTimeAdvance("
                + proposedTime.toString() + "): ";

        if (_debugging) {
            _hlaDebug("\n" + "start " + headMsg + " print proposedTime.toString="
                    + proposedTime.toString());
        }

        // Algorithm 4 - TAR (naive implementation to be compliant with the algorithm)

        // f() => _convertToPtolemyTime()
        // g() => _convertToCertiLogicalTime()

        // t   => Ptolemy time => getModelTime()

        // t'    => proposedTime
        // g(t') => certiProposedTime
        CertiLogicalTime certiProposedTime = _convertToCertiLogicalTime(
                proposedTime);

        // h      => HLA logical time => _federateAmbassador.logicalTimeHLA
        CertiLogicalTime hlaLogicaltime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;

        // TS     => _hlaTimeStep
        // h + TS => nextPointInTime
        CertiLogicalTime nextPointInTime = new CertiLogicalTime(
                hlaLogicaltime.getTime() + _hlaTimeStep);

        // algo4: 1: while g(t') >= h + TS then

        // NOTE: Microstep reset problem
        //  To retrieve the old behavior with the microstep reset problem, you may change the line below:
        //  reset    => while (certiProposedTime.isGreaterThan(nextPointInTime)) {
        //  no reset => while (certiProposedTime.isGreaterThanOrEqualTo(nextPointInTime)) {

        if (_debugging) {
            _hlaDebug("Before While g(t') > h+TS; g(t')= "
                    + certiProposedTime.getTime() + "; h+TS= "
                    + nextPointInTime.getTime() + " @ " + headMsg);
        }

        while (certiProposedTime.isGreaterThanOrEqualTo(nextPointInTime)) {
            // Wait the time grant from the HLA/CERTI Federation (from the RTI).
            _federateAmbassador.timeAdvanceGrant = false;

            // algo4: 2: TAR(h + TS))
            try {
                if (_enableHlaReporter) {
                    // Set time of last time advance request.
                    _hlaReporter
                            .setTimeOfTheLastAdvanceRequest(System.nanoTime());
                }

                // Call CERTI TAR HLA service.
                _rtia.timeAdvanceRequest(nextPointInTime);

                if (_enableHlaReporter) {
                    // Increment counter of TAR calls.
                    _hlaReporter.incrNumberOfTARs();
                }

                if (_debugging) {
                    _hlaDebug("  TAR(" + nextPointInTime.getTime() + ") in "
                            + headMsg);
                }
            } catch (InvalidFederationTime | FederationTimeAlreadyPassed
                    | TimeAdvanceAlreadyInProgress | EnableTimeRegulationPending
                    | EnableTimeConstrainedPending | FederateNotExecutionMember
                    | SaveInProgress | RestoreInProgress | RTIinternalError
                    | ConcurrentAccessAttempted e) {
                throw new IllegalActionException(this, e, e.getMessage());
            }

            // algo4: 3: while not granted do
            while (!(_federateAmbassador.timeAdvanceGrant)) {
                if (_debugging) {
                    _hlaDebug("      waiting TAG(" // + nextPointInTime.getTime() //jc: no need
                            + ") by calling tick2() in " + headMsg);
                }

                try {
                    _rtia.tick2();

                    if (_enableHlaReporter) {
                        _hlaReporter._numberOfTicks2++;
                        _hlaReporter._numberOfTicks
                                .set(_hlaReporter._numberOfTAGs,
                                        _hlaReporter._numberOfTicks
                                                .get(_hlaReporter._numberOfTAGs)
                                                + 1);
                    }

                } catch (SpecifiedSaveLabelDoesNotExist
                        | ConcurrentAccessAttempted | RTIinternalError e) {
                    throw new IllegalActionException(this, e, e.getMessage());
                } // algo4: 4: tick()  > Wait TAG()

            } // algo4: 5: end while

            // algo4: 6: h <- h + TS    => Update HLA time
            _federateAmbassador.hlaLogicalTime = nextPointInTime;

            // algo4: 7: if receivedRAV then
            if (_federateAmbassador.hasReceivedRAV) {

                // algo4: 8: t'' <- f(h)
                Time newPtolemyTime = _convertToPtolemyTime(
                        (CertiLogicalTime) _federateAmbassador.hlaLogicalTime);

                // algo4: 9: if t'' > t' then  => Update t' if the received time is smaller, otherwise keeps t'
                if (newPtolemyTime.compareTo(proposedTime) < 0) {
                    // algo4: 10: t' <- t''
                    if (_debugging) {
                        _hlaDebug("    newPtolemyTime= t'=t''=f(h)="
                                + newPtolemyTime.toString()
                                + " @line 10 in algo 4 " + headMsg);
                    }
                    proposedTime = newPtolemyTime;
                } // algo4: 11: end if

                // algo4: 12: putRAVonHlaSubs(t')
                // Store reflected attributes RAV as events on HLASubscriber actors.
                _putReflectedAttributesOnHlaSubscribers(proposedTime);

                _federateAmbassador.hasReceivedRAV = false;

                // algo4: 13: return t'
                if (_debugging) {
                    _hlaDebug("Returns proposedTime=" + proposedTime.toString()
                            + "    @line 13 algo 4 (hasReceivedRAV) " + headMsg
                            + "\n");
                }
                return proposedTime;

            } // algo4: 14: end if receivedRAV then

            // Update local variables with the new HLA logical time.
            hlaLogicaltime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;
            nextPointInTime = new CertiLogicalTime(
                    hlaLogicaltime.getTime() + _hlaTimeStep);

        } // algo4: 15: end while

        if (_debugging) {
            _hlaDebug("returns proposedTime=" + proposedTime.toString() + "from "
                    + headMsg);
        }

        // algo4: 16: return t' => Update PtII time
        return proposedTime;
    }

    /** The method {@link #_populatedHlaValueTables()} populates the tables
     *  containing information of HLA attributes required to publish and to
     *  subscribe value attributes in a HLA Federation.
     *  @exception IllegalActionException If a HLA attribute is declared twice.
     */
    private void _populateHlaAttributeTables() throws IllegalActionException {
        CompositeEntity ce = (CompositeEntity) getContainer();

        // HlaPublishers.
        _hlaAttributesToPublish.clear();
        List<HlaPublisher> _hlaPublishers = ce.entityList(HlaPublisher.class);
        for (HlaPublisher hp : _hlaPublishers) {
            // Note: The HLA attribute name is no more associated to the
            // HlaPublisher actor name. As Ptolemy do not accept two actors
            // of the same name at a same model level the following test is no
            // more required.
            //if (_hlaAttributesToPublish.get(hp.getFullName()) != null) {
            //    throw new IllegalActionException(this,
            //            "A HLA attribute with the same name is already "
            //                    + "registered for publication.");
            //}

            // Note: asked by JC on 20171128, the current implementation is not
            // optimized and may slow the model initialization step if there is
            // a lot of actors.
            // The HLA attribute is no more associated to the HlaPublisher
            // actor name but instead to the attribute name parameter. Checks
            // and throws an exception if two actors specify the same HLA
            // attribute from a same HLA object class and a same HLA instance
            // class name.
            for (HlaPublisher hpIndex : _hlaPublishers) {
                if ((!hp.getFullName().equals(hpIndex.getFullName())
                        && (hp.getAttributeName()
                                .compareTo(hpIndex.getAttributeName()) == 0)
                        && (hp.getClassObjectName()
                                .compareTo(hpIndex.getClassObjectName()) == 0)
                        && (hp.getClassInstanceName().compareTo(
                                hpIndex.getClassInstanceName()) == 0))
                        || (!hp.getFullName().equals(hpIndex.getFullName())
                                && (!hp.getClassObjectName()
                                        .equals(hpIndex.getClassObjectName()))
                                && (hp.getClassInstanceName().compareTo(hpIndex
                                        .getClassInstanceName()) == 0))) {

                    // FIXME: XXX: Highlight the faulty HlaPublisher actor here, see UCB for API.

                    throw new IllegalActionException(this, "A HlaPublisher '"
                            + hpIndex.getFullName()
                            + "' with the same HLA information specified by the "
                            + "HlaPublisher '" + hp.getFullName()
                            + "' \nis already registered for publication.");
                }
            }

            // Only one input port is allowed per HlaPublisher actor.
            TypedIOPort tIOPort = hp.inputPortList().get(0);

            _hlaAttributesToPublish.put(hp.getFullName(),

                    // XXX: FIXME: simply replace Object[] by a HlaPublisher instance ?

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
                    new Object[] { tIOPort, tIOPort.getType(),
                            hp.getClassObjectName(),
                            hp.getClassInstanceName() });
        }

        // HlaSubscribers.
        _hlaAttributesToSubscribeTo.clear();

        List<HlaSubscriber> _hlaSubscribers = _getHlaSubscribers(ce);

        for (HlaSubscriber hs : _hlaSubscribers) {
            // Note: The HLA attribute name is no more associated to the
            // HlaSubscriber actor name. As Ptolemy do not accept two actors
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
            // The HLA attribute is no more associated to the HlaSubscriber
            // actor name but instead to the attribute name parameter. Checks
            // and throws an exception if two actors specify the same HLA
            // attribute from a same HLA object class and a same HLA instance
            // class name.
            for (HlaSubscriber hsIndex : _hlaSubscribers) {
                if ((!hs.getFullName().equals(hsIndex.getFullName())
                        && (hs.getAttributeName()
                                .compareTo(hsIndex.getAttributeName()) == 0)
                        && (hs.getClassObjectName()
                                .compareTo(hsIndex.getClassObjectName()) == 0)
                        && (hs.getClassInstanceName().compareTo(
                                hsIndex.getClassInstanceName()) == 0))
                        || (!hs.getFullName().equals(hsIndex.getFullName())
                                && (!hs.getClassObjectName()
                                        .equals(hsIndex.getClassObjectName()))
                                && (hs.getClassInstanceName().compareTo(hsIndex
                                        .getClassInstanceName()) == 0))) {

                    // FIXME: XXX: Highlight the faulty HlaSubscriber actor here, see UCB for API.

                    throw new IllegalActionException(this, "A HlaSubscriber '"
                            + hsIndex.getFullName()
                            + "' with the same HLA information specified by the "
                            + "HlaSubscriber '" + hs.getFullName()
                            + "' \nis already registered for subscription.");
                }
            }

            // Only one output port is allowed per HlaSubscriber actor.
            TypedIOPort tiop = hs.outputPortList().get(0);

            _hlaAttributesToSubscribeTo.put(hs.getFullName(),

                    // XXX: FIXME: simply replace object[] by a HlaSubscriber instance ?

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
                            hs.getClassObjectName(),
                            hs.getClassInstanceName() });

            // The events list to store updated values of HLA attribute,
            // (received by callbacks) from the RTI, is indexed by the HLA
            // Subscriber actors present in the model.
            _fromFederationEvents.put(hs.getFullName(),
                    new LinkedList<TimedEvent>());

            // Joker wildcard support.
            _usedJoker = false;

            String classInstanceOrJokerName = hs.getClassInstanceName();

            if (classInstanceOrJokerName.contains(_jokerFilter)) {
                _usedJoker = true;
                if (_debugging) {
                    _hlaDebug("HLA actor " + hs.getFullName()
                            + " uses joker wildcard = " + _jokerFilter);
                }
            }

            if (_usedJoker) {
                if (!classInstanceOrJokerName.contains(_jokerFilter)) {
                    throw new IllegalActionException(this,
                            "Cannot mix class instance name and joker filter in HLA Subscribers "
                                    + "please check: " + hs.getFullName());
                } else {
                    // Add a new discovered joker to the joker table.
                    _usedJokerFilterMap.put(classInstanceOrJokerName, false);
                }
            }
        }
    }

    /** This method is called when a time advancement phase is performed. Every
     *  updated HLA attributes received by callbacks (from the RTI) during the
     *  time advancement phase is saved as {@link TimedEvent} and stored in a
     *  queue. Then, every {@link TimedEvent} is moved from this queue to the
     *  output port of their corresponding {@link HLASubscriber} actors
     *  @exception IllegalActionException If the parent class throws it.
     */
    private void _putReflectedAttributesOnHlaSubscribers(Time proposedTime)
            throws IllegalActionException {
        // Reflected HLA attributes, e.g. updated values of HLA attributes
        // received by callbacks (from the RTI) from the whole HLA/CERTI
        // Federation, are stored in a queue (see reflectAttributeValues()
        // in PtolemyFederateAmbassadorInner class).

        if (_debugging) {
            _hlaDebug("       t_ptII = " + _director.getModelTime().toString()
                    + "; t_hla = " + _federateAmbassador.hlaLogicalTime
                    + " in _putReflectedAttributesOnHlaSubscribers("
                    + proposedTime.toString() + ")");
        }

        Iterator<Entry<String, LinkedList<TimedEvent>>> it = _fromFederationEvents
                .entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, LinkedList<TimedEvent>> elt = it.next();

            // Multiple events can occur at the same time.
            LinkedList<TimedEvent> events = elt.getValue();
            while (events.size() > 0) {

                HlaTimedEvent ravEvent = (HlaTimedEvent) events.get(0);

                // Update received RAV event timestamp (see Table 3 - FestscrhiftLeeRapportInterneDisc).
                if (_timeStepped) {
                    // Table 3: e(f(h+TS)) (5)
                    ravEvent.timeStamp = _getHlaCurrentTime();
                } else {
                    // Table 3: e(f(h'')) (4)
                    ravEvent.timeStamp = proposedTime;
                }

                // If any RAV-event received by HlaSubscriber actors, RAV(tau) with tau < ptolemy startTime
                // are put in the event queue with timestamp startTime
                if (ravEvent.timeStamp
                        .compareTo(_director.getModelStartTime()) < 0) {
                    ravEvent.timeStamp = _director.getModelStartTime();
                }

                // Get the HLA subscriber actor to which the event is destined to.
                String actorName = elt.getKey();

                TypedIOPort tiop = _getPortFromTab(
                        _hlaAttributesToSubscribeTo.get(actorName));

                HlaSubscriber hs = (HlaSubscriber) tiop.getContainer();
                hs.putReflectedHlaAttribute(ravEvent);

                if (_debugging) {
                    _hlaDebug("       _putRAVOnHlaSubs(" + proposedTime.toString()
                            + " ravEvent.timeStamp=" + ravEvent.timeStamp
                            + ") for '" + hs.getAttributeName()
                            //+ "',timestamp=" + ravEvent.timeStamp //jc: non need
                            + " in HlaSubs=" + hs.getFullName());
                }

                if (_enableHlaReporter) {
                    _hlaReporter.updateFolRAVsTimes(ravEvent.timeStamp);
                }

                // Remove handled event.
                events.removeFirst();
            }
        }

        // At this point we have handled every events for all registered HlaSubscribers,
        // so we may clear the receivedRAV boolean.
        _federateAmbassador.hasReceivedRAV = false;

        if (_debugging) {
            _hlaDebug("        _putRAVOnHlaSubs(" + proposedTime.toString()
                    + ") - no more RAVs to deal with");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Content of the CERTI_HOST environment variable. */
    private String _certiHost;

    /** Indicates if the simulation is 'local' - i.e. federates and rtig on the
     *  same computer - or if the simulation is 'distant' - i.e. on different computers.
     *  This interpretation is based on the CERTI_HOST environement variable analysis.
     */
    private Boolean _distantSimulation;

    /** Name of the current Ptolemy federate ({@link HlaManager}). */
    private String _federateName;

    /**-Name of the HLA/CERTI federation to create or to join. */
    private String _federationName;

    /** Federate Ambassador for the Ptolemy Federate. */
    private PtolemyFederateAmbassadorInner _federateAmbassador;

    /** RTI Ambassador for the Ptolemy Federate. */
    private CertiRtiAmbassador _rtia;

    /** Indicates the use of the nextEventRequest() service. */
    private Boolean _eventBased;

    /** Indicates the use of the timeAdvanceRequest() service. */
    private Boolean _timeStepped;

    /** The lookahead value of the Ptolemy Federate. */
    private Double _hlaLookAHead;

    /** Time step of the Ptolemy Federate. */
    private Double _hlaTimeStep;

    /** Indicates the use of the enableTimeConstrained() service. */
    private Boolean _isTimeConstrained;

    /** Indicates the use of the enableTimeRegulation() service. */
    private Boolean _isTimeRegulator;

    /** Indicates if the Ptolemy Federate will use a synchronization point. */
    private Boolean _requireSynchronization;

    /** Name of the synchronization point to create or to reach. */
    private String _synchronizationPointName;

    /** Indicates if the Ptolemy Federate is the creator of the synchronization
     *  point. */
    private Boolean _isCreator;

    /** The simulation stop time. */
    private Time _stopTime;

    /** Represents the instant when the simulation is fully started
     * (when the last federate starts running).
     */
    private static double _startTime;

    /** A reference to the enclosing director. */
    private DEDirector _director;

    /** The RTIG subprocess. */
    private CertiRtig _certiRtig;

    /** Map class instance name and object instance ID. Those information are set
     *  using discoverObjectInstance() callback and used by the RAV service.
     */
    private HashMap<Integer, String> _discoverObjectInstanceMap;

    /**
     * Map <Sender actor + HlaPublishers> and ROI ID for an object instance. See HlaPublishers.
     *
     * HashMap for HlaPublishers to remember which actor's ID has
     * been registered (as an object instance) to the Federation.
     */
    private HashMap<String, Integer> _registerObjectInstanceMap;

    /** The actual value for hlaTimeUnit parameter. */
    private double _hlaTimeUnitValue;

    /** The reserved keyword to filter HLA subscribers using joker wildcard. */
    private static final String _jokerFilter = "joker_";

    /** Indicates if the 'joker' filter is used for HLA class instance name by HLA subscribers actors. */
    private boolean _usedJoker;

    /** The HLA reporter instance if enabled. */
    private HlaReporter _hlaReporter;

    /** Indicates if the HLA reporter is enabled or not. */
    private Boolean _enableHlaReporter;

    ///////////////////////////////////////////////////////////////////
    ////                    private  methods                       ////

    /** This method does the initial synchronization loop among the
     *  federate and register the synchronization point if the federate
     *  is the synchronization point creator.
     *  @exception IllegalActionException If the RTI throws it.
     */
    private void _doInitialSynchronization() throws IllegalActionException {
        if (!_requireSynchronization) {
            return;
        }
        // If the current Federate is the creator then create the
        // synchronization point.
        if (_isCreator) {
            try {
                byte[] rfspTag = EncodingHelpers
                        .encodeString(_synchronizationPointName);
                _rtia.registerFederationSynchronizationPoint(
                        _synchronizationPointName, rfspTag);
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }

            // Wait synchronization point callbacks.
            while (!(_federateAmbassador.synchronizationSuccess)
                    && !(_federateAmbassador.synchronizationFailed)) {
                try {
                    _rtia.tick2();

                    // HLA Reporter support.
                    if (_enableHlaReporter) {
                        _hlaReporter._numberOfTicks2++;
                        if (_hlaReporter.getTimeOfTheLastAdvanceRequest() > 0) {
                            _hlaReporter._numberOfTicks
                                    .set(_hlaReporter._numberOfTAGs,
                                            _hlaReporter._numberOfTicks.get(
                                                    _hlaReporter._numberOfTAGs)
                                                    + 1);
                        } else {
                            _hlaReporter._numberOfOtherTicks++;
                        }
                    }
                } catch (RTIexception e) {
                    throw new IllegalActionException(this, e,
                            "RTIexception: " + e.getMessage());
                }
            }

            if (_federateAmbassador.synchronizationFailed) {
                throw new IllegalActionException(this,
                        "CERTI: Synchronization error ! ");
            }
        } // End block for synchronization point creation case.

        // Wait synchronization point announcement.
        while (!(_federateAmbassador.inPause)) {
            try {
                _rtia.tick2();

                // HLA Reporter support.
                if (_enableHlaReporter) {
                    _hlaReporter._numberOfTicks2++;
                    if (_hlaReporter.getTimeOfTheLastAdvanceRequest() > 0) {
                        _hlaReporter._numberOfTicks
                                .set(_hlaReporter._numberOfTAGs,
                                        _hlaReporter._numberOfTicks
                                                .get(_hlaReporter._numberOfTAGs)
                                                + 1);
                    } else {
                        _hlaReporter._numberOfOtherTicks++;
                    }
                }

            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }
        }

        // Satisfied synchronization point.
        try {
            _rtia.synchronizationPointAchieved(_synchronizationPointName);
            if (_debugging) {
                _hlaDebug("_doInitialSynchronization() - initialize() - Synchronisation point "
                        + _synchronizationPointName + " satisfied");
            }
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e,
                    "RTIexception: " + e.getMessage());
        }

        // Wait federation synchronization.
        while (_federateAmbassador.inPause) {
            if (_debugging) {
                _hlaDebug("_doInitialSynchronization() - initialize() - Waiting for simulation phase");
            }

            try {
                _rtia.tick2();

                // HLA Reporter support.
                if (_enableHlaReporter) {
                    _hlaReporter._numberOfTicks2++;
                    if (_hlaReporter.getTimeOfTheLastAdvanceRequest() > 0) {
                        _hlaReporter._numberOfTicks
                                .set(_hlaReporter._numberOfTAGs,
                                        _hlaReporter._numberOfTicks
                                                .get(_hlaReporter._numberOfTAGs)
                                                + 1);
                    } else {
                        _hlaReporter._numberOfOtherTicks++;
                    }
                }
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }
        }
    }

    /** This method enables all time regulating aspect for the federate. After this call
     *  the federate as stated to the RTI if it is time regulating and/or time regulator
     *  and has enable asynchronous delivery for RO messages
     *  @exception IllegalActionException if the RTI throws it.
     */
    private void _initializeTimeAspects() throws IllegalActionException {

        // Initialize Federate timing values.
        _federateAmbassador.initializeTimeValues(0.0, _hlaLookAHead);

        // Declare the Federate time constrained (if true).
        if (_isTimeConstrained) {
            try {
                _rtia.enableTimeConstrained();
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }
        }

        // Declare the Federate time regulator (if true).
        if (_isTimeRegulator) {
            try {
                _rtia.enableTimeRegulation(_federateAmbassador.hlaLogicalTime,
                        _federateAmbassador.effectiveLookAHead);
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }
        }

        // Wait the response of the RTI towards Federate time policies that has
        // been declared. The only way to get a response is to invoke the tick()
        // method to receive callbacks from the RTI. We use here the tick2()
        // method which is blocking and saves more CPU than the tick() method.
        if (_isTimeRegulator && _isTimeConstrained) {
            while (!(_federateAmbassador.timeConstrained)) {
                try {
                    _rtia.tick2();

                    // HLA Reporter support.
                    if (_enableHlaReporter) {
                        _hlaReporter._numberOfTicks2++;
                        _hlaReporter._numberOfOtherTicks++;
                    }
                } catch (RTIexception e) {
                    throw new IllegalActionException(this, e,
                            "RTIexception: " + e.getMessage());
                }
            }

            while (!(_federateAmbassador.timeRegulator)) {
                try {
                    _rtia.tick2();

                    // HLA Reporter support.
                    if (_enableHlaReporter) {
                        _hlaReporter._numberOfTicks2++;
                        _hlaReporter._numberOfOtherTicks++;
                    }
                } catch (RTIexception e) {
                    throw new IllegalActionException(this, e,
                            "RTIexception: " + e.getMessage());
                }
            }

            if (_debugging) {
                _hlaDebug("_initializeTimeAspects() - initialize() -"
                        + " Time Management policies:" + " is Constrained = "
                        + _federateAmbassador.timeConstrained
                        + " and is Regulator = "
                        + _federateAmbassador.timeRegulator);
            }

            // The following service is required to allow the reception of
            // callbacks from the RTI when a Federate used the Time management.
            try {
                _rtia.enableAsynchronousDelivery();
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private static methods                 ////

    /* Getter functions to ease access to information stored in an object
     * array about HLA attributes to publish or to subscribe to. */

    /** Simple getter function to retrieve the TypedIOPort instance from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the instance of TypedIOPort
     */
    private static TypedIOPort _getPortFromTab(Object[] array) {
        return (TypedIOPort) array[0];
    }

    /** Simple getter function to retrieve the Type instance from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the instance of Type
     */
    static private Type _getTypeFromTab(Object[] array) {
        return (Type) array[1];
    }

    /** Simple getter function to retrieve the class object name from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the class object name as String
     */
    static private String _getClassObjectNameFromTab(Object[] array) {
        return (String) array[2];
    }

    /** Simple getter function to retrieve the class instance name from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the class instance name as String
     */
    static private String _getClassInstanceNameFromTab(Object[] array) {
        return (String) array[3];
    }

    /** Simple getter function to retrieve the class handle from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the class handle as Integer
     */
    static private Integer _getClassHandleFromTab(Object[] array) {
        return (Integer) array[4];
    }

    /** Simple getter function to retrieve the attribute handle from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the attribute handle as Integer
     */
    static private Integer _getAttributeHandleFromTab(Object[] array) {
        return (Integer) array[5];
    }

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
         *  time management and to order TSO events.
         */
        public LogicalTimeInterval effectiveLookAHead;

        /** Indicates the granted HLA logical time of the Federate. This value
         *  is set by callback by the RTI.
         */
        public LogicalTime grantedHlaLogicalTime;

        /** Indicates the current HLA logical time of the Federate. */
        public LogicalTime hlaLogicalTime;

        /** Indicates if the Federate is currently synchronize to others. This
         * value is set by callback by the RTI.
         */
        public Boolean inPause;

        /** Indicates if an RAV has been received. */
        public Boolean hasReceivedRAV;

        /** Indicates if the request of synchronization by the Federate is
         *  validated by the HLA/CERTI Federation. This value is set by callback
         *  by the RTI.
         */
        public Boolean synchronizationSuccess;

        /** Indicates if the request of synchronization by the Federate
         *  has failed. This value is set by callback by the RTI.
         */
        public Boolean synchronizationFailed;

        /** Indicates if the Federate has received the time advance grant from
         *  the HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public Boolean timeAdvanceGrant;

        /** Indicates if the Federate is declared as time constrained in the
         *  HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public Boolean timeConstrained;

        /** Indicates if the Federate is declared as time regulator in the
         *  HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public Boolean timeRegulator;

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
            _stopTime = _director.getModelStopTime();

            if (_enableHlaReporter) {
                _hlaReporter._numberOfTicks.add(0);
            }

            this.inPause = false;
            this.synchronizationSuccess = false;
            this.synchronizationFailed = false;
            this.timeAdvanceGrant = false;
            this.timeConstrained = false;
            this.timeRegulator = false;
            this.hasReceivedRAV = false;

            // Configure HlaPublisher actors from model */
            if (!_hlaAttributesToPublish.isEmpty()) {
                _setupHlaPublishers(rtia);
            } else {
                if (_debugging) {
                    _hlaDebug("INNER initialize: _hlaAttributesToPublish is empty");
                }
            }
            // Configure HlaSubscriber actors from model */
            if (!_hlaAttributesToSubscribeTo.isEmpty()) {
                _setupHlaSubscribers(rtia);
            } else {
                if (_debugging) {
                    _hlaDebug("INNER initialize: _hlaAttributesToSubscribeTo is empty");
                }
            }

        }

        /** Initialize Federate's timing properties provided by the user.
         *  @param startTime The start time of the Federate logical clock.
         *  @param timeStep The time step of the Federate.
         *  @param lookAHead The contract value used by HLA/CERTI to synchronize
         *  the Federates and to order TSO events.
         *  @exception IllegalActionException
         */
        public void initializeTimeValues(Double startTime, Double lookAHead)
                throws IllegalActionException {
            if (lookAHead <= 0) {
                throw new IllegalActionException(null, null, null,
                        "LookAhead field in HLAManager must be greater than 0.");
            }
            hlaLogicalTime = new CertiLogicalTime(startTime);
            grantedHlaLogicalTime = new CertiLogicalTime(0);

            effectiveLookAHead = new CertiLogicalTimeInterval(
                    lookAHead * _hlaTimeUnitValue);
            if (_debugging) {
                _hlaDebug("initializeTimeValues() - Effective HLA lookahead is "
                        + effectiveLookAHead.toString());
            }
            timeAdvanceGrant = false;

        }

        // HLA Object Management services (callbacks).

        /** Callback to receive updated value of a HLA attribute from the
         *  whole Federation (delivered by the RTI (CERTI)).
         */
        @Override
        public void reflectAttributeValues(int theObject,
                ReflectedAttributes theAttributes, byte[] userSuppliedTag,
                LogicalTime theTime, EventRetractionHandle retractionHandle)
                throws ObjectNotKnown, AttributeNotKnown,
                FederateOwnsAttributes, InvalidFederationTime,
                FederateInternalError {

            if (_debugging) {
                _hlaDebug("      t_ptII = " + _director.getModelTime()
                        + "; t_hla = " + _federateAmbassador.hlaLogicalTime
                        + " start reflectAttributeValues(), INNER callback");
            }

            // Get the object class handle corresponding to
            // the received "theObject" id.
            int classHandle = _objectIdToClassHandle.get(theObject);
            String classInstanceOrJokerName = _discoverObjectInstanceMap
                    .get(theObject);

            for (int i = 0; i < theAttributes.size(); i++) {

                Iterator<Entry<String, Object[]>> ot = _hlaAttributesToSubscribeTo
                        .entrySet().iterator();

                while (ot.hasNext()) {
                    Map.Entry<String, Object[]> elt = ot.next();
                    Object[] tObj = elt.getValue();

                    Time ts = null;
                    TimedEvent te = null;
                    Object value = null;
                    HlaSubscriber hs = (HlaSubscriber) _getPortFromTab(tObj)
                            .getContainer();

                    /*
                    _hlaDebugSys("INNER callback: reflectAttributeValues():"
                            + " theObject=" + theObject
                            + " theAttributes" + theAttributes
                            + " userSuppliedTag=" + userSuppliedTag
                            + " theTime=" + theTime
                            + " classHandle=" + classHandle
                            + " classInstanceOrJokerName=" + classInstanceOrJokerName
                            + " HlaSusbcriber=" + hs.getFullName());
                    */

                    // The tuple (attributeHandle, classHandle, classInstanceName)
                    // allows to identify the object attribute (i.e. one of the HlaSubscribers)
                    // where the updated value has to be put.
                    try {
                        if (theAttributes.getAttributeHandle(i) == hs
                                .getAttributeHandle()
                                && classHandle == hs.getClassHandle()
                                && (classInstanceOrJokerName != null
                                        && hs.getClassInstanceName().compareTo(
                                                classInstanceOrJokerName) == 0)) {

                            double timeValue = ((CertiLogicalTime) theTime)
                                    .getTime() / _hlaTimeUnitValue;

                            ts = new Time(_director, timeValue);

                            // Note: Sometimes a received RAV value is different than the UAV value sent.
                            // This could come from the decodeHlaValue and encodeHlaValue CERTI methods.
                            value = MessageProcessing.decodeHlaValue(hs,
                                    _getTypeFromTab(tObj),
                                    theAttributes.getValue(i));

                            te = new HlaTimedEvent(ts, new Object[] {
                                    (BaseType) _getTypeFromTab(tObj), value },
                                    theObject);

                            _fromFederationEvents.get(hs.getFullName()).add(te);

                            if (_debugging) {
                                _hlaDebug("       *RAV '" + hs.getAttributeName()
                                        + "', timestamp="
                                        + te.timeStamp.toString() + ",value="
                                        + value.toString() + " @ "
                                        + hs.getFullName());
                            }

                            // Notify RAV reception.
                            hasReceivedRAV = true;

                            if (_enableHlaReporter) {
                                _hlaReporter.updateRAVsInformation(hs,
                                        (HlaTimedEvent) te,
                                        _director.getModelTime(),
                                        _hlaAttributesToSubscribeTo, value);
                                _hlaReporter.incrNumberOfRAVs();
                            }
                        }
                    } catch (ArrayIndexOutOfBounds e) {
                        // Java classic exceptions are encapsulated as FederateInternalError to avoid system prints.
                        //_hlaDebugSys(
                        //        "INNER callback: reflectAttributeValues(): EXCEPTION ArrayIndexOutOfBounds");
                        //e.printStackTrace();

                        throw new FederateInternalError(
                                "INNER callback: reflectAttributeValues(): EXCEPTION ArrayIndexOutOfBounds: "
                                        + e.getMessage());
                    } catch (IllegalActionException e) {
                        // Java classic exceptions are encapsulated as FederateInternalError to avoid system prints.
                        //_hlaDebugSys(
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
         *  of HLA attribute that the Federate is subscribed to.
         */
        @Override
        public void discoverObjectInstance(int objectInstanceId,
                int classHandle, String someName) throws CouldNotDiscover,
                ObjectClassNotKnown, FederateInternalError {
            // Joker support.
            String matchingName = null;

            if (_usedJoker) {
                String jokerFilter = null;

                // Find a valid non-used joker filter.
                Iterator<Entry<String, Boolean>> it1 = _usedJokerFilterMap
                        .entrySet().iterator();

                while (it1.hasNext()) {
                    Map.Entry<String, Boolean> elt = it1.next();
                    // elt.getKey()   => joker filter.
                    // elt.getValue() => joker is already used or not (boolean).
                    if (!elt.getValue().booleanValue()) {
                        jokerFilter = elt.getKey();
                        _usedJokerFilterMap.put(jokerFilter, true);
                        if (_debugging) {
                            _hlaDebug("INNER callback: discoverObjectInstance: found a free joker, break with jokerFilter="
                                    + jokerFilter);
                        }
                        break;
                    }
                }

                if (jokerFilter == null) {
                    if (_debugging) {
                        _hlaDebug("INNER callback: discoverObjectInstance: no more filter available.\n"
                                + " objectInstanceId=" + objectInstanceId
                                + " classHandle=" + classHandle + " someName="
                                + someName
                                + " will be ignored during the simulation.");
                    }
                } else {
                    _discoverObjectInstanceMap.put(objectInstanceId,
                            jokerFilter);
                    if (_debugging) {
                        _hlaDebug("INNER callback: discoverObjectInstance: objectInstanceId="
                                + objectInstanceId + " jokerFilter="
                                + jokerFilter + " matchingName="
                                + matchingName);
                    }

                    matchingName = jokerFilter;
                }
            } else {
                // Nominal case, class instance name usage.
                if (_discoverObjectInstanceMap.containsKey(objectInstanceId)) {
                    if (_debugging) {
                        _hlaDebug("INNER callback: discoverObjectInstance: found an instance class already registered: "
                                + someName);
                    }
                    // Note: this case should not happen with the new implementation from CIELE. But as it is
                    // difficult to test this cas, we raise an exception.
                    throw new FederateInternalError(
                            "INNER callback: discoverObjectInstance(): EXCEPTION IllegalActionException: "
                                    + "found an instance class already registered: "
                                    + someName);

                } else {
                    _discoverObjectInstanceMap.put(objectInstanceId, someName);
                    matchingName = someName;
                }

            }

            // Bind object instance id to class handle.
            _objectIdToClassHandle.put(objectInstanceId, classHandle);

            // Joker support
            if (matchingName != null) {
                // Get classHandle and attributeHandle IDs for each attribute
                // value to subscribe to (i.e. HlaSubscriber). Update the HlaSubcribers.
                Iterator<Entry<String, Object[]>> it1 = _hlaAttributesToSubscribeTo
                        .entrySet().iterator();

                while (it1.hasNext()) {
                    Map.Entry<String, Object[]> elt = it1.next();
                    // elt.getKey()   => HlaSubscriber actor full name.
                    // elt.getValue() => tObj[] array.
                    Object[] tObj = elt.getValue();

                    // Get corresponding HlaSubscriber actor.
                    HlaSubscriber sub = (HlaSubscriber) ((TypedIOPort) tObj[0])
                            .getContainer();
                    try {
                        if (sub.getClassInstanceName()
                                .compareTo(matchingName) == 0) {
                            sub.setObjectInstanceId(objectInstanceId);

                            if (_debugging) {
                                _hlaDebug("INNER callback: discoverObjectInstance: matchingName="
                                        + matchingName + " hlaSub="
                                        + sub.getFullName());
                            }

                        }
                    } catch (IllegalActionException e) {
                        throw new FederateInternalError(
                                "INNER callback: discoverObjectInstance(): EXCEPTION IllegalActionException: "
                                        + "cannot retrieve HlaSubscriber actor class instance name.");
                    }
                }
            }

            if (_debugging) {
                _hlaDebug("INNER callback:"
                        + " discoverObjectInstance(): the object"
                        + " objectInstanceId=" + objectInstanceId
                        + " classHandle=" + classHandle
                        + " classIntanceOrJokerName=" + someName);
            }
        }

        // HLA Time Management services (callbacks).

        /** Callback delivered by the RTI (CERTI) to validate that the Federate
         *  is declared as time-regulator in the HLA Federation.
         */
        @Override
        public void timeRegulationEnabled(LogicalTime theFederateTime)
                throws InvalidFederationTime, EnableTimeRegulationWasNotPending,
                FederateInternalError {
            timeRegulator = true;
            if (_debugging) {
                _hlaDebug("INNER callback:"
                        + " timeRegulationEnabled(): timeRegulator = "
                        + timeRegulator);
            }
        }

        /** Callback delivered by the RTI (CERTI) to validate that the Federate
         *  is declared as time-constrained in the HLA Federation.
         */
        @Override
        public void timeConstrainedEnabled(LogicalTime theFederateTime)
                throws InvalidFederationTime,
                EnableTimeConstrainedWasNotPending, FederateInternalError {
            timeConstrained = true;
            if (_debugging) {
                _hlaDebug("INNER callback:"
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
                throws InvalidFederationTime, TimeAdvanceWasNotInProgress,
                FederateInternalError {

            grantedHlaLogicalTime = theTime;
            timeAdvanceGrant = true;

            // HLA Reporter support.
            if (_enableHlaReporter) {
                double delay = (System.nanoTime()
                        - _hlaReporter.getTimeOfTheLastAdvanceRequest())
                        / Math.pow(10, 9);

                // Reset time for last advance request (NER or TAG).
                _hlaReporter.setTimeOfTheLastAdvanceRequest(Integer.MIN_VALUE);

                // Compute elapsed time spent between latest TAR or NER and this received TAG.
                _hlaReporter._TAGDelay.add(delay);

                // As a new TAG has been received add and set is tick() counter to 0.
                _hlaReporter._numberOfTicks.add(0);

                // Increment TAG counter.
                _hlaReporter._numberOfTAGs++;
            }

            if (_debugging) {
                _hlaDebug("  TAG(" + grantedHlaLogicalTime.toString()
                        + " * (HLA time unit=" + _hlaTimeUnitValue
                        + ")) received in INNER callback: timeAdvanceGrant()");
            }
        }

        // HLA Federation Management services (callbacks).
        // Synchronization point services.

        /** Callback delivered by the RTI (CERTI) to notify if the synchronization
         *  point registration has failed.
         */
        @Override
        public void synchronizationPointRegistrationFailed(
                String synchronizationPointLabel) throws FederateInternalError {
            synchronizationFailed = true;
            if (_debugging) {
                _hlaDebug("INNER callback: synchronizationPointRegistrationFailed(): "
                        + "synchronizationFailed = " + synchronizationFailed);
            }
        }

        /** Callback delivered by the RTI (CERTI) to notify if the synchronization
         *  point registration has succeed.
         */
        @Override
        public void synchronizationPointRegistrationSucceeded(
                String synchronizationPointLabel) throws FederateInternalError {
            synchronizationSuccess = true;
            if (_debugging) {
                _hlaDebug("INNER callback: synchronizationPointRegistrationSucceeded(): "
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
            inPause = true;
            if (_debugging) {
                _hlaDebug("INNER callback: announceSynchronizationPoint(): inPause = "
                        + inPause);
            }
        }

        /** Callback delivered by the RTI (CERTI) to notify that the Federate is
         *  synchronized to others Federates using the same synchronization point
         *  in the HLA Federation.
         */
        @Override
        public void federationSynchronized(String synchronizationPointLabel)
                throws FederateInternalError {
            inPause = false;
            if (_debugging) {
                _hlaDebug("INNER callback: federationSynchronized(): inPause = "
                        + inPause + "\n");
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /** Configure the deployed HLA publishers.
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
        private void _setupHlaPublishers(RTIambassador rtia)
                throws ObjectClassNotDefined, FederateNotExecutionMember,
                RTIinternalError, SaveInProgress, RestoreInProgress,
                ConcurrentAccessAttempted, IllegalActionException {

            // For each HlaPublisher actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to publish.

            // 1. Get classHandle and attributeHandle IDs for each attribute
            //    value to publish (i.e. HlaPublisher). Update the HlaPublishers
            //    table with the information.
            Iterator<Entry<String, Object[]>> it = _hlaAttributesToPublish
                    .entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Object[]> elt = it.next();
                // elt.getKey()   => HlaPublisher actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // Get corresponding HlaPublisher actor.
                HlaPublisher pub = (HlaPublisher) _getPortFromTab(tObj)
                        .getContainer();

                if (_debugging) {
                    _hlaDebug("_setupHlaPublishers() - HlaPublisher: "
                            + pub.getFullName());
                }

                // Object class handle and attribute handle are IDs that
                // allow to identify an HLA attribute.

                // Retrieve HLA class handle from RTI.
                int classHandle = Integer.MIN_VALUE;

                try {
                    classHandle = rtia
                            .getObjectClassHandle(pub.getClassObjectName());

                    if (_debugging) {
                        _hlaDebug("_setupHlaPublishers() "
                                + "objectClassName (in FOM) = "
                                + pub.getClassObjectName() + " - classHandle = "
                                + classHandle);
                    }
                } catch (NameNotFound e) {
                    throw new IllegalActionException(null, e, "NameNotFound: "
                            + e.getMessage()
                            + " is not a HLA class from the FOM (see .fed file).");
                }

                // Retrieve HLA attribute handle from RTI.
                int attributeHandle = Integer.MIN_VALUE;
                try {
                    attributeHandle = rtia.getAttributeHandle(
                            pub.getAttributeName(), classHandle);

                    if (_debugging) {
                        _hlaDebug("_setupHlaPublishers() " + " attributeHandle = "
                                + attributeHandle);
                    }
                } catch (NameNotFound e) {
                    throw new IllegalActionException(null, e, "NameNotFound: "
                            + e.getMessage()
                            + " is not a HLA attribute value from the FOM (see .fed file).");
                }

                // Update HLA attribute information (for publication)
                // from HLA services. In this case, the tObj[] object as
                // the following structure:
                // tObj[0] => input port which receives the token to transform
                //            as an updated value of a HLA attribute,
                // tObj[1] => type of the port (e.g. of the attribute),
                // tObj[2] => object class name of the attribute,
                // tObj[3] => instance class name
                // tObj[4] => ID of the object class to handle,
                // tObj[5] => ID of the attribute to handle

                // tObj[0 .. 3] are extracted from the Ptolemy model.
                // tObj[3 .. 5] are provided by the RTI (CERTI).

                // All these information are required to publish/unpublish
                // updated value of a HLA attribute.
                elt.setValue(new Object[] { _getPortFromTab(tObj),
                        _getTypeFromTab(tObj), _getClassObjectNameFromTab(tObj),
                        _getClassInstanceNameFromTab(tObj), classHandle,
                        attributeHandle });
            }

            // 2.1 Create a table of HlaPublishers indexed by their corresponding
            //     classInstanceName (no duplication).
            HashMap<String, LinkedList<String>> classInstanceNameHlaPublisherTable = new HashMap<String, LinkedList<String>>();

            Iterator<Entry<String, Object[]>> it21 = _hlaAttributesToPublish
                    .entrySet().iterator();

            while (it21.hasNext()) {
                Map.Entry<String, Object[]> elt = it21.next();
                // elt.getKey()   => HlaPublisher actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // Get corresponding HlaPublisher actor.
                HlaPublisher pub = (HlaPublisher) _getPortFromTab(tObj)
                        .getContainer();
                String classInstanceName = pub.getClassInstanceName();

                if (classInstanceNameHlaPublisherTable
                        .containsKey(classInstanceName)) {
                    classInstanceNameHlaPublisherTable.get(classInstanceName)
                            .add(elt.getKey());
                } else {
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(elt.getKey());
                    classInstanceNameHlaPublisherTable.put(classInstanceName,
                            list);
                }
            }

            // 2.2 Create a table of HlaPublishers indexed by their corresponding
            //     class handle (no duplication).
            HashMap<Integer, LinkedList<String>> classHandleHlaPublisherTable = new HashMap<Integer, LinkedList<String>>();

            Iterator<Entry<String, Object[]>> it22 = _hlaAttributesToPublish
                    .entrySet().iterator();

            while (it22.hasNext()) {
                Map.Entry<String, Object[]> elt = it22.next();
                // elt.getKey()   => HlaPublisher actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                int classHandle = _getClassHandleFromTab(tObj);

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
            Iterator<Entry<Integer, LinkedList<String>>> it3 = classHandleHlaPublisherTable
                    .entrySet().iterator();

            while (it3.hasNext()) {
                Map.Entry<Integer, LinkedList<String>> elt = it3.next();
                // elt.getKey()   => HLA class instance name.
                // elt.getValue() => list of HlaPublisher actor full names.
                LinkedList<String> hlaPublishersFullnames = elt.getValue();

                // The attribute handle set to declare all attributes to publish
                // for one object class.
                AttributeHandleSet _attributesLocal = RtiFactoryFactory
                        .getRtiFactory().createAttributeHandleSet();

                // Fill the attribute handle set with all attribute to publish.
                for (String sPub : hlaPublishersFullnames) {
                    try {
                        _attributesLocal.add(_getAttributeHandleFromTab(
                                _hlaAttributesToPublish.get(sPub)));
                    } catch (AttributeNotDefined e) {
                        throw new IllegalActionException(null, e,
                                "AttributeNotDefined: " + e.getMessage());
                    }
                }

                // At this point, all HlaPublishers have been initialized and own their
                // corresponding HLA class handle and HLA attribute handle. Just retrieve
                // the first from the list to get those information.
                Object[] tObj = _hlaAttributesToPublish
                        .get(hlaPublishersFullnames.getFirst());
                int classHandle = _getClassHandleFromTab(tObj);

                // Declare to the Federation the HLA attribute(s) to publish.
                try {
                    rtia.publishObjectClass(classHandle, _attributesLocal);

                    if (_debugging) {
                        _hlaDebug("_setupHlaPublishers() - Publish Object Class: "
                                + " classHandle = " + classHandle
                                + " _attributesLocal = "
                                + _attributesLocal.toString());
                    }
                } catch (OwnershipAcquisitionPending e) {
                    throw new IllegalActionException(null, e,
                            "OwnershipAcquisitionPending: " + e.getMessage());
                } catch (AttributeNotDefined e) {
                    throw new IllegalActionException(null, e,
                            "AttributeNotDefined: " + e.getMessage());
                }
            }

            // 4. Register object instances. Only one registerObjectInstance() call is performed
            //    by class instance (name). Finally, update the hash map of class instance name
            //    with the returned object instance ID.
            Iterator<Entry<String, LinkedList<String>>> it4 = classInstanceNameHlaPublisherTable
                    .entrySet().iterator();

            while (it4.hasNext()) {
                Map.Entry<String, LinkedList<String>> elt = it4.next();
                // elt.getKey()   => HLA class instance name.
                // elt.getValue() => list of HlaPublisher actor full names.
                LinkedList<String> hlaPublishersFullnames = elt.getValue();

                // At this point, all HlaPublishers on the list have been initialized
                // and own their class handle and class instance name. Just retrieve
                // the first from the list to get those information.
                Object[] tObj = _hlaAttributesToPublish
                        .get(hlaPublishersFullnames.getFirst());

                int classHandle = _getClassHandleFromTab(tObj);
                String classInstanceName = _getClassInstanceNameFromTab(tObj);

                if (!_registerObjectInstanceMap
                        .containsKey(classInstanceName)) {
                    int objectInstanceId = -1;
                    try {
                        objectInstanceId = rtia.registerObjectInstance(
                                classHandle, classInstanceName);

                        if (_debugging) {
                            _hlaDebug("_setupHlaPublishers() - Register Object Instance: "
                                    + " classHandle = " + classHandle
                                    + " classIntanceName = " + classInstanceName
                                    + " objectInstanceId = "
                                    + objectInstanceId);
                        }

                        _registerObjectInstanceMap.put(classInstanceName,
                                objectInstanceId);
                    } catch (ObjectClassNotPublished e) {
                        throw new IllegalActionException(null, e,
                                "ObjectClassNotPublished: " + e.getMessage());
                    } catch (ObjectAlreadyRegistered e) {
                        throw new IllegalActionException(null, e,
                                "ObjectAlreadyRegistered: " + e.getMessage());
                    } // end catch ...
                } // end if ...
            }
        }

        /** Configure the deployed HLA subscribers.
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
        private void _setupHlaSubscribers(RTIambassador rtia)
                throws ObjectClassNotDefined, FederateNotExecutionMember,
                RTIinternalError, SaveInProgress, RestoreInProgress,
                ConcurrentAccessAttempted, IllegalActionException {
            // XXX: FIXME: check mixing between tObj[] and HlaSubcriber getter/setter.

            // For each HlaSubscriber actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to subscribe to.

            // 1. Get classHandle and attributeHandle IDs for each attribute
            // value to subscribe (i.e. HlaSubcriber). Update the HlaSubcribers.
            Iterator<Entry<String, Object[]>> it1 = _hlaAttributesToSubscribeTo
                    .entrySet().iterator();

            while (it1.hasNext()) {
                Map.Entry<String, Object[]> elt = it1.next();
                // elt.getKey()   => HlaSubscriber actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // Get corresponding HlaSubscriber actor.
                HlaSubscriber sub = (HlaSubscriber) ((TypedIOPort) tObj[0])
                        .getContainer();

                if (_debugging) {
                    _hlaDebug("_setupHlaSubscribers() - HlaSubscriber: "
                            + sub.getFullName());
                }

                // Object class handle and attribute handle are IDs that
                // allow to identify an HLA attribute.

                // Retrieve HLA class handle from RTI.
                int classHandle = Integer.MIN_VALUE;

                try {
                    classHandle = rtia.getObjectClassHandle(
                            _getClassObjectNameFromTab(tObj));

                    if (_debugging) {
                        _hlaDebug("_setupHlaSubscribers() "
                                + "objectClassName (in FOM) = "
                                + _getClassObjectNameFromTab(tObj)
                                + " - classHandle = " + classHandle);
                    }
                } catch (NameNotFound e) {
                    throw new IllegalActionException(null, e, "NameNotFound: "
                            + e.getMessage()
                            + " is not a HLA class from the FOM (see .fed file).");
                }

                // Retrieve HLA attribute handle from RTI.
                int attributeHandle = Integer.MIN_VALUE;
                try {
                    attributeHandle = rtia.getAttributeHandle(
                            sub.getAttributeName(), classHandle);

                    if (_debugging) {
                        _hlaDebug("_setupHlaSubscribers() " + " attributeHandle = "
                                + attributeHandle);
                    }
                } catch (NameNotFound e) {
                    throw new IllegalActionException(null, e, "NameNotFound: "
                            + e.getMessage()
                            + " is not a HLA attribute value from the FOM (see .fed file).");
                }

                // Subscribe to HLA attribute information (for subscription)
                // from HLA services. In this case, the tObj[] object as
                // the following structure:
                // tObj[0] => input port which receives the token to transform
                //            as an updated value of a HLA attribute,
                // tObj[1] => type of the port (e.g. of the attribute),
                // tObj[2] => object class name of the attribute,
                // tObj[3] => instance class name
                // tObj[4] => ID of the object class to handle,
                // tObj[5] => ID of the attribute to handle

                // tObj[0 .. 3] are extracted from the Ptolemy model.
                // tObj[3 .. 5] are provided by the RTI (CERTI).

                // All these information are required to subscribe/unsubscribe
                // updated value of a HLA attribute.
                elt.setValue(new Object[] { _getPortFromTab(tObj),
                        _getTypeFromTab(tObj), _getClassObjectNameFromTab(tObj),
                        _getClassInstanceNameFromTab(tObj), classHandle,
                        attributeHandle });

                sub.setClassHandle(classHandle);
                sub.setAttributeHandle(attributeHandle);
            }

            // 2. Create a table of HlaSubscribers indexed by their corresponding
            //    class handle (no duplication).
            HashMap<Integer, LinkedList<String>> classHandleHlaSubscriberTable = null;
            classHandleHlaSubscriberTable = new HashMap<Integer, LinkedList<String>>();

            Iterator<Entry<String, Object[]>> it22 = _hlaAttributesToSubscribeTo
                    .entrySet().iterator();

            while (it22.hasNext()) {
                Map.Entry<String, Object[]> elt = it22.next();
                // elt.getKey()   => HlaSubscriber actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // The class handle where the HLA attribute belongs to (see FOM).
                int classHandle = _getClassHandleFromTab(tObj);

                if (classHandleHlaSubscriberTable.containsKey(classHandle)) {
                    classHandleHlaSubscriberTable.get(classHandle)
                            .add(elt.getKey());
                } else {
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(elt.getKey());
                    classHandleHlaSubscriberTable.put(classHandle, list);
                }
            }

            // 3. Declare to the Federation the HLA attributes to subscribe to.
            // If these attributes belongs to the same object class then only
            // one subscribeObjectClass() call is performed.
            Iterator<Entry<Integer, LinkedList<String>>> it3 = classHandleHlaSubscriberTable
                    .entrySet().iterator();

            while (it3.hasNext()) {
                Map.Entry<Integer, LinkedList<String>> elt = it3.next();
                // elt.getKey()   => HLA class instance name.
                // elt.getValue() => list of HlaSubscriber actor full names.
                LinkedList<String> hlaSubscribersFullnames = elt.getValue();

                // The attribute handle set to declare all subscribed attributes
                // for one object class.
                AttributeHandleSet _attributesLocal = RtiFactoryFactory
                        .getRtiFactory().createAttributeHandleSet();

                for (String sSub : hlaSubscribersFullnames) {
                    try {
                        _attributesLocal.add(_getAttributeHandleFromTab(
                                _hlaAttributesToSubscribeTo.get(sSub)));
                    } catch (AttributeNotDefined e) {
                        throw new IllegalActionException(null, e,
                                "AttributeNotDefined: " + e.getMessage());
                    }
                }

                // At this point, all HlaSubscribers have been initialized and own their
                // corresponding HLA class handle and HLA attribute handle. Just retrieve
                // the first from the list to get those information.
                Object[] tObj = _hlaAttributesToSubscribeTo
                        .get(hlaSubscribersFullnames.getFirst());
                int classHandle = _getClassHandleFromTab(tObj);
                try {
                    _rtia.subscribeObjectClassAttributes(classHandle,
                            _attributesLocal);
                } catch (AttributeNotDefined e) {
                    throw new IllegalActionException(null, e,
                            "AttributeNotDefined: " + e.getMessage());
                }

                if (_debugging) {
                    _hlaDebug("_setupHlaSubscribers() - Subscribe Object Class Attributes: "
                            + " classHandle = " + classHandle
                            + " _attributesLocal = "
                            + _attributesLocal.toString());
                }
            }
        } // end 'private void setupHlaSubscribers(RTIambassador rtia) ...'
    } // end 'private class PtolemyFederateAmbassadorInner extends NullFederateAmbassador { ...'
}
