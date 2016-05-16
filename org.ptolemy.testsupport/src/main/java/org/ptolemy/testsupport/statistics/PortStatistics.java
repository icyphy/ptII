/* Statistics implementation for maintaining counts and timings of received and sent messages.

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

import ptolemy.actor.IOPort;
import ptolemy.actor.IOPortEvent;
import ptolemy.actor.IOPortEventListener;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Statistics implementation for maintaining counts of received and sent messages.
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
public class PortStatistics implements NamedStatistics, IOPortEventListener {

  public PortStatistics(IOPort port) {
    this._port = port;
    _receiptStatistics = new EventStatistics();
    _sendingStatistics = new EventStatistics();
  }

  @Override
  public void portEvent(IOPortEvent event) throws IllegalActionException {
    if (_port.equals(event.getPort())) {
      IOPortEvent pe = (IOPortEvent) event;
      if (_port.isInput() && IOPortEvent.GET_END == pe.getEventType()) {
        // TODO check if we need to handle token-arrays here?
        // probably not as we're basically only interested in counting msgs,
        // not really in looking at the content...
        acceptReceivedMessage(pe.getToken());
      }
      if (_port.isOutput() && IOPortEvent.SEND_END == pe.getEventType()) {
        // TODO check if we need to handle token-arrays here?
        // probably not as we're basically only interested in counting msgs,
        // not really in looking at the content...
        acceptSentMessage(pe.getToken());
      }
    }
  }

  /**
   * Log the count and timing of a new received message.
   *
   * @param msg
   */
  public void acceptReceivedMessage(Object msg) {
    _receiptStatistics.acceptEvent(msg);
  }

  /**
   * Log the count and timing of a new sent message.
   *
   * @param msg
   */
  public void acceptSentMessage(Object msg) {
    _sendingStatistics.acceptEvent(msg);
  }

  /**
   *
   * @return the statistics of received messages
   */
  public EventStatistics getReceiptStatistics() {
    return _receiptStatistics;
  }

  /**
   *
   * @return the statistics of sent messages
   */
  public EventStatistics getSendingStatistics() {
    return _sendingStatistics;
  }

  /**
   *
   * @return the count of sent messages
   */
  public long getNrSentMessages() {
    return _sendingStatistics.getNrEvents();
  }

  /**
   * @return the average interval in msec between sending two messages
   */
  public long getAvgIntervalSentMessages() {
    return _sendingStatistics.getAvgInterval();
  }

  /**
   *
   * @return the count of received messages
   */
  public long getNrReceivedMessages() {
    return _receiptStatistics.getNrEvents();
  }

  /**
   * @return the average interval in msec between receiving two messages
   */
  public long getAvgIntervalReceivedMessages() {
    return _receiptStatistics.getAvgInterval();
  }

  /**
   * clear all statistical data
   */
  public void reset() {
    _receiptStatistics.reset();
    _sendingStatistics.reset();
  }

  public String getName() {
    return _port.getFullName();
  }

  // private properties

  // We assume that statistics are only used short-term, during tests.
  // If they would be operationally used in high-throughput systems with long-running flows,
  // keeping references to model elements could be risky, and would need explicit cleanup!
  private IOPort _port;

  private EventStatistics _receiptStatistics;
  private EventStatistics _sendingStatistics;
}
