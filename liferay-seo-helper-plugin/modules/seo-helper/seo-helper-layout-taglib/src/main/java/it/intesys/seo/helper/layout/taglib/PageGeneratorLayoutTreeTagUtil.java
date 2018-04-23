
package it.intesys.seo.helper.layout.taglib;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import com.liferay.expando.kernel.model.ExpandoValue;
import com.liferay.expando.kernel.service.ExpandoValueLocalServiceUtil;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleServiceUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portlet.layoutsadmin.util.LayoutsTreeUtil;
import com.liferay.taglib.util.TagResourceBundleUtil;

public class PageGeneratorLayoutTreeTagUtil {

	/**
	 * Adds the custom url to sub into the custom link template in both maps
	 * supplied, portletURLs and portletURLsJSONArray
	 * 
	 * @param request
	 * @param portletURLs
	 * @param portletURLsJSONArray
	 * @param selPlid
	 * @param privateLayout
	 * @param groupId
	 */
	public static void addPageGeneratorUrlToMaps(
		HttpServletRequest request, Map<String, PortletURL> portletURLs,
		JSONArray portletURLsJSONArray, Long selPlid, boolean privateLayout,
		long groupId) {

		final String keyJsonUrlName = "name";
		final String keyJsonUrlValue = "value";
		final PortletURL pagegeneratorEditLayoutURL =
			_instance.getPageGeneratorEditLayoutUrl(
				request, selPlid, privateLayout, groupId);
		final String urlStringValue = pagegeneratorEditLayoutURL.toString();

		portletURLs.put(_JSP_KEY_PAGEGENERATOR_URL, pagegeneratorEditLayoutURL);

		JSONObject joPagegeneratorEditLayoutURL =
			JSONFactoryUtil.createJSONObject();
		joPagegeneratorEditLayoutURL.put(
			keyJsonUrlName, _JSP_KEY_PAGEGENERATOR_URL);
		joPagegeneratorEditLayoutURL.put(keyJsonUrlValue, urlStringValue);
		portletURLsJSONArray.put(joPagegeneratorEditLayoutURL);
	}

	/**
	 * Gets a link template for site navigation with an extra node in the list
	 * of urls for the page generator edit (one click to go from the menu to the
	 * journal article of interest).
	 * 
	 * @param linkTemplate
	 * @param pageContext
	 * @return
	 */
	public static String getCustomizedLinkTemplate(
		String linkTemplate, PageContext pageContext) {

		StringBuilder sb = new StringBuilder();

		final String liCloseTag = "</li>";
		int afterLastListElementClosingTagPos =
			linkTemplate.lastIndexOf(liCloseTag) + liCloseTag.length();

		String pageGeneratorListElementHtml =
			_instance.getPageGeneratorListElementHtml(pageContext);

		sb.append(linkTemplate);
		sb.insert(
			afterLastListElementClosingTagPos, pageGeneratorListElementHtml);

		String customizedLinkTemplate = sb.toString();

		return customizedLinkTemplate;
	}

	/**
	 * Recursively fix up each node of the navigation menu by adding the
	 * required information related to journal articles for the navigation items
	 * that have a journal article associated with them
	 * 
	 * @param request
	 * @param openNodes
	 * @param treeId
	 * @param groupId
	 * @param privateLayout
	 * @return
	 * @throws Exception
	 */
	public static String getIntesysLayoutsJsonStr(
		HttpServletRequest request, long[] openNodes, String treeId,
		long groupId, boolean privateLayout)
		throws Exception {

		String intesysLayoutsJson = LayoutsTreeUtil.getLayoutsJSON(
			request, groupId, privateLayout,
			LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, openNodes, true, treeId);
		JSONObject intesysLayoutsJsonObject =
			JSONFactoryUtil.createJSONObject(intesysLayoutsJson);

		JSONArray layoutsArr =
			_instance.getLayoutsArr(intesysLayoutsJsonObject);
		_instance.iterateLayoutsJsonArr(layoutsArr, groupId);

		String jsonStr = intesysLayoutsJsonObject.toString();

		return jsonStr;
	}

	public static void log(Exception e) {

		_log.info(e);
	}

	public static void log(String msg) {

		_log.info(msg);
	}

	private PageGeneratorLayoutTreeTagUtil() {
	}

	private PortletURL getPageGeneratorEditLayoutUrl(
		HttpServletRequest request, Long selPlid, boolean privateLayout,
		long groupId) {

		PortletURL pagegeneratorEditLayoutURL =
			PortalUtil.getControlPanelPortletURL(
				request, JournalPortletKeys.JOURNAL,
				PortletRequest.RENDER_PHASE);

		if (selPlid != null && selPlid >= LayoutConstants.DEFAULT_PLID) {
			pagegeneratorEditLayoutURL.setParameter(
				"selPlid", String.valueOf(selPlid));
		}

		if (privateLayout) {
			pagegeneratorEditLayoutURL.setParameter(
				"privateLayout", String.valueOf(privateLayout));
		}

		pagegeneratorEditLayoutURL.setParameter(
			"groupId", String.valueOf(groupId));

		pagegeneratorEditLayoutURL.setParameter("mvcPath", "/edit_article.jsp");
		pagegeneratorEditLayoutURL.setParameter(
			"searchContainerId", "articles");

		return pagegeneratorEditLayoutURL;
	}

	private String getPageGeneratorListElementHtml(PageContext pageContext) {

		String journalArticleParamStr = getJournalArticleParamStr();
		String hrefPlaceholder =
			"{" + _JSP_KEY_PAGEGENERATOR_URL + "}" + journalArticleParamStr;

		StringBuilder sb = new StringBuilder();

		ResourceBundle bundle =
			TagResourceBundleUtil.getResourceBundle(pageContext);

		String liferayConfigureMessage =
			LanguageUtil.get(bundle, _LANG_KEY_PAGEGENERATOR_CONFIGURE);
		String liferayConfigureMessageLabel =
			LanguageUtil.get(bundle, _LANG_KEY_PAGEGENERATOR_CONFIGURE_X);

		sb.append("<li class=\"").append(_CSS_CLASS_PAGEGENERATOR_LI).append(
			" ").append(
				wrapInCurlyBraces(_JSP_KEY_ARTICLE_LINK_CSS_CLASS)).append(
					" \">");
		sb.append("<a class=\"").append(_CSS_CLASS_PAGEGENERATOR_A).append(
			"\" data-deleteable=\"{deleteable}\" data-plid=\"{plid}\" data-qa-id=\"").append(
				_HTML_DATA_QA_ID_PAGEGENERATOR_A).append(
					"\" data-url=\"{url}\" data-uuid=\"{uuid}\" ");
		sb.append("href=\"").append(hrefPlaceholder).append("\" ");
		sb.append("id=\"{id}PagegeneratorEdit\">");
		sb.append("<span aria-hidden=\"true\">").append(
			liferayConfigureMessage).append("</span>");
		sb.append("<span class=\"sr-only\">").append(
			liferayConfigureMessageLabel).append("</span>");
		sb.append("</a>");
		sb.append("</li>");

		String html = sb.toString();

		return html;
	}

	private String getJournalArticleParamStr() {

		StringBuilder sb = new StringBuilder();

		sb.append(StringPool.AMPERSAND).append(
			_URL_PARAM_KEY_ARTICLE_ID).append(StringPool.EQUAL).append(
				wrapInCurlyBraces(_JSP_KEY_ARTICLE_ID));
		sb.append(StringPool.AMPERSAND).append(
			_URL_PARAM_KEY_ARTICLE_VERSION).append(StringPool.EQUAL).append(
				wrapInCurlyBraces(_JSP_KEY_ARTICLE_VERSION));
		sb.append(StringPool.AMPERSAND).append(
			_URL_PARAM_KEY_ARTICLE_FOLDER_ID).append(StringPool.EQUAL).append(
				wrapInCurlyBraces(_JSP_KEY_ARTICLE_FOLDER_ID));

		String journalArticleParamStr = sb.toString();

		return journalArticleParamStr;
	}

	private String wrapInCurlyBraces(String raw) {

		return StringPool.OPEN_CURLY_BRACE + raw + StringPool.CLOSE_CURLY_BRACE;
	}

	private void setLayoutLinkCustomProperties(
		JSONObject layoutJson, long groupId) {

		String currentLayoutUuid = layoutJson.getString(_JSP_KEY_ARTICLE_UUID);

		String articleId = null;
		double articleVersion = 0D;
		long articleFolderId = 0L;

		try {
			List<JournalArticle> currentLayoutArticles =
				JournalArticleServiceUtil.getArticlesByLayoutUuid(
					groupId, currentLayoutUuid);
			if (currentLayoutArticles != null &&
				currentLayoutArticles.size() > 0) {
				long resourcePrimKey =
					currentLayoutArticles.get(0).getResourcePrimKey();
				JournalArticle latestArticle =
					JournalArticleServiceUtil.getLatestArticle(resourcePrimKey);

				articleId = latestArticle.getArticleId();
				articleVersion = latestArticle.getVersion();
				articleFolderId = latestArticle.getFolderId();
			}
		}
		catch (final Exception e) {
			// suppress
			_log.info("Suppressed exception " + e.getMessage());
		}

		String articleLinkCssClass;

		if (articleId != null && isPageGenerated(layoutJson)) {
			articleLinkCssClass = _CSS_CLASS_VISIBLE;

			layoutJson.put(_JSP_KEY_ARTICLE_ID, articleId);
			layoutJson.put(_JSP_KEY_ARTICLE_VERSION, articleVersion);
			layoutJson.put(_JSP_KEY_ARTICLE_FOLDER_ID, articleFolderId);
		}
		else {
			articleLinkCssClass = _CSS_CLASS_HIDDEN;
		}

		layoutJson.put(_JSP_KEY_ARTICLE_LINK_CSS_CLASS, articleLinkCssClass);
		handleChildren(layoutJson, groupId);
	}

	private boolean isPageGenerated(JSONObject layoutJson) {

		boolean pageGenerated = false;

		try {
			Layout layout = LayoutLocalServiceUtil.getLayout(
				layoutJson.getLong(_JSP_KEY_PLID));

			ExpandoValue value = ExpandoValueLocalServiceUtil.getValue(
				layout.getCompanyId(), Layout.class.getName(),
				_EXPANDO_TABLE_NAME_LAYOUT, _EXPANDO_COLUMN_LAYOUT_UUID,
				layout.getPrimaryKey());

			if (value != null) {
				pageGenerated = true;
			}
		}
		catch (Exception e) {
			_log.info(
				"Could not determine if layout was generated with page generator, assuming false");
		}

		return pageGenerated;
	}

	private void handleChildren(JSONObject layoutJson, long groupId) {

		final String keyHasChildren = "hasChildren";

		if (layoutJson.getBoolean(keyHasChildren)) {
			final String keyChildren = "children";

			JSONObject children = layoutJson.getJSONObject(keyChildren);
			JSONArray childLayoutsArr = getLayoutsArr(children);
			iterateLayoutsJsonArr(childLayoutsArr, groupId);
		}
	}

	private void iterateLayoutsJsonArr(JSONArray layoutsArr, long groupId) {

		if (layoutsArr != null) {
			int numLayouts = layoutsArr.length();

			for (int layoutIndex = 0; layoutIndex < numLayouts; layoutIndex++) {
				JSONObject currentLayoutJson =
					layoutsArr.getJSONObject(layoutIndex);
				setLayoutLinkCustomProperties(currentLayoutJson, groupId);
			}
		}
	}

	private JSONArray getLayoutsArr(JSONObject jsonObject) {

		final String keyLayoutsArrNode = "layouts";
		JSONArray layoutsArr = jsonObject.getJSONArray(keyLayoutsArrNode);

		return layoutsArr;
	}

	private static final Log _log =
		LogFactoryUtil.getLog(PageGeneratorLayoutTreeTagUtil.class);

	private static final PageGeneratorLayoutTreeTagUtil _instance =
		new PageGeneratorLayoutTreeTagUtil();

	private static final String _CSS_CLASS_HIDDEN = "hidden";

	private static final String _CSS_CLASS_PAGEGENERATOR_A =
		"layout-tree-pagegenerator-special-link";

	// queste sono le classi usate per la creazione del link nel template custom
	private static final String _CSS_CLASS_PAGEGENERATOR_LI =
		"layout-tree-pagegenerator-special-item";

	private static final String _CSS_CLASS_VISIBLE = "visible";

	private static final String _EXPANDO_COLUMN_LAYOUT_UUID = "LAYOUT_UUID";

	// queste due costanti sono copiate da PageGeneratorExpandoServiceImpl in
	// pagegenerator-java
	private static final String _EXPANDO_TABLE_NAME_LAYOUT = "LAYOUT";

	// questo il valore dell'attributo data-qua-id (non so se serve o no)
	private static final String _HTML_DATA_QA_ID_PAGEGENERATOR_A =
		"pagegeneratorEditPage";

	private static final String _JOURNAL_NAMESPACE =
		"_" + JournalPortletKeys.JOURNAL + "_";

	private static final String _JSP_KEY_ARTICLE_FOLDER_ID = "articleFolderId";

	private static final String _JSP_KEY_ARTICLE_ID = "articleId";

	private static final String _JSP_KEY_ARTICLE_VERSION = "articleVersion";

	private static final String _JSP_KEY_ARTICLE_LINK_CSS_CLASS =
		"articleLinkCssClass";

	private static final String _JSP_KEY_ARTICLE_UUID = "uuid";

	// tutte queste chiavi in js sono membri di un oggetto (es oggetto.uuid =
	// "qualcosa") mentre quando sono presenti
	// nelle url generate, sono fra parentesi graffe, come per esempio
	// _namespace_PortletName_folderId={articleFolderId}
	// per le sostituzioni in modo che (per esempio, nel caso appena citato) si
	// ha una roba tipo
	// _namespage_PortletName_folderId=12345
	private static final String _JSP_KEY_PAGEGENERATOR_URL =
		"pagegeneratorEditLayoutURL";

	private static final String _JSP_KEY_PLID = "plid";

	// queste sono le chiavi per il file Language.properties
	private static final String _LANG_KEY_PAGEGENERATOR_CONFIGURE =
		"intesys-pagegenerator-configure";

	private static final String _LANG_KEY_PAGEGENERATOR_CONFIGURE_X =
		"intesys-pagegenerator-configure-x";

	// questi sono le chiavi per i parametri dell'url modifica contenuto
	private static final String _URL_PARAM_KEY_ARTICLE_ID =
		_JOURNAL_NAMESPACE + "articleId";

	private static final String _URL_PARAM_KEY_ARTICLE_VERSION =
		_JOURNAL_NAMESPACE + "version";

	private static final String _URL_PARAM_KEY_ARTICLE_FOLDER_ID =
		_JOURNAL_NAMESPACE + "folderId";
}
