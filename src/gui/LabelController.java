package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LabelController implements ActionListener{

	private DiskView disk;
	private int sector;
	
	public LabelController(DiskView disk, int sector) {
		super();
		this.disk = disk;
		this.sector = sector;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		disk.getLabel().setText(disk.getPrintSector()[sector]);
		disk.getlSector().setText("Values on sector -> " + sector);
	} 
	
}
