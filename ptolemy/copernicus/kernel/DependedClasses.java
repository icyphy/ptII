/* An analysis for detecting objects that must be aliased to each other.

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

package ptolemy.copernicus.kernel;

import soot.*;
import soot.jimple.*;
import soot.util.queue.*;
import java.util.*;

/** Collect all the classes that a set of classes depends on.  This includes:
 *  Any superclass.
 *  Any interface.
 *  The declaring class of any field or method referenced from the class.
 * 
 * @author Steve Neuendorffer
 */
public class DependedClasses { 

    /** Create a new set of classes that contains all of the classes that are
     *  required to load the given set of initial classes.
     */
    public DependedClasses(Collection initialClasses) {
        _reachableClasses = new LinkedList();
        _unprocessedClasses = new ChunkedQueue();
        Iterator classes = _unprocessedClasses.reader();

        _addClasses(initialClasses);
        
        while(classes.hasNext()) {
            SootClass nextClass = (SootClass)classes.next();
            _processClass(nextClass);
        }           
    }

    public List list() {
        return Collections.unmodifiableList(_reachableClasses);
    }
    
    private void _addClasses(Collection set) {
        for(Iterator i = set.iterator(); i.hasNext();) {
            _addClass((SootClass)i.next());
        }
    }

    private void _addClass(SootClass theClass) {
        if(!_reachableClasses.contains(theClass) && 
                !theClass.getName().startsWith("java")) {
            // System.out.println("adding class " + theClass);
            _reachableClasses.add(theClass);
            if(!theClass.isInterface()) {
                _unprocessedClasses.add(theClass);
            }
        }
    }

    private void _processClass(SootClass theClass) {
        //  System.out.println("processing class " + theClass);
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();

        // Make the method bodies analyzeable.
        if(!theClass.isApplicationClass()) {
            theClass.setLibraryClass();
        }

        _addClasses(hierarchy.getSuperclassesOfIncluding(theClass));
        // FIXME: what about super interfaces
        _addClasses(theClass.getInterfaces());
      
        // Grab the types of all fields.
        for(Iterator fields = theClass.getFields().iterator();
            fields.hasNext();) {
            SootField field = (SootField)fields.next();
            Type type = field.getType();
            if(type instanceof RefType) {
                _addClass(((RefType)type).getSootClass());
            }
        }

        for (Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod) methods.next();
            
            //   System.out.println("processing method = " + method);
            // Grab the classes of all arguments.
            for(Iterator types = method.getParameterTypes().iterator(); 
                types.hasNext();) {
                Type type = (Type)types.next();
                if(type instanceof RefType) {
                    _addClass(((RefType)type).getSootClass());
                }
            }
            
            // Grab the method return types.
            {
                Type type = method.getReturnType();
                if(type instanceof RefType) {
                    _addClass(((RefType)type).getSootClass());
                }
            } 
            
            // Don't drag in the bodies of abstract methods.
            if(!method.isConcrete()) {
                continue;
            }

            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            // Grab the types of all traps.
            for(Iterator it = body.getTraps().iterator();
                it.hasNext();) {
                Trap t = (Trap)it.next();
                _addClass(t.getException());
            }

            // Grab the classes of all referenced fields, invoked
            // methods, and created classes.
            for (Iterator units = body.getUnits().iterator();
                 units.hasNext();) {
                Unit unit = (Unit)units.next();
                for (Iterator boxes = unit.getUseAndDefBoxes().iterator();
                     boxes.hasNext();) {
                    ValueBox box = (ValueBox)boxes.next();
                    Value value = box.getValue();
                    if(value instanceof FieldRef) {
                        SootField field = ((FieldRef)value).getField();
                        SootClass refClass = field.getDeclaringClass();
                        if(!refClass.equals(theClass)) {
                            _addClass(refClass);
                        }
                    } else if(value instanceof InvokeExpr) {
                        SootMethod refMethod = ((InvokeExpr)value).getMethod();
                        SootClass refClass = refMethod.getDeclaringClass();
                        if(!refClass.equals(theClass)) {
                            _addClass(refClass);
                        }
                    } else if(value instanceof NewExpr) {
                        SootClass refClass = 
                            ((NewExpr)value).getBaseType().getSootClass();
                        if(!refClass.equals(theClass)) {
                            _addClass(refClass);
                        }
                    }
                }
            }
        }
    }

    private ChunkedQueue _unprocessedClasses;
    private List _reachableClasses;
}


