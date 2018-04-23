/**
 * Copyright (c) 1995-present INTESYS S.R.L., Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package it.intesys.seo.helper.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.util.StringPool;

@Component(immediate = true, service = JournalArticleHelper.class)
public class JournalArticleHelper {

	protected JournalArticle fetchJournalArticleByGroupIdArticleId(
		long groupId, String articleId)
		throws Exception {

		JournalArticle article =
			_journalArticleLocalService.fetchArticle(groupId, articleId);
		return article;
	}

	protected JournalArticle fetchLatestArticleByClassPK(long classPK)
		throws Exception {

		return _journalArticleLocalService.fetchLatestArticle(classPK);
	}

	protected JournalArticle fetchLatestArticleByResourcePrimKey(
		long resourcePrimKey)
		throws Exception {

		return _journalArticleLocalService.getLatestArticle(resourcePrimKey);
	}

	protected List<JournalArticle> getArticlesByResourcePrimKey(
		long resourcePrimKey)
		throws Exception {

		return _journalArticleLocalService.getArticlesByResourcePrimKey(
			resourcePrimKey);
	}

	protected List<JournalArticle> getUpdateableArticles(
		long groupId, String layoutUuid)
		throws Exception {

		Map<String, JournalArticle> mapArticles =
			new HashMap<String, JournalArticle>();
		for (JournalArticle article : _journalArticleService.getArticlesByLayoutUuid(
			groupId, layoutUuid)) {
			JournalArticle processedArticle =
				mapArticles.get(article.getArticleId());
			if (processedArticle == null) {
				mapArticles.put(article.getArticleId(), article);
			}
			else if (article.getVersion() >= processedArticle.getVersion()) {
				mapArticles.put(article.getArticleId(), article);
			}
		}

		return new ArrayList<JournalArticle>(mapArticles.values());
	}

	protected List<JournalArticle> getAllArticlesByLayout(Layout layout) {

		return _journalArticleService.getArticlesByLayoutUuid(
			layout.getGroupId(), layout.getUuid());
	}

	protected JournalArticle removeAssociatedLayoutUuid(JournalArticle article)
		throws Exception {

		article.setLayoutUuid(StringPool.BLANK);
		return this.updateArticle(article);
	}

	protected JournalArticle updateArticle(JournalArticle article)
		throws Exception {

		return _journalArticleLocalService.updateJournalArticle(article);
	}

	protected JournalArticle fetchArticleByPrimaryKey(long articlePrimaryKey)
		throws Exception {

		return _journalArticleLocalService.getArticle(articlePrimaryKey);
	}
	
	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private JournalArticleService _journalArticleService;

}
