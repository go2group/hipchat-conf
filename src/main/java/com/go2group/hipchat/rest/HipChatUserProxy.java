package com.go2group.hipchat.rest;


import com.go2group.hipchat.components.HipChatProxyClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/user/show")
public class HipChatUserProxy
{
    private HipChatProxyClient client;

    public HipChatUserProxy(HipChatProxyClient client)
    {
        this.client = client;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUser(@QueryParam("user_id") String userId)
    {
        return Response.ok(client.getUser(userId).toString()).build();
    }

}