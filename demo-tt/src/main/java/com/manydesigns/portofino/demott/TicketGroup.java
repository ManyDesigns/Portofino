package com.manydesigns.portofino.demott;

/**
 * Created by IntelliJ IDEA.
 * User: predo
 * Date: 10/30/13
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class TicketGroup {
    final String groupName;
    final String url;
    final int groupCount;

    public TicketGroup(String groupName, String url, int groupCount) {
        this.groupName = groupName;
        this.url = url;
        this.groupCount = groupCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getUrl() {
        return url;
    }

    public int getGroupCount() {
        return groupCount;
    }
}
