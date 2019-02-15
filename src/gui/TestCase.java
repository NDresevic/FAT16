package gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import rs.raf.os.dir.Directory;
import rs.raf.os.dir.File;
import rs.raf.os.disk.Disk;
import rs.raf.os.disk.SimpleDisk;
import rs.raf.os.fat.FAT16;
import rs.raf.os.test.MockDirectory;
import rs.raf.os.test.MockFAT;

public class TestCase {
	
	private FAT16 fat;
	private Disk disk;
	private Directory dir;
	
	public void testCase1() {
		//10 clusters, each two sectors width, can allocate 2000 bytes
		FAT16 fat = new MockFAT(2, 10);
		
		//sectors are 100 bytes, 6 of them on disk, for a total of 600 bytes
		Disk disk = new SimpleDisk(100, 6);
		
		Directory dir = new MockDirectory(fat, disk);
		
		//150 bytes of data, should take up one cluster, which is two sectors
		byte[] data = new byte[150];
		for(int i = 0; i < 150; i++) {
			data[i] = (byte)(i*2);
		}
		
		if (dir.writeFile("Even", data)) {
			byte[] readData = dir.readFile("Even");
		} else {
			fail("Could not write file");
		}
		
		new FAT16View(fat, dir.getFiles());
        new DiskView(disk);
		
		//assertEquals("[65528|0|0|0|0|0|0|0|0|0]", fat.getString());
	}
	
	public void testCase2() {
		fat = new MockFAT(1, 4);

        disk = new SimpleDisk(40, 10);

        dir = new MockDirectory(fat, disk);

        byte[] data = new byte[50];
        for(int i = 0; i < 50; i++) {
            data[i] = (byte)(i*2);
        }

        dir.writeFile("Even", data);
		
        new FAT16View(fat, dir.getFiles());
        new DiskView(disk);
        
        //assertEquals("[3|65528|0|0]", fat.getString());
	}
	
	public void testCase3() {
		
		//default FAT16 cluster count of 0xFFEF-2, cluster width is 1 sector
		FAT16 fat = new MockFAT(1, 20);
		
		//sectors of size 512 bytes, 2880 of them - totaling up to 1.44MB
		Disk disk = new SimpleDisk(40, 12);
		
		Directory dir = new MockDirectory(fat, disk);
		
		//800KB file
		byte[] largeFile1 = new byte[100];
		for(int i = 0; i < largeFile1.length; i++) {
			largeFile1[i] = 1;
		}
		
		//200KB file
		byte[] largeFile2 = new byte[150];
		for(int i = 0; i < largeFile2.length; i++) {
			largeFile2[i] = 2;
		}
		
		dir.writeFile("File1", largeFile1);
		
		dir.writeFile("File2", largeFile2);
		
		new FAT16View(fat, dir.getFiles());
        new DiskView(disk);
        
	}
	
	}