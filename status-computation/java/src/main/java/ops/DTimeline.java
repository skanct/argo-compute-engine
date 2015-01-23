package ops;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TreeMap;

public class DTimeline {
	
	private static SimpleDateFormat w3c_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	private int start_state;	// state to define the beginning of the timeline  
	private TreeMap<Integer,Integer> input_states; // input states with the timestamp converted to slots
	
	
	private int s_period;       // sampling period measured in minutes
	private int s_interval;   	// sampling interval measured in minutes;
	
	int[] samples;				// array of samples based on sampling frequency		
	
	DTimeline()	{
	
		s_period = 1440;			// 1 day = 24 hours = 24 * 60 minutes = 1440 minutes
		s_interval = 5;				// every 5 minutes;
		samples = new int[1440/5]; //288 samples;
		Arrays.fill(samples, -1);
	}
	

	public void setSampling(int _period, int _interval) {
		s_period = _period;
		s_interval = _interval;
		samples = new int[this.s_period/this.s_interval];
	}
	
	public void clearSamples(){
		samples = new int[this.s_period/this.s_interval];
	}
	
	
	public void clearTimestamps(){
		
	}
	
	public void insertTimestamps(){
		
	}
	

	
	
	
	
	
	
}
