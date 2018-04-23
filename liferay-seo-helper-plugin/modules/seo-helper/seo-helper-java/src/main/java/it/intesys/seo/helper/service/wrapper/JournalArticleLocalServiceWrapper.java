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
package it.intesys.seo.helper.service.wrapper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.TrashPermissionException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceWrapper;

import it.intesys.seo.helper.service.PageGeneratorService;

/**
 * The extension of the JournalArticleService allows for the management of a
 * copy of an article with an associated SEO page. In the case of a copy, in the
 * new article, the layoutUuid associated with the original article is removed.
 */
@Component(immediate = true, property = {}, service = ServiceWrapper.class)
public class JournalArticleLocalServiceWrapper
	extends com.liferay.journal.service.JournalArticleLocalServiceWrapper {

	public JournalArticleLocalServiceWrapper() {
		super(null);
	}

	public JournalArticleLocalServiceWrapper(
		JournalArticleLocalService journalArticleService) {
		super(journalArticleService);
	}

	@Override
	public JournalArticle copyArticle(
		long userId, long groupId, String oldArticleId, String newArticleId,
		boolean autoArticleId, double version)
		throws PortalException {

		JournalArticle article = super.copyArticle(
			userId, groupId, oldArticleId, newArticleId, autoArticleId,
			version);

		_log.info(
			String.format(
				"Old article id is %s, new article id is %s", oldArticleId,
				article.getArticleId()));

		try {
			if (_pageGeneratorService.hasGeneratedPage(article)) {
				_log.info(
					"The original article with article id was page-generated. Removing seo references and layout uuid from the new (copied) article");
				// Remove page generator expando references from the copy
				_pageGeneratorService.removeSeoVocabularyValuesAndAssociatedLayoutUuid(
					article);
			}
		}
		catch (Exception e) {
			_log.error(
				String.format(
					"Copied article with primary key %d may not have had all page generator references updated",
					article.getPrimaryKey()));
		}

		return article;

	}

	@Override
	public JournalArticle deleteArticle(
		JournalArticle article, String articleURL,
		ServiceContext serviceContext)
		throws PortalException {

		checkPermission(article);

		return super.deleteArticle(article, articleURL, serviceContext);
	}

	private void checkPermission(JournalArticle article)
		throws PortalException {

		_log.info(
			String.format(
				"Checking if article can be deleted: %s", article.toString()));

		if (_pageGeneratorService.hasChildArticles(article)) {
			_log.info(
				"The article has child articles, throwing custom trash permission exception");
			throw new TrashPermissionException(7);
		}
	}

	@Reference
	private PageGeneratorService _pageGeneratorService;

	private static final Log _log =
		LogFactoryUtil.getLog(JournalArticleLocalServiceWrapper.class);

}
