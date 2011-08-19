package edu.arizona.cs.learn.timeseries.model.symbols;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.arizona.cs.learn.algorithm.alignment.Normalize;
import edu.arizona.cs.learn.algorithm.alignment.Params;
import edu.arizona.cs.learn.algorithm.alignment.Report;
import edu.arizona.cs.learn.algorithm.alignment.SequenceAlignment;
import edu.arizona.cs.learn.algorithm.alignment.Similarity;
import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.util.Utils;

/**
 * A complex representation of a symbol.  Essentially, this treats the
 * list of intervals as a tree structure.  Each interval is in some sequence
 * of Allen relations with *all* of the other Intervals.  This symbol contains
 * both the interval and this sequence of Allen relations.
 * @author wkerr
 *
 */
public class IntervalAndSequence extends Symbol {
    private static Logger logger = Logger.getLogger(IntervalAndSequence.class);

    private String _proposition;
	
	/** Although defined as a list of Symbols, each is expected to be an Allen relation */
	private List<Symbol> _sequence;
	
	/**
	 * Create an instance of IntervalAndSequence representation
	 * of the time series.
	 * @param intervals
	 * @param index
	 */
	public IntervalAndSequence(List<Interval> intervals, int index) { 
		super(1.0);
		
		_proposition = intervals.get(index).name;
		_sequence = new ArrayList<Symbol>();
		
		Interval interval = intervals.get(index);
		for (int i = 0; i < intervals.size(); ++i) { 
			if (i == index)
				continue;
			
			Interval tmp = intervals.get(i);
			if (!Utils.LIMIT_RELATIONS || interval.overlaps(tmp, Utils.WINDOW)) {
				_sequence.add(new AllenRelation(AllenRelation.unordered(interval, tmp), interval, tmp, 1));
			}

		}
		
		// print out the sequence so that we can be sure of what we have.
//		logger.debug("  Symbol: " + index);
//		logger.debug("    Proposition: " + _proposition);
//		for (Symbol s : _sequence) { 
//			logger.debug("      Symbol: " + s.toString());
//		}
	}
	
	/**
	 * A convenience constructor when you know all of the necessary information
	 * in order to construct this object.
	 * @param proposition
	 * @param sequence
	 * @param weight
	 */
	public IntervalAndSequence(String proposition, List<Symbol> sequence, double weight) { 
		super(weight);
		
		_proposition = proposition;
		_sequence = sequence;
	}
	
	/**
	 * Return the proposition associated with this IntervalAndSequence
	 * @return
	 */
	public String proposition() { 
		return _proposition;
	}
	
	/**
	 * Return the sequence associated with this IntervalAndSequence
	 * @return
	 */
	public List<Symbol> sequence() { 
		return _sequence;
	}

	@Override
	public Symbol merge(Symbol s) {
		if (!(s instanceof IntervalAndSequence)) 
			throw new RuntimeException("Cannot merge an IntervalAndSequence with a " + s.getClass().getName());
		
		IntervalAndSequence ias = (IntervalAndSequence) s;
		double weight = weight() + s.weight();
		
		Params p = new Params(_sequence, ias._sequence);
		p.setMin(0, 0);
		p.setBonus(1, 1);
		p.setPenalty(-1, -1);
		p.similarity = Similarity.strings;
		p.normalize = Normalize.regular;
		
		Report r = SequenceAlignment.align(p);
		List<Symbol> seq = new ArrayList<Symbol>();
		for (int i = 0; i < r.results1.size(); i++) {
			Symbol item1 = r.results1.get(i);
			Symbol item2 = r.results2.get(i);
			
			if (item1 == null && item2 == null)
				throw new RuntimeException("Alignment cannot choose to align two null items!");

			if (item1 != null && item2 != null) 
				seq.add(item1.merge(item2));
			else if (item1 != null)
				seq.add(item1.copy());
			else
				seq.add(item2.copy());
		}
		
		return new IntervalAndSequence(_proposition, seq, weight);
	}

	@Override
	public String latex() {
		throw new RuntimeException("Not yet implemented!");
	}
	
	@Override
	public Symbol copy() {
		
		List<Symbol> seq = new ArrayList<Symbol>();
		for (Symbol s : _sequence)
			seq.add(s.copy());
		return new IntervalAndSequence(_proposition, seq, _weight);
	}
	
	@Override
	public void prune(int min) { 
		List<Symbol> seq = new ArrayList<Symbol>();
		for (Symbol s : _sequence) { 
			if (s.weight() > min)
				seq.add(s);
		}
		_sequence = seq;
	}

	@Override
	public void toXML(Element e) {
		Element ias = e.addElement("symbol")
			.addAttribute("class", "IntervalAndSequence");

		ias.addAttribute("weight", _weight+"");
		ias.addAttribute("proposition", _proposition);
		Element sequence = ias.addElement("sequence");
		for (Symbol s : _sequence)
			s.toXML(sequence);
	}
	
	public String toString() { 
		return _proposition + " " + _sequence;
	}
}
