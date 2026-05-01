package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code list_users} API command — lists all users in the directory.
 *
 * <pre>{@code
 * client.api(new ListUsersCommand());
 * client.api(new ListUsersCommand().domain("domain.com"));
 * client.api(new ListUsersCommand().domain("domain.com").group("default"));
 * }</pre>
 */
public final class ListUsersCommand implements EslApiCommand {

    private String domain;
    private String group;
    private String user;
    private String context;

    public ListUsersCommand domain(String domain)   { this.domain  = domain;  return this; }
    public ListUsersCommand group(String group)     { this.group   = group;   return this; }
    public ListUsersCommand user(String user)       { this.user    = user;    return this; }
    public ListUsersCommand context(String context) { this.context = context; return this; }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder("list_users");
        if (domain  != null) sb.append(" domain=").append(domain);
        if (group   != null) sb.append(" group=").append(group);
        if (user    != null) sb.append(" user=").append(user);
        if (context != null) sb.append(" context=").append(context);
        return sb.toString();
    }
}
