package org.sikuli.core.cv;

import java.util.ArrayList;
import java.util.List;

public class BooleanArraySegmenter {
			
	static public List<BooleanArraySegment> segment(Boolean[] booleans){
		ArrayList<BooleanArraySegment> segments = new ArrayList<BooleanArraySegment>();
		
		boolean previousValue = false;
		BooleanArraySegment currentSegment = null;
		for (int i = 0 ; i < booleans.length; ++i){
							
			boolean currentValue = booleans[i];
			
			if (!previousValue && currentValue){
				
				currentSegment = new BooleanArraySegment();
				currentSegment.value = true;
				currentSegment.position = i;
				currentSegment.size = 1;
			}
			
			if (previousValue && currentValue){					
				currentSegment.size += 1;					
			}
			
			if (previousValue && !currentValue){					
				segments.add(currentSegment);
				currentSegment = null;
			}
			
			previousValue = currentValue;				
		}
		
		if (currentSegment != null){
			segments.add(currentSegment);
		}
		
		return segments;			
	}
	
	
	
}