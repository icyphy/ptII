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
import javax.swing.JPanel;

import ptdb.kernel.bl.load.LoadManagerInterface;
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
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 *
*/

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

        setLayout(new FlowLayout());

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

        ImageIcon icon = createImageIcon("$CLASSPATH/ptdb/gui/arrow.gif",
                "Contains");
        JLabel arrowImage = new JLabel(icon);
        arrowImage.setMaximumSize(getMinimumSize());
        add(arrowImage);

        _parentModelLink.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                _loadModel();

            }

        });

    }

    /** Based on code from
     * http://java.sun.com/docs/books/tutorial/uiswing/components/icon.html.
     *
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path - The path to the image file.
     *
     * @param description - A description of the image.
     *
     * @return An ImageIcon object if the path could be converted
     *          into a URL.  Otherwise, returns null.
     * */
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

            LoadManagerInterface loadManagerInterface = new LoadManagerInterface();

            PtolemyEffigy effigy = loadManagerInterface.loadModel(
                    _parentModelName, _configuration);

            effigy.showTableaux();

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
