package xyz.lilsus.papp.presentation.main.components

import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WithCameraPermission(
    content: @Composable() () -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    if (cameraPermission.status.isGranted) {
        content()
    } else {
        RequestCameraPermission(cameraPermission)
    }
}