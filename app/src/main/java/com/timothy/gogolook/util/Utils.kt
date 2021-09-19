package com.timothy.gogolook.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.timothy.gogolook.R

@BindingAdapter("url")
fun setImage(image: ImageView, url: String?) {
    if (!url.isNullOrEmpty()){
        Glide.with(image.context).load(url)
            .transition(DrawableTransitionOptions.withCrossFade(200))
            .placeholder(R.color.background_color)
            .error(R.drawable.glide_error_image)
            .into(image)
    }
}