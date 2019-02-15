package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import rs.raf.os.dir.File;
import rs.raf.os.fat.FAT16;

public class FAT16View extends JFrame {
	
	private FAT16 fat;
	private ArrayList<File> files;
	private JList filesList;
	
	public FAT16View(FAT16 fat, ArrayList<File> files) throws HeadlessException {
		super();
		this.fat = fat;
		this.files = files;
		
		setTitle("FAT16");
		setVisible(true);
		
		setSize(600, 500);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		makeView();
	}
	
	private void makeView() {
		JPanel jPanel = new JPanel();
		JLabel labela = new JLabel("List of files.");
		JButton[] buttons = new JButton[fat.getClusterCount() + 2];
		JButton btnShow = new JButton("Show");
		
		DefaultListModel<File> defaultListModel = new DefaultListModel();
		for (File f : files) {
			defaultListModel.addElement(f);
		}
		filesList = new JList<>(defaultListModel);
		filesList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		Box btnBox = Box.createHorizontalBox();
		btnBox.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		for (int i = 2; i < fat.getClusterCount() + 2; i++) {
			JButton cluster = new JButton(Integer.toString(fat.readCluster(i)));
			buttons[i] = cluster;
			buttons[i].setBackground(Color.WHITE);
			buttons[i].setFocusable(false);
			
			btnBox.add(cluster);
			btnBox.add(btnBox.createRigidArea(new Dimension(3, 0)));
			
		}
		
		Box hBox = Box.createHorizontalBox();
		hBox.setBorder(new EmptyBorder(10, 10, 10, 10));
		hBox.add(filesList);
		hBox.add(hBox.createRigidArea(new Dimension(20, 0)));
		hBox.add(btnShow);
		hBox.add(hBox.createRigidArea(new Dimension(20, 0)));
		hBox.add(btnBox);
		
		btnShow.addActionListener(e -> {
			if (filesList.getSelectedValue() == null) {
				JOptionPane.showMessageDialog(this,
					    "You must select file.",
					    "Error",
					    JOptionPane.WARNING_MESSAGE);
			} else {
				File file = (File) filesList.getSelectedValue();
				int currentCluster = file.getClusterStart();
				
				for (int i = 2; i < fat.getClusterCount() + 2; i++) {
					buttons[i].setBackground(Color.WHITE);
				}
				
				while (true) {
					
					buttons[currentCluster].setBackground(Color.CYAN);
					currentCluster = fat.readCluster(currentCluster);
					
					if (currentCluster == fat.getEndOfChain()) {
						break;
					}
				}
			}
		});
		
		hBox.setAlignmentX(CENTER_ALIGNMENT);
		jPanel.add(hBox);
		add(jPanel);   
		this.pack();
	}

}
