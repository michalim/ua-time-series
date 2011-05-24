package edu.arizona.cs.learn.timeseries.classification;

import java.util.List;
import java.util.Map;

import edu.arizona.cs.learn.timeseries.model.Instance;

public abstract class Classifier {
	protected ClassifyParams _params;
	
	public Classifier(ClassifyParams params) { 
		_params = params;
	}
	
	public abstract String getName();

	// return the amount of time it took to performing training
	// organized by class name.
	public abstract Map<String,Long> train(int fold, Map<String,List<Instance>> training);
	public abstract Map<String,Long> train(Map<String,List<Instance>> training);

	public abstract String test(Instance testInstance);
}