/* A class for configuring the parameters of NamedObjs.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// ParameterEditor
/** A class for configuring the parameter of NamedObjs.  This class is a
 *  PtolemyQuery that creates a typein entry for every Parameter in the 
 *  NamedObj.  This mechanism is similar to the way Ptolemy Classic worked,
 *  and is not, in general the best thing to do for all objects!     
 *  @author Steve Neuendorffer 
 *  @version $Id$
 */
public class ParameterEditor extends PtolemyQuery implements Configurer {
    /** Create a new configurer that will configure the given NamedObject.
     *  @exception IllegalActionException If a subclass of this configurer
     *  cannot configure the given target.  Not thrown in this base class.
     */
    public ParameterEditor(NamedObj target) 
        throws IllegalActionException {
        _target = target;
	addQueryListener(this);
	refresh();
    }

    /** Initialize the configurer to the current state of its target.   This
     *  is automatically called by the constructor, but may be called later
     *  if the target's state has changed.
     *  @exception IllegalActionException If one of the parameters has an
     *  illegal value.
     */
    public void refresh() throws IllegalActionException {
	// FIXME what happens if I call this after construction?
        Enumeration parameters = _target.getAttributes();
        setTextWidth(20);
        while(parameters.hasMoreElements()) {
            Attribute attribute = (Attribute)parameters.nextElement();
            if(attribute instanceof Parameter) {
                Parameter param = 
                    (Parameter) attribute;
                addLine(param.getName(), param.getName(), 
                        param.getToken().stringValue());
                attachParameter(param, param.getName());
            }
        }
    }

    /** Return the object that this configurer is configuring.
     */
    public NamedObj getTarget() {
	return _target;
    }   

    NamedObj _target;
}
