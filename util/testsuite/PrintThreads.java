/* Print Information about all the threads

 @Copyright (c) 2000-2014 The Regents of the University of California.
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

 */
/* This code was originally found at
 http://www.fsg.com/tech/threadmon.htm
 and is
 Copyright (c) 1997-2014 Fusion Systems Group, a division of Context Integration, Inc. All rights reserved.

 Fusion Systems Group
 One Wall Street Court, New York, NY, 10005
 Phone: +1-212-376-6300
 Fax: +1-212-376-6320
 E-mail: threadmon@fsg.com
 */
package util.testsuite;

import javax.swing.SwingUtilities;

///////////////////////////////////////////////////////////////////
//// PrintThreads

/** PrintThreads prints all the Threads in the current JVM.
 This class will work in both applications and applets.
 When run in an applet, this class attempts to gracefully handle
 the various security restrictions concerning getting the parent
 of a ThreadGroup.
 The output includes the number of threads and whether the current thread
 is the Swing Event Dispatch Thread.

 <p>To get a stack trace for each thread:
 <br> Under Unix, try <code>kill -3 <i>pid</i></code>, where
 <code><i>pid</i></code> is the process id from <code>ps</code>.
 <br> Under Windows, try <code>Ctrl-Break</code>.

 @author Christopher Hylands, based on code from Fusion Systems Group
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PrintThreads {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the root ThreadGroup of the Java Virtual Machine. This
     * method assumes that the current Thread is a member of a ThreadGroup
     * which is a descendant of the root ThreadGroup.
     * @return The root thread group.
     */
    public static ThreadGroup rootThreadGroup() {
        ThreadGroup parent;
        ThreadGroup rootGroup;

        parent = Thread.currentThread().getThreadGroup();

        do {
            rootGroup = parent;
            parent = parent.getParent();
        } while (parent != null);

        return rootGroup;
    }

    /** Return a String containing all the ThreadGroups in the JVM
     *  that are descendents of the root ThreadGroup.
     *  @return A string naming all the ThreadGroups.
     */
    public static String allThreadGroups() {
        ThreadGroup rootGroup = rootThreadGroup();

        StringBuffer results = new StringBuffer("ThreadGroups: "
                + (rootGroup.activeGroupCount() + 1) + "\n");

        results.append(rootGroup.toString());

        ThreadGroup[] threadGroups = new ThreadGroup[rootGroup
                .activeGroupCount()];
        rootGroup.enumerate(threadGroups);

        for (ThreadGroup threadGroup : threadGroups) {
            results.append(threadGroup.toString() + "\n");
        }

        return results.toString();
    }

    /** Return a string containing all the threads in the JVM
     *  who are members of ThreadGroups which are descendants of the
     *  root ThreadGroup.
     *  @param indicateEventDispatchThread true if we should indicate
     *  which thread is the Swing Event Dispatch Thread.
     *  @return A string naming all the threads.
     */
    public static String allThreads(boolean indicateEventDispatchThread) {
        ThreadGroup rootGroup = null;

        try {
            rootGroup = rootThreadGroup();
        } catch (Exception e) {
            // If we are in an applet, then rootThreadGroup() might
            // throw a security exception
            rootGroup = Thread.currentThread().getThreadGroup();
        }

        StringBuffer results = new StringBuffer("Threads: "
                + rootGroup.activeCount() + "\n");

        if (indicateEventDispatchThread) {
            results.append("Current Thread (*) "
                    + (SwingUtilities.isEventDispatchThread() ? "_is_"
                            : "_is not_")
                    + " the Swing Event Dispatch Thread\n");
        }

        results.append(_getHeader());

        Thread[] threads = new Thread[rootGroup.activeCount()];
        rootGroup.enumerate(threads);

        for (Thread thread : threads) {
            results.append(toThreadDescription(thread) + "\n");
        }

        return results.toString();
    }

    /** Return a user friendly description of the thread.
     *  We could use Thread.toString(), but that is hard to read.
     *  @param thread The thread
     *  @return A user friendly description of the thread.
     */
    public static String toThreadDescription(Thread thread) {
        String name = "Unnamed thread";
        String group = "Unnamed group";

        try {
            if (thread == null) {
                return "PrintThreads.toThreadDescription(): "
                        + "thread argument == null\n   "
                        + "This can happen if the thread was "
                        + "killed while PrintThreads was called";
            }

            if (thread.getName() != null) {
                name = thread.getName();
            }

            if (thread.getThreadGroup() != null
                    && thread.getThreadGroup().getName() != null) {
                group = thread.getThreadGroup().getName();
            }

            return _stringFormat(name, 35)
                    + " "
                    + _stringFormat(group, 20)
                    + " "
                    + _stringFormat(Integer.toString(thread.getPriority()), 3)
                    + " "
                    + _stringFormat(Boolean.valueOf(thread.isDaemon())
                            .toString(), 6)
                    + " "
                    + _stringFormat(Boolean.valueOf(thread.isAlive())
                            .toString(), 5)
                    + (Thread.currentThread().equals(thread) ? " *" : "  ");
        } catch (Exception e) {
            return _stringFormat(name, 35) + " " + _stringFormat(group, 20)
                    + " " + "PrintThread.toThreadDescription(): Bad State!: "
                    + e;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Return a header string corresponding to the names of the fields in
     * the strings maintained by the dispString property of this class
     */
    private final static String _getHeader() {
        return _stringFormat("Name", 35) + " " + _stringFormat("Group", 20)
                + " Pri Daemon Alive Curr\n";
    }

    /* Pads inputString out with spaces to width length.
     */
    private final static String _stringFormat(String inputString, int length) {
        StringBuffer results;
        if (inputString == null) {
            return " ";
        } else {
            results = new StringBuffer(inputString);
        }

        // Pad string out to constant width
        int stringLength = inputString.length();

        if (stringLength < length) {
            for (int i = 0; i < length - stringLength; i++) {
                results.append(" ");
            }
        } else if (inputString.length() > length) {
            results = new StringBuffer(results.substring(0, length));
        }

        return results.toString();
    }
}
