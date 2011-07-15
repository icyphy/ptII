/* Panel responsible for designating which actors/attributes are
   to be run remotely.
   
 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.homer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;
import ptolemy.vergil.toolbox.PtolemyTransferable;

///////////////////////////////////////////////////////////////////
//// RemoteObjectList

/** A list of the model components that will be run remotely.
 * 
 *  @author Justin Killian  
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class RemoteObjectList extends JPanel {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create the listing of remote objects.
     */
    public RemoteObjectList(UIDesignerFrame parent) {
        setLayout(new BorderLayout(0, 0));
        setBorder(new TitledBorder(null, "Remote Named Objects",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));

        _mainFrame = parent;
        _list.setVisibleRowCount(16);
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _list.setLayoutOrientation(JList.VERTICAL_WRAP);
        _list.setModel(_listModel);
        _list.setPreferredSize(new Dimension(200, 300));
        _list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(final MouseEvent mouseEvt) {
                        if (mouseEvt.getButton() == MouseEvent.BUTTON2) {
                            JMenuItem delete = new JMenuItem("Delete");
                            delete.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    _listModel.removeElement(mouseEvt
                                            .getComponent());
                                }
                            });

                            JPopupMenu menu = new JPopupMenu();
                            menu.add(delete);
                            menu.show(mouseEvt.getComponent(), mouseEvt.getX(),
                                    mouseEvt.getY());
                        }
                    }

                    public void mouseEntered(MouseEvent e) {
                        System.out.printf("Mouse entered %s label%n", e
                                .getComponent().getName());
                    }
                });

                try {
                    NamedObj object = (NamedObj) value;
                    List iconList = object.attributeList(EditorIcon.class);

                    if (iconList.size() == 0) {
                        label.setIcon(XMLIcon.getXMLIcon(object, "_icon")
                                .createIcon());
                        label.setText(object.getFullName());
                    } else {
                        label.setIcon(((EditorIcon) iconList.get(iconList
                                .size() - 1)).createIcon());
                        label.setText(object.getFullName());
                    }
                } catch (Exception e) {
                    // Do nothing.
                }

                return label;
            }
        });

        DropTarget target = new DropTarget(this, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent dropEvent) {
                if (dropEvent
                        .isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
                    try {
                        List<?> dropItems = (List) dropEvent.getTransferable()
                                .getTransferData(
                                        PtolemyTransferable.namedObjFlavor);
                        if (dropItems.size() > 0) {
                            _mainFrame
                                    .addNonVisualNamedObject((NamedObj) dropItems
                                            .get(0));
                        }
                    } catch (Exception e) {
                        MessageHandler.error(
                                "Can't find a supported data flavor for drop in "
                                        + dropEvent, e);
                    }

                    dropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                } else {
                    dropEvent.rejectDrop();
                }
            }
        });

        add(new JScrollPane(_list));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an item to the list of named objects.
     *  @param namedObj Object to be appended to the list.
     */
    public void addItem(NamedObj namedObj) {
        _listModel.addElement(namedObj);
    }

    /** Get the items in the list that have been designated as being remote.
     *  @return The contents of the list.
     */
    public NamedObj[] getItems() {
        return (NamedObj[]) _listModel.toArray();
    }

    /** Remove the item from the list of named objects.
     *  @param namedObj Object to be removed from the list.
     */
    public void removeItem(NamedObj namedObj) {
        _listModel.removeElement(namedObj);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The reference to the panel's container.
     */
    private UIDesignerFrame _mainFrame;

    /** The window list control that shows all remote items.
     */
    private final JList _list = new JList();

    /** The underlying list that powers the JList.
     */
    private final DefaultListModel _listModel = new DefaultListModel();
}
