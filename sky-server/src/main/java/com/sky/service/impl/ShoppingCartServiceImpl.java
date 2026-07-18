package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //查询菜品或套餐是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //只能查询当前用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        //如果存在，则数量加一
        if(shoppingCartList != null && shoppingCartList.size() == 1){
            shoppingCart = shoppingCartList.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(shoppingCart);
        }else {
            //不存在则更新数据
            Long dishId =shoppingCartDTO.getDishId();
            if(dishId != null){
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    public List<ShoppingCart> showShoppingCart() {
        return shoppingCartMapper.list(ShoppingCart.
                                       builder().
                                       userId(BaseContext.getCurrentId()).
                                       build());

    }

    public void cleanShoppingCart() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO){
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO , shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if(list != null && list.size() > 0){
            shoppingCart = list.get(0);

            Integer number = shoppingCart.getNumber();
            if(number == 1){
                //当前商品在购物车中的份数为1，直接删除当前记录
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else {
                //当前商品在购物车中的份数不为1，修改份数即可
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
        }
    }
}
