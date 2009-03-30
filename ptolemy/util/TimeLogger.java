/* A class that stores timed events in a file.
 * 
 */package ptolemy.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

//////////////////////////////////////////////////////////////////////////
////TimeLineLogger

/**
A class that stores events in a file together with a time stamp,
the thread name and some (user defined) context.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 7.2
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public class TimeLogger {

    /** Construct a TimeLogger, that will start log the events in the file, specified
     *  with filename, when it is enabled and when the file has been opened.
     * @param filename The name of the file.
     * @param enabled Specifies whether the file is enabled. Typically you only want
     * this for debugging purposes. 
     */
    public TimeLogger(String filename, boolean enabled) {
        _filename = filename;
        _enabled = enabled;        
    }

    /** Close the file. Only after the file has been closed, you have guaranthees
     *  that everything has been written.
     */
    public void close() {
        if (_enabled) {
            synchronized(this) {
                _fileWriter.close();
            }
        }
    }
    
    /** Log an event in the file together with a time stamp,
     *  the thread name and some (user defined) context.
     *  context The context (for example the class name).
     *  log The log message.
     */
    public void log(String context, String log) {
        if (_enabled) {
            synchronized(this) {
                // Syntax: "[<(time)>] [<(processid)>] [<(context)>] [<(message)>]"
                _fileWriter.println("[<(" +
                        (System.currentTimeMillis() - _startTime) / 1000.0 +
                        ")>] [<(" + Thread.currentThread().getName() +
                        ")>] [<(" + context + ")>] [<(" + log + ")>]");
            }
        }        
    }

    
    /** Open the file.
     */    
    public void open() {
        if (_enabled) {
            synchronized(this) {
                _startTime = System.currentTimeMillis();
                try {
                    _fileWriter = new PrintWriter(
                        new BufferedWriter(new FileWriter(_filename)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
    }
    
    /** A flag that is true when the logger is enabled.*/
    private boolean _enabled;
    
    /** The name of the file.*/
    private String _filename;
    
    /** A print writer to store the file.*/
    private PrintWriter _fileWriter = null;
    
    /** A time the file has been opened.*/
    private long _startTime;
}
