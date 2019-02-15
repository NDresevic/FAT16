package rs.raf.os.test;

import rs.raf.os.fat.FAT16;
import rs.raf.os.fat.FATException;

public class MockFAT implements FAT16 {
	
	private int clusterWidth;
	private int clusterCount;
	private int[] FATData;

	public MockFAT(int clusterWidth) {
		this.clusterWidth = clusterWidth;
		this.clusterCount = 0xFFED;
		this.FATData = new int[clusterCount + 2];
	}
	
	public MockFAT(int clusterWidth, int clusterCount) {
		if (clusterCount < 2 || clusterCount >= 0xFFED) {
			throw new FATException("Error: Trying to create FAT16 with cluster width " + clusterWidth + ".");
		}
		this.clusterWidth = clusterWidth;
		this.clusterCount = clusterCount;
		this.FATData = new int[clusterCount + 2];
	}
	
	@Override
	public int getEndOfChain() {
		return 0xFFF8;
	}

	@Override
	public int getClusterCount() {
		return clusterCount;
	}

	@Override
	public int getClusterWidth() {
		return clusterWidth;
	}

	@Override
	public int readCluster(int clusterID) throws FATException {
		if (clusterID < 2 || clusterID >= clusterCount + 2) {
			throw new FATException("Error: Trying to read invalid cluster ID " + clusterID + ".");
		}
		return FATData[clusterID];
	}

	@Override
	public void writeCluster(int clusterID, int valueToWrite) throws FATException {
		if (clusterID < 2 || clusterID >= clusterCount + 2) {
			throw new FATException("Error: Trying to write invalid cluster ID " + clusterID + ".");
		}
		FATData[clusterID] = valueToWrite;
	}

	@Override
	public String getString() {
		String FATString = "[";
		for (int i = 2; i < FATData.length; i++) {
			FATString += FATData[i] + "|";
		}
		FATString = FATString.substring(0, FATString.length() - 1) + "]";
		return FATString;
	}

	public int[] getFATData() {
		return FATData;
	}

}