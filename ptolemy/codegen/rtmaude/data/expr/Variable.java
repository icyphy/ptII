/* RTMaude Code generator helper class for the Variable class.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.data.expr;

import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////PropertyParameter

/**
* Generate RTMaude code for a Variable in DE domain.
*
* @see ptolemy.data.expr.Variable
* @author Kyungmin Bae
* @version $Id: Variable.java 53821 2009-04-12 19:12:45Z cxh $
* @Pt.ProposedRating Red (kquine)
*
*/
public class Variable extends RTMaudeAdaptor {

    public Variable(ptolemy.data.expr.Variable component) {
        super(component);
    }
    
    @Override
    public String generateTermCode() throws IllegalActionException {
        ptolemy.data.expr.Variable v = (ptolemy.data.expr.Variable) getComponent();
        String ret;
        
        if(v.getElementName() != null && v.getExpression().trim().length() > 0)
            ret = _generateBlockCode("valBlock", v.getName());
        else
            ret = "nil";
        return _generateBlockCode(this.defaultTermBlock, v.getName(), ret);
    }
}
