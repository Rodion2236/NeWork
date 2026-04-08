package ru.netology.nework.util

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ru.netology.nework.R

fun ImageView.load(
    url: String?,
    @DrawableRes placeholder: Int = R.drawable.ic_account_circle_24,
    @DrawableRes error: Int = R.drawable.ic_broken_image_24,
    roundedCorners: Int? = null,
    centerCrop: Boolean = true
) {
    if (url.isNullOrBlank()) {
        Glide.with(this).clear(this)
        setImageResource(placeholder)
        return
    }

    val options = RequestOptions()
        .placeholder(placeholder)
        .error(error)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .apply {
            if (centerCrop) transform(CenterCrop())
            if (roundedCorners != null) transform(RoundedCorners(roundedCorners))
        }

    Glide.with(this)
        .load(url)
        .apply(options)
        .into(this)
}

fun ImageView.clearImage() {
    Glide.with(this).clear(this)
}