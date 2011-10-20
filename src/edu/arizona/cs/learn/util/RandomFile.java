package edu.arizona.cs.learn.util;

import java.io.File;
import java.io.FileNotFoundException;

public class RandomFile {
	public static File sequenceAlignmentFile() throws FileNotFoundException { 
		String pid = Utils.getPID();
		String threadName = Thread.currentThread().getName();
		File f = new File("/tmp/" + pid + "-" + threadName + "-sa.dat");
		return f;
	}
	
	public static String alignFile(String key) {
		String pid = Utils.getPID();
		String threadName = Thread.currentThread().getName();
		String s = "/tmp/" + pid + "-" + threadName + "-" + key + ".align";
		return s;
	}
}
