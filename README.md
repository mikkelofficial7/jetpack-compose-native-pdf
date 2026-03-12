# Jetpack Compose Passworded Native PDF from Base64 📁

Turn your Base64 Password/No Password PDF into Native Pdf Viewer (For Jetpack Compose).
  

Replaced 

`compileOnly 'com.gemalto.jp2:jp2-android:1.0.3'`

to

`implementation("org.opencv:opencv:4.9.0")`

for **JPEG-2000 (JP2) Encoder/Decoder**
  

This library was inspired by [Pdf Box Decryptor](https://github.com/TomRoush/PdfBox-Android)

<div align="left">
  <img src="https://github.com/mikkelofficial7/jetpack-compose-native-pdf/blob/main/screenshot.png" alt="Jetpack Compose Passworded Native PDF" width="500" height="600">
</div>

Full Demo video [here](https://videy.co/v/?id=84GCUo4u1)

Latest stable version: 

[![](https://jitpack.io/v/mikkelofficial7/jetpack-compose-native-pdf.svg)](https://github.com/mikkelofficial7/jetpack-compose-native-pdf/releases/tag/1.0.1)

How to use (Sample demo provided):

1. Add this gradle in ```build.gradle(:app)``` :
```
dependencies {
   implementation 'com.github.mikkelofficial7:jetpack-compose-native-pdf:1.0.1'
}
 ```
or gradle.kts:
```
dependencies {
   implementation("com.github.mikkelofficial7:jetpack-compose-native-pdf:1.0.1")
}
 ```

2. Add it in your root settings.gradle at the end of repositories:
```
repositories {
  mavenCentral()
  maven { url 'https://jitpack.io' }
}
```

3. Just call ```NativePdfCompose()``` in your parent activity and set base64 string and pdf name (optional)
```
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NativePdfTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    SampleNativePdf()
                }
            }
        }
    }
}

@Composable
fun SampleNativePdf() {
   NativePdfCompose("YOUR_BASE64_PDF", "YOUR_PDF_NAME")
}
```
