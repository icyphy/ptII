/* A Dialog box for editing PTMLObject parameters

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

package ptolemy.schematic.editor;

import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.schematic.util.*;
import ptolemy.schematic.xml.*;
import ptolemy.gui.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*; 
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// ParameterQuery
/** This query contains parameters from the given target.  
 *  When a change is made in the query, the target's parameters 
 *  are updated.
 * @author Steve Neuendorffer 
 * @version $Id$
 */

public class ParameterQuery extends Query implements QueryListener {
    public ParameterQuery(NamedObj target) 
        throws IllegalActionException {
        _target = target;
        Enumeration parameters = target.getAttributes();
        setTextWidth(20);
        while(parameters.hasMoreElements()) {
            Attribute attribute = (Attribute)parameters.nextElement();
            if(attribute instanceof Parameter) {
                Parameter param = 
                    (Parameter) attribute;
                addLine(param.getName(), param.getName(), 
                        param.getToken().stringValue());
            }
        }
        addQueryListener(this);
    }
    
    /** Called to notify that one of the entries has changed.
     *  The name of the entry is passed as an argument.
     *  @param name The name of the entry.
     */
    public void changed(String name) {
        String value = stringValue(name);
	System.out.println("name=" + name + ", value=" + value);
        Attribute attribute = _target.getAttribute(name);
        if(attribute instanceof Parameter) {
            Parameter param = (Parameter) attribute;
	    System.out.println("parem = " + param);
            param.setExpression(value);
        }
    }
    Query _query;
    NamedObj _target;
}
