package com.sailvan.dispatchcenter.common.domain;

import java.io.Serializable;

/**
 *  token相关检测类
 * @date 2021-04
 * @author menghui
 */
public class TokenUser implements Serializable {

    private static final long serialVersionUID = -9L;

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String userName;

    private String passWord;

    private String rememberMe;

    private String lastLogin;

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
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
                "userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", rememberMe='" + rememberMe + '\'' +
                '}';
    }
}
