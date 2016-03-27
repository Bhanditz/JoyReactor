package y2k.joyreactor.images

import rx.Completable
import rx.Single
import rx.Subscription
import y2k.joyreactor.common.*
import y2k.joyreactor.model.Image
import java.io.File
import java.util.*

/**
 * Created by y2k on 12/10/15.
 */
abstract class BaseImageRequest<T> {

    private lateinit var subscription: Subscription

    private val client = ServiceLocator.resolve<MultiTryDownloader>()

    private var image: Image? = null
    private var width: Int? = null
    private var height: Int? = null

    open fun setSize(width: Int, height: Int): BaseImageRequest<T> {
        this.width = width
        this.height = height
        return this
    }

    fun setUrl(image: Image?): BaseImageRequest<T> {
        this.image = image
        return this
    }

    fun to(target: Any, callback: (T?) -> Unit) {
        if (image == null) {
            sLinks.remove(target)
            callback(null)
            return
        }

        subscription = getFromCache()
            .replaceIfNull(putToCache().andThen(getFromCache()))
            .observeOn(ForegroundScheduler.instance)
            .subscribe { image, e ->
                e?.printStackTrace()
                if (sLinks[target] === subscription) {
                    image?.let(callback)
                    sLinks.remove(target)
                }
            }

        callback(null)
        sLinks.put(target, subscription)?.let {
            println("IMAGE REQUEST :: UNSUBSCRIBE")
            it.unsubscribe()
        }
    }

    private fun getFromCache(): Single<T?> {
        return sDiskCache.get(toURLString()).mapNotNull { decode(it) }
    }

    private fun putToCache(): Completable {
        return client
            .downloadAsync(sDiskCache.cacheDirectory, toURLString())
            .flatMap { sDiskCache.put(it, toURLString()) }
            .toCompletable()
    }

    private fun toURLString(): String {
        return image!!.thumbnailUrl(width, height)
    }

    protected abstract fun decode(path: File): T

    companion object {

        private val sDiskCache = DiskCache()
        private val sLinks = HashMap<Any, Subscription>()
    }
}