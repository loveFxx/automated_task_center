package com.sailvan.dispatchcenter.common.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录类
 * @date 2021-04
 * @author menghui
 */
public class LoginDTO  implements Serializable {

    private static final long serialVersionUID = -5L;

    private Integer id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String username;

    private String password;

    private String rememberMe;

    private LocalDateTime lastLogin;

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
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

    public String getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(String rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "LoginDTO{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", rememberMe='" + rememberMe + '\'' +
                '}';
    }
}
