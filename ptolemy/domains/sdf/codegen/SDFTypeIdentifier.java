/* A class containing declarations created by the compiler of
   of known fields and methods in the ptolemy.actor and ptolemy.data
   packages.

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

package ptolemy.domains.sdf.codegen;

import ptolemy.codegen.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A class containing declarations created by the compiler of
 *  of known fields and methods in the ptolemy.actor and ptolemy.data
 *  packages.
 *
 *  @author Jeff Tsay
 */
public class SDFTypeIdentifier extends PtolemyTypeIdentifier {

    public SDFTypeIdentifier() {
        super();
    }

    /** Return true iff the kind is a class kind. In derived classes, the
     *  kind() may return a different number for special classes, so this
     *  method checks if the kind is any class kind.
     */
    public boolean isClassKind(int kind) {
        return ((kind == TYPE_KIND_SDF_ATOMIC_ACTOR) ||
                (kind == TYPE_KIND_SDF_IO_PORT) ||
                super.isClassKind(kind));
    }

    public boolean isSupportedActorKind(int kind) {
        return  ((kind == TYPE_KIND_SDF_ATOMIC_ACTOR) ||
                super.isSupportedActorKind(kind));
    }

    public boolean isSupportedPortKind(int kind) {
        return  ((kind == TYPE_KIND_SDF_IO_PORT) ||
                super.isSupportedPortKind(kind));
    }

    /** Return an integer representing the user type that has the specified ClassDecl,
     *  which may be a special type in Ptolemy. If the type is not a special type,
     *  return the integer given by super.kindOfTypeNameNode(classDecl.getDefType()).
     */
    public int kindOfClassDecl(ClassDecl classDecl) {
        if (classDecl == SDF_ATOMIC_ACTOR_DECL) {
            return TYPE_KIND_SDF_ATOMIC_ACTOR;
        } if (classDecl == SDF_IO_PORT_DECL) {
            return TYPE_KIND_SDF_IO_PORT;
        }
        return super.kindOfClassDecl(classDecl);
    }

    /** Return a new TypeNameNode that corresponds to the type indicated by
     *  the kind. The TypeNameNode must be reallocated so that later operations
     *  on the node do not affect TypeNameNode stored in this class.
     */
    public TypeNameNode typeNodeForKind(int kind) {
        if (kind == TYPE_KIND_SDF_ATOMIC_ACTOR) {
            return (TypeNameNode) SDF_ATOMIC_ACTOR_TYPE.clone();
        } else if (kind == TYPE_KIND_SDF_IO_PORT) {
            return (TypeNameNode) SDF_IO_PORT_TYPE.clone();
        }
        return super.typeNodeForKind(kind);
    }

    // additional actor kind
    public static final int TYPE_KIND_SDF_ATOMIC_ACTOR = TYPE_KIND_TYPED_IO_PORT + 1;

    // additional port kind
    public static final int TYPE_KIND_SDF_IO_PORT = TYPE_KIND_SDF_ATOMIC_ACTOR + 1;

    // additional actor type
    public static final ClassDecl SDF_ATOMIC_ACTOR_DECL;
    public static final TypeNameNode SDF_ATOMIC_ACTOR_TYPE;

    // additional port type
    public static final ClassDecl SDF_IO_PORT_DECL;
    public static final TypeNameNode SDF_IO_PORT_TYPE;

    static {

        CompileUnitNode sdfAtomicActorUnit = StaticResolution.load(
                SearchPath.NAMED_PATH.openSource(
                        "ptolemy.domains.sdf.kernel.SDFAtomicActor", true), 1);

        SDF_ATOMIC_ACTOR_DECL = (ClassDecl) StaticResolution.findDecl(
                sdfAtomicActorUnit, "SDFAtomicActor", CG_CLASS);

        SDF_ATOMIC_ACTOR_TYPE = SDF_ATOMIC_ACTOR_DECL.getDefType();

        CompileUnitNode sdfIOPortUnit = StaticResolution.load(
                SearchPath.NAMED_PATH.openSource("ptolemy.domains.sdf.kernel.SDFIOPort", true), 1);

        SDF_IO_PORT_DECL = (ClassDecl) StaticResolution.findDecl(
                sdfIOPortUnit,  "SDFIOPort", CG_CLASS);

        SDF_IO_PORT_TYPE = SDF_IO_PORT_DECL.getDefType();
    }
}
