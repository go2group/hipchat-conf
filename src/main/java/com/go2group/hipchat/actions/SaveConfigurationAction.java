package com.go2group.hipchat.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.xwork.RequireSecurityToken;
import com.go2group.hipchat.components.ConfigurationManager;
import com.opensymphony.xwork.Action;
import org.apache.commons.lang.StringUtils;

public class SaveConfigurationAction extends ConfluenceActionSupport {
    private ConfigurationManager configurationManager;
    private PermissionManager permissionManager;

    private String spaceKey;
    private String hipChatAuthToken;

    @Override
    public boolean isPermitted() {
        return permissionManager.isConfluenceAdministrator(getRemoteUser());
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setHipChatAuthToken(String value) {
        this.hipChatAuthToken = value;
    }

    @Override public void validate() {
        if (StringUtils.isBlank(hipChatAuthToken)) {
            addActionError(getText("hipchat.token.form.invalidtokenerror"));
        }
    }

    public String getHipChatAuthToken() {
        return configurationManager.getHipChatAuthToken();
    }

    @Override
    @RequireSecurityToken(true)
    public String execute() throws Exception {
        configurationManager.updateConfiguration(hipChatAuthToken);

        if(StringUtils.isNotBlank(spaceKey)) {
            return "redirect";
        }
        return Action.SUCCESS;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }
    // =================================================================================================================
    // We have to use setter injection if we don't use the defaultStack
    // See https://jira.atlassian.com/browse/CONF-23137
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }
}