package com.go2group.hipchat.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAdminAction;
import com.go2group.hipchat.components.ConfigurationManager;
import com.go2group.hipchat.components.HipChatProxyClient;
import com.go2group.hipchat.utils.InvalidAuthTokenException;
import com.opensymphony.xwork.Action;

public class ViewSpaceConfigurationAction extends AbstractSpaceAdminAction
{
    private final HipChatProxyClient hipChatProxyClient;
    private final ConfigurationManager configurationManager;

    private String roomId;
    private String roomJson;
    private boolean successFullUpdate;
    
    private String[] events  = {"BlogPost created", "Page created", "Page updated with major changes", "Page updated with minor changes", "Page moved", "Comment added"};
    private List<String> selectedEvents;

    public ViewSpaceConfigurationAction(HipChatProxyClient hipChatProxyClient, ConfigurationManager configurationManager)
    {
        this.hipChatProxyClient = hipChatProxyClient;
        this.configurationManager = configurationManager;
    }

    public void setResult(String result) {
        if ("success".equals(result)) {
            successFullUpdate = true;
        }
    }
    
    @Override
    public String execute()
    {
        setRoomId(configurationManager.getHipChatRooms(key));
        if(StringUtils.isBlank(configurationManager.getHipChatAuthToken())) {
            return Action.INPUT;
        } else {
            try {
                setRoomJson(hipChatProxyClient.getRooms().toString());
                selectedEvents = configurationManager.getEventsList(key);
            } catch (InvalidAuthTokenException e) {
                return Action.ERROR;
            }
            return Action.SUCCESS;
        }
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
    
    public boolean isSelectedEvent(String event){
    	return selectedEvents.contains(event);
    }

	public String[] getEvents() {
		return events;
	}

	public void setEvents(String[] events) {
		this.events = events;
	}
}