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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import ptdb.common.dto.XMLDBModel;
import ptolemy.actor.gui.Configuration;

///////////////////////////////////////////////////////////////////
//// ParentHierarchyPanel

/**
 * An extended JPanel displaying a single branch of
 * parents for the search result.  A parent hierarchy
 * is one branch of parents that contain the model
 * matched by the model search.  The panel layout is
 * taken care of in the constructor.  A public method
 * getSelections() is available for getting the list
 * of parent model names for models that have been
 * selected for loading.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

@SuppressWarnings("serial")
public class ParentHierarchyPanel extends JPanel {

    /**
     * Construct a panel associated with a parent hierarchy.
     *
     * @param hierarchy
     *        The model returned as a search result.
     * @param searchResultModel
     *        The name of the model that was matched by the search.
     *        Displayed at the end of the hierarchy.
     * @param configuration
     *        The configuration under which an effigy of this model would be generated.
     */
    public ParentHierarchyPanel(List<XMLDBModel> hierarchy,
            String searchResultModel, Configuration configuration) {

        //setLayout(new BoxLayout(this, BoxLayout.X_AXIS));setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        setAlignmentX(LEFT_ALIGNMENT);
        Border border = BorderFactory.createLineBorder(Color.black);
        setBorder(border);

        _configuration = configuration;
        _parentList = new ArrayList();

        for (XMLDBModel parent : hierarchy) {

            ParentPanel panelToAdd;
            panelToAdd = new ParentPanel(parent.getModelName(), _configuration);
            panelToAdd.setAlignmentX(LEFT_ALIGNMENT);
            _parentList.add(panelToAdd);
            add(panelToAdd);

        }

        JLabel labelResultModel = new JLabel(searchResultModel);
        add(labelResultModel);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Traverse the list of ParentPanels.  If a ParentPanel's checkbox is
     * checked, add the model name to the returned list of model names (strings).
     *
     * @return A list of parent models that have been selected for loading.
     */
    public ArrayList<String> getSelections() {

        ArrayList<String> returnList = new ArrayList();

        for (ParentPanel panel : _parentList) {

            if (panel.getValue()) {

                returnList.add(panel.getParentModelName());

            }

        }

        return returnList;
    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private ArrayList<ParentPanel> _parentList;
    private Configuration _configuration;

}
