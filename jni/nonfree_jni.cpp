#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/flann/flann.hpp>
#include <vector>
#include <stdio.h>
#include "opencv2/calib3d/calib3d.hpp"
#include <string>
#include <android/log.h>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>
#include <opencv2/nonfree/features2d.hpp>
#include <opencv2/nonfree/nonfree.hpp>

using namespace cv;
using namespace std;



#define  LOG_TAG    "Native_Side"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

typedef unsigned char uchar;

extern "C" {
    JNIEXPORT void JNICALL Java_sang_mobile_visual_search_MainActivity_buildLib(JNIEnv * env, jobject, jstring addrFile);
    JNIEXPORT void JNICALL Java_sang_mobile_visual_search_MainActivity_loadLib(JNIEnv * env, jobject, jlong addDescription,jstring addrFile);
    JNIEXPORT void JNICALL Java_sang_mobile_visual_search_MainActivity_refdata(JNIEnv * env, jobject, jlong addDescription,jstring addrFile);
    JNIEXPORT jint JNICALL Java_sang_mobile_visual_search_MainActivity_match(JNIEnv * env, jobject, jlong addDescription1, jlong addDescription2);
};

JNIEXPORT void JNICALL Java_sang_mobile_visual_search_MainActivity_buildLib(JNIEnv *env, jobject thisObj, jstring addrFile)
{
//		LOGI( "Start build library! \n");
		//cv::initModule_nonfree();

		// Input and output image path.
   		const char *inCStr = env->GetStringUTFChars(addrFile, NULL);

   		// Input and output image path.
   		const char * imgInFile = inCStr;
   		std::string FileLoc = imgInFile;

   		env->ReleaseStringUTFChars(addrFile, inCStr);  // release resources

		Mat image;
		Mat imageOut;
		image = imread(imgInFile, CV_LOAD_IMAGE_GRAYSCALE);
		imageOut = image;
		resize(image,image,cvSize(0,0),0.4,0.4);

		if(! image.data )
		{
			LOGI("Could not open or find the image!\n");
		}

		//vector<KeyPoint> *pMatKp = (vector<KeyPoint>*)addKp;

		vector<KeyPoint> keypoints;
		// Create a SURF keypoint detector.
		SurfFeatureDetector detector(450);
		detector.detect(image, keypoints);

		// Create a SIFT keypoint detector
		//SiftFeatureDetector detector;
		//detector.detect(image, keypoints);

		// Create a ORB keypoint detector
		//ORB detector(500);
		//detector.detect(image, keypoints);

/*
		for( unsigned int i = 0; i < keypoints.size(); i++ )
				    {
				        const KeyPoint& kp = keypoints[i];
				        circle(imageOut, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
				    }

		std::string b =  imgInFile;
		b += "_result.jpg";
		imwrite( b, imageOut);
		*/

		// Compute feature description.
		Mat descriptors;
		SurfDescriptorExtractor extractor;
		//SiftDescriptorExtractor extractor;
		extractor.compute(image,keypoints,descriptors);

		//For ORB
		//detector.compute(image,keypoints,descriptors);

		// Store description to "FilePath.xml" for SURF "FilePathSIFT.xml" for SIFT
		//FileLoc += "xml";
		FileLoc += ".xml";
		FileStorage fs;
		fs.open(FileLoc, FileStorage::WRITE);
		fs << "descriptors" << descriptors;
		fs.release();

		LOGI("End BUILD LIBRARY");
}

JNIEXPORT void JNICALL Java_sang_mobile_visual_search_MainActivity_loadLib(JNIEnv *env, jobject thisObj, jlong addDescription, jstring addrFile)
{
	//LOGI("Load Library Start");
	   	const char *inCStr = env->GetStringUTFChars(addrFile, NULL);

		// Input and output image path.
		const char * imgInFile = inCStr;
		std::string FileLoc = imgInFile;

		env->ReleaseStringUTFChars(addrFile, inCStr);  // release resources

		Mat& pMatDesc = *(Mat*)addDescription;

		FileStorage fs;
		fs.open(FileLoc, FileStorage::READ);
		fs["descriptors"] >> pMatDesc;
		fs.release();
}

JNIEXPORT jint JNICALL Java_sang_mobile_visual_search_MainActivity_match(JNIEnv * env, jobject, jlong addDescription1, jlong addDescription2)
{
		//LOGI("START MATCH");
		Mat& descriptor1 = *(Mat*)addDescription1;
		Mat& descriptor2 = *(Mat*)addDescription2;

		//Mat img_1 = imread("/storage/emulated/0/IMG_LIB/Analog/image11.jpg", CV_LOAD_IMAGE_GRAYSCALE );
		//Mat img_2 = imread("/storage/emulated/0/IMG_LIB/Analog/image16.jpg", CV_LOAD_IMAGE_GRAYSCALE );

		  //.LOGI("COMPUTE");


		  //-- Step 3: Matching descriptor vectors using FLANN matcher

		  FlannBasedMatcher matcher;
		  //FlannBasedMatcher matcher(new flann::KDTreeIndexParams(4), new flann::SearchParams(0));
		  // construct an randomized kd-tree index using 4 kd-trees

		  //flann::Index<flann::L2<float> > index(dataset, flann::KDTreeIndexParams(4));
		  //index.buildIndex();



		//std::vector<vector<DMatch > > matches;
		  std::vector< DMatch > matches;
		  //Mat mask;
		  //matcher.knnMatch(descriptor1,descriptor2, matches,10);
		  matcher.match(descriptor1, descriptor2, matches);
		  //LOGI("Matched");



		  double max_dist = 0; double min_dist = 100;

		  //-- Quick calculation of max and min distances between keypoints
		  for( int i = 0; i < descriptor1.rows; i++ )
		  { double dist = matches[i].distance;
		    if( dist < min_dist ) min_dist = dist;
		    if( dist > max_dist ) max_dist = dist;
		  }


		  //-- Draw only "good" matches (i.e. whose distance is less than 2*min_dist,
		  //-- or a small arbitary value ( 0.02 ) in the event that min_dist is very
		  //-- small)
		  //-- PS.- radiusMatch can also be used here.
		  std::vector< DMatch > good_matches;

		  for( int i = 0; i < descriptor1.rows; i++ )
		  		  { if( matches[i].distance <= 0.1) // 0.1 for hessian = 400
		  		    { good_matches.push_back( matches[i]); }
		  		  }
/*
		  for( int i = 0; i < descriptor1.rows; i++ )
		  { if( matches[i].distance <= max(2*min_dist, 0.02) )
		    { good_matches.push_back( matches[i]); }
		  }

	  //-- Draw only "good" matches
		  Mat img_matches;
		  drawMatches( img_1, keypoints_1, img_2, keypoints_2,
		               good_matches, img_matches, Scalar::all(-1), Scalar::all(-1),
		               vector<char>(), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS );

		  //-- Show detected matches
		  imwrite( "/storage/emulated/0/IMG_LIB/result.jpg", img_matches );

		  for( int i = 0; i < (int)good_matches.size(); i++ )
		  { printf( "-- Good Match [%d] Keypoint 1: %d  -- Keypoint 2: %d  \n", i, good_matches[i].queryIdx, good_matches[i].trainIdx ); }

		  LOGI("END MATCH");*/

		  //return matches.size();
		  return (int)good_matches.size();

		 // LOGI("END MATCH");
		  //return -1;
	}

JNIEXPORT void JNICALL Java_sang_mobile_visual_search_MainActivity_refdata(JNIEnv * env, jobject, jlong addDescription, jstring addrFile){

	// Input and output image path.
	Mat& pMatDesc = *(Mat*)addDescription;
	const char *inCStr = env->GetStringUTFChars(addrFile, NULL);

	// Input and output image path.
	const char * imgInFile = inCStr;
	std::string FileLoc = imgInFile;

	env->ReleaseStringUTFChars(addrFile, inCStr);  // release resources

	Mat image;
	image = imread(imgInFile, CV_LOAD_IMAGE_GRAYSCALE);
	//resize(image,image,cvSize(0,0),0.9,0.9);

	if(! image.data )
	{
		LOGI("Could not open or find the image!\n");
	}

	//vector<KeyPoint> *pMatKp = (vector<KeyPoint>*)addKp;

	// SURF Detector
	vector<KeyPoint> keypoints;
	SurfFeatureDetector detector(450);
	//SiftFeatureDetector detector;


	detector.detect(image, keypoints);

	// Compute feature description.
	Mat descriptors;
	SurfDescriptorExtractor extractor;
	//SiftDescriptorExtractor extractor;

	extractor.compute(image,keypoints,pMatDesc);

	LOGI("End REF LIBRARY");
}
