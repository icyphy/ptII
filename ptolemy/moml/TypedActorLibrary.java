/* A hierarchical library of components specified in MoML.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

import ptolemy.actor.Configurable;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// TypedActorLibrary
/**
A hierarchical library of components specified in MoML.  The contents are
specified via the configure() method.  The MoML is evaluated
lazily; i.e. it is not actually evaluated until there is a request
for its contents, via a call to getEntity(), numEntities(),
entityList(), or any related method. You can also force evaluation
of the MoML by calling populate().
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

@author Edward A. Lee
@version $Id$
*/

public class TypedActorLibrary
        extends TypedCompositeActor implements Configurable {

    /** Construct a library in the default workspace with no
     *  container and an empty string as its name. Add the library to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public TypedActorLibrary() {
        super();
    }

    /** Construct a library in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public TypedActorLibrary(Workspace workspace) {
	super(workspace);
    }

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TypedActorLibrary(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
            _populating = true;
            if (!_configureDone) {
                // NOTE: Set this early to prevent repeated attempts to
                // evaluate if an exception occurs.  This way, it will
                // be possible to examine a partially populated entity.
                _configureDone = true;
                removeAllEntities();
                if (_parser == null) {
                    _parser = new MoMLParser(workspace());
                }
                _parser.setContext(this);
                if (_source != null && !_source.equals("")) {
                    URL xmlFile = new URL(_base, _source);
                    InputStream stream = xmlFile.openStream();
                    _parser.parse(xmlFile, stream);
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
                            _parser.parse(_base, trimmed);
                        }
                        // If it's not a moml processing instruction, ignore.
                    } else {
                        // Data is not enclosed in a processing instruction.
                        // Must have been given in a CDATA section.
                        _parser.parse(_base, _text);
                    }
                }
            }
        } catch (Exception ex) {
            // FIXME: Printing stack trace to aid debugging.
            // Need a better mechanism here.
            System.err.println("Original error:\n" + ex);
            ex.printStackTrace();
            throw new InvalidStateException(this, ex.getMessage());
        } finally {
            _populating = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object, which
     *  in this class is the configuration information. This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        // NOTE: Do not call the super class because that will result
        // in the library contents being exported.
        String configElement = "<configure>";
        if (_source != null && !_source.equals("")) {
            configElement = "<configure source=\"" + _source + "\">";
        }
        if (_text == null) _text = "";
        output.write(_getIndentPrefix(depth)
                + configElement
                + _text
                + "</configure>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    /** The base specified by the configure() method. */
    private URL _base;

    /** Indicate whether data given by configure() has been processed. */
    private boolean _configureDone = false;

    /** The parser for parsing MoML. */
    private MoMLParser _parser;

    /** Indicator that we are in the midst of populating. */
    private boolean _populating = false;

    /** URL specified to the configure() method. */
    private String _source;

    /** Text specified to the configure() method. */
    private String _text;
}
