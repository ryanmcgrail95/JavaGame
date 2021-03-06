package obj.env.blk;

import func.Math2D;
import gfx.CubeMap;
import gfx.GOGL;
import gfx.Shape;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import Sounds.SoundController;
import obj.prim.Drawable;
import obj.prim.Environmental;
import obj.prim.Physical;

public class GroundBlock extends Environmental {	
	private static List<GroundBlock> groundBlockList = new ArrayList<GroundBlock>();
	private List<Blocklet> blockletList = new ArrayList<Blocklet>();
	
	
	private float x, y, z;
	int sNum = 3;
	
	//Hammer
	private int requiredHammer = 0;
	
	//Destroy Variables
	private boolean wasDestroyed = false;
	private float destroyTimer = -1;
	
	private float size = 16;
	
	
	
	public GroundBlock(float x, float y, float z) {
		super();
		
		this.x = x;
		this.y = y;
		this.z = z;
		
		shape = Shape.createBlock("GBlock", -size,-size,size,size,size,-size, CubeMap.getMap("cmGroundBlock1"));
		shape.addBlockShadow(0, 0, size, size);
		
		shape.setPosition(x, y, z+size);
		//shape.setShadowPosition(z+.3f);

		
		groundBlockList.add(this);
	}	
	
	private class Blocklet {
		private float bX, bY, bZ, bSize, vel, dir, zVel;
		private Shape shape;
		private float bounceNum = 0;
		private float alpha = 1;
		
		private float rotVel, rot = 0;
		
		public Blocklet(float nx, float ny, float nz, float size, float dir) {
			super();
			
			this.bX = nx;
			this.bY = ny;
			this.bZ = nz;
			this.vel = .5f; //2
			this.dir = dir;
			this.zVel = 3f;
			this.rotVel = 5;
			this.bSize = size;
			
			shape = Shape.createBlock("Blocklet", -size,-size,size,size,size, -size, CubeMap.getMap("cmGroundBlocklet1"));
			shape.addBlockShadow(0, 0, 0, size);
			shape.setShadowPosition(z+.3f);

			blockletList.add(this);
		}
		
		public void destroy() {			
			shape.destroy();
		}
		
		public void update(float deltaT) {
			bX += Math2D.calcLenX(vel, dir);
			bY += Math2D.calcLenY(vel, dir);
			
			
			int MAX_T = 200, ALPH_T = 150;
			
			if(destroyTimer < MAX_T-ALPH_T)
				alpha = 1;
			else
				alpha = (MAX_T-destroyTimer)/ALPH_T;
			
			if(bZ < z) {
				//.6
				
				float fr = .8f;
				
				zVel = (float) (fr*Math.sqrt(Math.abs(zVel)));
				vel *= fr;
				bZ = z;
				
				bounceNum++;
			}
			if(bZ <= z && bounceNum >= 3) {//Math.abs(zVel) < .1) {
				bZ = z;
				zVel = 0;
				vel = 0;
			}
			else {
				rot += rotVel;
				bZ += zVel;
				zVel -= Physical.getGravity()/2;
			}
			
			shape.setPosition(bX, bY, bZ+bSize);
			shape.setRotation(rot, 0, dir);
			shape.setShadowPosition(z-size + .3f);
			shape.setShadowPosition(0);
			shape.setAlpha(alpha);
		}
		
		//if(destroyTimer > 149)
	    //draw_set_alpha(abs(destroyTimer - 200)/50);
	}
	

	
	public boolean collide(Physical other) {
		
		if(wasDestroyed)
			return false;
		
		//if(global.currentAction != "" || other.destroy || (z+24 < other.z || z > other.z+24))
		//    exit;
		
		boolean didCollide = false, colTop, colBot, colLeft, colRight;
		float oX, oY;
		oX = other.getX();	oY = other.getY();

		colTop = other.collideLine(x-13,y-16,x+13,y-16);
		colBot = other.collideLine(x-13,y+16,x+13,y+16);
		colLeft = other.collideLine(x-16,y-13,x-16,y+13);
		colRight = other.collideLine(x+16,y-13,x+16,y+13);
		
		didCollide = colTop || colBot || colLeft || colRight;
		
		
		float s = 20;
		
		/*if(colTop) {
			other.setY(y - s);
			didCollide = true;
		}
		if(colLeft) {
			other.setX(x - s);
			didCollide = true;
		}
		if(colRight) {
			other.setX(x + s);
			didCollide = true;
		}
		if(colBot) {
			other.setY(y + s);
			didCollide = true;
		}*/
		
		oX = other.getX();	oY = other.getY();
		
		//s = 23;
		s -= 2;
		if(oX > x-s && oX < x+s && oY < y+s && oY > y-s) {
			didCollide = true;
		    other.setX(oX + Math2D.calcLenX(3, Math2D.calcPtDir(x,y,oX,oY)));
		    other.setY(oY + Math2D.calcLenY(3, Math2D.calcPtDir(x,y,oX,oY)));
		}
		
		
		if(didCollide) {
			if(wasDestroyed)
				return false;
			else
				wasDestroyed = true;
		}
		
		return didCollide;
	}
	
	
	//Parent Functions
		public void destroy() {
			super.destroy();
			
			for(Blocklet b : blockletList)
				b.destroy();
			blockletList.clear();
			
			groundBlockList.remove(this);
		}
	
		public void update(float deltaT) {			
			if(wasDestroyed) {
				if(destroyTimer == -1) {	
					SoundController.playSound("BlockCrumble");
					shape.destroy();
					
					float bX, bY, bZ, s = (32/sNum)/2;
					
					for(int varZ = 0; varZ < sNum; varZ++)
						for(int varY = 0; varY < sNum; varY++)
							for(int varX = 0; varX < sNum; varX++) {
								bX = x-16 + varX*32/sNum;
								bY = y-16 + varY*32/sNum;
								bZ = z + varZ*32/sNum;
								
								new Blocklet(bX+s,bY+s,bZ+s, s, (float) (Math.random()*360));
							}
				}
					
				for(Blocklet b : blockletList)
					b.update(deltaT);
				
				
				destroyTimer += 2.5;
				if(destroyTimer >= 200)
					destroy();
			}
			
		}
	
		public boolean draw() {
			super.draw();
			
			return true;
		}

		public static boolean collideAll(Physical other) {
			for(GroundBlock gb : groundBlockList)
				if(gb.collide(other))
					return true;
			
			return false;
		}
		
		private float calcCamDis() {
			return GOGL.calcPerpDistance(x,y) - size;
		}
}
