/* A container for counting and value statistics

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

/**
 * Instances of this class maintain statistics about <i>long</i> values.
 * 
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Green (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
*/
final class StatisticalLongData {

  /**
   * Recalculate statistics, including this new data value
   * 
   * @param data the value to include in these statistics
   */
  public synchronized void acceptData(long data) {
    _count++;
    if (_minData > data) {
      _minData = data;
    }
    if (_maxData < data) {
      _maxData = data;
    }
    _avgData += (data - _avgData) / _count;
  }

  /**
   * Reset all statistical data :
   * <ul>
   * <li>count becomes 0</li>
   * <li>average value becomes 0</li>
   * <li>minimal value becomes Long.MAX_VALUE</li>
   * <li>maximal value becomes Long.MIN_VALUE</li>
   * </ul>
   * 
   */
  public synchronized void reset() {
    _count = 0;
    _avgData = 0;
    _minData = Long.MAX_VALUE;
    _maxData = Long.MIN_VALUE;
  }

  /**
   * 
   * @return the average of the received data values
   */
  public long getAvgData() {
    return _avgData;
  }

  /**
   * @return the maximal data value received.
   */
  public long getMaxData() {
    return _maxData;
  }

  /**
   * @return the minimal data value received.
   */
  public long getMinData() {
    return _minData;
  }

  /**
   * 
   * @return the number of received values
   */
  public long getCount() {
    return _count;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[StatisticalLongData:");
    buffer.append(" count: ");
    buffer.append(_count);
    buffer.append(" avgData: ");
    buffer.append(_avgData);
    buffer.append(" minData: ");
    buffer.append(_minData);
    buffer.append(" maxData: ");
    buffer.append(_maxData);
    buffer.append("]");
    return buffer.toString();
  }
  
  // private properties
  
  private long _count;
  private long _avgData;
  private long _minData = Long.MAX_VALUE;
  private long _maxData = Long.MIN_VALUE;
}
