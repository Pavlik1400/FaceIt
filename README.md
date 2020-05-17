# FaceIt - protect your applications without any discomfort
This is the repository of our course work. We're students at [Ukrainian Catholic University](https://ucu.edu.ua/), 
[Faculty of Applied Science](https://apps.ucu.edu.ua/en/), Computer Science. Contributors: 
- [Pavlo Hilei](https://github.com/Pavlik1400) (Павло Гілей)
- [Volodymyr Tsapiv](https://github.com/Tsapiv) (Володимир Цапів)
- [Yevhen Pankevych](https://github.com/yewhenp) (Євген Панкевич)

## Motivation
Every day we use many applications on our phone. Staring from just surfing the Internet by browser up to showing photos in the gallery. Each time we use different applications, which have direct access to our private data - photos, videos, documents, or even passwords and personal info. But what if someone wants to get this info illegally? N that case, our application can help you to protect your data and applications from someone else's access, and make the process of authorization easy.

## Project features
This application allows each Android user to block a number of installed apps on his/her phone and after trying to enter them it allows to use of face recognition instead of an ordinary password. Password authorization is available too. Of course, before it the user should tick some apps to block and take selfies to train an algorithm to recognize him/her. Application work fact and recognize the user in a few seconds after entering locked apps. Also, there are some additional functions, like the possibility to create many profiles for different tasks (work, free time, etc.), and a well-looking UI for a better experience of application usage. 

## Repository content
- modules - work directory of main and additional modules
- modules/main - main work repository
- modules/old - folder for tried app realizations on Python
- examples - folder with examples of using of different python libraries
- docs - contains various documents that explain the project more. Also, you can visit the wiki for more info

## Technical details and structure
Firstly, the whole project was written in Python 3.8, using Dlib and OpenCV for face recognition and Kivy for android implementation. Even now you can find these modules in modules/old directory.
However, as it turned out, currently it is almost impossible to implement Dlib on Android using Kiwi. So, we decided to write this project using the native Android language, Java.

**So, current project:**
- Project is written on Java 12.0.2
- Used Android Studio 3.6
- Android SDK version 29
- Android SDK Build-tools version 29.0.3
- Gradle version 3.6.3
- Android NDK version 21.0.6113669

### Algorithm and technical implementation
We based our project on the [FaceRecognitionApp](https://github.com/Lauszus/FaceRecognitionApp) by Lauszus. We used an Eigenface recognition algorithm, implemented in this project. [About Eigenface](https://habr.com/ru/post/68870/)
Dependencies for this project:
- OpenCV Android SDK version 4.3.0
- Eigen3 version 3.3.7

We partly changed and implemented this project as a recognition basis of our app.  See classes:
- `AddUserActivity.java` - Activity with UI for adding user biometrical info (face photos) 
- `CameraBridgeViewBase.java` - interaction with Camera and OpenCV library
- `JavaCameraView.java` - implementation of the Bridge View between OpenCV and Java Camera
- `NativeMethods.java` - implementation of recognition algoritm (uses [FaceRecognitionLib](https://github.com/Lauszus/FaceRecognitionLib), written on C++)
- `UsersDataBase.java` - database for image vectors
- `UserLockRecognitionActivity.java` - Activity with UI for recognizing the user when some app is locked

Another part of our project is the app locker and installed app finder.
- `BlockServise.java` - service for blocking chosen apps which the user tries to open
- `DataBase.java` - database for locked apps
- `Notification.java` - class for creating a notification
- `AppAdapter.java` - adapter for recyclerView that shows all installed apps
- `BootStart.java` - starts an application on device boot
- `ListOfAppsActivity.java` - activity with all installed apps
- `PasswordActivity.java` - an activity for setting and resetting the password
- `ProfileItemAdapter.java` - adapter for recyclerView in profiles menu
- `ProfilesActivity.java` - an activity for work with profiles

Finally, the main UI class:
- `FaceRecognitionActivity.java` - main UI Activity, which works as a menu and which starts `AddUserActivity`, `DetailsActivity`, and `BlockServise`. Here user can navigate through all features of our app

## Project status
The project is on the way to the pre-release version. Just finishing some technical bugs. Version: 1.1.1-beta

Now we are in process of hosting our app on getjar.com

## How to use?
1) **Install Android Studio, SDK and NDK**<br/>
For all platforms (Windows, Linux, macOS):

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

```
nano ~/.bash_profile

export OPENCV_ANDROID_SDK=/path/to/OpenCV-android-sdk

export EIGEN3_DIR=/path/to/eigen3

echo $OPENCV_ANDROID_SDK $EIGEN3_DIR
```

For MacOS:

```
nano /etc/launchd.conf

setenv OPENCV_ANDROID_SDK /path/to/OpenCV-android-sdk

setenv EIGEN3_DIR /path/to/eigen3

echo $OPENCV_ANDROID_SDK $EIGEN3_DIR
```

3) **Clone and open this project in Android Studio**</br>
- `git clone https://github.com/Pavlik1400/FaceIt`
- Android Studio -> Open -> FaceIt/modules/main
- Now, wait for the project to build. There may occur some errors, like "Install Build-tools" or "Install Andrion SDK" if you haven`t done this yet. Just agree to install them and the system will do the rest.
- If there is an error "NDK not configured", but you have already installed NDK, go to File -> Project structure -> SDK Location and set NDK location as default
- Run project on the virtual device or your real phone

4) **Give permissions on Android and usage**</br>
- After installation firstly go to Settings -> All programs -> FaceIt -> Permissions/Other permissions -> Start in background. Give that permission
- Open app, it will automatically redirect to Data access permission. Give it too
- Go to the app, enter Installed Apps and tick all apps you want to lock
- Go to Add user and take a few photos of yourself. It will ask you permission to use the camera. Give it. **Important!** Make photos from different edges. Don't be scared to take too many photos. The program will automatically return to the main screen when there are enough photos!
- Press Start service. Now the app is running!
- To stop the app, press Stop service

## Our future plans
- Implement better recognition algorithm
- Add search by keyword in the list of installed apps
- Make application totally "Unkillable"
- Fix all bugs

## Version history

### v0.1.0 - 0.6.0 - pre-alpha
- founded a face recognition algorithm on Android
- Prepared base classes (locking, app listing)

### v0.7.1 - 0.7.3 - alpha
- modified and implemented user add and face recognition
- implemented locker and app listing
- added profiles
- posted project on Github

### v1.0.1 - beta
- Added password protection
- Added automatic face recognition

### v1.0.2 - 1.0.3 - beta
- Rewrite TinyDB by UsersDataBase, using SQLite
- Changed DataBase by deleting all apps table
- Changed documentation

### v1.1.1 - beta
- fully updated UI
- small bugs fix
