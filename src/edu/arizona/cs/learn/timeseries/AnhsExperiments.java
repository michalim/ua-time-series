package edu.arizona.cs.learn.timeseries;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import edu.arizona.cs.learn.algorithm.alignment.Similarity;
import edu.arizona.cs.learn.timeseries.classification.Classifier;
import edu.arizona.cs.learn.timeseries.classification.Classify;
import edu.arizona.cs.learn.timeseries.classification.ClassifyParams;
import edu.arizona.cs.learn.timeseries.experiment.Classification;
import edu.arizona.cs.learn.timeseries.model.SequenceType;
import edu.arizona.cs.learn.timeseries.prep.ww2d.ExportStreamFile;
import edu.arizona.cs.learn.timeseries.prep.ww2d.WubbleWorld2d;
import edu.arizona.cs.learn.timeseries.recognizer.Recognizer;
import edu.arizona.cs.learn.timeseries.recognizer.RecognizerStatistics;
import edu.arizona.cs.learn.util.Utils;

public class AnhsExperiments {
	
	public static void main(String[] args) {
		int n = 30;
		String[] activities = {"chase", "eat", "fight", "flee", "kick-ball", "kick-column"};
		
		boolean exportFromDB, extractFluents, runRecognition, runClassification;
		exportFromDB = false;
		extractFluents = true;
		runRecognition = true;
		runClassification = true;
		
		if (exportFromDB)
			exportWW2DStateDBToCSV(n, activities);
		if (extractFluents)
			extractWW2DFluents(n, activities);
		if (runRecognition) {
			boolean setup = false;	/* only need to set up once, turn to false for subsequent runs */
			runRecognitionExperiment("global-internal-ww2d", setup, 1 /* num of experiments */ );
		}
		if (runClassification)
			runClassificationExperiment("global-internal-ww2d");
		
		System.out.println("Done");
	}
	
	private static void runClassificationExperiment(String prefix) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
		String fileName = "data/classification-" + prefix + "-" + dateFormat.format(new Date()) + ".csv";
		
		ClassifyParams rParams = new ClassifyParams();
		rParams.prunePct = 0.5;
		rParams.incPrune = true;
		rParams.similarity = Similarity.strings;
		Classifier c1 = Classify.prune.getClassifier(rParams);
		Classification.performance(Utils.load(prefix, SequenceType.allen), c1, fileName);
		
	}
	
	@SuppressWarnings("unused")
	private static void extractWW2DFluents(int n, String[] activities) {
		boolean ignoreWalls = true;
		String dataDir = "data/raw-data/ww2d/";
		String outDir = "data/input/";	//"data/raw-data/ww2d/lisp/";
		String prefix = "global-internal-ww2d";
		boolean trackInternalStates = false;
		WubbleWorld2d.global(n, activities, ignoreWalls, dataDir, outDir, prefix, trackInternalStates);
	}

	@SuppressWarnings("unused")
	private static void exportWW2DStateDBToCSV(int n, String[] activities) {
		ExportStreamFile.inPrefix = "data/raw-data/ww2d/states/";
		ExportStreamFile.outPrefix = "data/raw-data/ww2d/";
		ExportStreamFile.numEpisodes = n;
		ExportStreamFile.classes = activities;
		
		ExportStreamFile.convert(WubbleWorld2d.DBType.Global);		// global
		ExportStreamFile.convert(WubbleWorld2d.DBType.Agent);		// agent
		ExportStreamFile.convert(WubbleWorld2d.DBType.Object);		// obj
	}
	
	
	/*
	 **************************************************************
	 *  ================ IJCAI 2011 Experiments ================
	 **************************************************************
	 */

	@SuppressWarnings("unused")
	private static void runInformationDepthExperiment(String prefix) {
		int[] pcts = { 80 }; //{ 95 };
		boolean[] prunes = { true };
		String[] activities = { "approach", "jump-over", "jump-on", "push" };  //{ "left", "right" }; //{"d", "l", "a", "h" };
		String testActivity = "jump-over"; //"d"; 
		SequenceType type = SequenceType.allen;
		Recognizer recognizer = Recognizer.cave;
		boolean optimizeRecognizers = true;


		for (boolean prune : prunes) {
			for (int i : pcts) {
				Experiments cv = new Experiments(0);
				cv.informationDepth(prefix, activities, testActivity, recognizer, type, 
						i, prune, false, optimizeRecognizers);
			}
		}
	}


	@SuppressWarnings("unused")
	private static void runDecompositionExperiment(String prefix) {
		try {
			int[] pcts = { 80 };
			boolean[] prunes = { true };
			int experimentsCount = 20;
			String[] activities = { "jump-over" }; //, "jump-on", "left", "push", "right" };
			String subActivity = "approach";
			SequenceType type = SequenceType.allen;
			Recognizer recognizer = Recognizer.cave;
			boolean optimizeRecognizers = true;
			boolean composeGraphs = true;
			
			for (String activity : activities) {
				for (boolean prune : prunes) {
					for (int i : pcts) {
							
						Experiments cv = new Experiments(0);
						cv.decomposition(prefix, activity, subActivity,
								recognizer, type, i, prune, false,
								optimizeRecognizers, experimentsCount,
								composeGraphs);
						
						
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@SuppressWarnings("unused")
	private static void runRecognitionExperiment(String prefix, boolean setup, int experiments) {
		try {
//			int[] folds = { 2, 3, 4, 5, 6, 7, 8, 9, 10 };
			int[] folds = { 6 };
			int[] pcts = { 80 };
			boolean[] prunes = { true };
			boolean[] optimizeRecognizers = { true };
			
			SequenceType type = SequenceType.allen;
			Recognizer recognizer = Recognizer.cave;
			boolean outputRecognizer = true;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
			String fileName = "data/recognizer-" + prefix + "-" +
					type + "-all-" + dateFormat.format(new Date()) + ".csv";
			
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write("experimentID,className,type,folds,recognizer,optimized,prune,minPct,precision,recall,f\n");
			
			for (int expID = 0; expID < experiments; expID++) {
				for (int f : folds) {

					if (setup) {
						Experiments.selectCrossValidation(prefix, f);
					}
						
					for (boolean prune : prunes) {
						
						if (setup) {
							Experiments.signatures(prefix, type, f, prune);
						}
						
						for (boolean optimize : optimizeRecognizers) {
							
							for (int i : pcts) {
								
								Experiments cv = new Experiments(f);
								Map<String, RecognizerStatistics> map = 
									cv.recognition(prefix, recognizer, type, i, prune,
											false, outputRecognizer, optimize);
								
								System.out.println("i=" + i + ", folds=" + f);
								
								for (String className : map.keySet()) {
									RecognizerStatistics rs = map.get(className);
									out.write(expID + "," 
											+ className + "," 
											+ type + "," 
											+ f + ","
											+ recognizer.name() + "," 
											+ ((optimize) ? "true" : "false") + ","
											+ ((prune) ? "true" : "false") + ","
											+ i + ","
											+ rs.precision() + ","
											+ rs.recall() + ","
											+ rs.fscore() + "\n");
								}
							}
						}
					}
				}
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
