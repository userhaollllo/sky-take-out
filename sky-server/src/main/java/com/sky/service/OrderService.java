package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

   /**
    * 提交订单
    * */
   OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

   /**
    * 订单支付
    * @param ordersPaymentDTO
    * @return
    */
   OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

   /**
    * 支付成功，修改订单状态
    * @param outTradeNo
    */
   void paySuccess(String outTradeNo);


   /**
    * 查询历史订单
    *
    * */
   PageResult pageQuery4User(int page, int pageSize, Integer status);

   /**
    * 查看订单详情
    * */
   OrderVO details(Long id);
   /**
    * 取消订单
    * */
   void userCancelById(Long id) throws Exception;

   /**
    * 再来一单
    * */
   void repetition(Long id);


   /**
    * 订单搜索
    * */
   PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

   /**
    * 各个不同状态订单数量统计
    * */
   OrderStatisticsVO statistics();

   /**
    * 接单
    * */
   void confirm(OrdersConfirmDTO ordersConfirmDTO);

   /**
    * 拒单
    * */
   void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

   /**
    * 取消订单
    * */
   void cancel(OrdersCancelDTO ordersCancelDTO) throws  Exception;

   /**
    * 派送订单
    *
    * @param id
    */
   void delivery(Long id);

   /**
    * 完成订单
    *
    * @param id
    */
   void complete(Long id);


   /**
    * 催单
    * */
    void reminder(Long id);
}
