package edu.arizona.cs.learn.timeseries.prep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;


public class DevaSkeleton {

	private Map<Integer,List<Double>> _x;
	private Map<Integer,List<Double>> _y;
	
	public DevaSkeleton() { 
		
	}
	
	public void load(String file) throws Exception { 
		_x = new TreeMap<Integer,List<Double>>();
		_y = new TreeMap<Integer,List<Double>>();

		BufferedReader in = new BufferedReader(new FileReader(file));
		
		// clear out the headers
		String headers = in.readLine();
		
		while (in.ready()) { 
			String line = in.readLine();
			String[] tokens = line.split("[,]");
				
			int timeStep = Integer.parseInt(tokens[0]);
			int jointIndex = Integer.parseInt(tokens[2]);
			
			List<Double> x = _x.get(jointIndex);
			List<Double> y = _y.get(jointIndex);
			if (x == null || y == null) { 
				x = new ArrayList<Double>();
				y = new ArrayList<Double>();
				
				_x.put(jointIndex, x);
				_y.put(jointIndex, y);
			}
			
			x.add(Double.parseDouble(tokens[3]));
			y.add(Double.parseDouble(tokens[4]));
		}
	}
	
	/**
	 * The assumption is that the file has already been loaded.
	 */
	private void removeOutliers() { 
		// idea --- iterate over all of the x values and compute the 
		// median/mean and standard deviation.
		// anything far from the mean in terms of standard deviations should
		// be replaced with an average of it's neighbors.
		removeOutliers(_x);
		removeOutliers(_y);
	}
	
	/**
	 * @param map
	 */
	private void removeOutliers(Map<Integer,List<Double>> map) { 
		for (int key : map.keySet()) { 
			List<Double> values = map.get(key);
			List<Double> diff = TimeSeries.diff(values);
			SummaryStatistics ss = new SummaryStatistics();
			for (double d : diff) {
				ss.addValue(d);
			}
			double mean = ss.getMean();
			double stdev = ss.getStandardDeviation();
			
			for (int i = 1; i < values.size()-1; ++i) {
				double d = diff.get(i);
				double v = values.get(i);
				
				if (d > 30 || d < -30) {
					double n1 = values.get(i-1);
					double n2 = values.get(i+1);
					
					System.out.println("Changing the value " + i + " -- " + v + " -- " + ((n1+n2)/2));
					values.set(i, (n1+n2)/2);
				}
			}
		}
	}

	
	/**
	 * Actually perform the smoothing.
	 * @param csvFile
	 */
	public void smooth(String input, String output) throws Exception { 
		if (_x == null || _y == null)
			load(input);
		removeOutliers();
		
		
		Map<Integer,List<Double>> smoothX = smooth(_x);
		Map<Integer,List<Double>> smoothY = smooth(_y);
		Set<Integer> keys = new TreeSet<Integer>();
		keys.addAll(smoothX.keySet());
		keys.addAll(smoothY.keySet());
		
		BufferedWriter out = new BufferedWriter(new FileWriter(output));
		out.write("joint,time,x,y,xdiff,ydiff,sx,sy\n");
		for (Integer key : keys) { 
			List<Double> x = _x.get(key);
			List<Double> y = _y.get(key);
			
			List<Double> sx = smoothX.get(key);
			List<Double> sy = smoothY.get(key);
			
			List<Double> xdiff = TimeSeries.diff(x);
			List<Double> ydiff = TimeSeries.diff(y);

			for (int i = 0; i < x.size(); ++i) { 
				out.write(key + "," + i + "," + x.get(i) + "," + y.get(i) + ",");
				
				// now let's write out the diff when there are values.
				if (!xdiff.get(i).equals(Double.NaN)) 
					out.write(xdiff.get(i) + "");
				out.write(",");

				if (!ydiff.get(i).equals(Double.NaN)) 
					out.write(ydiff.get(i) + "");
				out.write(",");
				
				// now write out the smoothed values
				if (!sx.get(i).equals(Double.NaN)) 
					out.write(sx.get(i) + "");
				out.write(",");
				
				if (!sy.get(i).equals(Double.NaN)) 
					out.write(sy.get(i) + "");
				out.write("\n");
			}
		}
		
		out.close();
	}
	
	private Map<Integer,List<Double>> smooth(Map<Integer,List<Double>> map) {
		Map<Integer,List<Double>> smoothMap = new TreeMap<Integer,List<Double>>();
		for (int jointIndex : map.keySet()) { 
			List<Double> original = map.get(jointIndex);
			List<Double> smoothed = TimeSeries.linearFilter(original, 3);
			
			smoothMap.put(jointIndex, smoothed);
		}
		return smoothMap;
	}
	
	public static void main(String[] args) throws Exception { 
		DevaSkeleton ds = new DevaSkeleton();
		ds.smooth("/Users/wkerr/data/psi/skeletons/carry1-skeletons.csv", "/Users/wkerr/data/psi/smoothed/carry1.csv");
	}
}
