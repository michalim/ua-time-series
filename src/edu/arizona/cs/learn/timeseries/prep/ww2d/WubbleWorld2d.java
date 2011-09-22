package edu.arizona.cs.learn.timeseries.prep.ww2d;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.timeseries.prep.TimeSeries;


/**
 * The purpose of this class is to take in the raw ww2d data and
 * convert it into some set of propositions.  This is not a blind
 * procedure as it has been before.  In this case, we are interested in
 * certain relations and we'll try to find them.
 * @author wkerr
 *
 */
public class WubbleWorld2d {
	
	public static enum DBType {
		Global,
		Agent,
		Object
	}
	
	private static enum PropState {
		True,
		False,
		Unknown
	}
	
	public static double ZERO = 0.01;
	
	private boolean _ignoreWalls;
	
	private Map<DBType,List<String>> _headers = new HashMap<WubbleWorld2d.DBType, List<String>>();
	private Map<DBType, Map<String,List<Double>>> _doubleMap = new HashMap<WubbleWorld2d.DBType, Map<String,List<Double>>>();
	private Map<DBType, Map<String,List<String>>> _stringMap = new HashMap<WubbleWorld2d.DBType, Map<String,List<String>>>();
	private Map<DBType, Map<String,List<PropState>>> _propMap = new HashMap<WubbleWorld2d.DBType, Map<String,List<PropState>>>();
	private Map<DBType, Map<String,List<Boolean>>> _booleanMap = new HashMap<WubbleWorld2d.DBType, Map<String,List<Boolean>>>();
	
	public WubbleWorld2d(boolean ignoreWalls) { 
		_ignoreWalls = ignoreWalls;
	}
	
	public void load(String input, DBType dType) {
		_headers.put(dType, new ArrayList<String>());
		Map<String,List<String>> map = new HashMap<String,List<String>>();

		try { 
			BufferedReader in = new BufferedReader(new FileReader(input));
			
			String line = in.readLine();
			String[] tokens = line.split("[,]");
				
			for (String token : tokens) {
				String header = token.replaceAll("[\"]", "");
				_headers.get(dType).add(header);
				map.put(header, new ArrayList<String>());
			}
			
			while (in.ready()) { 
				line = in.readLine();
				tokens = line.split("[,]");
				for (int i = 0; i < tokens.length; ++i) 
					map.get(_headers.get(dType).get(i)).add(tokens[i]);
			}
			in.close();
		} catch (Exception e) { 
			e.printStackTrace();
		}

		_doubleMap.put(dType, new HashMap<String,List<Double>>());
		_stringMap.put(dType, new HashMap<String,List<String>>());
		_propMap.put(dType, new HashMap<String,List<PropState>>());
		_booleanMap.put(dType, new HashMap<String,List<Boolean>>());
		
		// Now iterate through all of the possible columns and 
		// partition them into the correct sets.
		for (String key : _headers.get(dType)) { 
			
			// determine the type....
			// Unfortunately, the only way to robustly determine the type is to go
			// through the entire list.
			boolean seeUnknown, seeBoolean, seeDouble, seeOthers;
			seeUnknown = seeBoolean = seeDouble = seeOthers = false;
			for (String s : map.get(key)) {
				if (s.equalsIgnoreCase("unknown")) {
					seeUnknown = true;
				} else if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
					seeBoolean = true;
				} else {
					try {
						Double.parseDouble(s);
						seeDouble = true;
					} catch (Exception e) {
						seeOthers = true;
					}
				}
			}
			
			// Add the data to the appropriate map
			if (seeBoolean && !seeDouble && !seeOthers) {
				if (!seeUnknown) {
					// Boolean
					System.out.println("Adding: " + key);
					List<Boolean> list = new ArrayList<Boolean>();
					for (String value : map.get(key)) { 
						list.add(Boolean.parseBoolean(value));
					}
					_booleanMap.get(dType).put(key, list);
				} else {
					// Proposition state (i.e. boolean + unknown)
					List<PropState> list = new ArrayList<PropState>();
					for (String value : map.get(key)) {
						if ("true".equalsIgnoreCase(value))
							list.add(PropState.True);
						else if ("false".equalsIgnoreCase(value))
							list.add(PropState.False);
						else if ("unknown".equalsIgnoreCase(value))
							list.add(PropState.Unknown);
						else
							throw new RuntimeException("Unknown prop state: " + value);
					}
					_propMap.get(dType).put(key, list);
				}
			} else if (seeDouble && !seeBoolean && !seeOthers) {
				// Double
				try {
					List<Double> list = new ArrayList<Double>();
					for (String value : map.get(key)) { 
						if (value.equalsIgnoreCase("unknown"))
							list.add(Double.NaN);
						else
							list.add(Double.parseDouble(value));
					}
					_doubleMap.get(dType).put(key, list);
				} catch (Exception e) { 
					throw new RuntimeException("Bad double value.");
				}
			} else {
				// String
				_stringMap.get(dType).put(key, map.get(key));
			}
		}
	}
	
	/**
	 * Take an array of strings and convert them into a map of boolean values
	 * @param values
	 * @return
	 */
	private Map<String,List<Boolean>> convert(List<String> values) { 
		Map<String,List<Boolean>> map = new HashMap<String,List<Boolean>>();
		for (String s : values) { 
			if (!map.containsKey(s) && !s.equals("NaN"))
				map.put(s, new ArrayList<Boolean>(values.size()));
		}

		for (String s : values) { 
			for (String key : map.keySet()) { 
				if (s.equals(key))
					map.get(key).add(true);
				else
					map.get(key).add(false);
			}
		}
		return map;
	}
	
	/**
	 * For each distance double, create four possible propositions
	 *  -- DistanceStable
	 *  -- DistanceDecreasing
	 *  -- DistanceIncreasing
	 *  -- DistanceUnknown
	 *  
	 *  Note: For now there is no smoothing going on.
	 *  Note: For now ignore the walls.
	 */
	public void doGlobalDistances() { 
		for (String key : _headers.get(DBType.Global)) {
			if (!key.startsWith("distance")) 
				continue;
			
			if (_ignoreWalls && key.matches(".*wall.*"))
				continue;
			
			String suffix = key.substring(8);
			System.out.println(suffix);
			
			// assumption is that it should be populated in the double map.
			List<Double> values = _doubleMap.get(DBType.Global).get(key);
			if (values == null) { 
				throw new RuntimeException("Unknown key: " + key);
			}
			List<Double> diff = TimeSeries.diff(values);
			
			List<Boolean> dd = new ArrayList<Boolean>();
			List<Boolean> di = new ArrayList<Boolean>();
			List<Boolean> ds = new ArrayList<Boolean>();
			List<Boolean> du = new ArrayList<Boolean>();
			
			// The first difference means that the first value is
			// not a number so none of these propositions can be true.
			dd.add(false);
			di.add(false);
			ds.add(false);
			du.add(false);
			diff.remove(0);
			
			for (Double d : diff) { 
				boolean inc = false;
				boolean dec = false;
				boolean stable = false;
				boolean unkn = false;
				if (Double.compare(Double.NaN, d) == 0) {
					unkn = true;
				} else if (d > ZERO)
					inc = true;
				else if (d < -ZERO)
					dec = true;
				else
					stable = true;
					
				ds.add(stable);
				di.add(inc);
				dd.add(dec);
				du.add(unkn);
			}
			
			_booleanMap.get(DBType.Global).put("distance-decreasing"+suffix, dd);
			_booleanMap.get(DBType.Global).put("distance-increasing"+suffix, di);
			_booleanMap.get(DBType.Global).put("distance-stable"+suffix, ds);
			_booleanMap.get(DBType.Global).put("distance-unknown"+suffix, du);
		}
	}
	
	/**
	 * The following real-valued variables will be converted into propositions
	 * 		relativeVx, relativeVy
	 * 		relativeX, relativeY
	 */
	public void doRelative() { 
		String[] prefixes = new String[] { "relativeVx", "relativeVy", "relativeX", "relativeY" };
		String[] mapped = new String[] { "rvx", "rvy", "rx" ,"ry" };
		for (int i = 0; i < prefixes.length; ++i) { 
			String prefix = prefixes[i];
			for (String key : _headers.get(DBType.Global)) {
				if (!key.startsWith(prefix)) 
					continue;
				
				if (_ignoreWalls && key.matches(".*wall.*"))
					continue;
				
				String suffix = key.substring(prefix.length());
				System.out.println(suffix);
				
				// assumption is that it should be populated in the 
				// double map.
				List<Double> values = _doubleMap.get(DBType.Global).get(key);
				if (values == null) { 
					throw new RuntimeException("Unknown key: " + key);
				}

				List<String> list = Arrays.asList("down", "stable", "up");
				List<Double> diff = TimeSeries.diff(values);
				List<String> sdl = TimeSeries.sdl(diff, Arrays.asList(-0.01,0.01), list);
				Map<String,List<Boolean>> sdlMap = convert(sdl);
				
				for (String symbol : sdlMap.keySet()) { 
					String s = mapped[i] + "-" + symbol + suffix;
					_booleanMap.get(DBType.Global).put(s, sdlMap.get(symbol));
				}
				
				List<Double> standard = TimeSeries.standardize(values);
				List<String> sax = TimeSeries.sax(standard, 7);
				Map<String,List<Boolean>> saxMap = convert(sax);

				for (String symbol : saxMap.keySet()) { 
					String s = "(sax " + mapped[i] + suffix + " " + symbol + ")";
					_booleanMap.get(DBType.Global).put(s, saxMap.get(symbol));
				}
			}
		}
	}
	
	/**
	 * For each x,y pair, determine if the agent is moving.  This could be augmented
	 * to additionally have a movement in one of the axes, such as moving-y and 
	 * moving-x
	 *  -- moving
	 *  -- moving-unknown
	 *  
	 *  Note: For now there is no smoothing going on.
	 */
	public void doMoving() { 
		Set<String> entities = new HashSet<String>();
		for (String key : _headers.get(DBType.Global)) {
			if (key.startsWith("x(") || key.startsWith("y(")) { 
				entities.add(key.substring(1));
			}
		}
		
		for (String suffix : entities) { 
			List<Double> x = _doubleMap.get(DBType.Global).get("x" + suffix);
			List<Double> y = _doubleMap.get(DBType.Global).get("y" + suffix);
			
			List<Double> diffX = TimeSeries.diff(x);
			List<Double> diffY = TimeSeries.diff(y);

			List<Boolean> moving = new ArrayList<Boolean>();
			List<Boolean> movingUnknown = new ArrayList<Boolean>();			
			moving.add(false);
			movingUnknown.add(false);
			
			for (int i = 1; i < x.size(); ++i) {
				if (Double.compare(Double.NaN, diffX.get(i)) == 0 && Double.compare(Double.NaN, diffY.get(i)) == 0) {
					moving.add(false);
					movingUnknown.add(true);
				} else {
					movingUnknown.add(false);
					if ( (Double.compare(Double.NaN, diffX.get(i)) != 0 && diffX.get(i) > ZERO || diffX.get(i) < -ZERO)
							|| (Double.compare(Double.NaN, diffY.get(i)) != 0 && diffY.get(i) > ZERO || diffY.get(i) < -ZERO) )
						moving.add(true);
					else
						moving.add(false);
				}
			}

			_booleanMap.get(DBType.Global).put("moving"+suffix, moving);
			_booleanMap.get(DBType.Global).put("moving-unknown"+suffix, movingUnknown);
		}
	}
	
	public List<Interval> toIntervals() { 
		List<Interval> intervals = new ArrayList<Interval>();
		for (String key : _booleanMap.get(DBType.Global).keySet()) { 			
			if (_ignoreWalls && key.matches(".*wall.*"))
				continue;

			intervals.addAll(TimeSeries.booleanToIntervals(key, _booleanMap.get(DBType.Global).get(key)));
		}
		return intervals;
	}

	public static void main(String[] args) { 
//		String[] activities = {"chase", "eat", "fight", "flee", "kick-ball", "kick-column"};
//		String[] activities = {"collide", "pass", "talk-a", "talk-b"};

//		String[] activities = {"chase", "fight", "flee", "kick-ball", "kick-column", "collide", "pass", "talk-a", "talk-b"};
		String[] activities = {"eat"};

		global(30, activities, true);
	} 
	
	public static void global(int n, String[] activities, boolean ignoreWalls) { 
		WubbleWorld2d ww2d = new WubbleWorld2d(ignoreWalls);
		String prefix = "data/raw-data/ww2d/";

		for (String act : activities) { 
			try { 
				BufferedWriter out = new BufferedWriter(new FileWriter(prefix + "lisp/ww2d-" + act + ".lisp"));
				for (int i = 1; i <= n; ++i) {
					String filename = act + "/" + act + "-" + i + ".csv";
					System.out.println("Activity: " + filename);
					
					// Load global
					ww2d.load(prefix + "global/" + filename, DBType.Global);
//					ww2d.load(prefix + "agent/" + filename, DataType.Agent);
//					ww2d.load(prefix + "object/" + filename, DataType.Object);
					
					// Calculate propositions
					ww2d.doGlobalDistances();
					ww2d.doMoving();
					ww2d.doRelative();
					
					// Convert to intervals
					List<Interval> intervals = ww2d.toIntervals();
					
					// Output episode
					out.write("(" + i + "\n");
					out.write(" (\n");
					for (Interval interval : intervals) { 
						out.write("(\"" + interval.name + "\" " + 
								interval.start + " " +
								interval.end + ")\n");
					}
					out.write(" )\n");
					out.write(")\n");
				}
				out.close();
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}
}
