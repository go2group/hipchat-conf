package com.go2group.hipchat.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.go2group.hipchat.components.HipChatProxyClient;
import com.opensymphony.xwork.Action;

public class ViewAnnouncementAction extends ConfluenceActionSupport {
	private final HipChatProxyClient hipChatProxyClient;

	private String roomId;
	private String roomJson;
	private boolean successFullUpdate;

	public ViewAnnouncementAction(HipChatProxyClient hipChatProxyClient) {
		this.hipChatProxyClient = hipChatProxyClient;
	}

	public void setResult(String result) {
		if ("success".equals(result)) {
			successFullUpdate = true;
		}
	}

	@Override
	public String execute() {
		setRoomId("");
		setRoomJson(hipChatProxyClient.getRooms().toString());
		return Action.SUCCESS;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getRoomId() {
		return roomId;
	}

	public String getRoomJson() {
		return roomJson;
	}

	public void setRoomJson(String roomJson) {
		this.roomJson = roomJson;
	}

	public boolean isSuccessFullUpdate() {
		return successFullUpdate;
	}
}