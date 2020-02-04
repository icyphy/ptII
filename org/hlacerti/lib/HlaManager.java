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


import hla.rti.FederateAmbassador;
import hla.rti.RTIambassador;
import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.TimeRegulator;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
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

    /**
     * Construct a HlaManager with a name and a container. The container
     * argument must not be null, or a NullPointerException will be thrown.
     * This actor will use the workspace of the container for synchronization
     * and version counts. If the name argument is null, then the name is set
     * to the empty string. Increment the version of the workspace.
     *
     * @param container Container of this attribute.
     * @param name Name of this attribute.
     * @exception IllegalActionException If the container is incompatible
     * with this actor.
     * @exception NameDuplicationException If the name coincides with
     * an actor already in the container.
     */

    private HlaManagerDelegate _hlaManagerDelegate;

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
     *  see fedFileOnRTIG
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
     *  see fedFileOnRTIG
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

    /**
     * Federate counter, to add to the federate names to have unique names
     */
    public static int nbFederates = 0;

    public HlaManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

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
        fedFile.setExpression("HLAFederation.fed"); //TODO Change to .xml ???

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
    ////                       private variables                  ////

    /** Name of the current Ptolemy federate ({@link HlaManager1_3}). */
    private String _federateName;

    /**-Name of the HLA/CERTI federation to create or to join. */
    private String _federationName;

    /** Indicates the use of the nextEventRequest() service. */
    private boolean _eventBased;

    /** Indicates the use of the timeAdvanceRequest() service. */
    private boolean _timeStepped;

    /** The lookahead value of the federate in HLA logical time units. */
    private double _hlaLookAHead;

    /** Time step in units of HLA logical time. */
    private double _hlaTimeStep;

    /** Indicates the use of the enableTimeConstrained() service. */
    private boolean _isTimeConstrained;

    /** Indicates the use of the enableTimeRegulation() service. */
    private boolean _isTimeRegulator;

    /** The actual value for hlaTimeUnit parameter. */
    private double _hlaTimeUnitValue;

    /** Indicates if the HLA reporter is enabled or not. */
    private boolean _enableHlaReporter;

    ///////////////////////////////////////////////////////////////////
    ////                    Getters / Setters                      ////

    /** Name of the current Ptolemy federate ({@link HlaManager1_3}). */
    public String get_FederateName(){
        return  _federateName;
    }

    /**-Name of the HLA/CERTI federation to create or to join. */
    public String get_FederationName(){
        return _federationName;
    }

    /** Indicates the use of the nextEventRequest() service. */
    public boolean get_EventBased(){
        return _eventBased;
    }

    /** Indicates the use of the timeAdvanceRequest() service. */
    public boolean get_TimeStepped(){
        return _timeStepped;
    }

    /** The lookahead value of the federate in HLA logical time units. */
    public double get_HlaLookAHead()
    {
        return _hlaLookAHead;
    }
    /** Time step in units of HLA logical time. */
    public double get_HlaTimeStep(){
        return _hlaTimeStep;
    }

    /** Indicates the use of the enableTimeConstrained() service. */
    public boolean get_IsTimeConstrained(){
        return _isTimeConstrained;
    }

    /** Indicates the use of the enableTimeRegulation() service. */
    public boolean get_IsTimeRegulator(){
        return _isTimeRegulator;
    }

    /** The actual value for hlaTimeUnit parameter. */
    public double get_HlaTimeUnitValue(){
        return _hlaTimeUnitValue;
    }

    /** Indicates if the HLA reporter is enabled or not. */
    public boolean get_EnableHlaReporter(){
        return _enableHlaReporter;
    }

    public void set_federateName(String _federateName) {
        this._federateName = _federateName;
    }

    public void set_federationName(String _federationName) {
        this._federationName = _federationName;
    }

    public void set_eventBased(boolean _eventBased) {
        this._eventBased = _eventBased;
    }

    public void set_timeStepped(boolean _timeStepped) {
        this._timeStepped = _timeStepped;
    }

    public void set_hlaLookAHead(double _hlaLookAHead) {
        this._hlaLookAHead = _hlaLookAHead;
    }

    public void set_hlaTimeStep(double _hlaTimeStep) {
        this._hlaTimeStep = _hlaTimeStep;
    }

    public void set_isTimeConstrained(boolean _isTimeConstrained) {
        this._isTimeConstrained = _isTimeConstrained;
    }

    public void set_isTimeRegulator(boolean _isTimeRegulator) {
        this._isTimeRegulator = _isTimeRegulator;
    }

    public void set_hlaTimeUnitValue(double _hlaTimeUnitValue) {
        this._hlaTimeUnitValue = _hlaTimeUnitValue;
    }

    public void set_enableHlaReporter(boolean _enableHlaReporter) {
        this._enableHlaReporter = _enableHlaReporter;
    }

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

        if (attribute == fedFile) {
            if (fedFile.getExpression().endsWith(".fed")) {
                _hlaManagerDelegate = new HlaManager1_3(this);
                System.out.println("HLA version is 1.3.");
            } else if (fedFile.getExpression().endsWith(".xml")) {
                _hlaManagerDelegate = new HlaManager1516e(this);
                System.out.println("HLA version is 1516e.");
            } else {
                throw new IllegalActionException(this,
                        "fedFile needs to be either a .fed file (for version 1.3) or "
                        + "a .xml file (for version 1516e).");
            }
        } else if (attribute == federateName) {
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
        // Force creation of a new delegate based on whether the fed file is .fed or .xml.
        try {
            attributeChanged(fedFile);
        } catch (IllegalActionException e) {
            System.err.println("ERROR: Cloning HlaManager failed.");
            e.printStackTrace();
            throw new CloneNotSupportedException("ERROR: Cloning HlaManager failed.");
        }
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
        _hlaManagerDelegate.initialize();
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
        _hlaManagerDelegate.preinitialize();
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
        return _hlaManagerDelegate.proposeTime(proposedTime);
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
        _hlaManagerDelegate.updateHlaAttribute(updater, in);
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
        _hlaManagerDelegate.wrapup();
    }

    public boolean getDebugging(){
        return _debugging;
    }

    public void setDebug(String s){
        _debug(s);
    }


}