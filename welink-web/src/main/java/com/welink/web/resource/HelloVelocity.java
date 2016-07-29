package com.welink.web.resource;

import com.daniel.weixin.mp.api.WxMpService;
import com.welink.biz.common.model.BaseController;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by daniel on 15-4-24.
 */
@Controller//上传文件选择文件页面
public class HelloVelocity extends BaseController {

    @Resource
    private Env env;

    @Resource
    private WxMpService wxMpService;

    @RequestMapping(value = {"/h/helloVelocity.htm"}, produces = "text/html;charset=UTF-8")
//    @ResponseBody
    public String userStaticLogin(HttpServletRequest request, HttpServletResponse response,
                                  ModelMap model) throws Exception {

        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }

//        FileSystemResource resource = new FileSystemResource("public.key");
//        File file = resource.getFile();
//
//        BufferedReader reader = null;
//        try {
//            System.out.println("以行为单位读取文件内容，一次读一整行：");
//            reader = new BufferedReader(new FileReader(file));
//            String tempString = null;
//            int line = 1;
//            // 一次读入一行，直到读入null为文件结束
//            while ((tempString = reader.readLine()) != null) {
//                // 显示行号
//                System.out.println("-----------line " + line + ": " + tempString);
//                line++;
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (IOException e1) {
//                }
//            }
//        }


        if (true) {
//            response.sendRedirect(redirectUrl);
//            return null;

//            response.sendRedirect("/h/hello.htm?session=expire");
//            return null;
        }
        model.addAttribute("addr", "你好么");
        return "helloVelocity";
    }

}
