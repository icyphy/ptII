/* A GUI property that is associated with an action.

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
package ptolemy.actor.gui.properties;

import java.net.URL;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ActionGUIProperty

/**
 A GUI property that is associated with an action.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class ActionGUIProperty extends GUIProperty implements
        Configurable {

    /** Construct a GUI property with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ActionGUIProperty(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        parse = new Parameter(this, "parse");
        parse.setTypeEquals(BaseType.BOOLEAN);
        parse.setToken(BooleanToken.FALSE);

        _action = _createAction();
    }

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data.  The object should interpret the
     *  source first, if it is specified, followed by the literal text,
     *  if that is specified.  The new configuration should usually
     *  override any old configuration wherever possible, in order to
     *  ensure that the current state can be successfully retrieved.
     *  <p>
     *  This method is defined to throw a very general exception to allow
     *  classes that implement the interface to use whatever exceptions
     *  are appropriate.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If something goes wrong.
     */
    @Override
    public void configure(URL base, String source, String text)
            throws Exception {
        _action.configure(base, source, text);
    }

    /** Return the input source that was specified the last time the configure
     *  method was called.
     *  @return The string representation of the input URL, or null if the
     *  no source has been used to configure this object, or null if no
     *  external source need be used to configure this object.
     */
    @Override
    public String getConfigureSource() {
        return _action.getConfigureSource();
    }

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be represented here
     *  as well.
     *  @return A configuration string, or null if no configuration
     *  has been used to configure this object, or null if no
     *  configuration string need be used to configure this object.
     */
    @Override
    public String getConfigureText() {
        return _action.getConfigureText();
    }

    /** Perform this action. In this base class, if a source
     *  file is specified in the configuration of this item, e.g.:
     *  <pre>
     *    &lt;configure source="some_file.xml"&gt;
     *    &lt;/configure&gt;
     *  </pre>
     *  then the source is read and its contents are used as the moml text.
     *  The moml text can also be given directly:
     *  <pre>
     *    &lt;configure&gt;
     *      &lt;entity name="C" class="ptolemy.actor.lib.Const"&gt;
     *      &lt;/entity&gt;
     *    &lt;/configure&gt;
     *  </pre>
     *
     *  Depending on whether the {@link #parse} parameter is true or false,
     *  the moml text may be parsed first or not. If it is parsed, the
     *  returned NamedObj is used to generate a new moml string to be
     *  applied to the model in the current tableau (the nearest tableau
     *  that contains this GUI property). If it is not parsed, then the moml
     *  text is directly applied to the model.
     */
    public void perform() {
        if (parse == null) {
            return;
        }
        try {
            boolean parse = ((BooleanToken) this.parse.getToken())
                    .booleanValue();
            _action.perform(parse);
        } catch (Exception e) {
            throw new InternalErrorException(e);
        }
    }

    /** A Boolean parameter that determines whether the moml text should be
     *  parsed before applying to the current model in the {@link #perform()}
     *  method.
     */
    public Parameter parse;

    /** Create the action to be used in this property.
     *
     *  @return The action.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    protected GUIAction _createAction() throws IllegalActionException,
            NameDuplicationException {
        return new GUIAction(this, "_actionHandler");
    }

    /** The action.
     */
    protected GUIAction _action;
}
