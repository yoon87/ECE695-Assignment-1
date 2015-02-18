package sang.mobile.visual.search;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.*;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";
	private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;

    List<String> FilePath = new ArrayList<String>();
    List<String> Sorted_FilePath = new ArrayList<String>();

    public int count=0;
	
	public int Filesize = 10;
	
	public int library_num = 0;

	public Mat[] arrayDes = new Mat[105];
		
	public Mat ref;
	
	public int[] rank = new int[5];

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				System.loadLibrary("opencv_java");
	    		System.loadLibrary("nonfree");
	    		System.loadLibrary("nonfree_jni");
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public MainActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	ImageView matchDrawArea;
	ImageView matchPic;
	ImageView similarPic1;
	ImageView similarPic2;
	ImageView similarPic3;
	ImageView similarPic4;
	TextView result;
	TextView similarText1;
	TextView similarText2;
	TextView similarText3;
	TextView similarText4;

	
	Button addBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main_layout);
		matchDrawArea = (ImageView) findViewById(R.id.refImageView);

		//matchPic = (ImageView) findViewById(R.id.match_img);
		similarPic1 = (ImageView) findViewById(R.id.similar_img1);
		similarPic2 = (ImageView) findViewById(R.id.similar_img2);
		similarPic3 = (ImageView) findViewById(R.id.similar_img3);
		similarPic4 = (ImageView) findViewById(R.id.similar_img4);
		result = (TextView) findViewById(R.id.resultText);
		similarText1 = (TextView) findViewById(R.id.simText1);
		similarText2 = (TextView) findViewById(R.id.simText2);
		similarText3 = (TextView) findViewById(R.id.simText3);
		similarText4 = (TextView) findViewById(R.id.simText4);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);		
				
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
	}

	boolean resize = false;
	boolean imageOnly = true;

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	 @Override
	public void onResume() {
		super.onResume();
	    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {

	}

	public void onCameraViewStopped() {
	}

	boolean showOriginal = true;

	public void cameraclick(View w) {
		showOriginal = !showOriginal;
	}
	
	Mat last;

	private Context context;

	public void takePic2(View w) {
		//String imgInFile = Environment.getExternalStorageDirectory().getPath() + "/IMG_LIB/Android/image41.jpg";
		//Mat im = Highgui.imread(imgInFile);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final File ref_file = new File(Environment.getExternalStorageDirectory().getPath() + "/ref.jpg");
		Mat im = last.clone();
		
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2RGB);
		
		Bitmap bmp = Bitmap.createBitmap(im.cols(), im.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(im, bmp);
		
		matchDrawArea.setImageBitmap(bmp);
		
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

		try {
			ref_file.createNewFile();
			FileOutputStream fo = new FileOutputStream(ref_file);
			fo.write(bytes.toByteArray());
			fo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ref = new Mat();
		refdata(ref.getNativeObjAddr(),"/storage/emulated/0/ref.jpg");		
		Log.i("REFERENCE","Array size" + ref.size().toString());
		
		//refScene = new Scene(im);
	}
	
	public void buildLib(View w) {
		buildLibrary();
		Toast.makeText(MainActivity.this, "Libary Built!", Toast.LENGTH_LONG).show();
	}

	public void loadLib(View w) {
		loadLibrary();
		Toast.makeText(MainActivity.this, "Loaded Libary!", Toast.LENGTH_LONG).show();
	}
	
	public void Find_Match(View w) {
		Find_Match();
		Toast.makeText(MainActivity.this, "Exact Match Found!", Toast.LENGTH_LONG).show();
	}
	
	public void Similar_Match(View w) {
		Similar_Match();
		Toast.makeText(MainActivity.this, "Similar Matches are Found!", Toast.LENGTH_LONG).show();
	}
	
	Bitmap bmp;

	public void buildLibrary(){
		final File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/IMG_LIB/");
		listFilesForFolder(folder);
		Log.i("FileName", " " + FilePath.size());
		library_num = 0;
		
		//int i = 0;
		//while(i < 1){
		for (int i = 0; i< FilePath.size(); i++){
			if (FilePath.get(i).contains("jpg") == true && FilePath.get(i).contains("xml") == false){
				Log.i("Build Library",FilePath.get(i).toString());
				buildLib(FilePath.get(i));
				library_num++;
			}else if(FilePath.get(i).contains("JPG") && FilePath.get(i).contains("xml")==false){  
				Log.i("Build Library",FilePath.get(i).toString());
				buildLib(FilePath.get(i));
				library_num++;
			}
		}
		Log.i("FileName","Number of Files Built" + library_num);
	}

	public void loadLibrary(){
		final File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/IMG_LIB/");
		listFilesForFolder(folder);
		Log.i("FileName", " " + FilePath.size());
		
		int j = 0;
		
		//int i = 0;
		//while(i < 50){
		Log.i("",""+FilePath.size());

		for (int i = 0; i<FilePath.size();i++){
			if (FilePath.get(i).contains("xml")&& FilePath.get(i).contains("SIFT")==false){   // For SURF
			//if (FilePath.get(i).contains("SIFT")){     // For SIFT
				arrayDes[j]=new Mat();
				//Log.i("Load Library in IF",FilePath.get(i).toString());
				loadLib(arrayDes[j].getNativeObjAddr(),FilePath.get(i));
				j++;
			}
		}
		Log.i("FileName","Number of Files Loaded" + j);
		
		// Sorting Image Order
		j = 0;
		for (int i = 0; i<FilePath.size();i++){
			if (FilePath.get(i).contains("jpg") && FilePath.get(i).contains("xml")==false){  
				listFilesForFolder_Sorted(i);
				//Log.i("FileName","Sorted Files" + Sorted_FilePath.get(j));
				j++;
			}else if(FilePath.get(i).contains("JPG") && FilePath.get(i).contains("xml")==false){  
				listFilesForFolder_Sorted(i);
				//Log.i("FileName","Sorted Files" + Sorted_FilePath.get(j));
				j++;
			}
		}
		//Log.i("FileName","Number of Files in Sorted Array" + Sorted_FilePath.size());
	}
	
	public void Find_Match(){		
		final File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/IMG_LIB/");
		final int[] match_score = new int[101];
		listFilesForFolder(folder);
		//Log.i("FileName", " " + FilePath.size());
		int j = 0;
		int good_match = 0;
		int compare = 0;
		int index = 0;
		/*
		for (int i =0; i< FilePath.size(); i++){
			FilePath.get(i).contains("xml");
			FilePath.remove(i);
		}
		
		Log.i("FileName","Sorted File Size"+FilePath.size());
*/	
		for (int i =0; i< 101; i++){
			good_match = match(ref.getNativeObjAddr(), arrayDes[i].getNativeObjAddr());
			//Log.i("FileName",""+Sorted_FilePath.get(i).toString());
			Log.i("FileName","Match Score of "+i+" is "+good_match);
			if (good_match > compare){
				compare = good_match;
				index = i;
			}
		}
		
		Log.i("FileName","Most matched Index: "+index);

		result.setText(Sorted_FilePath.get(index).toString());
		Log.i("FileName","Most matched: "+Sorted_FilePath.get(index).toString());
	}
	
	public void Similar_Match(){		
		final File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/IMG_LIB/");
		final int[] match_score = new int[101];
		listFilesForFolder(folder);
		int j = 0;
		int good_match = 0;
		int compare = 0;
		int index = 0;

		for (int i =0; i< 101; i++){
			good_match = match(ref.getNativeObjAddr(), arrayDes[i].getNativeObjAddr());
			//Log.i("FileName",""+Sorted_FilePath.get(i).toString());
			Log.i("FileName","Match Score of "+i+" is "+good_match);
			match_score[i] = good_match;
		}
		
		int temp_index = 0;
		for (int k = 0; k< 5; k++){
			for (int i = 1; i< 101; i++){
				if(match_score[temp_index] < match_score[i]){
					rank[k] = i;
					temp_index = i;
				}
			}
			match_score[temp_index] = 0;
		}
		
		Log.i("FileName","Similar matched Index1: "+rank[0]);
		Log.i("FileName","Similar matched Index2: "+rank[1]);
		Log.i("FileName","Similar matched Index3: "+rank[2]);
		Log.i("FileName","Similar matched Index4: "+rank[3]);

		similarText1.setText(Sorted_FilePath.get(rank[0]).toString());
		similarText2.setText(Sorted_FilePath.get(rank[1]).toString());
		similarText3.setText(Sorted_FilePath.get(rank[2]).toString());
		similarText4.setText(Sorted_FilePath.get(rank[3]).toString());

		Log.i("FileName","Similar Images 1 is: "+Sorted_FilePath.get(rank[0]).toString());
		Log.i("FileName","Similar Images 2 is: "+Sorted_FilePath.get(rank[1]).toString());
		Log.i("FileName","Similar Images 3 is: "+Sorted_FilePath.get(rank[2]).toString());
		Log.i("FileName","Similar Images 4 is: "+Sorted_FilePath.get(rank[3]).toString());
		
		Mat im = Highgui.imread(Sorted_FilePath.get(rank[1]).toString());
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2RGB);
		Bitmap bmp = Bitmap.createBitmap(im.cols(), im.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(im, bmp);
		im.release();
		bmp =Bitmap.createScaledBitmap(bmp, 300, 200, true);
		similarPic1.setImageBitmap(bmp);
		
		im = Highgui.imread(Sorted_FilePath.get(rank[2]).toString());
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2RGB);
		bmp = Bitmap.createBitmap(im.cols(), im.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(im, bmp);
		im.release();
		bmp =Bitmap.createScaledBitmap(bmp, 300, 200, true);
		similarPic2.setImageBitmap(bmp);
		
		im = Highgui.imread(Sorted_FilePath.get(rank[3]).toString());
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2RGB);
		bmp = Bitmap.createBitmap(im.cols(), im.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(im, bmp);
		im.release();
		bmp =Bitmap.createScaledBitmap(bmp, 300, 200, true);
		similarPic3.setImageBitmap(bmp);
		
		im = Highgui.imread(Sorted_FilePath.get(rank[4]).toString());
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2RGB);
		bmp = Bitmap.createBitmap(im.cols(), im.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(im, bmp);
		im.release();
		bmp =Bitmap.createScaledBitmap(bmp, 300, 200, true);
		similarPic4.setImageBitmap(bmp);
	}
	
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		last = inputFrame.rgba();
		return last;
	}
	
	public void listFilesForFolder(final File folder) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	FilePath.add(fileEntry.getAbsolutePath());
	        }
	    }
	}
	
	public void listFilesForFolder_Sorted(int index) {
	        	Sorted_FilePath.add(FilePath.get(index));
	}
		
    public native void buildLib(String addrFile);
    public native void loadLib(long addDescription, String addrFile);
    public native void refdata(long addDescription, String addrFile);
    public native int match(long addDescription1, long addDescription2);
}
