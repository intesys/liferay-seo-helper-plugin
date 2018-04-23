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
package it.intesys.seo.helper.service;

import java.util.List;
import java.util.Locale;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import it.intesys.seo.helper.bean.SeoBean;
import it.intesys.seo.helper.bean.SeoRenderBean;

public interface PageGeneratorService {

	public void deleteLayoutIfExistsAndGeneratedByPageGenerator(
		JournalArticle article)
		throws Exception;

	public JournalArticle fetchJournalArticleByGroupIdArticleId(
		long groupId, String articleId)
		throws Exception;

	public Layout fetchLayout(String layoutUuid, long groupId)
		throws Exception;

	public Layout generatePage(SeoBean bean)
		throws Exception;

	public String getAssetEntryLayoutFriendlyUrl(
		long groupId, long articleResourcePk, String languageId)
		throws Exception;

	public List<JournalArticle> getArticlesAndChildLayoutArticles(
		Layout layout);

	public String getAssetEntryParentLayoutFriendlyUrl(
		long groupId, long articleResourcePk, String languageId)
		throws Exception;

	public SeoRenderBean getDefaultSeoRenderBean(
		ThemeDisplay attribute, JournalArticle article)
		throws Exception;

	public List<JournalArticle> getUpdateableArticles(
		long groupId, String layoutUuid)
		throws Exception;

	public AssetEntry getLayoutAssetEntry(long plid)
		throws Exception;

	public SeoRenderBean getSeoRenderBean(JournalArticle article, Locale locale)
		throws Exception;

	public long getSeoVocabularyId(JournalArticle article)
		throws Exception;

	public Object[] getTargetSeoArticleAndCategory(
		long classPK, String associationClassName, long associationClassPK)
		throws Exception;

	public boolean hasApprovedVersions(JournalArticle article)
		throws Exception;

	public boolean hasChildArticles(JournalArticle article);

	public boolean hasGeneratedPage(JournalArticle article)
		throws Exception;

	public boolean isLatestVersion(JournalArticle article)
		throws Exception;

	public boolean isPageGenerated(Layout layout)
		throws Exception;

	public void remember(Layout layout)
		throws Exception;

	public void removeLayoutAssociation(JournalArticle article)
		throws Exception;

	public long removeSeoVocabularyValuesIfPresent(JournalArticle article)
		throws Exception;

	public void syncLayoutIfApplicable(JournalArticle article)
		throws Exception;

	public JournalArticle removeSeoVocabularyValuesAndAssociatedLayoutUuid(
		JournalArticle article)
		throws Exception;

	public void updateFriendlyUrls(Layout layout, JournalArticle article)
		throws Exception;

	public void updateFriendlyUrlByCategory(
		JournalArticle journalArticle, AssetCategory assetCategory)
		throws Exception;

	public void updateFriendlyUrlForCategory(AssetCategory category)
		throws Exception;

	public void updateFriendlyUrlWithoutCategory(JournalArticle journalArticle)
		throws Exception;

	public void updateLayoutFriendlyUrls(
		JournalArticle article, Layout parent, Layout child)
		throws Exception;

}
