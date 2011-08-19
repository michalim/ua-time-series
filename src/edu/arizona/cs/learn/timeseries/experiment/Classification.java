package edu.arizona.cs.learn.timeseries.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import edu.arizona.cs.learn.algorithm.alignment.Similarity;
import edu.arizona.cs.learn.algorithm.bpp.BPPFactory;
import edu.arizona.cs.learn.timeseries.classification.Classifier;
import edu.arizona.cs.learn.timeseries.classification.Classify;
import edu.arizona.cs.learn.timeseries.classification.ClassifyParams;
import edu.arizona.cs.learn.timeseries.evaluation.BatchStatistics;
import edu.arizona.cs.learn.timeseries.evaluation.SplitAndTest;
import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.timeseries.model.SequenceType;
import edu.arizona.cs.learn.timeseries.model.symbols.Symbol;
import edu.arizona.cs.learn.util.Utils;

public class Classification {
	
	/**
	 * Calculate the performance of the given Classify on the data given.
	 * Split the data into a training set and a testing set.  66% train 33% test
	 * @param data -- the data we will be testing
	 * @param c -- the classifier
	 * @param outputF -- the file we want the results output to
	 */
	public static void performance(Map<String,List<Instance>> data, Classifier c, String outputF) { 
		List<String> classNames = new ArrayList<String>(data.keySet());
		Collections.sort(classNames);
				
		SplitAndTest sad = new SplitAndTest(30, 2.0/3.0);
		List<BatchStatistics> stats = sad.run(System.currentTimeMillis(), classNames, data, c);
		
		// For now let's print out some informative stuff and build
		// one confusion matrix
		try { 
			BufferedWriter out = new BufferedWriter(new FileWriter(outputF));
			out.write("test," + BatchStatistics.csvHeader() + "\n");

			SummaryStatistics perf = new SummaryStatistics();
			int[][] matrix = new int[classNames.size()][classNames.size()];
			for (int i = 0; i < stats.size(); ++i) { 
				BatchStatistics bs = stats.get(i);
				out.write(bs.toCSV(i + ",", ""));

				double accuracy = bs.accuracy();
				int[][] confMatrix = bs.confMatrix();
				
				perf.addValue(accuracy);
				System.out.println("Fold - " + i + " -- " + accuracy);
				
				// dump out the current batch of statistics
				for (int j = 0; j < classNames.size(); ++j) { 
					for (int k = 0; k < classNames.size(); ++k) { 
						matrix[j][k] += confMatrix[j][k];
					}
				}
			}
			System.out.println("Performance: " + perf.getMean() + " sd -- " + perf.getStandardDeviation());
			
			// Now print out the confusion matrix (in csv format)
			System.out.println(Utils.toCSV(classNames, matrix));		
			out.close();
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	/**
	 * Calculate the performance of the CAVE classifier on
	 * the files given
	 * @param data
	 */
	public static void performance(Map<String,List<Instance>> data) { 
		
		List<String> classNames = new ArrayList<String>(data.keySet());
		Collections.sort(classNames);
		
		ClassifyParams params = new ClassifyParams();
		params.prunePct = 0.5;
		params.incPrune = true;
		params.similarity = Similarity.cosine;
		Classifier c = Classify.prune.getClassifier(params);
		
		SplitAndTest sad = new SplitAndTest(10, 2.0/3.0);
//		CrossValidation cv = new CrossValidation(5);
		List<BatchStatistics> stats = sad.run(System.currentTimeMillis(), classNames, data, c);
		
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
	
	public static void lispPerformance(Map<String,List<Instance>> data) { 
		List<String> classNames = new ArrayList<String>(data.keySet());
		Collections.sort(classNames);
		
		ClassifyParams params = new ClassifyParams();
		params.prunePct = 0.5;
		params.incPrune = true;
		params.similarity = Similarity.strings;
		Classifier c = Classify.prune.getClassifier(params);
		
		SplitAndTest sad = new SplitAndTest(10, 2.0/3.0);
		List<BatchStatistics> stats = sad.run(System.currentTimeMillis(), classNames, data, c);
		
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
//		Map<String,String> map = new HashMap<String,String>();
//		for (String character : Utils.alphabet) { 
//			map.put(character, "data/raw-data/handwriting/wes/xml/" + character + ".xml");
//		}
//		
//		Map<String,List<Instance>> data = new HashMap<String,List<Instance>>();
//		for (String key : map.keySet()) { 
//			data.put(key, XMLUtils.loadXML(map.get(key)));
//		}
//		
//		performance(data);
		
//		lispPerformance(Utils.load("wes-pen", SequenceType.allen));
		
		
		String prefix = "derek-pen";
		
		Utils.LIMIT_RELATIONS = true;
		Utils.WINDOW = 10;
		
		ClassifyParams rParams = new ClassifyParams();
		rParams.prunePct = 0.5;
		rParams.incPrune = true;
		rParams.type = SequenceType.tree;
		rParams.similarity = Similarity.alignment;
		Classifier c1 = Classify.prune.getClassifier(rParams);
		performance(Utils.load(prefix, SequenceType.tree), c1, "logs/tree.csv");
		
		ClassifyParams oParams = new ClassifyParams();
		oParams.prunePct = 0.5;
		oParams.incPrune = true;
		oParams.similarity = Similarity.strings;
		Classifier c2 = Classify.prune.getClassifier(oParams);
		performance(Utils.load(prefix, SequenceType.allen), c2, "logs/allen.csv");
		
		
		
//		List<String> activities = Utils.getActivityNames("ww3d");
//		for (String s : activities) { 
//			map.put(s, "data/input/" + s + ".lisp");
//		}
//		
//		performance(Utils.convert(map));
		
//		performance("ww3d");
//		learningCurve("ww3d");
//		learningCurve("ww2d");
	}
	
	public static void ida() { 
		// IDA 2011 Experiments are done using this bit of code
		// ----- BEGIN CODE -------
		// Relational Experiment
		String prefix = "wes-pen";
		
		ClassifyParams rParams = new ClassifyParams();
		rParams.prunePct = 0.5;
		rParams.incPrune = true;
		rParams.similarity = Similarity.strings;
		Classifier c1 = Classify.prune.getClassifier(rParams);
		performance(Utils.load(prefix, SequenceType.allen), c1, "logs/ida-2011-relational.csv");
		
		Map<String,String> fileMap = new HashMap<String,String>();
		for (File f : new File("data/input/").listFiles()) {
			if ((f.getName().startsWith(prefix)) && (f.getName().endsWith("lisp"))) {
				String name = f.getName().substring(0, f.getName().indexOf(".lisp"));
				fileMap.put(name, f.getAbsolutePath());
			}
		}
		Map<String,List<Instance>> dataMap = Utils.convert(fileMap);
		
		ClassifyParams oParams = new ClassifyParams();
		oParams.prunePct = 0.5;
		oParams.incPrune = true;
		oParams.similarity = Similarity.tanimoto;
		Classifier c2 = Classify.prune.getClassifier(oParams);
		performance(dataMap, c2, "logs/ida-2011-ordered.csv");
		
		
		// ----- END CODE   -------
	}
}
