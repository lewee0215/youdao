package com.liuwei.sharding.jdbc.controller;

import com.liuwei.sharding.jdbc.common.ResponseVO;
import com.liuwei.sharding.jdbc.entity.ShardingUser;
import com.liuwei.sharding.jdbc.common.BaseController;
import com.liuwei.sharding.jdbc.service.IShardingUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2021-01-29
 */
@RestController
@RequestMapping("/shardingUser")
public class ShardingUserController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private IShardingUserService service;
    /**
     * 实体查询
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public ResponseVO search(@PathVariable Long id) {
        return getFromData(service.getById(id));
    }

    /**
     * 实体删除
     */
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public ResponseVO del(@PathVariable Long id) {
        service.removeById(id);
        return getSuccess();
    }

    /**
     * 实体添加
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseVO add(@RequestBody ShardingUser entity) {
        service.save(entity);
        return getSuccess();
    }


}