# Liferay SEO Helper Plugin

## Info

In web portal projects it is very important to accurately manage search engine page search and visibility. For SEO it is important to have simple, understandable URLs with a low number of parameters, that give a hierarchy to the natural flow of content. This Liferay plugin allows for administration and easy configuration of content URLs according to the “Google SEO Starter Guide” indications and offers some fundamental SEO functionality.

The plugin improves the content URL management by creating correlated pages that are uniquely associated to articles. Articles can be shown in generated pages by an asset publisher with “Set as the Default Asset Publisher for This Page” enabled.

It can be useful for automating page detail creation of frequent content like news, products and other structured content.

The plugin runs on Tomcat and Wildfly bundles for Liferay 7 ga 6.

## Build / deploy

- Build all modules in the seo-helper folder and deploy them to your container. If hot deploying with blade, navigate to the modules directory and run `blade deploy`

## Use

### Navigate to a site's Web Content

- Log in to Liferay's backoffice with your admin credentials.
- Navigate to the **Web Content** section of your site in the left-hand menu. For example, **Liferay > Content > Web Content**.

### Create new Web Content with an associated page (layout)

- In the **Web Content** view, click the **[ + ]** (plus sign) button in the bottom-right corner.
- In the pop-up menu that appears, click the **Basic Web Content** option, or another template of your choice if available.
- Fill out the necessary **Web Content** fields such as **Title**, **Summary**, etc., as required.
- In the **New Web Content** view, scroll down to the **Display Page** section and expand it by clicking on it if it is closed.
- Click the **Enable page generation** checkbox to enable page generation.
- Select the parent layout of the page to generate with the **Select the parent page** select box, whose options correspond with the available layouts.
- Click the **Publish** button (10). When the article is published, a new layout will also be created.

## Notes

- With the plugin installed, it is not possible to delete Web Content with a generated page if there are child layouts which have associated journal articles.
- If a generated page is deleted, the association is removed from its Web Content but the Web Content is not deleted. The same logic is used for all child pages of the generated page.