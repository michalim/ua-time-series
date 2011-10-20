package edu.arizona.cs.learn.timeseries.model;

import edu.arizona.cs.learn.timeseries.model.signature.Signature;

public class Score { 
	public String key;
	public double distance;
	
	public Signature signature;
	
	public String toString() { 
		return key + " - " + distance;
	}
}

