package ptolemy.actor.lib.jjs.modules.contextAware;


import java.util.ArrayList;
import java.util.List;


import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;



public class ContextAwareGUI  {

	//All of the components 

	public JButton btnSearch;

	public JList<String> _list;
	public List<JTextField> textFields;
	public List<JLabel> labels;
	public DefaultListModel<String> listModel;
	GroupLayout groupLayout;



	public ContextAwareGUI(String[] list) {
		System.out.println(list.length);
		JFrame f = new JFrame("title");
		JPanel p = new JPanel();
		textFields = new ArrayList<JTextField>();
		labels = new ArrayList<JLabel>();
		for(int i=0; i<= list.length; i++ ) {
			labels.add(new JLabel());
			textFields.add(new JTextField(10));
			labels.get(i).setVisible(false);
			textFields.get(i).setVisible(false);
		}
		btnSearch = new JButton("Search");
		listModel = new DefaultListModel<String>();
		_list = new JList<String>(list);
		groupLayout = new GroupLayout(p);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addComponent(_list, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(207)
										.addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE))
										.addGroup(groupLayout.createSequentialGroup()
												.addGap(18)
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
														.addGroup(groupLayout.createSequentialGroup()
																.addComponent(labels.get(5))
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(textFields.get(5)))
																.addGroup(groupLayout.createSequentialGroup()
																		.addComponent(labels.get(4))
																		.addPreferredGap(ComponentPlacement.RELATED)
																		.addComponent(textFields.get(4)))
																		.addGroup(groupLayout.createSequentialGroup()
																				.addComponent(labels.get(3))
																				.addPreferredGap(ComponentPlacement.RELATED)
																				.addComponent(textFields.get(3)))
																				.addGroup(groupLayout.createSequentialGroup()
																						.addComponent(labels.get(2))
																						.addPreferredGap(ComponentPlacement.RELATED)
																						.addComponent(textFields.get(2)))
																						.addGroup(groupLayout.createSequentialGroup()
																								.addComponent(labels.get(1))
																								.addPreferredGap(ComponentPlacement.RELATED)
																								.addComponent(textFields.get(1)))
																								.addGroup(groupLayout.createSequentialGroup()
																										.addComponent(labels.get(0))
																										.addPreferredGap(ComponentPlacement.RELATED)
																										.addComponent(textFields.get(0), GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)))
																										.addGap(9)))
																										.addGap(122))
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(_list, GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
						.addGap(24)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(textFields.get(0), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(labels.get(0)))
								.addGap(5)
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
										.addComponent(labels.get(1), GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
										.addComponent(textFields.get(1), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGap(5)
										.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
												.addComponent(labels.get(2), GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
												.addComponent(textFields.get(2), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addGap(5)
												.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
														.addComponent(labels.get(3), GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
														.addComponent(textFields.get(3), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
														.addGap(5)
														.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
																.addComponent(textFields.get(4), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																.addComponent(labels.get(4)))
																.addGap(5)
																.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
																		.addComponent(textFields.get(5), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(labels.get(5)))
																		.addPreferredGap(ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
																		.addComponent(btnSearch)
																		.addContainerGap())
				);

		groupLayout.setHonorsVisibility(true);
		p.setLayout(groupLayout);

		f.setSize(300,200);
		p.add(btnSearch);                       // add button to panel
		f.setContentPane(p);    // add panel to frame
		f.setVisible(true);
	}



}


