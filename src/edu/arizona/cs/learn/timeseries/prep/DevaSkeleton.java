package edu.arizona.cs.learn.timeseries.prep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;


public class DevaSkeleton {

	private Map<Integer,Series> _map;
	
	public DevaSkeleton() { 
		
	}
	
	public void load(String file) throws Exception { 
		Map<Integer,Map<Integer,Double>> jxMap = new TreeMap<Integer,Map<Integer,Double>>();
		Map<Integer,Map<Integer,Double>> jyMap = new TreeMap<Integer,Map<Integer,Double>>();
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		// clear out the headers
		String headers = in.readLine();
		
		int maxTimeStep = Integer.MIN_VALUE;
		while (in.ready()) { 
			String line = in.readLine();
			String[] tokens = line.split("[,]");
				
			int timeStep = Integer.parseInt(tokens[0]);
			int jointIndex = Integer.parseInt(tokens[2]);

			maxTimeStep = Math.max(timeStep, maxTimeStep);

			double x = Double.parseDouble(tokens[3]);
			Map<Integer,Double> map = jxMap.get(jointIndex);
			if (map == null) { 
				map = new TreeMap<Integer,Double>();
				jxMap.put(jointIndex, map);
			}
			map.put(timeStep, x);
			
			double y = Double.parseDouble(tokens[4]);
			map = jyMap.get(jointIndex);
			if (map == null) { 
				map = new TreeMap<Integer,Double>();
				jyMap.put(jointIndex, map);
			}
			map.put(timeStep, y);
		}
		
		_map = new TreeMap<Integer,Series>();
		Set<Integer> joints = new TreeSet<Integer>();
		joints.addAll(jxMap.keySet());
		joints.addAll(jyMap.keySet());
		
		for (Integer joint : joints) { 
			Map<Integer,Double> x = jxMap.get(joint);
			Map<Integer,Double> y = jyMap.get(joint);
			
			Series series = new Series(maxTimeStep, x, y);
			_map.put(joint, series);
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
//		removeOutliers(_x);
//		removeOutliers(_y);
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
					
//					System.out.println("Changing the value " + i + " -- " + v + " -- " + ((n1+n2)/2));
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
//		if (_map == null)
//			load(input);
//
//		
//		Map<Integer,List<Double>> smoothX = smooth(_x);
//		Map<Integer,List<Double>> smoothY = smooth(_y);
//		Set<Integer> keys = new TreeSet<Integer>();
//		keys.addAll(smoothX.keySet());
//		keys.addAll(smoothY.keySet());
//		
//		BufferedWriter out = new BufferedWriter(new FileWriter(output));
//		out.write("joint,time,x,y,xdiff,ydiff,sx,sy\n");
//		for (Integer key : keys) { 
//			List<Double> x = _x.get(key);
//			List<Double> y = _y.get(key);
//			
//			List<Double> sx = smoothX.get(key);
//			List<Double> sy = smoothY.get(key);
//			
//			List<Double> xdiff = TimeSeries.diff(x);
//			List<Double> ydiff = TimeSeries.diff(y);
//
//			for (int i = 0; i < x.size(); ++i) { 
//				out.write(key + "," + i + "," + x.get(i) + "," + y.get(i) + ",");
//				
//				// now let's write out the diff when there are values.
//				if (!xdiff.get(i).equals(Double.NaN)) 
//					out.write(xdiff.get(i) + "");
//				out.write(",");
//
//				if (!ydiff.get(i).equals(Double.NaN)) 
//					out.write(ydiff.get(i) + "");
//				out.write(",");
//				
//				// now write out the smoothed values
//				if (!sx.get(i).equals(Double.NaN)) 
//					out.write(sx.get(i) + "");
//				out.write(",");
//				
//				if (!sy.get(i).equals(Double.NaN)) 
//					out.write(sy.get(i) + "");
//				out.write("\n");
//			}
//		}
//		
//		out.close();
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
	
	/**
	 * Prepare the skeletons generated from Deva for generation of 
	 * propositions.
	 */
	public void prepareTraining() { 		
		String[] verbs = { "carry", "catch", "collide", "dig", "drop", "fall", "hands", "hold", "jump",
				"kick", "pickup", "putdown", "raisehands", "run", "throw", "turn", "walk",
		};
		
		String skelPath = "/Users/wkerr/data/psi/skeletons/";
		String smoothPath = "/Users/wkerr/data/psi/smoothed/";
		
		for (String v : verbs) { 
			for (int i = 1; i < 32; ++i) { 
				try { 
					smooth(skelPath + v + i + "-skeletons.csv", smoothPath+v+i+".csv");
				} catch (Exception e) { 
					System.out.println("Missing file: " + v + "--" + i);
				}
			}
		}
	}
	
	/**
	 * Prepare the skeletons that we received from Deva's team and get them to 
	 * propositions.
	 */
	public void prepareTest() { 
		
	}
	
	public static void main(String[] args) throws Exception { 
		DevaSkeleton ds = new DevaSkeleton();
		ds.prepareTraining();
	}
}

class Series { 
	public double[] x;
	public double[] y;
	
	public Series(int numSteps, Map<Integer,Double> mx, Map<Integer,Double> my) { 
		x = new double[numSteps];
		y = new double[numSteps];
		
		for (int i = 0; i < numSteps; ++i) { 
			x[i] = Double.NaN;
			y[i] = Double.NaN;
		}
		
		for (Integer index : mx.keySet()) 
			x[index] = mx.get(index);
		
		for (Integer index : my.keySet())  
			y[index] = my.get(index);
		

		LoessInterpolator loess = new LoessInterpolator();
				
		for (int i = 0; i < numSteps; ++i) { 
			Double dx = new Double(x[i]);
			if (dx.equals(Double.NaN)) {
				
			}
			
			Double dy = new Double(y[i]);
			if (dy.equals(Double.NaN)) {
				
			}
		}
	}
}

