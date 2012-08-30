package eu.andlabs.studiolounge.sample.physics;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.shape.IShape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.PixelFormat;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andlabs.andengine.extension.physicsloader.PhysicsEditorLoader;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class PhysicsGameActivity extends SimpleBaseGameActivity implements
		IOnSceneTouchListener, IUpdateHandler {

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 1280;

	private static final FixtureDef FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(2, 0.5f, 0.5f);

	private BitmapTextureAtlas mBitmapTextureAtlas;

	private TextureRegion mBigAssetTextureRegion;

	private Scene mScene;

	private PhysicsWorld mPhysicsWorld;
	private Sprite mBigAsset;
	private TiledTextureRegion mCircleFaceTextureRegion;

	private Camera mCamera;
	private TextureRegion mGoalTextureRegion;
	private Sprite mGoal;
	private List<Sprite> mDynamicSprites = new ArrayList<Sprite>();

	private long mStartTime = 0;
	private long mTimeDiff = 0;

	@Override
	public EngineOptions onCreateEngineOptions() {
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	protected void onCreateResources() {
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
				this.getTextureManager(), 64, 32, TextureOptions.BILINEAR);
		try {
			this.mBigAssetTextureRegion = loadResource(this,
					getTextureManager(), PixelFormat.RGBA_8888,
					TextureOptions.DEFAULT, "lounge_sample.png");
			this.mGoalTextureRegion = loadResource(this, getTextureManager(),
					PixelFormat.RGBA_8888, TextureOptions.DEFAULT, "goal.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"face_circle_tiled.png", 0, 0, 2, 1); // 64x32

		this.mBitmapTextureAtlas.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.mEngine.registerUpdateHandler(this);

		this.mScene = new Scene();
		this.mScene.setOnSceneTouchListener(this);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,
				SensorManager.GRAVITY_EARTH), false);

		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();

		this.mBigAsset = new Sprite(0, CAMERA_HEIGHT
				- mBigAssetTextureRegion.getHeight(),
				this.mBigAssetTextureRegion, vertexBufferObjectManager);
		this.mScene.attachChild(mBigAsset);

		this.mGoal = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT
				- mGoalTextureRegion.getHeight() - 5, this.mGoalTextureRegion,
				vertexBufferObjectManager);
		this.mScene.attachChild(mGoal);

		final PhysicsEditorLoader loader = new PhysicsEditorLoader();
		try {
			loader.load(this, mPhysicsWorld, "lounge_sample.xml", mBigAsset,
					false, false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		return this.mScene;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {
		for (Sprite sprite : mDynamicSprites) {
			if (mGoal.collidesWith(sprite)) {
				if(mTimeDiff  == 0) {
					this.mTimeDiff = System.currentTimeMillis() - mStartTime;
					Log.i("Lounge physics sample", "Goal reached in "+ mTimeDiff + " ms!");
				}
			}
		}
	}

	@Override
	public void reset() {
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.isActionDown()) {
			final AnimatedSprite face;
			final Body body;
			face = new AnimatedSprite(pSceneTouchEvent.getX(), 5,
					this.mCircleFaceTextureRegion,
					getVertexBufferObjectManager());

			body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face,
					BodyType.DynamicBody, FIXTURE_DEF);

			if (mDynamicSprites.isEmpty()) {
				this.mStartTime = System.currentTimeMillis();
			}

			this.mDynamicSprites.add(face);

			this.mScene.attachChild(face);
			this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
					face, body, true, true));

			return true;
		}
		return false;
	}

	public static TextureRegion loadResource(final Context pContext,
			final TextureManager pTextureManager, final PixelFormat pFormat,
			final TextureOptions pOptions, final String pPath)
			throws IOException {
		final BitmapTexture texture = new BitmapTexture(pTextureManager,
				new IInputStreamOpener() {
					@Override
					public InputStream open() throws IOException {
						return pContext.getAssets().open(pPath);
					}
				}, BitmapTextureFormat.fromPixelFormat(pFormat), pOptions);

		texture.load();

		return TextureRegionFactory.extractFromTexture(texture);
	}

}