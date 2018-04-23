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
package it.intesys.seo.helper.journal.util.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalArticleConstants;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.journal.util.impl.JournalUtil;
import com.liferay.layouts.admin.kernel.util.SitemapURLProvider;
import com.liferay.layouts.admin.kernel.util.SitemapUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Element;

import it.intesys.seo.helper.service.PageGeneratorService;

@Component(immediate = true, service = SitemapURLProvider.class)
public class JournalArticleSitemapURLProvider implements SitemapURLProvider {

	@Override
	public String getClassName() {

		String realClassName = JournalArticleSitemapURLProvider.class.getName();
		String journalArticleClassName = JournalArticle.class.getName();
		_log.info(
			"getClassName override in " + realClassName + " returning " +
				journalArticleClassName);
		return journalArticleClassName;
	}

	@Override
	public void visitLayout(
		Element element, String layoutUuid, LayoutSet layoutSet,
		ThemeDisplay themeDisplay)
		throws PortalException {

		if (visit(layoutUuid, layoutSet)) {
			List<JournalArticle> journalArticles =
				_journalArticleService.getArticlesByLayoutUuid(
					layoutSet.getGroupId(), layoutUuid);

			visitArticles(element, layoutSet, themeDisplay, journalArticles);
		}
	}

	@Override
	public void visitLayoutSet(
		Element element, LayoutSet layoutSet, ThemeDisplay themeDisplay)
		throws PortalException {

		List<JournalArticle> journalArticles =
			_journalArticleService.getLayoutArticles(layoutSet.getGroupId());

		if (journalArticles.isEmpty()) {
			return;
		}

		Set<String> processedArticleIds = new HashSet<>();

		String portalURL = PortalUtil.getPortalURL(layoutSet, themeDisplay);

		for (JournalArticle journalArticle : journalArticles) {
			Layout layout = _layoutLocalService.getLayoutByUuidAndGroupId(
				journalArticle.getLayoutUuid(), layoutSet.getGroupId(),
				layoutSet.getPrivateLayout());

			if (!visit(layout)) {
				continue;
			}

			if (processedArticleIds.contains(journalArticle.getArticleId()) ||
				(journalArticle.getStatus() != WorkflowConstants.STATUS_APPROVED) ||
				!JournalUtil.isHead(journalArticle)) {

				continue;
			}

			String groupFriendlyURL = PortalUtil.getGroupFriendlyURL(
				_layoutSetLocalService.getLayoutSet(
					journalArticle.getGroupId(), false),
				themeDisplay);

			StringBundler sb = new StringBundler(4);

			if (!groupFriendlyURL.startsWith(portalURL)) {
				sb.append(portalURL);
			}

			sb.append(groupFriendlyURL);
			sb.append(JournalArticleConstants.CANONICAL_URL_SEPARATOR);
			sb.append(journalArticle.getUrlTitle());

			String articleURL =
				PortalUtil.getCanonicalURL(sb.toString(), themeDisplay, layout);

			Map<Locale, String> alternateURLs =
				SitemapUtil.getAlternateURLs(articleURL, themeDisplay, layout);

			SitemapUtil.addURLElement(
				element, articleURL, null, journalArticle.getModifiedDate(),
				articleURL, alternateURLs);

			if (alternateURLs.size() > 1) {
				Locale defaultLocale = LocaleUtil.getSiteDefault();

				for (Map.Entry<Locale, String> entry : alternateURLs.entrySet()) {

					Locale availableLocale = entry.getKey();
					String alternateURL = entry.getValue();

					if (!availableLocale.equals(defaultLocale)) {
						SitemapUtil.addURLElement(
							element, alternateURL, null,
							journalArticle.getModifiedDate(), articleURL,
							alternateURLs);
					}
				}
			}

			processedArticleIds.add(journalArticle.getArticleId());
		}
	}

	protected void visitArticles(
		Element element, LayoutSet layoutSet, ThemeDisplay themeDisplay,
		List<JournalArticle> journalArticles)
		throws PortalException {

		if (journalArticles.isEmpty()) {
			return;
		}

		Set<String> processedArticleIds = new HashSet<>();

		String portalURL = _portal.getPortalURL(layoutSet, themeDisplay);

		for (JournalArticle journalArticle : journalArticles) {
			if (processedArticleIds.contains(journalArticle.getArticleId()) ||
				(journalArticle.getStatus() != WorkflowConstants.STATUS_APPROVED) ||
				!JournalUtil.isHead(journalArticle)) {

				continue;
			}

			String groupFriendlyURL = _portal.getGroupFriendlyURL(
				_layoutSetLocalService.getLayoutSet(
					journalArticle.getGroupId(), false),
				themeDisplay);

			StringBundler sb = new StringBundler(4);

			if (!groupFriendlyURL.startsWith(portalURL)) {
				sb.append(portalURL);
			}

			sb.append(groupFriendlyURL);
			sb.append(JournalArticleConstants.CANONICAL_URL_SEPARATOR);
			sb.append(journalArticle.getUrlTitle());

			Layout layout = _layoutLocalService.getLayoutByUuidAndGroupId(
				journalArticle.getLayoutUuid(), layoutSet.getGroupId(),
				layoutSet.getPrivateLayout());

			String articleURL =
				_portal.getCanonicalURL(sb.toString(), themeDisplay, layout);

			Map<Locale, String> alternateURLs =
				SitemapUtil.getAlternateURLs(articleURL, themeDisplay, layout);

			for (String alternateURL : alternateURLs.values()) {
				SitemapUtil.addURLElement(
					element, alternateURL, null,
					journalArticle.getModifiedDate(), articleURL,
					alternateURLs);
			}

			processedArticleIds.add(journalArticle.getArticleId());
		}
	}

	private boolean visit(Layout layout) {

		boolean visit = true;

		try {
			if (_pageGeneratorService.isPageGenerated(layout)) {
				visit = false;
			}
		}
		catch (Exception e) {
			_log.info(
				"Could not determine if layout with pkey " +
					layout.getPrimaryKey() +
					" needs to be visited, assuming yes");
		}

		return visit;
	}

	private boolean visit(String layoutUuid, LayoutSet layoutSet) {

		boolean visit = true;

		try {
			boolean privateLayout = false;
			Layout layout = _layoutLocalService.getLayoutByUuidAndGroupId(
				layoutUuid, layoutSet.getGroupId(), privateLayout);

			if (_pageGeneratorService.isPageGenerated(layout)) {
				_log.info(
					"Page with layoutUuid " + layoutUuid +
						" is generated by pagegenerator module -- not visiting layout");
				visit = false;
			}
		}
		catch (Exception e) {
			_log.info(
				"Could not determine if layout with uuid " + layoutUuid +
					" needs to be visited, assuming yes");
		}

		return visit;
	}

	@Reference
	private JournalArticleService _journalArticleService;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutSetLocalService _layoutSetLocalService;

	@Reference
	private PageGeneratorService _pageGeneratorService;

	@Reference
	private Portal _portal;

	private static final Log _log =
		LogFactoryUtil.getLog(JournalArticleSitemapURLProvider.class);

}
