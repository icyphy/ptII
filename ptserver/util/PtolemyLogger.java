/* Ptolemy logger that provides common settings for loggers

 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

///////////////////////////////////////////////////////////////////
//// PtolemyLogger

/** This class is responsible for setting up the logger
 * 
 * PtolemyLogger.getLogger method should be used to create or
 * access a logger object
 * 
 * @author pdf
 * @version $Id$
 * @Pt.ProposedRating Red (pdf)
 * @Pt.AcceptedRating Red (pdf)
 */
public class PtolemyLogger extends Logger {

    protected PtolemyLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    //////////////////////////////////////////////////////////////////////
    ////                public methods
    /** Method to create or access a logger set up with the common settings
     * 
     * @param name Name of the logger. Should be the hierarchical name of the class where it's used
     * @return Standard logger object to be used for logging (see Logger.log method)
     */
    public static Logger getLogger(String name) {
        FileHandler logFile = null;
        Logger logger = Logger.getLogger(name);

        try {
            logFile = new FileHandler(_logFileName, false);
            logFile.setFormatter(new XMLFormatter());
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logger.addHandler(logFile);
        logger.setLevel(Level.ALL);

        return logger;
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables

    // TODO Move this to a common configuration place
    private static String _logFileName = "C:\\Users\\Peter\\Workspace\\pt.log";
}
