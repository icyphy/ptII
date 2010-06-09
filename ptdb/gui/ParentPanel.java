package ptdb.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ptdb.kernel.bl.load.LoadManagerInterface;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
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

        _parentModelName = parentModelName;
        _configuration = configuration;

        _loadCheck = new JCheckBox();
        add(_loadCheck);

        _parentModelLink = new JButton("<html><u>" + _parentModelName
                + "</html></u>");
        add(_parentModelLink);

        ImageIcon icon = new ImageIcon("arrow.gif");
        JLabel arrowImage = new JLabel(icon);
        add(arrowImage);

        _parentModelLink.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                _loadModel();

            }

        });

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     * @return The parent model name.
     */
    public String getParentModelName() {

        return _parentModelName;

    }

    /** 
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
