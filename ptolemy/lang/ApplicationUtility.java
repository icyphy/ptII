/*
Miscelleneous utilities for command-line applications.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)

*/

package ptolemy.lang;

//////////////////////////////////////////////////////////////////////////
//// ApplicationUtility
/** Miscelleneous utilities for command-line applications.
 *
 *  @author Jeff Tsay
 */
public class ApplicationUtility {

    // private constructor prevent instantiation of this class
    private ApplicationUtility() {}

    /** Ensure the condition evaluates to true if enableAsserts is true.
     *  This method simply calls assert(condition, "no reason specified").
     */     
    public static final void assert(boolean condition) {
        if (enableAsserts) {
           assert(condition, "no reason specified");
        }
    }

    /** Ensure the condition evaluates to true if enableAsserts is true.
     *  If condition is false, call error() with the argument error
     *  message.
     */     
    public static final void assert(boolean condition, String errMsg) {
        if (enableAsserts && !condition) {
           error("Assertion failed: " + errMsg);
        }
    }

    /** Handle an error in the application. If exceptionOnError is true, throw
     *  a RuntimeException. Otherwise, print the error message to standard 
     *  error. If exitOnError is true, exit the application.
     */
    public static final void error(String msg) {
        errors++;
        if (exceptionOnError) {
           throw new RuntimeException(msg);
        } else {
           System.err.println("Error: " + msg);
       
           if (exitOnError) {
              System.exit(-1);
           }
        }
    }

    /** Print a status message to the standard out. */
    public static final void status(String msg) {
        System.out.println(msg);
    }

    /** Print a warning to standard error if enableWarnings is true. If
     *  errorOnWarning is true, call error() with the argument message. 
     */
    public static final void warn(String msg) {
        if (enableWarnings) {
           System.err.println("Warning: " + msg);
           warnings++;

           if (errorOnWarning) {
              error("Error on warning");
           }
        }
    }

    /** Print a trace message to standard out if enableTrace is true. Append
     *  the separator string after the message.
     */
    public static final void trace(String msg, String separator) {
        if (enableTrace) {
           System.out.print(msg + separator);
        }
    }
    /** Print a trace message to standard out if enableTrace is true. */
    public static final void trace(String msg) {
        if (enableTrace) {
           System.out.println("Trace: " + msg);
        }
    }

    /** If true, enable assertions. */
    public static boolean enableAsserts  = true;
    
    /** If true, enable warning messages. */    
    public static boolean enableWarnings = true;
    
    /** If true, enable trace messages. */    
    public static boolean enableTrace  = false;
    
    /** If true, throw an exception when error() is called. */    
    public static boolean exceptionOnError = true;
    
    /** If true and exceptionOnError is false, exit the application when 
     *  error() is called. 
     */    
    public static boolean exitOnError = false;
    
    /** If true, call error() when warn() is called. */
    public static boolean errorOnWarning = false;

    /** The number of errors encountered. */
    public static int errors   = 0;
    
    /** The number of warnings encountered. */
    public static int warnings = 0;
}