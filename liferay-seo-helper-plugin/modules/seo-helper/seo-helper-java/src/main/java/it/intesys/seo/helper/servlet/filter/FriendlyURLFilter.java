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
package it.intesys.seo.helper.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.ParamUtil;

import it.intesys.seo.helper.service.PageGeneratorService;

@Component(immediate = true, property = {
	"after-filter=FriendlyURLServlet", "servlet-context-name=",
	"servlet-filter-name=Friendly URL Custom Filter",
	"url-pattern=/c/portal/layout", "dispatcher=FORWARD"
}, service = Filter.class)
public class FriendlyURLFilter extends BaseFilter {

	/*
	 * Copied from class com.liferay.portal.util.WebKeys, not accessible from
	 * classpath
	 */
	public static final String LAYOUT_ASSET_ENTRY =
		"LIFERAY_SHARED_LAYOUT_ASSET_ENTRY";

	@Override
	public void processFilter(
		HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain)
		throws IOException, ServletException {

		try {
			long plid = ParamUtil.getLong(request, "p_l_id", 0);

			if (plid > 0) {
				AssetEntry ae = _pageGeneratorService.getLayoutAssetEntry(plid);

				if (ae != null) {
					request.setAttribute(LAYOUT_ASSET_ENTRY, ae);
					_log.debug(
						"Set attribute " + LAYOUT_ASSET_ENTRY + " to " + ae);
				}
			}
			else {
				_log.info("No plid found in request");
			}
		}
		catch (Exception e) {
			_log.error("Can not set layout asset entry", e);
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected Log getLog() {

		return _log;
	}

	@Reference
	private PageGeneratorService _pageGeneratorService;

	private static final Log _log =
		LogFactoryUtil.getLog(FriendlyURLFilter.class);
}
