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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.kernel.service.persistence.AssetEntryQuery;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FriendlyURLNormalizerUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;

@Component(immediate = true, service = AssetHelper.class)
public class AssetHelper {

	protected AssetEntry getAssetEntry(long associationClassPK)
		throws Exception {

		return _assetEntryLocalService.fetchEntry(
			GetterUtil.getLong(associationClassPK));
	}

	protected AssetCategory getCategory(long classPK)
		throws Exception {

		return _assetCategoryLocalService.fetchAssetCategory(classPK);
	}

	protected long[] getCategoryIds(AssetCategory category)
		throws Exception {

		List<Long> categoryIds = new ArrayList<Long>();
		categoryIds.add(category.getCategoryId());
		addCategoryIdsRecursively(
			categoryIds, _assetCategoryLocalService.getChildCategories(
				category.getCategoryId()));
		long[] anyCategoryIds = new long[categoryIds.size()];
		for (int i = 0; i < categoryIds.size(); i++) {
			anyCategoryIds[i] = categoryIds.get(i);
		}
		return anyCategoryIds;
	}

	protected List<AssetEntry> getEntries(AssetEntryQuery query)
		throws Exception {

		return _assetEntryLocalService.getEntries(query);
	}

	protected Map<Locale, String> getFriendlyUrlMapByCategory(
		AssetCategory category)
		throws Exception {

		Map<Locale, String> friendlyURLMap = new HashMap<Locale, String>();
		for (Map.Entry<Locale, String> catTitle : category.getTitleMap().entrySet()) {
			if (Validator.isNotNull(catTitle.getValue())) {
				friendlyURLMap.put(
					catTitle.getKey(),
					StringPool.SLASH + FriendlyURLNormalizerUtil.normalize(
						catTitle.getValue()));
			}
		}
		AssetCategory parent = _assetCategoryLocalService.fetchAssetCategory(
			category.getParentCategoryId());
		if (parent == null) {
			return friendlyURLMap;
		}
		else {
			addCategoryRecursivelyToFriendlyUrl(friendlyURLMap, parent);
		}
		return friendlyURLMap;
	}

	protected Map<Locale, String> getFriendlyUrlMapByVocabulary(
		JournalArticle article, long vocabularyId)
		throws Exception {

		Map<Locale, String> friendlyURLMap = new HashMap<Locale, String>();

		Set<Locale> availableLocales = LanguageUtil.getAvailableLocales();

		for (Locale locale : availableLocales) {
			String localizedTitle = article.getTitle(locale, false);
			if (Validator.isNotNull(localizedTitle)) {
				friendlyURLMap.put(
					locale,
					"/" + FriendlyURLNormalizerUtil.normalize(localizedTitle));
			}
		}

		List<AssetCategory> categories =
			_assetCategoryLocalService.getCategories(
				JournalArticle.class.getName(), article.getResourcePrimKey());
		for (AssetCategory category : categories) {
			if (vocabularyId == category.getVocabularyId()) {
				addCategoryRecursivelyToFriendlyUrl(
					article, friendlyURLMap, category.getCategoryId());
				break;
			}
		}
		return friendlyURLMap;
	}

	protected AssetEntry getJournalAssetEntry(long resourcePrimKey)
		throws Exception {

		return _assetEntryLocalService.getEntry(
			JournalArticle.class.getName(), resourcePrimKey);
	}

	protected AssetEntry getLayoutAssetEntry(List<JournalArticle> articles)
		throws Exception {

		AssetEntry layoutAssetEntry = null;
		if (!articles.isEmpty()) {
			layoutAssetEntry = _assetEntryLocalService.getEntry(
				JournalArticle.class.getName(),
				articles.get(0).getResourcePrimKey());
		}
		return layoutAssetEntry;
	}

	private void addCategoryIdsRecursively(
		List<Long> categoryIds, List<AssetCategory> categories)
		throws SystemException {

		if (categories == null || categories.isEmpty()) {
			return;
		}
		for (AssetCategory category : categories) {
			categoryIds.add(category.getCategoryId());
			addCategoryIdsRecursively(
				categoryIds, _assetCategoryLocalService.getChildCategories(
					category.getCategoryId()));
		}

	}

	private void addCategoryRecursivelyToFriendlyUrl(
		Map<Locale, String> friendlyURLMap, AssetCategory category)
		throws PortalException, SystemException {

		for (Map.Entry<Locale, String> entry : friendlyURLMap.entrySet()) {
			if (Validator.isNotNull(category.getTitle(entry.getKey()))) {
				friendlyURLMap.put(
					entry.getKey(),
					StringPool.SLASH +
						FriendlyURLNormalizerUtil.normalize(
							category.getTitle(entry.getKey())) +
						friendlyURLMap.get(entry.getKey()));
			}
			else {
				_log.warn(
					"Incomplete SEO category transalations for category: " +
						category);
				friendlyURLMap.remove(entry.getKey());
			}
		}
		AssetCategory parent = _assetCategoryLocalService.fetchAssetCategory(
			category.getParentCategoryId());
		if (parent == null) {
			return;
		}
		else {
			addCategoryRecursivelyToFriendlyUrl(friendlyURLMap, parent);
		}
		return;
	}

	private void addCategoryRecursivelyToFriendlyUrl(
		JournalArticle article, Map<Locale, String> friendlyURLMap,
		long categoryId)
		throws PortalException, SystemException {

		if (categoryId <= 0) {
			return;
		}
		AssetCategory category =
			_assetCategoryLocalService.getCategory(categoryId);
		for (Map.Entry<Locale, String> entry : friendlyURLMap.entrySet()) {
			String title = category.getTitle(entry.getKey());
			if (Validator.isNotNull(title)) {
				friendlyURLMap.put(
					entry.getKey(), "/" + FriendlyURLNormalizerUtil.normalize(
						title + entry.getValue()));
			}
			else {
				_log.warn(
					"Incomplete SEO category transalations for category: " +
						category);
				friendlyURLMap.remove(entry.getKey());
			}
		}
		addCategoryRecursivelyToFriendlyUrl(
			article, friendlyURLMap, category.getParentCategoryId());
	}

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	private static final Log _log =
		LogFactoryUtil.getLog(AssetHelper.class);
}
