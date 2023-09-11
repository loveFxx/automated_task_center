package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

/**
 * 认证用户
 * @date 2021-04
 * @author menghui
 */
@Data
public class UserInfo {
	
    private String id;
    private String username;
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
