/* An object that can create a tableau for a model.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.net.URL;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// TableauFactory

/**
 This class is an attribute that creates a tableau to view a specified effigy.
 When a model is opened, if the top-level of that model contains this
 attribute or a subclass, then that attribute handles constructing a tableau.
 Otherwise, the configuration specifies which tableau is used.
 A configuration contains an instance of this class, and uses it to create
 a tableau for a model represented by an effigy.  This base class assumes
 that it contains other tableau factories. Its createTableau() method defers
 to each contained factory, in the order in which they were added,
 until one is capable of creating a tableau for the specified effigy.
 Subclasses of this class will usually be inner classes of a Tableau,
 and will create the Tableau, or might themselves be aggregates of
 instances of TextEditorTableauFactory.
 <p>
 When there are multiple distinct TableauFactory classes that are capable
 of providing views on the same effigy, then instances of these
 factories should be aggregated into a single factory contained herein.
 Those instances can be presented as alternative views of the data when
 any single view is opened.
 <p>
 There is a significant subtlety with respect to how Ptolemy II classes
 are dealt with. Normally, when one looks inside an instance of a class,
 what is opened is the class definition, not the instance. However,
 if the instance contains an instance of TableauFactory, then what
 is opened is the instance, not the class definition.  This is used,
 for example, when the look inside behavior is customized on a per
 instance basis.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (celaine)
 @see Configuration
 @see Effigy
 @see Tableau
 */
public class TableauFactory extends Attribute implements Configurable {
    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TableauFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a tableau for the specified effigy. The tableau will be
     *  created with a new unique name with the specified effigy as its
     *  container.  If this factory cannot create a tableau
     *  for the given effigy (perhaps because the effigy is not of the
     *  appropriate subclass), then return null.  This base class assumes
     *  that it contains other tableau factories. This method defers
     *  to each contained factory in order until one is capable of creating a
     *  tableau for the specified effigy.  As with all attributes, the order
     *  is simply the order of creation.  Subclasses of this class will
     *  usually be inner classes of a Tableau, and will create the Tableau.
     *  A subclass that actually creates a tableau is responsible for setting
     *  the container of the tableau to the specified effigy, and for naming
     *  the tableau.
     *  Subclasses should not call show() in createTableau(), it is the
     *  responsibility of the caller to check the return value and
     *  call show() after doing things like adjusting the size.
     *  @param effigy The model effigy.
     *  @return A tableau for the effigy, or null if one cannot be created.
     *  @exception Exception If the factory should be able to create a
     *   Tableau for the effigy, but something goes wrong.
     */
    public Tableau createTableau(Effigy effigy) throws Exception {
        Tableau tableau = null;
        Iterator factories = attributeList(TableauFactory.class).iterator();

        while (factories.hasNext() && tableau == null) {
            TableauFactory factory = (TableauFactory) factories.next();
            tableau = factory.createTableau(effigy);
            if (tableau != null) {
                factory._configureTableau(tableau);
            }
        }

        return tableau;
    }

    /** Configure the tableau factory with data from the specified input source
     *  (a URL) and/or textual data.  The data is recorded locally without
     *  parsing.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If something goes wrong. No thrown in this class.
     */
    @Override
    public void configure(URL base, String source, String text)
            throws Exception {
        _configureSource = source;
        _configureText = text;
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
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be represented here
     *  as well.
     *  @return A configuration string, or null if no configuration
     *  has been used to configure this object, or null if no
     *  configuration string need be used to configure this object.
     */
    @Override
    public String getConfigureText() {
        return _configureText;
    }

    /** Configure the given tableau with the configuration data attached to this
     *  tableau factory, if any.
     *
     *  @param tableau The tableau to be configured.
     */
    protected void _configureTableau(final Tableau tableau) {
        if (_configureText != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            tableau, _configureText);
                    tableau.requestChange(request);
                }
            });
        }
    }

    /** The input source that was specified the last time the configure method
     *  was called.
     */
    private String _configureSource;

    /** The text string that represents the current configuration of this
     *  object.
     */
    private String _configureText;
}
