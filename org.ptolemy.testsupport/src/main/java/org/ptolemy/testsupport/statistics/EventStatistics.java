/* A container for counting and timing information about event occurrences.

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
 * A container for counting and timing information about arbitrary actions/events/....
 * <p>
 * For the moment, this class is only meant for usage in this package. 
 * Actual "public" statistics are provided by
 * specific wrapper classes, related to actual model components (actors, ports,...).
 * </p>
 * <b>Implementation note:</b> the current implementation assumes a dummy event,
 * received when an EventStatistics instance is created. 
 * So, the time interval from instance construction time until the first call to acceptEvent()
 * is considered as the first event interval.
 * 
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 10.1
 * @Pt.ProposedRating Green (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
final class EventStatistics {

  /**
   * Add a new event, and recalculate all interval data.
   * 
   * @param event
   */
  public void acceptEvent(Object event) {
    Date time = new Date();
    _statData.acceptData(time.getTime() - _lastTime.getTime());
    _lastTime = time;
  }

  /**
   * Loose all statistical data and start all-over again.
   * 
   */
  public void reset() {
    _lastTime = new Date();
    _statData.reset();
  }

  /**
   * @return the average time interval (in ms) between two events.
   */
  public long getAvgInterval() {
    return _statData.getAvgData();
  }

  /**
   * @return the timestamp of the last received event.
   */
  public Date getLastTime() {
    return _lastTime;
  }

  /**
   * @return the maximum time interval (in ms) between two events.
   */
  public long getMaxInterval() {
    return _statData.getMaxData();
  }

  /**
   * @return the minimum time interval (in ms) between two events.
   */
  public long getMinInterval() {
    return _statData.getMinData();
  }

  /**
   * @return the number of events received until now.
   */
  public long getNrEvents() {
    return _statData.getCount();
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[EventStatistics:");
    buffer.append(" lastTime: ");
    buffer.append(_lastTime);
    buffer.append(_statData);
    buffer.append("]");
    return buffer.toString();
  }

  // private properties

  private Date _lastTime = new Date();
  private StatisticalLongData _statData = new StatisticalLongData();
}
