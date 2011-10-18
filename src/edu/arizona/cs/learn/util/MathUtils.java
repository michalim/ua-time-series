package edu.arizona.cs.learn.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MathUtils {

	public static Random random;
	private static final double EPSILON = 1e-6;

	static {
		random = new Random();
	}
	
	/**
	 * Return the Kullback-Leibler divergence between the two given discrete probability distributions.
	 * Typically, P represents the "true" distribution of the data, observations, or theoretical distribution
	 * The measure Q typically represents a theory, model, description or approximation of P
	 * 
	 * This process assumes that p sums to 1 and that q sums to 1
	 * @param pMap
	 * @param qMap
	 * @return
	 */
	public static double klDivergence(Map<Integer,Double> pMap, Map<Integer,Double> qMap) { 
		Set<Integer> emptySet = new HashSet<Integer>();
		
		List<Double> p = new ArrayList<Double>();
		List<Double> q = new ArrayList<Double>();
		
		for (Integer propId : pMap.keySet()) { 
			
			Double pValue = pMap.get(propId);
			Double qValue = qMap.get(propId);
			if (qValue == null) { 
				// The q value at the index p.size() will be EPSILON
				emptySet.add(p.size());
				qValue = EPSILON;
			}
			
			p.add(pValue);
			q.add(qValue);
		}
		
		// we have added emptySet.size()*EPSILON to q and we need to subtract it out evenly
		double added = (emptySet.size()*EPSILON) / (double) qMap.size();
		for (int i = 0; i < q.size(); ++i) { 
			if (emptySet.contains(i))
				continue;
			q.set(i, q.get(i)-added);
		}
		return klDivergence(p, q);
	}

	/**
	 * Return the Kullback-Leibler divergence between two probability distributions
	 * Typically, P represents the "true" distribution of the data, observations, or theoretical distribution
	 * The measure Q typically represents a theory, model, description or approximation of P
	 * 
	 * Only defined if both p and q sum to 1 and q(i) > 0 for any i such that p(i) > 0
	 * @param p 
	 * @param q 
	 * @return
	 */
	public static double klDivergence(List<Double> p, List<Double> q) { 
		if (p.size() != q.size())
			throw new RuntimeException("Cannot compute the KL Divergence on different sized distributions");
		
		double sum = 0;
		for (int i = 0; i < p.size(); ++i) { 
			if (p.get(i) == 0) 
				continue;
			
			if (q.get(i) == 0) 
				throw new RuntimeException("Q_i must be greater than zero when P_i is greater than 0");
			
			sum += p.get(i) * Math.log(p.get(i) / q.get(i));
		}
		return sum;
	}
}
