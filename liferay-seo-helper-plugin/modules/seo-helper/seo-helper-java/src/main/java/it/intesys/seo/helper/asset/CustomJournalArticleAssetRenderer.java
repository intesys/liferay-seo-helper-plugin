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
package it.intesys.seo.helper.asset;

import java.util.List;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.web.asset.JournalArticleAssetRenderer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import it.intesys.seo.helper.service.PageGeneratorService;

public class CustomJournalArticleAssetRenderer
	extends JournalArticleAssetRenderer {

	public CustomJournalArticleAssetRenderer(JournalArticle article) {
		super(article);

	}

	@Override
	public String getURLViewInContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		String noSuchEntryRedirect)
		throws Exception {

		JournalArticle article = getArticle();
		// if the article has an associated layout
		if (_pageGeneratorService.hasGeneratedPage(article)) {
			List<JournalArticle> jaList =
				_pageGeneratorService.getUpdateableArticles(
					article.getGroupId(), article.getLayoutUuid());
			// and it's the only article associated with the page
			if (jaList.size() == 1 &&
				article.getArticleId().equals(jaList.get(0).getArticleId())) {
				_log.info(
					"JorunalArticle with dedicated page { " +
						article.getArticleId() + "}");
				Layout layout = _pageGeneratorService.fetchLayout(
					article.getLayoutUuid(), article.getGroupId());
				ThemeDisplay themeDisplay =
					(ThemeDisplay) liferayPortletRequest.getAttribute(
						WebKeys.THEME_DISPLAY);

				return PortalUtil.getLayoutURL(layout, themeDisplay);
			}
		}
		return super.getURLViewInContext(
			liferayPortletRequest, liferayPortletResponse, noSuchEntryRedirect);
	}

	public void setPageGeneratorService(
		PageGeneratorService pageGeneratorService) {

		_pageGeneratorService = pageGeneratorService;
	}

	private PageGeneratorService _pageGeneratorService;

	private static final Log _log =
		LogFactoryUtil.getLog(CustomJournalArticleAssetRenderer.class);
}
