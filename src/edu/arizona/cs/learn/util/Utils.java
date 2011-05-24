package edu.arizona.cs.learn.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.arizona.cs.learn.algorithm.bpp.BPPFactory;
import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.timeseries.model.SequenceType;
import edu.arizona.cs.learn.timeseries.model.symbols.ComplexSymbol;
import edu.arizona.cs.learn.timeseries.model.symbols.Symbol;
import edu.arizona.cs.learn.timeseries.model.values.Binary;
import edu.arizona.cs.learn.timeseries.model.values.Value;

public class Utils {
	public static int EXPERIMENTS = 50;

	public static Random random;
	public static int numThreads;
	public static NumberFormat nf;

	public static String tmpDir = "/tmp/";
	
	public static Set<String> testExcludeSet = new HashSet<String>();
	  
	public static String[] HARD_DATA = { "vowel", "ww2d", "auslan", };
	public static String[] EASY_DATA = { "wes", "nicole", "derek", "ecg", "wafer", "ww3d" };
	
	public static String[] alphabet = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
		"k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
	};

    public static boolean LIMIT_RELATIONS = true;
	public static int WINDOW = 5;
	
	static { 
		random = new Random();
		numThreads = Runtime.getRuntime().availableProcessors();
		numThreads = Math.min(4, numThreads);

		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
	}

	/**
	 * Convert the string prefix into a list of possible 
	 * prefixes.  Handles bundles like easy, hard, and all
	 * @param pre
	 * @return
	 */
	public static List<String> getPrefixes(String pre) { 
		List<String> prefixes = new ArrayList<String>();
		if (pre.equals("all")) { 
			for (String prefix : EASY_DATA)  
				prefixes.add(prefix);
			for (String prefix : HARD_DATA)
				prefixes.add(prefix);
		} else if (pre.equals("easy")) { 
			for (String prefix : EASY_DATA)  
				prefixes.add(prefix);
		} else if (pre.equals("hard")) { 
			for (String prefix : HARD_DATA)
				prefixes.add(prefix);
		} else {
			prefixes.add(pre);
		}		
		return prefixes;
	}
	
	/**
	 * Helper function to convert a confusion matrix into a CSV
	 * string with new lines.
	 * @param classNames
	 * @param matrix
	 * @return
	 */
	public static String toCSV(List<String> classNames, int[][] matrix) { 
		StringBuffer buf = new StringBuffer();
		for (String s : classNames) 
			buf.append("," + s);
		buf.append("\n");

		for (int i = 0; i < classNames.size(); i++) {
			buf.append(classNames.get(i));
			for (int j = 0; j < classNames.size(); j++) 
				buf.append("," + matrix[i][j]);
			buf.append("\n");
		}
		return buf.toString();
	}
	
	/**
	 * Return the activity names rather than the files themselves.
	 * @param prefix
	 * @return
	 */
	public static List<String> getActivityNames(String prefix) {
		List<String> activities = new ArrayList<String>();
		for (File f : new File("data/input/").listFiles()) {
			if ((f.getName().startsWith(prefix)) && (f.getName().endsWith("lisp"))) {
				String name = f.getName();
				activities.add(name.substring(0, name.indexOf(".lisp")));
			}
		}
		return activities;
	}
	
	/**
	 * This method generates sequences of a more complex nature than Allen Relations.
	 * Each element in the seuqence is an array of bits, one bit for each proposition. 
	 * Since we will be learning across multiple activities, and each activity will potentially
	 * have a different set of proposition, we first have to load in all of the different
	 * activity types and create a complete set of propositions before we can construct
	 * the sequences.
	 * @param fileMap
	 * @return
	 */
	public static Map<String,List<Instance>> convert(Map<String,String> fileMap) { 
		
		Map<String,List<Instance>> map = new HashMap<String,List<Instance>>();
		Set<String> propSet = new TreeSet<String>();
		for (String key : fileMap.keySet()) { 
			
			List<Instance> instances = Instance.load(new File(fileMap.get(key)));
			map.put(key, instances);
			
			for (Instance instance : instances) { 
				for (Interval interval : instance.intervals()) {
					propSet.add(interval.name);
				}
			}
		}
		
		List<String> props = new ArrayList<String>(propSet);
		for (List<Instance> instances : map.values()) { 
			for (Instance instance : instances) { 
				List<Interval> cba = BPPFactory.compress(instance.intervals(), Interval.eff);
				List<Symbol> sequence = toSequence(props, cba);
				instance.intervals(cba);
				instance.sequence(sequence);
			}
		}

		return map;
	}
	
	/**
	 * Construct a sequence from the propositions and the intervals given.
	 * @param props
	 * @param intervals
	 * @return
	 */
	public static List<Symbol> toSequence(List<String> props, List<Interval> intervals) { 
		List<Symbol> results = new ArrayList<Symbol>();
		
		int tmpSize = 0;
		int endTime = 0;
		int startTime = Integer.MAX_VALUE;
		Map<String,List<Interval>> propMap = new TreeMap<String,List<Interval>>();
		for (Interval i : intervals) { 
			List<Interval> propIntervals = propMap.get(i.name);
			if (propIntervals == null) { 
				propIntervals = new ArrayList<Interval>();
				propMap.put(i.name, propIntervals);
			}
			
			propIntervals.add(i);
			startTime = Math.min(i.start, startTime);
			endTime = Math.max(i.end, endTime);
			
			tmpSize = Math.max(tmpSize, i.name.length());
		}
		tmpSize += 1;

		int time = (endTime - startTime);
		for (int i = 0; i < time; ++i) { 
			// Determine the state by looping over all of the props and determining
			// if they are on or off.
			List<Value> state = new ArrayList<Value>();
			for (int j = 0; j < props.size(); ++j) {
				String prop = props.get(j);
				List<Interval> propIntervals = propMap.get(prop);
				if (propIntervals == null) {
					state.add(new Binary(prop, Binary.FALSE));
					continue;
				} 

				boolean on = false;
				for (Interval interval : propIntervals) { 
					if (interval.on(i)) {
						state.add(new Binary(prop, Binary.TRUE));
						on = true;
						break;
					}
				}
				if (!on)
					state.add(new Binary(prop, Binary.FALSE));
			}
			
			results.add(new ComplexSymbol(state, 1));
		}
		return results;
	}

	/**
	 * Load in *all* of the files with a given prefix.  It is assumed that each file
	 * contains a set of instances that are examples of the *same* activity.  
	 * @param prefix
	 * @param type
	 * @return
	 */
	public static Map<String,List<Instance>> load(String prefix, SequenceType type) { 
		return load("data/input/", prefix, type);
	}
	
	/**
	 * Load in *all* of the files with a given prefix.  It is assumed that each file
	 * contains a set of instances that are examples of the *same* activity.  
	 * @param directory
	 * @param prefix
	 * @param type
	 * @return
	 */
	public static Map<String,List<Instance>> load(String directory, String prefix, SequenceType type) { 
		Map<String,List<Instance>> map = new HashMap<String,List<Instance>>();
		for (File f : new File(directory).listFiles()) {
			if (f.getName().startsWith(prefix) && f.getName().endsWith("lisp")) { 
				String name = f.getName();
				String label = name.substring(0, name.indexOf(".lisp"));

				map.put(label, Instance.load(label, f, type));
			}
		}
		return map;
	}

	public static Map<String, List<Integer>> getTestSet(String prefix, int k, int fold) {
		Map<String,List<Integer>> map = new TreeMap<String,List<Integer>>();
		try {
			String f = "data/cross-validation/k" + k + "/fold-" + fold + "/" + prefix + "-test.txt";
			BufferedReader in = new BufferedReader(new FileReader(f));
			while (in.ready()) {
				String line = in.readLine();
				String[] tokens = line.split("[ ]");

				List<Integer> list = new ArrayList<Integer>();
				for (int i = 1; i < tokens.length; i++) {
					list.add(Integer.valueOf(Integer.parseInt(tokens[i])));
				}
				map.put(tokens[0], list);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return map;
	}

	/**
	 * Write out the episodes in the format that we expect to read them in.
	 * @param file
	 * @param episodes
	 */
	public static void writeEpsiodes(String file, List<List<Interval>> episodes) { 
		try { 
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			
			for (int i = 0; i < episodes.size(); ++i) { 
				out.write("(" + (i+1) + "\n");
				out.write(" (\n");

				// now write out all of the intervals.
				for (Interval interval : episodes.get(i))  
					out.write("  (\"" + interval.name + "\" " + interval.start + " " + interval.end + ")\n");
				
				out.write(" )\n");
				out.write(")\n");
			}
			
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test to see if the two lists of intervals can interact with each other
	 * TODO: clean up this ugly code.
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static boolean interact(List<Interval> list1, List<Interval> list2, int window) {
		class Duration { 
			public int start;
			public int end;
			
			public Duration(int start, int end) { 
				this.start = start;
				this.end = end;
			}
		}
		
		List<Duration> list = new ArrayList<Duration>();
		for (Interval i1 : list1) { 
			boolean added = false;
			
			for (int i = 0; i < list.size() && !added; ++i) { 
				Duration d = list.get(i);
				if (i1.overlaps(d.start, d.end, window)) { 
					d.start = Math.min(d.start, i1.start);
					d.end = Math.min(d.end, i1.end);
					added = true;
				}
			}
			
			if (!added) 
				list.add(new Duration(i1.start, i1.end));
		}
		
		for (Interval i1 : list2) { 
			boolean added = false;
			
			for (int i = 0; i < list.size() && !added; ++i) { 
				Duration d = list.get(i);
				if (i1.overlaps(d.start, d.end, window)) { 
					d.start = Math.min(d.start, i1.start);
					d.end = Math.min(d.end, i1.end);
					added = true;
				}
			}
			
			if (!added) 
				list.add(new Duration(i1.start, i1.end));
		}
		
		
		int lastSize = 0;
		while (list.size() > 1 || list.size() == lastSize) {
			lastSize = list.size();
			
			List<Duration> newList = new ArrayList<Duration>();
			for (Duration d1 : list) { 
				boolean added = false;
				
				for (int i = 0; i < newList.size() && !added; ++i) { 
					Duration d2 = newList.get(i);
					if (Interval.overlaps(d1.start, d1.end, d2.start, d2.end, window)) {
						d2.start = Math.min(d2.start, d1.start);
						d2.end = Math.min(d2.end, d1.end);
						added = true;
					}
				}
				
				if (!added) 
					list.add(new Duration(d1.start, d1.end));
			}
			
			list = newList;
		}
		
		if (list.size() == 1)
			return true;
		return false;
	}
}
