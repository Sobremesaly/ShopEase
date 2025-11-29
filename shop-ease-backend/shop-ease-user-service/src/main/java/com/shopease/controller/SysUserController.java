package com.shopease.controller;
import com.shopease.dto.LoginDTO;
import com.shopease.result.Result;
import com.shopease.service.SysUserService;
import com.shopease.vo.LoginVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

/**
 * @author hspcadmin
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    private final SysUserService sysUserService;

    // 构造器注入（不用@Autowired，更规范）
    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    // 登录接口（POST请求，前端传JSON参数）
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = sysUserService.login(loginDTO);
        return Result.success(loginVO);
    }
}
