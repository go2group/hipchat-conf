package com.go2group.hipchat.components;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.SpaceContentEntityObject;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostCreateEvent;
import com.atlassian.confluence.event.events.content.comment.CommentEvent;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageMoveEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.TinyUrl;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.user.User;

public class AnnotatedListener implements DisposableBean, InitializingBean {
	private static final Logger log = LoggerFactory.getLogger(AnnotatedListener.class);

	private final WebResourceUrlProvider webResourceUrlProvider;
	private final EventPublisher eventPublisher;
	private final HipChatProxyClient hipChatProxyClient;
	private final ConfigurationManager configurationManager;
	private final PersonalInformationManager personalInformationManager;
	private final UserAccessor userAccessor;
	private final SpaceManager spaceManager;

	public AnnotatedListener(EventPublisher eventPublisher, ConfigurationManager configurationManager,
			PersonalInformationManager personalInformationManager, UserAccessor userAccessor,
			HipChatProxyClient hipChatProxyClient, WebResourceUrlProvider webResourceUrlProvider,
			SpaceManager spaceManager) {
		this.eventPublisher = eventPublisher;
		this.configurationManager = configurationManager;
		this.hipChatProxyClient = hipChatProxyClient;
		this.userAccessor = userAccessor;
		this.personalInformationManager = personalInformationManager;
		this.webResourceUrlProvider = webResourceUrlProvider;
		this.spaceManager = spaceManager;
	}

	@EventListener
	public void blogPostCreateEvent(BlogPostCreateEvent event) {
		final BlogPost blogPost = event.getBlogPost();
		String spaceKey = blogPost.getSpaceKey();
		if (isSelectedEvent("BlogPost created", spaceKey)) {
			final User user = userAccessor.getUser(blogPost.getCreatorName());

			String postLink = createLinkToBlogPost(blogPost);
			String spaceLink = createLinkToSpace(spaceKey);

			String chatMessage = String.format("%s&nbsp;%s - new blog post created in %s by %s", getIconUrl(),
					postLink, spaceLink, getPersonalSpaceUrl(user));

			List<String> roomsToNotify = configurationManager.getHipChatRoomList(spaceKey);

			for (String room : roomsToNotify) {
				hipChatProxyClient.notifyRoom(room, chatMessage, null);
			}
		}
	}

	private boolean isSelectedEvent(String event, String spaceKey) {
		List<String> eventsList = configurationManager.getEventsList(spaceKey);
		return eventsList.size() == 0 || eventsList.contains(event);
	}

	private String createLinkToSpace(String spaceKey) {
		String url = webResourceUrlProvider.getBaseUrl(UrlMode.ABSOLUTE) + "/display/" + spaceKey;
		return String.format("<a href=\"%s\">%s</a>", GeneralUtil.escapeForHtmlAttribute(url),
				GeneralUtil.escapeXMLCharacters(this.spaceManager.getSpace(spaceKey).getName()));
	}

	@EventListener
	public void pageCreateEvent(PageCreateEvent event) {
		Page newPage = event.getPage();
		String spaceKey = newPage.getSpaceKey();
		if (isSelectedEvent("Page created", spaceKey)) {
			final User user = userAccessor.getUser(newPage.getCreatorName());

			String postLink = createLinkToPage(newPage);
			String spaceLink = createLinkToSpace(spaceKey);

			String chatMessage = String.format("%s - new page created in %s by %s", postLink, spaceLink,
					getPersonalSpaceUrl(user));

			List<String> roomsToNotify = configurationManager.getHipChatRoomList(spaceKey);

			for (String room : roomsToNotify) {
				hipChatProxyClient.notifyRoom(room, chatMessage, null);
			}
		}
	}

	@EventListener
	public void pageUpdateEvent(PageUpdateEvent event) {
		Page page = event.getPage();
		String spaceKey = page.getSpaceKey();
		String eventName = event.isMinorEdit() ? "Page updated with minor changes" : "Page updated with major changes";
		if (isSelectedEvent(eventName, spaceKey)) {
			final User user = userAccessor.getUser(page.getLastModifierName());

			String postLink = createLinkToPage(page);
			String spaceLink = createLinkToSpace(spaceKey);

			String updateText = event.isMinorEdit() ? "minor change done" : "major change done";
			String chatMessage = String.format("%s - %s in %s by %s", postLink, updateText, spaceLink,
					getPersonalSpaceUrl(user));

			List<String> roomsToNotify = configurationManager.getHipChatRoomList(spaceKey);

			for (String room : roomsToNotify) {
				hipChatProxyClient.notifyRoom(room, chatMessage, null);
			}
		}
	}

	@EventListener
	public void pageMoveEvent(PageMoveEvent event) {
		Page page = event.getPage();
		final User user = event.getUser();

		String postLink = createLinkToPage(page);
		String updateText = "";
		if (event.isMovedSpace()) {
			updateText = "page moved from " + createLinkToSpace(event.getOldSpace().getKey()) + " to "
					+ createLinkToSpace(page.getSpace().getKey());
		} else {
			updateText = "page moved within " + createLinkToSpace(page.getSpaceKey());
		}
		String chatMessage = String.format("%s - %s by %s", postLink, updateText, getPersonalSpaceUrl(user));

		Set<String> uniqueRooms = new HashSet<String>();
		if (isSelectedEvent("Page moved", page.getSpaceKey())) {
			uniqueRooms.addAll(configurationManager.getHipChatRoomList(page.getSpaceKey()));
		}
		if (event.isMovedSpace() && isSelectedEvent("Page moved", event.getOldSpace().getKey())) {
			uniqueRooms.addAll(configurationManager.getHipChatRoomList(event.getOldSpace().getKey()));
		}

		for (String room : uniqueRooms) {
			hipChatProxyClient.notifyRoom(room, chatMessage, null);
		}
	}

	@EventListener
	public void commentEvent(CommentEvent event) {
		ContentEntityObject entity = event.getComment().getOwner();
		String spaceKey = getSpaceKey(entity);
		if (isSelectedEvent("Comment added", spaceKey)) {
			final User user = userAccessor.getUser(event.getComment().getCreatorName());

			if (spaceKey != null) {

				String postLink = createLinkToComment(event.getComment().getUrlPath());
				String spaceLink = createLinkToSpace(spaceKey);

				String chatMessage = String.format("%s on '%s' in %s by %s", postLink, entity.getTitle(), spaceLink,
						getPersonalSpaceUrl(user));

				List<String> roomsToNotify = configurationManager.getHipChatRoomList(spaceKey);

				for (String room : roomsToNotify) {
					hipChatProxyClient.notifyRoom(room, chatMessage, null);
				}
			}
		}
	}

	private String getSpaceKey(ContentEntityObject entity) {
		if (entity instanceof SpaceContentEntityObject) {
			SpaceContentEntityObject space = (SpaceContentEntityObject) entity;
			return space.getSpaceKey();
		}
		return null;
	}

	private String createLinkToComment(String urlPath) {
		String url = webResourceUrlProvider.getBaseUrl(UrlMode.ABSOLUTE) + urlPath;
		return String.format("<a href=\"%s\"><b>%s</b></a>", GeneralUtil.escapeForHtmlAttribute(url),
				GeneralUtil.escapeXMLCharacters("comment"));
	}

	private String createLinkToPage(Page page) {
		String title = page.getTitle();
		String url = webResourceUrlProvider.getBaseUrl(UrlMode.ABSOLUTE) + "/x/" + new TinyUrl(page).getIdentifier();
		return String.format("<a href=\"%s\"><b>%s</b></a>", GeneralUtil.escapeForHtmlAttribute(url),
				GeneralUtil.escapeXMLCharacters(title));
	}

	private String getPersonalSpaceUrl(User user) {
		if (null == user) {
			return "";
		}
		return String.format("<a href=\"%s/%s\">%s</a>", webResourceUrlProvider.getBaseUrl(UrlMode.ABSOLUTE),
				personalInformationManager.getPersonalInformation(user).getUrlPath(),
				GeneralUtil.escapeXml(user.getFullName()));
	}

	private String createLinkToBlogPost(BlogPost blogPost) {
		String title = blogPost.getTitle();
		String url = webResourceUrlProvider.getBaseUrl(UrlMode.ABSOLUTE) + "/x/"
				+ new TinyUrl(blogPost).getIdentifier();
		return String.format("<a href=\"%s\"><b>%s</b></a>", GeneralUtil.escapeForHtmlAttribute(url),
				GeneralUtil.escapeXMLCharacters(title));
	}

	private String getIconUrl() {
		return String.format("<img src=\"%s/images/icons/blogentry_16.gif\" width=16 height=16 />",
				webResourceUrlProvider.getBaseUrl(UrlMode.ABSOLUTE));
	}

	// Unregister the listener if the plugin is uninstalled or disabled.
	@Override
	public void destroy() throws Exception {
		log.debug("Unregister blog created event listener");
		eventPublisher.unregister(this);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.debug("Register blog created event listener");
		eventPublisher.register(this);
	}
}