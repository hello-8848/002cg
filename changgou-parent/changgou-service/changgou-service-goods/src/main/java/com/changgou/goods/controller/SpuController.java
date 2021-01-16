package com.changgou.goods.controller;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.changgou.goods.service.SpuService;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:itheima
 * @Description:
 *****/
@Api(value = "SpuController")
@RestController
@RequestMapping("/spu")
@CrossOrigin
public class SpuController {

    @Autowired
    private SpuService spuService;

    /**
     * 逻辑删除商品
     *
     * @param spid :
     * @return : com.changgou.util.Result
     */
    @DeleteMapping(value = "/logic/delete/{spid}")
    public Result logicDelete(@PathVariable(value = "spid") String spid) {
        spuService.logicDelete(spid);
        return new Result(true, StatusCode.OK, "逻辑删除商品成功");
    }

    /**
     * 批量下架
     *
     * @param spids :
     * @return : com.changgou.util.Result
     */
    @PutMapping(value = "/pull/many")
    public Result pullMany(@RequestBody String[] spids) {
        int i = spuService.pullMany(spids);
        return new Result(true, StatusCode.OK, "批量下架" + i + "个商品成功");
    }

    /**
     * 批量上架商品
     *
     * @param spids :
     * @return : com.changgou.util.Result
     */
    @PutMapping(value = "/put/many")
    public Result putMany(@RequestBody String[] spids) {
        int i = spuService.putMany(spids);
        return new Result(true, StatusCode.OK, "批量上架" + i + "个商品成功");
    }

    /**
     * 上架商品
     *
     * @param spid :
     * @return : com.changgou.util.Result
     */
    @PutMapping(value = "/put/{spid}")
    public Result put(@PathVariable(value = "spid") String spid) {
        spuService.put(spid);
        return new Result(true, StatusCode.OK, "商品上架成功");
    }

    /**
     * 下架商品
     *
     * @param spid :
     * @return : com.changgou.util.Result
     */
    @PutMapping(value = "/pull/{spid}")
    public Result pull(@PathVariable(value = "spid") String spid) {
        spuService.pull(spid);
        return new Result(true, StatusCode.OK, "商品下架成功");
    }

    /**
     * 审核商品并上架
     *
     * @param spid :
     * @return : com.changgou.util.Result
     */
    @PutMapping(value = "/audit/{spid}")
    public Result audit(@PathVariable(value = "spid") String spid) {
        spuService.audit(spid);
        return new Result(true, StatusCode.OK, "审核成功并上架");
    }

    /**
     * 根据spuId查询商品信息回显
     *
     * @param spid :
     * @return : com.changgou.util.Result<com.changgou.goods.pojo.Goods>
     */
    @GetMapping(value = "/goods/{spid}")
    public Result<Goods> findBySpuId(@PathVariable(value = "spid") String spid) {
        Goods goods = spuService.findBySpuId(spid);
        return new Result<>(true, StatusCode.OK, "根据spuId查询商品信息成功", goods);
    }

    /**
     * 新增或修改商品
     *
     * @param goods :
     * @return : com.changgou.util.Result
     */
    @PostMapping(value = "/save")
    public Result save(@RequestBody Goods goods) {
        spuService.saveGoods(goods);
        return new Result(true, StatusCode.OK, "编辑商品成功");
    }

    /***
     * Spu分页条件搜索实现
     * @param spu
     * @param page
     * @param size
     * @return
     */
    @ApiOperation(value = "Spu条件分页查询", notes = "分页条件查询Spu方法详情", tags = {"SpuController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true, dataType = "Integer")
    })
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@RequestBody(required = false) @ApiParam(name = "Spu对象", value = "传入JSON数据", required = false) Spu spu, @PathVariable int page, @PathVariable int size) {
        //调用SpuService实现分页条件查询Spu
        PageInfo<Spu> pageInfo = spuService.findPage(spu, page, size);
        return new Result(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /***
     * Spu分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @ApiOperation(value = "Spu分页查询", notes = "分页查询Spu方法详情", tags = {"SpuController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true, dataType = "Integer")
    })
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@PathVariable int page, @PathVariable int size) {
        //调用SpuService实现分页查询Spu
        PageInfo<Spu> pageInfo = spuService.findPage(page, size);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param spu
     * @return
     */
    @ApiOperation(value = "Spu条件查询", notes = "条件查询Spu方法详情", tags = {"SpuController"})
    @PostMapping(value = "/search")
    public Result<List<Spu>> findList(@RequestBody(required = false) @ApiParam(name = "Spu对象", value = "传入JSON数据", required = false) Spu spu) {
        //调用SpuService实现条件查询Spu
        List<Spu> list = spuService.findList(spu);
        return new Result<List<Spu>>(true, StatusCode.OK, "查询成功", list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @ApiOperation(value = "Spu根据ID删除", notes = "根据ID删除Spu方法详情", tags = {"SpuController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "String")
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable String id) {
        //调用SpuService实现根据主键删除
        spuService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /***
     * 修改Spu数据
     * @param spu
     * @param id
     * @return
     */
    @ApiOperation(value = "Spu根据ID修改", notes = "根据ID修改Spu方法详情", tags = {"SpuController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "String")
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody @ApiParam(name = "Spu对象", value = "传入JSON数据", required = false) Spu spu, @PathVariable String id) {
        //设置主键值
        spu.setId(id);
        //调用SpuService实现修改Spu
        spuService.update(spu);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    /***
     * 新增Spu数据
     * @param spu
     * @return
     */
    @ApiOperation(value = "Spu添加", notes = "添加Spu方法详情", tags = {"SpuController"})
    @PostMapping
    public Result add(@RequestBody @ApiParam(name = "Spu对象", value = "传入JSON数据", required = true) Spu spu) {
        //调用SpuService实现添加Spu
        spuService.add(spu);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    /***
     * 根据ID查询Spu数据
     * @param id
     * @return
     */
    @ApiOperation(value = "Spu根据ID查询", notes = "根据ID查询Spu方法详情", tags = {"SpuController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "String")
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable(value = "id") String id) {
        //调用SpuService实现根据主键查询Spu
        Spu spu = spuService.findById(id);
        return new Result<Spu>(true, StatusCode.OK, "查询成功", spu);
    }

    /***
     * 查询Spu全部数据
     * @return
     */
    @ApiOperation(value = "查询所有Spu", notes = "查询所Spu有方法详情", tags = {"SpuController"})
    @GetMapping
    public Result<List<Spu>> findAll() {
        //调用SpuService实现查询所有Spu
        List<Spu> list = spuService.findAll();
        return new Result<List<Spu>>(true, StatusCode.OK, "查询成功", list);
    }
}