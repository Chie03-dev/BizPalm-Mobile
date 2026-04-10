# Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses

# iText rules
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Apache POI and Logging rules
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-keep class org.apache.xmlbeans.** { *; }
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.logging.log4j.**
-dontwarn aQute.bnd.annotation.spi.**
-dontwarn org.osgi.framework.**
-dontwarn org.apache.commons.csv.**
-dontwarn edu.umd.cs.findbugs.annotations.**

# XML and StAX rules
-dontwarn javax.xml.stream.**
-dontwarn org.codehaus.stax2.**
-dontwarn org.apache.jcp.xml.dsig.internal.dom.**
-dontwarn javax.xml.crypto.**
-dontwarn sharpen.config.**

# Smile ML rules
-keep class smile.** { *; }
-dontwarn smile.**
-dontwarn java.lang.foreign.**

# Guava rules
-dontwarn com.google.common.**
-keep class com.google.common.** { *; }

# MapLibre / Mapbox rules
-keep class org.maplibre.** { *; }
-dontwarn org.maplibre.**
-dontwarn com.mapbox.**
-keep class com.mapbox.** { *; }
-dontwarn com.google.gson.internal.**

# Java AWT (referenced by POI/Graph)
-dontwarn java.awt.**

# Room rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**
