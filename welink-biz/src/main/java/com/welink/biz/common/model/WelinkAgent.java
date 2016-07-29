package com.welink.biz.common.model;

/**
 * Created by daniel on 14-10-30.
 */
public class WelinkAgent {

    private String scale;

    private String mode;

    private String appbundle;

    private String systemVersion;

    private String version;

    private String buildVersion;

    private String platform;

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAppbundle() {
        return appbundle;
    }

    public void setAppbundle(String appbundle) {
        this.appbundle = appbundle;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }
}
