-keep class com.keepr.data.model.** { *; }
-keep class com.keepr.data.database.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
