/* Embedding of a Vert.x core.

   Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs;

import javax.script.ScriptContext;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// HelperBase

/**
   A base class for helper classes. The main function of this class is to
   provide a reference to the JavaScript actor for which the helper is helping.
   This is available in a protected method so that it isn't directly available
   in JavaScript. This actor should be used for all synchronized actions
   (to avoid deadlocks and race conditions).
   
   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class HelperBase {
    
    /** Construct a helper for the specified JavaScript object.
     *  @param currentObj The JavaScript object that this is helping.
     */
    public HelperBase(ScriptObjectMirror currentObj) {
	_currentObj = currentObj;
	
	Object actorOrWrapper = _currentObj.eval("actor");
	if (actorOrWrapper instanceof ScriptObjectMirror) {
	    actorOrWrapper = ScriptObjectMirror.unwrap(actorOrWrapper, ScriptContext.ENGINE_SCOPE);
	}
	if (actorOrWrapper instanceof RestrictedJavaScriptInterface) {
	    _actor = ((RestrictedJavaScriptInterface)actorOrWrapper)._getActor();
	} else if (actorOrWrapper instanceof JavaScript) {
	    _actor = ((JavaScript)actorOrWrapper);
	} else {
	    throw new InternalErrorException("Invalid actor object: " + actorOrWrapper.toString());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected fields                      ////
    
    /** The JavaScript actor that this is helping. All synchronization
     *  done by the helper should synchronize using this object as the monitor.
     */
    protected JavaScript _actor;
    
    /** The JavaScript object that this is a helper for. */
    protected ScriptObjectMirror _currentObj;
}
