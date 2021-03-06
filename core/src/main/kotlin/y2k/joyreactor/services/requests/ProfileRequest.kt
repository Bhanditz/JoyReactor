package y2k.joyreactor.services.requests

import org.jsoup.nodes.Document
import rx.Observable
import rx.Single
import y2k.joyreactor.common.NotAuthorizedException
import y2k.joyreactor.common.getDocumentAsync
import y2k.joyreactor.common.http.HttpClient
import y2k.joyreactor.common.ioObservable
import y2k.joyreactor.model.Image
import y2k.joyreactor.model.Profile
import java.util.regex.Pattern

/**
 * Created by y2k on 19/10/15.
 */
class ProfileRequest(
    private val httpClient: HttpClient) : Function1<String, Single<Profile>> {

    override operator fun invoke(username: String): Single<Profile> {
        return getUrl(username)
            .let { httpClient.getDocumentAsync(it) }
            .map { ProfileParser(it).parse() }
    }

    @Deprecated("Use operator invoke + UserNameRequest")
    fun request(): Observable<Profile> {
        return UserNameRequest(httpClient)
            .request()
            .flatMap { username ->
                ioObservable {
                    if (username == null) throw NotAuthorizedException()

                    val page = httpClient.getDocument(getUrl(username))
                    ProfileParser(page).parse()
                }
            }
    }

    private fun getUrl(username: String?): String {
        if (username == null) throw RuntimeException()
        return "http://joyreactor.cc/user/" + username
    }

    private class ProfileParser(
        private val document: Document) {

        fun parse(): Profile {
            return Profile(
                document.select("div.sidebarContent > div.user > span").text(),
                Image(document.select("div.sidebarContent > div.user > img").attr("src"), 0, 0),
                document.select("#rating-text > b").text().replace(" ", "").toFloat(),
                document.select(".star-row-0 > .star-0").size,
                getProgressToNewStar(),
                getSubRatings(),
                getAwards())
        }

        private fun getProgressToNewStar(): Float {
            val style = document.select("div.stars div.poll_res_bg_active").first().attr("style")
            val m = Pattern.compile("width:(\\d+)%;").matcher(style)
            if (!m.find()) throw IllegalStateException()
            return java.lang.Float.parseFloat(m.group(1))
        }

        private fun getSubRatings(): List<Profile.SubRating> {
            return document
                .select("div.blogs tr")
                .filter { !it.select("small").isEmpty() }
                .map {
                    Profile.SubRating(
                        "\\d[\\d\\. ]*".toRegex().find(it.select("small").text())!!.value.replace(" ", "").toFloat(),
                        it.select("img").attr("alt"))
                }
        }

        private fun getAwards(): List<Profile.Award> {
            return document
                .select("div.award_holder > img")
                .map { Profile.Award(it.absUrl("src"), it.attr("alt")) }
        }
    }
}