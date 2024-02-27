package com.timothy.gogolook.util

import android.content.res.Resources.getSystem
import android.os.Build
import android.view.WindowInsets
import android.widget.ImageView
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.timothy.gogolook.R
import java.util.LinkedList
import java.util.PriorityQueue

@BindingAdapter("url")
fun setImage(image: ImageView, url: String?) {
    if (!url.isNullOrEmpty()){
        Glide.with(image.context).load(url)
            .transition(DrawableTransitionOptions.withCrossFade(200))
//            .placeholder(R.color.background_color)
            .error(R.drawable.glide_error_image)
            .into(image)
    }
}

val Fragment.windowWidth: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = requireActivity().windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
            metrics.bounds.width() - insets.left - insets.right
        } else {
            val view = requireActivity().window.decorView
            val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets, view).getInsets(WindowInsetsCompat.Type.systemBars())
            resources.displayMetrics.widthPixels - insets.left - insets.right
        }
    }

abstract class LRUCache<T>{
    val capacity = HISTORY_MAX_SIZE
    abstract fun add(el:T)
    abstract fun toList():List<T>
    abstract fun isEmpty():Boolean
    abstract val size:Int
}

class LRUCacheImpl<T>(list: LinkedList<T> = LinkedList()): LRUCache<T>() {
    private val linkedList = list
    override val size: Int
        get() = linkedList.size

    override fun add(el: T) {
        linkedList.remove(el)
        linkedList.offer(el)

        while(linkedList.size > capacity)
            linkedList.poll()
    }
    override fun toList(): List<T> = linkedList.toList()
    override fun isEmpty(): Boolean = (size==0)
}