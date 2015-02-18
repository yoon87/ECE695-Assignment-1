################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/jni_part.cpp \
../jni/nonfree_jni.cpp 

OBJS += \
./jni/jni_part.o \
./jni/nonfree_jni.o 

CPP_DEPS += \
./jni/jni_part.d \
./jni/nonfree_jni.d 


# Each subdirectory must supply rules for building sources it contributes
jni/%.o: ../jni/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C++ Compiler'
	g++ -I"D:\adt-bundle-windows-x86_64-20131030\NDK\android-ndk-r10d/platforms/android-9/arch-arm/usr/include" -I"D:\adt-bundle-windows-x86_64-20131030\NDK\android-ndk-r10d/sources/cxx-stl/gnu-libstdc++/4.6/include" -I"D:\adt-bundle-windows-x86_64-20131030\NDK\android-ndk-r10d/sources/cxx-stl/gnu-libstdc++/4.6/libs/armeabi-v7a/include" -I"D:/Workspace/OpenCV4Android/OpenCV-2.4.10-android-sdk/OpenCV-2.4.10-android-sdk/samples/image-matcher-master/../../sdk/native/jni/include" -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


