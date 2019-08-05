/* This interface is implemented by any actor that can reflect an HLA attribute.

@Copyright (c) 2013-2019 The Regents of the University of California.
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

package org.hlacerti.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// HlaReflectable

/**
 * FIXME
 *
 *  @author Janette Cardoso and Edward A. Lee
 *  @version $Id: HlaPublisher.java 214 2018-04-01 13:32:02Z j.cardoso $
 *  @since Ptolemy II 11.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public interface HlaReflectable extends Nameable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the HLA attribute handle.
     * @return The HLA attribute handle.
     * @see #setAttributeHandle.
     */
    public int getAttributeHandle();

    /** Return the HLA class handle.
     * @return the HLA class handle.
     * @see #setClassHandle.
     */
    public int getClassHandle();

    /** Returns the HLA object instance handle.
     * @return The HLA object instance handle.
     * @see #setInstanceHandle.
     */
    public int getInstanceHandle();

    /** FIXME: This should probably not be here. See HlaManager. */
    public TypedIOPort getOutputPort();

    /** Set the HLA attribute handle.
     * @param attributeHandle The attributeHandle to set.
     * @see #getAttributeHandle.
     */
    public void setAttributeHandle(int attributeHandle);

    /** Set the HLA class handle.
     * @param classHandle The classHandle to set.
     * @see #getClassHandle.
     */
    public void setClassHandle(int classHandle);

    /** Set the HLA object instance handle only when wildcard (joker_) is used.
     *
     * @param instanceHandle The HLA object instance to set.
     * @see #getInstanceHandle.
     */
    public void setInstanceHandle(int instanceHandle);

    /** Store each updated value of the HLA attribute (mapped to this actor) in
     *  the token queue. Then, program the next firing time of this actor to
     *  send the token at its expected time. This method is called by the
     *  {@link HlaManager} attribute.
     *  @param event The event containing the new value of the HLA
     *  attribute and its time-stamp.
     *  @exception IllegalActionException Not thrown here.
     */
    public void putReflectedHlaAttribute(HlaTimedEvent event)
            throws IllegalActionException;

    /** Return the value of the <i>attributeName</i> parameter.
     *  @return The value of the <i>attributeName</i> parameter.
     *  @exception IllegalActionException If the class name is empty.
     */
    public String getHlaAttributeName() throws IllegalActionException;

    /** Return the value of the <i>className</i> parameter.
     *  @return The value of the <i>className</i> parameter.
     *  @exception IllegalActionException If the class name is empty.
     */
    public String getHlaClassName() throws IllegalActionException;

    /** Return the value of the <i>instanceName</i> parameter.
     *  @return The value of the <i>instanceName</i> parameter.
     *  @exception IllegalActionException If the class name is empty.
     */
    public String getHlaInstanceName() throws IllegalActionException;

    /** Indicate if the HLA publisher actor uses the CERTI message
     *  buffer API.
     *  @return true if the HLA publisher actor uses the CERTI message and
     *  false if it doesn't.
     */
    public boolean useCertiMessageBuffer();
}
