/*

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.util;

import java.util.*;

import ptolemy.graph.*;

import ptolemy.actor.IOPort;

import ptolemy.kernel.Entity;
import ptolemy.kernel.ComponentEntity;

//////////////////////////////////////////////////////////////////////////
//// 
/**

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class ModelGraph extends DirectedGraph {

    public ModelGraph(ComponentEntity entity) {
        super();
        _entity = entity;
        _inputPortNodes = new Vector();
        _outputPortNodes = new Vector();
    }

    public ComponentEntity getEntity() { return _entity; }

    public Node addIOPortNode(IOPort port) {
        Node n = addNodeWeight(port);
        if (port.isInput())
            _inputPortNodes.add(n);
        else
            _outputPortNodes.add(n);
        return n;
    }


    protected ComponentEntity _entity;
    protected Collection _inputPortNodes;
    protected Collection _outputPortNodes;

}
