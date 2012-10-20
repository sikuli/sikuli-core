package org.sikuli.ocr;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

public class FontModel{
	public double getSize() {
		return size;
	}
	public void setWeight(int weight){
		this.weight = weight;
	}
	public int getWeight(){
		return weight;
	}		
	public void setSize(double size) {
		this.size = size;
	}

	public void setLetterSpacing(int letterSpacing) {
		this.letterSpacing = letterSpacing;
	}
	public int getLetterSpacing() {
		return letterSpacing;
	}

	public void setScaleX(double scalex) {
		this.scalex = scalex;
	}
	public double getScaleX() {
		return scalex;
	}

	public void setScaleY(double scaley) {
		this.scaley = scaley;
	}
	public double getScaleY() {
		return scaley;
	}

	private double size=12;
	private int weight=400;
	private int letterSpacing=0;
	private double scalex=1.0;
	private double scaley=1.0;
	private double tracking=0;
	private String name="";
	private Font font;

	
//	public Font getFont(){
//		InputStream is = TextImageRenderer.class.getResourceAsStream(name);
//		try {
//			f = Font.createFont(Font.TRUETYPE_FONT, is);
//		} catch (FontFormatException e) {
//		} catch (IOException e) {
//		}
//	}
			
	public String toString(){
		return "font:" + font.getName() + ", size:" + size + ", tracking:" + tracking;
	}
	
	
//	static public FontModel learnFrom(BufferedImage trainingImage, String label){
//		return FontModelLearner.learn(trainingImage, label);
//	}
//
//	static public FontModel learnFrom(URL trainingImageUrl, String label) throws IOException{
//		BufferedImage trainingImage = ImageIO.read(trainingImageUrl);
//		return FontModelLearner.learn(trainingImage, label);
//	}
	
	public double getTracking() {
		return tracking;
	}
	public void setTracking(double tracking) {
		this.tracking = tracking;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setFont(Font font) {
		this.font = font;		
	}
	public Font getFont() {
		return font;		
	}
	public BufferedImage toImage(String text) {
		TextImageRenderer renderer = new TextImageRenderer();
		return renderer.render(text, font, size, tracking);		
	}


}