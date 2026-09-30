# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /path/to/proguard-android-optimize.txt

# Keep data classes and Room schemas
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
