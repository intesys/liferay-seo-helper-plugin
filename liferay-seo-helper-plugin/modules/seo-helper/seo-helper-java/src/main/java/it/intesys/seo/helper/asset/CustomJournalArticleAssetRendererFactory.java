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

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.servlet.ServletContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.util.FieldsToDDMFormValuesConverter;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.service.JournalArticleResourceLocalService;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.journal.util.JournalContent;
import com.liferay.journal.util.JournalConverter;
import com.liferay.journal.web.asset.JournalArticleAssetRenderer;
import com.liferay.journal.web.asset.JournalArticleAssetRendererFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.util.Portal;

import it.intesys.seo.helper.service.PageGeneratorService;

@Component(immediate = true, property = {
	"javax.portlet.name=" + JournalPortletKeys.JOURNAL
}, service = AssetRendererFactory.class)
public class CustomJournalArticleAssetRendererFactory
	extends JournalArticleAssetRendererFactory {

	public CustomJournalArticleAssetRendererFactory() {
		super();
	}

	/*
	 * this method has to be overridden because if not, _portal will be null
	 * since it is referenced here and is private but has no setter method
	 */
	@Override
	public PortletURL getURLAdd(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse, long classTypeId) {

		PortletURL portletURL = _portal.getControlPanelPortletURL(
			liferayPortletRequest, getGroup(liferayPortletRequest),
			JournalPortletKeys.JOURNAL, 0, 0, PortletRequest.RENDER_PHASE);

		portletURL.setParameter("mvcPath", "/edit_article.jsp");

		if (classTypeId > 0) {
			DDMStructure ddmStructure =
				_ddmStructureLocalService.fetchDDMStructure(classTypeId);

			if (ddmStructure != null) {
				portletURL.setParameter(
					"ddmStructureKey", ddmStructure.getStructureKey());
			}
		}

		return portletURL;
	}

	@Override
	@Reference(unbind = "-")
	public void setFieldsToDDMFormValuesConverter(
		FieldsToDDMFormValuesConverter fieldsToDDMFormValuesConverter) {

		super.setFieldsToDDMFormValuesConverter(fieldsToDDMFormValuesConverter);
		_fieldsToDDMFormValuesConverter = fieldsToDDMFormValuesConverter;
	}

	@Reference(service = PageGeneratorService.class)
	public void setPageGeneratorService(
		PageGeneratorService pageGeneratorService) {

		_pageGeneratorService = pageGeneratorService;
	}

	@Override
	@Reference(target = "(osgi.web.symbolicname=com.liferay.journal.web)", unbind = "-")
	public void setServletContext(ServletContext servletContext) {

		super.setServletContext(servletContext);
	}

	@Override
	protected JournalArticleAssetRenderer getJournalArticleAssetRenderer(
		JournalArticle article) {

		_log.info("setting pagegenerator journal article asset renderer");

		CustomJournalArticleAssetRenderer journalArticleAssetRenderer =
			new CustomJournalArticleAssetRenderer(article);

		journalArticleAssetRenderer.setFieldsToDDMFormValuesConverter(
			_fieldsToDDMFormValuesConverter);
		journalArticleAssetRenderer.setJournalContent(_journalContent);
		journalArticleAssetRenderer.setJournalConverter(_journalConverter);
		journalArticleAssetRenderer.setServletContext(_servletContext);

		journalArticleAssetRenderer.setPageGeneratorService(
			_pageGeneratorService);

		return journalArticleAssetRenderer;
	}

	@Override
	@Reference(unbind = "-")
	protected void setDDMStructureLocalService(
		DDMStructureLocalService ddmStructureLocalService) {

		super.setDDMStructureLocalService(ddmStructureLocalService);
		_ddmStructureLocalService = ddmStructureLocalService;
	}

	@Override
	@Reference(unbind = "-")
	protected void setJournalArticleLocalService(
		JournalArticleLocalService journalArticleLocalService) {

		super.setJournalArticleLocalService(journalArticleLocalService);
	}

	@Override
	@Reference(unbind = "-")
	protected void setJournalArticleResourceLocalService(
		JournalArticleResourceLocalService journalArticleResourceLocalService) {

		super.setJournalArticleResourceLocalService(
			journalArticleResourceLocalService);
	}

	@Override
	@Reference(unbind = "-")
	protected void setJournalArticleService(
		JournalArticleService journalArticleService) {

		super.setJournalArticleService(journalArticleService);
	}

	@Override
	@Reference(unbind = "-")
	protected void setJournalContent(JournalContent journalContent) {

		super.setJournalContent(journalContent);
		_journalContent = journalContent;
	}

	@Override
	@Reference(unbind = "-")
	protected void setJournalConverter(JournalConverter journalConverter) {

		super.setJournalConverter(journalConverter);
		_journalConverter = journalConverter;
	}

	private DDMStructureLocalService _ddmStructureLocalService;

	private FieldsToDDMFormValuesConverter _fieldsToDDMFormValuesConverter;

	private JournalContent _journalContent;

	private JournalConverter _journalConverter;

	private PageGeneratorService _pageGeneratorService;

	@Reference
	private Portal _portal;

	private ServletContext _servletContext;

	private static final Log _log =
		LogFactoryUtil.getLog(CustomJournalArticleAssetRendererFactory.class);

}
