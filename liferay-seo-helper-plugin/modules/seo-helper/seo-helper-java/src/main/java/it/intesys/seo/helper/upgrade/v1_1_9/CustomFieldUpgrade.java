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
package it.intesys.seo.helper.upgrade.v1_1_9;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.upgrade.DummyUpgradeStep;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import it.intesys.seo.helper.common.constants.PGConstants;
import it.intesys.seo.helper.service.PageGeneratorService;

@Component(immediate = true, service = UpgradeStepRegistrator.class)
public class CustomFieldUpgrade implements UpgradeStepRegistrator {
	
	@Override
	public void register(Registry registry) {

		_log.info(
			String.format(
				"Registering upgrade steps for (bundleSymbolicName, fromSchemaVersionString, toSchemaVersionString) = (%s, %s, %s)",
				ConstantsUpgrade_1_1_9.BUNDLE_SYMBOLIC_NAME,
				ConstantsUpgrade_1_1_9.NO_VERSION,
				ConstantsUpgrade_1_1_9.SCHEMA_VERSION_TO));
		
		_log.info(
			String.format(
				"Registering dummy action to insert release_ information from version %s to %s",
				ConstantsUpgrade_1_1_9.NO_VERSION, ConstantsUpgrade_1_1_9.SCHEMA_VERSION_FROM));
		registry.register(
			ConstantsUpgrade_1_1_9.BUNDLE_SYMBOLIC_NAME, ConstantsUpgrade_1_1_9.NO_VERSION,
			ConstantsUpgrade_1_1_9.SCHEMA_VERSION_FROM, new DummyUpgradeStep());
		_log.info(
			String.format(
				"Registering upgrade action to insert upgrade article layout association information from version %s to %s",
				ConstantsUpgrade_1_1_9.SCHEMA_VERSION_FROM,
				ConstantsUpgrade_1_1_9.SCHEMA_VERSION_TO));
		registry.register(
			ConstantsUpgrade_1_1_9.BUNDLE_SYMBOLIC_NAME,
			ConstantsUpgrade_1_1_9.SCHEMA_VERSION_FROM,
			ConstantsUpgrade_1_1_9.SCHEMA_VERSION_TO, new UpgradeProcess() {

				@Override
				protected void doUpgrade()
					throws Exception {

					try {
						_log.info(
							"Running upgrade article-layout association process");

						for (JournalArticle article : _journalArticleLocalService.getArticles()) {

							long primaryKey = article.getPrimaryKey();

							try {
								if (_pageGeneratorService.hasGeneratedPage(
									article)) {
									_log.info(
										String.format(
											"Article with primary key %d has an associated pagegenerator layout, skipping",
											primaryKey));
								}
								else {
									_log.info(
										String.format(
											"Article with primary key %d does not have an associated pagegenerator layout",
											primaryKey));

									String layoutUuid = article.getLayoutUuid();
									if (Validator.isNotNull(layoutUuid)) {
										_log.info(
											String.format(
												"Article with primary key %d has layoutUuid %s",
												primaryKey, layoutUuid));

										long groupId = article.getGroupId();

										try {
											Layout layout =
												_layoutLocalService.getLayoutByUuidAndGroupId(
													layoutUuid, groupId,
													PGConstants.PRIVATE_LAYOUT);

											_log.info(
												String.format(
													"Found layout with (uuid, groupId, privateLayout, primary key) = (%s, %d, %s, %d); adding to expando",
													layoutUuid,
													layout.getGroupId(),
													PGConstants.PRIVATE_LAYOUT,
													layout.getPrimaryKey()));

											_pageGeneratorService.remember(
												layout);
										}
										catch (PortalException e) {
											_log.error(
												String.format(
													"No layout can be found with (uuid, groupId, privateLayout) = (%d, %d, %s)",
													layoutUuid, groupId,
													PGConstants.PRIVATE_LAYOUT),
												e);
										}
									}
									else {
										_log.info(
											String.format(
												"Article with primary key %d has no layoutUuid, skipping",
												primaryKey));
									}
								}
							}
							catch (Exception e) {
								_log.error(
									String.format(
										"Could not determine if article with primary key %d has an associated page",
										primaryKey));
							}
						}
					}
					catch (Exception e) {
						_log.info(
							"An exception occurred while running the upgrade process",
							e);
					}
				}
			});
	}

	private class ConstantsUpgrade_1_1_9 {
		
		private static final String BUNDLE_SYMBOLIC_NAME = "pagegenerator-java";

		private static final String SCHEMA_VERSION_FROM = "1.1.8";

		private static final String SCHEMA_VERSION_TO = "1.1.9";
		
		private static final String NO_VERSION = "0.0.0";

	}
	
	private static final Log _log =
		LogFactoryUtil.getLog(CustomFieldUpgrade.class);
	
	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private PageGeneratorService _pageGeneratorService;

}
