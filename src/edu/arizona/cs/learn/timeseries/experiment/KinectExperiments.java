package edu.arizona.cs.learn.timeseries.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import edu.arizona.cs.learn.algorithm.alignment.Normalize;
import edu.arizona.cs.learn.algorithm.alignment.Params;
import edu.arizona.cs.learn.algorithm.alignment.SequenceAlignment;
import edu.arizona.cs.learn.timeseries.classification.Classifier;
import edu.arizona.cs.learn.timeseries.classification.Classify;
import edu.arizona.cs.learn.timeseries.classification.ClassifyParams;
import edu.arizona.cs.learn.timeseries.classification.Distance;
import edu.arizona.cs.learn.timeseries.evaluation.BatchStatistics;
import edu.arizona.cs.learn.timeseries.evaluation.CrossValidation;
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
	public static void performance(String dataName) { 
		Utils.LIMIT_RELATIONS = true;
		Utils.WINDOW = 5;

		SequenceType type = SequenceType.allen;

		Map<String,List<Instance>> data = Utils.load("/data/input/", dataName, type);
		//		data = subset(data, 10);

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
		//		LeaveOneOut loo = new LeaveOneOut();
		//		List<BatchStatistics> stats = loo.run(System.currentTimeMillis(), classNames, data, c);


		CrossValidation cv = new CrossValidation(5);
		List<BatchStatistics> stats = cv.run(System.currentTimeMillis(), classNames, data, c);

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
		String option=args[0];
		String TrainName=args[1];
		String TestName=args[2];
		String oc="C";
		if (option.toUpperCase().equals(oc)) {
			// do cross validation
			performance(TrainName);
		}
		else {	
			// do classification
			darpaEvaluation(TrainName,TestName);
		}	
	}

	public static void darpaEvaluation(String trainName, String testName) {
		Utils.LIMIT_RELATIONS = true;
		Utils.WINDOW = 5;

		int k = 6;

		SequenceType type = SequenceType.allen;
		Map<String,List<Instance>> trainingMap = Utils.load("data/input","DevaSmooth2" , type);
		// combine all of the training data into a single list
		List<Instance> training = new ArrayList<Instance>();
		for (List<Instance> tmp : trainingMap.values()) {
			training.addAll(tmp);
		}

		List<String> testingFiles = new ArrayList<String>();

		for (File f : new File("data/input/").listFiles()) {

			if ((f.getName().startsWith(testName)) && (f.getName().endsWith("lisp"))) {
				System.out.println(f.getName());
				System.out.println(f.getAbsolutePath());
				testingFiles.add("data/input/"+f.getName());

			}
		}

		//				testingFiles.add("data/input/T2C_10_LEAVE10_A1_C1_id1.prop18.lisp");		
		//				testingFiles.add("data/input/T2C_10_LEAVE10_A1_C2_id1.prop10.lisp");	

		// Now let's test all of the files.
		ExecutorService execute = Executors.newFixedThreadPool(Utils.numThreads);;

		List<Future<ACallable>> futureList = new ArrayList<Future<ACallable>>();
		for (String file : testingFiles) { 
			// Based on conversations with Maria and Raquel -- There is only a single test per file.
			Instance test = Instance.load(new File(file)).get(0);
			test.sequence(SequenceType.allen.getSequence(test.intervals()));
			futureList.add(execute.submit(new ACallable(training, file, test, k)));
		}


		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("WindowClass2"+System.currentTimeMillis()+".txt"));	   

			for (Future<ACallable> future : futureList) {
				try {
					ACallable results = future.get();

					// Here is where you output the information that you want
					// to output for each test....
					System.out.print(results.file() + "xxxxxx ");
					out.write(results.file()+',');
					for (Distance d : results.results()) { 
						// print out the class name and the distance to the class being tested
						System.out.print(" " + d.instance.label() + " ");
						out.write(d.instance.label());	
					}
					out.write('\n');
					System.out.println();
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}

			out.close();

		} catch (IOException e) {
		}

		// no computation of accuracy since there isn't really a test.
	}
}

/**
 * Allows us to multithread the KNN classifier.  Each ACallable
 * represents a single test.
 * @author wkerr
 *
 */
class ACallable implements Callable<ACallable> {

	private String _file;

	private List<Instance> _training;
	private Instance _test;

	private int _k;

	private List<Distance> _results;


	public ACallable(List<Instance> training, String testFile, Instance test, int k) { 
		_training = training;
		_file = testFile;
		_test = test;
		_k = k;
	}

	@Override
	public ACallable call() throws Exception {
		Params params = new Params();
		params.setMin(0, 0);
		params.setBonus(1.0D, 1.0D);
		params.setPenalty(0.0D, 0.0D);
		params.normalize = Normalize.regular;

		_results = new ArrayList<Distance>();
		for (Instance tmp : _training) {
			params.seq1 = _test.sequence();
			params.seq2 = tmp.sequence();

			double distance = SequenceAlignment.distance(params);
			_results.add(new Distance(tmp, distance));
		}

		Collections.sort(_results, new Comparator<Distance>() {
			public int compare(Distance o1, Distance o2) {
				return Double.compare(o1.d, o2.d);
			}
		});

		while (_results.size() > _k) { 
			_results.remove(_results.size()-1);
		}
		return this;
	} 

	/**
	 * Return the file that contains this test instance.
	 * @return
	 */
	public String file() { 
		return _file;
	}

	/**
	 * Return the results after computing the nearest neighbors.
	 * @return
	 */
	public List<Distance> results() { 
		return _results;
	}
}