package com.fitness.app.ui.components

import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import com.squareup.picasso.Picasso

@Composable
fun PicassoImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Int? = null,
    error: Int? = null,
    contentScale: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP
) {
    Box(modifier = modifier.clipToBounds()) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = contentScale
                    adjustViewBounds = false
                    clipToOutline = true
                }
            },
            update = { imageView ->
                if (url.isBlank()) {
                    if (error != null) imageView.setImageResource(error)
                    else if (placeholder != null) imageView.setImageResource(placeholder)
                    else imageView.setImageDrawable(null)
                } else {
                    var request = Picasso.get().load(url)
                    placeholder?.let { request = request.placeholder(it) }
                    error?.let { request = request.error(it) }
                    request.into(imageView)
                }
            },
            modifier = Modifier.matchParentSize()
        )
    }
}
