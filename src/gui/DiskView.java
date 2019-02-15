package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import rs.raf.os.disk.Disk;

public class DiskView extends JFrame {
	
	private Disk disk;
	private byte[] data;
	private String[] printSector;
	private JLabel label;
	private JLabel lSector;

	public DiskView(Disk disk) throws HeadlessException {
		super();
		this.disk = disk;
		
		setTitle("Disk");
		setVisible(true);
		
		setSize(600, 500);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		makeView();
	}
	
	private void makeView() {
		JPanel jPanel = new JPanel();
		label = new JLabel("");
		lSector = new JLabel("");
		JButton[] buttons = new JButton[disk.getSectorCount()];
		data = new byte[disk.getSectorSize()];
		printSector = new String[disk.getSectorCount()];
		
		Box hBox = Box.createHorizontalBox();
		hBox.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		Box labelsBox = Box.createVerticalBox();
		labelsBox.setBorder(new EmptyBorder(10, 10, 10, 10));
		labelsBox.add(lSector);
		labelsBox.add(labelsBox.createRigidArea(new Dimension(0, 10)));
		labelsBox.add(label);
		
		for (int i = 0; i < disk.getSectorCount(); i++) {
			JButton sector = new JButton(Integer.toString(i));
			buttons[i] = sector;
			buttons[i].setFocusable(false);
			buttons[i].setBackground(Color.WHITE);
			data = new byte[disk.getSectorSize()];
			printSector[i] = new String("[ ");
			
			for (int j = 0; j < disk.getSectorSize(); j++) {
				data[j] = disk.readSector(i)[j];
				printSector[i] += data[j] + " ";
			}
			printSector[i] += "]";
			
			for (byte b : disk.readSector(i)) {
				if (b != 0) {
					buttons[i].setBackground(Color.BLACK);
					buttons[i].setForeground(Color.WHITE);
					buttons[i].addActionListener(new LabelController(this, i));
					break;
				}
			}
			
			hBox.add(sector);
			hBox.add(hBox.createRigidArea(new Dimension(3, 0)));
		}
		
		jPanel.add(hBox);
		jPanel.add(labelsBox);
		add(jPanel);   
		this.pack();
		setSize(700, 500);
	}

	public JLabel getLabel() {
		return label;
	}

	public void setLabel(JLabel label) {
		this.label = label;
	}

	public String[] getPrintSector() {
		return printSector;
	}

	public void setPrintSector(String[] printSector) {
		this.printSector = printSector;
	}

	public JLabel getlSector() {
		return lSector;
	}

	public void setlSector(JLabel lSector) {
		this.lSector = lSector;
	}
	
}
