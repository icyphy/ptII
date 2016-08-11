/* Helper for the Global Data Plane (GDP) JavaScript module.

   Copyright (c) 2015-2016 The Regents of the University of California.
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

import org.terraswarm.gdp.EP_TIME_SPEC;
import org.terraswarm.gdp.GDP;
import org.terraswarm.gdp.GDPException;
import org.terraswarm.gdp.GDP_GCL;
import org.terraswarm.gdp.GDP_NAME;


import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
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

    /** Create a GDP Helper by opening a pre-existing GDP Log
     *  or creating one if necessary.
     *
     *  @param logName The name of the log.  The format can be any
     *  string, but as multiple users could be sharing a log server, a
     *  dot-separated log name that is the reverse domain name
     *  followed by the user name could be helpful.  For example,
     *  org.ptolemy.claudius.myLog01.
     *  @param ioMode The i/o mode for the log (0: for internal use
     *  only, 1: read-only, 2: read-append, 3: append-only).
     *  @param logdName  Name of the log server where this should be 
     *  placed if it does not yet exist.  If the string is of length
     *  zero, then the hostname is used.
     *  @exception GDPException If the log does not exist or if the
     *  connection to the log server fails.
     */
    public GDPHelper(String logName, int ioMode, String logdname) throws GDPException {
        // The GDP_GCL constructor calls the gdp_init() C function for us.
        System.out.println("GDPHelper.GDPHelper(" + logName + ", " + ioMode + ", " + logdname + "): ");
	if (logdname.length() == 0) {
	    try {
		logdname = InetAddress.getLocalHost().getHostName();
	    } catch (Throwable throwable) {
		throw new GDPException("Could not get the local host name.", throwable);
	    }
	}
        _gcl = GDP_GCL.newGCL(new GDP_NAME(logName), ioMode, new GDP_NAME(logdname));
        _logName = logName;    
    }

    /** Append a string to the log.
     *  @param data The string to be appended, which assumed to be UTF-8.
     */   
    public void append(String data) throws GDPException {
        byte [] bytes = data.getBytes(StandardCharsets.UTF_8);
        System.out.println("GDPHelper.append(" + data + ")");
        _gcl.append(bytes);
    }

    /** Get the next data.
     *  @param timeout The timeout in milliseconds.
     *  @return The next data.
     */
    public String getNextData(int timeout) {
	// FIXME: timeout should be a long.
        HashMap<String, Object> gdp_event = GDP_GCL.get_next_event(_gcl, timeout);
        System.out.println("GDPHelper.getNextData(" + timeout + "): " + gdp_event);
        return _datumToData((HashMap<String, Object>)gdp_event.get("datum"));
    }
    
    /** Read a record.
     *  @param recordNumber The record number to be read.  The first record
     *  in the log is record 1.
     *  @return A string representing the records that were read or the empty
     *  string if no records were read.
     */
    public String read(long recordNumber) throws GDPException {
        HashMap<String,Object> datum = _gcl.read(recordNumber);
        return _datumToData(datum);
    }

    /** Set the value of the GDP debug flag.
     * @param debug The value of the GDP debug flag.  See
     * gdp/README-developers.md for a complete summary.  The value is typically
     * <code><i>pattern</i>=<i>level</i></code>, for example
     * <code>gdplogd.physlog=39</code>.  To see the patterns, use the
     * "what" command or <code>strings $PTII/lib/libgdp* | grep
     * '@(#)'</code>.  Use <code>*=40</code> to set the debug level to
     * 40 for all components. The value of level is not usually over
     * 127.  Values over 100 may modify the behavior.
     */
    public void setDebugLevel(String debugLevel) {
        GDP.dbg_set(debugLevel);
    }

    /** Subscribe to a log.
     *  @param currentObj The handle   
     *  @param startRecord The index of the starting record.  The first 
     *  record in the log is record 1.
     *  @param numberOfRecords The number of records to read.
     *  @param timeout The timeout in milliseconds.
     */
    public void subscribe(final ScriptObjectMirror currentObj, int startRecord,
            int numberOfRecords, int timeout) throws GDPException {
        // FIXME timeout should be a long.
        EP_TIME_SPEC timeoutSpec = null;
        if (timeout != 0) {
            timeoutSpec = new EP_TIME_SPEC(timeout/1000,
                    0, /* nanoseconds */
                    0.001f /* accuracy in seconds */);
        }
        // FIXME: We need to cast to a long here because it seems
        // like passing longs from JavaScript does not work for us.
        _gcl.subscribe((long)startRecord, numberOfRecords, timeoutSpec);
        Runnable blocking = new Runnable() {
            public void run() {
                while (_subscribed) {
                    // Zero arg means no timeout. Wait forever.
                    HashMap<String, Object> result = GDP_GCL.get_next_event(_gcl, 0);
                    if (result != null) {
                        System.out.println("GDPHelper.subscribe(): about to call handleResponse " + result.toString());
                        currentObj.callMember("handleResponse", result.toString());
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

    /** Given a datum, return the value of the "data" key as a String.
     *  @param datum A HashMap with a data key.
     *  @return The value of the "data" key.
     */
    private String _datumToData(HashMap<String, Object> datum) {
        if (datum != null) {
            Object data = datum.get("data");
            if (data != null) {
                if (data instanceof byte []) {
                    try {
                        return new String((byte[]) data, "UTF-8");
                    } catch (Throwable throwable) {
                        return throwable.toString();
                    }
                } else {
                    return "Object: " + data;
                }
            } else {
                return "data was null?";
            }
        } else {
            return "datum was null?";
        }
    }

    /** The log. */
    private GDP_GCL _gcl;

    /** The name of the log. */
    private String _logName;

    /** True if the log has been subscribed to. */
    private boolean _subscribed = false;
}
