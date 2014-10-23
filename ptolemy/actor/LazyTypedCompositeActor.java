/* A TypedCompositeActor with Lazy evaluation

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.LazyComposite;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// LazyTypedCompositeActor

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
 &lt;group&gt;
 ... <i>MoML elements giving library contents</i> ...
 &lt;/group&gt;
 ?&gt;
 </pre>
 The processing instruction, which is enclosed in "&lt;?" and "?&gt;"
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

 <p>
 To convert a preexisting model, see ptolemy.moml.ConvertToLazy.

 @author Christopher Brooks and Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class LazyTypedCompositeActor extends TypedCompositeActor implements
LazyComposite {

    // FIXME: Have to do ports and relations.  Only done attributes and entities.

    /** Construct a library in the default workspace with no
     *  container and an empty string as its name. Add the library to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public LazyTypedCompositeActor() {
        super();
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is LazyTypedCompositeActor.
        // However, the parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be LazyTypedCompositeActor.
        setClassName("ptolemy.actor.LazyTypedCompositeActor");
    }

    /** Construct a library in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public LazyTypedCompositeActor(Workspace workspace) {
        super(workspace);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is LazyTypedCompositeActor.
        // However, the parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be LazyTypedCompositeActor.
        setClassName("ptolemy.actor.LazyTypedCompositeActor");
    }

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LazyTypedCompositeActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is LazyTypedCompositeActor.
        // However, the parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be LazyTypedCompositeActor.
        setClassName("ptolemy.actor.LazyTypedCompositeActor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the library into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there). If the library has not yet been
     *  populated, then the clone will also not have been populated.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the library contains
     *   level crossing transitions so that its connections cannot be cloned,
     *   or if one of the attributes cannot be cloned.
     *  @return A new LazyTypedCompositeActor.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        // To prevent populating during cloning, we set a flag.
        _cloning = true;

        try {
            LazyTypedCompositeActor result = (LazyTypedCompositeActor) super
                    .clone(workspace);

            // There may or may not be configure text, but it won't be the
            // same as what we are cloning (instantiating) from.
            result._base = null;
            result._configureDone = false;
            result._populating = false;
            result._configureSource = null;
            result._configureText = null;

            result._cloning = false;
            return result;
        } finally {
            _cloning = false;
        }
    }

    /** Specify the library contents by giving either a URL (the
     *  <i>source</i> argument), or by directly giving the MoML text
     *  (the <i>text</i> argument), or both.  The MoML is evaluated
     *  when the populate() method is called.  This occurs
     *  lazily, when there is a request for the contents of the library.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     */
    @Override
    public void configure(URL base, String source, String text) {
        _base = base;
        _configureSource = source;
        _configureText = text;
        _configureDone = false;
    }

    /** List the contained class definitions in the order they were added
     *  (using their setContainer() method).
     *  The returned list is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of entities.  This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of ComponentEntity objects.
     */
    @Override
    public List classDefinitionList() {
        populate();
        return super.classDefinitionList();
    }

    /** Return true if this object contains the specified object,
     *  directly or indirectly.  That is, return true if the specified
     *  object is contained by an object that this contains, or by an
     *  object contained by an object contained by this, etc.
     *  This method ignores whether the entities report that they are
     *  atomic (see CompositeEntity), and always returns false if the entities
     *  are not in the same workspace.
     *  This method is read-synchronized on the workspace.
     *  @param inside The NamedObj that is searched for.
     *  @see ptolemy.kernel.CompositeEntity#isAtomic()
     *  @return True if this contains the argument, directly or indirectly.
     */
    @Override
    public boolean deepContains(NamedObj inside) {
        // If this has not yet been populated, then it can't possibly contain
        // the proposed object because the proposed object would not
        // exist yet.  Therefore, we do not need to populate.
        // Note that this makes this method override completely
        // unnecessary, but we keep it here anyway to record the fact.
        // Note that if we do call populate here, then the library
        // will be populated whenever setContainer() is called on this
        // library, which means that deferred evaluation will not ever
        // actually be deferred.
        // populate();
        return super.deepContains(inside);
    }

    /** List the opaque entities that are directly or indirectly
     *  contained by this entity.  The list will be empty if there
     *  are no such contained entities.  This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return A list of opaque ComponentEntity objects.
     */
    @Override
    public List deepEntityList() {
        populate();
        return super.deepEntityList();
    }

    /** Return a set with the relations that are directly or indirectly
     *  contained by this entity.  The set will be empty if there
     *  are no such contained relations. This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return A set of ComponentRelation objects.
     */
    @Override
    public Set<ComponentRelation> deepRelationSet() {
        populate();
        return super.deepRelationSet();
    }

    /** List the opaque entities that are directly or indirectly
     *  contained by this entity.  The list will be empty if there
     *  are no such contained entities. This list does not include
     *  class definitions nor anything contained by them.
     *  This method is read-synchronized on the workspace.
     *  @return A list of opaque ComponentEntity objects.
     *  @see #classDefinitionList()
     *  @see #allAtomicEntityList()
     */
    @Override
    public List deepOpaqueEntityList() {
        populate();
        return super.deepOpaqueEntityList();
    }

    /** List the contained entities in the order they were added
     *  (using their setContainer() method).
     *  The returned list is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of entities.  This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of ComponentEntity objects.
     */
    @Override
    public List entityList() {
        populate();
        return super.entityList();
    }

    /** Return a list of the component entities contained by this object that
     *  are instances of the specified Java class.  If there are no such
     *  instances, then return an empty list. The returned list does not
     *  include class definitions.
     *  This method is read-synchronized on the workspace.
     *  @param filter The class of ComponentEntity of interest.
     *  @return A list of instances of specified class.
     *  @see #classDefinitionList()
     */
    @Override
    public List entityList(Class filter) {
        populate();
        return super.entityList(filter);
    }

    /** Write a MoML description of this object with the specified
     *  indentation depth and with the specified name substituting
     *  for the name of this object.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        populate();
        super.exportMoML(output, depth, name);
    }

    /** Get a contained entity by name. The name may be compound,
     *  with fields separated by periods, in which case the entity
     *  returned is contained by a (deeply) contained entity.
     *  This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired entity.
     *  @return An entity with the specified name, or null if none exists.
     */
    @Override
    public ComponentEntity getEntity(String name) {
        populate();
        return super.getEntity(name);
    }

    /** Return the input source that was specified the last time the configure
     *  method was called.
     *  @return The string representation of the input URL, or null if the
     *  no source has been used to configure this object, or null if no
     *  external source need be used to configure this object.
     */
    @Override
    public String getConfigureSource() {
        return _configureSource;
    }

    /** Return the text string that represents the current configuration of
     *  this object. This will include whatever classes, entities, and
     *  relations have been previously instantiated. FIXME: Shouldn't this
     *  also include connections???
     *  @return A configuration string, or null if no configuration
     *  has been used to configure this object, or null if no
     *  configuration string need be used to configure this object.
     */
    @Override
    public String getConfigureText() {
        try {
            StringWriter stringWriter = new StringWriter();
            stringWriter.write("<group>\n");

            Iterator classes = lazyClassDefinitionList().iterator();

            while (classes.hasNext()) {
                ComponentEntity entity = (ComponentEntity) classes.next();
                entity.exportMoML(stringWriter, 1);
            }

            Iterator entities = lazyEntityList().iterator();

            while (entities.hasNext()) {
                ComponentEntity entity = (ComponentEntity) entities.next();
                entity.exportMoML(stringWriter, 1);
            }

            // FIXME: Include relations and links!

            stringWriter.write("</group>");
            return stringWriter.toString();
        } catch (IOException ex) {
            return "";
        }
    }

    /** Get a contained relation by name. The name may be compound,
     *  with fields separated by periods, in which case the relation
     *  returned is contained by a (deeply) contained entity.
     *  This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired relation.
     *  @return A relation with the specified name, or null if none exists.
     */
    @Override
    public ComponentRelation getRelation(String name) {
        populate();
        return super.getRelation(name);
    }

    /** Return a list that consists of all the atomic entities in a model
     *  that have been already instantiated.
     *  This method differs from {@link #deepEntityList()} in that
     *  this method looks inside opaque entities, whereas deepEntityList()
     *  does not. The returned list does not include any entities that
     *  are class definitions.
     *  @return a List of all atomic entities in the model.
     */
    @Override
    public List lazyAllAtomicEntityList() {
        LinkedList result = new LinkedList();
        // We don't use an Iterator here so that we can modify the list
        // rather than having both an Iterator and a result list.
        for (Object entity : lazyDeepEntityList()) {
            if (entity instanceof CompositeEntity) {
                result.addAll(((CompositeEntity) entity)
                        .lazyAllAtomicEntityList());
            } else {
                result.add(entity);
            }
        }
        return result;
    }

    /** Lazy version of {#link #allCompositeEntityList()}.
     *  In this base class, this is identical to allCompositeEntityList()
     *  but derived classes may omit from the returned list any class
     *  definitions whose instantiation is deferred.
     *  @return A list of ComponentEntity objects.
     */
    @Override
    public List lazyAllCompositeEntityList() {
        try {
            _workspace.getReadAccess();

            LinkedList result = new LinkedList();

            List<ComponentEntity> entities = lazyEntityList();
            for (ComponentEntity entity : entities) {
                if (!entity.isOpaque()) {
                    result.add(entity);
                    result.addAll(((CompositeEntity) entity)
                            .lazyAllCompositeEntityList());
                }
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Lazy version of {@link #classDefinitionList()}.
     *  In this base class, this is identical to classDefinitionList(),
     *  but derived classes may omit from the returned list any class
     *  definitions whose instantiation is deferred.
     *  @return A list of ComponentEntity objects.
     */
    @Override
    public List lazyClassDefinitionList() {
        return super.classDefinitionList();
    }

    /** Return an iterator over contained object that currently exist,
     *  omitting any objects that have not yet been instantiated because
     *  they are "lazy". A lazy object is one that is instantiated when it
     *  is needed, but not before. In this base class, this method returns
     *  the same iterator returned by {@link #containedObjectsIterator()}.
     *  If derived classes override it, they must guarantee that any omitted
     *  objects are genuinely not needed in whatever uses this method.
     *  @return An iterator over instances of NamedObj contained by this
     *   object.
     */
    @Override
    public Iterator lazyContainedObjectsIterator() {
        return new ContainedObjectsIterator();
    }

    /** Lazy version of {@link #deepEntityList()}.
     *  This method omits from the returned list any entities
     *  whose instantiation is deferred.
     *  @return A list of ComponentEntity objects.
     */
    @Override
    public List lazyDeepEntityList() {
        try {
            _workspace.getReadAccess();
            LinkedList result = new LinkedList();
            List<ComponentEntity> entities = lazyEntityList();
            for (ComponentEntity entity : entities) {
                if (!entity.isClassDefinition()) {
                    if (entity.isOpaque()) {
                        result.add(entity);
                    } else {
                        result.addAll(((CompositeEntity) entity)
                                .lazyDeepEntityList());
                    }
                }
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Lazy version of {@link #entityList()}.
     *  In this base class, this is identical to entityList(),
     *  but derived classes may omit from the returned list any
     *  entities whose instantiation is deferred.
     *  @return A list of ComponentEntity objects.
     */
    @Override
    public List lazyEntityList() {
        return super.entityList();
    }

    /** Lazy version of {@link #relationList()}.
     *  In this base class, this is identical to relationList(),
     *  but derived classes may omit from the returned list any
     *  relations whose instantiation is deferred.
     *  @return A list of ComponentEntity objects.
     */
    @Override
    public List lazyRelationList() {
        return super.relationList();
    }

    /** Create a new relation with the specified name, add it to the
     *  relation list, and return it. Derived classes can override
     *  this to create domain-specific subclasses of ComponentRelation.
     *  This method is write-synchronized on the workspace and increments
     *  its version number. This overrides the base class to force
     *  evaluation of any deferred MoML. This is necessary so that
     *  name collisions are detected deterministically and so that
     *  order of relations does not change depending on whether
     *  evaluation has occurred.
     *  @param name The name of the new relation.
     *  @return The new relation.
     *  @exception NameDuplicationException If name collides with a name
     *   already in the container.
     */
    @Override
    public ComponentRelation newRelation(String name)
            throws NameDuplicationException {
        populate();
        return super.newRelation(name);
    }

    /** Return the number of contained entities. This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return The number of entities.
     */
    @Override
    public int numberOfEntities() {
        populate();
        return super.numberOfEntities();
    }

    /** Return the number of contained relations.
     *  This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return The number of relations.
     */
    @Override
    public int numberOfRelations() {
        populate();
        return super.numberOfRelations();
    }

    /** Populate the actor by reading the file specified by the
     *  <i>source</i> parameter.  Note that the exception thrown here is
     *  a runtime exception, inappropriately.  This is because execution of
     *  this method is deferred to the last possible moment, and it is often
     *  evaluated in a context where a compile-time exception cannot be
     *  thrown.  Thus, extra care should be exercised to provide valid
     *  MoML specifications.
     *  @exception InvalidStateException If the source cannot be read, or if
     *   an exception is thrown parsing its MoML data.
     */
    @Override
    public void populate() throws InvalidStateException {
        boolean resetPolulatingValue = false;
        try {
            if (_populating) {
                resetPolulatingValue = true;
                return;
            }

            // Avoid populating during cloning.
            if (_cloning) {
                return;
            }

            // Do not populate if this is a derived object.
            // Instead, populate the object is this is derived
            // from, which will have the side effect of populating
            // this object.
            if (getDerivedLevel() != Integer.MAX_VALUE) {
                // Object is derived. Delegate to the most remote
                // prototype.
                List prototypes = getPrototypeList();
                if (prototypes == null || prototypes.size() == 0) {
                    throw new InternalErrorException(
                            getFullName()
                            + ": Object says it is derived but reports no prototypes!");
                }
                // The prototype must have the same class as this.
                LazyTypedCompositeActor prototype = (LazyTypedCompositeActor) prototypes
                        .get(prototypes.size() - 1);
                prototype.populate();
                return;
            }

            _populating = true;

            if (!_configureDone) {
                // NOTE: If you suspect this is being called prematurely,
                // the uncomment the following to see who is doing the
                // calling.
                // System.out.println("-----------------------");
                // (new Exception()).printStackTrace();
                // NOTE: Set this early to prevent repeated attempts to
                // evaluate if an exception occurs.  This way, it will
                // be possible to examine a partially populated entity.
                _configureDone = true;

                // NOTE: This does not seem like the right thing to do!
                // removeAllEntities();

                // If we have a subclass that has LazyTypedCompositeActor
                // in it, then things get tricky.  See
                // actor/lib/test/auto/LazySubClassModel.xml

                // If this is an instance or subclass of something, that
                // something must also be a LazyTypedCompositeActor and
                // it should be populated first.
                if (getParent() != null) {
                    ((LazyTypedCompositeActor) getParent()).populate();
                }

                // We used to temporarily set the MoMLFilters to null
                // for parsing here, but this fails when we are running
                // in a headless environment and we want to filter out
                // image icons.  To replicate this, run in a non-graphical
                // environment, such a remote Linux box and do:
                //   cd $PTII/ptolemy/actor/lib/colt/test;
                //   make JAVAFLAGS=-verbose test_auto
                // Formerly, we got:
                // "X connection to localhost:10.0 broken (explicit kill or server shutdown)."

                // We were getting ConcurrentModifications because
                // when we instantiate and call
                // NamedObj._markContentsDerived() we end up
                // eventually calling populate(), which calls
                // parse(URL, String, Reader) and adds a
                // ParserAttribute, which results in a
                // ConcurrentModificationException

                MoMLParser parser = new MoMLParser(workspace());

                // If we get the parser from ParserAttribute, then
                // after we call parse(), MoMLParser._xmlParser gets
                // set to null, which causes problems for the calling
                // parse() method.
                //NamedObj toplevel = toplevel();
                //MoMLParser parser = ParserAttribute.getParser(toplevel);

                parser.setContext(this);

                if (_configureSource != null && !_configureSource.equals("")) {
                    URL xmlFile = new URL(_base, _configureSource);
                    parser.parse(xmlFile, xmlFile);
                }

                if (_configureText != null && !_configureText.equals("")) {
                    // NOTE: Regrettably, the XML parser we are using cannot
                    // deal with having a single processing instruction at the
                    // outer level.  Thus, we have to strip it.
                    String trimmed = _configureText.trim();

                    if (trimmed.startsWith("<?") && trimmed.endsWith("?>")) {
                        trimmed = trimmed.substring(2, trimmed.length() - 2)
                                .trim();

                        if (trimmed.startsWith("moml")) {
                            trimmed = trimmed.substring(4).trim();
                            parser.parse(_base, trimmed);
                        }

                        // If it's not a moml processing instruction, ignore.
                    } else {
                        // Data is not enclosed in a processing instruction.
                        // Must have been given in a CDATA section.
                        parser.parse(_base, _configureText);

                        // Our work here is done, free this up.
                        _configureText = null;
                    }
                }
            }
        } catch (Exception ex) {
            // Oddly, under JDK1.3.1, we may see the line
            // "Exception occurred during event dispatching:"
            // in the console window, but there is no stack trace.
            // If we change this exception to a RuntimeException, then
            // the stack trace appears.  My guess is this indicates a
            // bug in the ptolemy.kernel.Exception* classes or in JDK1.3.1
            // Note that under JDK1.4, the stack trace is printed in
            // both cases.
            throw new InvalidStateException(this, ex,
                    "Failed to populate contents");
        } finally {
            _populating = resetPolulatingValue;
        }
    }

    /** List the relations contained by this entity.
     *  The returned list is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of relations. This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of ComponentRelation objects.
     */
    @Override
    public List relationList() {
        populate();
        return super.relationList();
    }

    /** Specify whether this object is a class definition.
     *  This method is write synchronized on the workspace.
     *  @param isClass True to make this object a class definition, false
     *   to make it an instance.
     *  @exception IllegalActionException If there are subclasses and/or
     *   instances and the argument is false.
     *  @see #isClassDefinition()
     *  @see ptolemy.kernel.util.Instantiable
     */
    @Override
    public void setClassDefinition(boolean isClass)
            throws IllegalActionException {
        try {
            // Get write access in case things change and
            // because super.setClassDefinition() gets write access.
            workspace().getWriteAccess();
            if (isClass) {
                setClassName("ptolemy.actor.TypedCompositeActor");
                populate();
            }
            super.setClassDefinition(isClass);
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return a name that is guaranteed to not be the name of
     *  any contained attribute, port, class, entity, or relation.
     *  In this implementation, the argument
     *  is stripped of any numeric suffix, and then a numeric suffix
     *  is appended and incremented until a name is found that does not
     *  conflict with a contained attribute, port, class, entity, or relation.
     *  If this composite entity or any composite entity that it contains
     *  defers its MoML definition (i.e., it is an instance of a class or
     *  a subclass), then the prefix gets appended with "_<i>n</i>_",
     *  where <i>n</i> is the depth of this deferral. That is, if the object
     *  deferred to also defers, then <i>n</i> is incremented.
     *  <p>Note that this method should be called judiciously from when
     *  the CompositeEntity is large.  The reason is that this method
     *  searches for matching attributes, ports, classes, entities
     *  and relations, which can result in slow performance.
     *  This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  @param prefix A prefix for the name.
     *  @return A unique name.
     */
    @Override
    public String uniqueName(String prefix) {
        populate();
        return super.uniqueName(prefix);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an entity or class definition to this container. This method
     *  should not be used directly.  Call the setContainer() method of
     *  the entity instead. This method does not set
     *  the container of the entity to point to this composite entity.
     *  It assumes that the entity is in the same workspace as this
     *  container, but does not check.  The caller should check.
     *  Derived classes may override this method to constrain the
     *  the entity to a subclass of ComponentEntity.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.  This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  This ensures that the entity being added now appears in order
     *  after the ones previously specified and lazily instantiated.
     *  @param entity Entity to contain.
     *  @exception IllegalActionException If the entity has no name, or the
     *   action would result in a recursive containment structure.
     *  @exception NameDuplicationException If the name collides with a name
     *  already in the entity.
     */
    @Override
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        populate();
        super._addEntity(entity);
    }

    /** Add a relation to this container. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead.
     *  This method does not set
     *  the container of the relation to refer to this container.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be. This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  This ensures that the relation being added now appears in order
     *  after the ones previously specified and lazily instantiated.
     *  @param relation Relation to contain.
     *  @exception IllegalActionException If the relation has no name.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained relations list.
     */
    @Override
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        populate();
        super._addRelation(relation);
    }

    /** Write a MoML description of the contents of this object, wrapped
     *  in a configure element.  This is done by first populating the model,
     *  and then exporting its contents into a configure element. This method
     *  is called by exportMoML().  Each description is indented according to
     *  the specified depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        populate();
        // Export top level attributes and ports

        List _attributes = attributeList();
        String _displayName = getDisplayName();

        //FIXME: start of duplicated code from NamedObj
        // If the display name has been set, then include a display element.
        // Note that copying parameters that have _displayName set need
        // to export _displayName.
        // See: http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3361
        if (!_displayName.equals(getName())) {
            output.write("<display name=\"");
            output.write(StringUtilities.escapeForXML(_displayName));
            output.write("\"/>");
        }

        // Callers of this method should hold read access
        // so as to avoid ConcurrentModificationException.
        if (_attributes != null) {
            //Iterator attributes = _attributes.elementList().iterator();
            Iterator attributes = _attributes.iterator();

            while (attributes.hasNext()) {
                Attribute attribute = (Attribute) attributes.next();
                attribute.exportMoML(output, depth);
            }
        }
        //FIXME: end of duplicated code from NamedObj

        //FIXME: start of duplicated code from Entity
        Iterator ports = portList().iterator();

        while (ports.hasNext()) {
            Port port = (Port) ports.next();
            port.exportMoML(output, depth);
        }
        //FIXME: end of duplicated code from Entity

        // Everything else is in a configure

        output.write(_getIndentPrefix(depth) + "<configure>\n");
        output.write(_getIndentPrefix(depth + 1) + "<group>\n");

        //FIXME: start of duplicated code from CompositeEntity
        Iterator classes = classDefinitionList().iterator();

        while (classes.hasNext()) {
            ComponentEntity entity = (ComponentEntity) classes.next();
            entity.exportMoML(output, depth + 2);
        }

        Iterator entities = entityList().iterator();

        while (entities.hasNext()) {
            ComponentEntity entity = (ComponentEntity) entities.next();
            entity.exportMoML(output, depth + 2);
        }

        Iterator relations = relationList().iterator();

        while (relations.hasNext()) {
            ComponentRelation relation = (ComponentRelation) relations.next();
            relation.exportMoML(output, depth + 2);
        }

        // NOTE: We used to write the links only if
        // this object did not defer to another
        // (getMoMLInfo().deferTo was null), and
        // would instead record links in a MoMLAttribute.
        // That mechanism was far too fragile.
        // EAL 3/10/04
        output.write(exportLinks(depth + 2, null));
        //FIXME: end of duplicated code from CompositeEntity

        output.write(_getIndentPrefix(depth + 1) + "</group>\n");
        output.write(_getIndentPrefix(depth) + "</configure>\n");

    }

    /** Remove the specified entity. This method should not be used
     *  directly.  Call the setContainer() method of the entity instead with
     *  a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be. This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  This ensures that the entity being removed now actually exists.
     *  @param entity The entity to remove.
     */
    @Override
    protected void _removeEntity(ComponentEntity entity) {
        populate();
        super._removeEntity(entity);
    }

    /** Remove the specified relation. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead with
     *  a null argument.
     *  The relation is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the relation in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be. This overrides the base class
     *  to first populate the actor, if necessary, by calling populate().
     *  This ensures that the relation being removed now actually exists.
     *  @param relation The relation to remove.
     */
    @Override
    protected void _removeRelation(ComponentRelation relation) {
        populate();
        super._removeRelation(relation);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Indicate whether data given by configure() has been processed. */
    protected boolean _configureDone = false;

    /** Indicator that we are in the midst of populating. */
    protected boolean _populating = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The base specified by the configure() method. */
    private URL _base;

    /** Indicate that we are cloning. */
    private boolean _cloning = false;

    /** URL specified to the configure() method. */
    private String _configureSource;

    /** Text specified to the configure() method. */
    private String _configureText;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class is an iterator over all the contained objects
     *  (all instances of NamedObj) except those that have not yet
     *  been instantiated because of laziness. In this class, the contained
     *  objects are attributes first, then ports, then entities,
     *  then relations.
     */
    protected class ContainedObjectsIterator extends
    Entity.ContainedObjectsIterator {
        /** Create an iterator over all the contained objects, which
         *  for CompositeEntities are attributes, ports, classes
         *  entities, and relations.
         */
        public ContainedObjectsIterator() {
            super();
            _classListIterator = lazyClassDefinitionList().iterator();
            _entityListIterator = lazyEntityList().iterator();
            _relationListIterator = lazyRelationList().iterator();
        }

        /** Return true if the iteration has more elements.
         *  In this class, this returns true if there are more
         *  attributes, ports, classes, entities, or relations.
         *  @return True if there are more elements.
         */
        @Override
        public boolean hasNext() {
            if (super.hasNext()) {
                return true;
            }
            if (_classListIterator.hasNext()) {
                return true;
            }
            if (_entityListIterator.hasNext()) {
                return true;
            }
            return _relationListIterator.hasNext();
        }

        /** Return the next element in the iteration.
         *  In this base class, this is the next attribute or port.
         *  @return The next attribute or port.
         */
        @Override
        public Object next() {
            if (super.hasNext()) {
                return super.next();
            }

            if (_classListIterator.hasNext()) {
                return _classListIterator.next();
            }

            if (_entityListIterator.hasNext()) {
                return _entityListIterator.next();
            }

            return _relationListIterator.next();
        }

        /** The remove() method is not supported because is is not
         *  supported in NamedObj.ContainedObjectsIterator.remove().
         */
        @Override
        public void remove() {
            super.remove();
        }

        private Iterator _classListIterator = null;

        private Iterator _entityListIterator = null;

        private Iterator _relationListIterator = null;
    }
}
