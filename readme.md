install system app

1. add permission

/frameworks/base/data/etc/oobe.xml
/frameworks/base/data/etc/privapp-permissions-platform.xml

 <privapp-permissions package="com.android.oobe">
      <permission name="android.permission.ACCESS_CHECKIN_PROPERTIES"/>
        <permission name="android.permission.ACCESS_NOTIFICATIONS"/>
        <permission name="android.permission.BACKUP"/>
        <permission name="android.permission.BATTERY_STATS"/>
        <permission name="android.permission.BLUETOOTH_PRIVILEGED"/>
        <permission name="android.permission.CHANGE_APP_IDLE_STATE"/>
        <permission name="android.permission.CHANGE_CONFIGURATION"/>
        <permission name="android.permission.DELETE_PACKAGES"/>
        <permission name="android.permission.FORCE_STOP_PACKAGES"/>
        <permission name="android.permission.LOCAL_MAC_ADDRESS"/>
        <permission name="android.permission.LOG_COMPAT_CHANGE" />
        <permission name="android.permission.MANAGE_DEBUGGING"/>
        <permission name="android.permission.MANAGE_DEVICE_ADMINS"/>
        <permission name="android.permission.MANAGE_FINGERPRINT"/>
        <permission name="android.permission.MANAGE_USB"/>
        <permission name="android.permission.MANAGE_USERS"/>
        <permission name="android.permission.MANAGE_USER_OEM_UNLOCK_STATE" />
        <permission name="android.permission.MASTER_CLEAR"/>
        <permission name="android.permission.MEDIA_CONTENT_CONTROL"/>
        <permission name="android.permission.MODIFY_PHONE_STATE"/>
        <permission name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
        <permission name="android.permission.MOVE_PACKAGE"/>
        <permission name="android.permission.OVERRIDE_COMPAT_CHANGE_CONFIG" />
        <permission name="android.permission.OVERRIDE_WIFI_CONFIG"/>
        <permission name="android.permission.PACKAGE_USAGE_STATS"/>
        <permission name="android.permission.READ_COMPAT_CHANGE_CONFIG" />
        <permission name="android.permission.READ_PRIVILEGED_PHONE_STATE"/>
        <permission name="android.permission.READ_SEARCH_INDEXABLES"/>
        <permission name="android.permission.REBOOT"/>
        <permission name="android.permission.STATUS_BAR"/>
        <permission name="android.permission.SUGGEST_MANUAL_TIME_AND_ZONE"/>
        <permission name="android.permission.TETHER_PRIVILEGED"/>
        <permission name="android.permission.USE_RESERVED_DISK"/>
        <permission name="android.permission.USER_ACTIVITY"/>
        <permission name="android.permission.WRITE_APN_SETTINGS"/>
        <permission name="android.permission.WRITE_MEDIA_STORAGE"/>
        <permission name="android.permission.MANAGE_EXTERNAL_STORAGE"/>
        <permission name="android.permission.WRITE_SECURE_SETTINGS"/>
        <permission name="android.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS" />
        <permission name="android.permission.INSTALL_DYNAMIC_SYSTEM"/>
        <permission name="android.permission.READ_DREAM_STATE"/>
        <permission name="android.permission.READ_DREAM_SUPPRESSION"/>
        <permission name="android.permission.CHANGE_COMPONENT_ENABLED_STATE"/>
        <permission name="android.permission.SET_TIME"/>
        <permission name="android.permission.SET_TIME_ZONE"/>
        <permission name="android.permission.INTERACT_ACROSS_USERS" />
    </privapp-permissions>


2. sign apk 
tools all from rom source code
java -Djava.library.path=. -jar signapk.jar platform.x509.pem platform.pk8 ./app-debug.apk ./app-debug-sign.apk

3. android:sharedUserId="android.uid.system"

4. fullscreen, immersive = toggleFreeformWindowingMode & hide natvigation/statusbar
/frameworks/base/core/java/com/android/internal/policy/DecorView.java
```
    setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
```

```
    public void toggleFreeformWindowingMode(){
        Window.WindowControllerCallback callback = mWindow.getWindowControllerCallback();
        final int windowingMode =
                getResources().getConfiguration().windowConfiguration.getWindowingMode();
        try {
            if (windowingMode == WINDOWING_MODE_FREEFORM && callback != null) {
                callback.toggleFreeformWindowingMode();
                updateDecorCaptionShade();
            } else if (windowingMode != WINDOWING_MODE_FREEFORM && callback != null) {
                callback.toggleFreeformWindowingMode();
                updateDecorCaptionShade();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "Catch exception when process F11", ex);
        }
    }
```