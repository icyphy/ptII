/* A helper class for ptolemy.actor.lib.Scale

Copyright (c) 1997-2004 The Regents of the University of California.
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

package ptolemy.codegen.c.actor.lib;

import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;

//////////////////////////////////////////////////////////////////////////
////Scale

/**
   A helper class for ptolemy.actor.lib.Scale
   
   @author Gang Zhou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (eal)
   
*/
public class Scale extends CCodeGeneratorHelper 
             implements ActorCodeGenerator {

	/**
	 * 
	 */
	public Scale(ptolemy.actor.lib.Scale actor) {
		_actor = actor;		
	}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

	public void generateFireCode(CodeStream stream) {
        _addCode(_codeBlock, stream);

	}
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variable                ////

    protected String _codeBlock = "$ref(output) = $val(factor) * $ref(input); \n";
    
    protected ptolemy.actor.lib.Scale _actor;
    

}
