package harmonicTable;

import processing.core.PApplet;
import processing.core.PGraphics;

//float a = length/2;
//float b = sin(radians(60))*length;
//float c = length;

public class Hexagon {
	
	 protected PApplet parent;
	 protected PGraphics buffer;
	 protected float a;
	 protected float b;
	 protected float c;
	 private float startX;
	 private float startY;
	 
	 @SuppressWarnings("static-access")
	public Hexagon(Object p, int newStartX, int newStartY, int sideLength){
			if (p instanceof PGraphics)
				buffer = (PGraphics) p;
			
			if (p instanceof PApplet)
				parent = (PApplet) p;
			
			setStartX(newStartX);
			setStartY(newStartY);
			c = sideLength;
			a = c/2;
			b = parent.sin(parent.radians(60))*c;
		}

	public void drawTranslatedHex(){

		parent.pushMatrix();
		parent.translate(getStartX(), getStartY());
		//draw hex shape
		drawHex();
		parent.popMatrix();
	}
	
	public void drawHex(){
		//draw hex shape
		parent.beginShape();
			parent.vertex(0,b);
			parent.vertex(a,0);
			parent.vertex(a+c,0);
			parent.vertex(2*c,b);
			parent.vertex(a+c,2*b);
			parent.vertex(a,2*b);
			parent.vertex(0,b);
		parent.endShape();
	}

	public void setStartX(float startX) {
		this.startX = startX;
	}

	public float getStartX() {
		return startX;
	}

	public void setStartY(float startY) {
		this.startY = startY;
	}

	public float getStartY() {
		return startY;
	}
}
