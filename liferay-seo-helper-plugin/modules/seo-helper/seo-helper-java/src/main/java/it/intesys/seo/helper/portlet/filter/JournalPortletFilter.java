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
package it.intesys.seo.helper.portlet.filter;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.PortletFilter;
import javax.portlet.filter.RenderFilter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import it.intesys.seo.helper.bean.SeoRenderBean;
import it.intesys.seo.helper.common.constants.AttrKeys;
import it.intesys.seo.helper.common.constants.ParamKeys;
import it.intesys.seo.helper.service.PageGeneratorService;

@Component(immediate = true, property = {
	"javax.portlet.name=" + JournalPortletKeys.JOURNAL
}, service = PortletFilter.class)
public class JournalPortletFilter implements RenderFilter, ActionFilter {

	@Override
	public void init(FilterConfig filterConfig)
		throws PortletException {

	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(
		RenderRequest request, RenderResponse response, FilterChain chain)
		throws IOException, PortletException {

		String articleId =
			ParamUtil.getString(request, ParamKeys.ARTICLE_ID, "");
		long groupId = ParamUtil.getLong(request, ParamKeys.GROUP_ID);

		try {
			SeoRenderBean bean;

			JournalArticle article =
				_pageGeneratorService.fetchJournalArticleByGroupIdArticleId(
					groupId, articleId);

			if (Validator.isNotNull(article)) {
				if (_pageGeneratorService.hasGeneratedPage(article)) {
					bean = _pageGeneratorService.getSeoRenderBean(
						article, PortalUtil.getLocale(request));
				}
				else {
					bean = _pageGeneratorService.getDefaultSeoRenderBean(
						(ThemeDisplay) request.getAttribute(
							WebKeys.THEME_DISPLAY),
						article);
				}

				mapBeanToRenderRequest(bean, request);
			}
		}
		catch (Exception e) {
			throw new PortletException(e);
		}

		chain.doFilter(request, response);
	}

	@Override
	public void doFilter(
		ActionRequest request, ActionResponse response, FilterChain chain)
		throws IOException, PortletException {

		chain.doFilter(request, response);
	}

	private void mapBeanToRenderRequest(
		SeoRenderBean bean, RenderRequest renderRequest) {

		renderRequest.setAttribute(
			AttrKeys.SEO_HTML_FRIENDLY_URL_MAP, bean.getHtmlFriendlyUrlMap());
		renderRequest.setAttribute(
			AttrKeys.SEO_HTML_TITLE_MAP, bean.getHtmlTitleMap());
		renderRequest.setAttribute(
			AttrKeys.SEO_HTML_KEYWORDS_MAP, bean.getHtmlKeywordsMap());
		renderRequest.setAttribute(
			AttrKeys.SEO_HTML_ROBOTS_MAP, bean.getHtmlRobotsMap());
		renderRequest.setAttribute(
			AttrKeys.SEO_SITEMAP_INCLUDE, bean.getSitemapInclude());
		renderRequest.setAttribute(
			AttrKeys.SEO_SITEMAP_PRIORITY, bean.getSitemapPriority());
		renderRequest.setAttribute(
			AttrKeys.SEO_SITEMAP_CHANGEFREQ, bean.getSitemapChangefreq());
		renderRequest.setAttribute(
			AttrKeys.ALL_FRIENDLY_URL_BASE, bean.getFriendlyUrlBase());
		renderRequest.setAttribute(
			AttrKeys.SHORTEN_FRIENDLY_URL_CASE,
			StringUtil.shorten(bean.getFriendlyUrlBase(), 40));
	}

	@Reference
	private PageGeneratorService _pageGeneratorService;
}
