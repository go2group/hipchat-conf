package com.go2group.hipchat.actions;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.xwork.RequireSecurityToken;
import com.go2group.hipchat.components.ConfigurationManager;
import com.google.common.base.Joiner;
import com.opensymphony.xwork.Action;

public class SaveSpaceConfigurationAction extends ConfluenceActionSupport {
	private ConfigurationManager configurationManager;
	private SpaceManager spaceManager;

	private String key;
	private String roomId;
	private String[] selectedEvents;

	@Override
	public void validate() {
		super.validate();

		if (StringUtils.isBlank(key) || spaceManager.getSpace(key) == null) {
			addActionError(getText("hipchat.spaceconfig.spacekeyerror"));
		}
	}

	@Override
	public boolean isPermitted() {
		return spacePermissionManager.hasPermissionForSpace(getRemoteUser(),
				Arrays.asList(SpacePermission.ADMINISTER_SPACE_PERMISSION), spaceManager.getSpace(key));
	}

	@Override
	@RequireSecurityToken(true)
	public String execute() throws Exception {
		String events = selectedEvents != null ? Joiner.on(",").join(selectedEvents) : "";
		configurationManager.setEvents(key, events);
		configurationManager.setNotifyRooms(key, roomId);
		return Action.SUCCESS;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	// =================================================================================================================
	// We have to use setter injection if we don't use the defaultStack
	// See https://jira.atlassian.com/browse/CONF-23137
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	public void setSpaceManager(SpaceManager spaceManager) {
		this.spaceManager = spaceManager;
	}

	public String[] getSelectedEvents() {
		return selectedEvents;
	}

	public void setSelectedEvents(String[] selectedEvents) {
		this.selectedEvents = selectedEvents;
	}
}