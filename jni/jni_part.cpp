#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <stdio.h>
#include "opencv2/calib3d/calib3d.hpp"

#include <android/log.h>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/nonfree/features2d.hpp>
#include <opencv2/nonfree/nonfree.hpp>
#include <iostream>

#define  LOG_TAG    "nonfree_jni_demo"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
	const char * imgInFile = "/storage/emulated/0/IMG_LIB/Hobbit/image5.jpg";
	const char * imgOutFile = "/storage/emulated/0/IMG_LIB/result1.jpg";
	const char * imgOutFile2 = "/storage/emulated/0/IMG_LIB/result2.jpg";
	const char * imgCmpFile = "/storage/emulated/0/IMG_LIB/Hobbit/image63.jpg";

	Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> keypoints;
    vector<KeyPoint> keypoints2;
    vector<DMatch> matches;
    vector<DMatch> good_matches;

    ORB detector;

    BFMatcher matcher;

	Mat img1;
	Mat img2;

	Mat descriptors;
	Mat descriptors2;

	img1 = imread(imgInFile, CV_LOAD_IMAGE_GRAYSCALE);
	img2 = imread(imgCmpFile, CV_LOAD_IMAGE_GRAYSCALE);

	if(! img1.data )
	{
	}

    // Create a SIFT keypoint detector.
	// detecting keypoints
	//SurfFeatureDetector detector_surf(2000);
	//SurfDescriptorExtractor extractor;
	//detector_surf.detect(img1, keypoints);
	//detector_surf.detect(img2, keypoints2);
	LOGI("SIFT1\n");


    detector.detect(img1, keypoints);
    detector.detect(img2, keypoints2);
    //LOGI("Detected %d keypoints\n", (int) keypoints.size());
    detector.compute(img1,keypoints,descriptors);
    detector.compute(img2,keypoints2,descriptors2);
	LOGI("SIFT2\n");

    FileStorage fs;
    	fs.open("/storage/emulated/0/IMG_LIB/descriptors.xml", FileStorage::WRITE);
    	fs << "descriptors" << descriptors;
    	fs.release();

   	FileStorage fs1;
    	fs1.open("/storage/emulated/0/IMG_LIB/descriptors2.xml", FileStorage::WRITE);
    	fs1 << "descriptors" << descriptors2;
    	fs1.release();

    for( unsigned int i = 0; i < keypoints.size(); i++ )
    {
        const KeyPoint& kp = keypoints[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }


    // Show keypoints in the output image.
    	Mat outputImg;
    	Scalar keypointColor = Scalar(255, 0, 0);
    	drawKeypoints(img1, keypoints, outputImg, keypointColor, DrawMatchesFlags::DRAW_RICH_KEYPOINTS);
    	imwrite(imgOutFile, outputImg);

    	matcher.match(descriptors,descriptors2,matches);
    	LOGI("SIFT3\n");

    	//-- Draw matches
    	Mat img_matches;
    	drawMatches(img1, keypoints, img2, keypoints2, matches, img_matches);
      	imwrite(imgOutFile2, img_matches);


      	DMatch match;
      	int i = descriptors.rows;
   		//LOGI("match value", descriptors.rows);

   	    for( int i = 0; i < descriptors.rows; i++ ){
   	    	if (match.distance < 500 ){
   	    		match = matches[i];
   	    		good_matches.push_back(match);
   	    	}
   	    }

   	    int DistanceFilteredMatches;

   	    DistanceFilteredMatches = good_matches.size();

   	    int size;

      	FileStorage fs2;
      	    	fs2.open("/storage/emulated/0/IMG_LIB/match_result.xml", FileStorage::WRITE);
      	    	fs2 << "matches";
      	    	fs2.release();
}
}
