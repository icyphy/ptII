/* A factory for making instances of classes for code generation in a 
   given domain.

 Copyright (c) 2000 The Regents of the University of California.
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

/** A factory for making instances of classes for code generation in a 
 *  given domain.
 *
 *  @author Jeff Tsay
 */
public class CodeGeneratorClassFactory {
    
    protected CodeGeneratorClassFactory() {}

    public ActorCodeGeneratorInfo createActorCodeGeneratorInfo() {
       return new ActorCodeGeneratorInfo();
    }

    public ActorTransformerVisitor createActorTransformerVisitor(
     ActorCodeGeneratorInfo actorInfo) {
       return new ActorTransformerVisitor(actorInfo,
        createPtolemyTypeVisitor(actorInfo));    
    }

    public PtolemyTypeVisitor createPtolemyTypeVisitor(ActorCodeGeneratorInfo actorInfo) {
       return new PtolemyTypeVisitor(actorInfo);
    }
    
    public static CodeGeneratorClassFactory getInstance() {
        if (_instance == null) {
           _instance = new CodeGeneratorClassFactory();
        }
        return _instance;
    }
    
    
    protected static CodeGeneratorClassFactory _instance = null;
}