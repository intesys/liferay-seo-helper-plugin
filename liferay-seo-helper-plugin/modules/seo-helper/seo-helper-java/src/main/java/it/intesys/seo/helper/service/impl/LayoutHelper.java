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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutPrototype;
import com.liferay.portal.kernel.service.LayoutFriendlyURLLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutPrototypeLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.FriendlyURLNormalizerUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;

import it.intesys.seo.helper.bean.SeoBean;
import it.intesys.seo.helper.bean.SeoRenderBean;
import it.intesys.seo.helper.common.constants.PGConstants;

@Component(immediate = true, service = LayoutHelper.class)
public class LayoutHelper {

	protected void deleteByPlid(long plid)
		throws Exception {

		_layoutLocalService.deleteLayout(
			plid, ServiceContextThreadLocal.getServiceContext());
	}

	protected Layout fetchByPlid(long plid)
		throws Exception {

		return _layoutLocalService.fetchLayout(plid);
	}

	protected Layout fetchByUuidGroupId(String uuid, long groupId)
		throws Exception {

		return _layoutLocalService.getLayoutByUuidAndGroupId(
			uuid, groupId, PGConstants.PRIVATE_LAYOUT);
	}

	protected Layout getChildLayout(
		Layout parentLayout, Map<Locale, String> friendlyURLMap,
		JournalArticle article, SeoBean bean, long userId)
		throws Exception {

		long parentLayoutId;

		ServiceContext serviceContext = new ServiceContext();
		if (Validator.isNotNull(parentLayout)) {
			parentLayoutId = parentLayout.getLayoutId();
		}
		else {
			parentLayoutId = getParentLayoutId(article.getGroupId(), userId);
		}

		if (bean.getLayoutPrototypeId() > 0) {
			serviceContext.setAttribute(
				_LAYOUT_PROTOTYPE_LINK_ENABLED,
				bean.isEnablePrototypePageLink());

			LayoutPrototype layoutPrototype =
				_layoutPrototypeLocalService.getLayoutPrototype(
					bean.getLayoutPrototypeId());
			serviceContext.setAttribute(
				_LAYOUT_PROTOTYPE_UUID, layoutPrototype.getUuid());
		}

		Layout layout = _layoutLocalService.addLayout(
			userId, article.getGroupId(), PGConstants.PRIVATE_LAYOUT,
			parentLayoutId, article.getTitleMap(), bean.getSeoTitle(),
			article.getDescriptionMap(), bean.getSeoKeywords(),
			bean.getSeoRobots(), LayoutConstants.TYPE_PORTLET,
			bean.getTypeSettingsProperties().toString(), _CHILD_LAYOUT_HIDDEN,
			friendlyURLMap, serviceContext);

		return layout;
	}

	protected String getFriendlyUrl(String uuid, long groupId, Locale locale)
		throws Exception {

		Layout layout = fetchByUuidGroupId(uuid, groupId);
		String friendlyUrl = layout.getFriendlyURL(locale);

		return friendlyUrl;
	}

	protected SeoRenderBean getSeoRenderBean(
		JournalArticle article, Locale locale)
		throws Exception {

		SeoRenderBean bean = new SeoRenderBean();

		Layout layout =
			fetchByUuidGroupId(article.getLayoutUuid(), article.getGroupId());
		Map<Locale, String> writableFrinedlyUrl =
			getFriendlyURLWriteablePart(article, layout);
		// see com.liferay.portal.model.impl.LayoutImpl.getFriendlyURLsXML()
		bean.setHtmlFriendlyUrlMap(
			LocalizationUtil.updateLocalization(
				writableFrinedlyUrl, StringPool.BLANK, "FriendlyURL",
				LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault())));

		Map<Locale, String> friendlyURLReadonlyPart =
			getFriendlyURLReadonlyPart(article, layout);
		bean.setFriendlyUrlBase(
			friendlyURLReadonlyPart.getOrDefault(
				(locale), friendlyURLReadonlyPart.get(
					LocaleUtil.fromLanguageId(layout.getDefaultLanguageId()))));
		bean.setHtmlTitleMap(layout.getTitle());
		bean.setHtmlKeywordsMap(layout.getKeywords());
		bean.setHtmlRobotsMap(layout.getRobots());
		bean.setSitemapInclude(
			layout.getTypeSettingsProperty(_PROP_KEY_SITEMAP_INCLUDE, "1"));
		bean.setSitemapPriority(
			layout.getTypeSettingsProperty(_PROP_KEY_SITEMAP_PRIORITY, ""));
		bean.setSitemapChangefreq(
			layout.getTypeSettingsProperty(
				_PROP_KEY_SITEMAP_CHANGEFREQ, "always"));

		return bean;
	}

	protected Layout update(Layout layout)
		throws Exception {

		return _layoutLocalService.updateLayout(layout);
	}

	protected void updateFriendlyURLs(JournalArticle article)
		throws Exception {

		Layout layout =
			fetchByUuidGroupId(article.getLayoutUuid(), article.getGroupId());
		if (layout == null) {
			_log.warn(
				"Article " + article.getArticleId() + " (version " +
					article.getVersion() + ") has layout uuid=" +
					article.getLayoutUuid() + " but no layout found");
			return;
		}

		Map<Locale, String> writablePart =
			getFriendlyURLWriteablePart(article, layout);
		if (!writablePart.equals(layout.getFriendlyURLMap())) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Changing friendlyUrl for plid=" + layout.getPlid() +
						" associated with articleId=" + article.getArticleId() +
						" without categorization from " +
						layout.getFriendlyURLMap() + " to " + writablePart);
			}

			_layoutFriendlyURLLocalService.updateLayoutFriendlyURLs(
				layout.getUserId(), article.getCompanyId(), layout.getGroupId(),
				layout.getPlid(), layout.isPrivateLayout(),
				normalizeFriendlyUrl(writablePart), new ServiceContext());
		}
	}

	protected void updateFriendlyURLs(Layout layout, JournalArticle article)
		throws Exception {

		Layout parent = fetchByPlid(layout.getParentPlid());
		Map<Locale, String> writablePart =
			getFriendlyURLWriteablePart(article, layout);
		Map<Locale, String> friendlyUrl =
			mergeFriendlyUrlParts(parent.getFriendlyURLMap(), writablePart);
		if (layout.getFriendlyURLMap() == null ||
			!friendlyUrl.equals(layout.getFriendlyURLMap())) {
			_layoutFriendlyURLLocalService.updateLayoutFriendlyURLs(
				layout.getUserId(), article.getCompanyId(), layout.getGroupId(),
				layout.getPlid(), layout.isPrivateLayout(),
				normalizeFriendlyUrl(friendlyUrl), new ServiceContext());
		}
	}

	protected void updateFriendlyURLs(
		Layout parent, Layout child, JournalArticle article)
		throws Exception {

		Map<Locale, String> writablePart =
			getFriendlyURLWriteablePart(article, child);
		Map<Locale, String> friendlyUrl =
			mergeFriendlyUrlParts(parent.getFriendlyURLMap(), writablePart);
		if (!friendlyUrl.equals(child.getFriendlyURLMap())) {
			_layoutFriendlyURLLocalService.updateLayoutFriendlyURLs(
				child.getUserId(), article.getCompanyId(), child.getGroupId(),
				child.getPlid(), child.isPrivateLayout(),
				normalizeFriendlyUrl(friendlyUrl), new ServiceContext());
		}
	}

	protected void updateFriendlyURLs(
		JournalArticle article, AssetCategory category,
		Map<Locale, String> readonlyPart)
		throws Exception {

		Layout layout =
			fetchByUuidGroupId(article.getLayoutUuid(), article.getGroupId());
		if (layout == null) {
			_log.warn(
				"Article " + article.getArticleId() + " (version " +
					article.getVersion() + ") has layout uuid=" +
					article.getLayoutUuid() + " but no layout found");
			return;
		}

		Map<Locale, String> writablePart =
			getFriendlyURLWriteablePart(article, layout);
		Map<Locale, String> friendlyUrl =
			mergeFriendlyUrlParts(readonlyPart, writablePart);
		if (!friendlyUrl.equals(layout.getFriendlyURLMap())) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Changing friendlyUrl for plid=" + layout.getPlid() +
						" associated with articleId=" + article.getArticleId() +
						" categorized by categoryId=" +
						category.getCategoryId() + " from " +
						layout.getFriendlyURLMap() + " to " + friendlyUrl);
			}
			_layoutFriendlyURLLocalService.updateLayoutFriendlyURLs(
				layout.getUserId(), article.getCompanyId(), layout.getGroupId(),
				layout.getPlid(), layout.isPrivateLayout(),
				normalizeFriendlyUrl(friendlyUrl), new ServiceContext());
		}
	}

	protected void updateFriendlyURLs(
		boolean keepOnlyWritable, Map<Locale, String> readOnlyPartFriendlyUrl,
		JournalArticle article, Layout layout, SeoBean bean)
		throws Exception {

		Map<Locale, String> friendlyUrl = null;

		if (keepOnlyWritable) {
			friendlyUrl = bean.getWritablePartFriendlyUrl();
		}
		else {
			friendlyUrl = mergeFriendlyUrlParts(
				readOnlyPartFriendlyUrl, bean.getWritablePartFriendlyUrl());
		}

		if (!layout.getFriendlyURLMap().equals(friendlyUrl) &&
			!friendlyUrl.isEmpty()) {
			_layoutFriendlyURLLocalService.updateLayoutFriendlyURLs(
				bean.getUserId(), article.getCompanyId(), article.getGroupId(),
				layout.getPlid(), layout.isPrivateLayout(),
				normalizeFriendlyUrl(friendlyUrl), bean.getServiceContext());

		}
	}

	protected void updateFriendlyURLs(
		Layout layout, JournalArticle article, AssetCategory category,
		Map<Locale, String> readonlyPart, Map<Locale, String> writeablePart)
		throws Exception {

		Map<Locale, String> friendlyUrl =
			mergeFriendlyUrlParts(readonlyPart, writeablePart);
		if (!friendlyUrl.equals(layout.getFriendlyURLMap())) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Changing friendlyUrl for plid=" + layout.getPlid() +
						" associated with articleId=" + article.getArticleId() +
						" categorized by categoryId=" +
						category.getCategoryId() + " from " +
						layout.getFriendlyURLMap() + " to " + friendlyUrl);
			}
			_layoutFriendlyURLLocalService.updateLayoutFriendlyURLs(
				layout.getUserId(), article.getCompanyId(), layout.getGroupId(),
				layout.getPlid(), layout.isPrivateLayout(),
				normalizeFriendlyUrl(friendlyUrl), new ServiceContext());
		}

	}

	protected Layout setLayoutHiddenAndHideFromSiteMap(
		JournalArticle article, boolean hidden)
		throws Exception {

		Layout layout =
			fetchByUuidGroupId(article.getLayoutUuid(), article.getGroupId());

		layout.setHidden(hidden);
		UnicodeProperties p = layout.getTypeSettingsProperties();
		p.setProperty(_PROP_KEY_SITEMAP_INCLUDE, "0");
		layout.setTypeSettingsProperties(p);
		layout = _layoutLocalService.updateLayout(layout);

		return layout;
	}

	protected void syncDescription(Layout layout, JournalArticle article)
		throws Exception {

		if (layout.getDescriptionMap().equals(article.getDescriptionMap())) {
			layout.setDescriptionMap(article.getDescriptionMap());
			layout = _layoutLocalService.updateLayout(layout);
		}
	}

	protected Map<Locale, String> getFriendlyURLReadonlyPart(
		JournalArticle article, Layout layout)
		throws Exception {

		return new PageGeneratorFriendlyURL(article, layout).readonly;
	}

	protected Map<Locale, String> getFriendlyURLWriteablePart(
		JournalArticle article, Layout layout)
		throws Exception {

		return new PageGeneratorFriendlyURL(article, layout).writeable;
	}

	protected long getLayoutPrototypeId(String uuid, long companyId)
		throws Exception {

		return _layoutPrototypeLocalService.getLayoutPrototypeByUuidAndCompanyId(
			uuid, companyId).getLayoutPrototypeId();
	}

	private class PageGeneratorFriendlyURL {

		private final Map<Locale, String> readonly;

		private final Map<Locale, String> writeable;

		private PageGeneratorFriendlyURL(JournalArticle article, Layout layout)
			throws Exception {
			this.readonly = new HashMap<>();
			this.writeable = new HashMap<>();

			Map<Locale, String> map = layout.getFriendlyURLMap();
			for (Map.Entry<Locale, String> e : map.entrySet()) {
				Locale locale = e.getKey();
				String friendlyURL = e.getValue();
				String parentFriendlyURL =
					getNearestNonNullParentFriendlyURL(layout, locale);

				if (Validator.isNull(parentFriendlyURL)) {
					throw new Exception("Parent friendly url cannot be null");
				}

				if (Validator.isNull(friendlyURL)) {
					friendlyURL = StringPool.SLASH;
				}

				if (!friendlyURL.startsWith(StringPool.SLASH)) {
					friendlyURL = StringPool.SLASH + friendlyURL;
				}

				String writeablePart;

				if (!friendlyURL.startsWith(parentFriendlyURL) &&
					!friendlyURL.contains(parentFriendlyURL)) {

					if (friendlyURL.length() > 1) {
						writeablePart = friendlyURL.substring(
							friendlyURL.lastIndexOf(StringPool.SLASH));
					}
					else {
						writeablePart =
							StringPool.SLASH + article.getUrlTitle();
					}
					_log.warn(
						String.format(
							"Parent friendly URL %s is not contained within friendly URL %s. Setting writeable part to %s.",
							parentFriendlyURL, friendlyURL, writeablePart));
				}
				else {
					writeablePart =
						friendlyURL.substring(parentFriendlyURL.length());
					if (StringPool.SLASH.equals(writeablePart)) {
						writeablePart += article.getUrlTitle();
						_log.warn(
							String.format(
								"Writeable part was /, set to %s.",
								writeablePart));
					}
				}

				readonly.put(locale, parentFriendlyURL);
				writeable.put(locale, writeablePart);
			}
		}

	}

	private String getNearestNonNullParentFriendlyURL(
		Layout layout, Locale locale)
		throws Exception {

		String result;
		long parentPlid = layout.getParentPlid();
		if (parentPlid > 0) {
			Layout parent = this.fetchByPlid(parentPlid);
			String friendlyURL = parent.getFriendlyURL(locale);
			if (Validator.isNull(friendlyURL)) {
				result = getNearestNonNullParentFriendlyURL(parent, locale);
			}
			else {
				result = friendlyURL;
			}
		}
		else {
			result = StringPool.BLANK;
		}

		return result;
	}

	private long getParentLayoutId(long groupId, long userId)
		throws PortalException, SystemException {

		Set<Locale> availableLocales = LanguageUtil.getAvailableLocales();
		Map<Locale, String> pageMap = new HashMap<Locale, String>();
		Map<Locale, String> urlMap = new HashMap<Locale, String>();
		for (Locale l : availableLocales) {
			pageMap.put(l, "SEO Container Page");
			urlMap.put(l, "/seo-container-page");
		}

		List<Layout> layouts =
			_layoutLocalService.getLayouts(groupId, false, 0);
		for (Layout layout : layouts) {
			if (layout.getNameMap().equals(pageMap)) {
				return layout.getLayoutId();
			}
		}

		Layout parentLayout = _layoutLocalService.addLayout(
			userId, groupId, false, 0, pageMap, pageMap,
			new HashMap<Locale, String>(), new HashMap<Locale, String>(),
			new HashMap<Locale, String>(), LayoutConstants.TYPE_PORTLET, "",
			true, urlMap, new ServiceContext());
		return parentLayout.getLayoutId();

	}

	private Map<Locale, String> mergeFriendlyUrlParts(
		Map<Locale, String> readonlyPart, Map<Locale, String> writablePart) {

		Map<Locale, String> result = new HashMap<Locale, String>();
		Set<Locale> availableLocales = SetUtil.intersect(
			new HashSet<Locale>(readonlyPart.keySet()),
			new HashSet<Locale>(writablePart.keySet()));
		for (Locale locale : availableLocales) {
			if (Validator.isNotNull(readonlyPart.get(locale)) &&
				Validator.isNotNull(writablePart.get(locale))) {
				result.put(
					locale,
					readonlyPart.get(locale) + writablePart.get(locale));
			}
		}
		return result;
	}

	private Map<Locale, String> normalizeFriendlyUrl(
		Map<Locale, String> friendlyURLMap) {

		for (Map.Entry<Locale, String> entry : friendlyURLMap.entrySet()) {
			entry.setValue(
				FriendlyURLNormalizerUtil.normalize(entry.getValue()));
		}
		return friendlyURLMap;
	}

	@Reference
	private LayoutFriendlyURLLocalService _layoutFriendlyURLLocalService;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutPrototypeLocalService _layoutPrototypeLocalService;

	private static final Log _log = LogFactoryUtil.getLog(LayoutHelper.class);

	private static final boolean _CHILD_LAYOUT_HIDDEN = false;

	private static final String _LAYOUT_PROTOTYPE_LINK_ENABLED =
		"layoutPrototypeLinkEnabled";

	private static final String _LAYOUT_PROTOTYPE_UUID = "layoutPrototypeUuid";

	private static final String _PROP_KEY_SITEMAP_CHANGEFREQ =
		"sitemap-changefreq";

	private static final String _PROP_KEY_SITEMAP_INCLUDE = "sitemap-include";

	private static final String _PROP_KEY_SITEMAP_PRIORITY = "sitemap-priority";

}
