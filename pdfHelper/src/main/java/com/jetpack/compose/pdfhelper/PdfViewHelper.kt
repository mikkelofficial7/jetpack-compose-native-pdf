package com.jetpack.compose.pdfhelper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.use

class PdfViewHelper(val context: Context) {
    private var pdfUriEncrypted: Uri? = null
    private var pdfFile: File? = null

    private var pdfFilename: String = ""

    var onShowPasswordDialog: (pdfByteArray: ByteArray) -> Unit = {}
    var onSuccess: (listBitmap: List<Bitmap>) -> Unit = {}
    var onError: (e: Exception) -> Unit = {}

    fun getPDFUri(): Uri? {
        return pdfUriEncrypted
    }

    fun clearCache() {
        try {
            pdfFile?.let { if (it.exists()) it.delete() }
            pdfFile = null
            pdfUriEncrypted = null
        } catch (e: Exception) {
            // println("Failed to clear pdf cache: ${e.message}")
        }
    }

    fun loadPdfFromBase64(
        base64String: String,
        filename: String,
        applicationId: String
    ) {
        if (base64String.isEmpty() || filename.isEmpty()) return
        pdfFilename = filename

        CoroutineScope(Dispatchers.IO).launch {
            val pdfByteArray = Base64.decode(base64String, Base64.DEFAULT)
            pdfFile = File(context.cacheDir, pdfFilename)
            pdfFile?.let {
                FileOutputStream(it).use { it.write(pdfByteArray) }
                pdfUriEncrypted = FileProvider.getUriForFile(
                    context,
                    applicationId,
                    it
                )

                if (isPdfEncrypted(pdfByteArray)) {
                    withContext(Dispatchers.Main) {
                        onShowPasswordDialog(pdfByteArray)
                    }
                } else {
                    val listBitmap = convertPdfFileToBitmaps(it)
                    withContext(Dispatchers.Main) {
                        onSuccess(listBitmap)
                    }
                }
            }
        }
    }

    fun decryptPdfFile(
        pdfBytes: ByteArray,
        password: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val doc: PDDocument = PDDocument.load(pdfBytes, password)
                if (doc.isEncrypted) {
                    doc.isAllSecurityToBeRemoved = true
                }

                val decryptedFile = File(context.cacheDir, pdfFilename)
                doc.save(decryptedFile)
                doc.close()
                // println("PDF Decrypt result: PDF decrypted successfully")

                val listBitmap = convertPdfFileToBitmaps(decryptedFile)
                onSuccess(listBitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                // println("PDF IOException result: Error => ${e.message}")
                onError(e)
            } catch (e: Exception) {
                e.printStackTrace()
                // println("PDF General result: Error => ${e.message}")
                onError(e)
            }
        }
    }

    private fun isPdfEncrypted(pdfBytes: ByteArray): Boolean {
        return try {
            val document: PDDocument = PDDocument.load(pdfBytes, "")
            val isFileEncrypted = document.isEncrypted
            document.close()
            isFileEncrypted
        } catch (e: Exception) {
            true
        }
    }

    private fun convertPdfFileToBitmaps(pdfFile: File): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        var fd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null

        try {
            fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(fd)

            for (i in 0 until renderer.pageCount) {
                renderer.openPage(i).use { page ->
                    val bitmap = createBitmap(page.width, page.height)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e)
        } finally {
            try {
                renderer?.close()
                fd?.close()
            } catch (e: Exception) {
                onError(e)
            }
        }
        return bitmaps
    }
}