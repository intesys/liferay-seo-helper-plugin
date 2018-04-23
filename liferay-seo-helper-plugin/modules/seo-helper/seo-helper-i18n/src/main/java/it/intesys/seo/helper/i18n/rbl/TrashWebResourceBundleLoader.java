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

package it.intesys.seo.helper.i18n.rbl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ResourceBundleLoader;

import it.intesys.seo.helper.i18n.BundleConstants;
import it.intesys.seo.helper.i18n.ResourceBundleLoaderAbstract;

//see http://www.ldapexplorer.com/en/manual/109010000-ldap-filter-syntax.htm for filter syntax

@Component(immediate = true, property = {
	"bundle.symbolic.name=" + BundleConstants.SYMBOLIC_NAME_TRASH_WEB,
	"servlet.context.name=" + BundleConstants.SERVLET_CONTEXT_NAME_TRASH_WEB,
	"resource.bundle.base.name=" + BundleConstants.BUNDLE_BASE_NAME
}, service = ResourceBundleLoader.class)
public class TrashWebResourceBundleLoader
	extends ResourceBundleLoaderAbstract {

	@Reference(target = "(&(bundle.symbolic.name=" +
		BundleConstants.SYMBOLIC_NAME_TRASH_WEB + ")" + _EXCLUDE_SELF + ")")
	public void init(
		ResourceBundleLoader resourceBundleLoader) {

		_log.info(
			"Init Intesys resource bundle for " +
				BundleConstants.SYMBOLIC_NAME_TRASH_WEB);
		super.init(resourceBundleLoader);
	}

	private static final Log _log =
		LogFactoryUtil.getLog(TrashWebResourceBundleLoader.class);

	private static final String _EXCLUDE_SELF =
		"(!(component.name=it.intesys.seo.helper.i18n.rbl.TrashWebResourceBundleLoader))";

}
