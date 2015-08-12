package resource.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;

import datatypes.vec2;
import datatypes.vec3;
import fl.FileExt;
import gfx.GOGL;

public class Model {
	private float[][] pointList;
	private float[][] normalList;
	private float[][] uvList;
	private float[][] vertexList;
	private int vertexNum;
	private Material materials;

	public static Model MOD_PINEBRANCHES, MOD_PINETREE, MOD_PINESTUMP;
	public static Model MOD_WMBLADES, MOD_WMFRAME, MOD_WMBODY;
	public static Model MOD_HOUSEBODY, MOD_HOUSEFRAME;
	public static Model MOD_BOWL;
	public static Model MOD_FERN;
	
	public static void ini() {
		
		MOD_WMBLADES = OBJLoader.load("windmill");
		MOD_WMFRAME = OBJLoader.load("windmillframe");
		MOD_WMBODY = OBJLoader.load("windmillbody");
		
		MOD_PINEBRANCHES = OBJLoader.load("pinebranches");
		MOD_PINETREE = OBJLoader.load("pinetree");
		MOD_PINESTUMP = OBJLoader.load("pinestump");
		
		MOD_HOUSEBODY = OBJLoader.load("housebody");
		MOD_HOUSEFRAME = OBJLoader.load("houseframe");
		
		MOD_BOWL = OBJLoader.load("bowl");
		
		MOD_FERN = OBJLoader.load("fern");
	}
	
	public Model(List<vec3> pointList,	List<vec3> normalList, List<vec2> uvList, List<vec3> vertexList) {		
		this.pointList = vec3.convert(pointList);
		this.normalList = vec3.convert(normalList);
		this.uvList = vec2.convert(uvList);
		this.vertexList = vec3.convert(vertexList);
		vertexNum = vertexList.size();
		
		scale(2f);
		flipNormals();
	}
	
	public void draw() {
		GL2 gl = GOGL.gl;
		
		int p,u,n;
		
		materials.enable();
		
		gl.glBegin(GL2.GL_TRIANGLES);
		for(float[] v : vertexList) {			
			p = (int) v[0];
			u = (int) v[1];
			n = (int) v[2];
			
			gl.glTexCoord2fv(uvList[u],0);
			gl.glNormal3fv(normalList[n],0);
			gl.glVertex3fv(pointList[p],0);
		}
		gl.glEnd();
		
		materials.disable();
	}

	public void scale(float scaleFactor) {		
		for(float[] v : pointList) {
			v[0] *= scaleFactor;
			v[1] *= scaleFactor;
			v[2] *= scaleFactor;
		}
	}
	public void flipNormals() {
		for(float[] v : normalList) {
			v[0] *= -1;
			v[1] *= -1;
			v[2] *= -1;
		}
	}
	
	public void println() {
		System.out.println(vertexNum);
	}

	public void attachMaterials(Material mat) {
		materials = mat;
	}
}
