/* This class saves the log result in the log file in the temporary folder.
 * 
 * Copyright (c) 2012-2013,
 * Programming Environments Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 *  http://www.opensource.org/licenses/bsd-license.php)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Authors nor the name of Linkopings University nor
 *   the names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ptolemy.domains.openmodelica.lib.omc;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import ptolemy.kernel.util.IllegalActionException;

/**
   <p>This class saves the log result in the log file in the temporary folder.</p>

   @author Mana Mirzaei
   @version $Id$
   @since Ptolemy II 9.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public class OMCLogger {
    /** Construct an OpenModelica Compiler(OMC) logger.
     *  This constructor has no parameter.
     *  It creates the log file in the temporary folder and sets the format
     *  of the log to show date and time first.
     *  This private Constructor prevents other class from instantiating.
     * @throws IllegalActionException 
     */
    private OMCLogger() throws IllegalActionException {

        String logPath = null;
        String username = System.getenv("USERNAME");
        String temp = System.getProperty("java.io.tmpdir");

        if (username == null) {
            logPath = temp + "/nobody/OpenModelica/";
        } else {
            logPath = temp + "/" + username + "/OpenModelica/";
        }

        // Create the directory.
        File logPathFile = new File(logPath);
        if (logPathFile.exists()) {
            if (!logPathFile.isDirectory()) {
                String message = "\"" + logPath
                        + "\" is a file, not a directory?"
                        + "Please delete it.";
                omcLogger.severe(message);
                throw new IllegalActionException(message);
            }
        } else {
            if (!logPathFile.mkdirs()) {
                String message = "Could not make the \"" + logPath
                        + "\" directory?";
                omcLogger.severe(message);
                throw new IllegalActionException(message);
            }
        }

        // Create  the log file. 
        String logFileName = logPath + "omcLog.txt";

        try {
            _fileHandler = new FileHandler(logFileName);
        } catch (SecurityException ex) {
            String message = "Security error related to the file handler,"
                    + " failed to create a FileHandler for \"" + logFileName
                    + "\"!";
            omcLogger.severe(message);
            throw new IllegalActionException(null, ex, message);
        } catch (IOException ex) {
            String message = "Unable to create file handler!"
                    + " failed to create a FileHandler for \"" + logFileName
                    + "\".";
            omcLogger.severe(message);
            throw new IllegalActionException(null, ex, message);
        }
        // Set format of the log to show date and time first.
        _fileHandler.setFormatter(new Formatter() {
            public String format(LogRecord rec) {
                StringBuffer buf = new StringBuffer(1000);
                buf.append(new java.util.Date());
                buf.append(' ');
                buf.append(rec.getLevel());
                buf.append(' ');
                buf.append(formatMessage(rec));
                buf.append('\n');
                return buf.toString();
            }
        });
        omcLogger.addHandler(_fileHandler);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                 ////

    /** Logger name of OpenModelica Compiler(OMC).*/
    public String loggerName = "omcLogger";

    /** Create the OpenModelica Compiler(OMC) logger. */
    public Logger omcLogger = Logger.getLogger("omcLogger");

    ///////////////////////////////////////////////////////////////////
    ////                          public methods                  ////

    /**
     * Get the Info LogLevel and info message will be written in the log file. 
     * @param infoMessage The info message. 
     */
    public void getInfo(String infoMessage) {
        omcLogger.info(infoMessage);
    }

    /**
     * Get the Warning LogLevel and warning message will be written in the log file.
     * @param warningMessage The warning message.
     */
    public void getWarning(String warningMessage) {
        omcLogger.warning(warningMessage);
    }

    /**
     * Get the Sever LogLevel and sever message will be written in the log file.
     * @param severMessage The sever message.
     */
    public void getSever(String severMessage) {
        omcLogger.severe(severMessage);
    }

    /**
     * Create an instance of OMCLogger in order to provide a global point of access to this instance.
     * It provides a unique source of OMCLogger instance.
     */
    public static OMCLogger getInstance() {

        if (_omcLoggerInstance == null) {
            try {
                _omcLoggerInstance = new OMCLogger();
            } catch (IllegalActionException e) {
               String message = "Unable to get instance of OMCLogger.";
               _omcLoggerInstance.getSever(message);
               //FIXME Add exception
            }
        }
        return _omcLoggerInstance;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The handler for writing to OpenModelica Compiler(OMC) log file.
    private FileHandler _fileHandler = null;

    // An instance of OMCLogger in order to provide a unique source of OMCLogger instance.
    private static OMCLogger _omcLoggerInstance = null;
}
