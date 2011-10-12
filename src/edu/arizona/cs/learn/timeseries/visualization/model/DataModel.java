package edu.arizona.cs.learn.timeseries.visualization.model;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.arizona.cs.learn.algorithm.bpp.BPPFactory;
import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.timeseries.model.SequenceType;
import edu.arizona.cs.learn.timeseries.model.Signature;
import edu.arizona.cs.learn.util.DataMap;
import edu.arizona.cs.learn.util.Utils;

public class DataModel {
    private static Logger logger = Logger.getLogger(DataModel.class);

    protected boolean _finish = true;
    
    protected int _uniqueCount = 0;
	
	protected SequenceType _sequenceType;
	
	/** maps the episode id to the index in the episode list */
	protected List<Instance> _instances;
	protected Signature _signature;

	protected int _currentId;
	protected Instance _instance;
	protected Set<String> _propSet;

	protected List<Interval> _compressed;
	
	public DataModel() {
		// the default is Allen sequences.
		_sequenceType = SequenceType.allen;
	}

	/**
	 * Load in all of the episodes found in some file.
	 * @param file
	 */
	public void load(File file) { 
		_instances = Instance.load(file);
	}
	
	/**
	 * Load in a specific signature in order to see heatmaps
	 * for the given Signature.
	 * @param file
	 */
	public void loadSignature(File file) { 
		_signature = Signature.fromXML(file.getAbsolutePath());
	}
	
	/**
	 * The the active episode for the DataModel
	 * @param id
	 */
	public void set(int id) { 
		_currentId = id;
		_instance = getInstance(id);
		_instance.sequence(_sequenceType);
		_compressed = BPPFactory.compress(_instance.intervals(), Interval.eff);
		
		_propSet = new TreeSet<String>();
		for (Interval interval : _instance.intervals())
			_propSet.add(DataMap.getKey(interval.keyId));
	}
	
	/**
	 * Retrieve the episode with the given id
	 * @param id
	 * @return
	 */
	public Instance getInstance(int id) { 
		Instance result = null;
		for (Instance instance : _instances) {
			if (instance.id() == id) {
				result = instance;
				break;
			}
		}
		return result;
	}
	
	/**
	 * @return
	 */
	public List<Instance> instances() { 
		return _instances;
	}
	
	/**
	 * Returns the signature associated with this
	 * DataModel
	 */
	public Signature signature() {
		return _signature;
	}
	
	/**
	 * Set the sequence type of this model.
	 * @param type
	 */
	public void sequenceType(SequenceType type) {
		_sequenceType = type;
	}
	
	/**
	 * return the current sequence type of this model
	 * @return
	 */
	public SequenceType sequenceType() { 
		return _sequenceType;
	}
	
	/**
	 * Retrieve the original list of Intervals for an episode
	 * @return
	 */
	public List<Interval> intervals() { 
		return _instance.intervals();
	}
	
	/**
	 * Retrieve the compressed list of intervals for an episode
	 * @return
	 */
	public List<Interval> compressed() { 
		return _compressed;
	}
	
	/**
	 * Return the set of propositions in the currently
	 * selected episode
	 * @return
	 */
	public Set<String> propSet() { 
		return _propSet;
	}
	
	/**
	 * Return the sequence associated with the selected episode
	 * @return
	 */
	public Instance instance() { 
		return _instance;
	}
}
