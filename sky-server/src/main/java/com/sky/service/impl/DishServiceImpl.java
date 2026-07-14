package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sky.constant.StatusConstant;

import java.util.List;


@Service
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    public void saveWithFlavor(DishDTO dishDTO) {
          Dish dish = new Dish();
          BeanUtils.copyProperties(dishDTO, dish);
          //插入一条数据
          dishMapper.insert(dish);

          //获取主键值
          Long dishId = dish.getId();

          List<DishFlavor> flavors = dishDTO.getFlavors();
          if(flavors != null && flavors.size() > 0){
              flavors.forEach(dishFlavor ->  {
                  dishFlavor.setDishId(dishId);
              } );
          }

          //设置n种口味
          dishFlavorMapper.insertBatch(flavors);
    }

    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO){
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return  new PageResult(page.getTotal(),page.getResult());
    }



    @Transactional //开启事务
    public void deleteBatch(List<Long> ids){
        //先判断是否菜品正在销售
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
             //抛出异常
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断是否关联套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds != null && setmealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品
        for (Long id : ids){
            dishMapper.deleteById(id);
            //
            dishFlavorMapper.deleteByDishId(id);
        }
    }
    public DishVO getByWithFlavorId(Long id){
        //获取菜品id
        Dish dish = dishMapper.getById(id);

        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

        //赋值给VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;

    }


    public void updateWithFlavor(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //修改菜品信息
        dishMapper.update(dish);

        //删除菜品口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //重新插入菜品口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishDTO.getId());
                    });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

}
