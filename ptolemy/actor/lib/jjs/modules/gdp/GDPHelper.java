/* Helper for the Global Data Plane (GDP) JavaScript module.

   Copyright (c) 2015 The Regents of the University of California.
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

package ptolemy.actor.lib.jjs.modules.gdp;

import org.terraswarm.gdp.GDP_GCL;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

/** Helper for the GDP JavaScript module for use by accessors.
 *
 *  @author Nitesh Mor and Edward A. Lee, Contributor: Christopher Brooks.
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class GDPHelper {

    /** Create a GDP Helper.
     *  @param logName FIXME: What is the format?
     *  @param ioMode FIXME: What is the format?
     */
    public GDPHelper(String logName, int ioMode) {
        _gcl = new GDP_GCL(logName, ioMode);
        _logName = logName;    
    }

    /** Append a string to the log.
     *  @param data The string to be appended.
     */   
    public void append(String data) {
        _gcl.append(data);
    }

    /** Get the next data.
     *  @param timeout The timeout in milliseconds.
     *  @return The next data.
     */
    public String getNextData(int timeout) {
        return _gcl.get_next_data(timeout);
    }
    
    /** Read the indicated number of records.
     *  @param numberOfRecords The number of records to read.
     *  @return A string representing the records that were read.
     */
    public String read(long numberOfRecords) {
        return _gcl.read(numberOfRecords);
    }

    /** Subscribe to a log.
     *  @param currentObj The handle   
     *  @param startRecord The index of the starting record.
     *  @param numberOfRecords The number of records to read.
     */
    public void subscribe(final ScriptObjectMirror currentObj, long startRecord,
            int numberOfRecords) {
        _gcl.subscribe(startRecord, numberOfRecords);
        Runnable blocking = new Runnable() {
            public void run() {
                while (_subscribed) {
                    // Zero arg means no timeout. Wait forever.
                    String result = _gcl.get_next_data(0);
                    if (result != null) {
                        currentObj.callMember("handleResponse", result);
                    } else {
                        _subscribed = false;
                    }
                }
            }
        };
        Thread thread = new Thread(blocking, "GDP subscriber thread: " + _logName);
        // Start this as a deamon thread so that it doesn't block exiting the process.
        thread.setDaemon(true);
        thread.start();
    }
    
    /** Unsubscribe from a log.
     *  @param currentObj The handle   
     */
    public void unsubscribe(final ScriptObjectMirror currentObj) {
        // FIXME: Properly close the C side.
        _subscribed = false;
    }

    /** The log. */
    private GDP_GCL _gcl;

    /** The name of the log. */
    private String _logName;

    /** True if the log has been subscribed to. */
    private boolean _subscribed = false;
}
