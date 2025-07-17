package com.supernova.ui.preload

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import coil.Coil
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.PriorityBlockingQueue

class ImagePreloader(
    private val context: Context,
    private val imageLoader: ImageLoader = Coil.imageLoader(context),
    private val skipCheck: () -> Boolean = { false }
) {
    enum class Priority { VISIBLE, NEXT, BACKGROUND }

    private data class Request(val url: String, val priority: Priority) : Comparable<Request> {
        override fun compareTo(other: Request): Int = this.priority.compareTo(other.priority)
    }

    private val queue = PriorityBlockingQueue<Request>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun preloadVisible(urls: List<String>) = enqueue(urls, Priority.VISIBLE)
    fun preloadNext(urls: List<String>) = enqueue(urls, Priority.NEXT)
    fun preloadBackground(urls: List<String>) = enqueue(urls, Priority.BACKGROUND)

    private fun enqueue(urls: List<String>, priority: Priority) {
        if (shouldSkipPreloading()) return
        urls.forEach { queue.offer(Request(it, priority)) }
        processQueue()
    }

    private fun processQueue() {
        scope.launch {
            while (queue.isNotEmpty() && !shouldSkipPreloading()) {
                val req = queue.poll() ?: break
                val request = ImageRequest.Builder(context)
                    .data(req.url)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .allowHardware(false)
                    .build()
                imageLoader.enqueue(request)
            }
        }
    }

    private fun shouldSkipPreloading(): Boolean {
        if (skipCheck()) return false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return true
        val caps = cm.getNetworkCapabilities(network) ?: return true
        val slow = !caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val lowMemory = memInfo.lowMemory
        return slow || lowMemory
    }

    fun clear() {
        queue.clear()
        imageLoader.memoryCache?.clear()
    }
}
