/* A hierarchical library of components specified in MoML.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.moml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// EntityLibrary
/**
This class provides a hierarchical library of components specified
in MoML.  The contents are typically specified via the configure()
method, which is passed MoML code.  The MoML is evaluated
lazily; i.e. it is not actually evaluated until there is a request
for its contents, via a call to getEntity(), numEntities(),
entityList(), or any related method. You can also force evaluation
of the MoML by calling populate().  When you export MoML for this
object, the MoML description of the contents is wrapped in a configure
element.  This object contains an attribute with name "_libraryMarker",
which marks it as a library.  This is used by the library browser in
vergil to know to expand the composite entity.
<p>
The contents of the library can be entities, ports, relations, or
attributes.  I.e., it can contain anything contained by a CompositeEntity.
An attempt to access any of these will trigger populating the library.
<p>
The configure method can be given a URL or MoML text or both.
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
<p>
A third subtlety involves the doc element.  Unfortunately, MoML
semantics define the doc element to replace any previously existing
doc elements.  But to find out whether there is any previously
existing doc element, the MoML parser calls getAttribute, which
has the effect of populating the library.  Thus, doc elements
should go inside the group, not outside.  For example, the
following organization results in no deferred evaluation:
<pre>
  &lt;entity name="director library" class="ptolemy.moml.EntityLibrary"&gt;
    &lt;doc&gt;default director library&lt;/doc&gt;
    &lt;configure&gt;
      &lt;?moml
        &lt;group&gt;
        ...
        &lt;/group&gt;
      ?&gt;
    &lt;/configure&gt;
  &lt;/entity&gt;
</pre>
The following, by contrast, is OK:
<pre>
  &lt;entity name="director library" class="ptolemy.moml.EntityLibrary"&gt;
    &lt;configure&gt;
      &lt;?moml
        &lt;group&gt;
        &lt;doc&gt;default director library&lt;/doc&gt;
        ...
        &lt;/group&gt;
      ?&gt;
    &lt;/configure&gt;
  &lt;/entity&gt;
</pre>

@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/

// FIXME: Have to do ports and relations.  Only done attributes and entities.

public class EntityLibrary
    extends CompositeEntity implements Configurable {

    /** Construct a library in the default workspace with no
     *  container and an empty string as its name. Add the library to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public EntityLibrary() {
        super();
        try {
            // NOTE: Used to call uniqueName() here to choose the name for the
            // marker.  This is a bad idea.  This calls getEntity(), which
            // triggers populate() on the library, defeating deferred
            // evaluation.
            new Attribute(this, "_libraryMarker");
        } catch (KernelException ex) {
            throw new InternalErrorException(null, ex, null);
        }
    }

    /** Construct a library in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public EntityLibrary(Workspace workspace) {
        super(workspace);
        try {
            // NOTE: Used to call uniqueName() here to choose the name for the
            // marker.  This is a bad idea.  This calls getEntity(), which
            // triggers populate() on the library, defeating deferred
            // evaluation.
            new Attribute(this, "_libraryMarker");
        } catch (KernelException ex) {
            throw new InternalErrorException(ex.toString());
        }
    }

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public EntityLibrary(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // NOTE: Used to call uniqueName() here to choose the name for the
        // marker.  This is a bad idea.  This calls getEntity(), which
        // triggers populate() on the library, defeating deferred
        // evaluation.
        new Attribute(this, "_libraryMarker");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list of the attributes contained by this object.
     *  If there are no attributes, return an empty list.
     *  This overrides the base class
     *  to first populate the library, if necessary, by calling populate().
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of instances of Attribute.
     */
    public List attributeList() {
        populate();
        return super.attributeList();
    }

    /** Return a list of the attributes contained by this object that
     *  are instances of the specified class.  If there are no such
     *  instances, then return an empty list.
     *  This overrides the base class
     *  to first populate the library, if necessary, by calling populate().
     *  This method is read-synchronized on the workspace.
     *  @param filter The class of attribute of interest.
     *  @return A list of instances of specified class.
     */
    public List attributeList(Class filter) {
        populate();
        return super.attributeList(filter);
    }

    /** Clone the library into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there). If the library has not yet been
     *  populated, then the clone will also not have been populated.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the library contains
     *   level crossing transitions so that its connections cannot be cloned,
     *   or if one of the attributes cannot be cloned.
     *  @return A new EntityLibrary.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        // To prevent populating during cloning, we set a flag.
        _cloning = true;
        try {
            EntityLibrary result = (EntityLibrary)super.clone(workspace);
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
    public void configure(URL base, String source, String text) {
        _base = base;
        _source = source;
        _text = text;
        _configureDone = false;
    }

    /** Return true if this object contains the specified object,
     *  directly or indirectly.  That is, return true if the specified
     *  object is contained by an object that this contains, or by an
     *  object contained by an object contained by this, etc.
     *  This method ignores whether the entities report that they are
     *  atomic (see CompositeEntity), and always returns false if the entities
     *  are not in the same workspace.
     *  This method is read-synchronized on the workspace.
     *  @see ptolemy.kernel.CompositeEntity#isAtomic()
     *  @return True if this contains the argument, directly or indirectly.
     */
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
     *  to first populate the library, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return A list of opaque ComponentEntity objects.
     */
    public List deepEntityList() {
        populate();
        return super.deepEntityList();
    }

    /** List the contained entities in the order they were added
     *  (using their setContainer() method).
     *  The returned list is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of entities.  This overrides the base class
     *  to first populate the library, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of ComponentEntity objects.
     */
    public List entityList() {
        populate();
        return super.entityList();
    }

    /** Get the attribute with the given name. The name may be compound,
     *  with fields separated by periods, in which case the attribute
     *  returned is contained by a (deeply) contained attribute.
     *  This overrides the base class
     *  to first populate the library, if necessary, by calling populate().
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired attribute.
     *  @return The requested attribute if it is found, null otherwise.
     */
    public Attribute getAttribute(String name) {
        populate();
        return super.getAttribute(name);
    }

    /** Get a contained entity by name. The name may be compound,
     *  with fields separated by periods, in which case the entity
     *  returned is contained by a (deeply) contained entity.
     *  This overrides the base class
     *  to first populate the library, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired entity.
     *  @return An entity with the specified name, or null if none exists.
     */
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
    public String getSource() {
        return _source;
    }

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be represented here
     *  as well.
     *  @return A configuration string, or null if no configuration
     *  has been used to configure this object, or null if no
     *  configuration string need be used to configure this object.
     */
    public String getText() {
        try {
            StringWriter stringWriter = new StringWriter();
            stringWriter.write("<group>\n");
            Iterator entities = entityList().iterator();
            while (entities.hasNext()) {
                ComponentEntity entity = (ComponentEntity)entities.next();
                entity.exportMoML(stringWriter, 1);
            }
            stringWriter.write("</group>");
            return stringWriter.toString();
        } catch (IOException ex) {
            return "";
        }
        //    return _text;
    }

    /** Return the number of contained entities. This overrides the base class
     *  to first populate the library, if necessary, by calling populate().
     *  Note that this may result in a runtime exception being thrown
     *  (if there is an error evaluating the MoML).
     *  This method is read-synchronized on the workspace.
     *  @return The number of entities.
     */
    public int numEntities() {
        populate();
        return super.numEntities();
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
    public void populate() throws InvalidStateException {
        try {
            if (_populating) return;

            // Avoid populating during cloning.
            if (_cloning) return;
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

                MoMLParser parser = new MoMLParser(workspace());

                parser.setContext(this);
                if (_source != null && !_source.equals("")) {
                    URL xmlFile = new URL(_base, _source);
                    InputStream stream = xmlFile.openStream();
                    parser.parse(xmlFile, stream);
                    stream.close();
                }
                if (_text != null && !_text.equals("")) {
                    // NOTE: Regrettably, the XML parser we are using cannot
                    // deal with having a single processing instruction at the
                    // outer level.  Thus, we have to strip it.
                    String trimmed = _text.trim();
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
                        parser.parse(_base, _text);
                    }
                }
            }
        } catch (Exception ex) {
            MessageHandler.error("Failed to populate library.", ex);
            // Oddly, under JDK1.3.1, we may see the line
            // "Exception occurred during event dispatching:"
            // in the console window, but there is no stack trace.
            // If we change this exception to a RuntimeException, then
            // the stack trace appears.  My guess is this indicates a
            // bug in the ptolemy.kernel.Exception* classes or in JDK1.3.1
            // Note that under JDK1.4, the stack trace is printed in
            // both cases.
            throw new InvalidStateException(this, ex,
                    "Failed to populate Library");
        } finally {
            _populating = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object, wrapped
     *  in a configure element.  This is done by first populating the model,
     *  and then exporting its contents into a configure element. This method
     *  is called by exportMoML().  Each description is indented according to
     *  the specified depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        output.write(_getIndentPrefix(depth) + "<configure>\n");
        output.write(_getIndentPrefix(depth+1) + "<group>\n");
        super._exportMoMLContents(output, depth+2);
        output.write(_getIndentPrefix(depth+1) + "</group>\n");
        output.write(_getIndentPrefix(depth) + "</configure>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The base specified by the configure() method. */
    private URL _base;

    /** Indicate that we are cloning. */
    private boolean _cloning = false;

    /** Indicate whether data given by configure() has been processed. */
    private boolean _configureDone = false;

    /** Indicator that we are in the midst of populating. */
    private boolean _populating = false;

    /** URL specified to the configure() method. */
    private String _source;

    /** Text specified to the configure() method. */
    private String _text;
}
