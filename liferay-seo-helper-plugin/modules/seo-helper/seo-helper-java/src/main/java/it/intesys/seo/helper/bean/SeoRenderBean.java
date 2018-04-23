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
package it.intesys.seo.helper.bean;

public class SeoRenderBean {

	public String getFriendlyUrlBase() {

		return _friendlyUrlBase;
	}

	public void setFriendlyUrlBase(String friendlyUrlBase) {

		_friendlyUrlBase = friendlyUrlBase;
	}

	public String getHtmlFriendlyUrlMap() {

		return _htmlFriendlyUrlMap;
	}

	public void setHtmlFriendlyUrlMap(String htmlFriendlyUrlMap) {

		_htmlFriendlyUrlMap = htmlFriendlyUrlMap;
	}

	public String getHtmlKeywordsMap() {

		return _htmlKeywordsMap;
	}

	public void setHtmlKeywordsMap(String htmlKeywordsMap) {

		_htmlKeywordsMap = htmlKeywordsMap;
	}

	public String getHtmlRobotsMap() {

		return _htmlRobotsMap;
	}

	public void setHtmlRobotsMap(String htmlRobotsMap) {

		_htmlRobotsMap = htmlRobotsMap;
	}

	public String getHtmlTitleMap() {

		return _htmlTitleMap;
	}

	public void setHtmlTitleMap(String htmlTitleMap) {

		_htmlTitleMap = htmlTitleMap;
	}

	public String getSitemapChangefreq() {

		return _sitemapChangefreq;
	}

	public void setSitemapChangefreq(String sitemapChangefreq) {

		_sitemapChangefreq = sitemapChangefreq;
	}

	public String getSitemapInclude() {

		return _sitemapInclude;
	}

	public void setSitemapInclude(String sitemapInclude) {

		_sitemapInclude = sitemapInclude;
	}

	public String getSitemapPriority() {

		return _sitemapPriority;
	}

	public void setSitemapPriority(String sitemapPriority) {

		_sitemapPriority = sitemapPriority;
	}

	private String _friendlyUrlBase;
	private String _htmlFriendlyUrlMap;
	private String _htmlKeywordsMap;
	private String _htmlRobotsMap;
	private String _htmlTitleMap;
	private String _sitemapChangefreq;
	private String _sitemapInclude;
	private String _sitemapPriority;

}
