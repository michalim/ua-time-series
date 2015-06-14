# Classification Accuracy #

This page discusses how to configure _ua-time-series_ to compute the classification accuracy of some dataset.  This wiki makes several assumptions

  1. Each data file is formated as discussed in InputFileFormat.
  1. Each data file contains all of the instances for a class label.
  1. There are multiple potential class labels, each instance of a label is stored in it's own file, but all share a common prefix.

In order to compute the classification accuracy, one first needs to make several decisions.

  1. What type of classifier are you planning on using?  Currently `kNN` and `CAVE` are implemented.
  1. How do you want to measure classification accuracy?  There are several methods for computing classification accuracy implemented.
    * [SplitAndTest](http://code.google.com/p/ua-time-series/source/browse/trunk/src/edu/arizona/cs/learn/timeseries/evaluation/SplitAndTest.java) - splits the dataset into training and testing based on the given percentage.  We perform multiple tests since the the partitioning is random.
    * [CrossValidation](http://code.google.com/p/ua-time-series/source/browse/trunk/src/edu/arizona/cs/learn/timeseries/evaluation/CrossValidation.java) - partitions the dataset int _K_ unique folds and compute the classification accuracy.
    * [LeaveOneOut](http://code.google.com/p/ua-time-series/source/browse/trunk/src/edu/arizona/cs/learn/timeseries/evaluation/LeaveOneOut.java) - the training set contains all of the instances but one.  Testing is performed on the instances left out of the training set.

What follows is some code that performs classification on one of the datasets (```ww3d``` included in the source code).

```
  // This line loads in the .lisp data files from the director data/input with the prefix ww3d
  Map<String,List<Instance>> data = Utils.load("data/input/", "ww3d", SequenceType.allen);
  
  List<String> classNames = new ArrayList<String>(data.keySet());
  Collections.sort(classNames);
		
  // The classify parameters are used to initialize the classifier.  The fields are documented in 
  // the ClassifyParams class
  ClassifyParams params = new ClassifyParams();
  params.prunePct = 0.5;
  params.incPrune = true;
  params.similarity = Similarity.strings;

  // Construct a classifier initialized with the parameters.
  Classifier c = Classify.prune.getClassifier(params);

  // In this example we are splitting the data into 2/3 training and 1/3 testing for each class label.		
  // The experiment is carried out 10 different times with 10 different random partitions
  SplitAndTest sad = new SplitAndTest(10, 2.0/3.0);

  // Perform the classification
  //    First parameter is the random seed
  //    Second parameter is the list of class names
  //    Third parameter is the data
  //    Fourth parameter is the classifier
  // The result is a bunch of statistics for each random partition
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
```