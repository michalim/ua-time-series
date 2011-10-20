package edu.arizona.cs.learn.timeseries.data.conversion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.util.DataMap;
import edu.arizona.cs.learn.util.Utils;

/**
 * This class converts the data from my Lisp based format into a format
 * supported by Fabian Morchen and TSKM
 * @author kerrw
 *
 */
public class ConvertDataset {
	
	private static void dumpFile(String file, Map<String,Integer> map) throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for (String key : map.keySet()) { 
			out.write(map.get(key) + "\t" + key + "\t" + key + "\n");
		}
		out.close();
	}

	// TODO: based on Fabian's advice, we'll change the file format.
	public static void convert(String directory, String prefix, String outputDir) throws Exception {
		Map<String,List<Instance>> map = Utils.load(directory,  prefix);

		Set<String> activities = new TreeSet<String>();
		Set<String> props = new TreeSet<String>();
		
		// Iterate over all of the instances and gather all of the potential propositions
		// so that we can correctly construct the map.
		for (String key : map.keySet()) { 
			activities.add(key);
			for (Instance instance : map.get(key)) { 
				for (Interval interval : instance.intervals()) { 
					props.add(DataMap.getKey(interval.keyId));
				}
			}
		}
		
		// Now construct a lookup map for activities and propositions
		Map<String,Integer> activityMap = new TreeMap<String,Integer>();
		for (String a : activities)
			activityMap.put(a, activityMap.size()+1);
		
		dumpFile(outputDir + prefix + "_scene_windows.int.tksm", activityMap);

		Map<String,Integer> propMap = new TreeMap<String,Integer>();
		for (String p : props)
			propMap.put(p, propMap.size()+1);
		
		dumpFile(outputDir + prefix + "_intervals.int.tksm", propMap);
		
		// Now that we have figured out how we are going to refer to activities and propositions
		// by integer ids we can begin writing out the intervals.
		BufferedWriter o1 = new BufferedWriter(new FileWriter(outputDir + prefix + "_intervals.int"));
		BufferedWriter o2 = new BufferedWriter(new FileWriter(outputDir + prefix + "_scene_windows.int"));
		
		int time = 0;		
		for (String activity : map.keySet()) { 
			for (Instance instance : map.get(activity)) { 
				int maxTime = Integer.MIN_VALUE;
				
				List<Interval> intervals = instance.intervals();
				Collections.sort(intervals, Interval.esf);
				for (Interval interval : intervals) { 
					int propId = propMap.get(DataMap.getKey(interval.keyId));
					
					// We subtract one because in CAVE the end time isn't inclusive and in TSKM
					// the end time is inclusive
					o1.write(propId + "\t" + (interval.start+time) + "\t" + (interval.end+time-1) + "\n");
					
					maxTime = Math.max(maxTime, interval.end);
				}
				
				// write out the scene information for this instance....
				o2.write(activityMap.get(activity) + "\t" + time + "\t" + (time+maxTime) + "\n");
				
				// Add in the time for this instance as well as some buffer
				time += maxTime + 100;
			}
		}
		
		o1.close();
		o2.close();
	}
	
	
	public static void main(String[] args) throws Exception {
		convert("data/input/", "ww3d", "data/tksm/");
	}
}
