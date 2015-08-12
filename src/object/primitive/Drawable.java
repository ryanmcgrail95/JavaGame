package object.primitive;
import functions.Math2D;
import gfx.Camera;
import gfx.CameraFX;
import gfx.GLText;
import gfx.GOGL;
import gfx.Gameboy;
import gfx.Overlay;
import gfx.RGBA;
import gfx.Shape;
import io.Mouse;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Comparator;

import cont.Messages;
import cont.Text;
import datatypes.StringExt;
import datatypes.lists.CleanList;
import obj.prt.Floaties;
import object.actor.Player;
import object.environment.Tree;
import time.Timer;
import window.Window;

public abstract class Drawable extends Updatable {
	private static CleanList<Drawable> renderList = new CleanList<Drawable>();
	private static CleanList<Drawable> drawList = new CleanList<Drawable>();
	private static CleanList<Drawable> hoverList = new CleanList<Drawable>();	
	private static CleanList<Drawable> onscreenList = new CleanList<Drawable>();
	private static CleanList<Drawable> onscreenHoverList = new CleanList<Drawable>();
	
	private static int colR = 1, colG = 0, colB = 0;
	
	// Mouse-Selection Variables
	protected boolean isSelected = false;
	private static Timer selectTimer = new Timer(10), sortTimer = new Timer(60);


	protected int R, G, B;
	protected boolean visible = true, isHoverable = false, isRenderable = false;	
	protected String name;
	

	
	public Drawable(boolean hoverable, boolean renderable) {		
		drawList.add(this);
		
		if(hoverable)
			generateHoverColor();
		if(renderable)
			renderList.add(this);
	}
	
	private void generateHoverColor() {
				
		R = colR; G = colG; B = colB;
		
		colR++;
		if(colR > 255) {
			colR = 0;	colG++;
			
			if(colG > 255) {
				colG = 0;	colB++;
			}
		}
			
		isHoverable = true;
	}

	//PARENT FUNCTIONS
	public abstract void draw();
	
	public void render() {}
	public void hover() {}
		
	public void destroy() {
		drawList.remove(this);
		if(isHoverable)
			hoverList.remove(this);
		if(isRenderable)
			renderList.remove(this);
		super.destroy();
	}
		
		
	//PERSONAL FUNCTIONS
	public void setVisible(boolean visible) {this.visible = visible;}
	public abstract boolean checkOnscreen();
	public abstract float calcDepth();	
	
	//GLOBAL FUNCTIONS
	public static void presort() {		
		onscreenList.clear();
		onscreenHoverList.clear();
		
		byte addHover = 2;
		if(selectTimer.check()) {
			if(Window.checkMouseAll())
				addHover = 1;
			else
				addHover = 0;
		}
		
		// Sort by Depth
		int dSi = drawList.size(), i, k;
		for(Drawable d : drawList)
			if(d.visible)
				if(d.calcDepth() < Camera.getViewDistance())
					if(d.checkOnscreen()) {
						onscreenList.add(d);
						
						if(addHover == 0) {
							if(d.isHoverable)
								onscreenHoverList.add(d);
						}
						else if(addHover == 1)
							d.isSelected = false;
					}
			
		//onscreenList.sort(Drawable.Comparators.DEPTH);
	}
	
		public static void display() {
						
			presort();
			
			int hSi = onscreenHoverList.size(), dSi = onscreenList.size();
			
			Mouse.resetCursor();
			        	
			
			//GOGL.setViewport(0,0,640,480);

			GOGL.setOrtho();
			for(Drawable d : renderList)
				d.render();
			Window.renderAll();
			
			
			GOGL.setPerspective();
			//GOGL.enableDepth();

			if(onscreenHoverList.size() > 0) {
				GOGL.allowLighting(false);
				GOGL.clearScreen();
				
				for(Drawable d : onscreenHoverList) {
					d.isSelected = false;
					
					GOGL.forceColor(new RGBA(d.R/255f,d.G/255f,d.B/255f));
					
					d.draw();
				}
			
				RGBA idRGBA = Mouse.getPixelRGBA();
				
				int r,g,b;
				r = idRGBA.getRi();
				g = idRGBA.getGi();
				b = idRGBA.getBi();
				
				if(r < 0)	r += 256;
				if(g < 0)	g += 256;
				if(b < 0)	b += 256;

				int cR, cG, cB;
				for(Drawable d : onscreenHoverList) {
					cR = d.R;
					cG = d.G;
					cB = d.B;

					if(cR == r && cG == g && cB == b) {
						d.isSelected = true;
						d.hover();
						break;
					}
				}
				
				GOGL.allowLighting(true);
				GOGL.unforceColor();
			}
	}

	public static int getNumber() {return drawList.size();}
		
	public static class Comparators {
		public final static Comparator<Drawable> DEPTH = new Comparator<Drawable>() {
            public int compare(Drawable o1, Drawable o2) {
                return (int) (o1.calcDepth() - o2.calcDepth());
            }
        };
	}

	public static void draw3D() {
		
		//Draw BG
		GOGL.setOrtho(-1000);
		RGBA skyTop = new RGBA(82,142,165), skyBottom = new RGBA(198,255,255);
		float tY = 140,bY = 300;
		GOGL.setColor(skyTop);
		GOGL.fillRectangle(0,0,640,tY);
		GOGL.fillVGradientRectangle(0,tY, 640,bY-tY, skyTop, skyBottom, 10);
		GOGL.setColor(skyBottom);
		GOGL.fillRectangle(0,bY,640,480-bY);
		
					
		if(Camera.checkMapView())
			GOGL.setOrthoPerspective();
		else
			GOGL.setPerspective();
		
		
		for(Drawable d : onscreenList) {
			d.draw();
				
			if(d.isSelected)
				d.hover();
		}
		
		
		Floaties.draw();
		Shape.drawAll();
	}
}
