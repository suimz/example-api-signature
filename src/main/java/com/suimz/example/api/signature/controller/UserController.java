package com.suimz.example.api.signature.controller;

import com.suimz.example.api.signature.bean.AddUserRequest;
import com.suimz.example.api.signature.bean.ApiResult;
import com.suimz.example.api.signature.bean.User;
import com.suimz.example.api.signature.sign.aop.ApiSignValid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 示例代码
 * 用户接口 - 模拟数据
 */
@RestController
@RequestMapping("/user")
public class UserController {
    // 用户数据 - 模拟数据存储
    private final Map<Integer, User> users = new HashMap<>();
    private int increment = 0;

    @GetMapping("/hello")
    public ApiResult<String> hello() {
        return ApiResult.success("hello");
    }

    /**
     * 添加用户
     * @param request
     * @return
     */
    @ApiSignValid
    @PostMapping
    public ApiResult<User> add(@RequestBody AddUserRequest request) {
        int uid = ++increment;
        User user = new User();
        user.setUid(uid);
        BeanUtils.copyProperties(request, user);
        // 模拟入库
        users.put(uid, user);
        return ApiResult.success(user);
    }

    /**
     * 读取用户
     * @param uid
     * @return
     */
    @ApiSignValid
    @GetMapping("/{uid}")
    public ApiResult get(@PathVariable Integer uid) {
        User user = users.get(uid);
        if (user == null) return ApiResult.fail("404", "not found user");
        return ApiResult.success(user);
    }

}
