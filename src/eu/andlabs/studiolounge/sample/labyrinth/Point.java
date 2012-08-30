package eu.andlabs.studiolounge.sample.labyrinth;

import java.util.ArrayList;
import java.util.List;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

public class Point extends Rectangle {

	private static final float WIDTH = 50;
	private static final float HEIGHT = 50;
	private static final Color COLOR = Color.YELLOW;

	private OnClickListener mListener;
	List<Edge> edges = new ArrayList<Edge>();

	public Point(float pX, float pY,
			VertexBufferObjectManager pLineVertexBufferObject) {
		super(pX, pY, WIDTH, HEIGHT, pLineVertexBufferObject);
		init(pX, pY);
	}

	public Point(float pX, float pY, OnClickListener pListener,
			VertexBufferObjectManager pLineVertexBufferObject) {
		super(pX, pY, WIDTH, HEIGHT, pLineVertexBufferObject);

		this.mListener = pListener;

		init(pX, pY);
	}

	private void init(float pX, float pY) {
		// Override the position
		setPosition(pX, pY, WIDTH, HEIGHT);

		setColor(COLOR);
	}

	public void setPosition(float pX, float pY, float pWidth, float pHeight) {
		pX -= pWidth / 2;
		pY -= pHeight / 2;
		setSize(pWidth, pHeight);
		super.setPosition(pX, pY);
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			float pTouchAreaLocalX, float pTouchAreaLocalY) {

		if (mListener != null && pSceneTouchEvent.isActionDown()) {
			mListener.onClick(this);
			return true;
		}

		return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX,
				pTouchAreaLocalY);

	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof Point || o instanceof Player) {
			Point other = (Point) o;
			if (inBoundingBoxOf(other)) {
				return true;
			}
		}
		return super.equals(o);
	}

	private boolean inBoundingBoxOf(Point other) {
		if ((other.getX() > getX() - 25 && other.getX() < getX() + 25)
				&& (other.getY() > getY() - 25 && other.getY() < getY() + 25)) {
			return true;
		}

		return false;
	}

	interface OnClickListener {
		public void onClick(Point point);
	}

}
