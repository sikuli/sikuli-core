package org.sikuli.core.search;

import java.awt.Point;
import java.awt.Rectangle;

public class RegionMatch extends Match {
	
	public int x;
	public int y;
	public int width;
	public int height;	
	
	public RegionMatch(Rectangle r) {
		super();
		x = r.x;
		y = r.y;
		width = r.width;
		height = r.height;
	}

	public Rectangle getBounds() {
		return new Rectangle(x,y,width,height);
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Point getLocation() {
		return getBounds().getLocation();
	}
}
