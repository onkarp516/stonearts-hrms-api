package com.opethic.hrms.HRMSNew.controllers.reports;

import com.google.gson.JsonObject;
import com.opethic.hrms.HRMSNew.services.reports_service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @PostMapping(path = "/employeeReportList")
    public Object employeeReportList(@RequestBody Map<String,String> requestParam, HttpServletRequest request){
        JsonObject jsonObject=reportsService.employeeReportList(requestParam,request);
        return jsonObject.toString();
    }

    @PostMapping(path = "/attendanceReportEmployee")
    public Object attendanceReportEmployee(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        JsonObject jsonObject=reportsService.attendanceReportEmployee(requestParam,request);
        return jsonObject.toString();
    }

    @PostMapping(path = "/attendanceReportBranch")
    public Object attendanceReportBranch(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        JsonObject jsonObject=reportsService.attendanceReportBranch(requestParam,request);
        return jsonObject.toString();
    }

    @PostMapping(path = "/leaveReportList")
    public Object leaveReportList(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        JsonObject jsonObject=reportsService.leaveReportList(requestParam,request);
        return jsonObject.toString();
    }

}
