package edu.arizona.cs.learn.timeseries.model;

import java.io.File;
import java.io.FileReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.clustering.Clusterable;
import org.apache.log4j.Logger;

import edu.arizona.cs.learn.timeseries.evaluation.cluster.Clustering;
import edu.arizona.cs.learn.timeseries.model.symbols.Symbol;
import edu.arizona.cs.learn.util.LispReader;

public class Instance implements Clusterable<Instance> {
	private static Logger logger = Logger.getLogger(Instance.class);
	private int _id;
	private int _uniqueId;
		
	/** The classification label for this instance */
	private String _label;
	
	private String _stringId;
	
	private List<Interval> _intervals;
	private List<Symbol> _sequence;

	/**
	 * Create a new instance with the given label and id.
	 * @param label -- the supervised classification label.
	 * @param id -- an id
	 */
	public Instance(String label, int id) {
		_label = label;
		_id = id;
	}

	public Instance(String label, int id, List<Interval> intervals) {
		this(label, id);
		_intervals = intervals;
	}
	
	public Instance(String label, String stringId, List<Interval> intervals) { 
		this(label, 1, intervals);
		_stringId = stringId;
	}
	
	public String label() {
		return _label;
	}

	public int id() {
		return _id;
	}

	public int uniqueId() {
		return _uniqueId;
	}

	public void uniqueId(int uniqueId) {
		_uniqueId = uniqueId;
	}
	
	public String stringId() { 
		return _stringId;
	}
	
	public void stringId(String stringId) {
		_stringId = stringId;
	}

	/**
	 * Return the intervals that are part of this
	 * instance.
	 * @return
	 */
	public List<Interval> intervals() { 
		return _intervals;
	}
	
	/**
	 * Set the intervals that make up this instance.
	 * Useful when the original instance is compressed.
	 * @param intervals
	 */
	public void intervals(List<Interval> intervals) { 
		_intervals = intervals;
	}
	
	/**
	 * Set the internal sequence to the sequence generated
	 * from the given sequence type.
	 * @param type
	 */
	public void sequence(SequenceType type) { 
		if (_intervals == null) 
			throw new RuntimeException("Instance does not have any intervals!");
		_sequence = type.getSequence(_intervals);
	}
	/**
	 * Set the sequence for this instance, when it is known.
	 * @param sequence
	 */
	public void sequence(List<Symbol> sequence) { 
		_sequence = sequence;
	}
	
	/**
	 * Return the sequence
	 * @return
	 */
	public List<Symbol> sequence() {
		if (_sequence == null) 
			throw new RuntimeException("Haven't initialized the sequence yet!");
		return _sequence;
	}

	/**
	 * Return a copy of this Instance
	 * @return
	 */
	public Instance copy() {
		List<Symbol> seq = new ArrayList<Symbol>();
		for (Symbol obj : _sequence) {
			seq.add(obj.copy());
		}

		Instance copy = new Instance(_label, _id, _intervals);
		copy.sequence(seq);
		copy.uniqueId(_uniqueId);
		return copy;
	}

	public Instance centroidOf(Collection<Instance> cluster) {
		if (cluster == null) {
			logger.error("NULL cluster");
			return null;
		}

		if (cluster.size() == 0) {
			logger.error("Empty cluster");
			return null;
		}

		List<Instance> instances = new ArrayList<Instance>(cluster);
		double[] sumDistance = new double[instances.size()];

		for (int i = 0; i < instances.size(); i++) {
			Instance i1 = (Instance) instances.get(i);
			for (int j = i + 1; j < instances.size(); j++) {
				Instance i2 = (Instance) instances.get(j);

				double d = Clustering.distances[i1.uniqueId()][i2.uniqueId()];
				sumDistance[i] += d;
				sumDistance[j] += d;
			}
		}

		int index = 0;
		double minDistance = (1.0D / 0.0D);
		for (int i = 0; i < instances.size(); i++) {
			if (sumDistance[i] < minDistance) {
				index = i;
				minDistance = sumDistance[i];
			}
		}
		return (Instance) instances.get(index);
	}

	public double distanceFrom(Instance i) {
		return Clustering.distances[this._uniqueId][i.uniqueId()];
	}
	
	/**
	 * Load the file of intervals into an array of instances.
	 * @param file
	 * @return
	 */
	public static List<Instance> load(File file) { 
		String name = file.getName();
		if (!name.endsWith(".lisp"))
			throw new RuntimeException("Can only load files that end with .lisp");

		String label = name.substring(0, name.indexOf(".lisp"));
		return load(label, file);
	}
	
	/**
	 * Load in a list of instances from the given file.  Each instance
	 * is given the class label that is handed in.
	 * @param label
	 * @param file
	 * @return
	 */
	public static List<Instance> load(String label, File file) {
		return load(label, file, new HashMap<String,String>(), new HashSet<String>());
	}
	
	/**
	 * Load in a list of instances from the given file.  Each instance
	 * is given the class label that is handed in and each instance
	 * has their sequence constructed for them.
	 * @param label
	 * @param file
	 * @param type -- the type of sequence we should convert this list
	 * of intervals into.
	 * @return
	 */
	public static List<Instance> load(String label, File file, SequenceType type) {
		List<Instance> instances = load(label, file, new HashMap<String,String>(), new HashSet<String>());
		for (Instance instance : instances)
			instance.sequence(type);
		return instances;
	}	
	
	/**
	 * Load in a list of instances from the given file.  Each instance
	 * will be given the class label that is handed in.
	 * @param label
	 * @param file
	 * @param rewrite -- rewrite the propositions found in this map with
	 * the values stored with them.
	 * @param exclude -- ignore any intervals with the propositions
	 * found in the exclude set
	 * @return
	 */
	public static List<Instance> load(String label, File file, Map<String,String> rewrite, Set<String> excludeSet) { 
		List<Instance> instances = new ArrayList<Instance>();

		try { 
			FileReader fileReader = new FileReader(file);
			PushbackReader reader = new PushbackReader(fileReader);

			List<Object> episode = LispReader.read(reader);
			while (episode != null) {
				int id = (Integer) episode.get(0);
				List<Interval> intervalSet = new ArrayList<Interval>();

				List<Object> intervals = (List<Object>) episode.get(1);
				for (Object o : intervals) { 
					List<Object> list = (List<Object>) o;
					String name = (String) list.get(0);
					if (rewrite.containsKey(name))
						name = rewrite.get(name);
					int start = (Integer) list.get(1);
					int end = (Integer) list.get(2);
					
					Interval interval = new Interval(name, start, end);
					interval.file = file.getName();
					interval.episode = id;
					
					boolean add = true;
					for (String exclude : excludeSet) { 
						if (name.endsWith(exclude)) {
							add = false;
							break;
						}
					}

					if (add)
						intervalSet.add(interval);
				}

				instances.add(new Instance(label, id, intervalSet));

				episode = LispReader.read(reader);
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return instances;
	}
}