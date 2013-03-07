/* This java object implements a virtual-link for an AFDX Network.

@Copyright (c) 2010-2011 The Regents of the University of California.
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

package ptolemy.actor.lib.qm;

import ptolemy.actor.Actor;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.Receiver;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/** This java obejct implements a virtual-link which belongs to the end-system
 *  of an AFDX Network.
 *  For more information please refer to:
 *      <i>AFDX network simulation using PtolemyII</i>.
 *
 *  @author G. Lasnier
 *  @version $Id$
   @since Ptolemy II 0.2
 */
public class AFDXVlink {

    /** Constructor.
     * @param source
     * @exception IllegalActionException
     */
    public AFDXVlink (Receiver source) throws IllegalActionException {
        // 'vlink' parameter and value.
        Parameter vlParam = (Parameter) ((NamedObj) ((IntermediateReceiver) source).source)
                .getAttribute("vlink");
        this._name = ((StringToken) vlParam.getToken()).stringValue();

        // 'bag' parameter and value.
        Parameter bagParam = (Parameter) ((NamedObj) ((IntermediateReceiver) source).source)
                .getAttribute("bag");
        this._bag = ((DoubleToken) bagParam.getToken()).doubleValue() / 1000;

        // 'trameSize' parameter and value.
        Parameter tsParam = (Parameter) ((NamedObj) ((IntermediateReceiver) source).source)
                .getAttribute("trameSize");
        this._trameSize = ((IntToken) tsParam.getToken()).intValue();

        // 'schedulerMux' parameter and value.
        Parameter smParam = (Parameter) ((NamedObj) ((IntermediateReceiver) source).source)
                .getAttribute("schedulerMux");
        this._schedulerMux = ((StringToken) smParam.getToken()).stringValue();

        // 'source' connected to the vlink.
        this._source = ((IntermediateReceiver) source).source;
    }

    /** Constructor.
     * @param nm The name of the virtual-link.
     * @param b The value of the bag.
     * @param ts The size of the trame.
     * @param sched The name of the virtual-link scheduler.
     * @param src The initial actor connected to the virtual-link.
     */
    public AFDXVlink (String nm, Double b, int ts, String sched, Actor src) {
        this._name = nm;
        this._bag = b;
        this._trameSize = ts;
        this._source = src;
        this._schedulerMux = sched;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* The followings methods are classics getter/setter. */

    public String getName() {
        return _name;
    }

    public Double getBag() {
        return _bag;
    }

    public int getTrameSize() {
        return _trameSize;
    }

    public Actor getSource() {
        return _source;
    }

    public String getSchedulerMux() {
        return _schedulerMux;
    }

    public String toString() {
        return "Object AFDXVlink {"
                + "vl_name=" + _name + " bag=" + _bag.toString()
                + " trameSize=" + _trameSize
                + " schedulerMux=" + _schedulerMux
                + " source=" + _source
                + "}";
    }

    /** The name of the virtual-link
     */
    private String _name;

    /** The bag of the virtual-link
     */
    private Double _bag;

    /** The size of the trame for the virtual-link
     */
    private int _trameSize;

    /** The name of the virtual-link scheduler multiplexor
     */
    private String _schedulerMux;

    /** The source actor connected to the virtual-link
     */
    private Actor _source;

}
