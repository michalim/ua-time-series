package edu.arizona.cs.learn.timeseries.classification;

import java.util.List;
import java.util.Map;

import edu.arizona.cs.learn.timeseries.model.Instance;

public abstract class Classifier {
	protected ClassifyParams _params;
	
	public Classifier(ClassifyParams params) { 
		_params = params;
	}
	
	/**
	 * Return the name of this classifier.
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Train is called on each classifier once during each batch of
	 * test we perform.  We give the batchId so that, if necessary,
	 * the system can load precomputed results for that batch in the
	 * event that training is incredibly slow.
	 * @param batchId
	 * @param trainingSet
	 * @return
	 * 	We return the amount of time it took to perform training in a map 
	 *  with the key being the class name.
	 */
	public abstract Map<String,Long> train(int batchId, Map<String,List<Instance>> trainingSet);
	

	/**
	 * Called when we want to test an unlabeled instance
	 * @param testInstance
	 * @return
	 * 	  the classifier assigned class name for this instance.
	 */
	public abstract String test(Instance testInstance);
}