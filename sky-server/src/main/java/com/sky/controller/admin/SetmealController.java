package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    //新增套餐
    @PostMapping()
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "setmealDTO.categoryId")
    public Result save (@RequestBody SetmealDTO setmealDTO){
        //日志记录
        log.info("新增套餐：{}",setmealDTO);
        //调用service方法
        setmealService.saveWithDish(setmealDTO);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page( SetmealPageQueryDTO setmealPageQueryDTO){
        //日志
        log.info("套餐分页查询：{}",setmealPageQueryDTO);
        //调用service方法
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);

        return Result.success(pageResult);
    }


    @DeleteMapping()
    @ApiOperation("删除套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        //日志
        log.info("删除套餐：{}",ids);
        //调用service方法
        setmealService.deleteBatch(ids);

        return Result.success();
    }


    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        SetmealVO setmealVO = setmealService.getIdWithDish(id);
        return Result.success(setmealVO);
    }


    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public  Result updateMeal(@RequestBody SetmealDTO setmealDTO){
        setmealService.updateWithDish(setmealDTO);
        return Result.success();
    }



    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result starOrStop(@PathVariable Integer status ,Long id){
        setmealService.startOrStop(status,id);
        return  Result.success();
    }

}
