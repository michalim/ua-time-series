package edu.arizona.cs.learn.timeseries.model;

public class Score { 
	public String key;
	public double distance;
	
	public Signature signature;
	
	public String toString() { 
		return key + " - " + distance;
	}
}

