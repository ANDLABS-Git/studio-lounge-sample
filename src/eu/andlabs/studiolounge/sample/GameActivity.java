package eu.andlabs.studiolounge.sample;

import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.util.FPSLogger;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseCubicInOut;

import android.util.Log;
import eu.andlabs.studiolounge.sample.Point.OnClickListener;

public class GameActivity extends SimpleBaseGameActivity implements
		OnClickListener {

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 720;

	private Field mField;
	private List<Edge> mCloseEdges;

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
				new FillResolutionPolicy(), camera);
	}

	@Override
	protected void onCreateResources() {
		mField = new Field(this, getVertexBufferObjectManager());
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		scene.getBackground().setColor(0.09804f, 0.6274f, 0.8784f);

		mField.attachToScene(scene);

		onPlayerMoveFinished();

		return scene;
	}

	private void onPlayerMoveFinished() {
		for (int i = 0; i < mField.player.length; i++) { // TODO: Replace i by
															// the actual player
			mCloseEdges = mField.getEdgesFromPoint(mField.player[0]);
			for (Edge edge : mCloseEdges) {
				edge.setColor(Edge.COLOR_CLOSEST);
			}

			for (Edge edge : mField.edges) {
				if (!mCloseEdges.contains(edge)) {
					edge.setColor(Edge.COLOR);
				}
			}
		}
	}

	@Override
	public void onCLick(Point point) {
		Log.i("lounge game", "in onclick");
		Log.i("lounge game", mCloseEdges.toString());
		Log.i("lounge game", point.edges.toString());
		for (Edge edge : point.edges) {
			if (mCloseEdges != null && mCloseEdges.contains(edge)) {
				final float pointX = point.getX() + point.getWidth() / 2
						- mField.player[0].getWidth() / 2;
				final float pointY = point.getY() + point.getHeight() / 2
						- mField.player[0].getHeight() / 2;
				mField.player[0].registerEntityModifier(new MoveModifier(1,
						mField.player[0].getX(), pointX, mField.player[0]
								.getY(), pointY, new IEntityModifierListener() {

							@Override
							public void onModifierStarted(
									IModifier<IEntity> pModifier, IEntity pItem) {
							}

							@Override
							public void onModifierFinished(
									IModifier<IEntity> pModifier, IEntity pItem) {
								onPlayerMoveFinished();
							}
						}, EaseCubicInOut.getInstance()));
			}
		}
	}
}