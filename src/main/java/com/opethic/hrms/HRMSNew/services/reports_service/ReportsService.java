package com.opethic.hrms.HRMSNew.services.reports_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opethic.hrms.HRMSNew.models.master.*;
import com.opethic.hrms.HRMSNew.repositories.master.AttendanceRepository;
import com.opethic.hrms.HRMSNew.repositories.master.BreakRepository;
import com.opethic.hrms.HRMSNew.repositories.master.EmployeeLeaveRepository;
import com.opethic.hrms.HRMSNew.repositories.master.EmployeeRepository;
import com.opethic.hrms.HRMSNew.services.master.AttendanceService;
import com.opethic.hrms.HRMSNew.util.JwtTokenUtil;
import com.opethic.hrms.HRMSNew.util.Utility;
import com.opethic.hrms.HRMSNew.views.AttendanceView;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportsService {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private Utility utility;

    @Autowired
    private BreakRepository breakRepository;

    private static final Logger attendanceLogger = LoggerFactory.getLogger(AttendanceService.class);
    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;

    public JsonObject employeeReportList(Map<String,String> requestParam, HttpServletRequest request){
        JsonObject response=new JsonObject();
        JsonArray jsonArray=new JsonArray();
        List<Employee> employeeList = null;
        try {
            Users users=jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            String designation=requestParam.get("designation");
            if(designation!=null){
                employeeList=employeeRepository.findByDesignationIdAndStatus(Long.parseLong(designation),true);
            if(employeeList!=null){
               for(Employee employee:employeeList){
                   JsonObject object=new JsonObject();
                   object.addProperty("employeeId",employee.getId());
                   object.addProperty("employeeName",employee.getFullName());
                   object.addProperty("mobileNumber",employee.getMobileNumber());
                   object.addProperty("designation",employee.getDesignation().getDesignationName());
                   object.addProperty("level",employee.getDesignation().getLevel().getLevelName());
                   object.addProperty("employeeType",employee.getEmployeeType());
                   object.addProperty("joiningDate",employee.getDoj().toString());
                   object.addProperty("basicSalary",employee.getExpectedSalary());
                   object.addProperty("dob",employee.getDob().toString());
                   object.addProperty("companyType",employee.getCompany().getCompanyType());

                jsonArray.add(object);
               }
            }
            response.add("response",jsonArray);
            response.addProperty("responseStatus",HttpStatus.OK.value());
            }else {
                response.addProperty("message","Failed to Load Data");
                response.addProperty("responseStatus",HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            response.addProperty("message","Failed to Load Data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject attendanceReportEmployee(Map<String, String> requestParam, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        Users user = jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            LocalDate today = LocalDate.now();
            LocalDate fromDate = null;
            if (!requestParam.get("toDate").equalsIgnoreCase("")) {
                today = LocalDate.parse(requestParam.get("toDate"));
            }
            if (!requestParam.get("fromDate").equalsIgnoreCase("")) {
                fromDate = LocalDate.parse(requestParam.get("fromDate"));
            }
            List<AttendanceView> attendanceViewList = new ArrayList<>();

            String query = "SELECT * from stonearts_new_db.attendance_view WHERE status=1";
            if (requestParam.get("fromDate").equalsIgnoreCase("")) {
                query += " AND attendance_date ='" + today + "'";
            } else if (!requestParam.get("fromDate").equalsIgnoreCase("")) {
                query += " AND attendance_date between '" + fromDate + "' AND '" + today + "'";
            }
            if (!requestParam.get("employeeId").equalsIgnoreCase("")) {
                query += " AND employee_id ='" + requestParam.get("employeeId") + "'";
            }
            if (!requestParam.get("attStatus").equalsIgnoreCase("")) {
                if (!requestParam.get("attStatus").equalsIgnoreCase("pending")) {
                    query += " AND attendance_status ='" + requestParam.get("attStatus") + "'";
                } else {
                    query += " AND attendance_status IS NULL";
                }
            }
//            if (!requestParam.get("selectedShift").equalsIgnoreCase("")) {
//                query += " AND shift_id ='" + requestParam.get("selectedShift") + "'";
//            }
            query += " ORDER BY attendance_date, first_name ASC";
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> query " + query);
            Query q = entityManager.createNativeQuery(query, AttendanceView.class);
            attendanceViewList = q.getResultList();
            System.out.println("attendanceViewList.size() " + attendanceViewList.size());
            for (AttendanceView attendanceView : attendanceViewList) {
                System.out.println("attendanceView.getEmployeeId() " + attendanceView.getEmployeeId() + " attendanceView.getId() " + attendanceView.getId());
                Long empId = Long.parseLong(attendanceView.getEmployeeId().toString());
                Employee employee = employeeRepository.findByIdAndStatus(empId , true);
                System.out.println("employee.getDesignation().getCode() " + employee.getDesignation().getDesignationCode());

                String empName = employee.getFirstName();
                if (employee.getLastName() != null)
                    empName = empName + " " + employee.getLastName();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", attendanceView.getId());
                jsonObject.addProperty("attendanceId", attendanceView.getId());
                jsonObject.addProperty("attendanceDate", attendanceView.getAttendanceDate().toString());
                jsonObject.addProperty("designationCode", employee.getDesignation().getDesignationCode().toUpperCase());
                jsonObject.addProperty("employeeId", employee.getId());
                jsonObject.addProperty("employeeName", empName);
                jsonObject.addProperty("employeeWagesType", attendanceView.getSalaryType() != null ?
                        attendanceView.getSalaryType() : employee.getEmployeeWagesType() != null ?
                        employee.getEmployeeWagesType() : "");
                jsonObject.addProperty("attendanceStatus",attendanceView.getAttendanceStatus() != null ? attendanceView.getAttendanceStatus().equals("approve") ? true : false : false);
                jsonObject.addProperty("isAttendanceApproved", attendanceView.getIsAttendanceApproved());
                jsonObject.addProperty("checkInTime", attendanceView.getCheckInTime().toString());
                jsonObject.addProperty("checkOutTime", attendanceView.getCheckOutTime() != null ? attendanceView.getCheckOutTime().toString() : "");
                jsonObject.addProperty("totalTime", attendanceView.getTotalTime() != null ? attendanceView.getTotalTime() : "");
                jsonObject.addProperty("designation",employee.getDesignation().getDesignationName());
                jsonObject.addProperty("level",employee.getDesignation().getLevel().getLevelName());
                jsonObject.addProperty("company",employee.getCompany().getCompanyName());
                jsonObject.addProperty("branch",employee.getBranch().getBranchName());
                if(attendanceView.getCheckOutTime() != null) {
                    LocalTime timeDiff = utility.getDateTimeDiffInTime(attendanceView.getCheckInTime(), attendanceView.getCheckOutTime());
                    String[] parts = timeDiff.toString().split(":");
                    int hr = Integer.parseInt(parts[0]);
                    int min = Integer.parseInt(parts[1]);
                    double workedHours = utility.getTimeInDouble(hr + ":" + min);
                    if (workedHours > employee.getShift().getWorkingHours().getHour()) {
                        LocalTime overtime = utility.getTimeDiffFromTimes(employee.getShift().getEndTime(), LocalTime.parse(attendanceView.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
                        jsonObject.addProperty("overtime", overtime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                }
                jsonObject.addProperty("lunchTimeInMin", attendanceView.getLunchTime() != null ? Precision.round(attendanceView.getLunchTime(), 2) : 0);
                jsonObject.addProperty("actualWorkTime", attendanceView.getActualWorkTime() != null ? Precision.round(attendanceView.getActualWorkTime(), 2) : 0);
                Double sumOfAvgTaskPercentage = 0.0;

//                double wagesPerHour = (attendanceView.getWagesPerDay() / 8);
                String type = null;
                if(attendanceView.getSalaryType() != null){
                    type = attendanceView.getSalaryType();
                } else if(employee.getEmployeeWagesType() != null) {
                    type = employee.getEmployeeWagesType();
                }
                if(type != null && type.equals("day"))
                    jsonObject.addProperty("wagesPerDay", Precision.round(attendanceView.getWagesPerDay(), 2));
                else if(type != null && type.equals("hour")){
                    jsonObject.addProperty("wagesPerHour", Precision.round(attendanceView.getWagesPerHour(), 2));
                    jsonObject.addProperty("wagesHourBasis", attendanceView.getWagesHourBasis() != null ? Precision.round(attendanceView.getWagesHourBasis(), 2) : 0);
                }

                if(attendanceView.getCheckOutTime() != null){
                    String[] timeParts = attendanceView.getTotalTime().split(":");
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);

                    int totalMinutes = hours * 60 + minutes + (seconds / 60);


                    double actualWorkingHoursInMinutes = totalMinutes - Precision.round(attendanceView.getLunchTime(), 2);
                    jsonObject.addProperty("actualWorkingHoursInMinutes", actualWorkingHoursInMinutes);
                } else {
                    jsonObject.addProperty("actualWorkingHoursInMinutes", "-");
                }
                jsonObject.addProperty("remark", attendanceView.getRemark() != null ? attendanceView.getRemark() : null);
                jsonObject.addProperty("adminRemark", attendanceView.getAdminRemark() != null ? attendanceView.getAdminRemark() : null);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                JsonArray breaksArray = new JsonArray();
                List<Break> breakList = breakRepository.getBreakData(attendanceView.getId(),  true);
                for (Break mBreak : breakList) {
                    JsonObject breakObject = new JsonObject();
                    breakObject.addProperty("id", mBreak.toString());
                    breakObject.addProperty("breakName", mBreak.getBreakMaster().getBreakName().toString());
                    breakObject.addProperty("startTime", mBreak.getBreakStartTime().toString());
                    breakObject.addProperty("endTime", mBreak.getBreakEndTime().toString());
                    breakObject.addProperty("totalTime", mBreak.getTotalBreakTime().toString());
                    breaksArray.add(breakObject);
                }
                jsonObject.add("breakData", breaksArray);
                Double totalBreakTime = breakRepository.getBreakSummary(attendanceView.getId());
                jsonObject.addProperty("totalBreakTime",totalBreakTime);
                jsonArray.add(jsonObject);
            }
            response.add("response", jsonArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            attendanceLogger.error("Data inconsistency, please validate data ===> " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            response.addProperty("message", "Data inconsistency, please validate data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject attendanceReportBranch(Map<String, String> requestParam, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        Users user = jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        try {
            LocalDate today = LocalDate.now();
            LocalDate fromDate = null;
            if (!requestParam.get("toDate").equalsIgnoreCase("")) {
                today = LocalDate.parse(requestParam.get("toDate"));
            }
            if (!requestParam.get("fromDate").equalsIgnoreCase("")) {
                fromDate = LocalDate.parse(requestParam.get("fromDate"));
            }
            List<AttendanceView> attendanceViewList = new ArrayList<>();

            String query = "SELECT * from stonearts_new_db.attendance_view WHERE status=1";
            if (requestParam.get("fromDate").equalsIgnoreCase("")) {
                query += " AND attendance_date ='" + today + "'";
            } else if (!requestParam.get("fromDate").equalsIgnoreCase("")) {
                query += " AND attendance_date between '" + fromDate + "' AND '" + today + "'";
            }
            if (!requestParam.get("branchId").equalsIgnoreCase("")) {
                query += " AND branch_id ='" + requestParam.get("branchId") + "'";
            }
            if (!requestParam.get("attStatus").equalsIgnoreCase("")) {
                if (!requestParam.get("attStatus").equalsIgnoreCase("pending")) {
                    query += " AND attendance_status ='" + requestParam.get("attStatus") + "'";
                } else {
                    query += " AND attendance_status IS NULL";
                }
            }

            query += " ORDER BY attendance_date, first_name ASC";
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> query " + query);
            Query q = entityManager.createNativeQuery(query, AttendanceView.class);
            attendanceViewList = q.getResultList();
            System.out.println("attendanceViewList.size() " + attendanceViewList.size());
            for (AttendanceView attendanceView : attendanceViewList) {
                System.out.println("attendanceView.getEmployeeId() " + attendanceView.getEmployeeId() + " attendanceView.getId() " + attendanceView.getId());
                Long empId = Long.parseLong(attendanceView.getEmployeeId().toString());
                Employee employee = employeeRepository.findByIdAndStatus(empId , true);
                System.out.println("employee.getDesignation().getCode() " + employee.getDesignation().getDesignationCode());

                String empName = employee.getFirstName();
                if (employee.getLastName() != null)
                    empName = empName + " " + employee.getLastName();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", attendanceView.getId());
                jsonObject.addProperty("attendanceId", attendanceView.getId());
                jsonObject.addProperty("attendanceDate", attendanceView.getAttendanceDate().toString());
                jsonObject.addProperty("designationCode", employee.getDesignation().getDesignationCode().toUpperCase());
                jsonObject.addProperty("employeeId", employee.getId());
                jsonObject.addProperty("employeeName", empName);
                jsonObject.addProperty("employeeWagesType", attendanceView.getSalaryType() != null ?
                        attendanceView.getSalaryType() : employee.getEmployeeWagesType() != null ?
                        employee.getEmployeeWagesType() : "");
                jsonObject.addProperty("attendanceStatus",attendanceView.getAttendanceStatus() != null ? attendanceView.getAttendanceStatus().equals("approve") ? true : false : false);
                jsonObject.addProperty("isAttendanceApproved", attendanceView.getIsAttendanceApproved());
                jsonObject.addProperty("checkInTime", attendanceView.getCheckInTime().toString());
                jsonObject.addProperty("checkOutTime", attendanceView.getCheckOutTime() != null ? attendanceView.getCheckOutTime().toString() : "");
                jsonObject.addProperty("totalTime", attendanceView.getTotalTime() != null ? attendanceView.getTotalTime() : "");
                jsonObject.addProperty("designation",employee.getDesignation().getDesignationName());
                jsonObject.addProperty("level",employee.getDesignation().getLevel().getLevelName());
                jsonObject.addProperty("company",employee.getCompany().getCompanyName());
                jsonObject.addProperty("branch",employee.getBranch().getBranchName());
                if(attendanceView.getCheckOutTime() != null) {
                    LocalTime timeDiff = utility.getDateTimeDiffInTime(attendanceView.getCheckInTime(), attendanceView.getCheckOutTime());
                    String[] parts = timeDiff.toString().split(":");
                    int hr = Integer.parseInt(parts[0]);
                    int min = Integer.parseInt(parts[1]);
                    double workedHours = utility.getTimeInDouble(hr + ":" + min);
                    if (workedHours > employee.getShift().getWorkingHours().getHour()) {
                        LocalTime overtime = utility.getTimeDiffFromTimes(employee.getShift().getEndTime(), LocalTime.parse(attendanceView.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
                        jsonObject.addProperty("overtime", overtime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                }
                jsonObject.addProperty("lunchTimeInMin", attendanceView.getLunchTime() != null ? Precision.round(attendanceView.getLunchTime(), 2) : 0);
                jsonObject.addProperty("actualWorkTime", attendanceView.getActualWorkTime() != null ? Precision.round(attendanceView.getActualWorkTime(), 2) : 0);
                Double sumOfAvgTaskPercentage = 0.0;

//                double wagesPerHour = (attendanceView.getWagesPerDay() / 8);
                String type = null;
                if(attendanceView.getSalaryType() != null){
                    type = attendanceView.getSalaryType();
                } else if(employee.getEmployeeWagesType() != null) {
                    type = employee.getEmployeeWagesType();
                }
                if(type != null && type.equals("day"))
                    jsonObject.addProperty("wagesPerDay", Precision.round(attendanceView.getWagesPerDay(), 2));
                else if(type != null && type.equals("hour")){
                    jsonObject.addProperty("wagesPerHour", Precision.round(attendanceView.getWagesPerHour(), 2));
                    jsonObject.addProperty("wagesHourBasis", attendanceView.getWagesHourBasis() != null ? Precision.round(attendanceView.getWagesHourBasis(), 2) : 0);
                }

                if(attendanceView.getCheckOutTime() != null){
                    String[] timeParts = attendanceView.getTotalTime().split(":");
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);

                    int totalMinutes = hours * 60 + minutes + (seconds / 60);


                    double actualWorkingHoursInMinutes = totalMinutes - Precision.round(attendanceView.getLunchTime(), 2);
                    jsonObject.addProperty("actualWorkingHoursInMinutes", actualWorkingHoursInMinutes);
                } else {
                    jsonObject.addProperty("actualWorkingHoursInMinutes", "-");
                }

                jsonObject.addProperty("remark", attendanceView.getRemark() != null ? attendanceView.getRemark() : null);
                jsonObject.addProperty("adminRemark", attendanceView.getAdminRemark() != null ? attendanceView.getAdminRemark() : null);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                JsonArray breaksArray = new JsonArray();
                List<Break> breakList = breakRepository.getBreakData(attendanceView.getId(),  true);
                for (Break mBreak : breakList) {
                    JsonObject breakObject = new JsonObject();
                    breakObject.addProperty("id", mBreak.toString());
                    breakObject.addProperty("breakName", mBreak.getBreakMaster().getBreakName().toString());
                    breakObject.addProperty("startTime", mBreak.getBreakStartTime().toString());
                    breakObject.addProperty("endTime", mBreak.getBreakEndTime().toString());
                    breakObject.addProperty("totalTime", mBreak.getTotalBreakTime().toString());
                    breaksArray.add(breakObject);
                }
                jsonObject.add("breakData", breaksArray);
                Double totalBreakTime = breakRepository.getBreakSummary(attendanceView.getId());
                jsonObject.addProperty("totalBreakTime",totalBreakTime);
                jsonArray.add(jsonObject);
            }
            response.add("response", jsonArray);
            response.addProperty("responseStatus", HttpStatus.OK.value());
        } catch (Exception e) {
            attendanceLogger.error("Data inconsistency, please validate data ===> " + e);
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            response.addProperty("message", "Data inconsistency, please validate data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public JsonObject leaveReportList(Map<String,String> requestParam, HttpServletRequest request){
        JsonObject response=new JsonObject();
        JsonArray jsonArray=new JsonArray();
        try {
            Users users=jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            String fromDate=requestParam.get("fromDate");
            String toDate= requestParam.get("toDate");
            String leaveStatus=requestParam.get("leaveStatus");
            if(leaveStatus!=null){
                List<EmployeeLeave> employeeLeaveList=employeeLeaveRepository.getEmployeeListByStatus(fromDate,toDate,leaveStatus);
                if(employeeLeaveList!=null){
                    for(EmployeeLeave employeeLeave:employeeLeaveList){
                        JsonObject object=new JsonObject();
                        object.addProperty("leaveId",employeeLeave.getId());
                        object.addProperty("employeeName",employeeLeave.getEmployee().getFullName());
                        object.addProperty("fromDate",employeeLeave.getFromDate().toString());
                        object.addProperty("toDate",employeeLeave.getToDate().toString());
                        object.addProperty("totalDays",employeeLeave.getTotalDays());
                        object.addProperty("leaveStatus",employeeLeave.getLeaveStatus());
                        object.addProperty("appliedOn",employeeLeave.getAppliedOn().toString());
                        object.addProperty("leaveType",employeeLeave.getLeaveMaster().getName());
                        object.addProperty("reason",employeeLeave.getReason());
                        jsonArray.add(object);
                    }
                }
                response.add("response",jsonArray);
                response.addProperty("responseStatus",HttpStatus.OK.value());
            }else {
                response.addProperty("message","Failed to Load Data");
                response.addProperty("responseStatus",HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
            response.addProperty("message","Failed to Load Data");
            response.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }
}
