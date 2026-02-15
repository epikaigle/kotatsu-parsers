package org.koitharu.kotatsu.parsers.site.vi

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.config.ConfigKey
import org.koitharu.kotatsu.parsers.core.SinglePageMangaParser
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import org.koitharu.kotatsu.parsers.model.MangaListFilterCapabilities
import org.koitharu.kotatsu.parsers.model.MangaListFilterOptions
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.model.SortOrder
import org.koitharu.kotatsu.parsers.util.attrAsRelativeUrl
import org.koitharu.kotatsu.parsers.util.generateUid
import org.koitharu.kotatsu.parsers.util.parseHtml
import org.koitharu.kotatsu.parsers.util.selectFirstOrThrow
import org.koitharu.kotatsu.parsers.util.splitByWhitespace
import org.koitharu.kotatsu.parsers.util.urlBuilder
import org.koitharu.kotatsu.parsers.util.urlEncoded
import java.util.EnumSet

@MangaSourceParser("BFANGTEAM", "BFang Team", "vi")
internal class BFangTeam (context: MangaLoaderContext) :
	SinglePageMangaParser(context, MangaParserSource.BFANGTEAM) {

	override val configKeyDomain = ConfigKey.Domain("moetruyen.net")

	override val availableSortOrders: Set<SortOrder> = EnumSet.of(SortOrder.UPDATED)

	override val filterCapabilities: MangaListFilterCapabilities
		get() = MangaListFilterCapabilities(
			isSearchSupported = true,
			isSearchWithFiltersSupported = true,
			isMultipleTagsSupported = true,
			isTagsExclusionSupported = true,
			isAuthorSearchSupported = true,
		)

	override suspend fun getFilterOptions(): MangaListFilterOptions {
		return MangaListFilterOptions(
			availableTags = availableTags(),
		)
	}

	override suspend fun getList(order: SortOrder, filter: MangaListFilter): List<Manga> {
		val url = urlBuilder().addPathSegment("manga")

		if (!filter.query.isNullOrEmpty()) {
			url.addQueryParameter("q",
				filter.query.splitByWhitespace().joinToString(separator = "+") {
					it.urlEncoded()
				}
			)
		}

		if (filter.tags.isNotEmpty()) {
			filter.tags.forEach {
				url.addQueryParameter("include", it.key)
			}
		}

		if (filter.tagsExclude.isNotEmpty()) {
			filter.tagsExclude.forEach {
				url.addQueryParameter("exclude", it.key)
			}
		}

		if (!filter.author.isNullOrEmpty()) {
			url.addQueryParameter("q",
				filter.author.splitByWhitespace().joinToString(separator = "+") {
					it.urlEncoded()
				}
			)
		}

		val request = webClient.httpGet(url.build()).parseHtml()
		return request.select("article.manga-card reveal").map { ar ->
			val href = ar.selectFirstOrThrow("a").attrAsRelativeUrl("href")
			val cover = ar.select(".cover img").attr("src")
			Manga(
				id = generateUid(href),
				title =
			)
		}
	}
}
