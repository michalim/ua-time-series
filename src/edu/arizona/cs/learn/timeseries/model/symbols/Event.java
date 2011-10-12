package edu.arizona.cs.learn.timeseries.model.symbols;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.util.DataMap;

/**
 * The event class essentially wraps the interval class so that we don't have to
 * make Intervals implement Symbol
 * 
 * @author wkerr
 * 
 */

public class Event extends StringSymbol {
	private String _key;
	private List<Integer> _props;
	private List<Interval> _intervals;

	public Event(Interval interval) {
		this(DataMap.getKey(interval.keyId), interval);
	}

	public Event(String name, Interval interval) {
		this(name, interval, 1.0);
	}
	
	public Event(String name, Interval interval, double weight) { 
		_key = name;
		_weight = weight;

		_props = new ArrayList<Integer>();
		_props.add(interval.keyId);

		_intervals = new ArrayList<Interval>();
		_intervals.add(interval);
	}

	public boolean equals(Object o) {
		if (!(o instanceof Event)) {
			return false;
		}
		Event event = (Event) o;
		return this._key.equals(event._key);
	}

	public List<Interval> getIntervals() {
		return this._intervals;
	}

	public String getKey() {
		return this._key;
	}

	public List<Integer> getProps() {
		return _props;
	}

	public String toString() {
		return this._key;
	}
	
	@Override
	public Symbol copy() { 
		return new Event(_key, _intervals.get(0), _weight);
	}
	
	@Override
	public Symbol merge(Symbol B) { 
		if (!(B instanceof Event))
			throw new RuntimeException("Must combine events with other events: " + B.getClass().getName());
		return new Event(_key, _intervals.get(0), _weight+B.weight());
	}	

	public void toXML(Element e) {
		Element evt = e.addElement("symbol")
			.addAttribute("class", "Event")	
			.addAttribute("key", _key)
			.addAttribute("weight", _weight+"");

		_intervals.get(0).toXML(evt);
	}

	public static Event fromXML(Element e) {
		String key = e.attributeValue("key");
		double weight = Double.parseDouble(e.attributeValue("weight"));
		Interval i = Interval.fromXML(e.element("Interval"));
		
		return new Event(key, i, weight);
	}
}