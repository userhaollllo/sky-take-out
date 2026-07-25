package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import java.time.LocalDate;

public interface ReportService {


     /**
      * 根据时间统计营业额
      * */
     TurnoverReportVO getTurnover(LocalDate begin , LocalDate end);
}
