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

import java.util.Locale;
import java.util.Map;

import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.UnicodeProperties;

public class SeoBean {

	public String getArticleId() {

		return _articleId;
	}

	public void setArticleId(String articleId) {

		_articleId = articleId;
	}

	public boolean isEnablePrototypePageLink() {

		return _enablePrototypePageLink;
	}

	public void setEnablePrototypePageLink(boolean enablePrototypePageLink) {

		_enablePrototypePageLink = enablePrototypePageLink;
	}

	public boolean isGeneratePage() {

		return _generatePage;
	}

	public void setGeneratePage(boolean generatePage) {

		_generatePage = generatePage;
	}

	public long getLayoutPrototypeId() {

		return _layoutPrototypeId;
	}

	public void setLayoutPrototypeId(long layoutPrototypeId) {

		_layoutPrototypeId = layoutPrototypeId;
	}

	public boolean isFromModifyArticleView() {
		
		return _fromModifyArticleView;
	}
	
	public void setFromModifyArticleView(boolean fromWodifyArticleView) {
		
		_fromModifyArticleView = fromWodifyArticleView;
	}
	
	public long getParentPlid() {

		return _parentPlid;
	}

	public void setParentPlid(long parentPlid) {

		_parentPlid = parentPlid;
	}

	public Map<Locale, String> getSeoKeywords() {

		return _seoKeywords;
	}

	public void setSeoKeywords(Map<Locale, String> seoKeywords) {

		_seoKeywords = seoKeywords;
	}

	public Map<Locale, String> getSeoRobots() {

		return _seoRobots;
	}

	public void setSeoRobots(Map<Locale, String> seoRobots) {

		_seoRobots = seoRobots;
	}

	public Map<Locale, String> getSeoTitle() {

		return _seoTitle;
	}

	public void setSeoTitle(Map<Locale, String> seoTitle) {

		_seoTitle = seoTitle;
	}

	public ServiceContext getServiceContext() {

		return _serviceContext;
	}

	public void setServiceContext(ServiceContext serviceContext) {

		_serviceContext = serviceContext;
	}

	public UnicodeProperties getTypeSettingsProperties() {

		return _typeSettingsProperties;
	}

	public void setTypeSettingsProperties(
		UnicodeProperties typeSettingsProperties) {

		_typeSettingsProperties = typeSettingsProperties;
	}

	public long getUserId() {

		return _userId;
	}

	public void setUserId(long userId) {

		_userId = userId;
	}

	public long getVocabularyId() {

		return _vocabularyId;
	}

	public void setVocabularyId(long vocabularyId) {

		_vocabularyId = vocabularyId;
	}

	public Map<Locale, String> getWritablePartFriendlyUrl() {

		return _writablePartFriendlyUrl;
	}

	public void setWritablePartFriendlyUrl(
		Map<Locale, String> writablePartFriendlyUrl) {

		_writablePartFriendlyUrl = writablePartFriendlyUrl;
	}

	public void setArticlePrimaryKey(long articlePrimaryKey) {
		
		_articlePrimaryKey = articlePrimaryKey;
		
	}
	
	public long getArticlePrimaryKey() {
		
		return _articlePrimaryKey;
	}
	
	private String _articleId;
	private boolean _enablePrototypePageLink;
	private boolean _generatePage;
	private long _layoutPrototypeId;
	private boolean _fromModifyArticleView;
	private long _parentPlid;
	private UnicodeProperties _typeSettingsProperties;
	private long _userId;
	private long _vocabularyId;
	private Map<Locale, String> _writablePartFriendlyUrl;
	private Map<Locale, String> _seoKeywords;
	private Map<Locale, String> _seoRobots;
	private Map<Locale, String> _seoTitle;
	private ServiceContext _serviceContext;
	private long _articlePrimaryKey;
}
