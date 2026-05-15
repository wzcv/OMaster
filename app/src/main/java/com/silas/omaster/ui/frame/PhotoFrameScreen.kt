package com.silas.omaster.ui.frame

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silas.omaster.R
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.AppDesign
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import com.silas.omaster.util.ImageExporter
import com.silas.omaster.util.OutputRatio

@Composable
fun PhotoFrameScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FrameViewModel = viewModel(factory = FrameViewModelFactory(context.applicationContext as Application))
    val state by viewModel.state.collectAsState()

    var dateTimeText by remember { mutableStateOf("") }
    var exifSynced by remember { mutableStateOf(false) }
    var useRounded by remember { mutableStateOf(true) }
    var selectedRatio by remember { mutableStateOf(OutputRatio.FULL) }

    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            exifSynced = false
            viewModel.loadImage(it)
        }
    }

    fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    LaunchedEffect(state.dateTime) {
        if (!exifSynced && state.dateTime != null) {
            dateTimeText = state.dateTime ?: ""
            exifSynced = true
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themedBackground())
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.photoframe_card_title),
            onBack = onBack,
            actions = {
                if (state.renderedBitmap != null) {
                    TextButton(
                        onClick = {
                            val bitmap = state.renderedBitmap ?: return@TextButton
                            val success = ImageExporter.saveToGallery(context, bitmap)
                            val msg = context.getString(
                                if (success) R.string.photoframe_saved else R.string.photoframe_save_failed
                            )
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                        Text(stringResource(R.string.save))
                    }
                }
            }
        )

        if (state.sourceBitmap == null) {
            EmptyPickerContent(onPickImage = { launchImagePicker() })
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(AppDesign.ScreenPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    PreviewArea(state = state)

                    Spacer(Modifier.height(AppDesign.SectionSpacing))

                    EditPanel(
                        dateTimeText = dateTimeText,
                        useRounded = useRounded,
                        selectedRatio = selectedRatio,
                        onDateTimeChange = {
                            dateTimeText = it
                            viewModel.updateTitle(it)
                        },
                        onRoundedChanged = {
                            useRounded = it
                            viewModel.toggleRoundedCorners(it)
                        },
                        onRatioChanged = {
                            selectedRatio = it
                            viewModel.selectRatio(it)
                        },
                        onPickNewImage = {
                            exifSynced = false
                            launchImagePicker()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPickerContent(onPickImage: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(AppDesign.ScreenPadding),
            colors = CardDefaults.cardColors(containerColor = themedCardBackground()),
            shape = RoundedCornerShape(20.dp),
            onClick = onPickImage
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.PhotoLibrary, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.photoframe_pick_photo), style = MaterialTheme.typography.titleLarge, color = themedTextPrimary(), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.photoframe_pick_desc), style = MaterialTheme.typography.bodyMedium, color = themedTextSecondary(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PreviewArea(state: FrameState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDesign.MediumRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (state.renderedBitmap != null) {
            val img = remember(state.renderedBitmap) { state.renderedBitmap.asImageBitmap() }
            Image(bitmap = img, contentDescription = stringResource(R.string.photoframe_preview), contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth())
        } else if (state.sourceBitmap != null) {
            val img = remember(state.sourceBitmap) { state.sourceBitmap.asImageBitmap() }
            Image(bitmap = img, contentDescription = stringResource(R.string.photoframe_original), contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun EditPanel(
    dateTimeText: String,
    useRounded: Boolean,
    selectedRatio: OutputRatio,
    onDateTimeChange: (String) -> Unit,
    onRoundedChanged: (Boolean) -> Unit,
    onRatioChanged: (OutputRatio) -> Unit,
    onPickNewImage: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = themedTextPrimary(),
        unfocusedTextColor = themedTextPrimary()
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themedCardBackground()),
        shape = RoundedCornerShape(AppDesign.LargeRadius)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.photoframe_aspect_ratio), style = MaterialTheme.typography.bodyMedium, color = themedTextPrimary())

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StyleButton(
                    selected = selectedRatio == OutputRatio.SQUARE,
                    onClick = { onRatioChanged(OutputRatio.SQUARE) },
                    modifier = Modifier.weight(1f),
                    content = {
                        RatioIcon(widthToHeight = 1f)
                        Spacer(Modifier.height(4.dp))
                        Text(OutputRatio.SQUARE.label, style = MaterialTheme.typography.labelSmall)
                    }
                )
                StyleButton(
                    selected = selectedRatio == OutputRatio.PORTRAIT_4_5,
                    onClick = { onRatioChanged(OutputRatio.PORTRAIT_4_5) },
                    modifier = Modifier.weight(1f),
                    content = {
                        RatioIcon(widthToHeight = 4f / 5f)
                        Spacer(Modifier.height(4.dp))
                        Text(OutputRatio.PORTRAIT_4_5.label, style = MaterialTheme.typography.labelSmall)
                    }
                )
                StyleButton(
                    selected = selectedRatio == OutputRatio.PORTRAIT_3_4,
                    onClick = { onRatioChanged(OutputRatio.PORTRAIT_3_4) },
                    modifier = Modifier.weight(1f),
                    content = {
                        RatioIcon(widthToHeight = 3f / 4f)
                        Spacer(Modifier.height(4.dp))
                        Text(OutputRatio.PORTRAIT_3_4.label, style = MaterialTheme.typography.labelSmall)
                    }
                )
                StyleButton(
                    selected = selectedRatio == OutputRatio.FULL,
                    onClick = { onRatioChanged(OutputRatio.FULL) },
                    modifier = Modifier.weight(1f),
                    content = {
                        RatioIcon(widthToHeight = 9f / 16f)
                        Spacer(Modifier.height(4.dp))
                        Text(OutputRatio.FULL.label, style = MaterialTheme.typography.labelSmall)
                    }
                )
                StyleButton(
                    selected = selectedRatio == OutputRatio.LANDSCAPE_16_9,
                    onClick = { onRatioChanged(OutputRatio.LANDSCAPE_16_9) },
                    modifier = Modifier.weight(1f),
                    content = {
                        RatioIcon(widthToHeight = 16f / 9f)
                        Spacer(Modifier.height(4.dp))
                        Text(OutputRatio.LANDSCAPE_16_9.label, style = MaterialTheme.typography.labelSmall)
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = dateTimeText,
                onValueChange = onDateTimeChange,
                label = { Text(stringResource(R.string.photoframe_shoot_time)) },
                placeholder = { Text(stringResource(R.string.photoframe_time_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Schedule, null, tint = themedTextSecondary(), modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.photoframe_image_style), style = MaterialTheme.typography.bodyMedium, color = themedTextPrimary())

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StyleButton(
                    selected = useRounded,
                    onClick = { onRoundedChanged(true) },
                    modifier = Modifier.weight(1f),
                    content = {
                        RoundedCornerIcon()
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.photoframe_rounded), style = MaterialTheme.typography.labelSmall)
                    }
                )
                StyleButton(
                    selected = !useRounded,
                    onClick = { onRoundedChanged(false) },
                    modifier = Modifier.weight(1f),
                    content = {
                        SharpCornerIcon()
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.photoframe_sharp), style = MaterialTheme.typography.labelSmall)
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onPickNewImage,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, null, Modifier.size(18.dp))
                Text(stringResource(R.string.photoframe_reselect))
            }
        }
    }
}

@Composable
private fun StyleButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else themedTextSecondary()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else themedCardBackground()
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CompositionLocalProvider(
                LocalContentColor provides tint
            ) {
                content()
            }
        }
    }
}

@Composable
private fun RatioIcon(widthToHeight: Float) {
    val iconColor = LocalContentColor.current
    Canvas(modifier = Modifier.size(width = 28.dp, height = 20.dp)) {
        val containerWidth = size.width
        val containerHeight = size.height
        val maxDrawWidth = containerWidth * 0.85f
        val maxDrawHeight = containerHeight * 0.85f

        val drawWidth: Float
        val drawHeight: Float
        if (widthToHeight >= 1f) {
            drawWidth = maxDrawWidth
            drawHeight = maxDrawWidth / widthToHeight
        } else {
            drawHeight = maxDrawHeight
            drawWidth = maxDrawHeight * widthToHeight
        }

        val left = (containerWidth - drawWidth) / 2f
        val top = (containerHeight - drawHeight) / 2f

        drawRoundRect(
            color = iconColor,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(drawWidth, drawHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
            style = Stroke(width = 2.5f)
        )
    }
}

@Composable
private fun RoundedCornerIcon() {
    val iconColor = LocalContentColor.current
    Canvas(modifier = Modifier.size(width = 28.dp, height = 20.dp)) {
        val w = size.width * 0.85f
        val h = size.height * 0.85f
        val left = (size.width - w) / 2f
        val top = (size.height - h) / 2f
        drawRoundRect(
            color = iconColor,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
            style = Stroke(width = 2.5f)
        )
    }
}

@Composable
private fun SharpCornerIcon() {
    val iconColor = LocalContentColor.current
    Canvas(modifier = Modifier.size(width = 28.dp, height = 20.dp)) {
        val w = size.width * 0.85f
        val h = size.height * 0.85f
        val left = (size.width - w) / 2f
        val top = (size.height - h) / 2f
        drawRect(
            color = iconColor,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(w, h),
            style = Stroke(width = 2.5f)
        )
    }
}
