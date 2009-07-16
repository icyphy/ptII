/* A TypedCompositeActor with Lazy evaluation for Modular code generation. 

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
package ptolemy.cg.lib;

import ptolemy.actor.LazyTypedCompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
////ModularCodeGenTypedCompositeActor

/**
An aggregation of typed actors with lazy evaluation. The contents of
this actor can be created in the usual way via visual editor by dragging
in other actors and ports and connecting them. When it exports a MoML
description of itself, it describes its ports and parameters in the
usual way, but contained actors, relations, and their interconnections
are exported within &lt;configure&gt; &lt;/configure&gt; tags.
When reloading the MoML description, evaluation of the MoML
within the configure tags is deferred until there is an explicit
request for the contents. This behavior is useful for large
complicated models where the time it takes to instantiate the
entire model is large. It permits opening and browsing the model
without a long wait. However, the cost comes typically when
running the model. The instantiation time will be added to
the time it takes to preinitialize the model.
<p>
The lazy contents of this composite are specified via the configure()
method, which is called by the MoML parser and passed MoML code.
The MoML is evaluated lazily; i.e. it is not actually evaluated
until there is a request for its contents, via a call to
getEntity(), numEntities(), entityList(), relationList(),
or any related method. You can also force evaluation
of the MoML by calling populate(). Accessing the attributes
or ports of this composite does not trigger a populate() call,
so a visual editor can interact with the actor from the outside
in the usual way, enabling connections to its ports, editing
of its parameters, and rendering of its custom icon, if any.
<p>
The configure method can be passed a URL or MoML text or both.
If it is given MoML text, that text will normally be wrapped in a
processing instruction, as follows:
<pre>
&lt;?moml
<group>
... <i>MoML elements giving library contents</i> ...
</group>
?&gt;
</pre>
The processing instruction, which is enclosed in "&lt;?" and "?&gt"
prevents premature evaluation of the MoML.  The processing instruction
has a <i>target</i>, "moml", which specifies that it contains MoML code.
The keyword "moml" in the processing instruction must
be exactly as above, or the entire processing instruction will
be ignored.  The populate() method
strips off the processing instruction and evaluates the MoML elements.
The group element allows the library contents to be given as a set
of elements (the MoML parser requires that there always be a single
top-level element, which in this case will be the group element).
<p>
One subtlety in using this class arises because of a problem typical
of lazy evaluation.  A number of exceptions may be thrown because of
errors in the MoML code when the MoML code is evaluated.  However,
since that code is evaluated lazily, it is evaluated in a context
where these exceptions are not expected.  There is no completely
clean solution to this problem; our solution is to translate all
exceptions to runtime exceptions in the populate() method.
This method, therefore, violates the condition for using runtime
exceptions in that the condition that causes these exceptions to
be thrown is not a testable precondition.
<p>
A second subtlety involves cloning.  When this class is cloned,
if the configure MoML text has not yet been evaluated, then the clone
is created with the same (unevaluated) MoML text, rather than being
populated with the contents specified by that text.  If the object
is cloned after being populated, the clone will also be populated.
Cloning is used in actor-oriented classes to create subclasses
or instances of a class.  When a LazyTypedCompositeActor contained
by a subclass or an instance is populated, it delegates to the
instance in the class definition. When that instance is populated,
all of the derived instances in subclasses and instances of the
class will also be populated as a side effect.
<p>
A third subtlety is that parameters of this actor cannot refer to
contained entities or relations, nor to attributes contained by
those. This is a rather esoteric use of expressions, so
this limitation may not be onerous. You probably didn't know
you could do that anyway.  An attempt to make such references
will simply result in the expression failing to evaluate.


 @author Bert Rodiers
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */

public class ModularCodeGenTypedCompositeActor extends LazyTypedCompositeActor {

    /** Construct a library in the default workspace with no
     *  container and an empty string as its name. Add the library to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public ModularCodeGenTypedCompositeActor() {
        super();
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is ModularCodeGenTypedCompositeActor.
        // However, a parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be ModularCodeGenTypedCompositeActor.
        setClassName("ptolemy.actor.ModularCodeGenTypedCompositeActor");
    }

    /** Construct a library in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public ModularCodeGenTypedCompositeActor(Workspace workspace) {
        super(workspace);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is ModularCodeGenTypedCompositeActor.
        // However, a parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be ModularCodeGenTypedCompositeActor.
        setClassName("ptolemy.actor.ModularCodeGenTypedCompositeActor");
    }

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModularCodeGenTypedCompositeActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is ModularCodeGenTypedCompositeActor.
        // However, a parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be ModularCodeGenTypedCompositeActor.
        setClassName("ptolemy.actor.ModularCodeGenTypedCompositeActor");
    }

}
