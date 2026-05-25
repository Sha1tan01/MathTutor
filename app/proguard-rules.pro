# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class ru.mathtutor.app.data.remote.dto.** { *; }
-keep class ru.mathtutor.app.data.assets.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Navigation Safe Args
-keep class ru.mathtutor.app.**FragmentArgs { *; }
-keep class ru.mathtutor.app.**FragmentDirections { *; }

# Markwon
-keep class io.noties.markwon.** { *; }

# Keep domain models
-keep class ru.mathtutor.app.domain.model.** { *; }
