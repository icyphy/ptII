package ptolemy.actor.lib.jjs.modules.gdp;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.terraswarm.gdp.GDP_GCL;

/** Helper for the GDP JavaScript module for use by accessors.
 *  @author Nitesh Mor and Edward A. Lee
 */
public class GDPHelper {

    private GDP_GCL g;
    private String _logName;
    private boolean _subscribed = false;

    public GDPHelper(String logname, int iomode) {
        this.g = new GDP_GCL(logname, iomode);
        _logName = logname;    
    }

    public String read(long recno) {
        return this.g.read(recno);
    }

    public void append(String data) {
        this.g.append(data);
    }

    public void subscribe(final ScriptObjectMirror currentObj, long startrec, int numrec) {
        this.g.subscribe(startrec, numrec);
        Runnable blocking = new Runnable() {
            public void run() {
                while (_subscribed) {
                    // Zero arg means no timeout. Wait forever.
                    String result = g.get_next_data(0);
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
    
    public void unsubscribe(final ScriptObjectMirror currentObj) {
        // FIXME: Properly close the C side.
        _subscribed = false;
    }

    public String get_next_data(int timeout_msec) {
        return this.g.get_next_data(timeout_msec);
    }
    
}
