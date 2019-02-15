package rs.raf.os.dir;

public class File {
	
	private String name;
	private int clusterStart;
	private int fileSize;
	
	public File(String name, int clusterStart) {
		super();
		this.name = name;
		this.clusterStart = clusterStart;
	}
	
	public File(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getClusterStart() {
		return clusterStart;
	}

	public void setClusterStart(int clusterStart) {
		this.clusterStart = clusterStart;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public String toString() {
		return name;
	}
}
