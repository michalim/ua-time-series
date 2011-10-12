package edu.arizona.cs.learn.timeseries.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.arizona.cs.learn.util.DataMap;

public class Interval {
	private static Logger logger = Logger.getLogger(Interval.class);

	/** This is the file that this episode came from */
	public String file;

	/** The keyId is a unique number that maps to a proposition */
	public int keyId;
	
	public int episode;

	public int start;
	public int end;
	
	/**
	 * Constructor to make a new interval.
	 * @param propId
	 * @param s
	 * @param e
	 */
	public Interval(int propId, int s, int e) { 
		this.keyId = propId;
		this.start = s;
		this.end = e;
		this.episode = -1;
		
	}
	
	/**
	 * Constructor to make a new interval. The proposition
	 * name is given as a string and converted into an integer.
	 * @param prop
	 * @param s
	 * @param e
	 */
	public Interval(String prop, int s, int e) {
		this.keyId = DataMap.findOrAdd(prop);
		this.start = s;
		this.end = e;
		this.episode = -1;
	}
	
	public boolean on(int time) {
		return time >= start && time < end;
	}

	/**
	 * Does this interval overlap with the given interval or does
	 * either interval start within some time period of the other?
	 * @param i
	 * @param window
	 * @return
	 */
	public boolean overlaps(Interval i, int window) {
		if (end + window <= i.start)
			return false;
		if (i.end + window <= start)
			return false;

		return true;
	}
	
	public boolean overlaps(int s, int e, int window) { 
		if (end + window <= s)
			return false;
		if (e + window <= start)
			return false;

		return true;
	}

	/**
	 * Returns the full string representation of this interval...
	 * TODO: Might want to use the keyId since it is a more compact representation.
	 */
	public String toString() {
		return file + " " + episode + " " + DataMap.getKey(keyId) + " " + start + " " + end;
	}

	public void toXML(Element e) {
		e.addElement("Interval").addAttribute("file", this.file)
				.addAttribute("episode", this.episode + "")
				.addAttribute("name", DataMap.getKey(this.keyId))
				.addAttribute("start", this.start + "")
				.addAttribute("end", this.end + "");
	}

	public static Interval fromXML(Element e) {
		String file = e.attributeValue("file");
		int episode = Integer.parseInt(e.attributeValue("episode"));
		String name = e.attributeValue("name");
		int start = Integer.parseInt(e.attributeValue("start"));
		int end = Integer.parseInt(e.attributeValue("end"));

		Interval i = new Interval(name, start, end);
		i.episode = episode;
		i.file = file;
		return i;
	}

	public void fromString(String s) {
		String[] args = s.split("[ ]");
		file = args[0];
		episode = Integer.parseInt(args[1]);
		keyId = DataMap.findOrAdd(args[2]);
		start = Integer.parseInt(args[3]);
		end = Integer.parseInt(args[4]);
	}

	public Interval copy() {
		Interval i = new Interval(keyId, start, end);
		i.episode = episode;
		i.file = file;
		return i;
	}

	/**
	 * Test the list of intervals to see if they are all off at the given time.
	 * 
	 * @param intervals
	 * @param time
	 * @return
	 */
	public static boolean allOff(List<Interval> intervals, int time) {
		for (Interval i : intervals) {
			if (i.on(time))
				return false;
		}
		return true;
	}

	/**
	 * Convert a stream into a list of intervals.
	 * 
	 * @param key
	 * @param stream
	 * @return
	 */
	public static List<Interval> getIntervals(String key, boolean[] stream) {
		List<Interval> list = new ArrayList<Interval>();

		int startPos = 0;
		boolean expected = false;
		for (int i = 0; i < stream.length; ++i) {
			if (stream[i] != expected) {
				if (expected) {
					list.add(new Interval(key, startPos, i));
				}
				expected = !expected;
				startPos = i;
			}
		}

		if (expected && startPos < stream.length) {
			list.add(new Interval(key, startPos, stream.length));
		}

		return list;
	}

	/**
	 * Convert a stream into a list of intervals.
	 * 
	 * @param key
	 * @param stream
	 * @return
	 */
	public static List<Interval> getIntervals(String key, List<Boolean> stream) {
		List<Interval> list = new ArrayList<Interval>();

		int startPos = 0;
		boolean expected = stream.get(0);
		for (int i = 0; i < stream.size(); ++i) {
			if (stream.get(i) != expected) {
				if (expected) {
					list.add(new Interval(key, startPos, i));
				}
				expected = !expected;
				startPos = i;
			}
		}

		if (expected && startPos < stream.size()) {
			list.add(new Interval(key, startPos, stream.size()));
		}

		return list;
	}

	/**
	 * A generic comparator that will compare two temporal objects in a sortable
	 * fashion. eff --> earliest finishing first.
	 */
	public static Comparator<Interval> eff = new Comparator<Interval>() {
		public int compare(Interval o1, Interval o2) {
			if (o1.end > o2.end)
				return 1;
			if (o1.end < o2.end)
				return -1;

			if (o1.start > o2.start)
				return 1;
			if (o1.start < o2.start)
				return -1;

			String name1 = o1.toString();
			String name2 = o2.toString();

			int compValue = name1.compareTo(name2);
			if (compValue != 0)
				return compValue;

			throw new RuntimeException("Error equals names " + name1 + " "
					+ name2);
		}

	};

	/**
	 * A generic comparator that will compare two temporal objects in a sortable
	 * fashion. esf --> earliest starting first.
	 */
	public static Comparator<Interval> esf = new Comparator<Interval>() {
		public int compare(Interval o1, Interval o2) {
			if (o1.start > o2.start)
				return 1;
			if (o1.start < o2.start)
				return -1;

			if (o1.end > o2.end)
				return 1;
			if (o1.end < o2.end)
				return -1;

			String name1 = o1.toString();
			String name2 = o2.toString();

			int compValue = name1.compareTo(name2);
			if (compValue != 0)
				return compValue;

			throw new RuntimeException("Error equals names " + name1 + " "
					+ name2);
		}

	};

	/**
	 * A generic comparator that will compare two temporal objects in a sortable
	 * fashion. Order by start time and ignore end time completely
	 */
	public static Comparator<Interval> starts = new Comparator<Interval>() {
		public int compare(Interval o1, Interval o2) {
			if (o1.start > o2.start)
				return 1;
			if (o1.start < o2.start)
				return -1;

			String name1 = o1.toString();
			String name2 = o2.toString();

			int compValue = name1.compareTo(name2);
			if (compValue != 0)
				return compValue;

			throw new RuntimeException("Error equals names " + name1 + " "
					+ name2);
		}

	};

	/**
	 * A generic comparator that will compare two temporal objects in a sortable
	 * fashion. Order by end time and ignore start time completely
	 */
	public static Comparator<Interval> ends = new Comparator<Interval>() {
		public int compare(Interval o1, Interval o2) {
			if (o1.end > o2.end)
				return 1;
			if (o1.end < o2.end)
				return -1;

			String name1 = o1.toString();
			String name2 = o2.toString();

			int compValue = name1.compareTo(name2);
			if (compValue != 0)
				return compValue;

			throw new RuntimeException("Error equals names " + name1 + " "
					+ name2);
		}

	};

	/**
	 * Test to see if the three intervals interact within the specified window.
	 * 
	 * @param i1
	 * @param i2
	 * @param i3
	 * @param window
	 * @return
	 */
	public static boolean interact(Interval i1, Interval i2, Interval i3,
			int window) {
		boolean overlaps1 = i1.overlaps(i2, window);
		boolean overlaps2 = i1.overlaps(i3, window);
		boolean overlaps3 = i2.overlaps(i3, window);

		if (overlaps1 && overlaps3 || overlaps2 && overlaps3)
			return true;

		return false;
	}
	
	/**
	 * Given the starting and ending times of two intervals, determine if
	 * they overlap with each other.  
	 * @param s1
	 * @param e1
	 * @param s2
	 * @param e2
	 * @param window
	 * @return
	 */
	public static boolean overlaps(int s1, int e1, int s2, int e2, int window) { 
		if (e2 + window <= s1)
			return false;
		if (e1 + window <= s2)
			return false;

		return true;
		
	}
}
