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

public class ApplicationUtility {

  // private constructor prevent instantiation of this class
  private ApplicationUtility() {}

  public static final void assert(boolean condition) {
    if (enableAsserts) {
       assert(condition, "no reason specified");
    }
  }

  public static final void assert(boolean condition, String errMsg) {
    if (enableAsserts && !condition) {
       error("Assertion failed: " + errMsg);
    }
  }

  public static final void warn(String msg) {
    if (enableWarnings) {
       System.err.println("Warning: " + msg);
       warnings++;

       if (errorOnWarning) {
          error("Error on warning");
       }
    }
  }

  public static final void trace(String msg, String separator) {
    if (enableTrace) {
       System.out.print(msg + separator);
    }
  }

  public static final void trace(String msg) {
    if (enableTrace) {
       System.out.println("Trace: " + msg);
    }
  }

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

  public static boolean enableAsserts  = false;
  public static boolean enableWarnings = true;
  public static boolean enableTrace  = false;
  public static boolean exceptionOnError = true;
  public static boolean exitOnError = false;
  public static boolean errorOnWarning = false;

  public static int errors   = 0;
  public static int warnings = 0;
}