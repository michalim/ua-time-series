package edu.arizona.cs.learn.timeseries.recognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.arizona.cs.learn.algorithm.bpp.BPPFactory;
import edu.arizona.cs.learn.algorithm.recognition.BPPNode;
import edu.arizona.cs.learn.algorithm.recognition.FSMFactory;
import edu.arizona.cs.learn.algorithm.recognition.FSMRecognizer;
import edu.arizona.cs.learn.timeseries.experiment.BitPatternGeneration;
import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.timeseries.model.signature.CompleteSignature;
import edu.arizona.cs.learn.timeseries.model.signature.Signature;
import edu.arizona.cs.learn.timeseries.model.symbols.StringSymbol;
import edu.arizona.cs.learn.timeseries.model.symbols.Symbol;
import edu.arizona.cs.learn.util.graph.Edge;
import edu.uci.ics.jung.graph.DirectedGraph;

public enum Recognizer {
	cave {
		@Override
		public FSMRecognizer build(
				String key, String signatureFile,
				List<Instance> training, List<Integer> test,
				int minPct, boolean onlyStart) {
			Signature s = Signature.fromXML(signatureFile);
			return build(key, s, minPct, onlyStart);
		}

		@Override
		public FSMRecognizer build(String key, Signature s, int minPct, boolean onlyStart) {
			double pct = minPct / 100.0D;

			CompleteSignature cs = (CompleteSignature) s;
			
			// Daniel: I changed this to a floor to avoid getting empty FSMs
			int minSeen = (int) Math.floor(s.trainingSize() * pct);
			s = s.prune(minSeen);

			// Assumption is that you will only build FSMRecognizer on 
			// StringSymbols....for now
			// TODO: extend to work with ComplexSymbols (but all binary)
			Set<Integer> propSet = new TreeSet<Integer>();
			for (Symbol obj : s.signature()) {
				StringSymbol ss = (StringSymbol) obj;
				propSet.addAll(ss.getProps());
			}
			List<Integer> props = new ArrayList<Integer>(propSet);
			List<List<Interval>> all = BitPatternGeneration.getBPPs(null, cs.table(), propSet);

			DirectedGraph<BPPNode, Edge> graph = FSMFactory.makeGraph(props, all, onlyStart);
			return new FSMRecognizer(key, graph);
		}
	},
	regular {
		@Override
		public FSMRecognizer build(
				String key, String signatureFile,
				List<Instance> training, List<Integer> test,
				int minPct, boolean onlyStart) {

			List<List<Interval>> bpps = new ArrayList<List<Interval>>();
			Set<Integer> propSet = new TreeSet<Integer>();
			for (Instance instance : training) { 
				if (test.indexOf(instance.id()) != -1) {
					continue;
				}
				
				// compress the instance .
				bpps.add(BPPFactory.compress(instance.intervals(), Interval.eff));
				for (Interval interval : instance.intervals()) {
					propSet.add(interval.keyId);
				}
			}
			List<Integer> props = new ArrayList<Integer>(propSet);
			DirectedGraph<BPPNode, Edge> graph = FSMFactory.makeGraph(props, bpps, onlyStart);

			FSMFactory.toDot(graph, "data/graph/tmp-" + key + ".dot");

			return new FSMRecognizer(key, graph);
		}

		@Override
		public FSMRecognizer build(String key, Signature s, int minPct, boolean onlyStart) {
			throw new RuntimeException("Not yet implemented!!");
		}

	};
	
	public abstract FSMRecognizer build(String key, Signature s, int minPct, boolean onlyStart);

	public abstract FSMRecognizer build(String key, String signatureFile,
			List<Instance> training, List<Integer> test,
			int minPct, boolean onlyStart);
}