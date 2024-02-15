-keep class it.contrammobilita.android.** { *; }
-keep class com.google.android.gms.maps.model.Marker { *; }

#For Crashlytics
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

-dontwarn com.squareup.okhttp.Cache
-dontwarn com.squareup.okhttp.CacheControl$Builder
-dontwarn com.squareup.okhttp.CacheControl
-dontwarn com.squareup.okhttp.Call
-dontwarn com.squareup.okhttp.OkHttpClient
-dontwarn com.squareup.okhttp.Request$Builder
-dontwarn com.squareup.okhttp.Request
-dontwarn com.squareup.okhttp.Response
-dontwarn com.squareup.okhttp.ResponseBody
-dontwarn org.bouncycastle.jce.provider.BouncyCastleProvider
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE


 # Keep generic signature of RxJava3 (R8 full mode strips signatures from non-kept items).
 -keep,allowobfuscation,allowshrinking class io.reactivex.Flowable
 -keep,allowobfuscation,allowshrinking class io.reactivex.Maybe
 -keep,allowobfuscation,allowshrinking class io.reactivex.Observable
 -keep,allowobfuscation,allowshrinking class io.reactivex.Single

  # With R8 full mode generic signatures are stripped for classes that are not
  # kept. Suspend functions are wrapped in continuations where the type argument
  # is used.
  -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

  # R8 full mode strips generic signatures from return types if not kept.
  -if interface * { @retrofit2.http.* public *** *(...); }
  -keep,allowoptimization,allowshrinking,allowobfuscation class <3>

  # With R8 full mode generic signatures are stripped for classes that are not kept.
  -keep,allowobfuscation,allowshrinking class retrofit2.Response