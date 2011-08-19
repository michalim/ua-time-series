package edu.arizona.cs.learn.experimental.histogram;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.Interval;

public class Histogram {

	
	public static void main(String[] args) {
		String dataF = "chpt1-approach";
		
		List<Instance> instances = Instance.load(dataF, new File("data/input/" + dataF + ".lisp"));
		List<Set<Data>> data = new ArrayList<Set<Data>>();
		for (Instance instance : instances) { 
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			
			Map<String,List<Interval>> map = new HashMap<String,List<Interval>>();
			for (Interval interval : instance.intervals()) { 
				min = Math.min(interval.start, min);
				max = Math.max(interval.end, max);
				
				List<Interval> intervals = map.get(interval.name);
				if (intervals == null) { 
					intervals = new ArrayList<Interval>();
					map.put(interval.name, intervals);
				}
				intervals.add(interval);
			}
			
			Set<Data> dataSet = new TreeSet<Data>();
			for (String name : map.keySet()) { 
				Data d = new Data(name);
				d.update(map.get(name), max-min);
				
				dataSet.add(d);
			}
			data.add(dataSet);
		}
		
		for (int i = 0; i < data.size(); ++i) { 
			Set<Data> dataSet = data.get(i);
			System.out.println("Instance: " + i);
			for (Data d : dataSet) { 
				System.out.println(" -- " + d.name() + " -- " + d.pctTime());
			}
		}
	}
}

class Data implements Comparable<Data> { 
	private String _name;
	private int _onTime;
	private int _offTime;
	
	private double _pctTime;
	
	public Data(String name) { 
		_name = name;
	}
	
	public void update(List<Interval> intervals, int length) { 
		_onTime = 0;
		for (Interval interval : intervals) { 
			_onTime += (interval.end - interval.start);
		}
		_offTime = length - _onTime;
		_pctTime = (double) _onTime / (double) length;
	}
	
	@Override
	public String toString() {
		return _name;
	}

	@Override
	public int hashCode() { 
		return _name.hashCode();
	}
	
	public String name() { 
		return _name;
	}
	
	public double pctTime() {
		return _pctTime;
	}
	
	public int onTime() { 
		return _onTime;
	}
	
	public int offTime() { 
		return _offTime;
	}

	@Override
	public int compareTo(Data arg0) {
		return _name.compareTo(arg0._name);
	}
}