/* This java object implements a virtual-link for an AFDX Network.

@Copyright (c) 2010-2014 The Regents of the University of California.
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

package ptolemy.domains.de.lib.aspect;

import ptolemy.actor.Actor;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.Receiver;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/** This java object implements a virtual-link which belongs to the end-system
 *  of an AFDX Network.
 *  For more information please refer to:
 *      <i>AFDX network simulation using PtolemyII</i>.
 *
 *  @author G. Lasnier
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Yellow (glasnier)
 */
public class AFDXVlink {

    /** Create a new virtual link object and initialize parameters.
     * @param source The source receiver.
     * @exception IllegalActionException If parameters cannot be initialized.
     */
    public AFDXVlink(Receiver source) throws IllegalActionException {
        // 'vlink' parameter and value.
        Parameter vlParam = (Parameter) ((NamedObj) ((IntermediateReceiver) source).source)
                .getAttribute("vlink");
        this._name = ((StringToken) vlParam.getToken()).stringValue();

        // 'bag' parameter and value.
        Parameter bagParam = (Parameter) ((NamedObj) ((IntermediateReceiver) source).source)
                .getAttribute("bag");
        this._bag = ((DoubleToken) bagParam.getToken()).doubleValue() / 1000;

        // 'frameSize' parameter and value.
        Parameter tsParam = (Parameter) ((NamedObj) ((IntermediateReceiver) source).source)
                .getAttribute("frameSize");
        this._frameSize = ((IntToken) tsParam.getToken()).intValue();

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
     * @param ts The size of the frame.
     * @param sched The name of the virtual-link scheduler.
     * @param src The initial actor connected to the virtual-link.
     */
    public AFDXVlink(String nm, Double b, int ts, String sched, Actor src) {
        this._name = nm;
        this._bag = b;
        this._frameSize = ts;
        this._source = src;
        this._schedulerMux = sched;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* The followings methods are classics getter/setter. */

    /** Get the name of the virtual link object.
     * @return The name.
     * @see #setName(String)
     */
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the virtual link object.
     * @param name The name.
     * @see #getName()
     */
    public void setName(String name) {
        _name = name;
    }

    /** Get the value of the bag.
     * @return The value.
     * @see #setBag(Double)
     */
    public Double getBag() {
        return _bag;
    }

    /**
     * Set the value of the bag.
     * @param bag The value.
     * @see #getBag()
     */
    public void setBag(Double bag) {
        _bag = bag;
    }

    /** Get the frame size.
     * @return The size.
     * @see #setFrameSize(int)
     */
    public int getFrameSize() {
        return _frameSize;
    }

    /** Set the frame size.
     * @param size The size.
     * @see #getFrameSize()
     */
    public void setFrameSize(int size) {
        _frameSize = size;
    }

    /** Get the source actor.
     * @return The actor.
     * @see #setSource(Actor)
     */
    public Actor getSource() {
        return _source;
    }

    /** Set the source actor.
     * @param source The actor.
     * @see #getSource()
     */
    public void setSource(Actor source) {
        _source = source;
    }

    /** Get the name of the scheduler multiplexor.
     * @return The name.
     * @see #setSchedulerMux(String)
     */
    public String getSchedulerMux() {
        return _schedulerMux;
    }

    /** Set the name of the scheduler multiplexor.
     * @param name The name.
     * @see #getSchedulerMux()
     */
    public void setSchedulerMux(String name) {
        _schedulerMux = name;
    }

    /** Return a string representation of this object.
     *  @return The string representation.
     */
    @Override
    public String toString() {
        return "Object AFDXVlink {" + "vl_name=" + _name + " bag="
                + _bag.toString() + " frameSize=" + _frameSize
                + " schedulerMux=" + _schedulerMux + " source=" + _source + "}";
    }

    /** The name of the virtual-link
     */
    private String _name;

    /** The bag of the virtual-link
     */
    private Double _bag;

    /** The size of the trame for the virtual-link
     */
    private int _frameSize;

    /** The name of the virtual-link scheduler multiplexor
     */
    private String _schedulerMux;

    /** The source actor connected to the virtual-link
     */
    private Actor _source;

}
