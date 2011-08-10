package edu.arizona.cs.learn.timeseries.experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import edu.arizona.cs.learn.timeseries.classification.Classifier;
import edu.arizona.cs.learn.timeseries.classification.Classify;
import edu.arizona.cs.learn.timeseries.classification.ClassifyCallable;
import edu.arizona.cs.learn.timeseries.classification.ClassifyParams;
import edu.arizona.cs.learn.timeseries.evaluation.BatchStatistics;
import edu.arizona.cs.learn.timeseries.evaluation.LeaveOneOut;
import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.SequenceType;
import edu.arizona.cs.learn.util.Utils;

/**
 *
 * @author wkerr
 *
 */
public class KinectExperiments {

	/**
	 * 
	 * @param map
	 * @param count
	 * @return
	 */
	public static Map<String,List<Instance>> subset(Map<String,List<Instance>> map, int count) { 

		Map<String,List<Instance>> sMap = new HashMap<String,List<Instance>>();
		
		for (String key : map.keySet()) { 
			List<Instance> instances = map.get(key);
			if (instances.size() < count)
				continue;
			
			Collections.shuffle(instances);
			List<Instance> sInstances = new ArrayList<Instance>();
			for (int i = 0; i < count; ++i) { 
				sInstances.add(instances.get(i));
			}
			
			sMap.put(key, sInstances);
		}
		
		return sMap;
	}
	
	/**
	 * Calculate the performance of the CAVE classifier on
	 * the kinect data....
	 * Assumption: maria -- you need to name your files accordingly
	 * 		kinect-activity1.lisp
	 * 		kinect-activity2.lisp
	 * 		...
	 * where activity1 and activity2 are the names of the behaviors/actions/activities
	 * that you are trying to learn.  Each file will contain all 10 examples of the 
	 * behavior/action/activity
	 * 
	 * The files *must* be placed into the data/input/ folder relative to this project
	 * for this piece of code to work.
	 * @param prefix
	 */
	public static void performance() { 
		Utils.LIMIT_RELATIONS = true;
		Utils.WINDOW = 5;
		
		SequenceType type = SequenceType.allen;
		
		Map<String,List<Instance>> data = Utils.load("/Users/wkerr/psi/maria/fluents/", "Deva", type);
		data = subset(data, 10);
		
		List<String> classNames = new ArrayList<String>(data.keySet());
		Collections.sort(classNames);
		
		ClassifyParams params = new ClassifyParams();
		params.type = type;

		// Vary the prunePct to get different results.
//		params.prunePct = 0.5;
//		params.incPrune = true;
//		Classifier c = Classify.prune.getClassifier(params);

		// Comment out the above line and comment in this line
		// to use nearest neighbor classification instead of CAVE classification.
		params.k = 1;
		Classifier c = Classify.knn.getClassifier(params);

//		SplitAndTest sat = new SplitAndTest(10, 2.0/3.0);
//		List<BatchStatistics> stats = sat.run(System.currentTimeMillis(), classNames, data, c);
		LeaveOneOut loo = new LeaveOneOut();
		List<BatchStatistics> stats = loo.run(System.currentTimeMillis(), classNames, data, c);
		
		// For now let's print out some informative stuff and build
		// one confusion matrix
		SummaryStatistics perf = new SummaryStatistics();
		int[][] matrix = new int[classNames.size()][classNames.size()];
		for (int i = 0; i < stats.size(); ++i) { 
			BatchStatistics fs = stats.get(i);
			double accuracy = fs.accuracy();
			int[][] confMatrix = fs.confMatrix();

			perf.addValue(accuracy);
			System.out.println("Fold - " + i + " -- " + accuracy);
			
			for (int j = 0; j < classNames.size(); ++j) { 
				for (int k = 0; k < classNames.size(); ++k) { 
					matrix[j][k] += confMatrix[j][k];
				}
			}
		}
		System.out.println("Performance: " + perf.getMean() + " sd -- " + perf.getStandardDeviation());
		
		// Now print out the confusion matrix (in csv format)
		System.out.println(Utils.toCSV(classNames, matrix));
	}
	
	public static void main(String[] args) throws Exception { 
		performance();
	}
	
	public static void darpaEvaluation() {
		Utils.LIMIT_RELATIONS = true;
		Utils.WINDOW = 5;
		
		SequenceType type = SequenceType.allen;
		Map<String,List<Instance>> training = Utils.load("<directory>", "<prefix>", type);
		Map<String,List<Instance>> test = Utils.load("<directory>", "<prefix>", type);
		
		List<String> classNames = new ArrayList<String>(training.keySet());
		Collections.sort(classNames);
		
		ClassifyParams params = new ClassifyParams();
		params.type = type;
		params.k = 1;
		Classifier c = Classify.knn.getClassifier(params);

		// Perform the training.
		c.train(training);
		
		// Now let's do a test....
		ExecutorService execute = Executors.newFixedThreadPool(Utils.numThreads);;

		BatchStatistics fs = new BatchStatistics(c.getName(), classNames);
		List<Future<ClassifyCallable>> futureList = new ArrayList<Future<ClassifyCallable>>();
		for (List<Instance> instances : test.values())
			for (Instance instance : instances) 
				futureList.add(execute.submit(new ClassifyCallable(c, instance)));
		
		for (Future<ClassifyCallable> future : futureList) {
			try {
				ClassifyCallable results = future.get();
				fs.addTestDetail(results.actual(), results.predicted(), results.duration());
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}	
		System.out.println("Accuracy: " + fs.accuracy());
	}
	
}
