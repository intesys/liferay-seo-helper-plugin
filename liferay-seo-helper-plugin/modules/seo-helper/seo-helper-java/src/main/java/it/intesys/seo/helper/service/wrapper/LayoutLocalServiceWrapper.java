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

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceWrapper;

import it.intesys.seo.helper.service.PageGeneratorService;

@Component(immediate = true, property = {}, service = ServiceWrapper.class)
public class LayoutLocalServiceWrapper
	extends com.liferay.portal.kernel.service.LayoutLocalServiceWrapper {

	public LayoutLocalServiceWrapper() {
		super(null);
	}

	@Override
	public void deleteLayout(
		long groupId, boolean privateLayout, long layoutId,
		com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		handleAssociatedArticles(
			LayoutLocalServiceUtil.fetchLayout(
				groupId, privateLayout, layoutId));

		super.deleteLayout(groupId, privateLayout, layoutId, serviceContext);
	}

	@Override
	public com.liferay.portal.kernel.model.Layout deleteLayout(
		com.liferay.portal.kernel.model.Layout layout) {

		handleAssociatedArticles(layout);

		return super.deleteLayout(layout);
	}

	@Override
	public void deleteLayout(
		com.liferay.portal.kernel.model.Layout layout, boolean updateLayoutSet,
		com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		_log.info(
			String.format(
				"Deleting layout with primary key %d; child layouts and associated resources will also be deleted",
				layout.getPrimaryKey()));

		handleAssociatedArticles(layout);

		super.deleteLayout(layout, updateLayoutSet, serviceContext);
	}

	@Override
	public com.liferay.portal.kernel.model.Layout deleteLayout(long plid)
		throws com.liferay.portal.kernel.exception.PortalException {

		handleAssociatedArticles(plid);

		return super.deleteLayout(plid);
	}

	private static final Log _log =
		LogFactoryUtil.getLog(LayoutLocalServiceWrapper.class);

	@Reference
	private PageGeneratorService _pageGeneratorService;

	private void handleAssociatedArticles(long plid) {

		Layout layout = null;

		try {
			if (plid > 0) {
				layout = this.getLayout(plid);
			}
		}
		catch (Exception e) {
			_log.info(String.format("Layout not found for plid %d", plid));
		}

		handleAssociatedArticles(layout);

	}

	private void handleAssociatedArticles(Layout layout) {

		if (layout != null) {
			List<JournalArticle> articles =
				_pageGeneratorService.getArticlesAndChildLayoutArticles(layout);
			if (articles != null && !articles.isEmpty()) {
				for (JournalArticle article : articles) {
					try {
						_log.info(
							String.format(
								"Removing association between (article, layout) with primary keys (%d, %d)",
								article.getPrimaryKey(),
								layout.getPrimaryKey()));
						_pageGeneratorService.removeLayoutAssociation(article);
					}
					catch (Exception e) {
						_log.info(
							String.format(
								"Unable to remove association between (article, layout) with primary keys (%d, %d)",
								article.getPrimaryKey(),
								layout.getPrimaryKey()));
					}
				}
			}
		}
	}
}
