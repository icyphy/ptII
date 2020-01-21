/* This attribute implements a HLA Manager to cooperate with a HLA/CERTI Federation.

@Copyright (c) 2013-2019 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import ptolemy.data.expr.StringParameter;
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
 * The Ptolemy-HLA co-simulation framework leverages two open source tools:
 * Ptolemy II and HLA/CERTI to enable construction in Ptolemy II of an HLA
 * federate.
 * </p>
 * <h2>About HLA</h2>
 * <p>
 * The High Level Architecture (HLA) [1][2] is a standard for distributed
 * discrete-event simulation. A simulation in HLA is called an HLA
 * federation. A federation is a collection of federates, typically modeling
 * components in a system, interconnected by a Run Time Infrastructure (RTI).
 * Each federate may be running on a different machine, and the RTI enables
 * them to send each other time-stamped events and ensures that the time
 * stamps respect DE semantics. The coordination between federates is handled
 * by a process called the RTIG, for RTI Gateway, which may be running
 * on a remote machine. Specifically, time advance in each of the
 * federates is coordinated by the RTIG so that events never arrive with time stamps
 * that are in the past with respect to the current time of the federate.
 * In Ptolemy II, a federate is a DE model with an instance of this HlaManager.
 * The model can contain composite actors with other models of computation, using
 * the ContinuousDirector for example.
 * </p><p>
 * This framework has been tested with an open-source RTI implementation
 * called CERTI [4]. In principle, it should work with other RTI implementations.
 * CERTI is implemented in C++, and an API called JCERTI is provided for Java.
 * Ptolemy II uses JCERTI.  CERTI also provides a Python API, so in principle,
 * a Ptolemy II federate should be able to interact with a federate written
 * in Python. For more about CERTI, see:
 * <a href="http://savannah.nongnu.org/projects/certi" target="_top">http://savannah.nongnu.org/projects/certi</a>.
 * </p>
 * <h2>Creating a Federate</h2>
 * <p>
 * To create a federate, in a Ptolemy model with a DE director, instantiate
 * an instance of this HLAManager. You will need to set the federationName
 * parameter to the name of the federation and
 * the <i>federateName</i> parameter to a unique name for the federate (unique
 * to the federation).
 * If, when running the model, there is no federation with the specified
 * name, then a federation with that name will be automatically created.
 * Otherwise, the federate will join the pre-existing federation.
 * </p><p>
 * Federates can exchange information using actors that implement
 * the {@link HlaUpdatable} and {@link HlaReflectable} interfaces,
 * such as {@link HlaAttributeUpdater} and {@link HlaAttributeReflector},
 * respectively. When a time-stamped event in one
 * federate is fed into an HlaUpdatable, then a corresponding time-stamped
 * event will pop out of any HlaReflectable elsewhere in the federation
 * if the parameters of that HlaReflectable match those of the
 * HlaUpdatable. These parameters conform to a
 * Federation Object Model (FOM) that is an important part of the
 * definition of a federation and is described in a file called a
 * Federation Execution Data (FED) file that needs to be
 * available to this HlaManager. More about this later.
 * </p>
 * <h2>Time-Stamped Communication</h2>
 * <p>
 * When a time-stamped event is sent from one federate to another
 * through the RTI,
 * the relationship of the received time stamp compared to the sent
 * time stamp depends on the parameters of the two HlaManagers at
 * each end of the communication.
 * First, the parameter <i>timeManagementService</i> selects between two
 * styles called "Next Event Request (NER)" and
 * "Time Advancement Request (TAR)".
 * We explain these in order.
 * </p><p>
 * The NER style is the default, and it is the most natural for use
 * with DE models. When <i>timeManagementService</i> specifies NER at both
 * the sending and receiving federate, then
 * the received time stamp is simply equal to the sent time stamp
 * plus the value of the <i>hlaLookahead</i> parameter of the sending
 * federate. So logically, there
 * is a "model-time delay" equal to <i>hlaLookahead</i> in each
 * communication.  The <i>hlaLookahead</i> is required to be strictly
 * greater than zero (this significantly simplifies distributed
 * coordination).
 * </p><p>
 * It rarely makes sense in a DE federate to use the TAR style,
 * and the result of using it is much more complicated.
 * The easiest way to understand the effect is to consider that
 * the time stamp may be modified first at the sending federate
 * and then again at the receiving federate. If the sending
 * federate is NER, then it simply increments the time stamp
 * by its <i>hlaLookahead</i>. If the receiving federate is NER, then
 * it makes no modification to the received time stamp. It uses whatever
 * it receives. Hence, if both ends are NER, the total modification
 * to the time stamp is to increment it by the <i>hlaLookahead</i> value at the sender.
 * But the situation is much more complicated if either end
 * uses TAR.
 * </p><p>
 * Suppose a sending federate is using TAR. In this case,
 * both <i>hlaLookahead</i> and the <i>hlaTimeStep</i> parameter may affect
 * the time stamp. Specifically, if the sender has an event
 * with time stamp equal to or greater than
 * <i>N</i> times <i>hlaTimeStep</i> and less than
 * <i>N</i> times <i>hlaTimeStep</i> plus <i>hlaLookahead</i>,
 * for any integer <i>N</i>, then the
 * time stamp will be modified by the sender to equal
 * <i>N</i> times <i>hlaTimeStep</i> plus <i>hlaLookahead</i>.
 * Otherwise, the time stamp is unmodified by the sender.
 * </p><p>
 * For example, if the sender has <i>hlaTimeStep</i> = 10 and
 * <i>hlaLookahead</i> = 2 and it wishes to send an event with
 * time stamp 1, the time stamp will be modified to 2,
 * introducing a delay of 1.
 * If it wants to send an event with time stamp 11,
 * the time stamp will be modified to 12.
 * </p><p>
 * Suppose a receiving federate is using TAR. In this
 * case, the time stamp of any incoming event will be
 * modified to equal the next largest (or equal)
 * multiple of the receiver's <i>hlaTimeStep</i>, i.e.
 * <i>M</i> times <i>hlaTimeStep</i> for some integer <i>M</i>.
 * </p><p>
 * For example, if a receiving federate of the previous message
 * whose time stamp was modified to 2 uses TAR with
 * <i>hlaTimeStep</i> = 5, then the time stamp will be modified
 * from 2 to 5, thereby getting a total delay of 4.
 * If the receiving federate uses NER, on the other
 * hand, the time stamp will not be modified by the receiver.
 * It will be received with time stamp 2.
 * </p><p>
 * If the sending federate is using NER and the receiving federate
 * is using TAR, then the sending federate will first modify the
 * time stamp by adding its <i>hlaLookahead</i> to the time stamp,
 * and then the receiving federate will further modify the time
 * stamp to make it equal to the least larger multiple of its
 * <i>hlaTimeStep</i>.
 * </p>
 * <h2>Exchanging Data</h2>
 * <p>
 * In HLA, the data that is sent from one federate to another through
 * the RTI is viewed
 * as an update to an attribute of an instance of a class.
 * Actors implementing the
 * {@link HlaUpdatable} and {@link HlaReflectable} interfaces specify
 * which attribute of which instance of which class they update
 * or are notified of updates.
 * See the documentation for {@link HlaAttributeUpdater} and
 * {@link HlaAttributeReflector}.
 * </p><p>
 * A Federation requires a Federation Execution Data (FED) file [7],
 * which defines classes and specifies which attributes a class contains.
 * The location of the FED file is given by two parameters of
 * this HlaManager, <i>fedFile</i> and <i>fedFileOnRTIG</i>.
 * Unfortunately, in HLA, the FED file is not a networked resource,
 * and it is needed by both the Ptolemy II federate and the RTIG,
 * and these may not be running on the same machine. So the locations
 * of the two files may be different.
 * By default, <i>fedFileOnRTIG</i> is set to "$fedFile", which
 * makes it the same as whatever you specify in the <i>fedFile</i>
 * parameter. This will usually work if the RTIG is being run
 * on the same machine as the federate, and will always work if
 * the federate itself launches the RTIG by setting the
 * <i>launchRTIG</i> parameter.
 * </p>
 * <h2>Environment Variables</h2>
 * <p>
 * CERTI, which is the implementation of HLA that Ptolemy II uses,
 * relies on some environment variables in order to execute:
 * <ul>
 * <li> CERTI_HOST, which provides the IP address of the machine that hosts the RTIG,
 * <li> CERTI_HOME, which provides the path to your installation of CERTI,
 * <li> CERTI_FOM_PATH which provides the default directory where the FED files are.
 *   This variable is needed if the FED file is not in the directory where the
 *   RTIG is launched.
 * <li> PATH must be updated with $CERTI_HOME/bin, so the binaries can be found,
 *   e.g., rtig and rtia.
 * <li> LD_LIBRARY_PATH and DYLD_LIBRARY_PATH must be updated with $CERTI_HOME/lib,
 *   the directory where the libRTI, libFedTime, libCERTId libraries are
 *   implemented.
 * </ul>
 * CERTI provides a script <i>myCERTI_env.sh</i> that set all the above
 * environment variables. The script is located in $CERTI_HOME/share/scripts and
 * must be run before a federation simulation.
 *
 * By default, the script <i>myCERTI_env.sh</i> set CERTI_HOST=localhost. This
 * specifies that the RTIG host is the local machine. If you are connecting to
 * a remote RTIG, you need to specify the IP address or domain name of that machine.
 * To set it, before launching Ptolemy II, you can execute a command like:
 * <pre>
 *    export CERTI_HOST=IP_address
 * </pre>
 * </p><p>
 * The environment variable CERTI_HOME is required
 * only if you wish the federate to launch an RTIG, something you specify
 * with the <i>launchRTIG</i> parameter.  If you wish the federate to launch
 * an RTIG, then you must set CERTI_HOME equal to the path to your
 * installation of CERTI, and you must set your PATH environment variable
 * to include the share/scripts and bin directories of your CERTI installation.
 * </p><p>
 * Notice that if the script <i>myCERTI_env.sh</i> is executed, all environment
 * variables are set and the RTIG can be launched executing a command line
 * (like <i>rtig</i>) or by the Ptolemy federate as explained above.
 * </p><p>
 * Sometime in the future, these environment variables may be replaced
 * or supplemented by parameters added to this HlaManager.
 * See also the {@link CertiRtig} class.
 * </p>
 * <h2>Lifecycle of Execution of a Federate</h2>
 * <p>
 * When a Ptolemy II federate starts executing, this HlaManager attempts
 * to connect to an RTIG running on the host specified by CERTI_HOST.
 * If this fails, and if <i>launchRTIG</i>
 * is set to true, and CERTI_HOST is either "localhost" or "127.0.0.1",
 * then this HlaManager will attempt to launch an RTIG on the local
 * machine.  This will require that the rtig executable be in your
 * PATH and that CERTI_HOME be specified, as explained above.
 * </p><p>
 * Once the federate has connected to the RTIG, it will attempt to
 * join the federation whose name is given by <i>federationName</i>.
 * If there is no such federation, then it will create it and then
 * join it.
 * </p><p>
 * After joining the federation, if <i>synchronizeStartTo</i> is
 * not empty, then it will stall the execution until the
 * synchronization point named in <i>synchronizeStartTo</i> is
 * reached by all federates in the federation.
 * If <i>synchronizeStartTo</i> is the same as <i>federateName</i>,
 * then it is <i>this</i> federate that registers the synchronization
 * point with the HLA. In that case, this federate should be last one launched.
 * All other federates that name this federate in their
 * <i>synchronizeStartTo</i> parameters will be waiting for
 * a message from the RTIG when all federates have reached this point,
 * and now the coordinated simulation can begin.
 * <b>NOTE:</b> If any federate fails to reach this synchronization point,
 * then the entire federation will be frozen in its initialization.
 * Consequently, <i>every</i> federate must have the same value for
 * <i>synchronizeStartTo</i>.
 * </p><p>
 * The simulation then executes like a normal DE simulation, except
 * that it may be stalled waiting for the RTI to permit time to advance.
 * This HlaManager implements the Ptolemy II TimeRegulator interface,
 * which enables it to regulate the advancement of time in the DEDirector.
 * The parameters <i>isTimeConstrained</i> and <i>isTimeRegulator</i> can be used
 * to decouple the advancement of time of the DEDirector from that of the RTI,
 * but this is rarely a good idea. For more details about time management
 * during execution, see [8].
 * </p><p>
 * The DEDirector in the model is required to have a finite <i>stopTime</i>
 * parameter set. It is usually also a good idea to set its
 * <i>stopWhenQueueIsEmpty</i> parameter to false, particularly if it
 * is relying on an HlaReflectable actor to provide it with events to process.
 * Otherwise, the federate may prematurely finish its simulation.
 * </p><p>
 * When the simulation ends, the federate resigns from the federation.
 * When all federates have resigned, the last federate destroys the federation.
 * In addition, if one of the federates launched the RTIG, and if
 * that federate's <i>killRTIG</i> parameter is set to true, then
 * that federate will also kill the RTIG.
 * </p><p>
 * This HlaManager provides extensive debug output. Listen to the attribute to get
 * the details. Some of these messages, primarily those related to the lifecycle of
 * the interaction with the RTI, are also printed to standard out, even if you are
 * listening to the attribute.
 * </p>
 * <p>NOTE FOR DEVELOPERS:
 * A federate needs to instruct the RTI that it is prepared to receive one or more
 * callbacks. The way to do it was not in the HLA 1.3 standard and was introduced
 * in the HLA IEEE 1516 standard. JCERTI, the Java API, is compliant only with DoD HLA 1.3
 * even though CERTI (coded with C++) is compliant with both standards. JCERTI
 * provides three different methods for receiving a callback: tick(),
 * tick(min,max) and tick2(). When a federate calls the tick() method, any
 * callbacks that are pending are invoked, and then the tick() returns.
 * If there are no pending callbacks, then it returns immediately.
 * This HLAManager, uses tick(min, max), which is not documented and we can't
 * be sure what it does, but it seems that the min argument specifies the
 * maximum real time to block waiting for a callback
 * before returning, and the second argument is not used. But we really can't
 * be sure without carefully evaluating all the C++ code.
 * The alternative, tick2(), is not an attractive alternative because it
 * blocks until a callback occurs, and a callback may never occur.
 * This results in the HLAManager freezing, which freezes the Ptolemy model
 * and Vergil, the user interface.
 * </p>
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
 *     Simulation Framework", In: Principles of Modeling. Springer International
 *     Publishing Switzerland, 122-142. ISBN 978-3-319-95245-1. </p>
 * <p>[7] Dpt. of Defense. High Level Architecture Run-Time Infrastructure
 *     Programmer's Guide RTI 1.3 Version 6, March 1999</p>
 * <p>[8] R. Fujimoto, "Time Management in The High Level Architecture",
 *     https://doi.org/10.1177/003754979807100604</p>
 * <p>[9] IEEE Standard for Modeling and Simulation HLA: Federate Interface Specification,
 *     IEEE Std 1516.1-2010, 18 August 2010. </p>
 * <p>[10] Chaudron, Jean-Baptiste and Siron, Pierre and Adelantado, Martin. Analysis and
 *     Optimization of time-management services in CERTI 4.0. (2018) In: 2018 Fall
 *     Simulation Innovation Workshop (SIW), 10-14 September 2018 (Orlando, United
 *     States). </p>
 *
 *  @author Gilles Lasnier, Janette Cardoso, and Edward A. Lee. Contributors: Patricia Derler, David Come, Yanxuan LI
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
        _objectHandleToClassHandle = new HashMap<Integer, Integer>();

        // Joker wildcard support.
        _usedJokerFilterMap = new HashMap<String, Boolean>();

        // HLA Federation management parameters.
        federateName = new StringParameter(this, "federateName");
        federateName.setExpression("Federate");
        attributeChanged(federateName);

        federationName = new StringParameter(this, "federationName");
        federationName.setExpression("HLAFederation");
        attributeChanged(federationName);

        fedFile = new FileParameter(this, "fedFile");
        new Parameter(fedFile, "allowFiles", BooleanToken.TRUE);
        new Parameter(fedFile, "allowDirectories", BooleanToken.FALSE);
        fedFile.setExpression("HLAFederation.fed");

        fedFileOnRTIG = new StringParameter(this, "fedFileOnRTIG");
        fedFileOnRTIG.setExpression("$fedFile");

        // HLA Time management parameters.
        timeManagementService = new ChoiceParameter(this,
                "timeManagementService", ETimeManagementService.class);
        attributeChanged(timeManagementService);

        hlaTimeStep = new Parameter(this, "hlaTimeStep");
        hlaTimeStep.setExpression("1.0");
        hlaTimeStep.setTypeEquals(BaseType.DOUBLE);
        attributeChanged(hlaTimeStep);

        isTimeConstrained = new Parameter(this, "isTimeConstrained");
        isTimeConstrained.setTypeEquals(BaseType.BOOLEAN);
        isTimeConstrained.setExpression("true");
        isTimeConstrained.setVisibility(Settable.NOT_EDITABLE);
        attributeChanged(isTimeConstrained);

        isTimeRegulator = new Parameter(this, "isTimeRegulator");
        isTimeRegulator.setTypeEquals(BaseType.BOOLEAN);
        isTimeRegulator.setExpression("true");
        isTimeRegulator.setVisibility(Settable.NOT_EDITABLE);
        attributeChanged(isTimeRegulator);

        hlaLookAHead = new Parameter(this, "hlaLookAHead");
        hlaLookAHead.setExpression("0.1");
        hlaLookAHead.setTypeEquals(BaseType.DOUBLE);
        attributeChanged(hlaLookAHead);

        // HLA Synchronization parameters.
        synchronizeStartTo = new StringParameter(this, "synchronizeStartTo");

        hlaTimeUnit = new Parameter(this, "hlaTimeUnit");
        hlaTimeUnit.setTypeEquals(BaseType.DOUBLE);
        hlaTimeUnit.setExpression("1.0");
        attributeChanged(hlaTimeUnit);

        // HLA Reporter support.
        enableHlaReporter = new Parameter(this, "enableHlaReporter");
        enableHlaReporter.setTypeEquals(BaseType.BOOLEAN);
        enableHlaReporter.setExpression("false");
        attributeChanged(enableHlaReporter);

        hlaReportPath = new FileParameter(this, "hlaReportPath");
        new Parameter(hlaReportPath, "allowFiles", BooleanToken.FALSE);
        new Parameter(hlaReportPath, "allowDirectories", BooleanToken.TRUE);
        hlaReportPath.setExpression("$HOME/HLATestResults");

        launchRTIG = new Parameter(this, "launchRTIG");
        launchRTIG.setTypeEquals(BaseType.BOOLEAN);
        launchRTIG.setExpression("false");

        killRTIG = new Parameter(this, "killRTIG");
        killRTIG.setTypeEquals(BaseType.BOOLEAN);
        killRTIG.setExpression("launchRTIG");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Name of the Ptolemy Federate. This is a string that defaults
     *  to "Federate". Note that it is required that every federate in
     *  a federation has to have a unique name.
     */
    public StringParameter federateName;

    /** Name of the federation. This is a string that default to
     *  "HLAFederation".
     */
    public StringParameter federationName;

    /** Path and name of the Federation Execution Data (FED) file (which should have
     *  extension .fed) on the local machine. This is not necessarily the file that
     *  is used by the RTIG because the RTIG may not be running on the local machine.
     *  It is expected that if these two files are on different machines, they are
     *  nevertheless the same.
     *  @see fedFileOnRTIG
     */
    public FileParameter fedFile;

    /** Name or path and name of the FED file on the machine that is running the
     *  RTIG. Unfortunately, that file may not be the same as the FED file
     *  on the local machine, pointed to by the fedFile parameter. By
     *  default, this string is set to "$fedFile", indicating that the default
     *  value should match the value of the fedFile parameter. But note that
     *  if the RTIG is running on a remote machine, this may need to be different
     *  from the value of fedFile. Note also that the remote FED file and the
     *  local FED file need to be the same, but there is no way to enforce that.
     *  @see fedFile
     */
    public StringParameter fedFileOnRTIG;

    /** Double value representing how many HLA time units there are in one
     *  second. An HLA federation runs in arbitrary time units, where each
     *  unit can represent any amount of time. This parameter gives an interpretation
     *  of those time units and has units of HLA time units per second.
     *  For example, if the HLA time unit is 1 millisecond, then the value of
     *  this parameter should be 1,000. Ptolemy uses units of seconds, so
     *  the value of this parameter affects the mapping from HLA time units to
     *  time stamps in Ptolemy.
     */
    public Parameter hlaTimeUnit;

    /** A boolean indicating that time advance in this federate is regulated
     *  by other federates in the federation. This defaults to true.
     */
    public Parameter isTimeConstrained;

    /** A boolean indicating that this federate regulates the time advance of
     *  other federates in the federation. This defaults to true.
     */
    public Parameter isTimeRegulator;

    /** Value of the time step of this Federate. This is a double that
     *  defaults to 1.0. This is used only if you select TAR in the
     *  timeManagementService parameter. The units here are HLA logical
     *  time, not seconds. See the hlaTimeUnit parameter.
     *  Normally, hlaTimeStep is larger than or equal to hlaLookAHead
     *  when using TAR.
     */
    public Parameter hlaTimeStep;

    /** Value of the lookahead of this federate. This is a double that
     *  default to 0.1. The units here are HLA logical logical
     *  time, not seconds. See the hlaTimeUnit parameter.
     *  Normally, hlaLookAhead is smaller than or equal to hlaTimeStep
     *  when using TAR.
     */
    public Parameter hlaLookAHead;

    /** If true, kill the HLA runtime infrastructure gateway (RTIG)
     *  in wrapup() that was created in preinitialize. If no RTIG was created
     *  in preinitialize, then ignore this.  This is a boolean that
     *  defaults to the value of launchRTIG.
     */
    public Parameter killRTIG;

    /** If true, launch an HLA runtime infrastructure gateway (RTIG) in preinitialize.
     *  This is a boolean that defaults to false.
     */
    public Parameter launchRTIG;

    /** Choice of time advancement service. This is a string that is one of
     *  "Next Event Request (NER)" or "Time Advancement Request (TAR)".
     *  If TAR is selected, then you need to also provide a time step
     *  in the hlaTimeStep parameter.
     */
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

    /** A boolean indicating whether a log file should be written to the
     *  location defined by the hlaReportPath parameter. This defaults to
     *  false.
     */
    public Parameter enableHlaReporter;

    /** Path and name of the folder to store log files that are generated
     *  if the enableHlaReporter parameter is true. This is a string that
     *  defaults to "$HOME/HLATestResults".
     */
    public FileParameter hlaReportPath;

    /** The name of a synchronization point to which this federate should
     *  synchronize its start. If the name given here is the name of this
     *  federate, then this federate registers itself as a
     *  synchronization point to which other federates can synchronize
     *  their start. The name of this federate will be the name of the
     *  synchronization point, and this federate needs to be the last one
     *  launched. If this parameter contains an empty string,
     *  then this federate starts right away without waiting for any other
     *  federate.  This parameter defaults to an empty string, indicating
     *  that there is no synchronization.
     *
     *  NOTE: All federates in a federation must use the same name here.
     *  If two federates use two different names, then both will block
     *  on initialization. If any one federate uses a blank here, then it
     *  will block the execution of all other federates until it resigns
     *  the federation after completing its execution.
     */
    public StringParameter synchronizeStartTo;

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
            double value = ((DoubleToken) hlaTimeStep.getToken()).doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "hlaTimeStep is required to be strictly positive.");
            }
            _hlaTimeStep = value;
        } else if (attribute == hlaLookAHead) {
            double value = ((DoubleToken) hlaLookAHead.getToken())
                    .doubleValue();
            if (value < 0) {
                throw new IllegalActionException(this,
                        "Cannot have negative value !");
            }
            _hlaLookAHead = value;
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

        newObject._fromFederationEvents = new HashMap<String, LinkedList<TimedEvent>>();

        newObject._objectHandleToClassHandle = new HashMap<Integer, Integer>();
        newObject._registerObjectInstanceMap = new HashMap<String, Integer>();
        newObject._discoverObjectInstanceMap = new HashMap<Integer, String>();

        newObject._usedJokerFilterMap = new HashMap<String, Boolean>();
        newObject._usedJoker = false;

        newObject._hlaReporter = null;

        return newObject;
    }

    /** Initializes the {@link HlaManager} attribute. This method: calls the
     *  _populateHlaAttributeTables() to initialize HLA attributes to publish
     *  or subscribe to; instantiates and initializes the {@link RTIambassador}
     *  and {@link FederateAmbassador} which handle the communication
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

        // Check whether this federate registers a synchronization point
        // to synchronize with the start of other federates.
        _isSynchronizationPointRegister = false;
        boolean needsToSynchronize = true;
        String synchronizationPointName = synchronizeStartTo.stringValue().trim();
        if (synchronizationPointName.equals(_federateName)) {
            _isSynchronizationPointRegister = true;
        } else if (synchronizationPointName.equals("")) {
            needsToSynchronize = false;
        }

        // HLA Reporter support.
        if (_enableHlaReporter) {
            // Get model filename.
            String modelName = getContainer().getFullName();
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
                    _hlaTimeUnitValue, 0.0, _director.getModelStopTime(),
                    _federateName, fedFile.asFile().getPath(), _isSynchronizationPointRegister,
                    _timeStepped, _eventBased);

            _hlaReporter.initializeAttributesToPublishVariables(
                    _hlaAttributesToPublish);
        }

        // Create the Federation if one does not already exist.
        _isFederationCreator = false;
        try {
            _hlaDebug("Creating Federation execution.");
            // Second argument is the location of the FED file on the machine
            // running the RTIG, which is not necessarily the same as the machine
            // running this federate.
            _rtia.createFederationExecution(_federationName,
                    fedFileOnRTIG.stringValue());
            _isFederationCreator = true;
            _hlaDebug("createFederationExecution: FED file on RTIG machine = "
                    + fedFileOnRTIG.stringValue());
        } catch (FederationExecutionAlreadyExists e) {
            _hlaDebug("initialize() - Federation execution already exists. No need to create one.");
        } catch (CouldNotOpenFED e) {
            _hlaDebug("createFederationExecution: RTIG failed to open FED file: "
                        + fedFileOnRTIG.stringValue());
            String more = "";
            if (_preexistingRTI) {
                more = "\nThis federate expected to launch its own RTIG (launchRTIG is true)\n"
                        + "but a pre-existing RTIG was found and we are trying to use that one.\n"
                        + "That pre-existing RTIG cannot find this FED file.\n"
                        + "Try killing the pre-existing RTIG so that a new one can start.\n"
                        + "Alternatively, adjust the CERTI_FOM_PATH used by the RTIG.";
            }
            throw new IllegalActionException(this, e,
                    "RTIG could not find FED file: " + fedFileOnRTIG.stringValue() + more);
        } catch (ErrorReadingFED e) {
            _hlaDebug("createFederationExecution: RTIG failed to open FED file: "
                    + fedFileOnRTIG.stringValue());
            throw new IllegalActionException(this, e,
                    "Error reading FED file.");
        } catch (RTIinternalError e) {
            _hlaDebug("RTI internal error.");
            throw new IllegalActionException(this, e,
                    "RTI internal error.");
        } catch (ConcurrentAccessAttempted e) {
            _hlaDebug("Concurrent access error.");
            throw new IllegalActionException(this, e,
                    "Concurrent access error.");
        }

        _federateAmbassador = new PtolemyFederateAmbassadorInner();

        // Join the Federation.
        try {
            _hlaDebug("Joining the federation.");
            _rtia.joinFederationExecution(_federateName, _federationName,
                    _federateAmbassador);
            _hlaDebug("initialize() - federation joined");
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e,
                    "RTIexception: " + e.getMessage());
        }

        // Initialize the Federate Ambassador.
        try {
            _hlaDebug("Initializing the RTI Ambassador");
            _federateAmbassador.initialize(_rtia);
            isThereArtiX("rtia");
            _hlaDebug("RTI Ambassador initialized.");
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e,
                    "RTIexception: " + e.getMessage());
        }

        // Initialize HLA time management aspects for a Federate
        // (constrained by and/or participate to the time management).
        _initializeTimeAspects();

        // Set initial synchronization point.
        if (needsToSynchronize) {
            _doInitialSynchronization();
        }
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
     *  NOTE: if another HlaManager (e.g. Federate) has already launched an RTIG,
     *  the subprocess created here is no longer required, so we destroy it.
     *  @exception IllegalActionException If the initialization of the
     *  CertiRtig or the execution of the RTIG as subprocess has failed.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        if (System.getenv("CERTI_FOM_PATH") != null) {
            _hlaDebug("preinitialize() - "
                    + "CERTI_FOM_PATH = " + System.getenv("CERTI_FOM_PATH"));
        }
     
        // First, check whether there is already an RTI running.
        _factory = null;
        _certiRtig = null;
        _rtia = null;
        _preexistingRTI = false;
        try {
            _factory = RtiFactoryFactory.getRtiFactory();
        } catch (RTIinternalError e) {
            throw new IllegalActionException(this, e, "Getting RTI factory failed.");
        }
        try {
            _hlaDebug("Creating RTI Ambassador");
            _rtia = (CertiRtiAmbassador) _factory.createRtiAmbassador();
            if (((BooleanToken)launchRTIG.getToken()).booleanValue()) {
                // The model is expecting to launch its own RTIG, but there is
                // already one running. That one is unlikely to know about the
                // FED files used by this federation, so we abort here.
                _hlaDebug("****** WARNING: Expected to launch a new RTIG, "
                        + "but one is running already. Will try using that one.");
                _preexistingRTI = true;
            }
            _hlaDebug("RTI Ambassador created.");
        } catch (Exception e) {
            // If this fails, there is likely no RTI running.
            _hlaDebug("preinitialize() - **** No RTI running.");
            // If set to create one, the create one.
            if (((BooleanToken)launchRTIG.getToken()).booleanValue()) {

                // Request to launch a local RTI.
                // Check for a compatible CERTI_HOST environment variable.
                String certiHost = System.getenv("CERTI_HOST");
                if (certiHost != null
                        && !certiHost.equals("localhost")
                        && !certiHost.equals("127.0.0.1")) {
                    throw new IllegalActionException(this,
                            "The environment variable CERTI_HOST, which has value: "
                            + certiHost
                            + ", is neither localhost nor 127.0.0.1."
                            + " We cannot launch an RTI at that address. "
                            + "You may want to set launchRTIG to false.");
                }
                if (certiHost == null) {
                    certiHost = "localhost";
                }
                _hlaDebug("preinitialize() - Launching CERTI RTI "
                        + " in directory "
                        + System.getProperty("user.dir"));
                // Try to launch the HLA/CERTI RTIG subprocess.
                _certiRtig = new CertiRtig(this, _debugging);
                _certiRtig.initialize(fedFile.asFile().getAbsolutePath());
                _certiRtig.exec();
                _hlaDebug("RTI launched.");
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
                    _hlaDebug("Creating RTI Ambassador");
                    _rtia = (CertiRtiAmbassador) _factory.createRtiAmbassador();
                    _hlaDebug("RTI Ambassador created.");
                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to create RTI Ambassador.");
                }
            } else {
                throw new IllegalActionException(this, "Could not connect to an RTI. "
                        + "Consider setting launchRTIG to true.\n"
                        + "Note that the RTIG relies on a number of environment "
                        + "variables defined in $CERTI_HOME/share/scripts/myCERTI_env.sh.");
            }
        }
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

        // If the proposedTime exceeds the simulation stop time, the simulation
        // must stop. The federate shall not ask to advance to this
        // proposedTime and must return the _stopTime. Notice that no RAV
        // callback with time stamp bigger than the previous received TAG will
        // be evoked.

        if (proposedTime.compareTo(_stopTime) > 0) {
            if (_debugging) {
                _hlaDebug("   proposeTime(" + strProposedTime + ") "
                        + "  > stopTime(" + _stopTime
                        + "): returns proposeTime("+ _stopTime+ "), skip RTI.");
            }
            return _stopTime;
        }

        // If the proposedTime is equal to current time so it has no need to
        // ask for the HLA service then return the currentTime.

        if (currentTime.equals(proposedTime)) {
            // Even if we avoid the multiple calls of the HLA Time management
            // service for optimization, it could be possible to have events
            // from the Federation in the Federate's priority timestamp queue,
            // so we tick() to get these events (if they exist).
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

        // If the HLA Time Management is required, ask to the RTI the
        // authorization to advance its time by invoking TAR or NER service
        // as chosen by the user in the HlaManager interface.
        if (_isTimeRegulator && _isTimeConstrained) {
            synchronized (this) {
                // Call the corresponding HLA Time Management service (NER or TAR).
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
                    // If some attribute is reflected with a time stamp smaller
                    // than the proposedTime, then this method returns that
                    // smaller time stamp (if NER is used).
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
        Time currentTime = _director.getModelTime();

        // The following operations build the different arguments required
        // to use the updateAttributeValues() (UAV) service provided by HLA/CERTI.

        // Retrieve information of the HLA attribute to publish.
        Object[] tObj = _hlaAttributesToPublish.get(updater.getFullName());

        // Encode the value to be sent to the CERTI.
        byte[] bAttributeValue = MessageProcessing.encodeHlaValue(updater, in);
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

        // Create a representation of uav-event timestamp for CERTI.
        // UAV timestamp sent by a HlaUpdatable
        CertiLogicalTime uavTimeStamp = null;

        // Convert Ptolemy time t (Time) to HLA time (double),
        // g(t) = _convertToCertiLogicalTime(t), where t=Ptolemy currentTime
        CertiLogicalTime ptIICurrentTime = _convertToCertiLogicalTime(
                currentTime);

        if (_eventBased) {
            // When the time management NER is used, the time stamp takes always
            // the value ptIICurrentTime + lookahead since HLA says that a
            // federate must promise that no event will be sent before
            // hlaCurrentTime + lookahead. Notice that when using NER,
            // ptIICurrentTime = hlaCurrentTime.
            uavTimeStamp = new CertiLogicalTime(
                    ptIICurrentTime.getTime() + _hlaLookAHead);
        } else {
            // If the time management is TAR (_timeStepped case) the value of
            // uavTimeStamp depends whether (Ptolemy) currentTime is inside or
            // outside the forbidden region [hlaCurrentTime, hlaCurrentTime +
            // lookahead]. If it is inside, uavTimeStamp takes the value
            // (hlaCurrentTime + lookahead), otherwise uavTimeStamp takes the
            // value ptIICurrentTime. Notice that when using TAR, the values
            // of hlaCurrentTime and ptIICurrentTime can be different.

            // h : HLA current logical time provided by the RTI
            CertiLogicalTime hlaCurrentTime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;

            // Calculate the end of the forbidden interval (i.e., earliest value
            // of the uavTimeStamp).
            CertiLogicalTime minimalNextUAVTime = new CertiLogicalTime(
                    hlaCurrentTime.getTime() + _hlaLookAHead);

            // g(t) <  h + lah
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
            _hlaReporter.updateUAVsInformation(updater, in, _getHlaCurrentTime(),
                    currentTime, _director.getMicrostep(), uavTimeStamp);
        }

        // XXX: FIXME: check if we may add the object instance id to the HLA updatable and remove this.
        int instanceHandle = _registerObjectInstanceMap
                .get(updater.getHlaInstanceName());

        try {
            if (_debugging) {
                _hlaDebug("      * UAV '" + updater.getHlaAttributeName()
                        + "', uavTimeStamp=" + uavTimeStamp.getTime()
                        + ", value=" + in.toString() + ", HlaPub="
                        + updater.getFullName());
            }
            // Name to pass to the RTI Ambassador for logging.
            byte[] tag = EncodingHelpers.encodeString(updater.getFullName());
            if (_debugging) {
                _hlaDebug(" tag " + tag);
            }
            // Call HLA service UAV
            _rtia.updateAttributeValues(instanceHandle, suppAttributes, tag,
                    uavTimeStamp);

            if (_enableHlaReporter) {
                _hlaReporter.incrNumberOfUAVs();
            }

        } catch (ObjectNotKnown e) {
            throw new IllegalActionException(this, e,
                    "ObjectNotKnown: " + updater.getHlaInstanceName() + ": "+ e.getMessage());
        } catch (AttributeNotDefined e) {
            throw new IllegalActionException(this, e,
                    "AttributeNotDefined: " + updater.getHlaAttributeName() + ": " + e.getMessage());
        } catch (AttributeNotOwned e) {
            throw new IllegalActionException(this, e,
                    "AttributeNotOwned: " + updater.getHlaAttributeName() + ": " + e.getMessage());
        } catch (InvalidFederationTime e) {
            throw new IllegalActionException(this, e, "InvalidFederationTime: "
                    + e.getMessage() + "    updateHlaAttribute() - sending UAV("
                    + "HLA updatable=" + updater.getFullName() + ",HLA attribute="
                    + updater.getHlaAttributeName() + ",uavTimeStamp="
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
     *  resign a Federation, and destroy a Federation if the current Federate is
     *  the last participant.
     *  @exception IllegalActionException If the parent class throws it
     *  of if a CERTI exception is raised.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_enableHlaReporter) {
            _hlaDebug(_hlaReporter.displayAnalysisValues());
            _hlaReporter.calculateRuntime();
            _hlaReporter.writeNumberOfHLACalls();
            _hlaReporter.writeDelays();
            _hlaReporter.writeUAVsInformation();
            _hlaReporter.writeRAVsInformation();
            _hlaReporter.writeTimes();
        }

        try {
            // Unsubscribe to HLA attributes.
            _hlaDebug("wrapup() - Unsubscribing to HLA attributes.");
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
            _hlaDebug("wrapup() - Unpublishing HLA attributes.");
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
        } finally {
            // Resign HLA/CERTI Federation execution.
            try {
                // _rtia can be null if we are exporting to JNLP.
                if (_rtia != null) {
                    _hlaDebug("wrapup() - Resigning Federation execution");
                    _rtia.resignFederationExecution(
                            ResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES);
                    _hlaDebug("wrapup() - Resigned Federation execution");
                }
            } catch (RTIexception e) {
                _hlaDebug("wrapup() - RTIexception.");
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            } finally {
                // Destroy the federation execution if this was the federate
                // that created it. This will wait
                // until all federates have resigned the federation
                // or until the model execution is stopped.
                boolean federationIsActive = true;
                try {
                    while (federationIsActive && _isFederationCreator) {

                        // Destroy federation execution.
                        try {
                            _hlaDebug("wrapup() - Destroying the federation.");
                            _rtia.destroyFederationExecution(_federationName);
                            federationIsActive = false;
                            _hlaDebug("wrapup() - Federation destroyed by this federate.");

                        } catch (FederatesCurrentlyJoined e) {
                            _hlaDebug("wrapup() - Federates are still joined to the federation."
                                    + " Wait some time and try again to destroy the federation.");

                            if (_director.isStopRequested()) {
                                _hlaDebug("wrapup() - Federate was stopped by the user.");
                                break;
                            }
                            try {
                                // Give the other federates a chance to finish.
                                Thread.sleep(2000l);
                            } catch (InterruptedException e1) {
                                // Ignore.
                            }
                        } catch (FederationExecutionDoesNotExist e) {
                            // No more federation. Some other federate must have
                            // succeeded in destroying it.
                            _hlaDebug("wrapup() - Federation was destroyed by some other federate.");
                            federationIsActive = false;
                        } catch (RTIinternalError e) {
                            throw new IllegalActionException(this, e,
                                    "RTIinternalError: " + e.getMessage());
                        } catch (ConcurrentAccessAttempted e) {
                            throw new IllegalActionException(this, e,
                                    "ConcurrentAccessAttempted: " + e.getMessage());
                        }
                    }
                } finally {
                    try {
                        // Clean HLA attribute tables.
                        _hlaAttributesToPublish.clear();
                        _hlaAttributesToSubscribeTo.clear();
                        _fromFederationEvents.clear();
                        _objectHandleToClassHandle.clear();

                        // Clean HLA object instance id maps.
                        _registerObjectInstanceMap.clear();
                        _discoverObjectInstanceMap.clear();

                        // Joker wildcard support.
                        _usedJokerFilterMap.clear();

                        // HLA Reporter support.
                        _hlaReporter = null;

                        // Close the connection socket connection between jcerti (the Java
                        // proxy for the ambassador) and certi.
                        // Sadly, this nondeterministically triggers an IOException:
                        _rtia.closeConnexion();
                    } finally {
                        // Terminate RTIG subprocess.
                        if (_certiRtig != null && ((BooleanToken)killRTIG.getToken()).booleanValue()) {
                            // CERTI seems to require some time for destroying the
                            // federation, done above, to settle.
                            try {
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {
                                // Continue to the kill.
                            }
                            _hlaDebug("**** Killing the RTIG process (if authorized by the system)");
                            _certiRtig.terminateProcess();
                        }
                      _hlaDebug("----------------------- End execution.");
                        
                    }
                }
            }
        }
    }

    /** Execute "ps -ax" then check if there is a "rtia" process running
     *  Print whether there is or not a rtia running.
     */
public void isThereArtiX(String msg) {
    //FIXME This way to run a command works with macos Sierra, centos 7 and debian 9
    // it does not work with Windows. Need to add the same code as in CertiRtig.java
System.out.flush();
    try {
        String process;
        // When using '|' (pipe) we need to indicate the shell
        // /bin/sh seems not to work on Debian
        //String[] cmd = {"/bin/sh","-c", "ps -ax | grep rtia | grep -v grep"} ;
        String[] cmd = {"/bin/bash","-c", "ps -ax | grep rtia | grep -v grep"} ;
        Process p = Runtime.getRuntime().exec(cmd);
        int nbFound=0; //
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((process = input.readLine()) != null) {
                System.out.println(process);
                if (process.contains(msg))
                        nbFound ++;
                }
                input.close();
                if (nbFound > 0)
                    System.out.println("---- HlaManager: " + nbFound + msg + " process was/were found");
                else {System.out.println("===== HlaManager: No " + msg + " processes found");}
                nbFound = 0;
       } catch (Exception err) {
               err.printStackTrace();
         }
   } 

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // FIXME: A better design for the following would have _hlaAttributesToPublish
    // be a LinkedHashMap<HlaUpdater,HLAAttribute>, where
    // HLAAttribute is an inner class with the three items
    // provided by the RTI.
    /** Table of HLA attributes (and their HLA information) that are published
     *  by the current {@link HlaManager} to the HLA/CERTI Federation. This
     *  table is indexed by the {@link HlaUpdatable} actors present in the model.
     */
    protected HashMap<String, Object[]> _hlaAttributesToPublish;

    /** Table of HLA attributes (and their HLA information) that the current
     *  {@link HlaManager} is subscribed to. This table is indexed by the
     *  {@link HlaReflectable} actors present in the model.
     */
    protected HashMap<String, Object[]> _hlaAttributesToSubscribeTo;

    /** List of events received from the HLA/CERTI Federation and indexed by the
     *  {@link HlaReflectable} actors present in the model.
     */
    protected HashMap<String, LinkedList<TimedEvent>> _fromFederationEvents;

    /** Table of object class handles associate to instance handles received by
     *  discoverObjectInstance and reflectAttributesValues services (from
     *  the RTI).
     */
    protected HashMap<Integer, Integer> _objectHandleToClassHandle;

    /** Table of used joker (wildcard) filter. */
    protected HashMap<String, Boolean> _usedJokerFilterMap;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Convert Ptolemy time, which has units of seconds, to HLA logical time
     *  units. Ptolemy time is implemented using Java classe Time and HLA time
     *  uses IEEE-754 double.
     *  @param pt The Ptolemy time.
     *  @return The time in units of HLA logical time.
     */
    private CertiLogicalTime _convertToCertiLogicalTime(Time pt) {
        return new CertiLogicalTime(pt.getDoubleValue() * _hlaTimeUnitValue);
    }

    /** Convert CERTI (HLA) logical time (IEEE-754 double) to Ptolemy time.
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

        // The director global time resolution may be used later in the
        // particular case where HLA time advances but Ptolemy times does not.
        Double r = _director.getTimeResolution();

        // Read current Ptolemy time and HLA time (provided by the RTI)
        Time ptolemyTime = _director.getModelTime();
        CertiLogicalTime hlaLogicaltime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;

        // Convert proposedTime to double so it can be compared with HLA time
        CertiLogicalTime certiProposedTime = _convertToCertiLogicalTime(
                proposedTime);

        // Call the NER service if the time Ptolemy wants to advance to is
        // bigger than current HLA time
        if (certiProposedTime.isGreaterThan(hlaLogicaltime)) {
            // Wait the time grant from the HLA/CERTI Federation (from the RTI).
            _federateAmbassador.timeAdvanceGrant = false;

            if (_enableHlaReporter) {
                // Set time of last time advance request.
                _hlaReporter.setTimeOfTheLastAdvanceRequest(System.nanoTime());
            }

            // Call CERTI NER HLA service.
            _rtia.nextEventRequest(certiProposedTime);

            if (_enableHlaReporter) {
                // Increment counter of NER calls.
                _hlaReporter.incrNumberOfNERs();
            }

            while (!(_federateAmbassador.timeAdvanceGrant)) {
                if (_director.isStopRequested()) {
                    // Stop is requested by the user.
                    // Not sure what to do here, but we can't just keep waiting.
                    throw new IllegalActionException(this,
                            "Stop requested while waiting for a time advance grant from the RTIG.");
                }
                if (_debugging) {
                    _hlaDebug("        proposeTime(t(lastFoundEvent)="
                            + strProposedTime + ") - _eventsBasedTimeAdvance("
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
                _rtia.tick(MAX_BLOCKING_TIME, 0);

                // HLA Reporter support.
                if (_enableHlaReporter) {
                    _hlaReporter._numberOfTicks2++;
                    _hlaReporter._numberOfTicks.set(_hlaReporter._numberOfTAGs,
                            _hlaReporter._numberOfTicks
                                    .get(_hlaReporter._numberOfTAGs) + 1);
                }

            } // end while

            // A TAG was received then the HLA current time is updated for this federate
            _federateAmbassador.hlaLogicalTime = _federateAmbassador.grantedHlaLogicalTime;

            // If a RAV was received, the time stamp of TAG is the same as the
            // RAV, and in the general case, the proposedTime will return with
            // this time stamp (converted to Time, the Ptolemy time representation).
            if (_federateAmbassador.hasReceivedRAV) {
                Time newPtolemyTime = _convertToPtolemyTime(
                        (CertiLogicalTime) _federateAmbassador.grantedHlaLogicalTime);

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
                _putReflectedAttributesOnHlaReflectables(proposedTime);

                _federateAmbassador.hasReceivedRAV = false;

            } // end  if receivedRAV then

        } //  end if

        return proposedTime;

    }

    /** Get the current time in HLA (using double) which is advanced after a TAG callback.
     *  @return the HLA current time converted to a Ptolemy time, which is in
     *   units of seconds (and uses Java class Time).
     */

    private Time _getHlaCurrentTime() throws IllegalActionException {
        CertiLogicalTime certiCurrentTime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;
        return _convertToPtolemyTime(certiCurrentTime);
    }

    /** The method {@link #_getHlaReflectables()} get all HLA reflectables
     *  actors across the model.
     *  @param ce the composite entity which may contain HlaReflectables
     *  @return the list of HlaReflectables
     */
    private List<HlaReflectable> _getHlaReflectables(CompositeEntity ce) {
        // The list of HLA reflectables to return.
        LinkedList<HlaReflectable> hlaReflectables = new LinkedList<HlaReflectable>();

        // List all classes from top level model.
        List<CompositeEntity> entities = ce.entityList();
        for (ComponentEntity classElt : entities) {
            if (classElt instanceof HlaReflectable) {
                hlaReflectables.add((HlaReflectable) classElt);
            } else if (classElt instanceof ptolemy.actor.TypedCompositeActor) {
                hlaReflectables
                        .addAll(_getHlaReflectables((CompositeEntity) classElt));
            }
        }

        return hlaReflectables;
    }

    /** Customized debug message for {@link #HlaManager}.
     *  This will send the message to debug listeners if there are any.
     *  Otherwise, it sends the message to standard out.
     *  @param reason The reason to print
     */
    private void _hlaDebug(String reason) {
        String dbgHeader = "Federate: " + _federateName + " - Federation: " + _federationName + " - ";
        if (_debugging) {
            _debug(dbgHeader + reason);
        } else {
            System.out.println(dbgHeader + reason);
        }
    }


    /** Time advancement method for time-stepped federates. This method
     *  uses TAR RTI service to propose a time t' to advance to
     *  in a HLA simulation. The federate wants to advance to
     *  <i>proposedTime</i> and it returns t' (if granted by the RTI) even if
     *  RAV(t''), t'' &lt;  t', are received in this time advance
     *  phase. All RAV received are put in the HlaReflectable actors.
     *  If <i>proposedTime</i> is equal to or greater than <i>N</i> times
     *  <i>hlaTimeStep</i> and less than <i>(N + 1)</i> times <i>hlaTimeStep</i>,
     *  for any integer <i>N</i>, then TAR will be called <i>N</i> times.
     *  This is why, when TAR is used, the time lines can be different: Ptolemy
     *  time is equal to <i>proposedTime</i> but HLA time is equal to <i>N</i>
     *  times <i>hlaTimeStep</i>. See [6], algorithm 4.
     *
     *  @param proposedTime time stamp of last found event
     *  @return A valid time to advance to.
     *  @exception IllegalActionException If the user requests that the execution
     *   stop while we are waiting for time-advance callbacks from the RTI.
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

        // Convert proposedTime to double so it can be compared with HLA time
        CertiLogicalTime certiProposedTime = _convertToCertiLogicalTime(
                proposedTime);

        // Read current HLA time (provided by the RTI)
        CertiLogicalTime hlaLogicaltime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;

        // Set the value of next HLA point in time (when using TAR)
        CertiLogicalTime nextPointInTime = new CertiLogicalTime(
                hlaLogicaltime.getTime() + _hlaTimeStep);

        // NOTE: Microstep reset problem
        //  To retrieve the old behavior with the microstep reset problem, you may change the line below:
        //  reset    => while (certiProposedTime.isGreaterThan(nextPointInTime)) {
        //  no reset => while (certiProposedTime.isGreaterThanOrEqualTo(nextPointInTime)) {

        if (_debugging) {
            _hlaDebug("Before While g(t') >= h+TS; g(t')= "
                    + certiProposedTime.getTime() + "; h+TS= "
                    + nextPointInTime.getTime() + " @ " + headMsg);
        }
        // Call as many TAR as needed for allowing Ptolemy time to advance to
        // proposedTime
        while (certiProposedTime.isGreaterThanOrEqualTo(nextPointInTime)) {
            // Wait the time grant from the HLA/CERTI Federation (from the RTI).
            _federateAmbassador.timeAdvanceGrant = false;

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

            while (!(_federateAmbassador.timeAdvanceGrant)) {
                if (_director.isStopRequested()) {
                    // Stop is requested by the user.
                    // Not sure what to do here, but we can't just keep waiting.
                    throw new IllegalActionException(this,
                            "Stop requested while waiting for a time advance grant from the RTIG.");
                }
                if (_debugging) {
                    _hlaDebug("      waiting for callbacks in " + headMsg);
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
                    _rtia.tick(MAX_BLOCKING_TIME, 0);

                    // HLA Reporter support.
                    if (_enableHlaReporter) {
                        _hlaReporter._numberOfTicks2++;
                        _hlaReporter._numberOfTicks
                                .set(_hlaReporter._numberOfTAGs,
                                        _hlaReporter._numberOfTicks
                                                .get(_hlaReporter._numberOfTAGs)
                                                + 1);
                    }

                } catch (ConcurrentAccessAttempted | RTIinternalError e) {
                    throw new IllegalActionException(this, e, e.getMessage());
                }

            } //  end while

            // A TAG was received then the HLA current time is updated for this federate
            _federateAmbassador.hlaLogicalTime = nextPointInTime;

            // If one or more RAV are received, HLA guarantees that their time
            // stamps will never be smaller than the current HLA time h (neither
            // larger than nextPointInTime). And proposeTime method must return
            // the same proposed time or a smaller time.
            // This guarantees that the received time stamp is smaller. If,
            // because of the time conversion, the newPtolemyTime appears
            // as bigger than the proposedTime, keep proposedTime. Otherwise
            // update it to newPtolemyTime.
            if (_federateAmbassador.hasReceivedRAV) {

                // Convert HLA time to Time so it can be compared with Ptolemy time
                Time newPtolemyTime = _convertToPtolemyTime(
                        (CertiLogicalTime) _federateAmbassador.hlaLogicalTime);

                // In the general case newPtolemyTime is smaller than proposedTime
                // and proposedTime is updated to newPtolemyTime. This value will
                // be the time stamp on the output of the HlaReflectable actor.
                if (newPtolemyTime.compareTo(proposedTime) < 0) {
                    if (_debugging) {
                        _hlaDebug("    newPtolemyTime= t'=t''=f(h)="
                                + newPtolemyTime.toString()
                                + " @line 10 in algo 4 " + headMsg);
                    }
                    proposedTime = newPtolemyTime;
                }

                // Store reflected attributes RAV as events on HlaReflectable actors.
                // Notice that proposedTime here is a multiple of _hlaTimeStep.
                _putReflectedAttributesOnHlaReflectables(proposedTime);

                // Reinitialize variable
                _federateAmbassador.hasReceivedRAV = false;

                // Ptolemy time is updated
                if (_debugging) {
                    _hlaDebug("Returns proposedTime=" + proposedTime.toString()
                            + "    (if hasReceivedRAV) " + headMsg
                            + "\n");
                }
                return proposedTime;

            } // end if receivedRAV then

            // Update local variables with the new HLA logical time.
            hlaLogicaltime = (CertiLogicalTime) _federateAmbassador.hlaLogicalTime;
            nextPointInTime = new CertiLogicalTime(
                    hlaLogicaltime.getTime() + _hlaTimeStep);

        } // end while:

        if (_debugging) {
            _hlaDebug("returns proposedTime=" + proposedTime.toString() + "from "
                    + headMsg);
        }

        // All needed TAR were called. Update Ptolemy time to the time the
        // federate asked to advance.
        return proposedTime;
    }

    /** The method {@link #_populatedHlaValueTables()} populates the tables
     *  containing information required to publish and to
     *  subscribe to attributes of a class in a HLA Federation.
     *  @exception IllegalActionException If a HLA attribute is declared twice.
     */
    private void _populateHlaAttributeTables() throws IllegalActionException {
        CompositeEntity ce = (CompositeEntity) getContainer();

        // HlaUpdatables.
        _hlaAttributesToPublish.clear();
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

            _hlaAttributesToPublish.put(updater.getFullName(),

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
        _hlaAttributesToSubscribeTo.clear();

        List<HlaReflectable> _hlaReflectables = _getHlaReflectables(ce);

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

                    throw new IllegalActionException(this, "A HlaReflectable '"
                            + hsIndex.getFullName()
                            + "' with the same HLA information specified by the "
                            + "HlaReflectable '" + hs.getFullName()
                            + "' \nis already registered for subscription.");
                }
            }

            // Only one output port is allowed per HlaReflectable actor.
            TypedIOPort tiop = hs.getOutputPort();

            _hlaAttributesToSubscribeTo.put(hs.getFullName(),

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
            _fromFederationEvents.put(hs.getFullName(),
                    new LinkedList<TimedEvent>());

            // Joker wildcard support.
            _usedJoker = false;

            String instanceNameOrJokerName = hs.getHlaInstanceName();

            if (instanceNameOrJokerName.contains(_jokerFilter)) {
                _usedJoker = true;
                if (_debugging) {
                    _hlaDebug("HLA actor " + hs.getFullName()
                            + " uses joker wildcard = " + _jokerFilter);
                }
            }

            if (_usedJoker) {
                if (!instanceNameOrJokerName.contains(_jokerFilter)) {
                    throw new IllegalActionException(this,
                            "Cannot mix class instance name and joker filter in HlaReflectable "
                                    + "please check: " + hs.getFullName());
                } else {
                    // Add a new discovered joker to the joker table.
                    _usedJokerFilterMap.put(instanceNameOrJokerName, false);
                }
            }
        }
    }

    /** This method is called when a time advancement phase is performed. Every
     *  updated HLA attribute received by callbacks (from the RTI) during the
     *  time advancement phase is saved as a {@link TimedEvent} and stored in a
     *  queue. Then, every {@link TimedEvent} is moved from this queue to the
     *  output port of their corresponding {@link HlaReflectable} actors
     *  @exception IllegalActionException If the parent class throws it.
     */
    private void _putReflectedAttributesOnHlaReflectables(Time proposedTime)
            throws IllegalActionException {
        // Reflected HLA attributes, i.e., updated values of HLA attributes
        // received by callbacks (from the RTI) from the whole HLA/CERTI
        // Federation, are stored in a queue (see reflectAttributeValues()
        // in PtolemyFederateAmbassadorInner class).

        if (_debugging) {
            _hlaDebug("       t_ptII = " + _director.getModelTime().toString()
                    + "; t_hla = " + _federateAmbassador.hlaLogicalTime
                    + " in _putReflectedAttributesOnHlaReflectables("
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

                // Update received RAV event timestamp (see [6]).
                if (_timeStepped) {
                    // The Ptolemy event corresponding to a RAV has
                    // time stamp equal to HLA current time, e(f(h+TS))
                    ravEvent.timeStamp = _getHlaCurrentTime();
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
                        .compareTo(_director.getModelStartTime()) < 0) {
                    ravEvent.timeStamp = _director.getModelStartTime();
                }

                // Get the HlaReflectable actor to which the event is destined to.
                String actorName = elt.getKey();

                TypedIOPort tiop = _getPortFromTab(
                        _hlaAttributesToSubscribeTo.get(actorName));

                HlaReflectable hs = (HlaReflectable) tiop.getContainer();
                hs.putReflectedHlaAttribute(ravEvent);

                if (_debugging) {
                    _hlaDebug("       _putRAVOnHlaReflectable(" + proposedTime.toString()
                            + " ravEvent.timeStamp=" + ravEvent.timeStamp
                            + ") for '" + hs.getHlaAttributeName()
                            + " in HlaSubs=" + hs.getFullName());
                }

                if (_enableHlaReporter) {
                    _hlaReporter.updateFolRAVsTimes(ravEvent.timeStamp);
                }

                // Remove handled event.
                events.removeFirst();
            }
        }

        // At this point we have handled all events for all registered,
        // HlaReflectable actors, so we may clear the receivedRAV boolean.
        _federateAmbassador.hasReceivedRAV = false;

        if (_debugging) {
            _hlaDebug("        _putRAVOnHlaSubs(" + proposedTime.toString()
                    + ") - no more RAVs to deal with");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Name of the current Ptolemy federate ({@link HlaManager}). */
    private String _federateName;

    /**-Name of the HLA/CERTI federation to create or to join. */
    private String _federationName;

    /** Federate Ambassador for the Ptolemy Federate. */
    private PtolemyFederateAmbassadorInner _federateAmbassador;

    /** RTI Ambassador for the Ptolemy Federate. */
    private CertiRtiAmbassador _rtia;

    /** Indicates the use of the nextEventRequest() service. */
    private boolean _eventBased;

    /** Indicates the use of the timeAdvanceRequest() service. */
    private boolean _timeStepped;

    /** The lookahead value of the federate in HLA logical time units. */
    private double _hlaLookAHead;

    /** Time step in units of HLA logical time. */
    private double _hlaTimeStep;

    /** Marker that this federate is the one that created the federation. */
    private boolean _isFederationCreator = false;

    /** Indicates the use of the enableTimeConstrained() service. */
    private boolean _isTimeConstrained;

    /** Indicates the use of the enableTimeRegulation() service. */
    private boolean _isTimeRegulator;

    /** Indicates if the Ptolemy Federate is the register of the synchronization
     *  point. */
    private boolean _isSynchronizationPointRegister;

    /** RTI factory, set up in preinitialize(). */
    private RtiFactory _factory;

    /** The simulation stop time. */
    private Time _stopTime;

    /** A reference to the enclosing director. */
    private DEDirector _director;

    /** The RTIG subprocess. */
    private CertiRtig _certiRtig;

    /** Map class instance name and object instance handle. Those information are set
     *  using discoverObjectInstance() callback and used by the RAV service.
     */
    private HashMap<Integer, String> _discoverObjectInstanceMap;

    /**
     * Map <Sender actor + HlaUpdatable> and registerObjectInstance (ROI)
     * handle for an object instance. See HlaPublisher and HlaAttributeUpdater.
     *
     * HashMap for HlaPublisher to remember which actor's ID has
     * been registered (as an object instance) to the Federation.
     */
    private HashMap<String, Integer> _registerObjectInstanceMap;

    /** The actual value for hlaTimeUnit parameter. */
    private double _hlaTimeUnitValue;

    /**
     * The reserved keyword to filter HlaReflectable actors using joker
     * wildcard.
     */
    private static final String _jokerFilter = "joker_";

    /** Indicates if the 'joker' filter is used in the classInstanceName
     * parameter of a HlaReflectable actor.
     */
    private boolean _usedJoker;

    /** The HLA reporter instance if enabled. */
    private HlaReporter _hlaReporter;

    /** Indicates if the HLA reporter is enabled or not. */
    private boolean _enableHlaReporter;

    /** Indicator that we are trying to use a preexisting RTI when we expected to launch our own. */
    private boolean _preexistingRTI;

    ///////////////////////////////////////////////////////////////////
    ////                    private  methods                       ////

    /** This method does the initial synchronization among the
     *  federates and registers the synchronization point if this federate's
     *  name matches the synchronizeStartTo parameter.
     *  @exception IllegalActionException If the RTI throws an exception.
     */
    private void _doInitialSynchronization() throws IllegalActionException {
        String synchronizationPointName = synchronizeStartTo.stringValue().trim();
        // If the current Federate is the register of a synchronization point,
        // then register the synchronization point.
        if (_isSynchronizationPointRegister) {
            try {
                byte[] rfspTag = EncodingHelpers
                        .encodeString(synchronizationPointName);
                _rtia.registerFederationSynchronizationPoint(
                        synchronizationPointName, rfspTag);

                // Wait for synchronization point callback.
                while (!(_federateAmbassador.synchronizationSuccess)
                        && !(_federateAmbassador.synchronizationFailed)) {
                    if (_director.isStopRequested()) {
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
                    _rtia.tick(MAX_BLOCKING_TIME, 0);

                    _logOtherTicks();
                }
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }

            if (_federateAmbassador.synchronizationFailed) {
                throw new IllegalActionException(this,
                        "CERTI: Synchronization failed! ");
            }
        } // End block for synchronization point creation case.

        // The first launched federates wait for synchronization point announcement.
        while (!(_federateAmbassador.inPause)) {
            if (_director.isStopRequested()) {
                // Stop is requested by the user.
                // Not sure what to do here, but we can't just keep waiting.
                throw new IllegalActionException(this,
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
                _rtia.tick(MAX_BLOCKING_TIME, 0);

                _logOtherTicks();
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
                        "RTIexception: " + e.getMessage());
            }
        }

        // Satisfied synchronization point.
        try {
            _rtia.synchronizationPointAchieved(synchronizationPointName);
            if (_debugging) {
                _hlaDebug("_doInitialSynchronization() - initialize() - Synchronisation point "
                        + synchronizationPointName + " satisfied");
            }
        } catch (RTIexception e) {
            throw new IllegalActionException(this, e,
                    "RTIexception: " + e.getMessage());
        }

        // Wait for federation synchronization.
        while (_federateAmbassador.inPause) {
            if (_director.isStopRequested()) {
                // Return to finish initialization so that we proceed to wrapup
                // and resign the federation.
                return;
            }
            if (_debugging) {
                _hlaDebug("_doInitialSynchronization() - initialize() - Waiting for simulation phase");
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
                _rtia.tick(MAX_BLOCKING_TIME, 0);

                _logOtherTicks();
            } catch (RTIexception e) {
                throw new IllegalActionException(this, e,
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

        // Declare the Federate to be a time regulator (if true).
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
        // method to receive callbacks from the RTI.
        if (_isTimeRegulator && _isTimeConstrained) {
            while (!(_federateAmbassador.timeConstrained)) {
                if (_director.isStopRequested()) {
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
                    _rtia.tick(MAX_BLOCKING_TIME, 0);

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
                if (_director.isStopRequested()) {
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
                    _rtia.tick(MAX_BLOCKING_TIME, 0);

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
            // callbacks from the RTI when a Federate is time constrained.
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

    /** Check that the specified label matches the synchronization point
     *  name given in the synchronizeStartTo parameter.
     *  @param synchronizationPointLabel The label.
     *  @return False if the names don't match.
     */
    private boolean _checkSynchronizationPointNameMatch(String synchronizationPointLabel) {
        try {
            String mySynchronizationPoint = synchronizeStartTo.stringValue();
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
    static private String _getHlaClassNameFromTab(Object[] array) {
        return (String) array[2];
    }

    /** Simple getter function to retrieve the class instance name from
     *  the opaque Object[] array.
     *  @param array the opaque Object[] array
     *  @return the class instance name as String
     */
    static private String _getHlaInstanceNameFromTab(Object[] array) {
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

    /** Use the HLA reporter class to log an "other tick".
     */
    private void _logOtherTicks() {
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

            // Configure HlaUpdatable actors from model */
            if (!_hlaAttributesToPublish.isEmpty()) {
                _setupHlaUpdatables(rtia);
            } else {
                if (_debugging) {
                    _hlaDebug("INNER initialize: _hlaAttributesToPublish is empty");
                }
            }
            // Configure HlaReflectable actors from model */
            if (!_hlaAttributesToSubscribeTo.isEmpty()) {
                _setupHlaReflectables(rtia);
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
            if (_hlaLookAHead <= 0) {
                throw new IllegalActionException(null, null, null,
                        "LookAhead field in HLAManager must be greater than 0.");
            }
            hlaLogicalTime = new CertiLogicalTime(startTime);
            grantedHlaLogicalTime = new CertiLogicalTime(0);
            // The hlaLookAHead is already in  HLA logical time units.
            effectiveLookAHead = new CertiLogicalTimeInterval(_hlaLookAHead);
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
            // the received "theObject" handle.
            int classHandle = _objectHandleToClassHandle.get(theObject);
            String instanceNameOrJokerName = _discoverObjectInstanceMap
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
                    HlaReflectable hs = (HlaReflectable) _getPortFromTab(tObj)
                            .getContainer();

                    if (_debugging) {
                        _hlaDebug("INNER callback: reflectAttributeValues():"
                                + " theObject=" + theObject
                                + " theAttributes" + theAttributes
                                + " userSuppliedTag=" + userSuppliedTag
                                + " theTime=" + theTime
                                + " classHandle=" + classHandle
                                + " instanceNameOrJokerName=" + instanceNameOrJokerName
                                + " HlaSusbcriber=" + hs.getFullName());
                    }

                    // The tuple (attributeHandle, classHandle, classInstanceName)
                    // allows to identify the object attribute (i.e. one of the HlaReflectables)
                    // where the updated value has to be put.
                    try {
                        if (theAttributes.getAttributeHandle(i) == hs
                                .getAttributeHandle()
                                && classHandle == hs.getClassHandle()
                                && (instanceNameOrJokerName != null
                                        && hs.getHlaInstanceName().compareTo(
                                                instanceNameOrJokerName) == 0)) {

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
                                _hlaDebug("       *RAV '" + hs.getHlaAttributeName()
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
                        //_hlaDebug(
                        //        "INNER callback: reflectAttributeValues(): EXCEPTION ArrayIndexOutOfBounds");
                        //e.printStackTrace();

                        throw new FederateInternalError(
                                "INNER callback: reflectAttributeValues(): EXCEPTION ArrayIndexOutOfBounds: "
                                        + e.getMessage());
                    } catch (IllegalActionException e) {
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
        public void discoverObjectInstance(int instanceHandle,
                int classHandle, String someName) throws CouldNotDiscover,
                ObjectClassNotKnown, FederateInternalError {
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
                                + " instanceHandle=" + instanceHandle
                                + " classHandle=" + classHandle + " someName="
                                + someName
                                + " will be ignored during the simulation.");
                    }
                } else {
                    _discoverObjectInstanceMap.put(instanceHandle,
                            jokerFilter);
                    if (_debugging) {
                        _hlaDebug("INNER callback: discoverObjectInstance: instanceHandle="
                                + instanceHandle + " jokerFilter="
                                + jokerFilter + " matchingName="
                                + matchingName);
                    }

                    matchingName = jokerFilter;
                }
            } else {
                // Nominal case, an instance name was defined in the callback discoverObjectInstance.
                if (_discoverObjectInstanceMap.containsKey(instanceHandle)) {
                    if (_debugging) {
                        _hlaDebug("INNER callback: discoverObjectInstance: found an instance class already registered: "
                                + someName);
                    }
                    // Note: this case should not happen with the new implementation from CIELE. But as it is
                    // difficult to test this case, we raise an exception.
                    throw new FederateInternalError(
                            "INNER callback: discoverObjectInstance(): EXCEPTION IllegalActionException: "
                                    + "found an instance class already registered: "
                                    + someName);

                } else {
                    _discoverObjectInstanceMap.put(instanceHandle, someName);
                    matchingName = someName;
                }

            }

            // Bind object instance handle to class handle.
            _objectHandleToClassHandle.put(instanceHandle, classHandle);

            // Joker support
            if (matchingName != null) {
                // Get classHandle and attributeHandle for each attribute
                // value to subscribe to. Update the HlaReflectable.
                Iterator<Entry<String, Object[]>> it1 = _hlaAttributesToSubscribeTo
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
                            sub.setInstanceHandle(instanceHandle);

                            if (_debugging) {
                                _hlaDebug("INNER callback: discoverObjectInstance: matchingName="
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

            if (_debugging) {
                _hlaDebug("INNER callback:"
                        + " discoverObjectInstance(): the object"
                        + " instanceHandle=" + instanceHandle
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
           if (!_checkSynchronizationPointNameMatch(synchronizationPointLabel)) {
               return;
           }
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
            if (!_checkSynchronizationPointNameMatch(synchronizationPointLabel)) {
                return;
            }
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
            if (!_checkSynchronizationPointNameMatch(synchronizationPointLabel)) {
                return;
            }
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
            if (!_checkSynchronizationPointNameMatch(synchronizationPointLabel)) {
                return;
            }
            if (_debugging) {
                _hlaDebug("INNER callback: federationSynchronized(): inPause = "
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
        private void _setupHlaUpdatables(RTIambassador rtia)
                throws ObjectClassNotDefined, FederateNotExecutionMember,
                RTIinternalError, SaveInProgress, RestoreInProgress,
                ConcurrentAccessAttempted, IllegalActionException {

            // For each HlaUpdatable actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to publish.

            // 1. Get classHandle and attributeHandle for each attribute
            //    value to publish. Update the HlaUpdatables
            //    table with the information.
            Iterator<Entry<String, Object[]>> it = _hlaAttributesToPublish
                    .entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Object[]> elt = it.next();
                // elt.getKey()   => HlaUpdatable actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // Get corresponding HlaUpdatable actor.
                HlaUpdatable pub = (HlaUpdatable) _getPortFromTab(tObj)
                        .getContainer();

                if (_debugging) {
                    _hlaDebug("_setupHlaUpdatables() - HlaUpdatable: "
                            + pub.getFullName());
                }

                // Object class handle and attribute handle are numerical (int)
                // representation, provided by the RTIA, for object classes
                // and object class attributes that appear in the FED file.

                // Retrieve HLA class handle from RTI.
                int classHandle = Integer.MIN_VALUE;

                try {
                    classHandle = rtia
                            .getObjectClassHandle(pub.getHlaClassName());

                    if (_debugging) {
                        _hlaDebug("_setupHlaUpdatables() "
                                + "objectClassName (in FOM) = "
                                + pub.getHlaClassName() + " - classHandle = "
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
                            pub.getHlaAttributeName(), classHandle);

                    if (_debugging) {
                        _hlaDebug("_setupHlaUpdatables() " + " attributeHandle = "
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
                // tObj[3] => instance class name to which this attribute belongs,
                // tObj[4] => object class handle,
                // tObj[5] => attribute handle.

                // tObj[0 .. 3] are extracted from the Ptolemy model.
                // tObj[3 .. 5] are provided by the RTI (CERTI).

                // FIXME: See FIXME below for a better design.

                // All these information are required to publish/unpublish
                // updated value of a HLA attribute.
                elt.setValue(new Object[] { _getPortFromTab(tObj),
                        _getTypeFromTab(tObj), _getHlaClassNameFromTab(tObj),
                        _getHlaInstanceNameFromTab(tObj), classHandle,
                        attributeHandle });
            }

            // 2.1 Create a table of HlaUpdatables indexed by their corresponding
            //     classInstanceName (no duplication).
            HashMap<String, LinkedList<String>> classInstanceNameHlaUpdatableTable = new HashMap<String, LinkedList<String>>();

            Iterator<Entry<String, Object[]>> it21 = _hlaAttributesToPublish
                    .entrySet().iterator();

            while (it21.hasNext()) {
                Map.Entry<String, Object[]> elt = it21.next();
                // elt.getKey()   => HlaUpdatable actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // Get corresponding HlaUpdatable actor.
                HlaUpdatable pub = (HlaUpdatable) _getPortFromTab(tObj)
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
            HashMap<Integer, LinkedList<String>> classHandleHlaPublisherTable = new HashMap<Integer, LinkedList<String>>();

            Iterator<Entry<String, Object[]>> it22 = _hlaAttributesToPublish
                    .entrySet().iterator();

            while (it22.hasNext()) {
                Map.Entry<String, Object[]> elt = it22.next();
                // elt.getKey()   => HlaUpdatable actor full name.
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
                // elt.getValue() => list of HlaUpdatable actor full names.
                LinkedList<String> hlaUpdatableFullnames = elt.getValue();

                // The attribute handle set to declare all attributes to publish
                // for one object class.
                AttributeHandleSet _attributesLocal = _factory.createAttributeHandleSet();

                // Fill the attribute handle set with all attribute to publish.
                for (String sPub : hlaUpdatableFullnames) {
                    try {
                        _attributesLocal.add(_getAttributeHandleFromTab(
                                _hlaAttributesToPublish.get(sPub)));
                    } catch (AttributeNotDefined e) {
                        throw new IllegalActionException(null, e,
                                "AttributeNotDefined: " + e.getMessage());
                    }
                }

                // At this point, all HlaUpdatables have been initialized and own their
                // corresponding HLA class handle and HLA attribute handle. Just retrieve
                // the first from the list to get those information.
                Object[] tObj = _hlaAttributesToPublish
                        .get(hlaUpdatableFullnames.getFirst());
                int classHandle = _getClassHandleFromTab(tObj);

                // Declare to the Federation the HLA attribute(s) to publish.
                try {
                    rtia.publishObjectClass(classHandle, _attributesLocal);

                    if (_debugging) {
                        _hlaDebug("_setupHlaUpdatables() - Publish Object Class: "
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
            //    with the returned object instance handle.
            Iterator<Entry<String, LinkedList<String>>> it4 = classInstanceNameHlaUpdatableTable
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
                Object[] tObj = _hlaAttributesToPublish
                        .get(hlaUpdatableFullnames.getFirst());

                int classHandle = _getClassHandleFromTab(tObj);
                String classInstanceName = _getHlaInstanceNameFromTab(tObj);

                if (!_registerObjectInstanceMap
                        .containsKey(classInstanceName)) {
                    int instanceHandle = -1;
                    try {
                        instanceHandle = rtia.registerObjectInstance(
                                classHandle, classInstanceName);

                        if (_debugging) {
                            _hlaDebug("_setupHlaUpdatables() - Register Object Instance: "
                                    + " classHandle = " + classHandle
                                    + " classIntanceName = " + classInstanceName
                                    + " instanceHandle = "
                                    + instanceHandle);
                        }

                        _registerObjectInstanceMap.put(classInstanceName,
                                instanceHandle);
                    } catch (ObjectClassNotPublished e) {
                        throw new IllegalActionException(null, e,
                                "ObjectClassNotPublished: " + e.getMessage());
                    } catch (ObjectAlreadyRegistered e) {
                        throw new IllegalActionException(null, e,
                                "ObjectAlreadyRegistered: " + e.getMessage());
                    }
                } // end if (!_registerObjectInstanceMap)
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
        private void _setupHlaReflectables(RTIambassador rtia)
                throws ObjectClassNotDefined, FederateNotExecutionMember,
                RTIinternalError, SaveInProgress, RestoreInProgress,
                ConcurrentAccessAttempted, IllegalActionException {
            // XXX: FIXME: check mixing between tObj[] and HlaSubcriber getter/setter.

            // For each HlaReflectable actors deployed in the model we declare
            // to the HLA/CERTI Federation a HLA attribute to subscribe to.

            // 1. Get classHandle and attributeHandle for each attribute
            // value to subscribe. Update the HlaReflectables.
            Iterator<Entry<String, Object[]>> it1 = _hlaAttributesToSubscribeTo
                    .entrySet().iterator();

            while (it1.hasNext()) {
                Map.Entry<String, Object[]> elt = it1.next();
                // elt.getKey()   => HlaReflectable actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // Get corresponding HlaReflectable actor.
                HlaReflectable sub = (HlaReflectable) ((TypedIOPort) tObj[0])
                        .getContainer();

                if (_debugging) {
                    _hlaDebug("_setupHlaReflectables() - HlaReflectable: "
                            + sub.getFullName());
                }

                // Object class handle and attribute handle are numerical (int)
                // representation, provided by the RTIA, for object classes
                // and object class attributes that appear in the FED file.

                // Retrieve HLA class handle from RTI.
                int classHandle = Integer.MIN_VALUE;

                try {
                    classHandle = rtia.getObjectClassHandle(
                            _getHlaClassNameFromTab(tObj));

                    if (_debugging) {
                        _hlaDebug("_setupHlaReflectables() "
                                + "objectClassName (in FOM) = "
                                + _getHlaClassNameFromTab(tObj)
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
                            sub.getHlaAttributeName(), classHandle);

                    if (_debugging) {
                        _hlaDebug("_setupHlaReflectables() " + " attributeHandle = "
                                + attributeHandle);
                    }
                } catch (NameNotFound e) {
                    throw new IllegalActionException(null, e, "NameNotFound: "
                            + e.getMessage()
                            + " is not a HLA attribute value from the FOM (see .fed file).");
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
                elt.setValue(new Object[] { _getPortFromTab(tObj),
                        _getTypeFromTab(tObj), _getHlaClassNameFromTab(tObj),
                        _getHlaInstanceNameFromTab(tObj), classHandle,
                        attributeHandle });

                sub.setClassHandle(classHandle);
                sub.setAttributeHandle(attributeHandle);
            }

            // 2. Create a table of HlaReflectables indexed by their corresponding
            //    class handle (no duplication).
            HashMap<Integer, LinkedList<String>> classHandleHlaReflectableTable = null;
            classHandleHlaReflectableTable = new HashMap<Integer, LinkedList<String>>();

            Iterator<Entry<String, Object[]>> it22 = _hlaAttributesToSubscribeTo
                    .entrySet().iterator();

            while (it22.hasNext()) {
                Map.Entry<String, Object[]> elt = it22.next();
                // elt.getKey()   => HlaReflectable actor full name.
                // elt.getValue() => tObj[] array.
                Object[] tObj = elt.getValue();

                // The handle of the class to which the HLA attribute belongs to.
                int classHandle = _getClassHandleFromTab(tObj);

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
            Iterator<Entry<Integer, LinkedList<String>>> it3 = classHandleHlaReflectableTable
                    .entrySet().iterator();

            while (it3.hasNext()) {
                Map.Entry<Integer, LinkedList<String>> elt = it3.next();
                // elt.getKey()   => HLA class instance name.
                // elt.getValue() => list of HlaReflectable actor full names.
                LinkedList<String> hlaReflectableFullnames = elt.getValue();

                // The attribute handle set to declare all subscribed attributes
                // for one object class.
                AttributeHandleSet _attributesLocal = _factory.createAttributeHandleSet();

                for (String sSub : hlaReflectableFullnames) {
                    try {
                        _attributesLocal.add(_getAttributeHandleFromTab(
                                _hlaAttributesToSubscribeTo.get(sSub)));
                    } catch (AttributeNotDefined e) {
                        throw new IllegalActionException(null, e,
                                "AttributeNotDefined: " + e.getMessage());
                    }
                }

                // At this point, all HlaReflectable actors have been initialized and own their
                // corresponding HLA class handle and HLA attribute handle. Just retrieve
                // the first from the list to get those information.
                Object[] tObj = _hlaAttributesToSubscribeTo
                        .get(hlaReflectableFullnames.getFirst());
                int classHandle = _getClassHandleFromTab(tObj);
                try {
                    _rtia.subscribeObjectClassAttributes(classHandle,
                            _attributesLocal);
                } catch (AttributeNotDefined e) {
                    throw new IllegalActionException(null, e,
                            "AttributeNotDefined: " + e.getMessage());
                }

                if (_debugging) {
                    _hlaDebug("_setupHlaReflectables() - Subscribe Object Class Attributes: "
                            + " classHandle = " + classHandle
                            + " _attributesLocal = "
                            + _attributesLocal.toString());
                }
            }
        } // end 'private void _setupHlaReflectables(RTIambassador rtia) ...'
    } // end 'private class PtolemyFederateAmbassadorInner extends NullFederateAmbassador { ...'
}
