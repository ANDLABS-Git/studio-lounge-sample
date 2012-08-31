package eu.andlabs.studiolounge.sample.physics;

import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.PixelFormat;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

/**
 * A simple dialog shown in the center of the screen
 * 
 * @author Johannes Borchardt
 * 
 */
public class Dialog {

	private Sprite mWindow;
	private Rectangle mBackground;
	private TextureRegion mPopupTextureRegion;
	private boolean mShowing;
	private String mPath = "popup.png";

	// private Scene mScene;

	public Dialog() {
	}

	public void loadResources(final Context pContext,
			final TextureManager pTextureManager) throws IOException {
		this.mPopupTextureRegion = PhysicsGameActivity.loadResource(pContext,
				pTextureManager, PixelFormat.RGBA_8888, TextureOptions.DEFAULT,
				mPath);
	}

	public void show(final Camera pCamera, final Scene pScene,
			final VertexBufferObjectManager pVertexBufferObjectManager) {
		show(pCamera, pScene, true, pVertexBufferObjectManager);
	}

	public void show(final Camera pCamera, final Scene pScene,
			boolean pTurnBackground,
			final VertexBufferObjectManager pVertexBufferObjectManager) {

		if (!mShowing) {
			float x = -pScene.getX();
			float y = -pScene.getY();

			float xSprite = x
					+ (pCamera.getWidth() - mPopupTextureRegion.getWidth()) / 2;
			float ySprite = y
					+ (pCamera.getHeight() - mPopupTextureRegion.getHeight())
					/ 2;

			//TODO: Use screen size instead of fixed value
			float ratio = 720f / pCamera.getWidth();

			if (mWindow == null) {
				this.mWindow = new Sprite(xSprite, ySprite,
						mPopupTextureRegion, pVertexBufferObjectManager);
				this.mWindow.setScale(ratio * 1.2f);
				if (!pTurnBackground) {
					this.mWindow.setRotation(90);
				}
			}

			if (mBackground == null) {
				this.mBackground = new Rectangle(x, y, pCamera.getWidth(),
						pCamera.getHeight(), pVertexBufferObjectManager);
				this.mBackground.setColor(0, 0, 0, 0.8f);
			}

			if (mBackground.getParent() == null) {
				pScene.attachChild(mBackground);
				pScene.registerTouchArea(mBackground);
			}

			if (mWindow.getParent() == null) {
				pScene.attachChild(mWindow);
			}

			this.mBackground.registerEntityModifier(new AlphaModifier(0.15f, 0,
					0.8f));
			this.mWindow.registerEntityModifier(new ScaleModifier(0.15f,
					0.00001f, 1f) {
				@Override
				protected void onModifierFinished(IEntity pItem) {
					super.onModifierFinished(pItem);

					mWindow.registerEntityModifier(new ScaleModifier(0.08f, 1f,
							0.75f));
				}
			});

			this.mShowing = true;
		}
	}

	public void dismiss() {
		// mScene.attachChild(mBackground);
		this.mBackground.registerEntityModifier(new AlphaModifier(0.15f, 0.8f,
				0));
		// mScene.attachChild(mWindow);
		this.mWindow.registerEntityModifier(new ScaleModifier(0.15f, 0.8f,
				0.00001f));

		this.mShowing = false;
	}

	public boolean isShowing() {
		return mShowing;
	}
	
	public void setPath(final String pPath) {
		this.mPath = pPath;
	}
}
