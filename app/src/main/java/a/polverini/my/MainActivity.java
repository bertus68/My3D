package a.polverini.my;

import android.app.*;
import android.os.*;
import android.opengl.*;
import android.opengl.GLSurfaceView.*;
import android.view.*;
import android.content.*;
import android.graphics.*;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;
import java.nio.*;
import java.io.*;

public class MainActivity extends Activity 
{

	private GLSurfaceView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
							 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		glView = findViewById(R.id.GLVIEW);
		glView.setRenderer(new MyRenderer3D(this));
    }

	@Override 
	protected void onPause() {
		super.onPause(); 
		glView.onPause(); 
	} 

	@Override
	protected void onResume() {
		super.onResume(); 
		glView.onResume();
	}

	public static class MyRenderer3D implements Renderer {

		private Context context;
		private Cube cube;
		private Light light;
		private LookAt lookAt;

		public MyRenderer3D(Context context) {
			this.context = context;
			lookAt = new LookAt();
			light = new Light();
			cube = new Cube();
		} 

		@Override 
		public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

			gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glShadeModel(GL10.GL_SMOOTH); 
			gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); 
			gl.glClearDepthf(1.0f); 
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glDepthFunc(GL10.GL_LEQUAL);
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
			gl.glDisable(GL10.GL_DITHER);

			light.create(gl, config);
			cube.loadTexture(gl, context);
		}

		@Override 
		public void onSurfaceChanged(GL10 gl, int width, int height) { 
			if (height == 0) height = 1; 
			float aspect = (float)width / height;
			gl.glViewport(0, 0, width, height); 
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();

			GLU.gluPerspective(gl, 45, aspect, 0.1f, 100.f); 
			gl.glMatrixMode(GL10.GL_MODELVIEW); 
			gl.glLoadIdentity();
		} 

		@Override 
		public void onDrawFrame(GL10 gl) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			gl.glLoadIdentity(); 
			lookAt.draw(gl);
			light.draw(gl);
			cube.draw(gl);
		}
	}

	public static abstract class Drawable {
		public abstract void draw(GL10 gl);
	}

	public static class LookAt extends Drawable {
		class Position {
			float x, y, z;
			public Position(float x, float y, float z) {
				this.x = x;
				this.y = y;
				this.z = z;
			}
		}
		
		Position eye = new Position(0.0f, 1.8f, 0.0f);
		Position center = new Position(0.0f, 0.0f, -6.0f);
		Position up = new Position(0.0f, 1.0f, 0.0f);

		public void draw(GL10 gl) {
			GLU.gluLookAt(gl, eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z);
		}
	}

	public static class Light extends Drawable {
		private boolean enabled = true; 
		private float[] ambient = {0.5f, 0.5f, 0.5f, 1.0f}; 
		private float[] diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
		private float[] position = {0.0f, 0.0f, 2.0f, 1.0f}; 

		public void create(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, ambient, 0); 
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, diffuse, 0);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, position, 0); 
			gl.glEnable(GL10.GL_LIGHT1);
			gl.glEnable(GL10.GL_LIGHT0); 
		}

		@Override
		public void draw(GL10 gl) {
			if (enabled) {
				gl.glEnable(GL10.GL_LIGHTING); 
			} else { 
				gl.glDisable(GL10.GL_LIGHTING); 
			} 
		}
	}

	public static class Cube {
		private static float angle = 0.0f;
		private static float speed = -1.5f;
		private static int texture = 2;

		private FloatBuffer vertexBuffer; 
		private FloatBuffer texBuffer; 

		private float[] vertices = {
			-1.0f, -1.0f, 0.0f, 
			1.0f, -1.0f, 0.0f, 
			-1.0f, 1.0f, 0.0f, 
			1.0f, 1.0f, 0.0f  
		};

		float[] texCoords = { 
			0.0f, 1.0f, 
			1.0f, 1.0f, 
			0.0f, 0.0f,
			1.0f, 0.0f
		}; 

		public Cube() {
			ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); 
			vbb.order(ByteOrder.nativeOrder()); 
			vertexBuffer = vbb.asFloatBuffer(); 
			vertexBuffer.put(vertices); 
			vertexBuffer.position(0); 

			ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4); 
			tbb.order(ByteOrder.nativeOrder()); 
			texBuffer = tbb.asFloatBuffer();
			texBuffer.put(texCoords);
			texBuffer.position(0); 
		} 

		public void draw(GL10 gl) { 

			gl.glPushMatrix();
			
			gl.glTranslatef(0.0f, 0.0f, -6.0f);
			gl.glScalef(0.8f, 0.8f, 0.8f); 
			gl.glRotatef(angle, 1.0f, 1.0f, 1.0f); 
			// angle += speed; 

			gl.glFrontFace(GL10.GL_CCW); 
			gl.glEnable(GL10.GL_CULL_FACE); 
			gl.glCullFace(GL10.GL_BACK);

			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer); 
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer); 
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[texture]);

			gl.glPushMatrix(); 
			gl.glTranslatef(0.0f, 0.0f, 1.0f); 
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4); 
			gl.glPopMatrix(); 

			gl.glPushMatrix();
			gl.glRotatef(270.0f, 0.0f, 1.0f, 0.0f); 
			gl.glTranslatef(0.0f, 0.0f, 1.0f); 
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4); 
			gl.glPopMatrix(); 

			gl.glPushMatrix();
			gl.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
			gl.glTranslatef(0.0f, 0.0f, 1.0f);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4); 
			gl.glPopMatrix(); 

			gl.glPushMatrix(); 
			gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f); 
			gl.glTranslatef(0.0f, 0.0f, 1.0f);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
			gl.glPopMatrix(); 

			gl.glPushMatrix();
			gl.glRotatef(270.0f, 1.0f, 0.0f, 0.0f); 
			gl.glTranslatef(0.0f, 0.0f, 1.0f); 
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4); 
			gl.glPopMatrix();

			gl.glPushMatrix(); 
			gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f); 
			gl.glTranslatef(0.0f, 0.0f, 1.0f); 
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4); 
			gl.glPopMatrix(); 
			
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); 
			gl.glDisable(GL10.GL_CULL_FACE);

			gl.glPopMatrix(); 
		} 

		public void rotate(GL10 gl, float x, float y, float z){
			gl.glRotatef(x, 1.0f, 0.0f, 0.0f);
			gl.glRotatef(y, 0.0f, 1.0f, 0.0f);
			gl.glRotatef(z, 0.0f, 0.0f, 1.0f);
		}

		private int[] textures = new int[3]; 

		public void loadTexture(GL10 gl, Context context) {

			InputStream istream = context.getResources().openRawResource(R.drawable.texture); 
			Bitmap bitmap; 
			try { 
				bitmap = BitmapFactory.decodeStream(istream); 
			} finally { 
				try { 
					istream.close(); 
				} catch(IOException e) { } 
			}

			gl.glGenTextures(3, textures, 0);

			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]); 
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0); 

			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1]); 
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); 
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0); 

			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[2]);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); 
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST); 
			if(gl instanceof GL11) { 
				gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
			} 

			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0); bitmap.recycle(); 
		}
	}
}
