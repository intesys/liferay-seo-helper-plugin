<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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
--%>

<%@ page import="it.intesys.seo.helper.common.constants.PGConstants"%>
<%@ page import="it.intesys.seo.helper.common.constants.ParamKeys"%>
<%@ page import="com.liferay.portal.kernel.exception.SitemapIncludeException" %>
<%@ page import="com.liferay.portal.kernel.exception.SitemapPagePriorityException" %>
<%@ page import="com.liferay.portal.kernel.exception.SitemapChangeFrequencyException" %>
<%@ page import="com.liferay.asset.kernel.model.AssetVocabulary" %>
<%@ page import="com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil" %>
<%@ page import="com.liferay.expando.kernel.service.ExpandoValueLocalServiceUtil" %>
<%@ page import="com.liferay.expando.kernel.model.ExpandoValue" %>

<%@ include file="/init.jsp" %>

<%
JournalArticle article = journalDisplayContext.getArticle();

long groupId = BeanParamUtil.getLong(article, request, "groupId", scopeGroupId);

Group group = GroupLocalServiceUtil.fetchGroup(groupId);

boolean changeStructure = GetterUtil.getBoolean(request.getAttribute("edit_article.jsp-changeStructure"));
%>

<%-- pagegenerator --%>
<aui:input name="<%= ParamKeys.FROM_MODIFY_ARTICLE_VIEW %>" type="hidden" value="true" />
<%-- pagegenerator --%>

<c:choose>
	<c:when test="<%= group.isLayout() %>">
		<p class="text-muted">
			<liferay-ui:message key="the-display-page-cannot-be-set-when-the-scope-of-the-web-content-is-a-page" />
		</p>
	</c:when>
	<c:otherwise>
		<%
		String layoutUuid = BeanParamUtil.getString(article, request, "layoutUuid");

		if (changeStructure && (article != null)) {
			layoutUuid = article.getLayoutUuid();
		}

		String layoutBreadcrumb = StringPool.BLANK;

		if (Validator.isNotNull(layoutUuid)) {
			Layout selLayout = LayoutLocalServiceUtil.fetchLayoutByUuidAndGroupId(layoutUuid, themeDisplay.getSiteGroupId(), false);

			if (selLayout == null) {
				selLayout = LayoutLocalServiceUtil.fetchLayoutByUuidAndGroupId(layoutUuid, themeDisplay.getSiteGroupId(), true);
			}

			if (selLayout != null) {
				layoutBreadcrumb = _getLayoutBreadcrumb(request, selLayout, locale);
			}
		}
		%>
		
		<%-- pagegenerator --%>
		<c:choose>
			<c:when test="<%= !isPageGenerated(groupId, layoutUuid) %>">
				<aui:fieldset label="intesys-display-page-generation-set">
					<aui:input name="<%= ParamKeys.ENABLE_PAGE_GENERATION %>" type="checkbox" value="false" label="intesys-display-page-enable-generation-flag"/>
<!-- 					hide asset vocabulary in jsp <liferay-ui:message key="intesys-display-page-select-category-or-layout"/> -->
					<aui:row >
						<div class="col-md-12 ">
							<aui:select inlineField="true" label="intesys-display-page-parent-layout" name="<%= ParamKeys.GENERATION_PARENT_PLID %>">
								<aui:option value="" />
								<optgroup label="<liferay-ui:message key="public-pages" />">
									<%=getLayoutsOption(themeDisplay.getScopeGroupId(), false, 0, 0, 0) %>
								</optgroup>
								<optgroup label="<liferay-ui:message key="private-pages" />">
									<%=getLayoutsOption(themeDisplay.getScopeGroupId(), true, 0, 0, 0) %>
								</optgroup>
							</aui:select>	
							
							<%-- hide asset vocabulary in jsp --%>
							<%--
							
							<aui:select inlineField="true" label="intesys-display-page-vocabulary" name="<%= ParamKeys.GENERATION_VOCABULARY %>" >
								<aui:option label="" value="" />
								<%
								Group companyGroup = company.getGroup();
								if (themeDisplay.getScopeGroupId() != companyGroup.getGroupId()) {
									List<AssetVocabulary> assetVocabularies = AssetVocabularyLocalServiceUtil.getGroupVocabularies(scopeGroupId, false);
									if (!assetVocabularies.isEmpty()) {
									%>
										<optgroup label="<liferay-ui:message key="intesys-display-page-vocabulary-local" />">
											<%
											for (AssetVocabulary assetVocabulary : assetVocabularies) {
												assetVocabulary = assetVocabulary.toEscapedModel();
											%>
												<aui:option label="<%= assetVocabulary.getTitle(themeDisplay.getLocale()) %>" value="<%= assetVocabulary.getVocabularyId() %>" />
											<%
											}
											%>
										</optgroup>
									<%
									}
								}
								List<AssetVocabulary> assetVocabularies = AssetVocabularyLocalServiceUtil.getGroupVocabularies(companyGroup.getGroupId(), false);
								if (!assetVocabularies.isEmpty()) {
								%>
									<optgroup label="<liferay-ui:message key="intesys-display-page-vocabulary-global" />">
										<%
										for (AssetVocabulary assetVocabulary : assetVocabularies) {
											assetVocabulary = assetVocabulary.toEscapedModel();
										%>
											<aui:option label="<%= assetVocabulary.getTitle(themeDisplay.getLocale()) %>" value="<%= assetVocabulary.getVocabularyId() %>" />
										<%
										}
										%>
									</optgroup>
								<%
								}
								%>
							</aui:select>
							
							--%>
						</div>
					</aui:row>
					<aui:select label="intesys-display-page-page-template" name="<%= ParamKeys.GENERATION_PAGE_TEMPLATE %>"  >
						<aui:option label="" value="" />
						<%
						List<LayoutPrototype> layoutPrototypes= LayoutPrototypeLocalServiceUtil.getLayoutPrototypes(-1, -1);
						for(LayoutPrototype lp: layoutPrototypes){
						%>
							<aui:option label="<%=lp.getName(themeDisplay.getLocale()) %>" value="<%= lp.getLayoutPrototypeId() %>" />
						<%
						}
						%>
					</aui:select>
					<aui:input name="<%= ParamKeys.ENABLE_PROTOTYPE_PAGE_LINK %>" type="checkbox" value="true" label="intesys-display-page-enable-prototype-link"/>
				</aui:fieldset>
			</c:when>
			<c:otherwise>
				<%
				Layout generatedLayout = LayoutLocalServiceUtil.getLayoutByUuidAndGroupId(layoutUuid, groupId, PGConstants.PRIVATE_LAYOUT);
				Group generatedLayoutGroup = GroupLocalServiceUtil.getGroup(generatedLayout.getGroupId());
				%>
				<liferay-ui:message key="intesys-display-page-web-content-association" arguments="<%= new String[] { layoutUuid } %>"/><br/>
				<table>
					<tr>
						<th><liferay-ui:message key="intesys-display-page-language" /></th>
						<th><liferay-ui:message key="intesys-display-page-friendly-url" /></th>
					</tr>
				<% for(Map.Entry<Locale, String> entry:generatedLayout.getFriendlyURLMap().entrySet()){ %>
					<tr>
						<td><%= entry.getKey() %></td>
						<%-- generatedLayoutGroup sostituisce group qui nell'url --%> 
						<td><a target="_blank" href="/<%= entry.getKey() %>/web<%=generatedLayoutGroup.getFriendlyURL()+entry.getValue()%>"><%= entry.getValue() %></a></td>
					</tr>
				<% } %>
				</table>
				<aui:fieldset label="intesys-display-page-seo-set">
					<c:if  test="<%= !Validator.isNull(layoutUuid) %>">
						<div class="form-group">
							<label for="<portlet:namespace/>friendlyURL">
								<liferay-ui:message key="friendly-url" />
								<liferay-ui:icon-help message='<%= LanguageUtil.format(request, "for-example-x", "<em>/news</em>", false) %>'/>
							</label>
							<div class="input-group liferay-friendly-url-input-group">
								<span class="input-group-addon" id="<portlet:namespace/>urlBase">
									<span class="input-group-constrain">
										<liferay-ui:message key="${shortenFriendlyURLBase}" />
									</span>
								</span>
								<liferay-ui:input-localized cssClass="form-control" defaultLanguageId="<%= LocaleUtil.toLanguageId(themeDisplay.getSiteDefaultLocale()) %>" name="<%= ParamKeys.GENERATION_SEO_FRIENDLY_URL %>" xml="${seoHtmlFrinedlyUrlMap}" />
							</div>
						</div>
					</c:if>
					<aui:input name="<%= ParamKeys.GENERATION_SEO_TITLE %>" type="textarea" localized="true" label="html-title" value="${seoHtmlTitleMap}" />
					<aui:input name="<%= ParamKeys.GENERATION_SEO_KEYWORDS %>" type="textarea" localized="true" label="keywords" value="${seoHtmlKeywordsMap}"/>
					<aui:input name="<%= ParamKeys.GENERATION_SEO_ROBOTS %>" type="textarea" localized="true" label="robots" value="${seoHtmlRobotsMap}"/>
					<h4><liferay-ui:message key="sitemap" /></h4>
					<liferay-ui:error exception="<%= SitemapChangeFrequencyException.class %>" message="please-select-a-valid-change-frequency" />
					<liferay-ui:error exception="<%= SitemapIncludeException.class %>" message="please-select-a-valid-include-value" />
					<liferay-ui:error exception="<%= SitemapPagePriorityException.class %>" message="please-enter-a-valid-page-priority" />
					<aui:select label="include" name="<%= ParamKeys.TYPE_SETTINGS_PROPERTIES_SITEMAP_INCLUDE %>">
						<aui:option label="yes" value="1" selected='${seoSitemapInclude eq "1"}'/>
						<aui:option label="no" value="0" selected='${seoSitemapInclude eq "0"}'/>
					</aui:select>
					<aui:input helpMessage="(0.0 - 1.0)" label="page-priority" name="<%= ParamKeys.TYPE_SETTINGS_PROPERTIES_SITEMAP_PRIORITY %>" size="3" type="text" value="${seoSitemapPriority}">
						<aui:validator name="number" />
						<aui:validator errorMessage="please-enter-a-valid-page-priority" name="range">[0,1]</aui:validator>
					</aui:input>
					<aui:select label="change-frequency" name="<%= ParamKeys.TYPE_SETTINGS_PROPERTIES_SITEMAP_CHANGEFREQ %>">
						<aui:option label="always" selected='${seoSitemapChangefreq eq "always"}' />
						<aui:option label="hourly" selected='${seoSitemapChangefreq eq "hourly"}' />
						<aui:option label="daily" selected='${seoSitemapChangefreq eq "daily"}' />
						<aui:option label="weekly" selected='${seoSitemapChangefreq eq "weekly"}' />
						<aui:option label="monthly" selected='${seoSitemapChangefreq eq "monthly"}' />
						<aui:option label="yearly" selected='${seoSitemapChangefreq eq "yearly"}' />
						<aui:option label="never" selected='${seoSitemapChangefreq eq "never"}' />
					</aui:select>
				</aui:fieldset>
			</c:otherwise>
		</c:choose>
		<%-- pagegenerator --%>
		

		<liferay-ui:error-marker key="<%= WebKeys.ERROR_SECTION %>" value="display-page" />

		<aui:input id="pagesContainerInput" ignoreRequestValue="<%= true %>" name="layoutUuid" type="hidden" value="<%= layoutUuid %>" />
<%-- pagegenerator remove 
		<p class="text-muted">
			<liferay-ui:message key="default-display-page-help" />
		</p>

		<p class="text-default">
			<span class="<%= Validator.isNull(layoutBreadcrumb) ? "hide" : StringPool.BLANK %>" id="<portlet:namespace />displayPageItemRemove" role="button">
				<aui:icon cssClass="icon-monospaced" image="times" markupView="lexicon" />
			</span>
			<span id="<portlet:namespace />displayPageNameInput">
				<c:choose>
					<c:when test="<%= Validator.isNull(layoutBreadcrumb) %>">
						<span class="text-muted"><liferay-ui:message key="none" /></span>
					</c:when>
					<c:otherwise>
						<%= layoutBreadcrumb %>
					</c:otherwise>
				</c:choose>
			</span>
		</p>
--%>
		<%-- 
		<aui:button name="chooseDisplayPage" value="choose" />

		<c:if test="<%= (article != null) && Validator.isNotNull(layoutUuid) %>">

			<%
			Layout defaultDisplayLayout = LayoutLocalServiceUtil.fetchLayoutByUuidAndGroupId(layoutUuid, scopeGroupId, false);

			if (defaultDisplayLayout == null) {
				defaultDisplayLayout = LayoutLocalServiceUtil.fetchLayoutByUuidAndGroupId(layoutUuid, scopeGroupId, true);
			}

			AssetRendererFactory<JournalArticle> assetRendererFactory = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClass(JournalArticle.class);

			AssetRenderer<JournalArticle> assetRenderer = assetRendererFactory.getAssetRenderer(article.getResourcePrimKey());

			String urlViewInContext = assetRenderer.getURLViewInContext(liferayPortletRequest, liferayPortletResponse, currentURL);
			%>

			<c:if test="<%= Validator.isNotNull(urlViewInContext) %>">
				<aui:a href="<%= urlViewInContext %>" target="blank">
					<liferay-ui:message arguments="<%= HtmlUtil.escape(defaultDisplayLayout.getName(locale)) %>" key="view-content-in-x" translateArguments="<%= false %>" />
				</aui:a>
			</c:if>
		</c:if>
		--%>
		<%
		String eventName = liferayPortletResponse.getNamespace() + "selectDisplayPage";

		ItemSelector itemSelector = (ItemSelector)request.getAttribute(JournalWebKeys.ITEM_SELECTOR);

		LayoutItemSelectorCriterion layoutItemSelectorCriterion = new LayoutItemSelectorCriterion();

		layoutItemSelectorCriterion.setCheckDisplayPage(true);

		List<ItemSelectorReturnType> desiredItemSelectorReturnTypes = new ArrayList<ItemSelectorReturnType>();

		desiredItemSelectorReturnTypes.add(new UUIDItemSelectorReturnType());

		layoutItemSelectorCriterion.setDesiredItemSelectorReturnTypes(desiredItemSelectorReturnTypes);

		PortletURL itemSelectorURL = itemSelector.getItemSelectorURL(RequestBackedPortletURLFactoryUtil.create(liferayPortletRequest), eventName, layoutItemSelectorCriterion);
		%>

		<aui:script use="liferay-item-selector-dialog">
			var displayPageItemContainer = $('#<portlet:namespace />displayPageItemContainer');
			var displayPageItemRemove = $('#<portlet:namespace />displayPageItemRemove');
			var displayPageNameInput = $('#<portlet:namespace />displayPageNameInput');
			var pagesContainerInput = $('#<portlet:namespace />pagesContainerInput');

			$('#<portlet:namespace />chooseDisplayPage').on(
				'click',
				function(event) {
					var itemSelectorDialog = new A.LiferayItemSelectorDialog(
						{
							eventName: '<%= eventName %>',
							on: {
								selectedItemChange: function(event) {
									var selectedItem = event.newVal;

									if (selectedItem) {
										pagesContainerInput.val(selectedItem.value);

										displayPageNameInput.html(selectedItem.layoutpath);

										displayPageItemRemove.removeClass('hide');
									}
								}
							},
							'strings.add': '<liferay-ui:message key="done" />',
							title: '<liferay-ui:message key="select-page" />',
							url: '<%= itemSelectorURL.toString() %>'
						}
					);

					itemSelectorDialog.open();
				}
			);

			displayPageItemRemove.on(
				'click',
				function(event) {
					displayPageNameInput.html('<liferay-ui:message key="none" />');

					pagesContainerInput.val('');

					displayPageItemRemove.addClass('hide');
				}
			);
		</aui:script>
	</c:otherwise>
</c:choose>
<%!
private String _getLayoutBreadcrumb(HttpServletRequest request, Layout layout, Locale locale) throws Exception {
	List<Layout> ancestors = layout.getAncestors();

	StringBundler sb = new StringBundler(4 * ancestors.size() + 5);

	if (layout.isPrivateLayout()) {
		sb.append(LanguageUtil.get(request, "private-pages"));
	}
	else {
		sb.append(LanguageUtil.get(request, "public-pages"));
	}

	sb.append(StringPool.SPACE);
	sb.append(StringPool.GREATER_THAN);
	sb.append(StringPool.SPACE);

	Collections.reverse(ancestors);

	for (Layout ancestor : ancestors) {
		sb.append(HtmlUtil.escape(ancestor.getName(locale)));
		sb.append(StringPool.SPACE);
		sb.append(StringPool.GREATER_THAN);
		sb.append(StringPool.SPACE);
	}

	sb.append(HtmlUtil.escape(layout.getName(locale)));

	return sb.toString();
}
%>
<%-- pagegenerator --%>
<%!
private String getLayoutsOption(long groupId, boolean privateLayout, long parentId, int depth, long rootPlid) throws Exception{
	List<Layout> layouts = LayoutLocalServiceUtil.getLayouts(groupId, privateLayout, parentId);
	if(layouts==null || layouts.isEmpty()){
		return "";
	}
	StringBuilder sb=new StringBuilder("");
	String namePrefix="";
	for (int j = 0; j < depth; j++) {
		namePrefix = "-&nbsp;" + namePrefix;
	}
	depth++;
	for(Layout layout: layouts){
		sb.append("<option value=");
		sb.append("\""+layout.getPlid()+"\" ");
		if(layout.getPlid()==rootPlid){
			sb.append("selected ");
		}
		sb.append(">"+namePrefix+layout.getNameCurrentValue());
		sb.append("</option>");
		sb.append(getLayoutsOption(groupId, privateLayout, layout.getLayoutId(), depth, rootPlid));
	}
	return sb.toString();
}

// TODO Find a better way to do this. Can custom osgi components be accessed here?
// much of this is copied from PageGeneratorServiceImpl and PageGeneratorExpandoServiceImpl
private boolean isPageGenerated(long groupId, String layoutUuid) {

	boolean pageGenerated;
	ExpandoValue value = null;
	
	try {
		if (Validator.isNotNull(layoutUuid)) {
			Layout layout = LayoutLocalServiceUtil.getLayoutByUuidAndGroupId(layoutUuid, groupId, PGConstants.PRIVATE_LAYOUT);
			
			value = ExpandoValueLocalServiceUtil.getValue(
				layout.getCompanyId(), Layout.class.getName(),
				"LAYOUT", "LAYOUT_UUID",
				layout.getPrimaryKey());
		}
		
		pageGenerated = Validator.isNotNull(value);
	} catch (Exception e) {
		pageGenerated = false;
	}
	
	return pageGenerated;
}
%>
<%-- pagegenerator --%>
