# Use ADB wirelessly.
You might be using usb to connect your android device to laptop to test your app.<br> But there's an easier way, use wireless debug feature in android studio which allows you to connect android devices over wifi network. This is especially convenient if you are woking on multiple devices. 
Follow the steps below to set this up. You can use the terminal console in android studio to execute the commands below.<br><br>
1. Connect your device to laptop with usb and enter usb debugging mode,<br>
`adb usb`<br><br>
2. Now check if your device is listed,<br>
`adb devices`<br><br>
3.Switch to tcpip mode, here 5555 is the port.<br>
`adb tcpip 5555`<br><br>
4. Unplug your device and ensure that android device and laptop are on same WiFi network. Connect device wirelessly using,<br>
`adb connect <ip-address-of-device>`<br><br>
5. Check again if device is connected,<br>
`adb devices`<br><br>
And you are all set.

## Extras
1. If you get "adb is not recognized as an internal command", you will need to set the PATH variable to adb.exe. On my laptop its in *C:\Users\<user-name>\AppData\Local\Android\sdk\platform-tools*. Restart android studio and you should be able to use abd command.<br><br>
2. If you need to use more than one device at the same time, use  the below commands,<br>
i. Get serial number of the device, <br>
`adb devices`<br><br>
ii. Switch to tcpip mode,<br>
`adb -s <serial # of device> tcpip 5555`<br><br>
iii. Connect to device.<br>
`adb -s <serial # of device> connect <ip-address-of-device>`<br><br>

via [android developers](http://developer.android.com/tools/help/adb.html#wireless)
