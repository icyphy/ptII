/* An analysis for extracting the constructors of named objects.

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

package ptolemy.copernicus.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.kernel.util.*;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.CompleteUnitGraph;

//////////////////////////////////////////////////////////////////////////
//// NamedObjAnalysis
/**
An analysis that establishes a correspondence between each local
variable that refers to a named obj in a method an the named object
that it refers to.  This information is used to inline methods on
named obj locals.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class NamedObjAnalysis {
    public NamedObjAnalysis(SootMethod method, NamedObj thisBinding) {
        JimpleBody body = (JimpleBody)method.getActiveBody();
        _localToObject = new HashMap();
        _set(body.getThisLocal(), thisBinding);
        _notDone = true;
        while(_notDone) {
            _notDone = false;
            for(Iterator units = body.getUnits().iterator();
                units.hasNext();) {
                Unit unit = (Unit)units.next();
                if(unit instanceof DefinitionStmt) {
                    DefinitionStmt stmt = (DefinitionStmt)unit;
                    Value rightValue = (Value)stmt.getRightOp();
                  
                        if(stmt.getLeftOp() instanceof Local) {
                            Local local = (Local)stmt.getLeftOp();
                            if (rightValue instanceof Local) {
                                _update(local, (Local)rightValue);
                            } else if (rightValue instanceof CastExpr) {
                                _update(local, 
                                        (Local)((CastExpr)rightValue).getOp());
                            } else if (rightValue instanceof FieldRef) {
                                SootField field = ((FieldRef)rightValue).getField();
                                _set(local, _getFieldValueTag(field));
                            }
                        } else if(stmt.getLeftOp() instanceof FieldRef) {
                            if(rightValue instanceof Local) {
                                SootField field = 
                                    ((FieldRef)stmt.getLeftOp()).getField();
                                _set((Local)rightValue, _getFieldValueTag(field));
                            }
                        } else {
                            // Ignore..  probably not a named obj anyway.
                        
                    }
                }
            } 
        }
    }
    
    public NamedObj getObject(Local local) {
        Object current = _localToObject.get(local);
        if(current != null && 
                current.equals(_errorObject)) {
            throw new RuntimeException( 
                    "Could not determine the static value of "
                    + local);
        } else {
            return (NamedObj)current;
        }
    }
    
    public NamedObj _getFieldValueTag(SootField field) {
        if(field.getType() instanceof RefType &&
                SootUtilities.derivesFrom(
                        ((RefType)field.getType()).getSootClass(), 
                                    PtolemyUtilities.namedObjClass)) {
            ValueTag tag = (ValueTag)field.getTag("_CGValue");
            if (tag == null) {
                return _errorObject;
            } else {
                return (NamedObj)tag.getObject();
            }
        } else {
            return null;
        }
    }

    private void _update(Local local, Local toLocal) {
        _set(local, (NamedObj)_localToObject.get(toLocal));
        _set(toLocal, (NamedObj)_localToObject.get(local));
    }

    private void _set(Local local, NamedObj object) {
        Object current = _localToObject.get(local);
        if(object == null) {
            // No new information.
            return;
        } else if(current != null) {
            if(current.equals(_errorObject) ||
                    current.equals(object)) {
                return;
            } else {
                _localToObject.put(local, _errorObject);
                _notDone = true;
            }
        } else {
            // Current == null && object != null
            _localToObject.put(local, object);
            _notDone = true;
        }
    }

    private NamedObj _errorObject = new NamedObj();
    private Map _localToObject;
    private boolean _notDone;
}
