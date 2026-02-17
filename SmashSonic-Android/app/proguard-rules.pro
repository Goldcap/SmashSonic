# Moshi
-keep class com.smashsonic.data.remote.dto.** { *; }
-keepclassmembers class com.smashsonic.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
