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
import hla.rti.AsynchronousDeliveryAlreadyEnabled;
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
import hla.rti.FederateAlreadyExecutionMember;
import hla.rti.FederateAmbassador;
import hla.rti.FederateInternalError;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederateOwnsAttributes;
import hla.rti.FederatesCurrentlyJoined;
import hla.rti.FederationExecutionAlreadyExists;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.FederationTimeAlreadyPassed;
import hla.rti.InvalidFederationTime;
import hla.rti.InvalidLookahead;
import hla.rti.InvalidResignAction;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;
import hla.rti.NameNotFound;
import hla.rti.ObjectAlreadyRegistered;
import hla.rti.ObjectClassNotDefined;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectClassNotPublished;
import hla.rti.ObjectClassNotSubscribed;
import hla.rti.ObjectNotKnown;
import hla.rti.OwnershipAcquisitionPending;
import hla.rti.RTIambassador;
import hla.rti.RTIinternalError;
import hla.rti.ReflectedAttributes;
import hla.rti.ResignAction;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SpecifiedSaveLabelDoesNotExist;
import hla.rti.SuppliedAttributes;
import hla.rti.SynchronizationLabelNotAnnounced;
import hla.rti.TimeAdvanceAlreadyInProgress;
import hla.rti.TimeAdvanceWasNotInProgress;
import hla.rti.TimeConstrainedAlreadyEnabled;
import hla.rti.TimeRegulationAlreadyEnabled;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RtiFactory;
import hla.rti.jlc.RtiFactoryFactory;

import java.io.File;
import java.net.MalformedURLException;
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
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
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
 * publish/susbcribe paradigm. The FOM is specified in a .fed file used by 
 * the RTI (e.g. by the RTIG process when using CERTI). More information in [3].
 * <br><a href="http://savannah.nongnu.org/projects/certi" target="_top">http://savannah.nongnu.org/projects/certi</a></br>
 * </p><p>
 * To enable a Ptolemy model as a Federate, the {@link HlaManager} has to be
 * deployed and configured (by double-clicking on the attribute).
 * Parameters <i>federateName</i>, <i>federationName</i> have to match the 
 * declaration in the FOM (.fed file). <i>fedFile</i> specifies the path to
 * locate the FOM (.fed file).
 * </p><p>
 * Parameters <i>ner</i>, <i>tar</i>, <i>timeConst<i> and <i>timeReg</i> are
 * used to configure the HLA time management services of the Federate. A 
 * Federate can only specify the use of the <i>ner</i> (nextEventRequest()
 * service) or the <i>tar</i> (timeAdvanceRequest()) service at a time.
 * <i>timeConst</i> is used to specify time-constrained Federate and 
 * <i>timeReg</i> to specify time-regulator Federate. The combination of both
 * parameters is possible.
 * </p><p>
 * Parameters <i>hlaStartTime</i>, <i>hlaStepTime</i> and <i>hlaLookAHead<i>
 * are used to specify Hla Timing attributes of a Federate.
 * </p><p>
 * Parameters <i>requireSynchronization</i>, <i>syncPtName</i>, <i>syncPtName</i>
 * and <i>creatorSyncPt</i> are used to configure HLA synchronization point. 
 * This mechanism is usually used to synchronize the Federates, during their 
 * initialization, to avoid that federate's that only consume some HLA 
 * attributes finished their simulation before the other federates have started.
 * <i>creatorSyncPt</i> indicates if the Federate is the creator of the 
 * synchronization. Only one Federate can create the named synchronization 
 * point the whole HLA Federation.
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
public class HlaManager extends AbstractInitializableAttribute
implements TimeRegulator {

    /** Construct a HlaManager with a name and a container. The container 
     *  argument must not be null, or a NullPointerException will be thrown.  
     *  This actor will use the workspace of the container for synchronization 
     *  and version counts. If the name argument is null, then the name is set 
     *  to the empty string. Increment the version of the workspace.
     *  @param container
     *  @param name
     *  @exception IllegalActionException If the container is incompatible
     *  with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *  an actor already in the container.
     */
    public HlaManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _lastProposedTime = null;
        _attributes = null;
        _rtia = null;
        _fedAmb = null;

        _hlaAttributesToPublish = new HashMap<String, Object[]>();
        _hlaAttributesSubscribedTo = new HashMap<String, Object[]>();
        _fromFederationEvents = new HashMap<String, LinkedList<TimedEvent>>();

        // HLA Federation management parameters.
        federateName = new Parameter(this, "federateName");
        federateName.setDisplayName("Federate's name");
        federateName.setTypeEquals(BaseType.STRING);       
        federateName.setExpression("\"HlaManager\"");
        attributeChanged(federateName);

        federationName = new Parameter(this, "federationName");
        federationName.setDisplayName("Federation's name");
        federationName.setTypeEquals(BaseType.STRING);       
        federationName.setExpression("\"SimpleProducerConsumer\"");
        attributeChanged(federationName);

        fedFile = new Parameter(this, "fedFile");
        fedFile.setDisplayName("Path for .fed file");
        fedFile.setTypeEquals(BaseType.STRING);       
        fedFile.setExpression("\"./ptolemy/domains/hla/demo/SimpleProducerConsumer.fed\"");
        attributeChanged(fedFile);

        // HLA Time management parameters.
        ner = new Parameter(this, "ner");
        ner.setTypeEquals(BaseType.BOOLEAN);
        ner.setExpression("true");
        ner.setDisplayName("NER");
        attributeChanged(ner);

        tar = new Parameter(this, "tar");
        tar.setTypeEquals(BaseType.BOOLEAN);
        tar.setExpression("false");
        tar.setDisplayName("TAR");
        attributeChanged(tar);

        timeConst = new Parameter(this, "timeConst");
        timeConst.setTypeEquals(BaseType.BOOLEAN);
        timeConst.setExpression("true");
        timeConst.setDisplayName("Time constrained ?");
        attributeChanged(timeConst);

        timeReg = new Parameter(this, "timeReg");
        timeReg.setTypeEquals(BaseType.BOOLEAN);
        timeReg.setExpression("true");
        timeReg.setDisplayName("Time regulator ?");
        attributeChanged(timeReg);

        hlaStartTime = new Parameter(this, "hlaStartTime");
        hlaStartTime.setDisplayName("logical start time (in ms)");
        hlaStartTime.setExpression("0.0");
        hlaStartTime.setTypeEquals(BaseType.DOUBLE);
        attributeChanged(hlaStartTime);

        hlaTimeStep = new Parameter(this, "hlaTimeStep");
        hlaTimeStep.setDisplayName("time step (in ms)");
        hlaTimeStep.setExpression("0.0");
        hlaTimeStep.setTypeEquals(BaseType.DOUBLE);
        attributeChanged(hlaTimeStep);

        hlaLookAHead = new Parameter(this, "hlaLookAHead");
        hlaLookAHead.setDisplayName("lookahead (in ms)");
        hlaLookAHead.setExpression("0.0");
        hlaLookAHead.setTypeEquals(BaseType.DOUBLE);
        attributeChanged(hlaLookAHead);

        // HLA Synchronization parameters.
        requireSynchronization = new Parameter(this, "requireSynchronization");
        requireSynchronization.setTypeEquals(BaseType.BOOLEAN);
        requireSynchronization.setExpression("true");
        requireSynchronization.setDisplayName("Require synchronization ?");
        attributeChanged(requireSynchronization);

        syncPtName = new Parameter(this, "syncPtName");
        syncPtName.setDisplayName("Synchronization point name");
        syncPtName.setTypeEquals(BaseType.STRING);       
        syncPtName.setExpression("\"Simulating\"");
        attributeChanged(syncPtName);

        creatorSyncPt = new Parameter(this, "creatorSyncPt");
        creatorSyncPt.setTypeEquals(BaseType.BOOLEAN);
        creatorSyncPt.setExpression("false");
        creatorSyncPt.setDisplayName("Is synchronization point creator ?");
        attributeChanged(creatorSyncPt);
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
    public Parameter fedFile;

    /** Boolean value, 'true' if the Federate requires the use of the
     *  nextEventRequest() HLA service. This parameter must contain an
     *  BooleanToken. */
    public Parameter ner;

    /** Boolean value, 'true' if the Federate requires the use of the
     *  timeAdvanceRequest() HLA service. This parameter must contain an
     *  BooleanToken. */
    public Parameter tar;

    /** Boolean value, 'true' if the Federate is declared time constrained
     *  'false' if not. This parameter must contain an BooleanToken. */
    public Parameter timeConst;

    /** Boolean value, 'true' if the Federate is declared time regulator
     *  'false' if not. This parameter must contain an BooleanToken. */
    public Parameter timeReg;

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
    public Parameter syncPtName;

    /** Boolean value, 'true' if the Federate is the creator of the 
     *  synchronization point 'false' if not. This parameter must contain
     *  an BooleanToken. */
    public Parameter creatorSyncPt;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Checks constraints on the changed attribute (when it is required) and
     *  associates his value to its corresponding local variables. 
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the attribute is empty or negative.
     */
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
        } else if (attribute == fedFile) {
            String value = ((StringToken) fedFile.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Missing FOM file !");
            }
            _fedFile = value;
        } else if (attribute == ner) {
            if (((BooleanToken) ner.getToken()).booleanValue()) {
                _ner = true;
            } else {
                _ner = false;
            }
        } else if (attribute == tar) {
            if (((BooleanToken) tar.getToken()).booleanValue()) {
                _tar = true;
            } else {
                _tar = false;
            }
        } else if (attribute == timeConst) {
            if (((BooleanToken) timeConst.getToken()).booleanValue()) {
                _timeConst = true;
            } else {
                _timeConst = false;
            }
        } else if (attribute == timeReg) {
            if (((BooleanToken) timeReg.getToken()).booleanValue()) {
                _timeReg = true;
            } else {
                _timeReg = false;
            }
        } else if (attribute == hlaStartTime) {
            Double value = ((DoubleToken) hlaStartTime.getToken())
                    .doubleValue();
            if (value < 0) {
                throw new IllegalActionException(this,
                        "Cannot have negative value !");
            }
            _hlaStartTime = value;
        } else if (attribute == hlaTimeStep) {
            Double value = ((DoubleToken) hlaTimeStep.getToken())
                    .doubleValue();
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
            if (((BooleanToken) requireSynchronization.getToken()).booleanValue()) {
                _requireSynchronization = true;
            } else {
                _requireSynchronization = false;
            }
        } else if (attribute == syncPtName) {
            String value = ((StringToken) syncPtName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
            _syncPtName = value;
        } else if (attribute == creatorSyncPt) {
            if (((BooleanToken) creatorSyncPt.getToken()).booleanValue()) {
                _creatorSyncPt = true;
            } else {
                _creatorSyncPt = false;
            }
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HlaManager newObject = (HlaManager) super.clone(workspace);

        newObject._hlaAttributesToPublish = new HashMap<String, Object[]>();;
        newObject._hlaAttributesSubscribedTo = new HashMap<String, Object[]>();;
        newObject._fromFederationEvents = new HashMap<String, LinkedList<TimedEvent>>();
        newObject._attributes = null;
        newObject._rtia = null;
        newObject._fedAmb = null;
        newObject._federateName = _federateName;
        newObject._federationName = _federationName;
        newObject._fedFile = _fedFile;
        newObject._timeConst = _timeConst;
        newObject._timeReg = _timeReg;
        newObject._hlaStartTime = _hlaStartTime;
        newObject._hlaTimeStep = _hlaTimeStep;
        newObject._hlaLookAHead = _hlaLookAHead;
        newObject._requireSynchronization = _requireSynchronization;
        newObject._syncPtName = _syncPtName;
        newObject._creatorSyncPt = _creatorSyncPt;
        newObject._ner = _ner;
        newObject._tar = _tar;

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
    public void initialize() throws IllegalActionException { 
        super.initialize();

        NamedObj container = getContainer();
        if (!(container instanceof Actor)) {
            throw new IllegalActionException(this, 
                    "HlaManager has to be contained by an Actor");
        }

        // Get the corresponding director associate to the HlaManager attribute.
        _director = (DEDirector) ((CompositeActor) this.getContainer()).getDirector();

        // Initialize HLA attribute tables for publication/subscription.
        _populateHlaAttributeTables();

        // Get a link to the RTI.
        RtiFactory factory = null;
        try {
            factory = RtiFactoryFactory.getRtiFactory();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
        }

        try {
            _rtia = (CertiRtiAmbassador) factory.createRtiAmbassador();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
        }

        // Create the Federation or raise a warning it the Federation already exits.
        File fom = new File(_fedFile);
        try {
            _rtia.createFederationExecution(_federationName, fom.toURI().toURL());
        } catch (FederationExecutionAlreadyExists e) {
            if (_debugging) {
                _debug(this.getDisplayName() + " initialize() - WARNING: FederationExecutionAlreadyExists");
            }
        } catch (CouldNotOpenFED e) {
            throw new IllegalActionException(this, "CouldNotOpenFED " + e.getMessage());        
        } catch (ErrorReadingFED e) {
            throw new IllegalActionException(this, "ErrorReadingFED " + e.getMessage());        
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
        } catch (ConcurrentAccessAttempted e) {
            throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
        } catch (MalformedURLException e) {
            throw new IllegalActionException(this, "MalformedURLException " + e.getMessage());        
        }

        _fedAmb = new PtolemyFederateAmbassadorInner();

        // Join the Federation.
        try {
            _rtia.joinFederationExecution(_federateName, _federationName, _fedAmb);
        } catch (FederateAlreadyExecutionMember e) {
            throw new IllegalActionException(this, "FederateAlreadyExecutionMember " + e.getMessage());        
        } catch (FederationExecutionDoesNotExist e) {
            throw new IllegalActionException(this, "FederationExecutionDoesNotExist " + e.getMessage());        
        } catch (SaveInProgress e) {
            throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());        
        } catch (RestoreInProgress e) {
            throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());        
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
        } catch (ConcurrentAccessAttempted e) {
            throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
        }

        // Initialize the Federate Ambassador.
        try {
            _fedAmb.initialize(_rtia);
        } catch (NameNotFound e) {
            throw new IllegalActionException(this, "NameNotFound " + e.getMessage());        
        } catch (ObjectClassNotDefined e) {
            throw new IllegalActionException(this, "ObjectClassNotDefined " + e.getMessage());        
        } catch (FederateNotExecutionMember e) {
            throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());        
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
        } catch (AttributeNotDefined e) {
            throw new IllegalActionException(this, "AttributeNotDefined " + e.getMessage());        
        } catch (SaveInProgress e) {
            throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());        
        } catch (RestoreInProgress e) {
            throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());        
        } catch (ConcurrentAccessAttempted e) {
            throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
        }

        // Initialize Federate timing values.
        _fedAmb.initializeTimeValues(_hlaStartTime, _hlaTimeStep, _hlaLookAHead);

        // Declare the Federate time constrained (if true).
        if (_timeConst) {
            try {
                _rtia.enableTimeConstrained();
            } catch (TimeConstrainedAlreadyEnabled e) {
                throw new IllegalActionException(this, "TimeConstrainedAlreadyEnabled " + e.getMessage());        
            } catch (EnableTimeConstrainedPending e) {
                throw new IllegalActionException(this, "EnableTimeConstrainedPending " + e.getMessage());        
            } catch (TimeAdvanceAlreadyInProgress e) {
                throw new IllegalActionException(this, "TimeAdvanceAlreadyInProgress " + e.getMessage());        
            } catch (FederateNotExecutionMember e) {
                throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());        
            } catch (SaveInProgress e) {
                throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());        
            } catch (RestoreInProgress e) {
                throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());        
            } catch (RTIinternalError e) {
                throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
            } catch (ConcurrentAccessAttempted e) {
                throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
            }
        }

        // Declare the Federate time regulator (if true).
        if (_timeReg) {
            try {
                _rtia.enableTimeRegulation(_fedAmb._logicalTimeHLA, _fedAmb._lookAHeadHLA);
            } catch (TimeRegulationAlreadyEnabled e) {
                throw new IllegalActionException(this, "TimeRegulationAlreadyEnabled " + e.getMessage());        
            } catch (EnableTimeRegulationPending e) {
                throw new IllegalActionException(this, "EnableTimeRegulationPending " + e.getMessage());        
            } catch (TimeAdvanceAlreadyInProgress e) {
                throw new IllegalActionException(this, "TimeAdvanceAlreadyInProgress " + e.getMessage());        
            } catch (InvalidFederationTime e) {
                throw new IllegalActionException(this, "InvalidFederationTime " + e.getMessage());        
            } catch (InvalidLookahead e) {
                throw new IllegalActionException(this, "InvalidLookahead " + e.getMessage());        
            } catch (FederateNotExecutionMember e) {
                throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());        
            } catch (SaveInProgress e) {
                throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());        
            } catch (RestoreInProgress e) {
                throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());        
            } catch (RTIinternalError e) {
                throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
            } catch (ConcurrentAccessAttempted e) {
                throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
            }
        }

        // Wait the response of the RTI towards Federate time policies that has 
        // been declared. The only way to get a response is to invoke the tick()
        // method to receive callbacks from the RTI. We use here the tick2() 
        // method which is blocking and saves more CPU than the tick() method.
        if (_timeReg && _timeConst) {
            while (!(_fedAmb._isTimeConst)) {
                try {
                    _rtia.tick2();
                } catch (SpecifiedSaveLabelDoesNotExist e) {
                    throw new IllegalActionException(this, "SpecifiedSaveLabelDoesNotExist " + e.getMessage());        
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
                }
            }

            while (!(_fedAmb._isTimeReg)) {
                try {
                    ((CertiRtiAmbassador) _rtia).tick2();
                } catch (SpecifiedSaveLabelDoesNotExist e) {
                    throw new IllegalActionException(this, "SpecifiedSaveLabelDoesNotExist " + e.getMessage());        
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
                }
            }

            if (_debugging) {
                _debug(this.getDisplayName() + " initialize() -"
                        + " Time Management policies:"
                        + " is Constrained = " + _fedAmb._isTimeConst
                        + " and is Regulator = " + _fedAmb._isTimeReg);
            }

            // The following service is required to allow the reception of
            // callbacks from the RTI when a Federate used the Time management.
            try {
                _rtia.enableAsynchronousDelivery();
            } catch (AsynchronousDeliveryAlreadyEnabled e) {
                throw new IllegalActionException(this, "AsynchronousDeliveryAlreadyEnabled " + e.getMessage());        
            } catch (FederateNotExecutionMember e) {
                throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());        
            } catch (SaveInProgress e) {
                throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());        
            } catch (RestoreInProgress e) {
                throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());        
            } catch (RTIinternalError e) {
                throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
            } catch (ConcurrentAccessAttempted e) {
                throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
            }
        }

        if (_requireSynchronization) {
            // If the current Federate is the creator then create the 
            // synchronization point.
            if (_creatorSyncPt) {
                try {
                    byte[] rfspTag = EncodingHelpers.encodeString(_syncPtName);
                    _rtia.registerFederationSynchronizationPoint(_syncPtName, rfspTag);
                } catch (FederateNotExecutionMember e) {
                    throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());        
                } catch (SaveInProgress e) {
                    throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());        
                } catch (RestoreInProgress e) {
                    throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());        
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
                }

                // Wait synchronization point callbacks.
                while (!(_fedAmb._syncRegSuccess) && !(_fedAmb._syncRegFailed)) {
                    try {
                        ((CertiRtiAmbassador) _rtia).tick2();
                    } catch (SpecifiedSaveLabelDoesNotExist e) {
                        throw new IllegalActionException(this, "SpecifiedSaveLabelDoesNotExist " + e.getMessage());        
                    } catch (ConcurrentAccessAttempted e) {
                        throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
                    } catch (RTIinternalError e) {
                        throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
                    }
                }

                if (_fedAmb._syncRegFailed) {
                    throw new IllegalActionException(this, "CERTI: Synchronization error ! ");        
                }
            } // End block for synchronization point creation case.

            // Wait synchronization point announcement.
            while (!(_fedAmb._inPause)) {
                try {
                    ((CertiRtiAmbassador) _rtia).tick2();
                } catch (SpecifiedSaveLabelDoesNotExist e) {
                    throw new IllegalActionException(this, "SpecifiedSaveLabelDoesNotExist " + e.getMessage());        
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
                }
            }

            // Satisfied synchronization point.
            try {
                _rtia.synchronizationPointAchieved(_syncPtName);
                if (_debugging) {
                    _debug(this.getDisplayName()
                            + " initialize() - Synchronisation point "
                            + _syncPtName + " satisfied !");
                }
            } catch (SynchronizationLabelNotAnnounced e) {
                throw new IllegalActionException(this, "SynchronizationLabelNotAnnounced " + e.getMessage());        
            } catch (FederateNotExecutionMember e) {
                throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());        
            } catch (SaveInProgress e) {
                throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());        
            } catch (RestoreInProgress e) {
                throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());        
            } catch (RTIinternalError e) {
                throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
            } catch (ConcurrentAccessAttempted e) {
                throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
            }

            // Wait federation synchronization.
            while (((PtolemyFederateAmbassadorInner) _fedAmb)._inPause) {
                if (_debugging) {
                    _debug(this.getDisplayName()
                            + " initialize() - Waiting for simulation phase !");
                }

                try {
                    ((CertiRtiAmbassador) _rtia).tick2();
                } catch (SpecifiedSaveLabelDoesNotExist e) {
                    throw new IllegalActionException(this, "SpecifiedSaveLabelDoesNotExist " + e.getMessage());        
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
                }
            }
        } // End block for synchronization point.

        // GL: FIXME: need to test deeper then remove this call.
        // tick() one time to avoid missing callbacks before the start of the
        // simulation.
        try {
            ((CertiRtiAmbassador) _rtia).tick();
        } catch (ConcurrentAccessAttempted e) {
            throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
        }
    }

    /** Launch the HLA/CERTI RTIG process as subprocess. The RTIG has to be 
     *  launched before the initialization of a Federate.
     *  NOTE: if another HlaManager (e.g. Federate) has already launched a RTIG,
     *  the subprocess creates here is no longer required, then we destroy it.
     *  @exception IllegalActionException If the initialization of the 
     *  CertiRtig or the execution of the RTIG as subprocess has failed.
     */
    public void preinitialize() throws IllegalActionException { 
        super.preinitialize();

        // Try to launch the HLA/CERTI RTIG subprocess.
        _certiRtig = new CertiRtig(this, _debugging);
        _certiRtig.initialize(_fedFile);

        _certiRtig.exec();
        if (_debugging) {
            _debug(this.getDisplayName() + " preinitiliaze() - " 
                    + "Launch RTIG process");
        }

        if (_certiRtig.isAlreadyLaunched()) {
            _certiRtig.terminateProcess();
            _certiRtig = null;

            if (_debugging) {
                _debug(this.getDisplayName() + " preinitiliaze() - "
                        + "Destroy RTIG process as another one is already "
                        + "launched");
            }
        }        
    }

    /** Propose a time to advance to. This method is the one implementing the
     *  TimeRegulator interface and using the HLA/CERTI Time Management services
     *  (if required). If the Time Management is required, two cases: a) the 
     *  Federate is a time-stepped Federate, then the timeAdvanceRequest() (TAR)
     *  is used; b) the Federate is an event-based Federate, then the 
     *  nextEventRequest() (NER) service is used. Otherwise the proposedTime
     *  is returned.
     *  @param proposedTime The proposed time.
     *  @return The proposed time or a smaller time.
     *  @exception IllegalActionException If this attribute is not
     *  contained by an Actor.
     */
    public Time proposeTime(Time proposedTime) throws IllegalActionException {
        Time breakpoint = null;

        // This test is used to avoid exception when the RTIG subproces is
        // shutdown before the last call of this method.
        // GL: FIXME: see Ptolemy team why this called again after STOPTIME ?
        if (_rtia == null) {
            if (_debugging) {
                _debug(this.getDisplayName()
                        + " proposeTime() -" 
                        + " called but _rtia is null");
            }
            return proposedTime;
        }

        // If the proposedTime has already been asked to the HLA/CERTI Federation 
        // then return it.
        if (_lastProposedTime != null) {
            if (_lastProposedTime.compareTo(proposedTime) == 0) {

                // Even if we avoid the multiple calls of the HLA Time management
                // service for optimization, it could be possible to have events
                // from the Federation in the Federate's priority timestamp queue,
                // so we tick() to get these events (if they exist).
                try {
                    _rtia.tick();
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
                }

                return _lastProposedTime;
            }
        }

        // If the HLA Time Management is required, ask to the HLA/CERTI 
        // Federation (the RTI) the authorization to advance its time.
        if (_timeReg && _timeConst) {
            synchronized (this) {
                // Build a representation of the proposedTime in HLA/CERTI.
                CertiLogicalTime certiProposedTime = 
                        new CertiLogicalTime(proposedTime.getDoubleValue());

                // Call the corresponding HLA Time Management service.
                try {
                    if (_ner) {
                        if (_debugging) {
                            _debug(this.getDisplayName()
                                    + " proposeTime() -" 
                                    + " call CERTI NER - nextEventRequest(" 
                                    + certiProposedTime.getTime() +")");
                        }
                        _rtia.nextEventRequest(certiProposedTime);
                    } else {
                        if (_debugging) {
                            _debug(this.getDisplayName()
                                    + " proposeTime() -"
                                    + " call CERTI TAR - timeAdvanceRequest(" 
                                    + certiProposedTime.getTime() +")");
                        }
                        _rtia.timeAdvanceRequest(certiProposedTime);
                    }      
                } catch (InvalidFederationTime e) {
                    throw new IllegalActionException(this, "InvalidFederationTime " + e.getMessage());        
                } catch (FederationTimeAlreadyPassed e) {
                    throw new IllegalActionException(this, "FederationTimeAlreadyPassed " + e.getMessage());        
                } catch (TimeAdvanceAlreadyInProgress e) {
                    throw new IllegalActionException(this, "TimeAdvanceAlreadyInProgress " + e.getMessage());        
                } catch (EnableTimeRegulationPending e) {
                    throw new IllegalActionException(this, "EnableTimeRegulationPending " + e.getMessage());        
                } catch (EnableTimeConstrainedPending e) {
                    throw new IllegalActionException(this, "EnableTimeConstrainedPending " + e.getMessage());        
                } catch (FederateNotExecutionMember e) {
                    throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());        
                } catch (SaveInProgress e) {
                    throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());        
                } catch (RestoreInProgress e) {
                    throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());        
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
                } catch (NoSuchElementException e) {
                    // GL: FIXME: to investigate.
                    if (_debugging) {
                        _debug(this.getDisplayName()
                                + " proposeTime() -" + " NoSuchElementException " 
                                + " for _rtia");
                    }
                    return proposedTime;
                }

                // Wait the grant from the HLA/CERTI Federation (from the RTI).
                _fedAmb._isTimeAdvanceGrant = false;
                while (!(_fedAmb._isTimeAdvanceGrant)) {
                    if (_debugging) {
                        _debug(this.getDisplayName()
                                + " proposeTime() -" + " wait CERTI TAG - " 
                                + "timeAdvanceGrant(" + certiProposedTime.getTime() 
                                +") by calling tick2()");
                    }

                    try {
                        _rtia.tick2();
                    } catch (SpecifiedSaveLabelDoesNotExist e) {
                        throw new IllegalActionException(this, "SpecifiedSaveLabelDoesNotExist " + e.getMessage());        
                    } catch (ConcurrentAccessAttempted e) {
                        throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());        
                    } catch (RTIinternalError e) {
                        throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());        
                    }
                }

                // At this step we are sure that the HLA logical time of the 
                // Federate has been updated (by the reception of the TAG callback
                // (timeAdvanceGrant()) and its value is the proposedTime or 
                // less, so we have a breakpoint time.
                try {
                    breakpoint = new Time (_director, 
                            ((CertiLogicalTime) _fedAmb._logicalTimeHLA).getTime());
                } catch (IllegalActionException e) {
                    throw new IllegalActionException(this, 
                            "The breakpoint time is not a valid Ptolemy time");        
                }

                // Stored reflected attributes as events on HLASubscriber actors.
                _putReflectedAttributesOnHlaSubscribers();
            }
        }

        _lastProposedTime = breakpoint;
        return breakpoint;
    }

    /** Update the HLA attribute <i>attributeName</i> with the containment of 
     *  the token <i>in</i>. The updated attribute is sent to the HLA/CERTI 
     *  Federation.
     *  @param attributeName Name of the HLA attribute to update.
     *  @param in The updated value of the HLA attribute to update.
     *  @throws IllegalActionException If a CERTI exception is raised then
     *  displayed it to the user.
     */
    void updateHlaAttribute(String attributeName, Token in) throws IllegalActionException {
        Time currentTime = _director.getModelTime();

        // The following operations build the different arguments required
        // to use the updateAttributeValues() service provided by CERTI.

        // Retrieve information of the HLA attribute to publish.
        Object[] tObj = _hlaAttributesToPublish.get(attributeName);

        byte[] valAttribute = _encodeHlaValue(in);

        SuppliedAttributes suppAttributes = null;
        try {
            suppAttributes = RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();
        } catch (RTIinternalError error) {
            throw new IllegalActionException(this, "RTIinternalError " + error.getMessage());
        }
        suppAttributes.add((Integer) tObj[4], valAttribute);

        byte[] tag = EncodingHelpers.encodeString(((TypedIOPort) tObj[0]).getContainer().getName());

        // Create a representation of the current director time for CERTI.
        // GL: XXX: FIXME: need a little epsilon to avoid InvalidFederationTime exception when sending an UAV.
        CertiLogicalTime ct = new CertiLogicalTime(currentTime.getDoubleValue() + 0.0000001);

        try {
            _rtia.updateAttributeValues((Integer) tObj[5], suppAttributes, tag, ct);
        } catch (ObjectNotKnown e) {
            throw new IllegalActionException(this, "ObjectNotKnown " + e.getMessage());
        } catch (AttributeNotDefined e) {
            throw new IllegalActionException(this, "AttributeNotDefined " + e.getMessage());
        } catch (AttributeNotOwned e) {
            throw new IllegalActionException(this, "AttributeNotOwned " + e.getMessage());
        } catch (FederateNotExecutionMember e) {
            throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());
        } catch (SaveInProgress e) {
            throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());
        } catch (RestoreInProgress e) {
            throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());
        } catch (ConcurrentAccessAttempted e) {
            throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());
        } catch (InvalidFederationTime e) {
            throw new IllegalActionException(this, "InvalidFederationTime " + e.getMessage());
        }
        if (_debugging) {
            _debug(this.getDisplayName()
                    + " publish() -" + " send (UAV) updateAttributeValues "
                    + " current Ptolemy Time=" + currentTime.getDoubleValue() 
                    + " HLA attribute (timestamp=" + ct.getTime() 
                    + ", value=" + ((DoubleToken) in).doubleValue() + ")");
        }
    }

    /** Manage the correct termination of the {@link HlaManager}. Call the
     *  HLA services to: unsubscribe to HLA attributes, unpublish HLA attributes,
     *  resign a Federation and destroy a Federation if the current Federate is
     *  the last participant.
     *  @throws IllegalActionException If the parent class throws it
     *  of if a CERTI exception is raised then displayed it to the user.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        if (_debugging) {
            _debug(this.getDisplayName() + " wrapup() - ... so termination");
        }

        // Unsubscribe to HLA attributes
        for (int i = 0; i < _hlaAttributesSubscribedTo.size(); i++) {
            Iterator<Entry<String, Object[]>> it = _hlaAttributesSubscribedTo.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Object[]> elt = (Map.Entry) it.next();
                try {
                    _rtia.unsubscribeObjectClass((Integer) ((Object[]) elt.getValue())[3]);
                } catch (ObjectClassNotDefined e) {
                    throw new IllegalActionException(this, "ObjectClassNotDefined " + e.getMessage());
                } catch (ObjectClassNotSubscribed e) {
                    throw new IllegalActionException(this, "ObjectClassNotSubscribed " + e.getMessage());
                } catch (FederateNotExecutionMember e) {
                    throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());
                } catch (SaveInProgress e) {
                    throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());
                } catch (RestoreInProgress e) {
                    throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());
                }
                if (_debugging) {
                    _debug(this.getDisplayName() + " wrapup() - Unsubscribe "
                            + ((TypedIOPort) ((Object[]) elt.getValue())[0])
                            .getContainer().getName() 
                            + "(classHandle = " 
                            + ((Object[]) elt.getValue())[3]
                                    + ")");
                }
            }
        }

        // Unpublish HLA attributes.
        for (int i = 0; i < _hlaAttributesToPublish.size(); i++) {
            Iterator<Entry<String, Object[]>> it = _hlaAttributesToPublish.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Object[]> elt = (Map.Entry) it.next();
                try {
                    _rtia.unpublishObjectClass((Integer) ((Object[]) elt.getValue())[3]);
                } catch (ObjectClassNotDefined e) {
                    throw new IllegalActionException(this, "ObjectClassNotDefined " + e.getMessage());
                } catch (ObjectClassNotPublished e) {
                    throw new IllegalActionException(this, "ObjectClassNotPublished " + e.getMessage());
                } catch (OwnershipAcquisitionPending e) {
                    throw new IllegalActionException(this, "OwnershipAcquisitionPending " + e.getMessage());
                } catch (FederateNotExecutionMember e) {
                    throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());
                } catch (SaveInProgress e) {
                    throw new IllegalActionException(this, "SaveInProgress " + e.getMessage());
                } catch (RestoreInProgress e) {
                    throw new IllegalActionException(this, "RestoreInProgress " + e.getMessage());
                } catch (RTIinternalError e) {
                    throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());
                } catch (ConcurrentAccessAttempted e) {
                    throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());
                }
                if (_debugging) {
                    _debug(this.getDisplayName() + " wrapup() - Unpublish "
                            + ((TypedIOPort) ((Object[]) elt.getValue())[0])
                            .getContainer().getName() 
                            + "(classHandle = " 
                            + ((Object[]) elt.getValue())[3]
                                    + ")");
                }
            }
        }

        // Resign HLA/CERTI Federation execution.
        try {
            _rtia.resignFederationExecution(ResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES);
        } catch (FederateOwnsAttributes e) {
            throw new IllegalActionException(this, "FederateOwnsAttributes " + e.getMessage());
        } catch (FederateNotExecutionMember e) {
            throw new IllegalActionException(this, "FederateNotExecutionMember " + e.getMessage());
        } catch (InvalidResignAction e) {
            throw new IllegalActionException(this, "InvalidResignAction " + e.getMessage());
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());
        } catch (ConcurrentAccessAttempted e) {
            throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());
        }
        if (_debugging) {
            _debug(this.getDisplayName() + " wrapup() - Resign Federation execution");
        }

        boolean canDestroyRtig = false;
        while (!canDestroyRtig) {

            // Destroy federation execution - nofail.
            try {
                _rtia.destroyFederationExecution(_federationName);
            } catch (FederatesCurrentlyJoined e) {
                if (_debugging) {
                    _debug(this.getDisplayName() + " wrapup() - WARNING: FederatesCurrentlyJoined");
                }         
            } catch (FederationExecutionDoesNotExist e) {
                // GL: FIXME: This should be an IllegalActionExeception
                if (_debugging) {
                    _debug(this.getDisplayName() + " wrapup() - WARNING: FederationExecutionDoesNotExist");
                }
                canDestroyRtig = true;
            } catch (RTIinternalError e) {
                throw new IllegalActionException(this, "RTIinternalError " + e.getMessage());
            } catch (ConcurrentAccessAttempted e) {
                throw new IllegalActionException(this, "ConcurrentAccessAttempted " + e.getMessage());
            }
            if (_debugging) {
                _debug(this.getDisplayName() + " wrapup() - "
                        + "Destroy Federation execution - no fail");
            }

            canDestroyRtig = true;
            System.out.println("Yes2");

        }

        // Terminate RTIG subprocess.
        if (_certiRtig != null) {
            _certiRtig.terminateProcess();

            if (_debugging) {
                _debug(this.getDisplayName() + " wrapup() - "
                        + "Destroy RTIG process");
            }
        }

        // Clean HLA attribute tables.
        _hlaAttributesToPublish.clear();
        _hlaAttributesSubscribedTo.clear();
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** This generic method should call the {@link EncodingHelpers} API provided 
     *  by CERTI to handle type decoding operation for HLA value attribute that
     *  has been reflected. 
     *  @param tok The token to encode.
     *  @throws IllegalActionException If the token is not handled or the
     *  decoding has failed.
     */
    private Object _decodeHlaValue(BaseType type, byte[] buffer) throws IllegalActionException {
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

    /** This generic method should call the {@link EncodingHelpers} API provided 
     *  by CERTI to handle type encoding operation for HLA value attribute that
     *  will be published. 
     *  @param tok The token to encode.
     *  @throws IllegalActionException If the token is not handled or the
     *  encoding had failed.
     */
    private byte[] _encodeHlaValue(Token tok) throws IllegalActionException {        
        BaseType type = (BaseType) tok.getType();

        if (type.equals(BaseType.BOOLEAN)) {
            return EncodingHelpers.encodeBoolean(((BooleanToken) tok).booleanValue());
        } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
            return EncodingHelpers.encodeByte(((UnsignedByteToken) tok).byteValue());
        } else if (type.equals(BaseType.DOUBLE)) {
            return EncodingHelpers.encodeDouble(((DoubleToken) tok).doubleValue());
        } else if (type.equals(BaseType.FLOAT)) {
            return EncodingHelpers.encodeFloat(((FloatToken) tok).floatValue());
        } else if (type.equals(BaseType.INT)) {
            return EncodingHelpers.encodeInt(((IntToken) tok).intValue());
        } else if (type.equals(BaseType.LONG)) {
            return EncodingHelpers.encodeLong(((LongToken) tok).longValue());
        } else if (type.equals(BaseType.SHORT)) {
            return EncodingHelpers.encodeShort(((ShortToken) tok).shortValue());
        } else if (type.equals(BaseType.STRING)) {
            return EncodingHelpers.encodeString(((StringToken) tok).stringValue());
        } else {
            throw new IllegalActionException(this,
                    "The current type of the token " + tok 
                    + " is not handled by " + this.getDisplayName());
        }
    }

    /** The method {@link _populatedHlaValueTables()} populates the tables 
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
                        "A HLA value with the same name is already registered" +
                        " for publication");
            } 
            // Only one input port is allowed per HlaPublisher actor.
            TypedIOPort tiop = hp.inputPortList().get(0);

            _hlaAttributesToPublish.put(hp.getName(),
                    new Object[] {tiop, tiop.getType(), 
                ((StringToken) ((Parameter) hp.getAttribute("classObjectHandle"))
                        .getToken()).stringValue()});
        }

        _hlaSubscribers = ca.entityList(HlaSubscriber.class);
        for (HlaSubscriber hs : _hlaSubscribers) {
            if (_hlaAttributesSubscribedTo.get(hs.getName()) != null) {
                throw new IllegalActionException(this, 
                        "A HLA value with the same name is already registered" +
                        " for subcription");
            }
            // Only one output port is allowed per HlaSubscriber actor.
            TypedIOPort tiop = hs.outputPortList().get(0);

            _hlaAttributesSubscribedTo.put(hs.getName(),
                    new Object[] {tiop, tiop.getType(), 
                ((StringToken) ((Parameter) hs.getAttribute("classObjectHandle"))
                        .getToken()).stringValue()});

            // The events list to store HLA updated values (received by callbacks
            // from the RTI is indexed by the HLA Subscriber actors present in
            // the model.
            _fromFederationEvents.put(hs.getName(), new LinkedList<TimedEvent>());
        }
    }

    /** This method is called when a time advancement phase is performed. Every 
     *  updated HLA attributes received by callbacks (from the RTI) during the 
     *  time advancement phase is saved as {@link TimedEvent} and stored in a 
     *  queue. Then, every {@link TimedEvent}s are moved from this queue to the
     *  output port of their corresponding {@link HLASubscriber} actors
     *  @throws IllegalActionException If the parent class throws it.
     */
    private void _putReflectedAttributesOnHlaSubscribers() throws IllegalActionException {
        // Reflected HLA attributes, e.g. update of HLA attributes received by
        // callbacks (from the RTI) from the whole HLA/CERTI Federation are store
        // in the _subscribedValues queue (see reflectAttributeValues() in
        // PtolemyFederateAmbassadorInner class).

        TimedEvent event;
        for (int i = 0; i < _hlaAttributesSubscribedTo.size(); i++) {
            Iterator<Entry<String, LinkedList<TimedEvent>>> it = 
                    _fromFederationEvents.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, LinkedList<TimedEvent>> elt = (Map.Entry) it.next();

                // GL: FIXME: Check if multiple events here with same timestamp 
                // make sense and can occur, if true we need to update the 
                // following code to handle this case.
                if (elt.getValue().size() > 0) {
                    event = elt.getValue().getFirst();

                    TypedIOPort tiop = (TypedIOPort) ((Object[]) 
                            _hlaAttributesSubscribedTo.get(elt.getKey()))[0];
                    HlaSubscriber hs = (HlaSubscriber) tiop.getContainer();

                    hs.putReflectedAttribute(event);
                    if (_debugging) {
                        _debug(this.getDisplayName()
                                + " _putReflectedAttributesOnHlaSubscribers() - "
                                + " put Event: " + event.toString()
                                + " in " + hs.getDisplayName());
                    }

                    elt.getValue().remove(event);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Set of attributes handle for publication to the HLA Federation. */
    private AttributeHandleSet _attributes;

    /** Name of the current Ptolemy federate ({@link HlaManager}).*/
    private String _federateName;

    /**-Name of the HLA/CERTI federation to create or to join. */
    private String _federationName;

    /** Path and name of the HLA Federate Object Model (FOM) file.*/
    private String _fedFile;

    /** RTI Ambassador for the Ptolemy Federate. */
    private CertiRtiAmbassador _rtia;

    /** Federate Ambassador for the Ptolemy Federate. */
    private PtolemyFederateAmbassadorInner _fedAmb;

    /** Indicates the use of the {@link nextEventRequest()} service. */
    private Boolean _ner;

    /** Indicates the use of the {@link timeAdvanceRequest()} service. */
    private Boolean _tar;

    /** Indicates the use of the {@link enableTimeConstrained()} service. */
    private Boolean _timeConst;

    /** Indicates the use of the {@link enableTimeRegulation()} service. */
    private Boolean _timeReg;

    /** Start time of the Ptolemy Federate HLA logical clock. */
    private Double _hlaStartTime;

    /** Time step of the Ptolemy Federate. */
    private Double _hlaTimeStep;

    /** The lookahead value of the Ptolemy Federate. */
    private Double _hlaLookAHead;

    /** Indicates if the Ptolemy Federate will use a synchronization point. */
    private Boolean _requireSynchronization;

    /** Name of the synchronization point to create or to reach. */
    private String _syncPtName;

    /** Indicates if the Ptolemy Federate is the creator of the synchronization
     *  point.
     */
    private Boolean _creatorSyncPt;

    /** Records the last proposed time to avoid multiple HLA time advancement
     *  requests at the same time.
     */
    private Time _lastProposedTime;

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
        public Boolean _isTimeReg;

        /** Indicates if the Federate is declared as time constrained in the
         *  HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public Boolean _isTimeConst;

        /** Indicates if the Federate has received the time advance grant from
         *  the HLA/CERTI Federation. This value is set by callback by the RTI.
         */
        public Boolean _isTimeAdvanceGrant;

        /** Indicates the current HLA logical time of the Federate. This value 
         *  is set by callback by the RTI.
         */
        public LogicalTime _logicalTimeHLA;

        /** Federate time step. */
        public LogicalTime _timeStepHLA;

        /** Indicates if the request of synchronization by the Federate is
         *  validated by the HLA/CERTI Federation. This value is set by callback
         *  by the RTI.
         */
        public Boolean _syncRegSuccess;

        /** Indicates if the request of synchronization by the Federate 
         *  has failed. This value is set by callback by the RTI.
         */
        public Boolean _syncRegFailed;

        /** Indicates if the Federate is currently synchronize to others. This 
         * value is set by callback by the RTI.
         */
        public Boolean _inPause;

        /** The lookahead value set by the user and used by CERTI to handle
         *  time management and to order TSO events.
         */
        public LogicalTimeInterval _lookAHeadHLA;

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Initialize the {@link PtolemyFederateAmbassador} which handles
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
        ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, 
        AttributeNotDefined, SaveInProgress, RestoreInProgress, ConcurrentAccessAttempted {
            this._isTimeAdvanceGrant = false;
            this._isTimeConst = false;
            this._isTimeReg = false;
            this._syncRegSuccess = false;
            this._syncRegFailed = false;
            this._inPause = false;

            // For each HlaPublisher actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to publish.
            Iterator<Entry<String, Object[]>> it = _hlaAttributesToPublish.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Object[]> elt = (Map.Entry) it.next();
                Object[] tObj = (Object[]) elt.getValue();

                int myObjectInstId = -1;

                // Handle ids to identify and bind the HLA attribute.
                int classHandle = rtia.getObjectClassHandle((String) tObj[2]);
                int objAttributeHandle = rtia.getAttributeHandle(((TypedIOPort) tObj[0]).getContainer().getName(), classHandle);

                // GL: FIXME: investigate why we cannot declare this variable as
                // a local variable.
                if (_attributes == null) {
                    _attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
                }
                _attributes.add(objAttributeHandle);

                // Declare to the Federation the HLA attribute to publish.
                try {
                    rtia.publishObjectClass(classHandle, _attributes);
                } catch (OwnershipAcquisitionPending e) {
                    e.printStackTrace();
                }

                // Register to the Federation an instance of the attribute.
                try {
                    myObjectInstId = rtia.registerObjectInstance(classHandle, ((TypedIOPort) tObj[0]).getContainer().getName());
                } catch (ObjectClassNotPublished e) {
                    e.printStackTrace();
                } catch (ObjectAlreadyRegistered e) {
                    e.printStackTrace();
                }

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
                elt.setValue(new Object[] {tObj[0], tObj[1], tObj[2],
                        classHandle, objAttributeHandle, myObjectInstId});
            }


            // For each HlaSubscriber actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to subscribe to.
            Iterator<Entry<String, Object[]>> ot = _hlaAttributesSubscribedTo.entrySet().iterator();

            while (ot.hasNext()) {
                Map.Entry<String, Object[]> elt = (Map.Entry) ot.next();
                Object[] tObj = (Object[]) elt.getValue();

                int classHandle = rtia.getObjectClassHandle((String) tObj[2]);
                int objAttributeHandle = rtia.getAttributeHandle(((TypedIOPort) tObj[0]).getContainer().getName(), classHandle);

                // GL: FIXME: investigate why this one cannot be local to the
                // inner class (this is a CERTI bug).
                if (_attributes == null) {
                    _attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
                }
                _attributes.add(objAttributeHandle);

                _rtia.subscribeObjectClassAttributes(classHandle, _attributes);

                // Update HLA attribute information (for subscription)
                // from HLA services. In this case, the tObj[] object as
                // the following structure:
                // tObj[0] => output port which will produce the event received
                //            as updated value of a HLA attribute,
                // tObj[1] => type of the port (e.g. of the attribute),
                // tObj[2] => object class name of the attribute,
                // tObj[3] => id of the object class to handle,
                // tObj[4] => id of the attribute to handle.

                // tObj[0 .. 2] are extracted from the Ptolemy model.
                // tObj[3 .. 4] are provided by the RTI (CERTI).

                // All these information are required to subscribe to/unsubscribe
                // to a HLA attribute.
                elt.setValue(new Object[] {tObj[0], tObj[1], tObj[2], 
                        classHandle, objAttributeHandle});
            }
        }

        /** Initialize Federate's timing properties provided by the user.
         *  @param startTime The start time of the Federate logical clock.
         *  @param timeStep The time step of the Federate.
         *  @param lookAHead The contract value used by HLA/CERTI to synchronize
         *  the Federates and to order TSO events.
         */
        public void initializeTimeValues(Double startTime, Double timeStep, Double lookAHead) {
            _logicalTimeHLA = new CertiLogicalTime(startTime);
            _lookAHeadHLA = new CertiLogicalTimeInterval(lookAHead);
            _timeStepHLA = new CertiLogicalTime(timeStep);

            _isTimeAdvanceGrant = false;
        }

        // HLA Object Management services (callbacks).

        /** Callback to receive updated value of a HLA attribute from the
         *  whole Federation (delivered by the RTI (CERTI)).
         */
        public void reflectAttributeValues(int theObject, ReflectedAttributes theAttributes, byte[] userSuppliedTag, LogicalTime theTime, EventRetractionHandle retractionHandle)
                throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, InvalidFederationTime, FederateInternalError {
            try {
                for (int i = 0; i < theAttributes.size(); i++) {
                    Iterator<Entry<String, Object[]>> ot = _hlaAttributesSubscribedTo.entrySet().iterator();

                    while (ot.hasNext()) {
                        Map.Entry<String, Object[]> elt = (Map.Entry) ot.next();
                        Object[] tObj = (Object[]) elt.getValue();

                        Time ts = null;
                        TimedEvent te = null;
                        if (theAttributes.getAttributeHandle(i) == (Integer) tObj[4]) {
                            try {
                                ts = new Time(_director, ((CertiLogicalTime) theTime).getTime());

                                //te = new TimedEvent(ts, (Object) EncodingHelpers.decodeDouble(theAttributes.getValue(i)));
                                te = new TimedEvent(ts, new Object[] {(BaseType) tObj[1], _decodeHlaValue((BaseType) tObj[1], theAttributes.getValue(i))});

                            } catch (IllegalActionException e) {
                                e.printStackTrace();
                            }     

                            _fromFederationEvents.get(((TypedIOPort) tObj[0]).getContainer().getName()).add(te);
                            if (_debugging) {
                                _debug(HlaManager.this.getDisplayName() + " INNER"
                                        + " reflectAttributeValues() (RAV) - " 
                                        + "HLA attribute: "
                                        + ((TypedIOPort) tObj[0]).getContainer().getName()
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
        public void discoverObjectInstance(int theObject, int theObjectClass, String objectName) throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError {
            try {
                if (_hlaAttributesSubscribedTo.get(objectName) != null) {
                    _rtia.requestObjectAttributeValueUpdate(theObject, _attributes);

                    if (_debugging) {
                        _debug(HlaManager.this.getDisplayName() + " INNER"
                                + " discoverObjectInstance() - " + objectName
                                + " has been discovered");
                    }
                }
            } catch (ObjectNotKnown e) {
                e.printStackTrace();
            } catch (AttributeNotDefined e) {
                e.printStackTrace();
            } catch (FederateNotExecutionMember e) {
                e.printStackTrace();
            } catch (SaveInProgress e) {
                e.printStackTrace();
            } catch (RestoreInProgress e) {
                e.printStackTrace();
            } catch (RTIinternalError e) {
                e.printStackTrace();
            } catch (ConcurrentAccessAttempted e) {
                e.printStackTrace();
            }
        }

        // HLA Time Management services (callbacks).

        /** Callback delivered by the RTI (CERTI) to validate that the Federate 
         *  is declared as time-regulator in the HLA Federation.
         */
        public void timeRegulationEnabled(LogicalTime theFederateTime)
                throws InvalidFederationTime, EnableTimeRegulationWasNotPending, FederateInternalError {
            _isTimeReg = true;
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " timeRegulationEnabled() - isTimeReg = " + _isTimeReg);            
            }
        }

        /** Callback delivered by the RTI (CERTI) to validate that the Federate 
         *  is declared as time-constrained in the HLA Federation.
         */
        public void timeConstrainedEnabled(LogicalTime theFederateTime)
                throws InvalidFederationTime, EnableTimeConstrainedWasNotPending, FederateInternalError {
            _isTimeConst = true;
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " timeConstrainedEnabled() - isTimeConst = " + _isTimeConst);            
            }
        }

        /** Callback (TAG) delivered by the RTI (CERTI) to notify that the
         *  Federate is authorized to advance its time to <i>theTime</i>.
         *  This time is the same or smaller than the time specified
         *  when calling the nextEventRequest() or the timeAdvanceRequest() 
         *  services.
         */
        public void timeAdvanceGrant(LogicalTime theTime)
                throws InvalidFederationTime, TimeAdvanceWasNotInProgress, FederateInternalError {
            _logicalTimeHLA = theTime;
            _isTimeAdvanceGrant = true;
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " timeAdvanceGrant() - TAG(" + _logicalTimeHLA.toString()
                        + ") received");
            }
        }

        // HLA Federation Management services (callbacks).
        // Synchronization point services.

        /** Callback delivered by the RTI (CERTI) to notify if the synchronization
         *  point registration has failed.
         */
        public void synchronizationPointRegistrationFailed(String synchronizationPointLabel)
                throws FederateInternalError {
            _syncRegFailed = true;
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " synchronizationPointRegistrationFailed() - syncRegFailed = " + _syncRegFailed);            
            }
        }

        /** Callback delivered by the RTI (CERTI) to notify if the synchronization
         *  point registration has succeed.
         */
        public void synchronizationPointRegistrationSucceeded(String synchronizationPointLabel)
                throws FederateInternalError {
            _syncRegSuccess = true;
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " synchronizationPointRegistrationSucceeded() - syncRegSuccess = " + _syncRegSuccess);            
            }
        }

        /** Callback delivered by the RTI (CERTI) to notify the announcement of
         *  a synchronization point in the HLA Federation.
         */
        public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] userSuppliedTag)
                throws FederateInternalError {
            _inPause = true;
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " announceSynchronizationPoint() - inPause = " + _inPause);            
            }
        }

        /** Callback delivered by the RTI (CERTI) to notify that the Federate is
         *  synchronized to others Federates using the same synchronization point
         *  in the HLA Federation.
         */
        public void federationSynchronized(String synchronizationPointLabel)
                throws FederateInternalError {
            _inPause = false;
            if (_debugging) {
                _debug(HlaManager.this.getDisplayName() + " INNER"
                        + " federationSynchronized() - inPause = " + _inPause);            
            }
        }
    }
}
