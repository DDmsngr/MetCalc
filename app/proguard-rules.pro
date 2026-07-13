# Kotlinx Serialization
-keepattributes InnerClasses
-dontnote kotlinx.serialization.SerializationKt

-keep,includedescriptorclasses class tech.deepdrift.metallist.**$$serializer { *; }
-keepclassmembers class tech.deepdrift.metallist.** {
    *** Companion;
}
-keepclasseswithmembers class tech.deepdrift.metallist.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
