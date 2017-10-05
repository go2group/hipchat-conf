package com.go2group.hipchat.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class ConfigurationManager {
    private static final String PLUGIN_STORAGE_KEY = "com.atlassian.labs.hipchat";
    private static final String HIPCHAT_AUTH_TOKEN_KEY = "hipchat-auth-token";

    private final PluginSettingsFactory pluginSettingsFactory;

    public ConfigurationManager(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public String getHipChatAuthToken() {
        return getValue(HIPCHAT_AUTH_TOKEN_KEY);
    }

    public List<String> getHipChatRoomList(String spaceKey) {
        return Arrays.asList(StringUtils.split(StringUtils.defaultIfEmpty(getValue(spaceKey), ""), ","));
    }
    
    public List<String> getEventsList(String spaceKey) {
        String eventList = StringUtils.defaultIfEmpty(getValue(spaceKey+".events"), "");
        return StringUtils.isEmpty(eventList) ? new ArrayList<String>() : Arrays.asList(StringUtils.split(eventList, ","));
    }

    public String getHipChatRooms(String spaceKey){
        return getValue(spaceKey);
    }

    private String getValue(String storageKey) {
        PluginSettings settings = pluginSettingsFactory.createSettingsForKey(PLUGIN_STORAGE_KEY);
        Object storedValue = settings.get(storageKey);
        return storedValue == null ? "" : storedValue.toString();
    }

    public void updateConfiguration(String authToken) {
        PluginSettings settings = pluginSettingsFactory.createSettingsForKey(PLUGIN_STORAGE_KEY);
        settings.put(HIPCHAT_AUTH_TOKEN_KEY, authToken);
    }

    public void setNotifyRooms(String spaceKey, String rooms) {
        PluginSettings settings = pluginSettingsFactory.createSettingsForKey(PLUGIN_STORAGE_KEY);
        settings.put(spaceKey, rooms);
    }
    
    public String getEvents(String spaceKey){
        return getValue(spaceKey+".events");
    }
    
    public void setEvents(String spaceKey, String events) {
    	System.out.println("Saving for "+spaceKey+", event:"+events);
        PluginSettings settings = pluginSettingsFactory.createSettingsForKey(PLUGIN_STORAGE_KEY);
        settings.put(spaceKey+".events", events);
    }


}