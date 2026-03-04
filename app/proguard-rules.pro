# Максимальная оптимизация

-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-keep public class * extends android.inputmethodservice.InputMethodService
-keep public class * extends android.app.Activity
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepattributes *Annotation*
-dontwarn androidx.**
-dontwarn com.google.android.material.**