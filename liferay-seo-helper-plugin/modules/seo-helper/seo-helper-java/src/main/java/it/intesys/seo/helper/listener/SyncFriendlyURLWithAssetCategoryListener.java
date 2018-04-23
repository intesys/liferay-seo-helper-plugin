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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.util.GetterUtil;

import it.intesys.seo.helper.service.PageGeneratorService;

/**
 * Category listener te keep friendlyURLs updated that were generated from
 * categories
 */
@Component(immediate = true, service = ModelListener.class)
public class SyncFriendlyURLWithAssetCategoryListener
	extends BaseModelListener<AssetCategory> {

	/**
	 * Case in which the article is categorised after generating the page
	 */
	@Override
	public void onAfterAddAssociation(
		Object classPK, String associationClassName, Object associationClassPK)
		throws ModelListenerException {

		try {
			if (AssetEntry.class.getName().equals(associationClassName)) {
				Object[] articleAndCategory =
					_pageGeneratorService.getTargetSeoArticleAndCategory(
						GetterUtil.getLong(classPK), associationClassName,
						GetterUtil.getLong(associationClassPK));
				if (articleAndCategory[0] == null ||
					articleAndCategory[1] == null) {
					return;
				}
				_pageGeneratorService.updateFriendlyUrlByCategory(
					(JournalArticle) articleAndCategory[0],
					(AssetCategory) articleAndCategory[1]);
			}
		}
		catch (Exception e) {
			String error = "Can not handle category association. classPK=" +
				classPK + " associationClassName=" + associationClassName +
				" associationClassPK=" + associationClassPK;
			_log.error(error);
			throw new ModelListenerException(error, e);
		}

		super.onAfterAddAssociation(
			classPK, associationClassName, associationClassPK);
	}

	/**
	 * Case in which article categorisation is removed after generating the
	 * page, or in the case that the category with which the pages have been
	 * generated is eliminated
	 */
	@Override
	public void onAfterRemoveAssociation(
		Object classPK, String associationClassName, Object associationClassPK)
		throws ModelListenerException {

		try {
			if (associationClassName.equals(AssetEntry.class.getName())) {
				Object[] articleAndCategory =
					_pageGeneratorService.getTargetSeoArticleAndCategory(
						GetterUtil.getLong(classPK), associationClassName,
						GetterUtil.getLong(associationClassPK));
				if (articleAndCategory[0] == null) {
					return;
				}
				else if (articleAndCategory[1] != null) {
					_pageGeneratorService.updateFriendlyUrlWithoutCategory(
						(JournalArticle) articleAndCategory[0]);
				}
			}
		}
		catch (Exception e) {
			String error = "Can not handle category association. classPK=" +
				classPK + " associationClassName=" + associationClassName +
				" associationClassPK=" + associationClassPK;
			_log.error(error, e);
			throw new ModelListenerException(error, e);
		}

		super.onAfterRemoveAssociation(
			classPK, associationClassName, associationClassPK);
	}

	@Override
	public void onAfterUpdate(AssetCategory category)
		throws ModelListenerException {

		try {
			_pageGeneratorService.updateFriendlyUrlForCategory(category);
		}
		catch (Exception e) {
			_log.error(
				"Cannot sync SEO Url that used the following category " +
					category,
				e);
			throw new ModelListenerException(e);
		}
	}

	@Reference
	private PageGeneratorService _pageGeneratorService;

	private static final Log _log =
		LogFactoryUtil.getLog(SyncFriendlyURLWithAssetCategoryListener.class);

}
