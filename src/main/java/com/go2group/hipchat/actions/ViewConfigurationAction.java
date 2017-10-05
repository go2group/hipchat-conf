package com.go2group.hipchat.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.go2group.hipchat.components.ConfigurationManager;
import com.opensymphony.xwork.Action;

public class ViewConfigurationAction extends ConfluenceActionSupport {


    private final ConfigurationManager configurationManager;
    private final PermissionManager permissionManager;
    private boolean successFullUpdate;

    public ViewConfigurationAction(ConfigurationManager configurationManager, PermissionManager permissionManager) {
        this.configurationManager = configurationManager;
        this.permissionManager = permissionManager;
    }

    public void setResult(String result) {
        if ("success".equals(result)) {
            successFullUpdate = true;
        }
    }

    @Override
    public boolean isPermitted() {
        return permissionManager.isConfluenceAdministrator(getRemoteUser());
    }

    public String getHipChatAuthToken() {
        return configurationManager.getHipChatAuthToken();
    }

    @Override
    public String execute() throws Exception {
        return Action.SUCCESS;
    }

    public boolean isSuccessFullUpdate() {
        return successFullUpdate;
    }
}