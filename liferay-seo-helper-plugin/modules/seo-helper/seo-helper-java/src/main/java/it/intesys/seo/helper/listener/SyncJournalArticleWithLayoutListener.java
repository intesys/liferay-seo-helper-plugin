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
package it.intesys.seo.helper.listener;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import it.intesys.seo.helper.service.PageGeneratorService;

/**
 * Article listener to maintain the relation article <-> SEO page updated
 */
@Component(immediate = true, service = ModelListener.class)
public class SyncJournalArticleWithLayoutListener
	extends BaseModelListener<JournalArticle> {

	/**
	 * When an article is removed from the bin, the page associated with it is
	 * automatically deleted
	 * 
	 * @param article
	 * @throws ModelListenerException
	 */
	@Override
	public void onAfterRemove(JournalArticle article)
		throws ModelListenerException {

		try {
			if (_pageGeneratorService.hasGeneratedPage(article)) {
				List<JournalArticle> articles =
					_pageGeneratorService.getUpdateableArticles(
						article.getGroupId(), article.getLayoutUuid());
				if (articles.size() == 0) {
					_pageGeneratorService.deleteLayoutIfExistsAndGeneratedByPageGenerator(
						article);
					_log.info(
						String.format(
							"Deleted layout %s associated with article id %s, article resource primary key %s",
							article.getLayoutUuid(), article.getArticleId(),
							article.getResourcePrimKey()));
					long catId =
						_pageGeneratorService.removeSeoVocabularyValuesIfPresent(
							article);
					if (catId > 0) {
						_log.info(
							String.format(
								"Deleted SEO TABLE article-category association between article (articleId, resourcePrimKey) := (%s, %s) and category id %d",
								article.getArticleId(),
								article.getResourcePrimKey(), catId));
					}
				}
			}
		}
		catch (Exception e) {
			_log.error(
				"Can not delete layout " + article.getLayoutUuid() +
					" associated with article " + article.getArticleId(),
				e);
		}

		super.onAfterRemove(article);
	}

	/**
	 * When an article is updated, if connected to a page, the description of
	 * the page associated with the article is updated
	 * 
	 * @param article
	 * @throws ModelListenerException
	 */
	@Override
	public void onAfterUpdate(JournalArticle article)
		throws ModelListenerException {

		synchronized (article) {
			if (!_running) {
				try {
					_running = true;
					_pageGeneratorService.syncLayoutIfApplicable(article);
					_running = false;
				}
				catch (Exception e) {
					_running = false;
					_log.error(
						String.format(
							"Could not perform sync of article with pkey %d and layoutUuid %s. Exception is: ",
							article.getPrimaryKey(), article.getLayoutUuid()),
						e);
				}
				super.onAfterUpdate(article);
			}
		}
	}

	@Reference
	private PageGeneratorService _pageGeneratorService;

	private volatile boolean _running = false;

	private static final Log _log =
		LogFactoryUtil.getLog(SyncJournalArticleWithLayoutListener.class);
}
