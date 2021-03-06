package a.polverini.my;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.opengl.*;
import android.opengl.GLSurfaceView.*;
import android.os.*;
import android.preference.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import javax.microedition.khronos.opengles.*;

public class MainActivity extends Activity 
{
	private static final String TAG = "My3D";
	private static final String DEBUG = "DEBUG: ";
	private static final String ERROR = "ERROR: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTitle(TAG+" build 000001");
		new Timer().schedule(new TimerTask() {
				public void run() {
					runOnUiThread(new Runnable() {
							public void run() {
								startActivity(new Intent(MainActivity.this, MonoActivity.class));
							}
						});
				}
			}, 3000); 
	}
	
	private static boolean debug = false;
	
	public static void debug(String s) {
		if(debug) {
			System.out.println(DEBUG+s);
		}
	}
	
	private static boolean verbose = true;
	
	public static void verbose(String s) {
		if(verbose) {
			System.out.println(s);
		}
	}

	public static void error(Exception e) {
		System.out.println(ERROR+e.getClass().getSimpleName()+" "+e.getMessage());
	}
	
    public static class StereoActivity extends Activity {
		
		enum SIDE {
			LEFT,
			RIGHT
			}
			
		private ProgressBar progress; 
		private TextView logView;
		private GLSurfaceView leftView;
		private GLSurfaceView rightView;
		
		@Override
		protected void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
								 WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.stereo);
			
			logView = this.findViewById(R.id.LOGVIEW);
			if(logView!=null) {
				TextHandler.instance(logView);
				System.out.println(getTitle());
				System.out.println("A.Polverini");
			}
			
			progress = this.findViewById(R.id.PROGRESS);
			
			leftView = findViewById(R.id.LEFTVIEW);
			leftView.setRenderer(new StereoRenderer(this, SIDE.LEFT));

			rightView = findViewById(R.id.RIGHTVIEW);
			rightView.setRenderer(new StereoRenderer(this, SIDE.RIGHT));
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}

		@Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			boolean log = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("log", true);
			menu.findItem(R.id.MENU_LOG).setChecked(log);
			findViewById(R.id.LOGVIEW).setVisibility(log ? View.VISIBLE : View.GONE);
			return true;
		}

		@Override public boolean onOptionsItemSelected(MenuItem item) {
			try {
				switch (item.getItemId()) {
					case R.id.MENU_LOG:
						item.setChecked(!item.isChecked());
						PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("log", item.isChecked()).apply();
						findViewById(R.id.LOGVIEW).setVisibility(item.isChecked() ? View.VISIBLE : View.GONE);
						return true;
					default:
						return super.onOptionsItemSelected(item);
				}
			} catch(Exception e) {
				error(e);
			}
			return true;
		}

		@Override 
		protected void onPause() {
			super.onPause(); 
			leftView.onPause(); 
			rightView.onPause(); 
		} 

		@Override
		protected void onResume() {
			super.onResume(); 
			leftView.onResume();
			rightView.onResume();
		}
		
		public class StereoRenderer implements Renderer {

			private final Context context;
			private final SIDE side;

			private Cube cube;
			private Cube[][] cubes = new Cube[10][10];

			private Light light;
			private LookAt lookAt;

			public StereoRenderer(Context context, SIDE side) {
				this.context = context;
				this.side = side;

				lookAt = new LookAt(
					new Position(0.0f+(side == SIDE.LEFT ? +0.1f : -0.1f), 10.0f, 0.0f),
					new Position(0.0f, 5.0f, -20.0f),
					new Position(0.0f, 1.0f, 0.0f));

				light = new Light();

				//cube = new Cube();
				//cube.setPosition(new Position(0.0f, 0.0f, -6.0f));

				for(int ix=0;ix<9;ix++) {
					for(int iz=0;iz<9;iz++) {
						cubes[ix][iz] = new Cube();
						cubes[ix][iz].setPosition(new Position(-10f+(2.0f*ix), 0.0f, -20.0f-(2.0f*iz)));
					}
				}

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
				loadTexture(gl, context);
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
				// cube.draw(gl);

				for(int ix=0;ix<9;ix++) {
					for(int iz=0;iz<9;iz++) {
						cubes[ix][iz].draw(gl);
					}
				}

			}
		}
	}
	
	public static class MonoActivity extends Activity {
		
		private ProgressBar progress; 
		private TextView logView;
		private GLSurfaceView glView;
		
		@Override
		protected void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.mono);
			setTitle(TAG+" build 000001");

			logView = this.findViewById(R.id.LOGVIEW);
			TextHandler.instance(logView);
			System.out.println(getTitle());
			System.out.println("A.Polverini");

			progress = this.findViewById(R.id.PROGRESS);
			
			glView = findViewById(R.id.MONOVIEW);
			glView.setRenderer(new MonoRenderer(this));
			
		}
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}

		@Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			boolean log = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("log", true);
			menu.findItem(R.id.MENU_LOG).setChecked(log);
			findViewById(R.id.LOGVIEW).setVisibility(log ? View.VISIBLE : View.GONE);
			return true;
		}

		@Override public boolean onOptionsItemSelected(MenuItem item) {
			try {
				switch (item.getItemId()) {
					case R.id.MENU_LOG:
						item.setChecked(!item.isChecked());
						PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("log", item.isChecked()).apply();
						findViewById(R.id.LOGVIEW).setVisibility(item.isChecked() ? View.VISIBLE : View.GONE);
						return true;
					default:
						return super.onOptionsItemSelected(item);
				}
			} catch(Exception e) {
				error(e);
			}
			return true;
		}
		
		public class MonoRenderer implements Renderer {

			private final Context context;
			
			private Cube cube;
			
			private Light light;
			private LookAt lookAt;

			public MonoRenderer(Context context) {
				this.context = context;
				
				lookAt = new LookAt(
					new Position(5.0f, 5.0f, 20.0f),
					new Position(0.0f, 0.0f, 0.0f),
					new Position(0.0f, 1.0f, 0.0f));

				light = new Light();

				cube = new Cube();
				cube.setPosition(new Position(0.0f, 0.0f, 0.0f));
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
				loadTexture(gl, context);
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
	}
	
	private static class TextHandler extends android.os.Handler
	{
		private final int MESSAGE = 100;
		private final TextView text;

		public static TextHandler instance(TextView textView) {
			try {
				final TextHandler textHandler = new TextHandler(textView);
				System.setOut(new PrintStream(System.out) {

						@Override
						public PrintStream printf(String format, Object... args) {
							textHandler.print(format, args);
							return this;
						}

						@Override
						public void print(String s) {
							printf(s);
						}

						@Override
						public void println(String s) {
							printf(s+"\n");
						}

						@Override
						public void println() {
							printf("\n");
						}
					});
				return textHandler;
			} catch (Exception e) {
				error(e);
			}
			return null;
		}

		public TextHandler(TextView text) {
			super(Looper.getMainLooper());
			this.text = text;
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
				case MESSAGE:
					try {
						String s = message.getData().getString("text");
						if(s.startsWith(DEBUG)) {
							text.append(Html.fromHtml("<font color=\"blue\">" + s.replaceAll("\n","<br>") + "</font>"));
							return;
						}
						if(s.startsWith(ERROR)) {
							text.append(Html.fromHtml("<font color=\"red\">" + s.replaceAll("\n","<br>") + "</font>"));
							return;
						}
						text.append(s);
					} catch(Exception e) {
						text.append(ERROR+e.getClass().getSimpleName()+" "+e.getMessage());
					}
					break;
				default:
					super.handleMessage(message);
					break;
			}
		}

		public void print(String format, Object... args) {
			Message message = obtainMessage(MESSAGE);
			Bundle data = new Bundle();
			data.putString("text", String.format(format, args));
			message.setData(data);
			message.sendToTarget();
		}

	}

	public static abstract class Drawable {
		public abstract void draw(GL10 gl);
	}

	public static class Position {
		public float x, y, z;
		public Position(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	
	public static class LookAt extends Drawable {

		private Position eye;
		private Position center;
		private Position up;
		
		public LookAt() {
			this(new Position(0.0f, 0.0f, 0.0f),
				 new Position(0.0f, 0.0f, 0.0f),
				 new Position(0.0f, 0.0f, 0.0f));
		}
		
		public LookAt(Position eye, Position center, Position up) {
			set(eye, center, up);
		}
		
		public void set(Position eye, Position center, Position up) {
			this.eye = eye;
			this.center = center;
			this.up = up;
		}
		
		public void draw(GL10 gl) {
			GLU.gluLookAt(gl, eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z);
		}

	}
	
	public static class Light extends Drawable {
		private boolean enabled = true; 
		private float[] ambient = {0.5f, 0.5f, 0.5f, 1.0f}; 
		private float[] diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
		private float[] position = {0.0f, 2.0f, 2.0f, 1.0f}; 

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

	public static class Cube extends Drawable {

		private Position p;

		public void setPosition(Position p) {
			this.p = p;
		}
		
		private FloatBuffer mVertexBuffer;
		private FloatBuffer mColorBuffer;
		private ByteBuffer  mIndexBuffer;

		private float vertices[] = {
			-1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f,
			1.0f,  1.0f, -1.0f,
			-1.0f, 1.0f, -1.0f,
			-1.0f, -1.0f,  1.0f,
			1.0f, -1.0f,  1.0f,
			1.0f,  1.0f,  1.0f,
			-1.0f,  1.0f,  1.0f
		};
		private float colors[] = {
			0.0f,  1.0f,  0.0f,  1.0f,
			0.0f,  1.0f,  0.0f,  1.0f,
			1.0f,  0.5f,  0.0f,  1.0f,
			1.0f,  0.5f,  0.0f,  1.0f,
			1.0f,  0.0f,  0.0f,  1.0f,
			1.0f,  0.0f,  0.0f,  1.0f,
			0.0f,  0.0f,  1.0f,  1.0f,
			1.0f,  0.0f,  1.0f,  1.0f
		};

		private byte indices[] = {
			0, 4, 5, 0, 5, 1,
			1, 5, 6, 1, 6, 2,
			2, 6, 7, 2, 7, 3,
			3, 7, 4, 3, 4, 0,
			4, 7, 6, 4, 6, 5,
			3, 0, 1, 3, 1, 2
		};

		public Cube() {
            ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            mVertexBuffer = byteBuf.asFloatBuffer();
            mVertexBuffer.put(vertices);
            mVertexBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            mColorBuffer = byteBuf.asFloatBuffer();
            mColorBuffer.put(colors);
            mColorBuffer.position(0);

            mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
            mIndexBuffer.put(indices);
            mIndexBuffer.position(0);
		}

		public void draw(GL10 gl) {             
            gl.glFrontFace(GL10.GL_CW);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}
	}
	
	private static final String vertexShaderCode =
	"attribute vec4 vPosition;" +
	"void main() {" +
	"  gl_Position = vPosition;" +
	"}";

	private static final String fragmentShaderCode =
	"precision mediump float;" +
	"uniform vec4 vColor;" +
	"void main() {" +
	"  gl_FragColor = vColor;" +
	"}";

	public static int loadShader(int type, String shaderCode){
		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);
		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		return shader;
	}
	
	public static int shader() {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		// create empty OpenGL ES Program
		int program = GLES20.glCreateProgram();
		// add the vertex shader to program
		GLES20.glAttachShader(program, vertexShader);
		// add the fragment shader to program
		GLES20.glAttachShader(program, fragmentShader);
		// creates OpenGL ES program executables
		GLES20.glLinkProgram(program);
		
		return program;
	}
	
	public static class CubeTexture extends Drawable {
		
		private Position p;
		
		public void setPosition(Position p) {
			this.p = p;
		}
		
		private float angle = 0.0f;
		private float speed = -1.5f;
		private int texture = 2;

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

		public CubeTexture() {
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

			gl.glTranslatef(p.x, p.y, p.z);
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
	}
	
	private static int[] textures = new int[3]; 

	public static void loadTexture(GL10 gl, Context context) {

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
