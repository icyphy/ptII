/* RTMaude Code generator helper class for the Entity class.

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
 PROVIDED HEREUNDER IS ON AN AS IS BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.rtmaude.kernel;

import java.util.List;

import ptolemy.codegen.kernel.CodeStream;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Entity

/**
* Generate RTMaude code for a general entity in DE domain.
*
* @see ptolemy.kernel.Entity
* @author Kyungmin Bae
* @version $Id$
* @Pt.ProposedRating Red (kquine)
*
*/
public class Entity extends RTMaudeAdaptor {

    public Entity(ptolemy.kernel.Entity component) {
        super(component);
    }

    /**
     * Generate the fire code for an entity. In the Real-time Maude,
     * any entity of Ptolemy is translated to Object term
     *   < Name : ClassName | attr_1 : attr_value_1, ... , attr_n : attr_value_n >
     */
    protected String _generateFireCode() throws IllegalActionException { 
        
        return _generateBlockCode("fireBlock",
                CodeStream.indent(1,
                    new ListTerm<String>("", "," + _eol, 
                            _codeStream.getAllCodeBlockNames()) {
                        public String item(String v) throws IllegalActionException {
                            if (v.matches("attr_.*")) {
                                return _generateBlockCode(v);
                            }
                            else
                                return null;
                        }
                    }.generateCode())
            );
    }
    
    @Override
    protected String _generateInfoCode(String name, List<String> parameters)
            throws IllegalActionException {
        if (name.equals("ports"))
            return new ListTerm<Port>("none", "", 
                    ((ptolemy.kernel.Entity)getComponent()).portList()) {
                public String item(Port v) throws IllegalActionException {
                    return ((RTMaudeAdaptor) _getHelper(v)).generateTermCode();
                }
            }.generateCode();
        if (name.equals("parameters"))
            return new ListTerm<Variable>("emptyMap", " ;" + _eol, 
                    getComponent().attributeList(Variable.class)) {
                public String item(Variable v) throws IllegalActionException {
                    return ((RTMaudeAdaptor) _getHelper(v)).generateTermCode();
                }
            }.generateCode();
        return super._generateInfoCode(name, parameters);
    }
}
