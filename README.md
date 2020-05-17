# FaceIt - protect your applications without any discomfort
This is the repository of our course work. We're students at [Ukrainian Catholic University](https://ucu.edu.ua/), 
[Faculty of Applied Science](https://apps.ucu.edu.ua/en/), Computer Science. Contributors: 
- [Pavlo Hilei](https://github.com/Pavlik1400) (Павло Гілей)
- [Volodymyr Tsapiv](https://github.com/Tsapiv) (Володимир Цапів)
- [Yevhen Pankevych](https://github.com/yewhenp) (Євген Панкевич)

## Motivation
Every day we use many applications on our phone. Staring from just surfing Internet by browser up to showing photos in the gallery. Each time we use different applications, which have a direct access to our private data - photos, videos, docuents or even passwords and personal info. But what if someone wants to get this info illegaly? N that case, our appication can help you to protect your data and applications from someone else's access, and make process of authorization easy.

## Project features
This application allows each Android user to block a number of installed apps on his/her phone and after trying to enter them it allows to use face recognition instead of ordinary password. The password authorization is available too. Of course, before it user should tick some apps to block and make sefie to train algoritm to tecognise him/her. Application work fact and recognise user in a few seconds after entering locked apps. Also, there are some additional functions, like possibility to create many profiles for different tasks (work, free time, etc.), and a well-looking UI fot better experiense of application usage. 

## Repository content
- modules - work directory of main and additional modules
- modules/main - main work repository
- modules/old - folder for tried app realisations on Rython
- examples - folder with examples of using of different python libraries
- docs - contains various documents that explain the project more. Also, you can visit the wiki for more info

## Technical details and structure
Firslty, the whole project was written on Python 3.8, using Dlib and OpenCV for face recognition and Kiwy for android implementation. Even now you can find this modules in modules/old directory.
However, as it turned out, currently it is almost impossible to implement Dlib on Android using Kiwi. So, we decided to write this project using native Android language, Java.

**So, current project:**
- Project is written on Java 12.0.2
- Used Android Studio 3.6
- Android SDK vesion 29
- Android SDK Build-tools version 29.0.3
- Gradle version 3.6.3
- Android NDK version 21.0.6113669

### Algoritm and techinical implementation
We based our project on the [FaceRecognitionApp](https://github.com/Lauszus/FaceRecognitionApp) by Lauszus. We used an Eigenface recognition algoritm, implemented in this project. [About Eigenface](https://habr.com/ru/post/68870/)
Dependencies for this project:
- OpenCV Android SDK version 4.3.0
- Eigen3 version 3.3.7

We partly changed and implemented this project as recognition basis of our app.  See classes:
- `AddUserActivity.java` - Activity with UI for adding user biometrical info (face photos) 
- `CameraBridgeViewBase.java` - interaction with Camera and OpenCV library
- `JavaCameraView.java` - implementation of the Bridge View between OpenCV and Java Camera
- `NativeMethods.java` - implementation of recognition algoritm (uses [FaceRecognitionLib](https://github.com/Lauszus/FaceRecognitionLib), written on C++)
- `UsersDataBase.java` - database for image vectors
- `UserLockRecognitionActivity.java` - Activity with UI for recognising user when some app is locked

Another part of our project is app locker and installed app finder.
- `BlockServise.java` - servise for blocking chosen apps which user tries to open
- `DataBase.java` - database for locked apps
- `Notification.java` - class for creating notification
- `AppAdapter.java` - adapter for recyclerView that shows all installed apps
- `BootStart.java` - starts application on device boot
- `ListOfAppsActivity.java` - activity with all installed apps
- `PasswordActivity.java` - activity for setting and resetting password
- `ProfileItemAdapter.java` - adapter for recyclerView in profiles menu
- `ProfilesActivity.java` - activity for work with profiles

Finally, the main UI class:
- `FaceRecognitionActivity.java` - main UI Activity, which works as menu and which starts `AddUserActivity`, `DetailsActivity` and `BlockServise`. Here user can navidate throught all features of our app

## Project status
Project is on the way to the pre-release version. Just finishing some technical bugs. Version: 1.1.1-beta

Now we are in process of hosting our app on getjar.com

## How to use?
1) **Install Android Studio, SDK and NDK**<br/>
For all platforms (Windows, Linux, MacOS):

- Go to [official web site](https://developer.android.com/studio) and install version for your platform
- Go to Android Studio -> SDK Manager -> SDK Platforms and install Android 10.0 (API level 29)
- Go to Android Studio -> SDK Manager -> SDK Tools and install Android SDK Build-tools version 29.0.3, NDK version 21.0.6113669, CMake version 3.10.2.4988404

2) **Install needed libraries and set environmental variables**<br/>
- Download and unzip in any location:  https://github.com/opencv/opencv/releases/download/4.3.0/opencv-4.3.0-android-sdk.zip
- Download and unzip in any location:  https://gitlab.com/libeigen/eigen/-/archive/3.3.7/eigen-3.3.7.zip
- Set environmental variables:

For Windows:
    - go to Conrtol Panel -> System -> Additional system settings -> Environmental variables

    - Set variable `OPENCV_ANDROID_SDK` to directory with unzipped OpenCV SDK
    
    - Set variable `EIGEN3_DIR` to directory with unzipped Eigen3

For Linux:

`

nano ~/.bash_profile

export OPENCV_ANDROID_SDK=/path/to/OpenCV-android-sdk

export EIGEN3_DIR=/path/to/eigen3

echo $OPENCV_ANDROID_SDK $EIGEN3_DIR

`

For MacOS:

`

nano /etc/launchd.conf

setenv OPENCV_ANDROID_SDK /path/to/OpenCV-android-sdk

setenv EIGEN3_DIR /path/to/eigen3

echo $OPENCV_ANDROID_SDK $EIGEN3_DIR

`

3) **Clone and open this porject in Android Studio**</br>
- `git clone https://github.com/Pavlik1400/FaceIt`
- Android Studio -> Open -> FaceIt/modules/main
- Now wait for project to build. There may occur some errors, like "Install Build-tools" or "Install Andrion SDK" if you haven`t done this yet. Just agree to install them and system will do the rest.
- If there is an error "NDK not configured", but you have already installed NDK, go to File -> Project structure -> SDK Location an set NDK location as default
- Run project on virtual device or your real phone

4) **Give permissions on Android and usage**</br>
- After installation firstly go to Settings -> All programs -> FaceIt -> Permissions/Other permissions -> Start in background. Give that permission
- Open app, it will authomatically redirect to Data access permission. Give it too
- Go to app, enter Installed Apps and tick all apps yuo want to lock
- Go to Add user and take a few photos of yourself. It will ask you permission to use camera. Give it. **Important!** Make photos from different edges. Dot`d be scared to make too much photos. Program will authomatically return to to main screen when where are enough photos!
- Press Start service. Now app is running!
- To stop app, press Stop service

## Our furure plans
- Implement better recognition algorithm
- Add seach by keyword in list of installed apps
- Make application totally "Unkillable"
- Fix all bugs

## Version history

### v0.1.0 - 0.6.0 - pre-alpha
- founded face recognition algorithm on Android
- Prepared base classes (locking, app listing)

### v0.7.1 - 0.7.3 - alpha
- modified and implemented user add and  face recognition
- implemented locker and app listing
- allded profiles
- posted project on github

### v1.0.1 - beta
- Added password protection
- Added automatic face recognition

### v1.0.2 - 1.0.3 - beta
- Rewrited TinyDB by UsersDataBase, using SQLite
- Changed DataBase by deleting all apps table
- Changed documentation

### v1.1.1 - beta
- fully updated UI
- small bugs fix
