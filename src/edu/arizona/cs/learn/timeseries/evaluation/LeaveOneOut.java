package edu.arizona.cs.learn.timeseries.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import edu.arizona.cs.learn.timeseries.classification.Classifier;
import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.util.Utils;

/**
 * LeaveOneOut is a performance evaluation technique.  The dataset
 * is split between training and testing --- 1 instance from each class
 * is kept out as part of the test set and all others are part of the 
 * training set.
 * @author wkerr
 *
 */
public class LeaveOneOut extends ClassificationTest {
	
	public LeaveOneOut() { 

	}

	/**
	 * run LeaveOneOut on the data and automatically partition
	 * the data into training and test sets.
	 * @param seed - so that you can control the splitting of k
	 * @params classNames - so that the classNames order is unique
	 * @param data
	 * @param c
	 * @return
	 */
	public List<BatchStatistics> run(long seed, List<String> classNames, Map<String,List<Instance>> data, Classifier c) { 
		_execute = Executors.newFixedThreadPool(Utils.numThreads);
		List<BatchStatistics> foldStats = new ArrayList<BatchStatistics>();

		// First make a list of all of the Instances and we will simply rotate through them.
		List<Instance> allInstances = new ArrayList<Instance>();
		for (List<Instance> list : data.values()) {
			allInstances.addAll(list);
		}
		
		for (int i = 0; i < allInstances.size(); ++i) { 
			// Construct the training and testing sets...
			Map<String,List<Instance>> train = new HashMap<String,List<Instance>>();
			Map<String,List<Instance>> test = new HashMap<String,List<Instance>>();
			for (String className : classNames) { 
				train.put(className, new ArrayList<Instance>());
				test.put(className, new ArrayList<Instance>());
			}

			Instance testInstance = allInstances.get(i);
			test.get(testInstance.name()).add(testInstance);
			
			for (int j = 0; j < allInstances.size(); ++j) { 
				if (i == j)
					continue;
				
				Instance trainInstance = allInstances.get(j);
				train.get(trainInstance.name()).add(trainInstance);
			}
			
			foldStats.add(runBatch(i, c, classNames, train, test));
		}
		_execute.shutdown();
		return foldStats;
	}
}
