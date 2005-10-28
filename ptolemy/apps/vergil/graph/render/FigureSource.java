/* An actor that creates figures.

Copyright (c) 1998-2005 The Regents of the University of California.
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

@ProposedRating Red (chf)
@AcceptedRating Red (chf)
*/
package ptolemy.apps.vergil.graph.render;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.*;
import ptolemy.apps.vergil.graph.util.FigureToken;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import diva.canvas.Figure;
import diva.canvas.toolbox.PaintedFigure;
import diva.canvas.toolbox.SVGParser;
import diva.util.java2d.*;
import diva.util.xml.*;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;


//////////////////////////////////////////////////////////////////////////
//// FigureSource

/**

@author Steve Neuendorffer
*/
public class FigureSource extends Source implements Configurable {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FigureSource(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setTypeEquals(FigureToken.TYPE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
    public void configure(URL base, String source, String text)
        throws Exception {
        _base = base;
        _source = source;
        _text = text;

        try {
            // FIXME: Do we need a base here?
            // FIXME: ignoring source.
            Reader in = new StringReader(text);
            XmlDocument document = new XmlDocument((URL) null);
            XmlReader reader = new XmlReader();
            reader.parse(document, in);

            XmlElement root = document.getRoot();
            _paintedList = SVGParser.createPaintedList(root);
        } catch (Exception ex) {
            // If we fail, then we'll just get a default figure.
            //    ex.printStackTrace();
            _paintedList = null;
        }
    }

    /** Output a new TokenFigure on the output.  The figure that the token
     *  references will be a new figure.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Figure figure = new PaintedFigure(_paintedList);
        Token token = new FigureToken(figure);
        output.send(0, token);
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
        return _text;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The base specified in the configure() method.
    private URL _base = null;

    // The list of painted objects contained in this icon.
    private PaintedList _paintedList;

    // The URL from which configure data is read.
    private String _source = null;

    // The text in the body of the configure.
    private String _text = null;
}
