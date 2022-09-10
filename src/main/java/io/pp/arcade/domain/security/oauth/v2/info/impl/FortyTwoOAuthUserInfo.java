package io.pp.arcade.domain.security.oauth.v2.info.impl;


import io.pp.arcade.domain.security.oauth.v2.info.OAuthUserInfo;

import java.util.Map;

public class FortyTwoOAuthUserInfo extends OAuthUserInfo {
    public FortyTwoOAuthUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }
    @Override
    public String getIntraId() {
        return attributes.get("login").toString();
    }

    public String getEmail() {
        return attributes.get("email").toString();
    }

    public String getImageUrl() { return attributes.get("image_url").toString(); }
}
