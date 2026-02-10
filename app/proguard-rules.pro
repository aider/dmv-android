# Keep Gson classes
-keep class com.dmv.texas.Question { *; }
-keep class com.dmv.texas.Question$Image { *; }

# Gson uses generic type information stored in a class file when working with fields.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.dmv.texas.** { *; }
