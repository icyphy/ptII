/**
 * 
 */
package ptolemy.domains.qss.kernel;

import org.ptolemy.qss.solver.QSSBase;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
<p>This director extends the discrete-event (DE) model of computation (MoC).
It has an additional parameter, the errorTolerance, which is used by 
QSSIntegrators. 
It should be used as the local director of a CompositeActor that is
to be executed according to the DE MoC. This director maintains a totally
ordered set of events and processes these events in the order defined on
their tags and depths.
</p><p>
An event is associated with a tag, which is a tuple of timestamp and
microstep. A timestamp indicates the model time when this event occurs. It
is an object of the {@link ptolemy.actor.util.Time} class. A microstep is an
integer which represents the index of the sequence of execution phases when
this director processes events with the same timestamp. Two tags are equal
if they have the same timestamp and microstep. If two events have the same
tag, they are called simultaneous events.
</p><p>
Microsteps can only be increased by calling the fireAt() method. For example,
when an actor requests to be fired again at the current model time, a
new event with the same timestamp but a bigger microstep (incremented by 1)
will be generated.
</p><p>
An event is also associated with a depth reflecting its priority, based
on which a DE director chooses the execution order for simultaneous events.
A depth is an integer and a larger value of depth indicates a lower priority.
The depth of an event is determined by topologically sorting all the ports
of actors according to their data dependencies over which there is no time
delay.
</p><p>
The order of events is defined as follows. An event A is said to be earlier
than another event B if A's timestamp is smaller than B's; or if A's
timestamp is the same as B's, and A's microstep is smaller than B's; or if
A's tag is the same as B's, and A's depth is smaller than B's. By giving
events this well-defined order, this director can handle simultaneous events
in a deterministic way.
</p><p>
The bottleneck in a typical DE simulator is in the maintenance of the
global event queue. This director uses the calendar queue as the global
event queue. This is an efficient algorithm with O(1) time complexity in
both enqueue and dequeue operations. Sorting in the
{@link ptolemy.actor.util.CalendarQueue} class is done according to the
order defined above.
</p><p>
The complexity of the calendar algorithm is sensitive to the length of the
event queue. When the size of the event queue becomes too long or changes
very often, the simulation performance suffers from the penalties of queuing
and dequeuing events. A few mechanisms are implemented to reduce such
penalties by keeping the event queue short. The first mechanism is to only
store in the event queue <i>pure</i> events and the <i>trigger</i> events
with the same timestamp and microstep as those of the director. See
{@link DEEvent} for explanation of these two types of events. What is more,
no duplicate trigger events are allowed in the event queue. Another mechanism
is that in a hierarchical model, each level keeps a local event queue.
A lower level only reports the earliest event to its upper level
to schedule a future firing. The last mechanism is to maintain a list which
records all actors that are disabled. Any triggers sent to the actors in
this list are discarded.
</p><p>
In the initialize() method, depths of actors and IO ports are statically
analyzed and calculated. They are not calculated in the preinitialize()
method because hierarchical models may change their structures during their
preinitialize() method. For example, a modal model does not specify its
initial state (and its refinement) until the end of its preinitialize()
method. See {@link ptolemy.domains.modal.kernel.FSMActor}. In order to support
mutation, this director recalculates the depths at the beginning of its next
iteration.
</p><p>
There are two types of depths: one is associated with IO ports, which
reflects the order of trigger events; the other one is associated with
actors, which is for pure events. The relationship between the depths of IO
ports and actors is that the depth of an actor is the smallest of the depths
of its IO ports. Pure events can only be produced by calling the fireAt()
method, and trigger events can only be produced by actors that produce
outputs. See {@link ptolemy.domains.de.kernel.DEReceiver#put(Token)}.
</p><p>
Directed loops of IO ports with no delay will trigger an exception.
These are called <i>causality loops</i>. Such loops can be broken with
actors whose output ports do not have an immediate dependence on their
input ports, such as the <i>TimeDelay</i> actor.  Notice that the
<i>TimeDelay</i> actor breaks a causality loop even if the time
delay is set to 0.0. This is because DE uses a <i>superdense</i>
notion of time.  The output is interpreted as being strictly later
than the input even though its time value is the same.
Whether a causality loop exists is determined by the
{@link ptolemy.actor.util.CausalityInterface} returned by each actor's
getCausalityInterface() method.
</p><p>
An input port in a DE model contains an instance of DEReceiver.
When a token is put into a DEReceiver, that receiver posts a trigger
event to the director. This director sorts trigger events in a global event
queue.
</p><p>
An iteration, in the DE domain, is defined as processing all the events
whose tags are equal to the current tag of the director (also called the
model tag). At the beginning of the fire() method, this director dequeues
a subset of the earliest events (the ones with smallest timestamp, microstep,
and depth) that have the same destination actor
from the global event queue. Then, this director fires that actor.
This actor must consume tokens from its input port(s),
and usually produces new events on its output port(s). These new events will
trigger the destination actors to fire. It is important that the actor
actually consumes tokens from its inputs, even if the tokens are solely
used to trigger reactions, because the actor will be fired repeatedly
until there are no more tokens in its input ports with the same tag,
or until the actor returns false in its prefire() method. The
director then keeps dequeuing and processing the earliest events from the
event queue until no more events have the same tag. 
</p><p>
Note that each time this director fires an actor, it
also invokes postfire() on that actor.
Note that under this policy, it is possible for an actor to be fired and postfired
multiple times in an iteration.
This does not really correctly implement superdense time semantics, but it is
an approximation that will reject some models that should be able to be executed.
An actor like the TimeDelay will be fired (and postfired) multiple times
at a superdense time index if it is in a feedback loop.
</p><p>
A model starts from the time specified by <i>startTime</i>. This is blank
by default, which indicates that the start time is the current time of
the enclosing director, if there is one, and 0.0 otherwise.
The stop time of the execution can be set
using the <i>stopTime</i> parameter. The parameter has a default value
<i>Infinity</i>, which means the execution runs forever.
</p><p>
Execution of a DE model ends when the timestamp of the earliest event
exceeds the stop time. This stopping condition is checked inside
the postfire() method of this director. By default, execution also ends
when the global event queue becomes empty. Sometimes, the desired
behaviour is for the director to wait on an empty queue until another
thread makes new events available. For example, a DE actor may produce
events when a user hits a button on the screen. To prevent ending the
execution when there are no more events, set the
<i>stopWhenQueueIsEmpty</i> parameter to <code>false</code>.
</p><p>
Parameters <i>isCQAdaptive</i>, <i>minBinCount</i>, and
<i>binCountFactor</i>, are used to configure the calendar queue.
Changes to these parameters are ignored when the model is running.
</p><p>
If the parameter <i>synchronizeToRealTime</i> is set to <code>true</code>,
then the director will not process events until the real time elapsed
since the model started matches the timestamp of the event.
This ensures that the director does not get ahead of real time. However,
of course, this does not ensure that the director keeps up with real time.
</p><p>
This director tolerates changes to the model during execution.
The change should be queued with a component in the hierarchy using
requestChange().  While invoking those changes, the method
invalidateSchedule() is expected to be called, notifying the director
that the topology it used to calculate the priorities of the actors
is no longer valid.  This will result in the priorities (depths of actors)
being recalculated the next time prefire() is invoked.
</p><p>
<b>Limitations</b>: According to [1], at each microstep, DE should
perform a fixed point iteration. This implementation does not do that,
and consequently, this director is not able to execute all correctly
constructed DE models. For an example, see
$PTII/ptolemy/domains/de/test/auto/DEFixedPointLimitation.xml.
That example has a DE opaque composite actor in a feedback loop.
In principle, there should be no causality loop. The actor output
should be able to be produced without knowing the input. However,
the inside director has to guarantee that when it fires any of
its contained actors, all inputs of a given microstep are available
to that actor. As a consequence, the opaque actor also needs to know
all of its inputs at the current microstep. Hence, a causality loop
is reported. We encourage the reader to make a variant of this director
that can handle such models.
 </p><p>
<b>References</b>:
<br>
[1] Lee, E. A. and H. Zheng (2007). Leveraging Synchronous Language
Principles for Heterogeneous Modeling and Design of Embedded Systems.
EMSOFT, Salzburg, Austria, October, ACM.

@author Thierry S. Nouidui based on DEDirector.java
@version $Id: QSSDirector.java 69159 2014-05-08 23:32:34Z eal $
@since Ptolemy 10
@Pt.ProposedRating 
@Pt.AcceptedRating 
*/
public class QSSDirector extends DEDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public QSSDirector() throws IllegalActionException,
            NameDuplicationException {
        _initSolverParameters();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public QSSDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _initSolverParameters();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public QSSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initSolverParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Error tolerance for QSS integration methods
     *  The default value is 1e-4, and the type is double.
     */
    public Parameter errorTolerance;

    /** The class name of the QSS solver used for integration.
     *  This is a string that defaults to "QSS1".
     *  Solvers are all required to be in package
     *  "org.ptolemy.qss".
     */
    public StringParameter QSSSolver;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the integrator. Check for the existence of a
     *  director and an ODE solver. Set the state to the value given
     *  by <i>initialState</i>.
     *  @exception IllegalActionException If there is no director,
     *   or the director has no ODE solver, or the initialState
     *   parameter does not contain a valid token, or the superclass
     *   throws it.
     */
    public void preinitialize() throws IllegalActionException {
        // Called preinitialize() of super class.
        super.preinitialize();
    }

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the new parameter value
     *  is not valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (_debugging) {
            _debug("attributeChanged: Updating QSSDirector parameter: "
                    + attribute.getName());
        }
        if (attribute == errorTolerance) {
            double value = ((DoubleToken) errorTolerance.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative error tolerance.");
            }
            _errorTolerance = value;
        }
        super.attributeChanged(attribute);
    }

    /** Return the local truncation error tolerance.
     *  @return The local truncation error tolerance.
     */
    public final double getErrorTolerance() {
        // This method is final for performance reason.
        return _errorTolerance;
    }

    
    /** Return a QSS solver for use.
     *  @return A QSS solver.
     * @throws IllegalActionException 
     */
    public QSSBase get_QSSSolver() throws IllegalActionException {
        // Instantiate an QSS solver, using the class name given
        // by QSSSolver parameter, which is a string parameter.
        final String solverClassName = QSSSolver.stringValue().trim();
        // Instantiate the solver.
        return(_instantiateQSSSolver(_solverClasspath + solverClassName));
    }

    /////////////////////////////////////////////////////////////////////
    ////                  protected methods                        ////

    /** Instantiate an QSSSolver from its classname. Given the solver's full
     *  class name, this method will try to instantiate it by looking
     *  for the corresponding java class.
     *  This method is based on _instantiateOEDSolver of the CT domain.
     *  @param className The solver's full class name.
     *  @return a new QSS solver.
     *  @exception IllegalActionException If the solver can not be created.
     */
    protected final QSSBase _instantiateQSSSolver(String className)
            throws IllegalActionException {

        // All solvers must be in the package given by _solverClasspath.
        if (!className.trim().startsWith(_solverClasspath)) {
            className = _solverClasspath + className;
        }

        if (_debugging) {
            _debug("instantiating solver..." + className);
        }

        QSSBase newSolver;

        try {
            Class solver = Class.forName(className);
            newSolver = (QSSBase) solver.newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, "QSSSolver: " + className
                    + " is not found.");
        } catch (InstantiationException e) {
            throw new IllegalActionException(this, "QSSSolver: " + className
                    + " instantiation failed." + e);
        } catch (IllegalAccessException e) {
            throw new IllegalActionException(this, "QSSSolver: " + className
                    + " is not accessible.");
        }
        return newSolver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////
    /** initialize parameters. Set all parameters to their default values.
     */
    private void _initSolverParameters() {
        _verbose = true;
        try {
            errorTolerance = new Parameter(this, "errorTolerance");
            errorTolerance.setExpression("1e-4");
            errorTolerance.setTypeEquals(BaseType.DOUBLE);
            QSSSolver = new StringParameter(this, "QSSSolver");
            QSSSolver.setExpression("QSS1");
            QSSSolver.addChoice("QSS1");
            QSSSolver.addChoice("QSS2Fd");
            QSSSolver.addChoice("QSS2Pts");
            QSSSolver.addChoice("QSS2Qts");
            QSSSolver.addChoice("QSS3Fd");
            QSSSolver.addChoice("QSS3Pts");
            QSSSolver.addChoice("LIQSS1");
            QSSSolver.addChoice("LIQSS2Fd");
        } catch (KernelException e) {
            throw new InternalErrorException("Cannot set parameter:\n"
                    + e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                 ////
    /** The error tolerance for state resolution. */
    private double _errorTolerance;

    /** The package name for the solvers supported by this director. */
    private static String _solverClasspath = "org.ptolemy.qss.solver.";
}
