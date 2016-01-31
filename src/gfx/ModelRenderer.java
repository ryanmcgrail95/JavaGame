package gfx;

import collision.C3D;
import object.primitive.Drawable;
import resource.model.Model;

public class ModelRenderer extends Drawable {

	private float dis = 10;
	private Model mod;
	
	public ModelRenderer(String modelName) {
		super(false,false);
		mod = Model.get(modelName);
		
		C3D.splitModel(mod.getTriangles(), 10,10,48);
	}

	@Override
	public void draw() {
		float x,y,z, toX,toY,toZ;
		toX = toY = toZ = 0;
		
		x = -dis;
		y = z = 0;
		z = 1;
		
		/*GL.clear(RGBA.YELLOW);
		GL.getMainCamera().setProjection(x,y,z,toX,toY,toZ);
		
		GT.transformClear();
		GT.transformTranslation(0,0,0);
		
			GT.transformRotationZ(GL.getTime());

			GT.transformScale(2);
			GT.transformRotationX(-90);
			
			GL.enableCulling();
			GL.enableShader("Model");
			mod.draw();
			GL.disableShaders();
		GT.transformClear();*/
		
		GL.setPerspective();
		GT.transformClear();
			GL.enableShader("Model");
			
			mod.drawFast();
			GL.disableShaders();
		GT.transformClear();
	}

	@Override
	public void add() {}
}
