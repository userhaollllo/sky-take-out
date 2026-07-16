package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO){
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO , setmeal);

        //插入套餐信息
        setmealMapper.insert(setmeal);

        //获取主键
        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish ->{
            setmealDish.setDishId(setmealId);
        });

        //保存套餐菜品关系
        setmealDishMapper.insertBatch(setmealDishes);
    }


    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Transactional
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }


    public void deleteBatch(List<Long> ids){
        //检查菜品是否在售
        for(Long id :ids){
           Setmeal setmeal = setmealMapper.getById(id);
           if(setmeal.getStatus() == StatusConstant.ENABLE){
               throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
           }
        }

        //删除套餐
        for(Long id :ids){
            setmealMapper.deleteById(id);

            setmealDishMapper.deleteDishById(id);
        }
    }


    //根据id查询套餐
    public SetmealVO getIdWithDish(Long id){
        Setmeal setmeal = setmealMapper.getById(id);

        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal , setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }


    //修改套餐
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO){
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO , setmeal);

        setmealMapper.update(setmeal);

        Long setmealID = setmealDTO.getId();


        setmealDishMapper.deleteDishById(setmealID);

        List<SetmealDish> dishs = setmealDTO.getSetmealDishes();
        dishs.forEach(setmealDish -> {
            setmealDish.setDishId(setmealID);
        });

        setmealDishMapper.insertBatch(dishs);
    }

    public void startOrStop(Integer status, Long id){
        if (status == StatusConstant.ENABLE){
            List<Dish> dishList = dishMapper.getBysetmealId(id);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    if(StatusConstant.DISABLE == dish.getStatus()){
                        throw  new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    public List<Setmeal> list(Setmeal setmeal){
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    public List<DishItemVO> getDishItemById(Long id){

        return setmealMapper.getDishItemById(id);
    }
}
