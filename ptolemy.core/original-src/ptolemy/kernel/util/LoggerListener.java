/* A debug listener that sends messages using Java's Logger API.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.kernel.util;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

///////////////////////////////////////////////////////////////////
//// LoggerListener

/**
 A debug listener that sends messages using Java's Logger API.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class LoggerListener implements DebugListener {
    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a logger.
     *  @param name The name for this logger. This should normally be a fully qualified
     *   classname or full name of the object being listened to.
     *  @param directory The directory in which to store the log file, or null to use the system temporary directory.
     *  @param level The logger level.
     *  @exception IllegalActionException If the log file cannot be opened.
     */
    public LoggerListener(String name, File directory, Level level)
            throws IllegalActionException {
        _logger = Logger.getLogger(name);
        _logger.setLevel(level);
        if (directory != null) {
            if (!directory.exists()) {
                // Attempt to create the directory.
                try {
                    directory.mkdir();
                } catch (SecurityException ex) {
                    throw new IllegalActionException(
                            "Failed to create directory for log file: "
                                    + directory.getPath());
                }
            }
            if (!directory.isDirectory()) {
                throw new IllegalActionException(
                        "Cannot create directory for log file. There is a file in the way: "
                                + directory.getPath());
            }
            if (!directory.canWrite()) {
                throw new IllegalActionException(
                        "Directory for log file is not writable: "
                                + directory.getPath());
            }
        }
        try {
            // Use rotating file names, file limit of 1Mbyte, limit of 10 files, append mode.
            String directoryName = "%t/";
            if (directory != null) {
                directoryName = directory.getCanonicalPath();
                if (!directoryName.endsWith("/")) {
                    directoryName += "/";
                }
            }
            String filename = directoryName + name + "%g.log";

            // To prevent logs from going to the console.
            _logger.setUseParentHandlers(false);
            _handler = new FileHandler(filename, 1000000, 10, false);

            // Lamely, creating a FileHandler is useless without a formatter.
            SimpleFormatter formatter = new SimpleFormatter();
            _handler.setFormatter(formatter);

            _logger.addHandler(_handler);

            _logger.log(Level.INFO, "******* Starting new log for " + name);
        } catch (Exception e) {
            throw new IllegalActionException(null, e,
                    "Failed to open log file: " + name);
        }
    }

    /** Create a logger.
     *  This constructor creates a logger that logs all messages,
     *  and stores the log in system temporary directory (where ever that is).
     *  @param name The name for this logger. This should normally be a fully qualified
     *   classname or full name of the object being listened to.
     *  @param directory The directory in which to store the log file, or null to use the system temporary directory.
     *  @exception IllegalActionException If the log file cannot be opened.
     */
    public LoggerListener(String name, File directory)
            throws IllegalActionException {
        this(name, directory, Level.ALL);
    }

    /** Create a logger.
     *  This constructor creates a logger that logs all messages,
     *  and stores the log in system temporary directory (whereever that is).
     *  @param name The name for this logger. This should normally be a fully qualified
     *   classname or full name of the object being listened to.
     *  @exception IllegalActionException If the log file cannot be opened.
     */
    public LoggerListener(String name) throws IllegalActionException {
        this(name, null, Level.ALL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the file handler.
     */
    public void close() {
        _handler.close();
    }

    /** Send a string representation of the event to the log.
     *  @param event The event.
     */
    @Override
    public void event(DebugEvent event) {
        _logger.log(Level.INFO, event.toString());
    }

    /** Send the message to the log.
     *  @param level The level of the message.
     *  @param message The message.
     */
    public void log(Level level, String message) {
        _logger.log(level, message);
    }

    /** Send the message to the log.
     *  @param message The message.
     */
    @Override
    public void message(String message) {
        _logger.log(Level.INFO, message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The file handler. */
    private FileHandler _handler;

    /** The logger. */
    private Logger _logger;
}
