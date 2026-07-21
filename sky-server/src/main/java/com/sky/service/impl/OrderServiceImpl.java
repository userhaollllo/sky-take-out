package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

     @Autowired
     private OrderMapper orderMapper;
     @Autowired
     private OrderDetailMapper orderDetailMapper;
     @Autowired
     private ShoppingCartMapper shoppingCartMapper;
     @Autowired
     private UserMapper userMapper;
     @Autowired
     private AddressBookMapper addressBookMapper;
     @Autowired
     private WeChatPayUtil weChatPayUtil;



     public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
          //地址为空时，抛出异常
          AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
          if(addressBook == null){
               throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
          }

          Long userId = BaseContext.getCurrentId();
          ShoppingCart shoppingCart = new ShoppingCart();
          shoppingCart.setUserId(userId);
          //查询购物车数据
          List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
          if(shoppingCartList == null || shoppingCartList.size() == 0){
               throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
          }
          //构建订单
          Orders order = new Orders();
          BeanUtils.copyProperties(ordersSubmitDTO,order);
          order.setPhone(addressBook.getPhone());
          order.setAddress(addressBook.getDetail());
          order.setConsignee(addressBook.getConsignee());
          order.setNumber(String.valueOf(System.currentTimeMillis()));
          order.setUserId(userId);
          order.setStatus(Orders.PENDING_PAYMENT);
          order.setPayStatus(Orders.UN_PAID);
          order.setOrderTime(LocalDateTime.now());

          //插入数据
          orderMapper.insert(order);

          List<OrderDetail> orderDetailList = new ArrayList<>();
          for (ShoppingCart cart : shoppingCartList){
               OrderDetail orderDetail = new OrderDetail();
               BeanUtils.copyProperties(cart,orderDetail);
               orderDetail.setOrderId(order.getId());
               orderDetailList.add(orderDetail);
          }

          orderDetailMapper.insertBatch(orderDetailList);

          //返回数据VO
          OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                  .id(order.getId())
                  .orderAmount(order.getAmount())
                  .orderNumber(order.getNumber())
                  .orderTime(order.getOrderTime())
                  .build();

          return orderSubmitVO;
     }

     /**
      * 订单支付
      *
      * @param ordersPaymentDTO
      * @return
      */
     public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        /*  // 当前登录用户id
          Long userId = BaseContext.getCurrentId();
          User user = userMapper.getById(userId);

          //调用微信支付接口，生成预支付交易单
          JSONObject jsonObject = weChatPayUtil.pay(
                  ordersPaymentDTO.getOrderNumber(), //商户订单号
                  new BigDecimal(0.01), //支付金额，单位 元
                  "苍穹外卖订单", //商品描述
                  user.getOpenid() //微信用户的openid
          );

          if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
               throw new OrderBusinessException("该订单已支付");
          }

          OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
          vo.setPackageStr(jsonObject.getString("package"));*/

          paySuccess(ordersPaymentDTO.getOrderNumber());

          return new OrderPaymentVO();
     }

     /**
      * 支付成功，修改订单状态
      *
      * @param outTradeNo
      */
     public void paySuccess(String outTradeNo) {

          // 根据订单号查询订单
          Orders ordersDB = orderMapper.getByNumber(outTradeNo);

          // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
          Orders orders = Orders.builder()
                  .id(ordersDB.getId())
                  .status(Orders.TO_BE_CONFIRMED)
                  .payStatus(Orders.PAID)
                  .checkoutTime(LocalDateTime.now())
                  .build();

          orderMapper.update(orders);
     }

     public PageResult pageQuery4User(int pageNum,int pageSize,Integer status){
          //设置分页
          PageHelper.startPage(pageNum,pageSize);
          OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
          ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
          ordersPageQueryDTO.setStatus(status);
          //分页查询
          Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

          List<OrderVO> list = new ArrayList<>();
          //查询订单详情，封装给vo
          if(page != null && page.getTotal() > 0){
               for(Orders orders : page){
                   Long orderId = orders.getId();
                   //查询订单明细
                    List<OrderDetail>  orderDetails = orderDetailMapper.getByOrderId(orderId);
                    //封装订单VO
                    OrderVO orderVO = new OrderVO();
                    BeanUtils.copyProperties(orders,orderVO);
                    orderVO.setOrderDetailList(orderDetails);

                    list.add(orderVO);
               }
          }
          return new PageResult(page.getTotal(),list);
     }


     public OrderVO detail(Long id){
          //根据订单id查询订单
          Orders orders =orderMapper.getById(id);

          List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

          OrderVO orderVO = new OrderVO();
          BeanUtils.copyProperties(orders,orderVO);
          orderVO.setOrderDetailList(orderDetails);

          return orderVO;
     }

     public void userCancelById(Long id){
          Orders ordersDB = orderMapper.getById(id);
          if(ordersDB == null){
               throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
          }
          //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
          if(ordersDB.getStatus()>2){
               throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
          }
          Orders orders = new Orders();
          orders.setId(ordersDB.getId());
          //如果订单状态为待接单，直接取消订单，然后退款
          if(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
               //调用微信支付退款接口
               //  weChatPayUtil.refund(
               //          ordersDB.getNumber(), //商户订单号
               //          ordersDB.getNumber(), //商户退款单号
               //          new BigDecimal(0.01),//退款金额，单位 元
               //          new BigDecimal(0.01));//原订单金额

               //支付状态修改为 退款
               orders.setPayStatus(Orders.REFUND);
          }

          //更新订单状态，取消原因，取消时间
          orders.setStatus(Orders.CANCELLED);
          orders.setCancelReason(("用户取消订单"));
          orders.setCancelTime(LocalDateTime.now());
          orderMapper.update(orders);
     }


     public void repetition(Long id){
          //获取当前用户id
          Long userId = BaseContext.getCurrentId();
          //通过订单id查询订单详情
          List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(id);
          //订单详情对象转换成购物车对象
          List<ShoppingCart> shoppingCartList = orderDetail.stream().map(x ->{
               ShoppingCart shoppingCart = new ShoppingCart();

               BeanUtils.copyProperties(x,shoppingCart,"id");
               shoppingCart.setUserId(userId);
               shoppingCart.setCreateTime(LocalDateTime.now());
               return shoppingCart;
          }).collect(Collectors.toList());
          //插入数据库
          shoppingCartMapper.insertBatch(shoppingCartList);
     }
}
