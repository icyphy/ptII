package ptolemy.vergil.debugger.MMI;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import ptolemy.kernel.util.*;

////////////////////////////////////////////////////////////////////////
//Class EditWatcher
//   Creates a window with a radiobutton list displaying the contollable
//elements on the place where the watcher has been selected. It returns
//an array with the chosen elements.
////////////////////////////////////////////////////////////////////////
public class WatcherEditor extends JFrame implements ListSelectionListener, ActionListener {

    private JRadioButton portRadioButton, paramRadioButton;
    private JList portList, paramList;
    private JButton button = null;
    private Box rightBox, leftBox;
    private JScrollPane portPane, paramPane;
    private JPanel contentPane;

    public WatcherEditor(ActorWatcher w) {
	super ("Edit Watcher");
	String _labelport[] = {"port1", "port2", "port3"};
	String _labelparam[] = {"param1", "param2"};
	NamedList portNamedList = new NamedList();
	NamedList paramNamedList = new NamedList();
	try {
	    portNamedList.append((Nameable)new NamedObj("port1"));
	    portNamedList.append((Nameable)new NamedObj("port2"));
	    portNamedList.append((Nameable)new NamedObj("port3"));
	    paramNamedList.append((Nameable)new NamedObj("param1"));
	    paramNamedList.append((Nameable)new NamedObj("param2"));
	    //	int[] selectedPorts, selectedParam;
	} catch (IllegalActionException ex) {
	} catch (NameDuplicationException ex) {}

	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}
	    });	 
	
	portRadioButton.setSelected(true);  //Port JRadioButton selected by default
	portList = new JList(_labelport);  // Port list shown by default

	////////////////////////////////////////////////////
	//
	//	portList.addListSelectionListener(this);
	//
	////////////////////////////////////////////////////

	portPane = new JScrollPane(portList);
	paramPane = new JScrollPane(paramList);
	//	    buttons = new JRadioButton[_label.length];
	
	rightBox = new Box(BoxLayout.Y_AXIS);
	leftBox = new Box(BoxLayout.Y_AXIS);
	
	portRadioButton = new JRadioButton();
	portRadioButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    leftBox.add(portPane);
		    contentPane.remove(leftBox);
		    contentPane.add(leftBox, BorderLayout.WEST);
		    setContentPane(contentPane);	
		}
	    });
	rightBox.add(portRadioButton);
	
	
	
	paramRadioButton = new JRadioButton();
	paramRadioButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    leftBox.add(paramPane);
		    contentPane.remove(leftBox);
		    contentPane.add(leftBox, BorderLayout.WEST);
		    setContentPane(contentPane);
		}
	    });

	rightBox.add(paramRadioButton);
	leftBox.add(portPane);  //Set by default
	
	JToolBar toolBar = new JToolBar();
	//OK button
	//For the moment it finds out what are the selected elements
	//and displays them on the scrollbar of the debugger interface
	button = new JButton("OK");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int selectedPorts[] = portList.getSelectedIndices();
		    for(int i = 0; i < selectedPorts.length; i++){
			String element = (String)portList.getModel().getElementAt(selectedPorts[i]);
			_mmi.displayResult("      " +element);
		    }	
		    int selectedParam[] = paramList.getSelectedIndices();
		    for(int i = 0; i < selectedParam.length; i++){
			String element = (String)paramList.getModel().getElementAt(selectedParam[i]);
			_mmi.displayResult("      " +element);
		    }	
		    WatcherEditor.this.dispose();
		}
	    });
	toolBar.add(button);
	
	//Cancel  button
	button = new JButton("Cancel");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    WatcherEditor.this.dispose();
		    _mmi.displayResult("       None");
		}
	    });
	toolBar.add(button);
	
	contentPane = new JPanel();
	contentPane.setLayout(new BorderLayout());
	contentPane.add(toolBar, BorderLayout.SOUTH);
	contentPane.add(rightBox, BorderLayout.EAST);
	contentPane.add(leftBox, BorderLayout.WEST);
	setContentPane(contentPane);
	
	}

    public void valueChanged (ListSelectionEvent e){};
    
    //    public void valueChanged (ListSelectionEvent e){
	//event ignored if
    //if((e.getValueIsAdjusting() == false) || 
    //   (e.getFirstIndex() == -1)) return;
    //for(int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
    //    buttons[i].setSelected(((JList)e.getSource()).isSelectedIndex(i));
    //}
    //}
	public void actionPerformed(ActionEvent e) {}
}
