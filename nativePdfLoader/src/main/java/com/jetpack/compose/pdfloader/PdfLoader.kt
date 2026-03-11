package com.jetpack.compose.pdfloader

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetpack.compose.pdfloader.helper.PdfViewHelper

@Composable
fun NativePdfCompose(pdfBase64String: String = "",
               filename: String = "",
               onError: (Exception) -> Unit = {}
) {
    var pdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    var listOfBitmap by remember { mutableStateOf<List<Bitmap>>(listOf()) }
    var showPasswordDialog by remember { mutableStateOf(true) }
    var isWrongPassword by remember { mutableStateOf(false) }

    val pdfFilename = filename.ifEmpty { "${System.currentTimeMillis()}.pdf" }
    val context = LocalContext.current
    val pdfViewHelper = remember { PdfViewHelper(context) }

    pdfViewHelper.onShowPasswordDialog = { pdfByteArray ->
        pdfBytes = pdfByteArray
    }
    pdfViewHelper.onSuccess = { listBitmap ->
        listOfBitmap = listBitmap
        isWrongPassword = false
        showPasswordDialog = false
    }
    pdfViewHelper.onError = { e ->
        onError(e)
        isWrongPassword = true
    }

    pdfViewHelper.loadPdfFromBase64(
        pdfBase64String,
        pdfFilename,
        LocalContext.current.packageName
    )

    Box(modifier = Modifier.fillMaxSize().background(color = Color.White)) {
        RenderAsImageBitmap(listOfBitmap)
        pdfBytes?.let { bytes ->
            if (showPasswordDialog) {
                ShowInputPasswordDialog(
                    isWrongPassword,
                    onDismiss = {
                        showPasswordDialog = false
                    },
                    onSubmit = { password ->
                        pdfViewHelper.decryptPdfFile(bytes, password)
                    },
                    onValueChange = {
                        isWrongPassword = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RenderAsImageBitmap(listBitmap: List<Bitmap>) {
    ZoomableLayout(listBitmap)
}

@Composable
private fun ShowInputPasswordDialog(
    isWrongPassword: Boolean,
    onValueChange: () -> Unit = {},
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = Color.White,
        onDismissRequest = {  },
        title = { Text("Enter PDF Password", fontSize = 18.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        onValueChange()
                    },
                    label = { Text("Input password here") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isWrongPassword) Color.Red else Color.Black,
                        focusedLabelColor = if (isWrongPassword) Color.Red else Color.Black,
                        focusedTextColor = Color.Black,
                        focusedLeadingIconColor = Color.Black,
                        focusedTrailingIconColor = Color.Black,
                        unfocusedBorderColor = if (isWrongPassword) Color.Red else Color.Gray,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )
                if (isWrongPassword) Text("Incorrect password", color = Color.Red,
                    modifier = Modifier.padding(0.dp, 10.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(password) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.Black
                ),
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
            ) {
                Text("Close")
            }
        }
    )
}