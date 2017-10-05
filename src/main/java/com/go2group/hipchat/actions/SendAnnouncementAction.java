package com.go2group.hipchat.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.xwork.RequireSecurityToken;
import com.go2group.hipchat.components.ConfigurationManager;
import com.go2group.hipchat.components.HipChatProxyClient;
import com.go2group.hipchat.components.HipChatProxyClient.JSONString;
import com.opensymphony.xwork.Action;

public class SendAnnouncementAction extends ConfluenceActionSupport {
	private static Logger log = Logger.getLogger(SendAnnouncementAction.class);

	private HipChatProxyClient hipChatProxyClient;
	private SpaceManager spaceManager;
	private ConfigurationManager configurationManager;
	private PermissionManager permissionManager;
	
	public SendAnnouncementAction() {
		// TODO Auto-generated constructor stub
	}

	public void setHipChatProxyClient(HipChatProxyClient hipChatProxyClient) {
		this.hipChatProxyClient = hipChatProxyClient;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	
	public void setSpaceManager(SpaceManager spaceManager) {
		this.spaceManager = spaceManager;
	}
	
	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	private String roomId;
	private String message;
	private String color;
	private String roomOption;

	@Override
	public boolean isPermitted() {
		return permissionManager.isConfluenceAdministrator(getRemoteUser());
	}

	@Override
	@RequireSecurityToken(true)
	public String execute() throws Exception {
		if ("all".equals(roomOption)) {
			JSONString roomString = hipChatProxyClient.getRooms();
			JSONObject rooms;
			try {
				rooms = new JSONObject(roomString.toString());
				if (rooms != null) {
					JSONArray roomArray = rooms.getJSONArray("rooms");
					for (int i = 0; i < roomArray.length(); i++) {
						JSONObject room = roomArray.getJSONObject(i);
						this.hipChatProxyClient.notifyRoom(room.getString("room_id"), message, color);
					}
				}
			} catch (JSONException e) {
				log.error("Error parsing room json:" + roomString.toString(), e);
				e.printStackTrace();
			}
		} else if ("subscribed".equals(roomOption)) {
			Set<String> rooms = new HashSet<String>();
			List<Space> spaces = this.spaceManager.getAllSpaces();
			for (Space space : spaces) {
				rooms.addAll(this.configurationManager.getHipChatRoomList(space.getKey()));
			}
			for (String room : rooms) {
				this.hipChatProxyClient.notifyRoom(room, message, color);
			}
		} else {
			List<String> rooms = Arrays.asList(StringUtils.split(StringUtils.defaultIfEmpty(roomId, ""), ","));
			for (String room : rooms) {
				this.hipChatProxyClient.notifyRoom(room, message, color);
			}
		}
		return Action.SUCCESS;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getRoomOption() {
		return roomOption;
	}

	public void setRoomOption(String roomOption) {
		this.roomOption = roomOption;
	}
}