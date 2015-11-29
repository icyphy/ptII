/* A container for counting and timing information about (processing) cycles.

Copyright (c) 2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package org.ptolemy.testsupport.statistics;

import java.util.Date;

/**
 * A container for counting and timing information about (processing) cycles.
 * <br>
 * <b>Limited to sequential cycles, no support for concurrent/nested cycles!</b>
 * <p>
 * For the moment, this class is only meant for usage in this package.
 * Actual "public" statistics are provided by specific wrapper classes,
 * related to actual model components (actors, ports,...).
 * <p>
 * <b>Implementation note:</b> the current implementation assumes the thing is in the 
 * <i>idle</i> state when a CycleStatistics instance is created. 
 * So, the time interval from instance construction time until the first call to acceptCycleBegin()
 * is considered as the first idle period.
 * 
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Green (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
final class CycleStatistics {

  /**
   * Loose all statistical data.
   *
   */
  public void reset() {
    _lastCycleStartTime = null;
    _lastCycleEndTime = new Date();
    _idle = true;
    _idleData.reset();
    _cycleData.reset();
  }

  /**
   * Log the count and timing of a cycle start.
   * 
   * @throws IllegalStateException if the element is not idle, i.e. there's already a cycle ongoing
   */
  public void acceptCycleBegin() throws IllegalStateException {
    if (!_idle) {
      throw new IllegalStateException("Not idle");
    }
    _idle = false;
    _lastCycleStartTime = new Date();
    _idleData.acceptData(_lastCycleStartTime.getTime() - _lastCycleEndTime.getTime());
  }

  /**
   * Log the count and timing of a cycle end.
   * 
   * @throws IllegalStateException if the element is idle, i.e. there's no cycle ongoing that could be ended
   */
  public void acceptCycleEnd() throws IllegalStateException {
    if (_idle) {
      throw new IllegalStateException("Idle");
    }
    _idle = true;
    _lastCycleEndTime = new Date();
    _cycleData.acceptData(_lastCycleEndTime.getTime() - _lastCycleStartTime.getTime());
  }

  /**
   * @return the average idle time between cycles
   */
  public long getAvgIdleTime() {
    return _idleData.getAvgData();
  }

  /**
   * @return the minimum idle time between cycles
   */
  public long getMinIdleTime() {
    return _idleData.getMinData();
  }

  /**
   * @return the maximum idle time between cycles
   */
  public long getMaxIdleTime() {
    return _idleData.getMaxData();
  }

  /**
   * @return the average duration of a processing cycle
   */
  public long getAvgProcessingTime() {
    return _cycleData.getAvgData();
  }

  /**
   * @return the minimum duration of a processing cycle
   */
  public long getMinProcessingTime() {
    return _cycleData.getMinData();
  }

  /**
   * @return the maximum duration of a processing cycle
   */
  public long getMaxProcessingTime() {
    return _cycleData.getMaxData();
  }

  /**
   * @return the number of processing cycle until now
   */
  public long getNrCycles() {
    return _cycleData.getCount();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("CycleStatistics [_idle=");
    builder.append(_idle);
    builder.append(", _lastCycleStartTime=");
    builder.append(_lastCycleStartTime);
    builder.append(", _lastCycleEndTime=");
    builder.append(_lastCycleEndTime);
    builder.append(", _idleData=");
    builder.append(_idleData);
    builder.append(", _cycleData=");
    builder.append(_cycleData);
    builder.append("]");
    return builder.toString();
  }
  
  // private properties

  // boolean flag to maintain whether we're
  // in a cycle (idle==false) or in-between cycles (idle==true)
  private boolean _idle = true;

  private Date _lastCycleStartTime;
  private Date _lastCycleEndTime = new Date();

  private StatisticalLongData _idleData = new StatisticalLongData();
  private StatisticalLongData _cycleData = new StatisticalLongData();
}
