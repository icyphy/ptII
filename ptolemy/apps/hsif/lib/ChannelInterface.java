/* A ChannelInterface is used for HSIF only.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.apps.hsif.lib;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedList;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ChannelInterface
/**
@author John S. Davis II, Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.kernel.Port
@see ptolemy.kernel.Relation
*/
public class ChannelInterface extends Attribute {

    /** Construct an entity in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public ChannelInterface() {
	super();
    }

    /** Construct an entity in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version tracking.
     */
    public ChannelInterface(Workspace workspace) {
	super(workspace);
    }

    /** Construct an entity in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version tracking.
     *  @param name The name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public ChannelInterface(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void setDst(String dst) {
        try {
            _workspace.getWriteAccess();
	    _dst = dst;
        } finally {
            _workspace.doneWriting();
        }
    }

    public void setSrc(String src) {
        try {
            _workspace.getWriteAccess();
	    _src = src;
        } finally {
            _workspace.doneWriting();
        }
    }

    public String getDst() {
        try {
            _workspace.getReadAccess();
            return _dst;
        } finally {
            _workspace.doneReading();
        }
    }

    public String getSrc() {
        try {
            _workspace.getReadAccess();
            return _src;
        } finally {
            _workspace.doneReading();
        }
    }

    public void setInput(boolean input) {
        _input = input;
	_output = !input;
    }

    public void setOutput(boolean output) {
        _input = !output;
	_output = output;
    }

    public String getType() {
        if (_input) return "input";
	if (_output) return "output";
	return "unknown";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

private String _dst = "";
private String _src = "";
private boolean _input = false;
private boolean _output = false;
}
