# PdfBox-Android does reflection to instantiate SecurityHandlers.
-keep,allowobfuscation class * extends com.jetpack.compose.pdfDecryptor.pdfbox.pdmodel.encryption.SecurityHandler {
   public <init>(...);
}

 -keep,allowobfuscation class com.jetpack.compose.pdfDecryptor.pdfbox.pdmodel.documentinterchange.** { *; }