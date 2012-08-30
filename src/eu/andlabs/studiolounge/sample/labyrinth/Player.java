package eu.andlabs.studiolounge.sample.labyrinth;

import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

public class Player extends Point {
	
	private static final Color COLOR = Color.BLUE;
	private static final int HEIGHT = 35;
	private static final int WIDTH = 35;
	
	
	public Player(float pX, float pY,
			VertexBufferObjectManager pLineVertexBufferObject) {
		super(pX, pY, pLineVertexBufferObject);
		
		setPosition(pX, pY, WIDTH, HEIGHT);

		setColor(COLOR);
	}
	
}
