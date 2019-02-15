package rs.raf.os.test;

import java.util.ArrayList;
import rs.raf.os.dir.Directory;
import rs.raf.os.dir.DirectoryException;
import rs.raf.os.dir.File;
import rs.raf.os.disk.Disk;
import rs.raf.os.fat.FAT16;

public class MockDirectory implements Directory {

	private FAT16 fat;
	private Disk disk;
	private ArrayList<File> files;

	public MockDirectory(FAT16 fat, Disk disk) {
		this.fat = fat;
		this.disk = disk;
		this.files = new ArrayList<>();
	}
	
	@Override
	public boolean writeFile(String name, byte[] data) {
		
		int clusterStart = -1;
		int previousCluster = -1;
		byte[] part = new byte[disk.getSectorSize()];
		File file;
		// broj klastera potrebnih za upis
		int clusterCount = (int) Math.ceil(data.length / (fat.getClusterWidth() * disk.getSectorSize())) + 1;
		int indexData = 0;
		int k = 0;
		// da li se prepisuje preko vec postojece
		boolean overwrite = false;
		boolean smallerOverwrite = false;
		byte[] smallerData = new byte[data.length];
		ArrayList<Integer> sectorsToWrite = new ArrayList<>();
		
		// fajl postoji
		for (File f : files) {
			if (f.getName().equals(name)) {
				clusterStart = f.getClusterStart();
				file = f;
				overwrite = true;
				
				// fajl je manji, upisuje se novi na njegovo mesto i ostatak podataka se popunjava 0
				if (data.length < f.getFileSize()) {
					smallerData = new byte[f.getFileSize()];
					int countData = 0;
					for (int i = 0; i < smallerData.length; i++) {
						if (countData < data.length) {
							smallerData[i] = data[countData];
							countData++;
						} else {
							smallerData[i] = 0;
						}
					}
					smallerOverwrite = true;
					file = new File(name, clusterStart);
					file.setFileSize(data.length);
				}
				// fajl je veci, proverava se da li moze da se upise
				else if (data.length > f.getFileSize()) {
					if (data.length > getUsableFreeSpace() - f.getFileSize()) {
						return false;
					}
					deleteFile(f.getName());
					file = new File(name, clusterStart);
					file.setFileSize(data.length);
				}
				
				break;
			}
		}
		
		// ako fajl ne postoji, pravimo novi i nalazimo pocetni klaster
		if (!overwrite) {
			// ako je velicina fajla veca
			if (data.length > getUsableFreeSpace()) {
				return false;
			}
			
			for (int i = 2; i < fat.getClusterCount() + 2; i++) {
				if (fat.readCluster(i) == 0) {
					clusterStart = i;
					break;
				}
			}
			
			file = new File(name, clusterStart);
			file.setFileSize(data.length);
			files.add(file);
		}
		
		// pisemo nov fajl, propunjavamo fat
		if (!overwrite) {
			boolean start = true;
			previousCluster = clusterStart;
			
			for (int i = 2; i < fat.getClusterCount() + 2; i++) {
				
				// nasli smo slobodan klaster
				if (fat.readCluster(i) == 0) {
					
					// ucitan samo pocetni
					if (start) {
						previousCluster = i;
						start = false;
						
						if (clusterCount == 1) {
							break;
						}
						
						continue;
					}
					
					clusterStart = i;
					fat.writeCluster(previousCluster, clusterStart);
					for (int j = 0; j < fat.getClusterWidth(); j++) {
						sectorsToWrite.add((previousCluster - 2) * fat.getClusterWidth() + j);
					}
					previousCluster = clusterStart;
					clusterCount--;
					
					if (clusterCount == 1) {
						break;
					}
				}
			}
			
			fat.writeCluster(previousCluster, fat.getEndOfChain());
			for (int i = 0; i < fat.getClusterWidth(); i++) {
				sectorsToWrite.add((previousCluster - 2) * fat.getClusterWidth() + i);
			}
			
		}
		// citamo sa fata koje sektore treba popuniti
		else {
			
			while (fat.readCluster(clusterStart) != fat.getEndOfChain()) {
				for (int i = 0; i < fat.getClusterWidth(); i++) {
					sectorsToWrite.add((clusterStart - 2) * fat.getClusterWidth() + i);
				}
				clusterStart = fat.readCluster(clusterStart);
			}
			
			for (int i = 0; i < fat.getClusterWidth(); i++) {
				sectorsToWrite.add((clusterStart - 2) * fat.getClusterWidth() + i);
			}
			
		}
		
		// upis podataka na odgovarajuci sektor ako je prepisivanje manjeg fajla
		if (smallerOverwrite) {
			indexData = 0;
			for (int i = 0; i < sectorsToWrite.size(); i++) {
				
				part = new byte[disk.getSectorSize()];
				for (k = 0; k < part.length; k++) {
					part[k] = smallerData[indexData++];
					if (k == smallerData.length || indexData == smallerData.length) {
						break;
					}
				}
				disk.writeSector(sectorsToWrite.get(i), part);
				
				if (k == data.length || indexData == data.length) {
					break;
				}
				
			}
			
			return true;
			
		}
		
		// upis podataka na odgovarajuci sektor
		for (int i = 0; i < sectorsToWrite.size(); i++) {
			
			part = new byte[disk.getSectorSize()];
			for (k = 0; k < part.length; k++) {
				part[k] = data[indexData++];
				if (k == data.length || indexData == data.length) {
					break;
				}
			}
			disk.writeSector(sectorsToWrite.get(i), part);
			
			if (k == data.length || indexData == data.length) {
				break;
			}
			
		}

		return true;
	}

	@Override
	public byte[] readFile(String name) throws DirectoryException {
		byte[] fileData;
		int indexFile = 0;
		int currentCuster = 0; 
		int currentSector = 0;
		
		for (File file : files) {
			if (file.getName().equals(name)) {
				fileData = new byte[file.getFileSize()];
				currentCuster = file.getClusterStart();
				
				while (true) {
					for (int i = 0; i < fat.getClusterWidth(); i++) {
						currentSector = (currentCuster - 2) * fat.getClusterWidth() + i;
						for (int j = 0; j < disk.getSectorSize(); j++) {
							fileData[indexFile++] = disk.readSector(currentSector)[j];
							
							if (indexFile == fileData.length) {
								return fileData;
							}
						}
					}
					
					currentCuster = fat.readCluster(currentCuster);
					
					if (currentCuster == fat.getEndOfChain()) {
						break;
					}
					
				}
			}
		}
		
		throw new DirectoryException("Error: Trying to read a file that doesn't exist -> " + name);
	}

	@Override
	public void deleteFile(String name) throws DirectoryException {
		for (File file : files) {
			if (file.getName().equals(name)) {
				files.remove(file);
				ArrayList<Integer> deleteCluster = new ArrayList<>();
				
				int i = file.getClusterStart();
				while (true) {
					deleteCluster.add(i);
					
					for (int j = 0; j < fat.getClusterWidth(); j++) {
						int sectorID = (i - 2) * fat.getClusterWidth() + j;
						byte[] sectorData = new byte[disk.getSectorSize()];
						disk.writeSector(sectorID, sectorData);
					}
					
					i = fat.readCluster(i);
					if (i == fat.getEndOfChain()) {
						break;
					}
				}
				
				for (i = 0; i < deleteCluster.size(); i++) {
					fat.writeCluster(deleteCluster.get(i), 0);
				}
				
				return;
			}
		}
		throw new DirectoryException("Error: Trying to delete a file that doesn't exist -> " + name);
	}

	@Override
	public String[] listFiles() {
		String[] filesList = new String[files.size()];
		int n = 0;
		for (File f : files) {
			filesList[n++] = f.toString();
		}
		return filesList;
	}

	@Override
	public int getUsableTotalSpace() {
		return Math.min(disk.diskSize(), fat.getClusterCount()*fat.getClusterWidth()*disk.getSectorSize());
	}

	@Override
	public int getUsableFreeSpace() {
		int freeDiskSpace = getUsableTotalSpace();
		for (int i = 2; i < fat.getClusterCount() + 2; i++) {
			if (fat.readCluster(i) != 0) {
				freeDiskSpace -= disk.getSectorSize() * fat.getClusterWidth();
			}
		}
		return freeDiskSpace;
	}

	public ArrayList<File> getFiles() {
		return files;
	}

}
