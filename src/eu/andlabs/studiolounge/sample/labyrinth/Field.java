package eu.andlabs.studiolounge.sample.labyrinth;

import java.util.ArrayList;
import java.util.List;

import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import eu.andlabs.studiolounge.sample.labyrinth.Point.OnClickListener;

public class Field {

	List<Edge> edges = new ArrayList<Edge>();
	Player[] player = new Player[2];
	Goal goal;
	static List<Point> points = new ArrayList<Point>();

	Field(OnClickListener listener, final VertexBufferObjectManager pVertexBufferObjectManager) {
		player[0] = new Player(25, 25, pVertexBufferObjectManager);
		player[1] = new Player(250, 250, pVertexBufferObjectManager);
		
		Edge edge = new Edge(25, 25, 25, 125, listener,
				pVertexBufferObjectManager);
		edges.add(edge);

		edge = new Edge(25, 25, 125, 25, listener, pVertexBufferObjectManager);
		edges.add(edge);
		
		edge = new Edge(125, 25, 250, 25, listener, pVertexBufferObjectManager);
		edges.add(edge);
		
		goal = new Goal(240, 360, pVertexBufferObjectManager);
	}

	public void attachToScene(final Scene pScene) {
		for (Edge edge : edges) {
			pScene.attachChild(edge);
			pScene.attachChild(edge.point1);
			pScene.attachChild(edge.point2);
			
			pScene.registerTouchArea(edge.point1);
			pScene.registerTouchArea(edge.point2);
		}
		for(int i = 0; i < player.length; i++) {
			pScene.attachChild(player[i]);
		}
		
		pScene.attachChild(goal);
	}
	
	public List<Edge> getEdgesFromPoint(Point point) {
		List<Edge> retEdges = new ArrayList<Edge>();
		for(Edge edge : edges) {
			if(edge.point1.equals(point) || edge.point2.equals(point)) {
				retEdges.add(edge);
			}
		}
		
		return retEdges;
	}
}
