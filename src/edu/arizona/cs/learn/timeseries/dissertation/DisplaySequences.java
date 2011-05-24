package edu.arizona.cs.learn.timeseries.dissertation;

import java.io.File;
import java.util.Collections;
import java.util.List;

import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.timeseries.model.SequenceType;
import edu.arizona.cs.learn.timeseries.model.symbols.AllenRelation;
import edu.arizona.cs.learn.timeseries.model.symbols.Symbol;

public class DisplaySequences {

	public static void main(String[] args) { 
		SignatureExample.init();
		
		String file = "data/input/chpt1-approach.lisp";
		AllenRelation.text = AllenRelation.fullText;
		SequenceType type = SequenceType.allen;
		
		List<Instance> instances = Instance.load(new File(file));
		for (Instance instance : instances) { 
			Collections.sort(instance.intervals(), Interval.eff);
			
			// Let's print the intervals...
			for (Interval i : instance.intervals()) { 
				System.out.println("-------" + i);
			}
			
			List<Symbol> sequence = type.getSequence(instance.intervals());
			System.out.println("Sequence: " + instance.id());
			for (Symbol obj : sequence) { 
				System.out.println("  " + obj.toString());
			}
		}
	}
	
}
