/*          
An object that encapsulates a type policy in Ptolemy.

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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.codegen;

import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** An object that encapsulates a type policy in Ptolemy.
 *
 *  @author Jeff Tsay
 */ 
public class PtolemyTypePolicy extends TypePolicy {

    public PtolemyTypePolicy() {
        this(new PtolemyTypeIdentifier());
    }
    
    public PtolemyTypePolicy(PtolemyTypeIdentifier ptTypeID) {
        super(ptTypeID);
        _ptTypeID = ptTypeID;
    }
        
    public boolean isSubClassOfSupportedActor(TypeNode type) {
        if (type.classID() != TYPENAMENODE_ID) {
           return false;
        }
        
        ClassDecl decl = (ClassDecl) JavaDecl.getDecl((NamedNode) type);
        
        return isSubClassOfSupportedActor(decl);    
    }

    public boolean isSubClassOfSupportedActor(ClassDecl decl) {

        while (true) {
           if ((decl == StaticResolution.OBJECT_DECL) || (decl == null)) {
              return false;
           }
                
           int kind = _ptTypeID.kindOfClassDecl(decl);
           
           if (_ptTypeID.isSupportedActorKind(kind)) {
              return true;
           }
  
           decl = decl.getSuperClass();
        }
    }    
    
    protected final PtolemyTypeIdentifier _ptTypeID;              
}
