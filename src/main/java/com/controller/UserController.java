package com.controller;


import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.service.UserService;
import javafx.scene.control.Alert;
import org.eclipse.jdt.internal.compiler.codegen.VerificationTypeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.UserEntity;
import com.service.TokenService;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.GraphicHelper;


/**
 * 登录相关
 */
@RequestMapping("users")
@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private TokenService tokenService;

	/**
	 * 登录
	 */
	@IgnoreAuth
	@PostMapping(value = "/login")
	public R login(String username, String password, String captcha, HttpServletRequest request ,HttpServletResponse response) throws ServletException, IOException {
		String inputCode=(String) request.getParameter("verifyCode");

		String verifyCode= (String)request.getSession().getAttribute("verifyCode");
		if((inputCode == "") || !inputCode.equalsIgnoreCase(verifyCode)){
			System.out.println("验证码输入错误");
			return R.error("验证码输入错误");
		}
		UserEntity user = userService.selectOne(new EntityWrapper<UserEntity>().eq("username", username));
		if (user == null || !user.getPassword().equals(password)) {
			return R.error("账号或密码不正确");
		}
		String token = tokenService.generateToken(user.getId(), username, "users", user.getRole());
		R r = R.ok();
		r.put("token", token);
		r.put("role", user.getRole());
		r.put("userId", user.getId());
		return r;
	}

	/**
	 * 注册
	 */
	@IgnoreAuth
	@PostMapping(value = "/register")
	public R register(@RequestBody UserEntity user) {
//    	ValidatorUtils.validateEntity(user);
		if (userService.selectOne(new EntityWrapper<UserEntity>().eq("username", user.getUsername())) != null) {
			return R.error("用户已存在");
		}
		userService.insert(user);
		return R.ok();
	}

	/**
	 * 退出
	 */
	@GetMapping(value = "/logout")
	public R logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return R.ok("退出成功");
	}

	/**
	 * 密码重置
	 */
	@IgnoreAuth
	@RequestMapping(value = "/resetPass")
	public R resetPass(String username, HttpServletRequest request) {
		UserEntity user = userService.selectOne(new EntityWrapper<UserEntity>().eq("username", username));
		if (user == null) {
			return R.error("账号不存在");
		}
		user.setPassword("123456");
		userService.update(user, null);
		return R.ok("密码已重置为：123456");
	}

	/**
	 * 列表
	 */
	@RequestMapping("/page")
	public R page(@RequestParam Map<String, Object> params, UserEntity user) {
		EntityWrapper<UserEntity> ew = new EntityWrapper<UserEntity>();
		PageUtils page = userService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.allLike(ew, user), params), params));
		return R.ok().put("data", page);
	}

	/**
	 * 列表
	 */
	@RequestMapping("/list")
	public R list(UserEntity user) {
		EntityWrapper<UserEntity> ew = new EntityWrapper<UserEntity>();
		ew.allEq(MPUtil.allEQMapPre(user, "user"));
		return R.ok().put("data", userService.selectListView(ew));
	}

	/**
	 * 信息
	 */
	@RequestMapping("/info/{id}")
	public R info(@PathVariable("id") String id) {
		UserEntity user = userService.selectById(id);
		return R.ok().put("data", user);
	}

	/**
	 * 获取用户的session用户信息
	 */
	@RequestMapping("/session")
	public R getCurrUser(HttpServletRequest request) {
		Integer id = (Integer) request.getSession().getAttribute("userId");
		UserEntity user = userService.selectById(id);
		return R.ok().put("data", user);
	}

	/**
	 * 保存
	 */
	@PostMapping("/save")
	public R save(@RequestBody UserEntity user) {
//    	ValidatorUtils.validateEntity(user);
		if (userService.selectOne(new EntityWrapper<UserEntity>().eq("username", user.getUsername())) != null) {
			return R.error("用户已存在");
		}
		user.setPassword("123456");
		userService.insert(user);
		return R.ok();
	}

	/**
	 * 修改
	 */
	@RequestMapping("/update")
	public R update(@RequestBody UserEntity user) {
//        ValidatorUtils.validateEntity(user);
		userService.updateById(user);//全部更新
		return R.ok();
	}

	/**
	 * 删除
	 */
	@RequestMapping("/delete")
	public R delete(@RequestBody Long[] ids) {
		userService.deleteBatchIds(Arrays.asList(ids));
		return R.ok();
	}

	/*固定页面大小，以及处理验证码的使用场景，捕获页面生成的验证码*//*
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
	}*/

}
