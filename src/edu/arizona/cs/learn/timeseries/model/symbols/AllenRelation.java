package edu.arizona.cs.learn.timeseries.model.symbols;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.util.DataMap;
import edu.arizona.cs.learn.util.Utils;


public class AllenRelation extends StringSymbol implements Comparable<AllenRelation> {
	
	public static final String[] fullText = new String[] { 
		"equals", "starts-with", "finishes-with", "before",
		"none", "meets", "overlaps", "contains", "during"
	};

	public static final String[] partialText = new String[] { 
		"eq", "s", "f", "b", "n", "m", "o", "c", "d"
	};
	
	public static String[] text = partialText;
	
	private int _prop1;
	private int _relation;
	private int _prop2;
	
	private Interval _interval1;
	private Interval _interval2;
		
	/**
	 * Construct a new AllenRelation
	 * @param p1
	 * @param r
	 * @param p2
	 */
	public AllenRelation(String p1, String r, String p2, double weight) { 
		this(DataMap.findOrAdd(p1), DataMap.findOrAdd(r), DataMap.findOrAdd(p2));
	}
	
	/**
	 * Construct a new AllenRelation from the IDs 
	 * @param p1 -- the numeric id for proposition 1
	 * @param r -- the numeric id for the Allen relation
	 * @param p2 -- the numeric id for proposition 2
	 */
	public AllenRelation(int p1, int r, int p2) { 
		this(p1, r, p2, 1.0);
	}
	
	/**
	 * Construct a new AllenRelation from the intervals.
	 * @param relation -- the numeric id of the relation
	 * @param i1
	 * @param i2
	 */
	public AllenRelation(int relation, Interval i1, Interval i2) { 
		this(relation, i1, i2, 1.0);
	}

	/**
	 * Construct a new AllenRelation from the intervals.
	 * @param relation -- the numeric id of the relation
	 * @param i1
	 * @param i2
	 * @param weight
	 */
	public AllenRelation(int relation, Interval i1, Interval i2, double weight) { 
		this(i1.keyId, relation, i2.keyId, weight);
		
		_interval1 = i1;
		_interval2 = i2;
	}
	
	/**
	 * Construct a new AllenRelation from the IDs 
	 * @param p1 -- the numeric id for proposition 1
	 * @param r -- the numeric id for the Allen relation
	 * @param p2 -- the numeric id for proposition 2
	 * @param weight
	 */
	public AllenRelation(int p1, int r, int p2, double weight) { 
		super(weight);
		
		_prop1 = p1;
		_relation = r;
		_prop2 = p2;
	}
	
	public int prop1() { 
		return _prop1;
	}

	public int prop2() { 
		return _prop2;
	}
	
	public int relation() { 
		return _relation;
	}
	
	public String toString() { 
		if (_name == null || _name.equals(""))
			_name = "(" + _prop1 + " " + _relation + " " + _prop2 + ")";
		return _name;
	}
	
	/**
	 * The hash code for this object is just the hash code of
	 * the key itself...
	 */
	public int hashCode() { 
		throw new RuntimeException("Tracking down who is calling this...");
	}
	
	public String latex() { 
		throw new RuntimeException("Revisit this method");
//		return "\\ar{" + _prop1 + "}{" + _relation + "}{" + _prop2 + "} [$\\cdot$]";
	}
	
	public Interval interval1() { 
		if (_interval1 == null)
			throw new RuntimeException("Used the wrong constructor for this call");
		return _interval1;
	}
	
	public Interval interval2() { 
		if (_interval2 == null)
			throw new RuntimeException("Used the wrong constructor for this call");
		return _interval2;
	}
	
	public boolean equals(Object o) { 
		if (!(o instanceof AllenRelation))
			return false;
		
		AllenRelation r = (AllenRelation) o;
		return _prop1 == r._prop1 && _prop2 == r._prop2 && _relation == r._relation;
	}
	
	@Override
	public List<Integer> getProps() {
		List<Integer> props = new ArrayList<Integer>();
		props.add(_prop1);
		props.add(_prop2);
		return props;
	}

	@Override
	public List<Interval> getIntervals() {
		List<Interval> intervals = new ArrayList<Interval>();
		intervals.add(_interval1);
		intervals.add(_interval2);
		return intervals;
	}

	@Override
	public Symbol copy() { 
		if (_interval1 != null && _interval2 != null) 
			return new AllenRelation(_relation, _interval1, _interval2, _weight);

		return new AllenRelation(_prop1, _relation, _prop2, _weight);
	}
	
	@Override
	public Symbol merge(Symbol B) { 
		return new AllenRelation(_relation, _interval1, _interval2, _weight+B._weight);
	}
	
	@Override
	public int compareTo(AllenRelation ar) {
		return toString().compareTo(ar.toString());
	}
	
	/**
	 * When storing the AllenRelation to XML, we store out the string value
	 * since there won't be a guarantee that the relations will be the same.
	 * @param e
	 */
	public void toXML(Element e) {
		Element allen = e.addElement("symbol")
			.addAttribute("class", "AllenRelation");

		allen.addAttribute("relation", DataMap.getKey(_relation));
		allen.addAttribute("weight", _weight +"");
		this._interval1.toXML(allen);
		this._interval2.toXML(allen);
	}

	/**
	 * The string relationship is stored in the file.
	 * @param e
	 * @return
	 */
	public static AllenRelation fromXML(Element e) {
		String relation = e.attributeValue("relation");
		double weight = Double.parseDouble(e.attributeValue("weight"));

		List<?> list = e.elements("Interval");
		if (list.size() != 2) {
			throw new RuntimeException("The number of intervals is wrong: "
					+ list.size());
		}
		Interval i1 = Interval.fromXML((Element) list.get(0));
		Interval i2 = Interval.fromXML((Element) list.get(1));

		
		return new AllenRelation(DataMap.findOrAdd(relation), i1, i2, weight);
	}
	
	/**
	 * Called when Interval i1 finishes 
	 * @param i1
	 * @param i2
	 * @return
	 */
	public static String forward(Interval i1, Interval i2) { 
		if (i1.start == i2.start && i1.end == i2.end)
			return text[0];
		if (i1.start == i2.start)
			return text[1];
		if (i1.end == i2.end)
			return text[2];

		if (i1.end < i2.start && i2.start - i1.end < Utils.WINDOW)
			return text[3];
		if (i1.end < i2.start)
			return text[4];
		if (i1.end == i2.start)
			return text[5];

		// i2.start > i1.start due to starts-with check and sorting
		if (i1.end < i2.end)
			return text[6];
		if (i1.end > i2.end) 
			return text[7];
		
		throw new RuntimeException("Unknown relation between:\n  " + 
				i1.toString() + "\n  " + i2.toString());	
	}
	
	/**
	 * Assumptions made when calling this function....
	 *   Interval i1 ends before or at the same time as Interval i2.
	 * @param i1
	 * @param i2
	 * @return
	 */
	public static String get(Interval i1, Interval i2) {
		if (i1.start == i2.start && i1.end == i2.end)
			return text[0];
		if (i1.start == i2.start)
			return text[1];
		if (i1.start > i2.start)
			return text[8];
		
		if (i1.end < i2.start && i2.start - i1.end < Utils.WINDOW)
			return text[3];
		if (i1.end < i2.start)
			return text[4];
		if (i1.end == i2.start)
			return text[5];
		if (i1.end < i2.end)
			return text[6];
		if (i1.end == i2.end)
			return text[2];
		
		throw new RuntimeException("Unknown relation between:\n  " + 
				i1.toString() + "\n  " + i2.toString());	
	}
	
	/**
	 * Return the Allen relation between the given intervals.  The 
	 * intervals can be in any order since we are enumerating all 13 possible
	 * Allen relations.
	 * @param i1
	 * @param i2
	 * @return
	 */
	public static String unordered(Interval i1, Interval i2) { 
		return unordered(i1, i2, false);
	}

	/**
	 * Return the Allen relation between the given intervals.  The 
	 * intervals can be in any order since we are enumerating all 13 possible
	 * Allen relations.
	 * @param i1
	 * @param i2
	 * @param shortVersion -- shortened version of the relational strings
	 * @return
	 */
	public static String unordered(Interval i1, Interval i2, boolean shortVersion) { 
		if (i1.start == i2.start && i1.end == i2.end)
			return (shortVersion ? "eq" : "equals");
		
		if (i1.start == i2.start && i1.end < i2.end)  
			return (shortVersion ? "s" : "starts-with");
		if (i1.start == i2.start && i1.end > i2.end)
			return (shortVersion ? "is" : "i-starts-with");
		
		if (i1.end == i2.end && i1.start < i2.start)
			return (shortVersion ? "e" : "ends-with");
		if (i1.end == i2.end && i1.start > i2.start)
			return (shortVersion ? "ie" : "i-ends-with");
		
		if (i1.start < i2.start && i1.end < i2.end && i1.end > i2.start)
			return (shortVersion ? "o" : "overlaps");
		if (i2.start < i1.start && i2.end < i1.end && i2.end > i1.start)
			return (shortVersion ? "io" : "i-overlaps");
		
		if (i1.start < i2.start && i1.end == i2.start)
			return (shortVersion ? "m" : "meets");
		if (i2.start < i1.start && i2.end == i1.start)
			return (shortVersion ? "im" : "i-meets");
	
		if (i1.start < i2.start && i1.end > i2.end)
			return (shortVersion ? "d" : "i-during");
		if (i2.start < i1.start && i2.end > i1.end)
			return (shortVersion ? "id" : "during");
		
		if (i1.end < i2.start)
			return (shortVersion ? "b" : "before");
		if (i2.end < i1.start)
			return (shortVersion ? "ib" : "i-before");

		throw new RuntimeException("Unable to find relation between : " + i1.toString() + " " + i2.toString());
	}
	
	public static void main(String[] args) { 
		Interval i1 = new Interval("a", 0, 10);
		Interval i2 = new Interval("b", 0, 10);
		
		System.out.println("equals : " + unordered(i1, i2));
		System.out.println("equals : " + unordered(i2, i1));
		
		i2 = new Interval("b", 0, 5);
		System.out.println("i-starts-with : " + unordered(i1, i2));
		System.out.println("starts-with : " + unordered(i2, i1));
		
		i2 = new Interval("b", 5, 10);
		System.out.println("ends-with : " + unordered(i1, i2));
		System.out.println("i-ends-with : " + unordered(i2, i1));
		
		i2 = new Interval("b", 5, 15);
		System.out.println("overlaps : " + unordered(i1, i2));
		System.out.println("i-overlaps : " + unordered(i2, i1));
		
		i2 = new Interval("b", 10, 15);
		System.out.println("meets : " + unordered(i1, i2));
		System.out.println("i-meets : " + unordered(i2, i1));
		
		i2 = new Interval("b", 5, 8);
		System.out.println("during : " + unordered(i1, i2));
		System.out.println("i-during : " + unordered(i2, i1));

		i2 = new Interval("b", 12, 15);
		System.out.println("before : " + unordered(i1, i2));
		System.out.println("i-before : " + unordered(i2, i1));
	}
}