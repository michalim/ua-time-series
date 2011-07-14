package edu.arizona.cs.learn.timeseries.model.symbols;

import java.util.List;

import org.dom4j.Element;

import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.util.Utils;

public class IntervalAndSequence extends Symbol {

	private Interval _interval;
	private List<AllenRelation> _seqeunce;
	
	public IntervalAndSequence() { 
		
	}

	@Override
	public Symbol merge(Symbol s) {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	public String latex() {
		throw new RuntimeException("Not yet implemented!");
	}
	
	@Override
	public Symbol copy() {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	public void toXML(Element e) {
		throw new RuntimeException("Not yet implemented!");
	}
	
}
