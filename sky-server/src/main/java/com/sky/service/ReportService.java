package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {


     /**
      * 根据时间统计营业额
      * */
     TurnoverReportVO getTurnover(LocalDate begin , LocalDate end);

     /**
      * 统计用户数量
      * */
     UserReportVO getUserStatistics(LocalDate begin, LocalDate end);


     /**
      * 订单统计接口
      * */
     OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

     /**
      * 统计销量前十
      * */
     SalesTop10ReportVO getTop(LocalDate begin, LocalDate end);


     /**
      * 导出excel数据报表
      * */
    void exportBusinessData(HttpServletResponse response);
}
