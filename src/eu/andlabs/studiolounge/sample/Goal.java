package eu.andlabs.studiolounge.sample;

import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

public class Goal extends Point {

	public Goal(float pX, float pY,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pVertexBufferObjectManager);
		
		setPosition(pX, pY, 80, 80);
		setColor(Color.WHITE);
	}
}
