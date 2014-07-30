/*
@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptdb.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ptdb.kernel.bl.load.LoadManager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// ParentPanel

/**
 * An extended JPanel displaying a parent of a model result.
 * Parents are models somewhere in the database that include the search
 * result as a submodel. Multiple ParentPanel objects will be displayed as
 * a hierarchy beneath the ModelPanel which contains the search result model itself.
 * This class is simply for uniform layout and behavior.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 *
 */

@SuppressWarnings("serial")
public class ParentPanel extends JPanel {

    /**
     * Construct a panel associated with a parent model of the search result.
     * The panel layout is taken care of in the constructor.
     * A listener is added for _parentModelLink,
     * which is a button used to open the model in a new editing frame.
     *
     * @param parentModelName
     *        The parent model name.
     * @param configuration
     *        The configuration under which an effigy of the parent model
     *        would be generated.
     */
    public ParentPanel(String parentModelName, Configuration configuration) {

        setLayout(new FlowLayout(FlowLayout.LEADING));

        setAlignmentX(LEFT_ALIGNMENT);

        _parentModelName = parentModelName;
        _configuration = configuration;

        _loadCheck = new JCheckBox();
        _loadCheck.setMaximumSize(getMinimumSize());
        add(_loadCheck);

        _parentModelLink = new JButton("<html><u>" + _parentModelName
                + "</html></u>");
        _parentModelLink.setForeground(Color.BLUE);
        _parentModelLink.setMaximumSize(getMinimumSize());
        add(_parentModelLink);

        ImageIcon icon = createImageIcon("$CLASSPATH/ptdb/gui/img/arrow.gif",
                "Contains");
        JLabel arrowImage = new JLabel(icon);
        arrowImage.setMaximumSize(getMinimumSize());
        add(arrowImage);

        _parentModelLink.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                _loadModel();

            }

        });

    }

    /** Return an ImageIcon, or null if the path was invalid.
     * <p>Based on code from
     * http://download.oracle.com/javase/tutorial/uiswing/components/icon.html.
     *
     * Returns an ImageIcon, or null if the path was invalid.
     * @param path - The path to the image file.
     *
     * @param description - A description of the image.
     *
     * @return An ImageIcon object if the path could be converted
     *          into a URL.  Otherwise, returns null.
     *
     */
    protected ImageIcon createImageIcon(String path, String description) {

        URL imgURL;

        try {

            imgURL = FileUtilities.nameToURL(path, null, getClass()
                    .getClassLoader());

        } catch (Exception e) {

            imgURL = null;

        }

        if (imgURL != null) {

            return new ImageIcon(imgURL, description);

        } else {

            //System.err.println("Couldn't find file: " + path);
            return null;

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the parent model name.
     *
     * @return The parent model name.
     */
    public String getParentModelName() {

        return _parentModelName;

    }

    /** Get the value of the _loadCheck checkbox.
     *  The method, isSelected(), will return true or false.
     *
     * @return The value of the _loadCheck checkbox.
     */
    public Boolean getValue() {

        return _loadCheck.isSelected();

    }

    /**
     * Create an effigy of the parent model and open it in a new editing frame.
     */
    private void _loadModel() {

        try {

            PtolemyEffigy effigy = LoadManager.loadModel(_parentModelName,
                    _configuration);

            if (effigy != null) {

                effigy.showTableaux();

            } else {

                JOptionPane.showMessageDialog(this,
                        "The specified model could "
                                + "not be found in the database.",
                                "Load Error", JOptionPane.INFORMATION_MESSAGE, null);

            }

        } catch (Exception e) {

            MessageHandler.error("Cannot load the specified model. ", e);

        }

    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private JCheckBox _loadCheck;
    private JButton _parentModelLink;
    private String _parentModelName;
    private Configuration _configuration;

}
