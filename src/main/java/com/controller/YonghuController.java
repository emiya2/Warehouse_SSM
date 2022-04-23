package com.controller;


import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;

import com.utils.GraphicHelper;
import org.springframework.beans.BeanUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.service.TokenService;
import com.utils.StringUtil;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.YonghuEntity;

import com.service.YonghuService;
import com.entity.view.YonghuView;

import com.utils.PageUtils;
import com.utils.R;

/**
 * 用户
 * 后端接口
 * @author
 * @email
 * @date 2021-04-15
*/
@RestController
@Controller
@RequestMapping("/yonghu")
public class YonghuController {
    private static final Logger logger = LoggerFactory.getLogger(YonghuController.class);

    @Autowired
    private YonghuService yonghuService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;



    //级联表service


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        params.put("orderBy","id");
        PageUtils page = yonghuService.queryPage(params);

        //字典表数据转换
        List<YonghuView> list =(List<YonghuView>)page.getList();
        for(YonghuView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        YonghuEntity yonghu = yonghuService.selectById(id);
        if(yonghu !=null){
            //entity转view
            YonghuView view = new YonghuView();
            BeanUtils.copyProperties( yonghu , view );//把实体数据重构到view中

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody YonghuEntity yonghu, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,yonghu:{}",this.getClass().getName(),yonghu.toString());
        Wrapper<YonghuEntity> queryWrapper = new EntityWrapper<YonghuEntity>()
            .eq("username", yonghu.getUsername())
            .or()
            .eq("yonghu_phone", yonghu.getYonghuPhone())
            .or()
            .eq("yonghu_id_number", yonghu.getYonghuIdNumber());
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YonghuEntity yonghuEntity = yonghuService.selectOne(queryWrapper);
        if(yonghuEntity==null){
            yonghu.setCreateTime(new Date());
            yonghu.setPassword("123456");
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      yonghu.set
        //  }
            yonghuService.insert(yonghu);
            return R.ok();
        }else {
            return R.error(511,"账户或者身份证号或者手机号已经被使用");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody YonghuEntity yonghu, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,yonghu:{}",this.getClass().getName(),yonghu.toString());
        //根据字段查询是否有相同数据
        Wrapper<YonghuEntity> queryWrapper = new EntityWrapper<YonghuEntity>()
            .notIn("id",yonghu.getId())
            .andNew()
            .eq("username", yonghu.getUsername())
            .or()
            .eq("yonghu_phone", yonghu.getYonghuPhone())
            .or()
            .eq("yonghu_id_number", yonghu.getYonghuIdNumber());
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YonghuEntity yonghuEntity = yonghuService.selectOne(queryWrapper);
        if("".equals(yonghu.getYonghuPhoto()) || "null".equals(yonghu.getYonghuPhoto())){
                yonghu.setYonghuPhoto(null);
        }
        if(yonghuEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      yonghu.set
            //  }
            yonghuService.updateById(yonghu);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"账户或者身份证号或者手机号已经被使用");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        yonghuService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
    * 登录
    */
    @IgnoreAuth
    @RequestMapping(value = "/login")
    public R login(String username, String password, String captcha, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String inputCode=(String) request.getParameter("verifyCode");

        String verifyCode= (String)request.getSession().getAttribute("verifyCode");
        if((inputCode == "") || !inputCode.equalsIgnoreCase(verifyCode)){
            System.out.println("验证码输入错误");
            return R.error("验证码输入错误");
        }

        YonghuEntity yonghu = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("username", username));
        if(yonghu==null || !yonghu.getPassword().equals(password)) {
            return R.error("账号或密码不正确");
        }
        //  // 获取监听器中的字典表
        // ServletContext servletContext = ContextLoader.getCurrentWebApplicationContext().getServletContext();
        // Map<String, Map<Integer, String>> dictionaryMap= (Map<String, Map<Integer, String>>) servletContext.getAttribute("dictionaryMap");
        // Map<Integer, String> role_types = dictionaryMap.get("role_types");
        // role_types.get(yonghu.getRoleTypes());
        String token = tokenService.generateToken(yonghu.getId(),username, "yonghu", "用户");
        R r = R.ok();
        r.put("token", token);
        r.put("role","用户");
        r.put("username",yonghu.getYonghuName());
        r.put("tableName","yonghu");
        r.put("userId",yonghu.getId());
        return r;
    }

    /**
    * 注册
    */
    @IgnoreAuth
    @PostMapping(value = "/register")
    public R register(@RequestBody YonghuEntity yonghu){
    //    	ValidatorUtils.validateEntity(user);
        if(yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("username", yonghu.getUsername()).orNew().eq("yonghu_phone",yonghu.getYonghuPhone()).orNew().eq("yonghu_id_number",yonghu.getYonghuIdNumber())) !=null) {
            return R.error("账户已存在或手机号或身份证号已经被使用");
        }
        yonghuService.insert(yonghu);
        return R.ok();
    }

    /**
     * 重置密码
     */
    @GetMapping(value = "/resetPassword")
    public R resetPassword(Integer  id){
        YonghuEntity yonghu = new YonghuEntity();
        yonghu.setPassword("123456");
        yonghu.setId(id);
        yonghuService.updateById(yonghu);
        return R.ok();
    }

    /**
    * 获取用户的session用户信息
    */
    @RequestMapping("/session")
    public R getCurrYonghu(HttpServletRequest request){
        Integer id = (Integer)request.getSession().getAttribute("userId");
        YonghuEntity yonghu = yonghuService.selectById(id);
        return R.ok().put("data", yonghu);
    }


    /**
    * 退出
    */
    @GetMapping(value = "logout")
    public R logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return R.ok("退出成功");
    }

   /* *//*固定页面大小，以及处理验证码的使用场景，捕获页面生成的验证码*//*
    @IgnoreAuth
    @RequestMapping("/verify")
    public R createVerifyCode(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获得 当前请求 对应的 会话对象
        HttpSession session = request.getSession();
        if(session.getAttribute("verifyCode") !=null || session.getAttribute("verifyCode") !=""){
            session.removeAttribute("verifyCode");
            System.out.println("清除已存在的验证码");
        }
        // 从请求中获得 URI ( 统一资源标识符 )
        String uri = request.getRequestURI();

        System.out.println("hello : " + uri);
        final int width = 180; // 图片宽度
        final int height = 40; // 图片高度
        final String imgType = "jpeg"; // 指定图片格式 (不是指MIME类型)
        final OutputStream output = response.getOutputStream(); // 获得可以向客户端返回图片的输出流
        // (字节流)
        // 创建验证码图片并返回图片上的字符串
        String code = GraphicHelper.create(width, height, imgType, output);
        System.out.println("验证码内容: " + code);
        // 建立 uri 和 相应的 验证码 的关联 ( 存储到当前会话对象的属性中 )
        session.setAttribute("verifyCode", code);
        if (session.getAttribute("verifyCode") != null || session.getAttribute("verifyCode") != "null") {
            System.out.println("页面生成的验证码为" + session.getAttribute("verifyCode"));
            return R.ok();
        }
        return R.error("页面验证码不存在");
    }
*/
}

