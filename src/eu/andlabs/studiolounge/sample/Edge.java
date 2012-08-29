package eu.andlabs.studiolounge.sample;

import org.andengine.entity.primitive.Line;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import eu.andlabs.studiolounge.sample.Point.OnClickListener;

public class Edge extends Line {

	private static final int LINE_WIDTH = 25;

	public static final Color COLOR = Color.WHITE;
	public static final Color COLOR_CLOSEST = Color.GREEN;

	Point point1;
	Point point2;

	public Edge(float pX1, float pY1, float pX2, float pY2,
			OnClickListener listener,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX1, pY1, pX2, pY2, LINE_WIDTH, pVertexBufferObjectManager);

		point1 = new Point(pX1, pY1, listener, pVertexBufferObjectManager);
		point2 = new Point(pX2, pY2, listener, pVertexBufferObjectManager);

		//TODO: Register points in a non-redundant way to enable two-way moves
//		for (Point point : Field.points) {
//			if (point.equals(point1)) {
//				point1 = point;
//			} else {
//				Field.points.add(point1);
//
//			}
//
//			if (point.equals(point2)) {
//				point2 = point;
//			} else {
//				Field.points.add(point2);
//
//			}
//		}
		point1.edges.add(this);
		point2.edges.add(this);

		setClosest(false);
	}

	public void setClosest(boolean closest) {
		if (closest) {
			setColor(COLOR_CLOSEST);
		} else {
			setColor(COLOR);
		}
	}

}
