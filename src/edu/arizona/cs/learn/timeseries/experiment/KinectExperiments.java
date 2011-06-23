package edu.arizona.cs.learn.timeseries.experiment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import edu.arizona.cs.learn.timeseries.classification.Classifier;
import edu.arizona.cs.learn.timeseries.classification.Classify;
import edu.arizona.cs.learn.timeseries.classification.ClassifyParams;
import edu.arizona.cs.learn.timeseries.evaluation.BatchStatistics;
import edu.arizona.cs.learn.timeseries.evaluation.CrossValidation;
import edu.arizona.cs.learn.timeseries.evaluation.LeaveOneOut;
import edu.arizona.cs.learn.timeseries.evaluation.SplitAndTest;
import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.SequenceType;
import edu.arizona.cs.learn.util.Utils;

/**
 * These experiments are run on the data prepared for
 * me by Federico and have been gathered from the brain
 * hats that Carole owns.
 * @author wkerr
 *
 */
public class KinectExperiments {

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
		Map<String,List<Instance>> data = Utils.load("kinect", SequenceType.allen);
		List<String> classNames = new ArrayList<String>(data.keySet());
		Collections.sort(classNames);
		
		ClassifyParams params = new ClassifyParams();
		params.type = SequenceType.allen;
		// Vary the prunePct to get different results.
		params.prunePct = 0.5;
		params.incPrune = false;
		Classifier c = Classify.prune.getClassifier(params);

		// Comment out the above line and comment in this line
		// to use nearest neighbor classification instead of CAVE classification.
//		params.k = 1;
//		Classifier c = Classify.knn.getClassifier(params);

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
	
}
