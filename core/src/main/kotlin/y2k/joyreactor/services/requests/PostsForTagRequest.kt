package y2k.joyreactor.services.requests

import y2k.joyreactor.common.async.CompletableFuture
import y2k.joyreactor.common.async.runAsync
import y2k.joyreactor.common.http.HttpClient
import y2k.joyreactor.model.Group
import y2k.joyreactor.model.Post
import y2k.joyreactor.services.requests.parser.PostParser
import java.util.regex.Pattern

/**
 * Created by y2k on 9/26/15.
 */
class PostsForTagRequest(
    private val httpClient: HttpClient,
    private val urlBuilder: UrlBuilder,
    private val parser: PostParser) :
    Function2<String, String?, CompletableFuture<PostsForTagRequest.Data>> {

    @Deprecated("")
    fun requestAsync(group: Group, pageId: String? = null): CompletableFuture<Data> {
        return this(group.id, pageId)
    }

    override fun invoke(groupId: String, pageId: String?): CompletableFuture<Data> {
        return runAsync {
            val url = urlBuilder.build(groupId, pageId)
            val doc = httpClient.getDocument(url)

            val posts = doc
                .select("div.postContainer")
                .map { parser(it).first }

            val next = doc.select("a.next").first()
            Data(posts, next?.let { extractNumberFromEnd(next.attr("href")) })
        }
    }

    private fun extractNumberFromEnd(text: String): String {
        val m = Pattern.compile("\\d+$").matcher(text)
        if (!m.find()) throw IllegalStateException()
        return m.group()
    }

    class Data(val posts: List<Post>, val nextPage: String?)
}