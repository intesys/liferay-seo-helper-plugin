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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.expando.kernel.exception.NoSuchTableException;
import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.model.ExpandoValue;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
import com.liferay.expando.kernel.service.ExpandoTableLocalService;
import com.liferay.expando.kernel.service.ExpandoValueLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.util.Validator;

@Component(immediate = true, service = ExpandoHelper.class)
public class ExpandoHelper {

	protected void deleteUpdateJournalArticle(
		JournalArticle article, Layout parentLayout, long vocabularyId)
		throws Exception {

		ExpandoTable table = getOrCreateExpandoTable(
			article.getCompanyId(), JournalArticle.class.getName(),
			_EXPANDO_TABLE_NAME_SEO);
		getOrCreateExpandoColumn(
			table, _EXPANDO_COLUMN_VOCABULARY_SEO, ExpandoColumnConstants.LONG);
		try {
			removeSeoVocabularyValues(article);
		}
		catch (Exception e) {
			// Nothing to do here
		}
		if (Validator.isNull(parentLayout)) {
			_expandoValueLocalService.addValue(
				article.getCompanyId(), JournalArticle.class.getName(),
				_EXPANDO_TABLE_NAME_SEO, _EXPANDO_COLUMN_VOCABULARY_SEO,
				article.getResourcePrimKey(), vocabularyId);
		}
	}
	
	protected long getSeoVocabularyId(JournalArticle article)
		throws Exception {

		long vocabularyId = 0;
		ExpandoValue value = _expandoValueLocalService.getValue(
			article.getCompanyId(), JournalArticle.class.getName(),
			_EXPANDO_TABLE_NAME_SEO, _EXPANDO_COLUMN_VOCABULARY_SEO,
			article.getResourcePrimKey());
		vocabularyId = value != null ? value.getLong() : 0;
		return vocabularyId;
	}

	protected boolean isPageGenerated(Layout layout)
		throws Exception {

		return getPageGeneratorLayoutExpandoValue(layout) != null;
	}

	protected void remember(Layout layout)
		throws Exception {

		String className = Layout.class.getName();
		ExpandoTable table = getOrCreateExpandoTable(
			layout.getCompanyId(), className, _EXPANDO_TABLE_NAME_LAYOUT);
		getOrCreateExpandoColumn(
			table, _EXPANDO_COLUMN_LAYOUT_UUID, ExpandoColumnConstants.STRING);

		String uuid = layout.getUuid();

		_expandoValueLocalService.addValue(
			layout.getCompanyId(), className, _EXPANDO_TABLE_NAME_LAYOUT,
			_EXPANDO_COLUMN_LAYOUT_UUID, layout.getPrimaryKey(), uuid);

		_log.info(
			"Saved expando value " + uuid + " to table " +
				_EXPANDO_TABLE_NAME_LAYOUT + "." + _EXPANDO_COLUMN_LAYOUT_UUID);

	}
	
	protected void removeSeoVocabularyValues(JournalArticle article)
		throws Exception {

		_expandoValueLocalService.deleteValue(
			article.getCompanyId(), JournalArticle.class.getName(),
			_EXPANDO_TABLE_NAME_SEO, _EXPANDO_COLUMN_VOCABULARY_SEO,
			article.getResourcePrimKey());
	}

	protected Object[] toResultEntry(
		AssetCategory category, AssetEntry entry, JournalArticle article)
		throws Exception {

		Object[] resultEntry;

		long vocabularyId = _expandoValueLocalService.getData(
			category.getCompanyId(), JournalArticle.class.getName(),
			_EXPANDO_TABLE_NAME_SEO, _EXPANDO_COLUMN_VOCABULARY_SEO,
			article.getResourcePrimKey(), -1L);
		if (vocabularyId == category.getVocabularyId()) {
			resultEntry = new Object[2];
			resultEntry[0] = article;
			for (AssetCategory articleCategory : entry.getCategories()) {
				if (articleCategory.getVocabularyId() == category.getVocabularyId()) {
					resultEntry[1] = articleCategory;
				}
			}
			if (resultEntry[1] == null) {
				_log.warn(
					"Cannot find any category for article " +
						article.getArticleId() + " and vocabulary " +
						category.getVocabularyId() +
						". Maybe it's a Lucene index problem.");
			}
		}
		else {
			_log.warn(
				String.format(
					"Could not determine result entry for (category.primaryKey, entry.primaryKey, article.primaryKey) : (%d, %d, %d); returning null resultEntry",
					category.getPrimaryKey(), entry.getPrimaryKey(),
					article.getPrimaryKey()));

			resultEntry = null;
		}

		return resultEntry;
	}

	private ExpandoTable getOrCreateExpandoTable(
		long companyId, String className, String tableName)
		throws Exception {

		ExpandoTable table = null;
		try {
			table = _expandoTableLocalService.getTable(
				companyId, className, tableName);
		}
		catch (NoSuchTableException e) {
			table = _expandoTableLocalService.addTable(
				companyId, className, tableName);
		}
		return table;
	}

	private ExpandoColumn getOrCreateExpandoColumn(
		ExpandoTable table, String columnName, int columnType)
		throws Exception {

		ExpandoColumn column = _expandoColumnLocalService.getColumn(
			table.getTableId(), columnName);
		if (column == null) {
			column = _expandoColumnLocalService.addColumn(
				table.getTableId(), columnName, columnType);
		}

		return column;
	}
	
	private ExpandoValue getPageGeneratorLayoutExpandoValue(Layout layout)
		throws Exception {

		ExpandoValue value = _expandoValueLocalService.getValue(
			layout.getCompanyId(), Layout.class.getName(),
			_EXPANDO_TABLE_NAME_LAYOUT, _EXPANDO_COLUMN_LAYOUT_UUID,
			layout.getPrimaryKey());

		return value;
	}

	@Reference
	private ExpandoColumnLocalService _expandoColumnLocalService;

	@Reference
	private ExpandoValueLocalService _expandoValueLocalService;

	@Reference
	private ExpandoTableLocalService _expandoTableLocalService;

	private static final Log _log =
		LogFactoryUtil.getLog(ExpandoHelper.class);

	private static final String _EXPANDO_TABLE_NAME_SEO = "SEO_TABLE";

	private static final String _EXPANDO_COLUMN_VOCABULARY_SEO =
		"SEO_VOCABULARY_ID";

	private static final String _EXPANDO_TABLE_NAME_LAYOUT = "LAYOUT";

	private static final String _EXPANDO_COLUMN_LAYOUT_UUID = "LAYOUT_UUID";

}
