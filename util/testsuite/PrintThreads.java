/* Print Information about all the threads

@Author: Christopher Hylands, based on code from Fusion Systems Group

@Copyright (c) 2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

/* This code was originally found at
   http://www.fsg.com/tech/threadmon.htm
   and is
   Copyright (c) 1997 Fusion Systems Group, a division of Context Integration, Inc. All rights reserved. 

   Fusion Systems Group
   One Wall Street Court, New York, NY, 10005 
   Phone: +1-212-376-6300
   Fax: +1-212-376-6320
   E-mail: threadmon@fsg.com 
*/

package util.testsuite;

import javax.swing.SwingUtilities; // for isEventDispatchThread()

//////////////////////////////////////////////////////////////////////////
//// PrintThreads
/** PrintThreads prints all the Threads in the current JVM
 */
public class PrintThreads {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the root ThreadGroup of the Java Virtual Machine. This
     * method assumes that the current Thread is a member of a ThreadGroup
     * which is a descendant of the root ThreadGroup. 
     */
    public static ThreadGroup rootThreadGroup() {
	ThreadGroup parent, rootGroup;

	parent = Thread.currentThread().getThreadGroup();
	do {
            rootGroup = parent;
            parent = parent.getParent();
        } while (parent != null);

	return rootGroup;
    }

    /** Return a String containing all the ThreadGroups in the JVM
     *  that are decendents of the root ThreadGroup.  
     */
    public static void allThreadGroups() {
	ThreadGroup rootGroup = rootThreadGroup();

	System.out.println("ThreadGroups: "
                + (rootGroup.activeGroupCount() + 1));

	System.out.println(rootGroup.toString());
	ThreadGroup threadGroups[]
	    = new ThreadGroup[rootGroup.activeGroupCount()];
	rootGroup.enumerate(threadGroups);

	String lineSeparator = _getLineSeparator();
	String results = new String();
	for (int i = 0; i < threadGroups.length; i++) {
	    results += threadGroups[i].toString() + lineSeparator;
	}
    }

    /** Return a string containing all the threads in the JVM
     * who are members of ThreadGroups which are descendants of the
     * root ThreadGroup.
     */
    public static String allThreads() {
	ThreadGroup rootGroup = null;
	try {
	    rootGroup = rootThreadGroup();
	} catch (Exception e) {
	    // If we are in an applet, then rootThreadGroup() might
	    // throw a security exception
	    rootGroup = Thread.currentThread().getThreadGroup();
	}

	String lineSeparator = _getLineSeparator();
	String results =
	    new String("Threads: " + rootGroup.activeCount() + lineSeparator
                    + "Current Thread (*) "
                    + (SwingUtilities.isEventDispatchThread() ?
                            "_is_" : "_is not_")
                    + " the Swing Event Dispatch Thread" + lineSeparator
                    + _getHeader() + lineSeparator);

        Thread threads[]= new Thread[rootGroup.activeCount()];
        rootGroup.enumerate(threads);

        for (int i = 0; i < threads.length; i++ ) {
	    Thread thread = threads[i];
	    results += toThreadDescription(thread) + lineSeparator;
	}
	return results;
    }

    /* Return a user friendly description of the thread.
     * We could use Thread.toString(), but that is hard to read.
     */
    public static String toThreadDescription(Thread thread) {
	try {
	    String name = thread.getName();
	    if (name == null)
		name = "unknown thread";

	    String group;
	    if ((thread.getThreadGroup() == null)
                    || (thread.getThreadGroup().getName() == null))
		group = "unknown group";
	    else
		group = thread.getThreadGroup().getName();

	    return _stringFormat(name, 35) + " "
		+ _stringFormat(group, 20) + " "  
                    + _stringFormat(Integer.toString(thread.getPriority()), 3) 
                        + " "
                        + _stringFormat(new Boolean(thread.isDaemon()).toString(), 6)
                            + " "
                            + _stringFormat(new Boolean(thread.isAlive()).toString(), 5)
                                + (Thread.currentThread().equals(thread) ? " *": "  ");
	} catch (Exception e) {
	    return _stringFormat("unknown thread with bad state" + e,
                    _getHeader().length() );
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /* Return a header string corresponding to the names of the fields in
     * the strings maintained by the dispString property of this class
     */
    private final static String _getHeader() {
	return _stringFormat("Name", 35)
	    + " " + _stringFormat("Group", 20) + " Pri Daemon Alive Curr";
    }

    /* Return the line separator.  If we are in an applet return \n */
    private final static String _getLineSeparator() {
	String results =  new String("\n");
	try {
	    results = System.getProperty("lineSeparator");
	} catch (Exception e) {
	    // getProperty() will throw an exception if we are
	    // running in an applet, in which case we default to "\n".
	}
	return results;
    }

    /* Pads inputString out with spaces to width length.
     */
    private final static String _stringFormat(String inputString, int length) {
	if (inputString == null)
	    inputString = " ";

	// Pad string out to constant width
	int stringLength = inputString.length();

	if (stringLength < length) {
	    for (int i = 0; i < length - stringLength; i++) {
		inputString = inputString + " ";
	    }
	}
	else if (inputString.length() > length) {
	    inputString = inputString.substring(0, length);
	}
	return inputString;
    }
}


