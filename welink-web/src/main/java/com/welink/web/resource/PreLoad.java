package com.welink.web.resource;

import com.welink.commons.persistence.ProfileDOMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by daniel on 14-9-22.
 */
@RestController
public class PreLoad {

    @Resource
    private ProfileDOMapper profileDOMapper;

    @RequestMapping(value = {"/1.0/preLoad.json", "/1.0/preLoad.htm"}, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String execute() throws Exception {
        if (null == profileDOMapper) {
            return "nonok";
        }
        return "ok";
    }

    public void setProfileDOMapper(ProfileDOMapper profileDOMapper) {
        this.profileDOMapper = profileDOMapper;
    }
}
