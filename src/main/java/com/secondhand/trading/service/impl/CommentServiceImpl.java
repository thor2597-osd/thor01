package com.secondhand.trading.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.secondhand.trading.entity.CommentDO;
import com.secondhand.trading.dao.CommentDAO;
import com.secondhand.trading.entity.UserDO;
import com.secondhand.trading.model.Comment;
import com.secondhand.trading.model.ProductDetail;
import com.secondhand.trading.model.Result;
import com.secondhand.trading.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.trading.service.ProductDetailService;
import com.secondhand.trading.service.SseService;
import com.secondhand.trading.service.UserService;
import com.secondhand.trading.util.MQConstants;
import com.secondhand.trading.util.RedisConstants;
import com.secondhand.trading.model.CommentUser;
import com.secondhand.trading.util.WebSocketServer;
import lombok.AllArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
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
public class CommentServiceImpl extends ServiceImpl<CommentDAO, CommentDO> implements CommentService {

    private final CommentDAO commentDAO;

    private final RedisTemplate<String,Object> redisTemplate;

    private final StringRedisTemplate stringRedisTemplate;

    private final ProductDetailService productDetailService;

    private final RabbitTemplate rabbitTemplate;

    private final SseService sseService;
    @Override // 发布评论
    public Result post(String refId, CommentUser user, long parentId, String content) {
        Result result = new Result();
        if (StrUtil.isEmpty(refId)|| user == null || StrUtil.isEmpty(content)){
            result.setCode(400);
            result.setMessage("refId,user,content不能为空");
            return result;
        }
        // 防止XSS攻击
        String body = StringEscapeUtils.escapeHtml4(content);

        CommentDO commentDO = new CommentDO();
        commentDO.setUserId(user.getId());
        commentDO.setRefId(refId);
        commentDO.setParentId(parentId);
        commentDO.setContent(body);
        commentDO.setGmtCreated(LocalDateTime.now());
        commentDO.setGmtModified(LocalDateTime.now());

        // 数据存放至数据库并获取主键
        commentDAO.insert(commentDO);

        Comment comment = commentDO.toModel();
        comment.setAuthor(user);
        // 商品详情里面的评论数量也需要增加
        // 先删除缓存
        Object object = redisTemplate.opsForValue().get(RedisConstants.PRODUCT_DETAIL + refId);
        String str1 = JSONUtil.toJsonStr(object);
        ProductDetail productDetail = JSONUtil.toBean(str1, ProductDetail.class);

        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + refId);
        // 修改数据库
        productDetail.setCommentNumber(productDetail.getCommentNumber() + 1);
        // 修改redis里面的评论数据
        if (parentId != 0) {
            // 从redis里面查询，
            String str = JSONUtil.toJsonStr(redisTemplate.opsForHash().get(RedisConstants.COMMENT + refId, String.valueOf(commentDO.getParentId())));
            if (StrUtil.isEmpty(str)) {
                // 那就说明评论过期了，得查询数据库了。(这种情况不会出现，一般都是先查询得到了商品的评论，才会发表评论)
                return Result.fail("该评论已不存在，请重新进入商品查看");
            }
            // 再将String转换为Comment类。
            Comment commentFather = JSONUtil.toBean(str, Comment.class);
            // 子级评论加一
            List<Comment> children = commentFather.getChildren();
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(comment);
            commentFather.setChildren(children);

            // 只需要添加父级评论即可，平台只要显示一级评论
            redisTemplate.opsForHash().put(RedisConstants.COMMENT + refId,String.valueOf(commentFather.getId()),commentFather);
        } else {
            // 如果只是一级评论，那么直接存储即可
            // 将评论添加在redis里面
            redisTemplate.opsForHash().put(RedisConstants.COMMENT + refId, String.valueOf(comment.getId()), comment);
        }
        // redis数据设置有效期时间
        redisTemplate.expire(RedisConstants.COMMENT + refId,RedisConstants.COMMENT_TIME,TimeUnit.MINUTES);

        // 另外，发布商品的redis相关数据也要删除
        redisTemplate.delete(RedisConstants.LISTING + user.getId());
        // 收藏的redis相关数据也要删除
        redisTemplate.delete(RedisConstants.COLLECTION + user.getId());
        // 浏览的redis相关数据也要删除
        redisTemplate.delete(RedisConstants.VIEW + user.getId());

        // 最后，发送评论信息到队列
        rabbitTemplate.convertAndSend(MQConstants.commentQueue,JSONUtil.toJsonStr(commentDO));

        result.setCode(200);
        result.setMessage("发表成功");
        result.setData(comment);
        return result;
    }

    @RabbitListener(queues = MQConstants.commentQueue)
    public void sendMessage(String comment){
        // 先进行反序列化
        CommentDO commentDO = JSONUtil.toBean(comment, CommentDO.class);
        // 先获取商品详情
        ProductDetail details = productDetailService.getProductDetailsById(Long.parseLong(commentDO.getRefId()));
        // redis里面的未读消息数也要加1
        stringRedisTemplate.opsForValue().increment(RedisConstants.UNREAD + details.getAuthor().getId());
        // 然后获取商家ID并发送未读消息给商家客户端
        sseService.sendUnreadCount(String.valueOf(details.getAuthor().getId()));
    }

    @Override
    public Result query(String refId) {
        Result result = new Result();

        String commentName = RedisConstants.COMMENT + refId;
        // 先查询redis里面有没有对应商品详情的数据
        Map<Object, Object> map = redisTemplate.opsForHash().entries(commentName);
        if (!map.isEmpty()) {
            // 刷新评论有效期时间，这个时间应当与商品时间一致
            redisTemplate.expire(commentName,RedisConstants.COMMENT_TIME,TimeUnit.MINUTES);
            // 将map转换为list数据，方便前端查看。
            List<Object> comments = new ArrayList<>();
            map.forEach((key,value) -> {
                comments.add(value);
            });
            result.setCode(200);
            result.setData(comments);
            result.setMessage("查询成功");

            return result;
        }
        // 查询数据库并存储到redis
        List<Comment> data = SelectAndInsertRedis(commentName, map, refId);

        result.setCode(200);
        result.setData(data);
        result.setMessage("查询成功");
        return result;
    }

    // 删除单个评论
    @Override
    public Result delete(Comment comment,String refId) {
        Result result = new Result();
        // 判断Id
        if (comment == null) {
            return Result.fail("该评论已经不存在");
        }
        String commentName = RedisConstants.COMMENT + refId;
        // 先删除该商品详情下在缓存里面的所有评论
        redisTemplate.delete(commentName);
        // 采用条件查询器
        QueryWrapper<CommentDO> queryWrapper = new QueryWrapper<>();
        // 然后查询是否有子级评论
        int delete = 0;
        if (comment.getChildren() != null) {
            // 父级评论删除那么子级评论也要删除
            queryWrapper.eq("parent_id",comment.getId());
            delete = commentDAO.delete(queryWrapper);
        }
        // 删除该评论即可
        commentDAO.deleteById(comment.toConvert());
        // 商品详情的评论数量也要删除
        productDetailService.updateCommentToProductDetail(Long.parseLong(refId),-(delete + 1),0);

        Map<Object,Object> map = new HashMap<>();
        // 查询数据库并存储到redis
        List<Comment> comments = SelectAndInsertRedis(commentName, map, refId);

        // 另外，发布商品的redis相关数据也要删除
        redisTemplate.delete(RedisConstants.LISTING + comment.getAuthor().getId());
        // 收藏的redis相关数据也要删除
        redisTemplate.delete(RedisConstants.COLLECTION + comment.getAuthor().getId());
        // 浏览的redis相关数据也要删除
        redisTemplate.delete(RedisConstants.VIEW + comment.getAuthor().getId());

        result.setCode(200);
        result.setData(comments);
        result.setMessage("查询成功");
        return result;
    }

    private List<Comment> querySQL(String refId) {
        //查询所有的评论记录包含回复的
        List<Comment> comments = commentDAO.findByRefId(refId);
        // 如果数据库里面也没有评论，那么就返回Null
        if (comments == null) {
            return null;
        }
        //构建map结构
        Map<Long,Comment> commentMap = new HashMap<>();
        //初始化一个虚拟根节点，0可以对应的是所有一级评论的父亲
        commentMap.put(0L,new Comment());
        //把所有评论转为map数据
        comments.forEach(comment -> commentMap.put(comment.getId(),comment));
        //再次遍历评论数据
        comments.forEach(comment -> {
            //得到父评论
            Comment parent = commentMap.get(comment.getParentId());
            if (parent != null) {
                //初始化children变量
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                //在父评论里添加回复数据
                parent.getChildren().add(comment);
            }
        });
        // 返回所有的一级评论
        return commentMap.get(0L).getChildren();
    }

    // 存储redis并返回所有一级评论
    private List<Comment> SelectAndInsertRedis(String commentName,Map<Object,Object> map,String refId){
        // redis里面不存在数据则查询数据库即可
        List<Comment> comments = querySQL(refId);

        // 如果数据库里面也没有评论，那么就返回Null
        if (comments == null) {
            return null;
        }

        // 遍历存储到map
        comments.forEach(data -> {
            map.put(String.valueOf(data.getId()),data);
        });
        // hash结构存储到redis里面
        redisTemplate.opsForHash().putAll(commentName,map);

        // 刷新redis数据有效期时间，评论与商品详情时间一致
        redisTemplate.expire(commentName,RedisConstants.COMMENT_TIME,TimeUnit.MINUTES);
        return comments;
    }
}
