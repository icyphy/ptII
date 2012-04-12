/* An actor that represents a placeholder for data sent to a web page.

 Copyright (c) 1998-2011 The Regents of the University of California.
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
package org.ptolemy.ptango.lib;

import ptolemy.actor.lib.gui.MonitorValue;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// WebSink

/** An actor that represents a placeholder for data sent to a web page. Connect 
 * this actor to an output port on an HttpService actor to indicate that the 
 * HttpService actor should copy the output port's data to a web page.  
 *
 * @author Beth Latronico
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ltrnc)
 * @Pt.AcceptedRating Red (ltrnc)
 * @see org.ptolemy.ptango.HttpService
 * @see org.ptolemy.ptango.HttpCompositeServiceProvider
 */
public class WebSink extends MonitorValue {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public WebSink(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);  
        
        // See ptolemy.actor.lib.genericsinks.xml  
        
        /*
        <property name="displayWidth" class="ptolemy.data.expr.Parameter" value="20"/>
        <property name="_icon" class="ptolemy.vergil.icon.UpdatedValueIcon">
           <property name="attributeName" value="value"/>
           <property name="displayWidth" value="displayWidth"/>
        </property>
        */
    }
}
