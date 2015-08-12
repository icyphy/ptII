/* A clock that keeps track of model time at a level of the model hierarchy.

 Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.actor;

import java.util.Collection;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.ExtendedMath;

/** A clock that keeps track of model time at a level of the model hierarchy
 *  and relates it to the time of the enclosing model, if there is one. The time
 *  of the enclosing model is referred to as the environment time. This
 *  clock has a notion of local time and committed time. The committed time
 *  is "simultaneous" with the environment time.
 *
 *  <p>The local time is
 *  not allowed to move backwards past the committed time, but ahead
 *  of that time, it can move around at will. </p>
 *  <p>
 *  There is no way of explicitly committing time, but
 *  several methods have the side effect of committing the current
 *  local time. For example, {@link #setClockDrift(double)} will commit
 *  the current local time and change the clock drift.  So will
 *  {@link #start()} and {@link #stop()} </p>
 *
 *  <p>
 *  This class implements the AbstractSettableAttribute interface because
 *  we want the localClock to be shown as a parameter in the editor
 *  dialogue of a director. A better implementation would be to derive
 *  LocalClock from Attribute and make changes to vergil such that
 *  Attributes are displayed in the dialogue, however, for the moment,
 *  the required changes are too complex.
 *  The value of the clock is exposed as an attribute that, by default,
 *  is non editable. The clock drift is a contained attribute that can
 *  be modified. </p>
 *
 *  <p> This class also specifies a <i>globalTimeResolution</i>
 *  parameter. This is a double with default 1E-10, which is
 *  10<sup>-10</sup>.  All time values are rounded to the nearest
 *  multiple of this value. If the value is changed during a run, an
 *  exception is thrown.  This is a shared parameter, which means that
 *  all instances of Director in the model will have the same value
 *  for this parameter. Changing one of them changes all of them. </p>
 *
 *  <p>FIXME: Setting of clock drift must be controlled because it commits
 *  time. </p>
 *
 * @author Ilge Akkaya, Patricia Derler, Edward A. Lee, Christos Stergiou, Michael Zimmer
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating yellow (eal)
 * @Pt.AcceptedRating red (eal)
 */
public class LocalClock extends AbstractSettableAttribute {

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public LocalClock(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        globalTimeResolution = new SharedParameter(this,
                "globalTimeResolution", null, "1E-10");

        clockDrift = new Parameter(this, "clockRate");
        clockDrift.setExpression("1.0");
        clockDrift.setTypeEquals(BaseType.DOUBLE);

        // Make sure getCurrentTime() never returns null.
        _localTime = Time.NEGATIVE_INFINITY;
        _drift = 1.0;
        _visibility = Settable.NOT_EDITABLE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The time precision used by this director. All time values are
     *  rounded to the nearest multiple of this number. This is a double
     *  that defaults to "1E-10" which is 10<sup>-10</sup>.
     *  This is a shared parameter, meaning that changing one instance
     *  in a model results in all instances being changed.
     */
    public SharedParameter globalTimeResolution;

    /** The drift of the local clock with respect to the environment
     *  clock. If this is a top level director the clock drift has no
     *  consequence. The value is a double that is initialized to
     *  1.0 which means that the local clock drift matches the one
     *  of the environment.
     */
    public Parameter clockDrift;

    ///////////////////////////////////////////////////////////////////
    ////                         public method                     ////

    /** This method has to be implemented for the AbstractSettableAttribute
     *  interface. This interface is only needed for the LocalClock to
     *  show up in the configuration dialogue of the container (the director).
     *  The method will not be used for this class so the implementation
     *  is empty.
     *  @param listener The listener to be added.
     *  @see #removeValueListener(ValueListener)
     */
    @Override
    public void addValueListener(ValueListener listener) {
        // nothing to do.
    }

    /** Delegate the call to the director, which handles changes
     *  to the parameters of the clock.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the director throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == clockDrift) {
            double drift;
            drift = ((DoubleToken) clockDrift.getToken()).doubleValue();
            if (drift != getClockDrift()) {
                setClockDrift(drift);
            }
        } else if (attribute == globalTimeResolution) {
            // This is extremely frequently used, so cache the value.
            // Prevent this from changing during a run!
            double newResolution = ((DoubleToken) globalTimeResolution
                    .getToken()).doubleValue();

            // FindBugs reports this comparison as a problem, but it
            // is not an issue because we usually don't calculate
            // _timeResolution, we set it.
            if (newResolution != getTimeResolution()) {
                NamedObj container = getContainer().getContainer();

                if (container instanceof Actor) {
                    Manager manager = ((Actor) container).getManager();

                    if (manager != null) {
                        Manager.State state = manager.getState();

                        if (state != Manager.IDLE
                                && state != Manager.PREINITIALIZING) {
                            throw new IllegalActionException(this,
                                    "Cannot change timePrecision during a run.");
                        }
                    }
                }

                if (newResolution <= ExtendedMath.DOUBLE_PRECISION_SMALLEST_NORMALIZED_POSITIVE_DOUBLE) {
                    throw new IllegalActionException(
                            this,
                            "Invalid timeResolution: "
                                    + newResolution
                                    + "\n The value must be "
                                    + "greater than the smallest, normalized, "
                                    + "positive, double value with a double "
                                    + "precision: "
                                    + ExtendedMath.DOUBLE_PRECISION_SMALLEST_NORMALIZED_POSITIVE_DOUBLE);
                }

                setTimeResolution(newResolution);
            }
        }
        super.attributeChanged(attribute);
    }

    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the cloned object.
     *  @return The cloned object.
     *  @exception CloneNotSupportedException If thrown by super class.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        LocalClock newObject = (LocalClock) super.clone(workspace);
        newObject._localTime = Time.NEGATIVE_INFINITY;
        newObject._offset = ((Director) getContainer())._zeroTime;
        newObject._drift = 1.0;
        return newObject;
    }

    /** Get clock drift.
     *  @return The clock drift.
     *  @see #setClockDrift(double)
     */
    public double getClockDrift() {
        // FIXME: This returns a double, what does 1.0 mean?  0.0?
        return _drift;
    }

    /** Get the environment time that corresponds to the given local time.
     *  The given local time is required to be either equal to or
     *  greater than the committed time when this method is called.
     *  @param time The local Time.
     *  @return The corresponding environment Time.
     *  @exception IllegalActionException If the specified local time
     *   is in the past, or if Time objects cannot be created.
     */
    public Time getEnvironmentTimeForLocalTime(Time time)
            throws IllegalActionException {
        if (time.compareTo(_lastCommitLocalTime) < 0) {
            throw new IllegalActionException(
                    "Cannot compute environment time for local time " + time
                    + " because "
                    + "the last commit of the local time occurred at "
                    + "local time " + _lastCommitLocalTime);
        }
        Time localTimePassedSinceCommit = time.subtract(_lastCommitLocalTime);
        Time environmentTimePassedSinceCommit = localTimePassedSinceCommit;
        if (_drift != 1.0) {
            double environmentTimePassedSinceCommitDoubleValue = environmentTimePassedSinceCommit
                    .getDoubleValue();
            environmentTimePassedSinceCommitDoubleValue = environmentTimePassedSinceCommitDoubleValue
                    / _drift;
            environmentTimePassedSinceCommit = new Time(
                    (Director) getContainer(),
                    environmentTimePassedSinceCommitDoubleValue);
        }
        Time environmentTime = _lastCommitEnvironmentTime
                .add(environmentTimePassedSinceCommit);
        return environmentTime;
    }

    /** Return the local time.
     *  @return The local time as a string value.
     */
    @Override
    public String getExpression() {
        if (_localTime == null) {
            return "";
        } else {
            return String.valueOf(_localTime);
        }
    }

    /** Get current local time. If it has never been set, then this will return
     *  Time.NEGATIVE_INFINITY. The returned value may have been set by
     *  {@link #setLocalTime(Time)}.
     *  @return The current local time.
     *  @see #setLocalTime(Time)
     */
    public Time getLocalTime() {
        return _localTime;
    }

    /** Get the local time that corresponds to the current environment time.
     *  The current environment time is required to be greater than or equal
     *  to the environment time corresponding to the last committed local time.
     *  @return The corresponding local time.
     *  @exception IllegalActionException If Time objects cannot be created, or
     *   if the current environment time is less than the time
     *   corresponding to the last committed local time.
     */
    public Time getLocalTimeForCurrentEnvironmentTime()
            throws IllegalActionException {
        return getLocalTimeForEnvironmentTime(((Director) getContainer())
                .getEnvironmentTime());
    }

    /** Get the local time that corresponds to the given environment time.
     *  The given environment time is required to be greater than or equal
     *  to the environment time corresponding to the last committed local time.
     *  @param time The environment time.
     *  @return The corresponding local time.
     *  @exception IllegalActionException If the specified environment time
     *   is less than the environment time corresponding to the last
     *   committed local time, or if Time objects cannot be created.
     */
    public Time getLocalTimeForEnvironmentTime(Time time)
            throws IllegalActionException {
        if (_lastCommitEnvironmentTime == null
                || time.compareTo(_lastCommitEnvironmentTime) < 0) {
            throw new IllegalActionException(
                    "Cannot compute local time for environment time " + time
                    + " because "
                    + "the last commit of the local time occurred at "
                    + "local time " + _lastCommitLocalTime + " which "
                    + "corresponds to environment time "
                    + _lastCommitEnvironmentTime);
        }

        Time environmentTimePassedSinceCommit = time
                .subtract(_lastCommitEnvironmentTime);
        Time localTimePassedSinceCommit = environmentTimePassedSinceCommit;
        if (_drift != 1.0) {
            double localTimePassedSinceCommitDoubleValue = environmentTimePassedSinceCommit
                    .getDoubleValue();
            localTimePassedSinceCommitDoubleValue = localTimePassedSinceCommitDoubleValue
                    * _drift;
            localTimePassedSinceCommit = new Time((Director) getContainer(),
                    localTimePassedSinceCommitDoubleValue);
        }
        Time localTime = _lastCommitEnvironmentTime.subtract(_offset).add(
                localTimePassedSinceCommit);
        return localTime;
    }

    /** Get the time resolution of the model. The time resolution is
     *  the value of the <i>timeResolution</i> parameter. This is the
     *  smallest time unit for the model.
     *  @return The time resolution of the model.
     *  @see #setTimeResolution(double)
     */
    public final double getTimeResolution() {
        // This method is final for performance reason.
        return _timeResolution;
    }

    /** The LocalClock is not editable, thus visibility is
     *  always set to NOT_EDITABLE.
     *  @return NOT_EDITABLE.
     *  @see #setVisibility(Visibility)
     */
    @Override
    public Visibility getVisibility() {
        return _visibility;
    }

    /** Initialize parameters that cannot be initialized in the
     *  constructor. For instance, Time objects cannot be created
     *  in the constructor because the time resolution might not be
     *  known yet. Older models have the timeResolution parameter
     *  specified in the director which will only be loaded by the
     *  MOMLParser after the director is initialized.
     */
    public void initialize() {
        _offset = ((Director) getContainer())._zeroTime;
    }

    /** This method has to be implemented for the AbstractSettableAttribute
     *  interface. This interface is only needed for the LocalClock to
     *  show up in the configuration dialogue of the container (the director).
     *  The method will not be used for this class so the implementation
     *  is empty.
     *  @param listener The listener to be removed.
     *  @see #addValueListener(ValueListener)
     */
    @Override
    public void removeValueListener(ValueListener listener) {
        // nothing to do.
    }

    /** Set local time and commit.
     *  This is allowed to set time earlier than the
     *  last committed local time.
     *  @param time The new local time.
     */
    public void resetLocalTime(Time time) {
        if (_debugging) {
            _debug("reset local time to " + time);
        }
        _localTime = time;
        _commit();
    }

    /** Set the new clock drift and commit it.
     *  @param drift New clock drift.
     *  @exception IllegalActionException If the specified drift is
     *   non-positive.
     *  @see #getClockDrift()
     */
    public void setClockDrift(double drift) throws IllegalActionException {
        // FIXME: This returns a double, what does 1.0 mean?  0.0?
        if (drift <= 0.0) {
            throw new IllegalActionException(getContainer(),
                    "Illegal clock drift: " + drift
                    + ". Clock drift is required to be positive.");
        }
        _drift = drift;
        _commit();
    }

    /** Set local time without committing.
     *  This is not allowed to set
     *  time earlier than the last committed local time.
     *  @param time The new local time.
     *  @exception IllegalActionException If the specified time is
     *   earlier than the current time.
     *  @see #getLocalTime()
     */
    public void setLocalTime(Time time) throws IllegalActionException {
        if (_lastCommitLocalTime != null
                && time.compareTo(_lastCommitLocalTime) < 0) {
            throw new IllegalActionException(
                    getContainer(),
                    "Cannot set local time to "
                            + time
                            + ", which is earlier than the last committed current time "
                            + _lastCommitLocalTime);
        }
        _localTime = time;
    }

    /** Set time resolution.
     *  @param timeResolution The new time resolution.
     *  @see #getTimeResolution()
     */
    public void setTimeResolution(double timeResolution) {
        _timeResolution = timeResolution;
    }

    /** This method has to be implemented for the AbstractSettableAttribute
     *  interface. This interface is only needed for the LocalClock to
     *  show up in the configuration dialogue of the container (the director).
     *  This method does not do anything because visibility is always
     *  NOT_EDITABLE.
     *  @param visibility The new visibility.
     *  @see #getVisibility()
     */
    @Override
    public void setVisibility(Visibility visibility) {
        _visibility = visibility;
    }

    /** Start the clock with the current drift as specified by the
     *  last call to {@link #setClockDrift(double)}.
     *  If {@link #setClockDrift(double)} has never been called, then
     *  the drift is 1.0.
     *  This method commits current local time.
     */
    public void start() {
        _commit();
    }

    /** Stop the clock. The current time will remain the
     *  same as its current value until the next call to
     *  {@link #start()}.
     *  This method commits current local time.
     */
    public void stop() {
        _commit();
    }

    /** This method has to be implemented for the AbstractSettableAttribute
     *  interface. This interface is only needed for the LocalClock to
     *  show up in the configuration dialogue of the container (the director).
     *  The value of the LocalClock does not need validation, thus this method
     *  does not do anything.
     *  @return Null.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Commit the current local time.
     */
    private void _commit() {
        if (_offset == null || _localTime == null) { // not initialized.
            return;
        }
        // skip if local time has never been set.
        if (_localTime != Time.NEGATIVE_INFINITY) {
            Time environmentTime = ((Director) getContainer())
                    .getEnvironmentTime();
            if (environmentTime == null) {
                _offset = ((Director) getContainer())._zeroTime;
            } else {
                _offset = environmentTime.subtract(_localTime);
            }
            _lastCommitEnvironmentTime = environmentTime;
            _lastCommitLocalTime = _localTime;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////

    /** The current time of this clock. */
    private Time _localTime;

    /** The current clock drift.
     *  The drift is initialized to 1.0 which means that the
     *  local time matches to the environment time.
     */
    private double _drift;

    /** The environment time at which a change to local time, drift,
     *  or resumption occurred.
     */
    private Time _lastCommitEnvironmentTime;

    /** The local time at which a change to local time, drift,
     *  or resumption occurred.
     */
    private Time _lastCommitLocalTime;

    /** The environment time minus the local time at the the point
     *  at which a commit occurred.
     *  By default, the offset is zero.
     */
    private Time _offset;

    private Visibility _visibility;

    /** Time resolution cache, with a reasonable default value. */
    private double _timeResolution = 1E-10;

}
