package edu.arizona.cs.learn.util.graph;

import java.util.Set;
import java.util.TreeSet;

import edu.arizona.cs.learn.util.DataMap;
import edu.arizona.cs.learn.util.Utils;

public class Edge {
	private Set<Integer> _props;
	
	private String _key;
	private String _label;
	private double _count;

	public Edge(Set<Integer> props) {
		_props = new TreeSet<Integer>(props);

		StringBuffer buf = new StringBuffer();
		StringBuffer key = new StringBuffer("[");
		for (Integer propId : _props) {
			if (buf.length() > 0) {
				buf.append("\\n");
			}
			if (key.length() > 1)
				key.append(" ");
			
			String prop = DataMap.getKey(propId);
			buf.append(prop);
			key.append(prop);
		}
		_label = buf.toString();
		_key = (key.toString() + "]");

		_count = 0.0D;
	}

	public Edge(String values) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < values.length(); i++) {
			buf.append(values.charAt(i) + "\\n");
		}
		_label = buf.toString();
		_count = 0.0D;
	}

	public Edge(double count) {
		_count = count;
	}

	public Set<Integer> props() {
		return _props;
	}

	public void increment() {
		_count += 1.0D;
	}

	public void increment(double count) {
		_count += count;
	}

	public double count() {
		return _count;
	}

	public boolean satisfied(Set<String> props) {
		return props.containsAll(_props);
	}

	public String toDot(boolean edgeProb, double prob) {
		if (!edgeProb) {
			return " [label=\"" + this._label + "\"] ";
		}
		return " [label=\"" + this._label + "\\n" + Utils.nf.format(prob) + "\"] ";
	}
}