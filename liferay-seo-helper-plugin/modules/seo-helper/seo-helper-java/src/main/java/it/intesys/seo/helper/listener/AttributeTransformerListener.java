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

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.templateparser.BaseTransformerListener;
import com.liferay.portal.kernel.templateparser.TransformerListener;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;

import it.intesys.seo.helper.service.PageGeneratorService;

@Component(immediate = true, property = {
	"javax.portlet.name=" + JournalPortletKeys.JOURNAL
}, service = TransformerListener.class)
public class AttributeTransformerListener extends BaseTransformerListener {

	@Activate
	public void activate() {

		_log.info("Activating custom attribute transformer listener");
	}

	@Override
	public Document onXml(
		Document document, String languageId, Map<String, String> tokens) {

		try {
			doTokenHandler(document, languageId, tokens);
		}
		catch (Exception e) {
			_log.info("Could not run custom token handler", e);
		}

		return document;
	}

	private void doTokenHandler(
		Document document, String languageId, Map<String, String> tokens)
		throws Exception {

		long groupId = GetterUtil.getLong(tokens.get(_ARTICLE_GROUP_ID));
		long articleResourcePrimKey =
			GetterUtil.getLong(tokens.get(_ARTICLE_RESOURCE_PK));

		String friendlyUrl =
			_pageGeneratorService.getAssetEntryLayoutFriendlyUrl(
				groupId, articleResourcePrimKey, languageId);
		addReservedEl(
			document.getRootElement(), tokens, _FRIENDLY_URL, friendlyUrl);

		String parentFriendlyUrl =
			_pageGeneratorService.getAssetEntryParentLayoutFriendlyUrl(
				groupId, articleResourcePrimKey, languageId);
		addReservedEl(
			document.getRootElement(), tokens, _PARENT_FRIENDLY_URL,
			parentFriendlyUrl);

	}

	// COPIED FROM JournalUtil
	private void addReservedEl(
		Element rootElement, Map<String, String> tokens, String name,
		String value) {

		// XML
		if (rootElement != null) {
			Element dynamicElementElement =
				rootElement.addElement("dynamic-element");

			dynamicElementElement.addAttribute("name", name);

			dynamicElementElement.addAttribute("type", "text");

			Element dynamicContentElement =
				dynamicElementElement.addElement("dynamic-content");

			// dynamicContentElement.setText("<![CDATA[" + value + "]]>");
			dynamicContentElement.setText(value);
		}

		// Tokens
		tokens.put(
			StringUtil.replace(name, StringPool.DASH, StringPool.UNDERLINE), value);
	}

	private static final String _FRIENDLY_URL = "pageGeneratorFriendlyUrl";

	private static final String _PARENT_FRIENDLY_URL =
		"pageGeneratorParentFriendlyUrl";

	private static final String _ARTICLE_GROUP_ID = "article_group_id";

	private static final String _ARTICLE_RESOURCE_PK = "article_resource_pk";

	@Reference
	private PageGeneratorService _pageGeneratorService;

	private static final Log _log =
		LogFactoryUtil.getLog(AttributeTransformerListener.class);
}
