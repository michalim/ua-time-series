package edu.arizona.cs.learn.timeseries.model.symbols;

import org.dom4j.Element;

/**
 * The ProportionSymbol is used to keep track of the amount
 * of time that a proposition has been on during any particular
 * instance.  
 * 
 * 
 * @author kerrw
 *
 */
public class ProportionSymbol extends Symbol {

	private int _propId;
	
	private int _timeOn;
	private int _duration;
	
	public ProportionSymbol(int propId, int timeOn, int duration) {
		_propId = propId;
		
		_timeOn = timeOn;
		_duration = duration;
	}
	
	/**
	 * Return the numeric identifier for the proposition.
	 * @return
	 */
	public int propId() { 
		return _propId;
	}
	
	/**
	 * Return the amount of time that this symbol has been on (true).
	 * @return
	 */
	public int timeOn() { 
		return _timeOn;
	}
	
	/**
	 * Return the amount of time in the instance that led to this
	 * symbol being created.
	 * @return
	 */
	public int duration() { 
		return _duration;
	}
	
	@Override
	public Symbol copy() {
		return new ProportionSymbol(_propId, _timeOn, _duration);
	}
	
	@Override
	public Symbol merge(Symbol s) {
		throw new RuntimeException("You are not allowed to merge ProportionSymbols!");
	}

	@Override
	public void toXML(Element e) {
		throw new RuntimeException("Not yet implemented!!");
	}

	@Override
	public String latex() {
		throw new RuntimeException("Not yet implemented!!");
	}

}
