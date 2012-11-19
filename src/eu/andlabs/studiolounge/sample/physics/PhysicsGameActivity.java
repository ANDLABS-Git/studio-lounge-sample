package eu.andlabs.studiolounge.sample.physics;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.PixelFormat;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andlabs.andengine.extension.physicsloader.PhysicsEditorLoader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import eu.andlabs.studiolounge.gcp.GCPService;
import eu.andlabs.studiolounge.gcp.Lounge;
import eu.andlabs.studiolounge.gcp.Lounge.GameMsgListener;

public class PhysicsGameActivity extends SimpleBaseGameActivity implements
		IOnSceneTouchListener, IUpdateHandler, IScrollDetectorListener,
		IPinchZoomDetectorListener, GameMsgListener {

	private static final String EXTRA_RESULT = "result";
	private static final String EXTRA_X = "x";
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 1280;

	private static final FixtureDef FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(2, 0.5f, 0.5f);
	private static final float FONT_SIZE = 48;

	private BitmapTextureAtlas mBitmapTextureAtlas;

	private TextureRegion mBigAssetTextureRegion;

	private Scene mScene;

	private PhysicsWorld mPhysicsWorld;
	private Sprite mBigAsset;
	private TextureRegion mPandaTextureRegion;

	private ZoomCamera mCamera;
	private TextureRegion mGoalTextureRegion;
	private Sprite mGoal;
	private List<Sprite> mDynamicSprites = new ArrayList<Sprite>();

	private long mStartTime = 0;
	private long mTimeDiff = 0;
	private Font mFont;
	private Text mText;
	private boolean mFinished;
	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private Dialog mDialog;
	private TextureRegion mOtherPandaTextureRegion;
	private boolean mOtherPanda = false;

	private Lounge mLounge;

	@Override
	protected void onStart() {
		mLounge = GCPService.bind(this);
		
		mLounge.register(this);
	}

	@Override
	protected void onStop() {
		GCPService.unbind(this, mLounge);
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		this.mCamera.setBounds(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		this.mCamera.setBoundsEnabled(true);

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
				new FillResolutionPolicy(), mCamera);
	}

	@Override
	protected void onCreateResources() {
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
				this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		try {
			this.mBigAssetTextureRegion = loadResource(this,
					getTextureManager(), PixelFormat.RGBA_8888,
					TextureOptions.DEFAULT, "lounge_sample.png");
			this.mGoalTextureRegion = loadResource(this, getTextureManager(),
					PixelFormat.RGBA_8888, TextureOptions.DEFAULT, "goal.png");
			this.mPandaTextureRegion = loadResource(this, getTextureManager(),
					PixelFormat.RGBA_8888, TextureOptions.DEFAULT,
					"character.png");
			this.mOtherPandaTextureRegion = loadResource(this,
					getTextureManager(), PixelFormat.RGBA_8888,
					TextureOptions.DEFAULT, "other_character.png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.mBitmapTextureAtlas.load();

		final ITexture fontTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mFont = new Font(this.getFontManager(), fontTexture,
				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), FONT_SIZE,
				true, Color.BLACK);
		this.mFont.load();

		this.mDialog = new Dialog();
		try {
			this.mDialog.loadResources(this, getTextureManager());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		
		
		
		
		
		
		//********************
		final PhysicsEditorLoader loader = new PhysicsEditorLoader();
		try {
			loader.load(this, mPhysicsWorld, "lounge_sample.xml", mBigAsset,
					false, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//********************
		
		
		
		
		
		// Pinch zoom and camera movement
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		this.mText = new Text(10, 10, this.mFont, "0.00000000 seconds",
				getVertexBufferObjectManager());
		this.mScene.attachChild(mText);

		this.mScene.setTouchAreaBindingOnActionDownEnabled(true);

		return this.mScene;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {

		if (mStartTime != 0 && !mFinished) {
			this.mTimeDiff = System.currentTimeMillis() - mStartTime;
			this.mText.setText((float) (mTimeDiff / 1000.0) + " seconds");
		}

		for (Sprite sprite : mDynamicSprites) {
			if (mGoal.collidesWith(sprite)) {
				if (!mFinished) {
					this.mTimeDiff = System.currentTimeMillis() - mStartTime;
					Log.i("Lounge physics sample", "Goal reached in "
							+ mTimeDiff + " ms!");
					this.mFinished = true;

					this.mOtherPanda = true;
					sendGameResult(Long.toString(mTimeDiff));

					// TODO Show some actual content
					this.mDialog.show(mCamera, mScene,
							getVertexBufferObjectManager());
				}
			}
		}
	}

	@Override
	public void reset() {
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// pinch zoom and scene moving
		this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

		if (this.mPinchZoomDetector.isZooming()) {
			this.mScrollDetector.setEnabled(false);
		} else {
			if (pSceneTouchEvent.isActionDown()) {
				this.mScrollDetector.setEnabled(true);
			}
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		// adding new dynamic bodies
		if (pSceneTouchEvent.isActionDown() && !mOtherPanda) {
			final float x = pSceneTouchEvent.getX();
			addPanda(x);
			sendNewPanda(x);
		}

		return true;
	}

	private void addPanda(final float pX) {
		final Sprite face;
		final Body body;
		TextureRegion character = this.mPandaTextureRegion;
		if (mOtherPanda) {
			character = this.mOtherPandaTextureRegion;
		}

		face = new Sprite(pX, 5, character, getVertexBufferObjectManager());

		body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face,
				BodyType.DynamicBody, FIXTURE_DEF);

		if (mDynamicSprites.isEmpty()) {
			this.mStartTime = System.currentTimeMillis();
		}

		this.mDynamicSprites.add(face);

		this.mScene.attachChild(face);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face,
				body, true, true));
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

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector,
			final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mCamera.getZoomFactor();
		this.mCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY
				/ zoomFactor);
	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector,
			final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mCamera.getZoomFactor();
		this.mCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY
				/ zoomFactor);
	}

	@Override
	public void onScrollFinished(final ScrollDetector pScollDetector,
			final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mCamera.getZoomFactor();
		this.mCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY
				/ zoomFactor);
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent) {
		this.mPinchZoomStartedCameraZoomFactor = this.mCamera.getZoomFactor();
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent, final float pZoomFactor) {
		this.mCamera.setZoomFactor(Math.max(
				this.mPinchZoomStartedCameraZoomFactor * pZoomFactor, 1));
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent, final float pZoomFactor) {
		this.mCamera.setZoomFactor(Math.max(
				this.mPinchZoomStartedCameraZoomFactor * pZoomFactor, 1));
	}

	private void sendGameResult(String pResult) {
		final Bundle bundle = new Bundle();
		bundle.putString(EXTRA_RESULT, pResult);
		sendMessage(bundle);
	}

	private void sendNewPanda(final float pX) {
		final Bundle bundle = new Bundle();
		bundle.putFloat(EXTRA_X, pX);
		sendMessage(bundle);
	}

	private void sendMessage(final Bundle pBundle) {
		 this.mLounge.sendGameMessage(pBundle);
	}

	public void onMessageRecieved(final Bundle pBundle) {
		if (pBundle.containsKey(EXTRA_X)) {
			addPanda(Float.parseFloat(pBundle.getString(EXTRA_X)));
		} else if (pBundle.containsKey(EXTRA_RESULT)) {
			// TODO: Do something with the result
			Toast.makeText(
					this,
					"Result of other panda is "
							+ pBundle.getString(EXTRA_RESULT),
					Toast.LENGTH_LONG).show();
		}
	}
}