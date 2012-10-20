package org.sikuli.core.search;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;


public class RegionFilter extends Filter<RegionMatch> {
		
	final private List<Rectangle> includedRegions = Lists.newArrayList();
	final private List<Rectangle> excludedRegions = Lists.newArrayList();
	
	public void exclude(Rectangle region){
		excludedRegions.add(region);
	}
	
	public void include(Rectangle region){
		includedRegions.add(region);
	}
	
	@Override
	public boolean accept(RegionMatch m){
		for (Rectangle o : includedRegions){
			Rectangle r = m.getBounds();
			if (o.contains(r)){
				return true;
			}
		}
		for (Rectangle o : excludedRegions){			
			Rectangle r = m.getBounds();
			if (o.contains(r)){
				return false;
			}
		}
		return false;
	}	
	
}