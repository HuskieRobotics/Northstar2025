# Northstar

Northstar is 6328's AprilTag tracking and object detection system. This code is provided for reference, but **we do NOT provide support to other teams using it**. Please use [PhotonVision](https://photonvision.org) or [Limelight](https://limelightvision.io) instead.

Object detection models can be found [here](https://drive.google.com/drive/folders/1l3Bx3FGBGiY3hcpaPtvrNNPMZHChCi9w?usp=sharing), and are available under an AGPL-3.0 license located in the same folder.


Development Notes
===

* download cmake 3.31.8 source and build in terminal based on README
* build ns-iokit-ctl (from ./ns-iokit-ctl)
    * mkdir build
    * cd build
    * cmake ..
* run 
* build reenumerate (from ./reenumerate)
    * make
* test with openpnp-capture-test
    *  ./openpnp-capture-test
        * find ID for desired camera
        * find format ID for desired resolution and FPS
    *  ./openpnp-capture-test 0 8
        * 0: is ID
        * 8: is format (1600 x 1200 @ 50 FPS)
* test reenumerate
    * ./reenumerate -v 0x0C45,0x6366
        * where 0x0C45 is vendorID and 0x6366 is productID
        * output displays the location ID; for example:
            * Found "Arducam OV2311 USB Camera" @ 0x00110000
    * try to reenumerate camera using location ID:
        * ./reenumerate -v -l 0x00110000
        * this is done in the northstar_launch.scpt before running the main python file
* test ns-iokit-ctl
    * this is used to configure camera properties
    * for example:
        * ./ns_iokit_ctl 0x0C45 0x6366 0x00110000 0 157 2
* install Python 3.12.10
    * latest version supported by Core ML Tool 8.1
* need to add Northstar vision code to 3061-lib
    * create a new robot type for northstar vision test platform
* still trying to figure out how to calibrate the camera; I may need to do the above first, regardless
    * it appears that you set "calibration" to true in NT under the northstar key
    * you capture frames by setting "capture_flag" to true in NT under the same key
    * calibration server runs on port 7999
* struggled to get the VideoCapture to work
    * For DefaultCapture, a string is passed to VideoCapture; however, I believe it should be an integer
        * I'm not sure how this is working for 6328. Maybe they don't use DefaultCapture.,
        * Upon further investigation, they are using Basler cameras; so, maybe they are using PylonCapture
    * I converted the string passed through NT to an integer and it works
    * I also had an issue where reading from NT didn't return values immediately after subscribing.
    * I added a check for an empty camera device ID and to exit the get_frame method
* switch to using AVFoundationCapture
    * have to specify device id as follows: "0x110000:0c45:6366"
        * no leading 0s for the camera location
        * no "0x" for the vendor or product ID
        * letters must be lowercase
    * set the gain to 0
    * frame rate increased to 50 fps from 16 fps
* installed ffmpeg using homebrew for frame capture
* shouldn't have committed the models to GitHub as they are large; will remove
    * they are available here: https://drive.google.com/drive/folders/1l3Bx3FGBGiY3hcpaPtvrNNPMZHChCi9w
* run the AppleScript file from the terminal:
    * osascript northstar_launch.scpt

Mac mini Configuration
===
* purchased 2024 Mac mini (MU9D3LL/A)
* System Settings -> Energy -> Start up automatically after a power failure
    * this will auto power on the Mac mini when the robot is turned on
* Update to macOS Sequoia 15.7.1
* Download and install Pylon 25.09 from Basler for cameras
* Download and install GitHub Desktop
* Download and install VS Code
* Download and install xCode; accept license agreement
* install Python 3.12.10
    * latest version supported by Core ML Tool 8.1
* create virtual environment in this folder
    * python3 -m venv venv
    * source ./venv/bin/activate
    * pip install -r requirements.txt
* Download and install homebrew
* install ffmpeg using homebrew for frame capture
    * brew install ffmpeg
* download cmake 3.31.8 source and build in terminal based on README
* build reenumerate (from ./reenumerate)
    * make
* test reenumerate
    * ./reenumerate -v 0x0C45,0x6366
        * where 0x0C45 is vendorID and 0x6366 is productID
        * output displays the location ID; for example:
            * Found "Arducam OV2311 USB Camera" @ 0x00110000
    * try to reenumerate camera using location ID:
        * ./reenumerate -v -l 0x00110000
        * this is done in the northstar_launch.scpt before running the main python file
* pylon Viewer
    * https://docs.baslerweb.com/overview-of-the-pylon-viewer
    * camera -> automatic image adjustment
        * sets configuration automatically
        * this should be done as part of field calibration
        * the gain and expsure settings are in the Features - All -> Analog Control and Acquisition Control
        * https://docs.baslerweb.com/automatic-image-adjustment.html
    * toolbar -> open sharpness indicator
        * focus the camera and set the stop nut to preserve focus
        * https://docs.baslerweb.com/sharpness-indicator

Cameras
===
* Basler daA1920-160um
    * config0.json
    * ID: 40686739
    * vendor ID: 0x2676
    * product ID: 0xba06
    * location: 0x00200000
* Basler daA1920-160um
    * config1.json
    * ID: 40708569
    * vendor ID: 0x2676
    * product ID: 0xba06
    * location: 0x02210000
* Basler daA1920-160um
    * config2.json
    * ID: 40708556
    * vendor ID: 0x2676
    * product ID: 0xba06
    * location: 0x02210000
* Basler daA1920-160um
    * config3.json
    * ID: ???
    * vendor ID: 0x2676
    * product ID: 0xba06
    * location: 0x02210000
* Basler da1280-54uc
    * config4.json
    * ID: 25249734 (serial number from Pylon app)
    * vendor ID: 0x2676
    * product ID: 0xba03
    * location: 0x03200000


Future Work
===
* √ explore camera configuration once lenses arrive
* √ calibrate cameras
* check performance of using pylon-cropped (need to update 3061-lib for new resolution as well)
* I'm using the CoreML models provided by 6328.
* We should learn how to create our own CoreML models.
    * [This example](https://apple.github.io/coremltools/docs-guides/source/introductory-quickstart.html) may be helpful.
* get rid of poseestimation and taganglecalculator pipelines

    

