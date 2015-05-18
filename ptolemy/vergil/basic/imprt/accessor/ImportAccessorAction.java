/* Implement the Import Accessor menu choice.

   Copyright (c) 2014-2015 The Regents of the University of California.
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
   COPYRIGHTENDKEY 2
 */

package ptolemy.vergil.basic.imprt.accessor;

import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;

import org.json.JSONArray;
import org.terraswarm.accessor.JSAccessor;

import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.gui.Top;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// ImportAccessorAction

/**
   An Action to import an Internet of Things (IoT) accessor.
   
   <p>This action presents a dialog box that permits specifying a URL
   or local directory.  If the URL or local directory contains a file
   named index.json, then it presents a list of the options given in
   that file, which it assumes are all accessors.  To manually specify
   a particular accessor on the local file system, you can browse to
   its directory, but then you have to manually type in the file name
   of the accessor.</p>

   <p>This package is optional.  To add the "Import Accessor" menu choice
   to the GraphEditor, add the following to the configuration:</p>
   <pre>
   &lt;property name="_importActionClassNames"
   class="ptolemy.data.expr.Parameter"
   value="{&quot;ptolemy.vergil.basic.imprt.accessor.ImportAccessorAction&quot;}"/&gt;
   </pre>
   <p>{@link ptolemy.vergil.basic.BasicGraphFrame} checks for this
   parameter and adds the "Import Accessor" menu choice if the class named
   by that parameter exists.</p>

   <p>The <code>$PTII/ptolemy/configs/defaultFullConfiguration.xml</code> file
   already has this parameter.  The ptiny configuration does <b>not</b> have
   this parameter so that we have a smaller download.</p>

   @author  Christopher Brooks, Patricia Derler.
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class ImportAccessorAction extends AbstractAction {

    // This package is called "imprt" because "import" is a Java keyword.

    /** Create a new action to import an accessor.
     *  The initial default value for last location is
     *  "http://www.terraswarm.org/accessors".
     * @param frame The Top that gets the menu.
     */
    public ImportAccessorAction(Top frame) {
        super("Import Accessor");
        if (!(frame instanceof BasicGraphFrame)) {
            throw new InternalErrorException("Frame " + _frame
                    + " is not a BasicGraphFrame?");
        }
        _frame = (BasicGraphFrame) frame;
        _lastLocation = "http://www.terraswarm.org/accessors";
        putValue("tooltip", "Import an accessor");
        // putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_E));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Import an accessor.
     *  @param event The Action Event.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        final Query query = new Query();
        query.setTextWidth(60);
        query.addFileChooser("location", "location", _lastLocation, null, null,
                true, true);
        // query.addLine("location", "location", _lastLocation);
        final JComboBox box = query.addChoice("accessor", "accessor",
                new String[] {}, _lastAccessorName);
        _updateComboBox(box, query);
        query.addQueryListener(new QueryListener() {
            @Override
            public void changed(String name) {
                if (name.equals("location")) {
                    _updateComboBox(box, query);
                }
            }
        });
        ComponentDialog dialog = new ComponentDialog(_frame,
                "Import Accessor", query);

        if (dialog.buttonPressed().equals("OK")) {
            // Get the associated Ptolemy model.
            GraphController controller = _frame.getJGraph().getGraphPane()
                    .getGraphController();
            AbstractBasicGraphModel model = (AbstractBasicGraphModel) controller
                    .getGraphModel();
            NamedObj context = model.getPtolemyModel();

            // Use the center of the screen as a location.
            Rectangle2D bounds = _frame.getVisibleCanvasRectangle();
            final double x = bounds.getWidth() / 2.0;
            final double y = bounds.getHeight() / 2.0;

            final String urlSpec = _lastLocation + query.getStringValue("accessor");

            // Do not add any code that modifies the MoML here,
            // instead edit org/terraswarm/accessor/JSAccessor.java so
            // that we can properly test the code.
            String moml = "";
            try {
               moml = JSAccessor.accessorToMoML(urlSpec);
            } catch (Throwable throwable) {
                MessageHandler.error("Failed to import accessor \""
                        + urlSpec + "\".", throwable);
                return;
            }
            
            JSAccessor.handleAccessorMoMLChangeRequest(this,
                    urlSpec, context, moml, x, y);
        }
    }

    ////////////////////////////////////////////////////////////
    ////          private methods                           ////

    private void _updateComboBox(JComboBox box, Query query) {
        box.removeAllItems();
        URL url;
        BufferedReader in;
        try {
            _lastLocation = query.getStringValue("location");
            if (_lastLocation.endsWith(".xml")) {
                return;
            } else if (!_lastLocation.endsWith("/")) {
                _lastLocation = _lastLocation + "/";
            }
            String index = _lastLocation + "index.json";
            url = FileUtilities.nameToURL(index, null, null);

            in = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuffer buffer = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                buffer.append(inputLine);
            }
            in.close();

            JSONArray array = new JSONArray(buffer.toString());
            for (int i = 0; i < array.length(); i++) {
                box.addItem(array.get(i));
            }
        } catch (Exception e) {
            // Don't provide a list of options, but issue a message.
            MessageHandler.error("Cannot suggest accessors, because there is no index.json file at "
                    + _lastLocation, e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private variables                      ////
    
    /** The top-level window of the contents to be exported. */
    BasicGraphFrame _frame;

    /** The most recent accessor. */
    private String _lastAccessorName;

    /** The most recent location. */
    private String _lastLocation;
}
