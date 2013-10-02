/* MetroDebugger is a debugging facility for Metro directors.

 Copyright (c) 2012-2013 The Regents of the University of California.
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
package ptolemy.domains.metroII.kernel;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;

///////////////////////////////////////////////////////////////////
//// MetroDebugger

/** 
 * MetroDebugger is a debugging facility for Metro directors. 
 * It enables printing information in a customized format.
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
*/
public class MetroDebugger implements Cloneable {

    /**
     * Construct a MetroDebugger with no info printed by default.
     */
    public MetroDebugger() {
        turnOffDebugging();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Clone a MetroDebugger.
     * 
     * @throws CloneNotSupportedException the object's class does not implement the Cloneable interface.
     */
    @Override
    public MetroDebugger clone() throws CloneNotSupportedException {
        MetroDebugger newObject = (MetroDebugger) super.clone(); 
        return newObject; 
    }

    /**
     * Set the string to be printed out at the beginning of each line.
     * 
     * @param prefix prefix string of each line .
     */
    public void setPrefix(String prefix) {
        _prefix = prefix;
    }

    /**
     * Return whether debugging info is being printed.
     * 
     * @return whether debugging info is being printed.
     */
    public boolean debugging() {
        return _debugging;
    }

    /**
     * Turn on printing debugging info.
     */
    public void turnOnDebugging() {
        _debugging = true;
    }

    /**
     * Turn off printing debugging info.
     */
    public void turnOffDebugging() {
        _debugging = false;
    }

    /**
     * Print out a title.
     * 
     * @param title the title to be printed out.
     */
    public void printTitle(String title) {
        if (!_debugging) {
            return;
        }
        System.out.println("---------- " + title);
    }

    /**
     * Print out text.
     * 
     * @param text the text to be printed out.
     */
    public void printText(String text) {
        if (!_debugging) {
            return;
        }
        System.out.println(_prefix + text);
    }

    /**
     * Print out the details of a MetroII event.
     * 
     * @param event the event to be printed out.
     */
    public void printMetroEvent(Builder event) {
        if (!_debugging) {
            return;
        }
        String buffer = _prefix;

        if (event.hasTime()) {
            buffer = buffer.concat("Time " + event.getTime().getValue());
        }

        buffer = buffer.concat(" " + event.getStatus().toString());

        buffer = buffer.concat(" " + event.getName().toString());

        System.out.println(buffer);
    }

    /**
     * Print out the details of a list of MetroII events.
     * 
     * @param metroIIEventList the event list.
     */
    public void printMetroEvents(Iterable<Builder> metroIIEventList) {
        if (!_debugging) {
            return;
        }
        printText("Event List Begins");
        for (Builder event : metroIIEventList) {
            printMetroEvent(event);
        }
        printText("Event List Ends");
    }

    /**
     * Print out the details of notified events in a list.
     * 
     * @param metroIIEventList the event list.
     */
    public void printNotifiedMetroEvents(Iterable<Builder> metroIIEventList) {
        if (!_debugging) {
            return;
        }
        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Event.Status.NOTIFIED) {
                printMetroEvent(event);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /**
     * Prefix of each line.
     */
    private String _prefix = "DEBUG: ";

    /**
     * Whether the debugging info is printed.d
     */
    private boolean _debugging = false;

}
