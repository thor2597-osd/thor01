package com.thor.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thor.dao.CommentDAO;
import com.thor.dao.ProductDetailDAO;
import com.thor.entity.CommentDO;
import com.thor.entity.ProductDetailDO;
import com.thor.entity.UserDO;
import com.thor.dao.UserDAO;
import com.thor.model.Result;
import com.thor.service.OssService;
import com.thor.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thor.util.JWTUtils;
import com.thor.util.RedisConstants;
import com.thor.util.RegexUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserDAO, UserDO> implements UserService {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisTemplate<String,Object> redisTemplate;

    private final UserDAO userDAO;

    private final OssService ossService;

    private final ProductDetailDAO productDetailDAO;

    private final CommentDAO commentDAO;

    @Override
    public Result login(String userName, String passWord, HttpServletRequest request) {
        Result result = new Result();
        // 先判断账号和密码是否为空
        if (StrUtil.isEmpty(userName) || StrUtil.isEmpty(passWord)) {
            result.setCode(400);
            result.setMessage("账号和密码不可以为空");

            return result;
        }
        // 获取请求标头的 Authorization 字段里面的 token 值
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && !authorizationHeader.isEmpty() && JWTUtils.verify(authorizationHeader)) {
            // 如果正确那就检查token是否快过期（有效期小于24小时）
            if (JWTUtils.isTokenExpiringSoon(authorizationHeader,24)) {
                // 过期了，返回新的token
                String token = refreshOrGetToken(userName);

                result.setCode(200);
                result.setMessage("登陆成功,token快过期了，刷新成功");
                result.setData(token);

                return result;
            }
            // 没过期，返回旧的token
            result.setCode(200);
            result.setMessage("登陆成功,token没有过期");
            result.setData(authorizationHeader);

            return result;
        }
        String password = encryption(passWord);
        // 如果token过期或者为空直接进行密码和账号验证
        // 数据库密码都是加过密的，需要加密后对比

        UserDO userDO = query()
                .eq("user_name", userName)
                .eq("pass_word", password)
                .one();
        // 如果账号或密码不正确则返回错误
        if (userDO == null) {
            result.setCode(400);
            result.setMessage("账号或密码不正确，请重新登录");

            return result;
        }
        // 如果正确那就生成一个全新的token，并且返回给前端
        String token = refreshOrGetToken(userName);

        // 登录用户的userId也需要传递给前端
        Map<String,Object> map = new HashMap<>();
        map.put("accessToken",token);
        userDO.setPassWord(password);
        map.put("user",userDO);

        result.setCode(200);
        result.setMessage("登陆成功");
        result.setData(map);

        return result;
    }

    @Override
    public Result register(String phone,String passWord,String email) {
        // 检查电话号码里面是否输入的空字符串
        if (StrUtil.isEmpty(phone) || !RegexUtil.matchPhone(phone)) {
            // 电话为空，那么就检测邮箱即可
            return checkEmail(email,passWord);
        } else if (StrUtil.isEmpty(email) || !RegexUtil.matchEmail(email)) {
            // 邮箱为空，则检测电话
            return checkPhone(phone,passWord);
        }
        // 如果两者格式都不对，返回错误。
        return Result.fail("请输入正确的电话或邮箱");
    }

    @Override
    public Result uploadPhoto(MultipartFile file, int userId) {
        Result result = new Result();
        try {
            // 上传文件到OSS
            String url = ossService.uploadPhoto(file);

            // 更新用户信息
            UserDO user = query().eq("id",userId).one();
            user.setAvatar(url);
            int success = userDAO.updateById(user);

            if (success > 0) {
                result.setCode(200);
                result.setMessage("照片上传成功");
                result.setData(url);
            } else {
                result.setCode(400);
                result.setMessage("图片上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(400);
            result.setMessage("Error:" + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public Result uploadInfo(UserDO user) {
        Result result = new Result();

        // 检查传入对象是否为空
        if (user == null) {
            result.setCode(400);
            result.setMessage("无法修改，用户为空");
            result.setTotal(0);

            return result;
        }
        // 获取用户
        UserDO userDO = getUserById(user.getId());

        if (userDO == null) {
            result.setCode(400);
            result.setMessage("不存在该用户");
            result.setTotal(0);

            return result;
        }

        return updateUserAndRedis(result,RedisConstants.USER_UPDATE + user.getId(),user);
    }

    public Result updateUserAndRedis(Result result,String userName,UserDO user){
        String passWord = user.getPassWord();
        // redis里面的密码同样需要加密
        // user.setPassWord(encryption(user.getPassWord()));
        // 更新数据库并要确保账号和电话一样，如果没有电话那就检查邮箱
        if (StrUtil.isEmpty(user.getPhone())) {
            user.setUserName(user.getEmail());
        } else {
            user.setUserName(user.getPhone());
        }
        // 收货地址更改
        userDAO.updateById(user);
        // 修改完之后再添加缓存即可
        // 先将用户序列化成字符串
        stringRedisTemplate.opsForValue().set(userName,JSONUtil.toJsonStr(user));
        // 设置有效期时间
        stringRedisTemplate.expire(userName,RedisConstants.USER_TIME, TimeUnit.HOURS);

        // redis里面的商品详情用户信息和评论用户信息都需要更改
        // 商品详情信息更改
        deleteRelatedUserInfo(user);

        user.setPassWord(passWord);
        result.setCode(200);
        result.setMessage("修改成功");
        result.setTotal(1);
        result.setData(user);

        return result;
    }

    // 注销
    private void deleteRelatedUserInfo(UserDO user) {
        // 获取用户发布的所有商品详情Id
        List<ProductDetailDO> productDetailDOS = productDetailDAO.selectByAuthorId(user.getId());
        // 删除redis里面的所有相关的商品详情信息
        productDetailDOS.forEach(productDetailDO -> {
            redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + productDetailDO.getId());
            // 删除redis商品详情相关的评论信息
            redisTemplate.delete(RedisConstants.COMMENT + productDetailDO.getId());
            // TODO 收藏等信息
        });
        // 还要删除自己在其他商品详情的评论
        QueryWrapper<CommentDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",user.getId());
        List<CommentDO> commentDOS = commentDAO.selectList(wrapper);
        // 删除redis里面的数据即可
        commentDOS.forEach(commentDO -> {
            // 删除该商品的所有评论即可
            redisTemplate.delete(RedisConstants.COMMENT + commentDO.getRefId());
        });
    }

    // 电话号码注册
    private Result checkPhone(String phone,String passWord){
        Result result = new Result();
        // 1. 检查手机号用户是否已存在。
        UserDO userDO1 = query().eq("phone", phone).one();
        if (userDO1 != null) {
            result.setCode(400);
            result.setMessage("该手机号已被注册，请更换手机号或切换邮箱注册");
            return result;
        }
        // 密码也需要验证
        if (StrUtil.isEmpty(passWord)) {
            result.setCode(400);
            result.setMessage("密码不可以为空");
            return result;
        }
        // 2. 手机号不存在则进行注册
        UserDO userDO = new UserDO();
        userDO.setPhone(phone);
        // 2.1 先将手机号作为账号使用，并对密码进行加密
        userDO.setUserName(phone);
        // 生成md5值
        String md5Pwd = encryption(passWord);
        userDO.setPassWord(md5Pwd);
        // 2.2 初始化用户数据
        userDO.register();
        // 3.将用户存储到数据库
        userDAO.insert(userDO);

        result.setCode(200);
        result.setMessage("注册成功");
        result.setData(userDO);

        return result;
    }

    // 邮箱注册
    private Result checkEmail(String email,String passWord){
        Result result = new Result();
        // 1. 检查手机号用户是否已存在。
        UserDO userDO1 = query().eq("email",email).one();
        if (userDO1 != null) {
            result.setCode(400);
            result.setMessage("该邮箱已被注册，请更换邮箱或切换手机号注册");
            return result;
        }
        // 密码也需要验证
        if (StrUtil.isEmpty(passWord)) {
            result.setCode(400);
            result.setMessage("密码不可以为空");
            return result;
        }
        // 2. 邮箱不存在则进行注册
        UserDO userDO = new UserDO();
        userDO.setEmail(email);
        // 2.1 先将邮箱作为账号使用，并对密码进行加密
        userDO.setUserName(email);
        // 生成md5值
        String md5Pwd = encryption(passWord);
        userDO.setPassWord(md5Pwd);
        // 2.2 初始化用户数据
        userDO.register();
        // 3.将用户存储到数据库
        userDAO.insert(userDO);

        result.setCode(200);
        result.setMessage("注册成功");
        result.setData(userDO);

        return result;
    }

    // 通过Id查看是否含有用户信息
    @Override
    public UserDO getUserById(long userId){
        // 先查询redis里面是否有
        String str = stringRedisTemplate.opsForValue().get(RedisConstants.USER_UPDATE + userId);
        if (!StrUtil.isEmpty(str)) {
            // redis里面不为空
          return JSONUtil.toBean(str, UserDO.class);
        }
        // 如果redis为空，那么查询数据库
        UserDO userDO = userDAO.selectById(userId);
        if (userDO == null) {
            // 数据库也不存在该数据
            return null;
        }
        // 如果数据库中存在那么存放到redis里面
        stringRedisTemplate.opsForValue().set(RedisConstants.USER_UPDATE + userId,JSONUtil.toJsonStr(userDO));
        // 刷新redis有效期时间
        stringRedisTemplate.expire(RedisConstants.USER_UPDATE + userId,RedisConstants.USER_TIME,TimeUnit.HOURS);

        return userDO;
    }

    // 登录注册时对密码md5加密
    public String encryption(String passWord){
        String saltPwd = passWord + "_stu2024";
        return DigestUtil.md5Hex(saltPwd).toUpperCase();
    }

    // 刷新以及获取token
    public String refreshOrGetToken(String userName){
        Map<String, String> map = new HashMap<>();
        map.put("userName", userName);
        return JWTUtils.getToken(map);
    }
}
