/* This attribute implements a HLA Manager to cooperate with a HLA/CERTI Federation.

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

package ptolemy.apps.hlacerti.lib;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotKnown;
import hla.rti.ConcurrentAccessAttempted;
import hla.rti.CouldNotDiscover;
import hla.rti.EnableTimeConstrainedPending;
import hla.rti.EnableTimeConstrainedWasNotPending;
import hla.rti.EnableTimeRegulationPending;
import hla.rti.EnableTimeRegulationWasNotPending;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TimeRegulator;
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
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import certi.communication.MessageBuffer;
import certi.rti.impl.CertiLogicalTime;
import certi.rti.impl.CertiLogicalTimeInterval;
import certi.rti.impl.CertiRtiAmbassador;

///////////////////////////////////////////////////////////////////
//// HlaManager

/**
 * <p>This attribute implements a HLA Manager which allows a Ptolemy model to
 * cooperate with a HLA/CERTI Federation. The main is to allow a Ptolemy
 * simulation as Federate of a Federation.
 * <p></p>
 * The High Level Architecture (HLA) [1][2] is a standard for distributed
 * discrete-event simulations. A complex simulation in HLA is called a HLA
 * Federation. A Federation is a collection of Federates (e.g. simpler simula-
 * -tors), each performing a sequence of computations, interconnected by a Run
 * Time Infrastructure (RTI).
 * </p><p>
 * CERTI is an Open-Source middleware RTI compliant with HLA [NRS09] which
 * manages every part of federation. It also ensures a real-time behavior of
 * a federation execution. CERTI is implemented in C++ and bindings are
 * provided as JCERTI for Java and PyHLA for Python. For more information see:
 * <br><a href="http://savannah.nongnu.org/projects/certi" target="_top">http://savannah.nongnu.org/projects/certi</a></br>
 * </p><p>
 * The {@link HlaManager} attribute handles the time synchronization between
 * Ptolemy model time and HLA logical time by implementing the {@link TimeRegulator}
 * interface. It also manages objects that implement interfaces provided by
 * JCERTI relatives to Federation, Declaration, Object and Time management
 * areas in HLA (each management areas provides a set of services).
 * </p><p>
 * To develop a HLA Federation it is required to specify a Federate Object
 * Model (FOM) which describes the architecture of the Federation (HLA version,
 * name of Federates which belong to, shared HLA attributes) and the interaction
 * between Federates and shared attributes. Data exchanged in a HLA Federation
 * are called HLA attributes and their interaction mechanism is based on the
 * publish/subscribe paradigm. The FOM is specified in a .fed file used by
 * the RTI (e.g. by the RTIG process when using CERTI). More information in [3].
 * <br><a href="http://savannah.nongnu.org/projects/certi" target="_top">http://savannah.nongnu.org/projects/certi</a></br>
 * </p><p>
 * To enable a Ptolemy model as a Federate, the {@link HlaManager} has to be
 * deployed and configured (by double-clicking on the attribute).
 * Parameters <i>federateName</i>, <i>federationName</i> have to match the
 * declaration in the FOM (.fed file). <i>fedFile</i> specifies the FOM file and
 * its path.
 * </p><p>
 * Parameters <i>useNextEventRequest</i>, <i>UseTimeAdvanceRequest</i>,
 * <i>isTimeConstrained</i> and <i>isTimeRegulator</i> are
 * used to configure the HLA time management services of the Federate. A
 * Federate can only specify the use of the <i>nextEventRequest()
 * service</i> or the <i>timeAdvanceRequest()</i> service at a time.
 * <i>istimeConstrained</i> is used to specify time-constrained Federate and
 * <i>istimeRegulator</i> to specify time-regulator Federate. The combination of
 * both parameters is possible and is recommended.
 * </p><p>
 * Parameters <i>hlaStartTime</i>, <i>hlaStepTime</i> and <i>hlaLookAHead</i>
 * are used to specify Hla Timing attributes of a Federate.
 * </p><p>
 * Parameters <i>requireSynchronization</i>, <i>synchronizationPointName</i>
 * and <i>isCreatorSyncPt</i> are used to configure HLA synchronization point.
 * This mechanism is usually used to synchronize the Federates, during their
 * initialization, to avoid that Federates that only consume some HLA
 * attributes finished their simulation before the other federates have started.
 * <i>isCreatorSyncPt</i> indicates if the Federate is the creator of the
 * synchronization. Only one Federate can create the named synchronization
 * point for the whole HLA Federation.
 * </p><p>
 * {@link HlaPublisher} and {@link HlaSubscriber} actors are used to
 * respectively publish and subscribe to HLA attributes. The name of those
 * actors and their <i>classObjectHandle</i> parameter have to match the
 * identifier of the shared HLA attributes and their object class that they
 * belong to, specified in the FOM (.fed file).
 * </p><p>
 * For a correct execution, the <i>CERTI_HOME</i> environment variable has to
 * be set. It could be set in the shell (by running one of the scripts provided
 * by CERTI) where Vergil is executed, or as a parameter of the Ptolemy model
 * or as a parameter of the {@link HlaManager}:
 * </p><pre>
 * CERTI_HOME="/absolute/path/to/certi/"
 * </pre><p>
 * Otherwise, the current implementation is not able to find the CERTI
 * environment, the RTIG binary and to perform its execution. See also
 * the {@link CertiRtig} class.
 * </p><p>
 * NOTE: For a correct behavior CERTI has to be compiled with the option
 * "CERTI_USE_NULL_PRIME_MESSAGE_PROTOCOL"
 * </p>
 *
 * <b>References</b>:
 * <br>
 * [1] Dpt. of Defense (DoD) Specifications, "High Level Architecture Interface
 *     Specification, Version 1.3", DOD/DMSO HLA IF 1.3, Tech. Rep., Apr 1998.
 * [2] IEEE, "IEEE standard for modeling and simulation High Level Architecture
 *     (HLA)", IEEE Std 1516-2010, vol. 18, pp. 1-38, 2010.
 * [3] D. of Defense (DoD) Specifications, "High Level Architecture Object Model
 *     Template, Version 1.3", DOD/DMSO OMT 1.3, Tech. Rep., Feb 1998.
 * [4] E. Noulard, J.-Y. Rousselot, and P. Siron, "CERTI, an open source RTI,
 *     why and how ?", Spring Simulation Interoperability Workshop, pp. 23-27,
 *     Mar 2009.
 *
 *  @author Gilles Lasnier, Contributors: Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaManager extends AbstractInitializableAttribute implements
        TimeRegulator {

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

        //_lastProposedTime = null;
        _rtia = null;
        _federateAmbassador = null;

        _hlaAttributesToPublish = new HashMap<String, Object[]>();
        _hlaAttributesSubscribedTo = new HashMap<String, Object[]>();
        _fromFederationEvents = new HashMap<String, LinkedList<TimedEvent>>();
        _objectIdToClassHandle = new HashMap<Integer, Integer>();

        _hlaStartTime = null;
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
        eventBased = new Parameter(this, "eventBased");
        eventBased.setTypeEquals(BaseType.BOOLEAN);
        eventBased.setExpression("true");
        eventBased.setDisplayName("event-based federate");
        attributeChanged(eventBased);

        timeStepped = new Parameter(this, "timeStepped");
        timeStepped.setTypeEquals(BaseType.BOOLEAN);
        timeStepped.setExpression("false");
        timeStepped.setDisplayName("time-stepped federate");
        attributeChanged(timeStepped);

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

        hlaStartTime = new Parameter(this, "hlaStartTime");
        hlaStartTime.setDisplayName("logical start time (in ms)");
        hlaStartTime.setExpression("0.0");
        hlaStartTime.setTypeEquals(BaseType.DOUBLE);
        attributeChanged(hlaStartTime);

        hlaTimeStep = new Parameter(this, "hlaTimeStep");
        hlaTimeStep.setDisplayName("time step (in ms)");
        hlaTimeStep.setExpression("0.0");
        hlaTimeStep.setTypeEquals(BaseType.DOUBLE);
        hlaTimeStep.setVisibility(Settable.NOT_EDITABLE);
        attributeChanged(hlaTimeStep);

        hlaLookAHead = new Parameter(this, "hlaLookAHead");
        hlaLookAHead.setDisplayName("lookahead (in ms)");
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
        isCreator.setDisplayName("Is synchronization point creator ?");
        attributeChanged(isCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public variables                     ////

    /** Name of the Ptolemy Federate. This parameter must contain an
     *  StringToken. */
    public Parameter federateName;

    /** Name of the federation. This parameter must contain an StringToken. */
    public Parameter federationName;

    /** Path and name of the Federate Object Model (FOM) file. This parameter
     *  must contain an StringToken. */
    public FileParameter fedFile;

    /** Boolean value, 'true' if the Federate requires the use of the
     *  event-based HLA services (NER or NERA). This parameter must contain an
     *  BooleanToken. */
    public Parameter eventBased;

    /** Boolean value, 'true' if the Federate requires the use of the
     *  time-stepped HLA services (TAR or TARA). This parameter must contain an
     *  BooleanToken. */
    public Parameter timeStepped;

    /** Boolean value, 'true' if the Federate is declared time constrained
     *  'false' if not. This parameter must contain an BooleanToken. */
    public Parameter isTimeConstrained;

    /** Boolean value, 'true' if the Federate is declared time regulator
     *  'false' if not. This parameter must contain an BooleanToken. */
    public Parameter isTimeRegulator;

    /** Value of the start time of the Federate. This parameter must contain
     *  an DoubleToken. */
    public Parameter hlaStartTime;

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

    /** Boolean value, 'true' if the Federate is the creator of the
     *  synchronization point 'false' if not. This parameter must contain
     *  an BooleanToken. */
    public Parameter isCreator;

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
            setDisplayName(value);
        } else if (attribute == federationName) {
            String value = ((StringToken) federationName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
            _federationName = value;
        } else if (attribute == eventBased) {
            _eventBased = ((BooleanToken) eventBased.getToken()).booleanValue();

        } else if (attribute == timeStepped) {
            _timeStepped = ((BooleanToken) timeStepped.getToken())
                    .booleanValue();

        } else if (attribute == isTimeConstrained) {
            _isTimeConstrained = ((BooleanToken) isTimeConstrained.getToken())
                    .booleanValue();

        } else if (attribute == isTimeRegulator) {
            _isTimeRegulator = ((BooleanToken) isTimeRegulator.getToken())
                    .booleanValue();

        } else if (attribute == hlaStartTime) {
            Double value = ((DoubleToken) hlaStartTime.getToken())
                    .doubleValue();
            if (value < 0) {
                throw new IllegalActionException(this,
                        "Cannot have negative value !");
            }
            _hlaStartTime = value;
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
        newObject._hlaAttributesSubscribedTo = new HashMap<String, Object[]>();
        newObject._fromFederationEvents = new HashMap<String, LinkedList<TimedEvent>>();
        newObject._objectIdToClassHandle = new HashMap<Integer, Integer>();

        newObject._rtia = null;
        newObject._federateAmbassador = null;
        newObject._federateName = _federateName;
        newObject._federationName = _federationName;
        newObject._isTimeConstrained = _isTimeConstrained;
        newObject._isTimeRegulator = _isTimeRegulator;
        try {
            newObject._hlaStartTime = ((DoubleToken) hlaStartTime.getToken())
                    .doubleValue();
            newObject._hlaTimeStep = ((DoubleToken) hlaTimeStep.getToken())
                    .doubleValue();
            newObject._hlaLookAHead = ((DoubleToken) hlaLookAHead.getToken())
                    .doubleValue();
        } catch (IllegalActionException ex) {
            CloneNotSupportedException ex2 = new CloneNotSupportedException(
                    "Failed to get a token.");
            ex2.initCause(ex);
            throw ex2;
        }
        newObject._requireSynchronization = _requireSynchronization;
        newObject._synchronizationPointName = _synchronizationPointName;
        newObject._isCreator = _isCreator;
        newObject._eventBased = _eventBased;
        newObject._timeStepped = _timeStepped;

        return newObject;
    }

    /** Initializes the {@link HlaManager} attribute. This method: calls the
     *  _populateHlaAttributeTables() to initialize HLA attributes to publish
     *  or subscribe to; instantiates and initializes the {@link RTIambassador}
     *  and {@link FederateAmbassador} which handle the communication
     *  Federate <-> RTIA <-> RTIG. RTIA and RTIG are both external communicant
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

        // Get the corresponding director associate to the HlaManager attribute.
        _director = (DEDirector) ((CompositeActor) this.getContainer())
                .getDirector();

        // Initialize HLA attribute tables for publication/subscription.
        _populateHlaAttributeTables();

        // Get a link to the RTI.
        RtiFactory factory = null;
        try {
            factory = RtiFactoryFactory.getRtiFactory();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, e, "RTIinternalError ");
        }

        try {
            _rtia = (CertiRtiAmbassador) factory.createRtiAmbassador();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, e, "RTIinternalError. "
                    + "If the error is \"Connection to RTIA failed\", "
                    + "then the problem is likely that the rtig "
                    + "binary could not be started by CertRtig. "
                    + "One way to debug this is to set the various "
                    + "environment variables by sourcing certi/share/scripts/myCERTI_env.sh, "
                    + "then invoking rtig on the .fed file "
                    + "then rerunning the model.");
        }

        // Create the Federation or raise a warning it the Federation already exits.
        try {
            _rtia.createFederationExecution(_federationName, fedFile.asFile()
                    .toURI().toURL());
        } catch (FederationExecutionAlreadyExists e) {
            if (_debugging) {
                _debug(this.getDisplayName()
                        + " initialize() - WARNING: FederationExecutionAlreadyExists");
            }
        } catch (Exception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }

        _federateAmbassador = new PtolemyFederateAmbassadorInner();

        // Join the Federation.
        try {
            _rtia.joinFederationExecution(_federateName, _federationName,
                    _federateAmbassador);
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }

        // Initialize the Federate Ambassador.
        try {
            _federateAmbassador.initialize(_rtia);
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }

        // Initialize Federate timing values.
        _federateAmbassador.initializeTimeValues(_hlaStartTime, _hlaTimeStep,
                _hlaLookAHead);

        // Declare the Federate time constrained (if true).
        if (_isTimeConstrained) {
            try {
                _rtia.enableTimeConstrained();
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e, e.getMessage());
            }
        }

        // Declare the Federate time regulator (if true).
        if (_isTimeRegulator) {
            try {
                _rtia.enableTimeRegulation(_federateAmbassador.logicalTimeHLA,
                        _federateAmbassador.lookAHeadHLA);
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e, e.getMessage());
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
                } catch (RTIexception e) {
                    throw new IllegalActionException(this, e, e.getMessage());
                }
            }

            while (!(_federateAmbassador.timeRegulator)) {
                try {
                    _rtia.tick2();
                } catch (RTIexception e) {
                    throw new IllegalActionException(this, e, e.getMessage());
                }
            }

            if (_debugging) {
                _debug(this.getDisplayName() + " initialize() -"
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
                throw new IllegalActionException(this, e, e.getMessage());
            }
        }

        if (_requireSynchronization) {
            // If the current Federate is the creator then create the
            // synchronization point.
            if (_isCreator) {
                try {
                    byte[] rfspTag = EncodingHelpers
                            .encodeString(_synchronizationPointName);
                    _rtia.registerFederationSynchronizationPoint(
                            _synchronizationPointName, rfspTag);
                } catch (RTIexception e) {
                    throw new IllegalActionException(this, e, e.getMessage());
                }

                // Wait synchronization point callbacks.
                while (!(_federateAmbassador.synchronizationSuccess)
                        && !(_federateAmbassador.synchronizationFailed)) {
                    try {
                        _rtia.tick2();
                    } catch (RTIexception e) {
                        throw new IllegalActionException(this, e,
                                e.getMessage());
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
                } catch (RTIexception e) {
                    throw new IllegalActionException(this, e, e.getMessage());
                }
            }

            // Satisfied synchronization point.
            try {
                _rtia.synchronizationPointAchieved(_synchronizationPointName);
                if (_debugging) {
                    _debug(this.getDisplayName()
                            + " initialize() - Synchronisation point "
                            + _synchronizationPointName + " satisfied !");
                }
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e, e.getMessage());
            }

            // Wait federation synchronization.
            while (_federateAmbassador.inPause) {
                if (_debugging) {
                    _debug(this.getDisplayName()
                            + " initialize() - Waiting for simulation phase !");
                }

                try {
                    _rtia.tick2();
                } catch (RTIexception e) {
                    throw new IllegalActionException(this, e, e.getMessage());
                }
            }
        } // End block for synchronization point.

        // GL: FIXME: need to test deeper then remove this call.
        // tick() one time to avoid missing callbacks before the start of the
        // simulation.
        try {
            _rtia.tick();
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }
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

        // Try to launch the HLA/CERTI RTIG subprocess.
        _certiRtig = new CertiRtig(this, _debugging);
        _certiRtig.initialize(fedFile.asFile().getAbsolutePath());

        _certiRtig.exec();
        if (_debugging) {
            _debug(this.getDisplayName() + " preinitialize() - "
                    + "Launch RTIG process");
        }

        if (_certiRtig.isAlreadyLaunched()) {
            _certiRtig.terminateProcess();
            _certiRtig = null;

            if (_debugging) {
                _debug(this.getDisplayName() + " preinitialize() - "
                        + "Destroy RTIG process as another one is already "
                        + "launched");
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
     *  Case 2: If lookahead > 0
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
        Time breakpoint = null;

        // This test is used to avoid exception when the RTIG subprocess is
        // shutdown before the last call of this method.
        // GL: FIXME: see Ptolemy team why this is called again after STOPTIME ?
        if (_rtia == null) {
            if (_debugging) {
                _debug(this.getDisplayName() + " proposeTime() -"
                        + " called but _rtia is null");
            }
            return proposedTime;
        }

        // If the proposedTime has already been asked to the HLA/CERTI Federation
        // then return it.

        // GL: FIXME: Comment this until the clarification with NERA and TARA
        // and the use of TICK is made.
        /*
         if (_lastProposedTime != null) {
         if (_lastProposedTime.compareTo(proposedTime) == 0) {

         // Even if we avoid the multiple calls of the HLA Time management
         // service for optimization, it could be possible to have events
         // from the Federation in the Federate's priority timestamp queue,
         // so we tick() to get these events (if they exist).
         try {
         _rtia.tick();
         } catch (ConcurrentAccessAttempted e) {
         throw new IllegalActionException(this, e, "ConcurrentAccessAttempted ");
         } catch (RTIinternalError e) {
         throw new IllegalActionException(this, e, "RTIinternalError ");
         }

         return _lastProposedTime;
         }
         }
         */

        // If the HLA Time Management is required, ask to the HLA/CERTI
        // Federation (the RTI) the authorization to advance its time.
        if (_isTimeRegulator && _isTimeConstrained) {
            synchronized (this) {
                // Build a representation of the proposedTime in HLA/CERTI.
                CertiLogicalTime certiProposedTime = new CertiLogicalTime(
                        proposedTime.getDoubleValue());

                // Call the corresponding HLA Time Management service.
                try {
                    if (_eventBased) {
                        if (_hlaLookAHead > 0) {
                            // Event-based + lookahead > 0 => NER.
                            if (_debugging) {
                                _debug(this.getDisplayName()
                                        + " proposeTime() - call CERTI NER -"
                                        + " nextEventRequest("
                                        + certiProposedTime.getTime() + ")");
                            }
                            _rtia.nextEventRequest(certiProposedTime);
                        } else {
                            // Event-based + lookahead = 0 => NERA + NER.
                            // Start the time advancement loop with one NERA call.
                            if (_debugging) {
                                _debug(this.getDisplayName()
                                        + " proposeTime() - call CERTI NERA -"
                                        + " nextEventRequestAvailable("
                                        + certiProposedTime.getTime() + ")");
                            }
                            _rtia.nextEventRequestAvailable(certiProposedTime);

                            // Wait the grant from the HLA/CERTI Federation (from the RTI).
                            _federateAmbassador.timeAdvanceGrant = false;
                            while (!(_federateAmbassador.timeAdvanceGrant)) {
                                if (_debugging) {
                                    _debug(this.getDisplayName()
                                            + " proposeTime() -"
                                            + " wait CERTI TAG - "
                                            + "timeAdvanceGrant("
                                            + certiProposedTime.getTime()
                                            + ") by calling tick2()");
                                }

                                try {
                                    _rtia.tick2();
                                } catch (RTIexception e) {
                                    throw new IllegalActionException(this, e,
                                            e.getMessage());
                                }
                            }

                            // End the loop with one NER call.
                            if (_debugging) {
                                _debug(this.getDisplayName()
                                        + " proposeTime() - call CERTI NER -"
                                        + " nextEventRequest("
                                        + certiProposedTime.getTime() + ")");
                            }
                            _rtia.nextEventRequest(certiProposedTime);

                        }
                    } else {
                        if (_hlaLookAHead > 0) {
                            // Time-stepped + lookahead > 0 => TAR.
                            if (_debugging) {
                                _debug(this.getDisplayName()
                                        + " proposeTime() -  call CERTI TAR -"
                                        + " timeAdvanceRequest("
                                        + certiProposedTime.getTime() + ")");
                            }
                            _rtia.timeAdvanceRequest(certiProposedTime);
                        } else {
                            // Time-stepped + lookahead = 0 => TARA + TAR.
                            // Start the loop with one TARA call.
                            if (_debugging) {
                                _debug(this.getDisplayName()
                                        + " proposeTime() -  call CERTI TARA -"
                                        + " timeAdvanceRequest("
                                        + certiProposedTime.getTime() + ")");
                            }
                            _rtia.timeAdvanceRequestAvailable(certiProposedTime);

                            // Wait the grant from the HLA/CERTI Federation (from the RTI).
                            _federateAmbassador.timeAdvanceGrant = false;
                            while (!(_federateAmbassador.timeAdvanceGrant)) {
                                if (_debugging) {
                                    _debug(this.getDisplayName()
                                            + " proposeTime() -"
                                            + " wait CERTI TAG - "
                                            + "timeAdvanceGrant("
                                            + certiProposedTime.getTime()
                                            + ") by calling tick2()");
                                }

                                try {
                                    _rtia.tick2();
                                } catch (SpecifiedSaveLabelDoesNotExist e) {
                                    throw new IllegalActionException(this, e,
                                            "SpecifiedSaveLabelDoesNotExist ");
                                } catch (ConcurrentAccessAttempted e) {
                                    throw new IllegalActionException(this, e,
                                            "ConcurrentAccessAttempted ");
                                } catch (RTIinternalError e) {
                                    throw new IllegalActionException(this, e,
                                            "RTIinternalError ");
                                }
                            }

                            // End the loop with one TAR call.
                            if (_debugging) {
                                _debug(this.getDisplayName()
                                        + " proposeTime() -  call CERTI TAR -"
                                        + " timeAdvanceRequest("
                                        + certiProposedTime.getTime() + ")");
                            }
                            _rtia.timeAdvanceRequest(certiProposedTime);

                        }
                    }
                } catch (InvalidFederationTime e) {
                    throw new IllegalActionException(this, e,
                            "InvalidFederationTime ");
                } catch (FederationTimeAlreadyPassed e) {
                    throw new IllegalActionException(this, e,
                            "FederationTimeAlreadyPassed ");
                } catch (TimeAdvanceAlreadyInProgress e) {
                    throw new IllegalActionException(this, e,
                            "TimeAdvanceAlreadyInProgress ");
                } catch (EnableTimeRegulationPending e) {
                    throw new IllegalActionException(this, e,
                            "EnableTimeRegulationPending ");
                } catch (EnableTimeConstrainedPending e) {
                    throw new IllegalActionException(this, e,
                            "EnableTimeConstrainedPending ");
                } catch (FederateNotExecutionMember e) {
                    throw new IllegalActionException(this, e,
                            "FederateNotExecutionMember ");
                } catch (SaveInProgress e) {
                    throw new IllegalActionException(this, e, "SaveInProgress ");
                } catch (RestoreInProgress e) {
                    throw new IllegalActionException(this, e,
                            "RestoreInProgress ");
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, e,
                            "RTIinternalError ");
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, e,
                            "ConcurrentAccessAttempted ");
                } catch (NoSuchElementException e) {
                    // GL: FIXME: to investigate.
                    if (_debugging) {
                        _debug(this.getDisplayName() + " proposeTime() -"
                                + " NoSuchElementException " + " for _rtia");
                    }
                    return proposedTime;
                }

                // Wait the grant from the HLA/CERTI Federation (from the RTI).
                _federateAmbassador.timeAdvanceGrant = false;
                while (!(_federateAmbassador.timeAdvanceGrant)) {
                    if (_debugging) {
                        _debug(this.getDisplayName() + " proposeTime() -"
                                + " wait CERTI TAG - " + "timeAdvanceGrant("
                                + certiProposedTime.getTime()
                                + ") by calling tick2()");
                    }

                    try {
                        _rtia.tick2();
                    } catch (RTIexception e) {
                        throw new IllegalActionException(this, e,
                                e.getMessage());
                    }
                }

                // At this step we are sure that the HLA logical time of the
                // Federate has been updated (by the reception of the TAG callback
                // (timeAdvanceGrant()) and its value is the proposedTime or
                // less, so we have a breakpoint time.
                try {
                    breakpoint = new Time(
                            _director,
                            ((CertiLogicalTime) _federateAmbassador.logicalTimeHLA)
                                    .getTime());
                } catch (IllegalActionException e) {
                    throw new IllegalActionException(this, e,
                            "The breakpoint time is not a valid Ptolemy time");
                }

                // Stored reflected attributes as events on HLASubscriber actors.
                _putReflectedAttributesOnHlaSubscribers();
            }
        }

        //_lastProposedTime = breakpoint;
        return breakpoint;
    }

    /** Update the HLA attribute <i>attributeName</i> with the containment of
     *  the token <i>in</i>. The updated attribute is sent to the HLA/CERTI
     *  Federation.
     *  @param hp The HLA publisher actor (HLA attribute) to update.
     *  @param in The updated value of the HLA attribute to update.
     *  @throws IllegalActionException If a CERTI exception is raised then
     *  displayed it to the user.
     */
    void updateHlaAttribute(HlaPublisher hp, Token in)
            throws IllegalActionException {
        Time currentTime = _director.getModelTime();

        // The following operations build the different arguments required
        // to use the updateAttributeValues() (UAV) service provided by HLA/CERTI.

        // Retrieve information of the HLA attribute to publish.
        Object[] tObj = _hlaAttributesToPublish.get(hp.getName());

        // Encode the value to be sent to the CERTI.
        byte[] valAttribute = _encodeHlaValue(hp, in);

        SuppliedAttributes suppAttributes = null;
        try {
            suppAttributes = RtiFactoryFactory.getRtiFactory()
                    .createSuppliedAttributes();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, e, "RTIinternalError ");
        }
        suppAttributes.add((Integer) tObj[4], valAttribute);

        byte[] tag = EncodingHelpers.encodeString(((TypedIOPort) tObj[0])
                .getContainer().getName());

        // Create a representation of the current director time for CERTI.
        // HLA implies to send event in the future when using NER or TAR services with lookahead > 0.
        // To avoid CERTI exception when calling UAV service, in the case of NER or TAR
        // with lookahead > 0, we add the lookahead value to the event's timestamp.
        CertiLogicalTime ct = new CertiLogicalTime(currentTime.getDoubleValue()
                + _hlaLookAHead);
        try {
            _rtia.updateAttributeValues((Integer) tObj[5], suppAttributes, tag,
                    ct);
        } catch (Exception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }
        if (_debugging) {
            _debug(this.getDisplayName() + " publish() -"
                    + " send (UAV) updateAttributeValues "
                    + " current Ptolemy Time=" + currentTime.getDoubleValue()
                    + " HLA attribute \""
                    + ((TypedIOPort) tObj[0]).getContainer().getName()
                    + "\" (timestamp=" + ct.getTime() + ", value="
                    + in.toString() + ")");
        }
    }

    /** Manage the correct termination of the {@link HlaManager}. Call the
     *  HLA services to: unsubscribe to HLA attributes, unpublish HLA attributes,
     *  resign a Federation and destroy a Federation if the current Federate is
     *  the last participant.
     *  @throws IllegalActionException If the parent class throws it
     *  of if a CERTI exception is raised then displayed it to the user.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        if (_debugging) {
            _debug(this.getDisplayName() + " wrapup() - ... so termination");
        }

        // Unsubscribe to HLA attributes
        for (Object[] obj : _hlaAttributesSubscribedTo.values()) {
            try {
                _rtia.unsubscribeObjectClass((Integer) obj[3]);
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e, e.getMessage());
            }
            if (_debugging) {
                _debug(this.getDisplayName() + " wrapup() - Unsubscribe "
                        + ((TypedIOPort) obj[0]).getContainer().getName()
                        + "(classHandle = " + obj[3] + ")");
            }
        }

        // Unpublish HLA attributes.
        for (Object[] obj : _hlaAttributesToPublish.values()) {
            try {
                _rtia.unpublishObjectClass((Integer) obj[3]);
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e, e.getMessage());
            }
            if (_debugging) {
                _debug(this.getDisplayName() + " wrapup() - Unpublish "
                        + ((TypedIOPort) obj[0]).getContainer().getName()
                        + "(classHandle = " + obj[3] + ")");
            }
        }

        // Resign HLA/CERTI Federation execution.
        try {
            _rtia.resignFederationExecution(ResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES);
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }
        if (_debugging) {
            _debug(this.getDisplayName()
                    + " wrapup() - Resign Federation execution");
        }

        boolean canDestroyRtig = false;
        while (!canDestroyRtig) {

            // Destroy federation execution - nofail.
            try {
                _rtia.destroyFederationExecution(_federationName);
            } catch (FederatesCurrentlyJoined e) {
                if (_debugging) {
                    _debug(this.getDisplayName()
                            + " wrapup() - WARNING: FederatesCurrentlyJoined");
                }
            } catch (FederationExecutionDoesNotExist e) {
                // GL: FIXME: This should be an IllegalActionExeception
                if (_debugging) {
                    _debug(this.getDisplayName()
                            + " wrapup() - WARNING: FederationExecutionDoesNotExist");
                }
                canDestroyRtig = true;
            } catch (RTIinternalError e) {
                throw new IllegalActionException(this, e, "RTIinternalError ");
            } catch (ConcurrentAccessAttempted e) {
                throw new IllegalActionException(this, e,
                        "ConcurrentAccessAttempted ");
            }
            if (_debugging) {
                _debug(this.getDisplayName() + " wrapup() - "
                        + "Destroy Federation execution - no fail");
            }

            canDestroyRtig = true;
        }

        // Terminate RTIG subprocess.
        if (_certiRtig != null) {
            _certiRtig.terminateProcess();

            if (_debugging) {
                _debug(this.getDisplayName() + " wrapup() - "
                        + "Destroy RTIG process (if authorized)");
            }
        }

        // Clean HLA attribute tables.
        _hlaAttributesToPublish.clear();
        _hlaAttributesSubscribedTo.clear();
        _fromFederationEvents.clear();
        _objectIdToClassHandle.clear();
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
    protected HashMap<String, Object[]> _hlaAttributesSubscribedTo;

    /** List of events received from the HLA/CERTI Federation and indexed by the
     *  {@link HlaSubscriber} actors present in the model.
     */
    protected HashMap<String, LinkedList<TimedEvent>> _fromFederationEvents;

    /** Table of object class handles associate to object ids received by
     *  discoverObjectInstance and reflectAttributesValues services (e.g. from
     *  the RTI).
     */
    protected HashMap<Integer, Integer> _objectIdToClassHandle;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** This generic method should call the {@link EncodingHelpers} API provided
     *  by CERTI to handle type decoding operations for HLA value attribute that
     *  has been reflected.
     *  @param hs The targeted HLA subcriber actor.
     *  @param type The type to decode the token.
     *  @param buffer The encoded value to decode.
     *  @return The decoded value as an object.
     *  @throws IllegalActionException If the token is not handled or the
     *  decoding has failed.
     */
    private Object _decodeHlaValue(HlaSubscriber hs, Type type, byte[] buffer)
            throws IllegalActionException {

        // GL: FIXME: Case to handle PTIDES events.
        if (hs.useHLAPtidesEvent()) {
            if (!(type instanceof RecordType)) {
                throw new IllegalActionException(this,
                        "Try to decode a value for a PTIDES platform"
                                + " which is not a RecordType");
            }
            Double ret = EncodingHelpers.decodeDouble(buffer);
            return ret;
        }

        // Case to handle CERTI message buffer.
        if (hs.useCertiMessageBuffer()) {
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            MessageBuffer msgBuffer = new MessageBuffer(bis, bos);
            try {
                msgBuffer.receiveData();
            } catch (Exception e) {
                throw new IllegalActionException(this,
                        "Error to store CERTI message buffer from"
                                + " ByteArrayInputStream, reason: "
                                + e.getMessage());
            }

            if (type.equals(BaseType.BOOLEAN)) {
                return msgBuffer.readBoolean();
            } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
                return msgBuffer.readByte();
            } else if (type.equals(BaseType.DOUBLE)) {
                return msgBuffer.readDouble();
            } else if (type.equals(BaseType.FLOAT)) {
                return msgBuffer.readFloat();
            } else if (type.equals(BaseType.INT)) {
                return msgBuffer.readInt();
            } else if (type.equals(BaseType.LONG)) {
                return msgBuffer.readLong();
            } else if (type.equals(BaseType.SHORT)) {
                return msgBuffer.readShort();
            } else if (type.equals(BaseType.STRING)) {
                return msgBuffer.readString();
            } else {
                throw new IllegalActionException(this,
                        "The current type received by the HLA/CERTI Federation"
                                + " , through a CERTI message buffer,"
                                + " is not handled by " + this.getDisplayName());
            }
        }

        // Case to handle "normal" events.
        if (type.equals(BaseType.BOOLEAN)) {
            return EncodingHelpers.decodeBoolean(buffer);
        } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
            return EncodingHelpers.decodeByte(buffer);
        } else if (type.equals(BaseType.DOUBLE)) {
            return EncodingHelpers.decodeDouble(buffer);
        } else if (type.equals(BaseType.FLOAT)) {
            return EncodingHelpers.decodeFloat(buffer);
        } else if (type.equals(BaseType.INT)) {
            return EncodingHelpers.decodeInt(buffer);
        } else if (type.equals(BaseType.LONG)) {
            return EncodingHelpers.decodeLong(buffer);
        } else if (type.equals(BaseType.SHORT)) {
            return EncodingHelpers.decodeShort(buffer);
        } else if (type.equals(BaseType.STRING)) {
            return EncodingHelpers.decodeString(buffer);
        } else {
            throw new IllegalActionException(this,
                    "The current type received by the HLA/CERTI Federation"
                            + " is not handled by " + this.getDisplayName());
        }
    }

    /** This generic method call the {@link EncodingHelpers} API or the
     *  {@link MessageBuffer} API provided by CERTI to handle data encoding
     *  operation for updated value of HLA attribute that will be published
     *  to the federation.
     *  @param hp The HLA publisher actor which sends the HLA attribute value.
     *  @param tok The token to encode, i.e. the HLA attribute value to encode.
     *  @return The encoded value as an array of byte.
     *  @throws IllegalActionException If the token is not handled or the
     *  encoding has failed.
     */
    private byte[] _encodeHlaValue(HlaPublisher hp, Token tok)
            throws IllegalActionException {
        byte[] encodedValue = null;
        Token t = null;
        double recordTimestamp = -1.0;
        int recordMicrostep = -1;
        double sourceTimestamp = -1.0;

        // GL: FIXME: HLA PTIDES event support.
        // If we deal with an event sent from PtidesPlatform to PtidesPlatform,
        // then unwrap the corresponding RecordToken.
        // Be careful, only PTIDES network port are supported.
        if (hp.useHLAPtidesEvent()) {
            RecordToken rt = (RecordToken) tok;

            recordTimestamp = ((DoubleToken) rt.get("timestamp")).doubleValue();
            recordMicrostep = ((IntToken) rt.get("microstep")).intValue();
            sourceTimestamp = ((DoubleToken) rt.get("sourceTimestamp"))
                    .doubleValue();
            t = rt.get("payload");
        } else {
            t = tok;
        }

        // Get the corresponding type of the HLA attribute value.
        BaseType type = (BaseType) t.getType();

        // Case to handle CERTI message buffer.
        if (hp.useCertiMessageBuffer()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            MessageBuffer msgBuffer = new MessageBuffer(null, bos);

            if (type.equals(BaseType.BOOLEAN)) {
                msgBuffer.write(((BooleanToken) t).booleanValue());
            } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
                msgBuffer.write(((UnsignedByteToken) t).byteValue());
            } else if (type.equals(BaseType.DOUBLE)) {
                msgBuffer.write(((DoubleToken) t).doubleValue());
            } else if (type.equals(BaseType.FLOAT)) {
                msgBuffer.write(((FloatToken) t).floatValue());
            } else if (type.equals(BaseType.INT)) {
                msgBuffer.write(((IntToken) t).intValue());
            } else if (type.equals(BaseType.LONG)) {
                msgBuffer.write(((LongToken) t).longValue());
            } else if (type.equals(BaseType.SHORT)) {
                msgBuffer.write(((ShortToken) t).shortValue());
            } else if (type.equals(BaseType.STRING)) {
                msgBuffer.write(((StringToken) t).stringValue());
            } else {
                throw new IllegalActionException(this,
                        "The current type of the token " + t
                                + " is not handled by " + this.getDisplayName());
            }

            try {
                // Write the buffer in the output stream.
                msgBuffer.send();
            } catch (IOException e) {
                throw new IllegalActionException(this,
                        "Error to write CERTI message buffer"
                                + " in ByteArrayOutputStream, reason: "
                                + e.getMessage());
            }

            // Write the output stream in an array of bytes.
            encodedValue = bos.toByteArray();

            if (_debugging) {
                _debug(this.getDisplayName()
                        + " _encodeHlaValue() - A HLA value from ptolemy has been"
                        + " encoded as CERTI MessageBuffer" + " , currentTime="
                        + _director.getModelTime().getDoubleValue());
            }

            // If we deal with CERTI MessageBuffer then we only need to return
            // the array of bytes.
            return encodedValue;
        }

        // Case to handle "normal" event or HLA PTIDES event payload.
        if (type.equals(BaseType.BOOLEAN)) {
            encodedValue = EncodingHelpers.encodeBoolean(((BooleanToken) t)
                    .booleanValue());
        } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
            encodedValue = EncodingHelpers.encodeByte(((UnsignedByteToken) t)
                    .byteValue());
        } else if (type.equals(BaseType.DOUBLE)) {
            encodedValue = EncodingHelpers.encodeDouble(((DoubleToken) t)
                    .doubleValue());
        } else if (type.equals(BaseType.FLOAT)) {
            encodedValue = EncodingHelpers.encodeFloat(((FloatToken) t)
                    .floatValue());
        } else if (type.equals(BaseType.INT)) {
            encodedValue = EncodingHelpers.encodeInt(((IntToken) t).intValue());
        } else if (type.equals(BaseType.LONG)) {
            encodedValue = EncodingHelpers.encodeLong(((LongToken) t)
                    .longValue());
        } else if (type.equals(BaseType.SHORT)) {
            encodedValue = EncodingHelpers.encodeShort(((ShortToken) t)
                    .shortValue());
        } else if (type.equals(BaseType.STRING)) {
            encodedValue = EncodingHelpers.encodeString(((StringToken) t)
                    .stringValue());
        } else {
            throw new IllegalActionException(this,
                    "The current type of the token " + t
                            + " is not handled by " + this.getDisplayName());
        }

        // GL: FIXME: HLA PTIDES event support.
        // The HLA PTIDES event is only instantiated here as we need all
        // parameters when calling the constructor.
        if (hp.useHLAPtidesEvent()) {
            Time currentTime = _director.getModelTime();

            if (_debugging) {
                _debug(this.getDisplayName()
                        + " _encodeHlaValue() - Encoded as HlaPtidesEvent"
                        + " currentTime=" + currentTime.getDoubleValue()
                        + " recordTimestamp=" + recordTimestamp);
            }

            HlaPtidesEvent he = new HlaPtidesEvent(recordTimestamp,
                    recordMicrostep, sourceTimestamp, encodedValue);

            // Return the HLA PTIDES event as array of bytes.
            return he.getBuffer();
        } else {
            // Here we are sure that we don't deal with HLA PTIDES event or
            // CERTI MessageBuffer, so just return the encoded value as array
            // of bytes.
            return encodedValue;
        }
    }

    /** The method {@link #_populatedHlaValueTables()} populates the tables
     *  containing information of HLA attributes required to publish and to
     *  subscribe value attributes in a HLA Federation.
     *  @throws IllegalActionException If a HLA attribute is declared twice.
     */
    private void _populateHlaAttributeTables() throws IllegalActionException {
        CompositeActor ca = (CompositeActor) this.getContainer();

        List<HlaSubscriber> _hlaSubscribers = null;
        List<HlaPublisher> _hlaPublishers = null;
        _hlaAttributesToPublish.clear();
        _hlaAttributesSubscribedTo.clear();

        _hlaPublishers = ca.entityList(HlaPublisher.class);
        for (HlaPublisher hp : _hlaPublishers) {
            if (_hlaAttributesToPublish.get(hp.getName()) != null) {
                throw new IllegalActionException(this,
                        "A HLA attribute with the same name is already "
                                + "registered for publication");
            }
            // Only one input port is allowed per HlaPublisher actor.
            TypedIOPort tiop = hp.inputPortList().get(0);

            _hlaAttributesToPublish.put(
                    hp.getName(),
                    new Object[] {
                            tiop,
                            tiop.getType(),
                            ((StringToken) ((Parameter) hp
                                    .getAttribute("classObjectHandle"))
                                    .getToken()).stringValue() });
        }

        _hlaSubscribers = ca.entityList(HlaSubscriber.class);
        for (HlaSubscriber hs : _hlaSubscribers) {
            if (_hlaAttributesSubscribedTo.get(hs.getName()) != null) {
                throw new IllegalActionException(this,
                        "A HLA attribute with the same name is already "
                                + "registered for subscription");
            }
            // Only one output port is allowed per HlaSubscriber actor.
            TypedIOPort tiop = hs.outputPortList().get(0);

            _hlaAttributesSubscribedTo.put(
                    hs.getName(),
                    new Object[] {
                            tiop,
                            tiop.getType(),
                            ((StringToken) ((Parameter) hs
                                    .getAttribute("classObjectHandle"))
                                    .getToken()).stringValue() });

            // The events list to store updated values of HLA attribute,
            // (received by callbacks) from the RTI, is indexed by the HLA
            // Subscriber actors present in the model.
            _fromFederationEvents.put(hs.getName(),
                    new LinkedList<TimedEvent>());
        }
    }

    /** This method is called when a time advancement phase is performed. Every
     *  updated HLA attributes received by callbacks (from the RTI) during the
     *  time advancement phase is saved as {@link TimedEvent} and stored in a
     *  queue. Then, every {@link TimedEvent}s are moved from this queue to the
     *  output port of their corresponding {@link HLASubscriber} actors
     *  @throws IllegalActionException If the parent class throws it.
     */
    private void _putReflectedAttributesOnHlaSubscribers()
            throws IllegalActionException {
        // Reflected HLA attributes, e.g. updated values of HLA attributes
        // received by callbacks (from the RTI) from the whole HLA/CERTI
        // Federation, are store in the _subscribedValues queue (see
        // reflectAttributeValues() in PtolemyFederateAmbassadorInner class).

        TimedEvent event;
        for (int i = 0; i < _hlaAttributesSubscribedTo.size(); i++) {
            Iterator<Entry<String, LinkedList<TimedEvent>>> it = _fromFederationEvents
                    .entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, LinkedList<TimedEvent>> elt = it.next();

                // GL: FIXME: Check if multiple events here with same timestamp
                // make sense and can occur, if true we need to update the
                // following code to handle this case.
                if (elt.getValue().size() > 0) {
                    event = elt.getValue().getFirst();

                    // Get the HLA subscriber actor destinatory of the event.
                    TypedIOPort tiop = (TypedIOPort) _hlaAttributesSubscribedTo
                            .get(elt.getKey())[0];
                    HlaSubscriber hs = (HlaSubscriber) tiop.getContainer();

                    hs.putReflectedHlaAttribute(event);
                    if (_debugging) {
                        _debug(this.getDisplayName()
                                + " _putReflectedAttributesOnHlaSubscribers() - "
                                + " put Event: " + event.toString() + " in "
                                + hs.getDisplayName());
                    }

                    elt.getValue().remove(event);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Name of the current Ptolemy federate ({@link HlaManager}).*/
    private String _federateName;

    /**-Name of the HLA/CERTI federation to create or to join. */
    private String _federationName;

    /** RTI Ambassador for the Ptolemy Federate. */
    private CertiRtiAmbassador _rtia;

    /** Federate Ambassador for the Ptolemy Federate. */
    private PtolemyFederateAmbassadorInner _federateAmbassador;

    /** Indicates the use of the nextEventRequest() service. */
    private Boolean _eventBased;

    /** Indicates the use of the timeAdvanceRequest() service. */
    private Boolean _timeStepped;

    /** Indicates the use of the enableTimeConstrained() service. */
    private Boolean _isTimeConstrained;

    /** Indicates the use of the enableTimeRegulation() service. */
    private Boolean _isTimeRegulator;

    /** Start time of the Ptolemy Federate HLA logical clock. */
    private Double _hlaStartTime;

    /** Time step of the Ptolemy Federate. */
    private Double _hlaTimeStep;

    /** The lookahead value of the Ptolemy Federate. */
    private Double _hlaLookAHead;

    /** Indicates if the Ptolemy Federate will use a synchronization point. */
    private Boolean _requireSynchronization;

    /** Name of the synchronization point to create or to reach. */
    private String _synchronizationPointName;

    /** Indicates if the Ptolemy Federate is the creator of the synchronization
     *  point.
     */
    private Boolean _isCreator;

    /** Records the last proposed time to avoid multiple HLA time advancement
     *  requests at the same time.
     */
    //private Time _lastProposedTime;

    /** A reference to the enclosing director. */
    private DEDirector _director;

    /** The RTIG subprocess. */
    private CertiRtig _certiRtig;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** This class extends the {@link NullFederateAmbassador} class which
     *  implements the basics HLA services provided by the JCERTI bindings.
     *  @author Gilles Lasnier
     */
    private class PtolemyFederateAmbassadorInner extends NullFederateAmbassador {

        ///////////////////////////////////////////////////////////////////
        ////                         public variables                  ////

        /** Indicates if the Federate is declared as time regulator in the
         *  HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public Boolean timeRegulator;

        /** Indicates if the Federate is declared as time constrained in the
         *  HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public Boolean timeConstrained;

        /** Indicates if the Federate has received the time advance grant from
         *  the HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public Boolean timeAdvanceGrant;

        /** Indicates the current HLA logical time of the Federate. This value
         *  is set by callback by the RTI.
         */
        public LogicalTime logicalTimeHLA;

        /** Federate time step. */
        //public LogicalTime timeStepHLA;

        /** Indicates if the request of synchronization by the Federate is
         *  validated by the HLA/CERTI Federation. This value is set by callback
         *  by the RTI.
         */
        public Boolean synchronizationSuccess;

        /** Indicates if the request of synchronization by the Federate
         *  has failed. This value is set by callback by the RTI.
         */
        public Boolean synchronizationFailed;

        /** Indicates if the Federate is currently synchronize to others. This
         * value is set by callback by the RTI.
         */
        public Boolean inPause;

        /** The lookahead value set by the user and used by CERTI to handle
         *  time management and to order TSO events.
         */
        public LogicalTimeInterval lookAHeadHLA;

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Initialize the {@link PtolemyFederateAmbassadorInner} which handles
         *  the communication from RTI -> to RTIA -> to FEDERATE. The
         *  <i>rtia</i> manages the interaction with the external communicant
         *  process RTIA. This method called the Declaration Management
         *  services provide by HLA/CERTI to publish/subscribe to HLA attributes
         *  in a HLA Federation.
         *  @param rtia
         *  @throws NameNotFound
         *  @throws ObjectClassNotDefined
         *  @throws FederateNotExecutionMember
         *  @throws RTIinternalError
         *  @throws AttributeNotDefined
         *  @throws SaveInProgress
         *  @throws RestoreInProgress
         *  @throws ConcurrentAccessAttempted
         *  All those exceptions are from the HLA/CERTI implementation.
         */
        public void initialize(RTIambassador rtia) throws NameNotFound,
                ObjectClassNotDefined, FederateNotExecutionMember,
                RTIinternalError, AttributeNotDefined, SaveInProgress,
                RestoreInProgress, ConcurrentAccessAttempted {
            this.timeAdvanceGrant = false;
            this.timeConstrained = false;
            this.timeRegulator = false;
            this.synchronizationSuccess = false;
            this.synchronizationFailed = false;
            this.inPause = false;

            // GL: FIXME: XXX: This method needs to be review and simplified.

            // For each HlaPublisher actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to publish.

            // 1. Set classHandle and objAttributeHandle ids for each attribute
            // value to publish (i.e. HlaPublisher). Update the HlaPublishers
            // table with the information.
            Iterator<Entry<String, Object[]>> it = _hlaAttributesToPublish
                    .entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Object[]> elt = it.next();
                Object[] tObj = elt.getValue();

                // Object class handle and object attribute handle are ids that
                // allow to identify an HLA attribute.
                int classHandle = rtia.getObjectClassHandle((String) tObj[2]);
                int objAttributeHandle = rtia.getAttributeHandle(
                        ((TypedIOPort) tObj[0]).getContainer().getName(),
                        classHandle);

                // Update HLA attribute information (for publication)
                // from HLA services. In this case, the tObj[] object as
                // the following structure:
                // tObj[0] => input port which receives the token to transform
                //            as an updated value of a HLA attribute,
                // tObj[1] => type of the port (e.g. of the attribute),
                // tObj[2] => object class name of the attribute,
                // tObj[3] => id of the object class to handle,
                // tObj[4] => id of the attribute to handle,
                // tObj[5] => id of the registered attribute's instance.

                // tObj[0 .. 2] are extracted from the Ptolemy model.
                // tObj[3 .. 5] are provided by the RTI (CERTI).

                // All these information are required to publish/unpublish
                // updated value of a HLA attribute.
                elt.setValue(new Object[] { tObj[0], tObj[1], tObj[2],
                        classHandle, objAttributeHandle, null });
            }

            // 2. Create a table of HlaPublishers indexed by their corresponding
            // classHandle (no duplication).
            HashMap<String, LinkedList<String>> classHandleHlaPublisherTable = null;
            classHandleHlaPublisherTable = new HashMap<String, LinkedList<String>>();

            Iterator<Entry<String, Object[]>> it2 = _hlaAttributesToPublish
                    .entrySet().iterator();

            while (it2.hasNext()) {
                Map.Entry<String, Object[]> elt = it2.next();
                Object[] tObj = elt.getValue();

                // The classHandle where the HLA attribute belongs to (see FOM).
                String classHandleName = (String) tObj[2];

                if (classHandleHlaPublisherTable.containsKey(classHandleName)) {
                    classHandleHlaPublisherTable.get(classHandleName).add(
                            elt.getKey());
                } else {
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(elt.getKey());
                    classHandleHlaPublisherTable.put(classHandleName, list);
                }
            }

            // 3. Declare to the Federation the HLA attributes to publish. If
            // these attributes belongs to the same object class then only
            // one registerObjectInstance call is performed.
            // Then, update the HlaPublishers table with the new information.
            Iterator<Entry<String, LinkedList<String>>> it3 = classHandleHlaPublisherTable
                    .entrySet().iterator();

            while (it3.hasNext()) {
                Map.Entry<String, LinkedList<String>> elt = it3.next();
                LinkedList<String> hlaPublishers = elt.getValue();

                int classHandle = rtia.getObjectClassHandle(elt.getKey());

                // The attribute handle set to declare all attributes to publish
                // for one object class.
                AttributeHandleSet _attributesLocal = RtiFactoryFactory
                        .getRtiFactory().createAttributeHandleSet();

                // Fill the attribute handle set with all attibute to publish.
                for (String s : hlaPublishers) {
                    _attributesLocal.add((Integer) _hlaAttributesToPublish
                            .get(s)[4]);
                }

                // Declare to the Federation the HLA attribute(s) to publish.
                try {
                    rtia.publishObjectClass(classHandle, _attributesLocal);
                } catch (OwnershipAcquisitionPending e) {
                    e.printStackTrace();
                }

                int myObjectInstId = -1;

                // ROI is called ** only once time ** for one federate. This
                // means that only one instance of object class are allowed.
                try {
                    myObjectInstId = rtia.registerObjectInstance(classHandle,
                            elt.getKey() + "%%" + _federateName);
                } catch (ObjectClassNotPublished e) {
                    e.printStackTrace();
                } catch (ObjectAlreadyRegistered e) {
                    e.printStackTrace();
                }

                for (String s : hlaPublishers) {
                    Object[] tObj = _hlaAttributesToPublish.get(s);

                    _hlaAttributesToPublish.put(s,
                            new Object[] { tObj[0], tObj[1], tObj[2], tObj[3],
                                    tObj[4], myObjectInstId });
                }
            }

            // For each HlaSubscriber actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to subscribe to.
            Iterator<Entry<String, Object[]>> ot = _hlaAttributesSubscribedTo
                    .entrySet().iterator();

            while (ot.hasNext()) {
                Map.Entry<String, Object[]> elt = ot.next();
                Object[] tObj = elt.getValue();

                int classHandle = rtia.getObjectClassHandle((String) tObj[2]);
                int objAttributeHandle = rtia.getAttributeHandle(
                        ((TypedIOPort) tObj[0]).getContainer().getName(),
                        classHandle);

                // Update HLA attribute information (for subscription)
                // from HLA services. In this case, the tObj[] object as
                // the following structure:
                // tObj[0] => output port which will produce the event received
                //            as updated value of a HLA attribute,
                // tObj[1] => type of the port (e.g. of the attribute),
                // tObj[2] => object class name of the attribute,
                // tObj[3] => id of the object class handle,
                // tObj[4] => id of the attribute handle.

                // tObj[0 .. 2] are extracted from the Ptolemy model.
                // tObj[3 .. 4] are provided by the RTI (CERTI).

                // All these information are required to subscribe to/unsubscribe
                // to a HLA attribute.
                elt.setValue(new Object[] { tObj[0], tObj[1], tObj[2],
                        classHandle, objAttributeHandle });
            }

            // 2. Create a table of HlaSubscribers indexed by their corresponding
            //    object class handle (no duplication).
            HashMap<String, LinkedList<String>> classHandleHlaSubscriberTable = null;
            classHandleHlaSubscriberTable = new HashMap<String, LinkedList<String>>();

            Iterator<Entry<String, Object[]>> it4 = _hlaAttributesSubscribedTo
                    .entrySet().iterator();

            while (it4.hasNext()) {
                Map.Entry<String, Object[]> elt = it4.next();
                Object[] tObj = elt.getValue();

                // The object class name of the HLA attribute (see FOM).
                String classHandleName = (String) tObj[2];

                if (classHandleHlaSubscriberTable.containsKey(classHandleName)) {
                    classHandleHlaSubscriberTable.get(classHandleName).add(
                            elt.getKey());
                } else {
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(elt.getKey());
                    classHandleHlaSubscriberTable.put(classHandleName, list);
                }
            }

            // 3. Declare to the Federation the HLA attributes to subscribe to.
            Iterator<Entry<String, LinkedList<String>>> it5 = classHandleHlaSubscriberTable
                    .entrySet().iterator();

            while (it5.hasNext()) {
                Map.Entry<String, LinkedList<String>> elt = it5.next();
                LinkedList<String> hlaSubList = elt.getValue();

                int classHandle = rtia.getObjectClassHandle(elt.getKey());

                // The attribute handle set to declare all subscribed attributes
                // for one object class.
                AttributeHandleSet _attributesLocal = RtiFactoryFactory
                        .getRtiFactory().createAttributeHandleSet();

                for (String s : hlaSubList) {
                    _attributesLocal.add((Integer) _hlaAttributesSubscribedTo
                            .get(s)[4]);
                }

                _rtia.subscribeObjectClassAttributes(classHandle,
                        _attributesLocal);
            }
        }

        /** Initialize Federate's timing properties provided by the user.
         *  @param startTime The start time of the Federate logical clock.
         *  @param timeStep The time step of the Federate.
         *  @param lookAHead The contract value used by HLA/CERTI to synchronize
         *  the Federates and to order TSO events.
         */
        public void initializeTimeValues(Double startTime, Double timeStep,
                Double lookAHead) {
            logicalTimeHLA = new CertiLogicalTime(startTime);
            lookAHeadHLA = new CertiLogicalTimeInterval(lookAHead);
            //timeStepHLA = new CertiLogicalTime(timeStep);

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
            try {
                for (int i = 0; i < theAttributes.size(); i++) {
                    Iterator<Entry<String, Object[]>> ot = _hlaAttributesSubscribedTo
                            .entrySet().iterator();

                    while (ot.hasNext()) {
                        Map.Entry<String, Object[]> elt = ot.next();
                        Object[] tObj = elt.getValue();

                        Time ts = null;
                        TimedEvent te = null;

                        // Get the object class handle corresponding to
                        // received "theObject" id.
                        int classHandle = _objectIdToClassHandle.get(theObject);

                        // The tuple (attributeHandle, classHandle) allows to
                        // identify the object attribute (i.e. the HlaSubscriber)
                        // where the updated value has to be stored.
                        if (theAttributes.getAttributeHandle(i) == (Integer) tObj[4]
                                && (Integer) tObj[3] == classHandle) {
                            try {
                                HlaSubscriber hs = (HlaSubscriber) ((TypedIOPort) tObj[0])
                                        .getContainer();

                                // GL: FIXME: PTIDES
                                // This first case handle events from a PtidesPlatform
                                // to PtidesPlatform, only network port are supported.
                                if (tObj[1] instanceof RecordType) {
                                    HlaPtidesEvent hpe = new HlaPtidesEvent(
                                            theAttributes.getValue(i));

                                    // GL: FIXME: PTIDES: should be:
                                    //ts = new Time(_director, he.getSourceTime());
                                    ts = new Time(_director,
                                            ((CertiLogicalTime) theTime)
                                                    .getTime());
                                    te = new TimedEvent(ts, new Object[] {
                                            (RecordType) tObj[1],
                                            _decodeHlaValue(hs,
                                                    (RecordType) tObj[1],
                                                    hpe.getValue()),
                                            // GL: FIXME: PTIDES: should be:
                                            // he.getLogicalTime(),
                                            ts.getDoubleValue(),
                                            hpe.getMicroStep(),
                                            // GL: FIXME: PTIDES: should be:
                                            hpe.getSourceTime() });
                                    //ts.getDoubleValue()});

                                    //  System.out.println("RAV " + "has to decode asHlaPtidesEvent");
                                    //  System.out.println("RAV " + "logicalTime="+he.getLogicalTime());
                                    //  System.out.println("RAV " + "srcTimeStamp="+he.getSourceTime());
                                    //  System.out.println("RAV " + "time of RAV=" + ts.getDoubleValue());

                                } else {
                                    ts = new Time(_director,
                                            ((CertiLogicalTime) theTime)
                                                    .getTime());
                                    te = new TimedEvent(
                                            ts,
                                            new Object[] {
                                                    (BaseType) tObj[1],
                                                    _decodeHlaValue(
                                                            hs,
                                                            (BaseType) tObj[1],
                                                            theAttributes
                                                                    .getValue(i)) });
                                }
                            } catch (IllegalActionException e) {
                                e.printStackTrace();
                            }

                            _fromFederationEvents.get(
                                    ((TypedIOPort) tObj[0]).getContainer()
                                            .getName()).add(te);
                            if (_debugging) {
                                _debug(HlaManager.this.getDisplayName()
                                        + " INNER"
                                        + " reflectAttributeValues() (RAV) - "
                                        + "HLA attribute: "
                                        + ((TypedIOPort) tObj[0])
                                                .getContainer().getName()
                                        + "(value=" + te.contents
                                        + ", timestamp=" + te.timeStamp
                                        + ") has been received");
                            }
                        }
                    }
                }
            } catch (ArrayIndexOutOfBounds e) {
                e.printStackTrace();
            }
        }

        /** Callback delivered by the RTI (CERTI) to discover attribute instance
         *  of HLA attribute that the Federate is subscribed to.
         */
        @Override
        public void discoverObjectInstance(int theObject, int theObjectClass,
                String objectName) throws CouldNotDiscover,
                ObjectClassNotKnown, FederateInternalError {
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " discoverObjectInstance() - the object class "
                        + objectName + " has been discovered" + " (theObject="
                        + theObject + ", theObjectClass=" + theObjectClass
                        + ")");
            }

            _objectIdToClassHandle.put(theObject, theObjectClass);
        }

        // HLA Time Management services (callbacks).

        /** Callback delivered by the RTI (CERTI) to validate that the Federate
         *  is declared as time-regulator in the HLA Federation.
         */
        @Override
        public void timeRegulationEnabled(LogicalTime theFederateTime)
                throws InvalidFederationTime,
                EnableTimeRegulationWasNotPending, FederateInternalError {
            timeRegulator = true;
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " timeRegulationEnabled() - timeRegulator = "
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
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " timeConstrainedEnabled() - timeConstrained = "
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
            logicalTimeHLA = theTime;
            timeAdvanceGrant = true;
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " timeAdvanceGrant() - TAG("
                        + logicalTimeHLA.toString() + ") received");
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
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " synchronizationPointRegistrationFailed()"
                        + " - synchronizationFailed = " + synchronizationFailed);
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
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " synchronizationPointRegistrationSucceeded()"
                        + " - synchronizationSuccess = "
                        + synchronizationSuccess);
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
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " announceSynchronizationPoint() - inPause = "
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
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " federationSynchronized() - inPause = " + inPause);
            }
        }
    }
}
