package com.opethic.hrms.HRMSNew.services.master;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opethic.hrms.HRMSNew.models.master.*;
import com.opethic.hrms.HRMSNew.repositories.master.*;
import com.opethic.hrms.HRMSNew.response.ResponseMessage;
import com.opethic.hrms.HRMSNew.util.JwtTokenUtil;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class TeamService {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private TeamAllocationRepository teamAllocateRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;

    public Object createTeam(Map<String,String> requestParam, HttpServletRequest request){
        ResponseMessage responseMessage=new ResponseMessage();
        Branch branch=branchRepository.findByIdAndStatus(Long.parseLong(requestParam.get("branch")),true);
        if(branch!=null){
            Team team=new Team();
            team.setTeamName(requestParam.get("teamName"));
            team.setBranch(branch);
            team.setStatus(true);
            if(request.getHeader("Authorization")!=null){
                Users users=jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
                team.setCreatedBy(users.getId());
                team.setCompany(users.getCompany());
                team.setBranch(users.getBranch());
            }
            try{
                Team team1=teamRepository.save(team);
                responseMessage.setResponse(team1);
                responseMessage.setMessage("Team Added Successfully");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            } catch (Exception e){
                responseMessage.setMessage("Failed to Create Team");
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                e.printStackTrace();
                System.out.println("Exception:" + e.getMessage());
            }
        }else {
            responseMessage.setMessage("Failed to Create Team");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    public JsonObject listOfTeam(HttpServletRequest httpServletRequest){
        Users users=jwtTokenUtil.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject response=new JsonObject();
        JsonArray jsonArray=new JsonArray();
        try{
            if(users.getIsAdmin()){
                List<Team> teamList=teamRepository.findAllByCompanyIdAndBranchIdAndStatus(users.getCompany().getId(),users.getBranch().getId(),true);
                for(Team team:teamList){
                    JsonObject object=new JsonObject();
                    object.addProperty("id",team.getId());
                    object.addProperty("teamName",team.getTeamName());
                    object.addProperty("branch",team.getBranch().getBranchName());
                    jsonArray.add(object);
                }
                response.add("response",jsonArray);
                response.addProperty("responseStatus",HttpStatus.OK.value());
            }
        }catch (Exception e){
            response.addProperty("message","Failed to Load Data");
            response.addProperty("responseStatus",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object findTeam(Map<String,String> requestParam,HttpServletRequest request){
        JsonObject response=new JsonObject();
        Long teamId=Long.parseLong(requestParam.get("id"));
        try {
            Team team=teamRepository.findByIdAndStatus(teamId,true);
            if(team!=null){
                JsonObject object=new JsonObject();
                object.addProperty("id",team.getId());
                object.addProperty("teamName",team.getTeamName());
                object.addProperty("branch",team.getBranch().getId());
                response.add("response",object);
                response.addProperty("responseStatus",HttpStatus.OK.value());
            }else {
                response.addProperty("message","Failed to Load Data");
                response.addProperty("responseStatus",HttpStatus.NOT_FOUND.value());
            }
        }catch (Exception e){
            System.out.println("Exception" + e.getMessage());
            e.printStackTrace();
            response.addProperty("message","Failed to Load Data");
            response.addProperty("responseStatus",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object updateTeam(Map<String,String> requestParam,HttpServletRequest request){
        ResponseMessage responseMessage=new ResponseMessage();
        Long teamId=Long.parseLong(requestParam.get("id"));
        Users users=jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        if(teamId!=null){
            Team team=teamRepository.findByIdAndStatus(teamId,true);
            team.setTeamName(requestParam.get("teamName"));
            Branch branch=branchRepository.findByIdAndStatus(Long.parseLong(requestParam.get("id")),true);
            team.setBranch(branch);
            team.setUpdatedBy(users.getId());
            team.setCompany(users.getCompany());
            team.setBranch(users.getBranch());
            try {
                teamRepository.save(team);
                responseMessage.setMessage("Team Created Successfully");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Exception " + e.getMessage());
                responseMessage.setMessage("Failed to Update Team");
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }else {
            responseMessage.setMessage("Failed to Update Team");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    public Object deleteTeam(Map<String,String> requestParam,HttpServletRequest request){
        ResponseMessage responseMessage=new ResponseMessage();
        Team team=teamRepository.findByIdAndStatus(Long.parseLong(requestParam.get("id")),true);
        if(team!=null){
            team.setStatus(false);
            team.setUpdatedAt(LocalDateTime.now());
            try {
                teamRepository.save(team);
                responseMessage.setMessage("Team Deleted Successfully");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            }catch (Exception e){
                System.out.println("Exception " + e.getMessage());
                responseMessage.setMessage("Failed to Delete Team");
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }else {
            responseMessage.setMessage("Failed to delete Team");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    public JsonObject createTeamAllocation(String requestParam, HttpServletRequest request) {
        JsonObject response=new JsonObject();
        Users users = jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(requestParam);
        Team team = teamRepository.findByIdAndStatus(json.get("teamId").getAsLong(),true);
        if(team != null) {
            JsonArray membersArray = json.getAsJsonArray("members");
            Long[] members = new Long[membersArray.size()];
            for (int i = 0; i < membersArray.size(); i++) {
                members[i] = membersArray.get(i).getAsLong();
            }
            for (int i = 0; i < members.length; i++) {
                TeamAllocate teamAllocate = new TeamAllocate();
                Employee teamLead = employeeRepository.findByIdAndStatus(json.get("teamLeaderId").getAsLong(),true);
                teamAllocate.setTeamLeader(teamLead);
                teamAllocate.setTeam(team);
                Employee member = employeeRepository.findByIdAndStatus(members[i], true);
                teamAllocate.setMember(member);
                teamAllocate.setStatus(true);
                teamAllocate.setCreatedAt(LocalDateTime.now());
                teamAllocate.setCreatedBy(users.getId());
                TeamAllocate ta = teamAllocateRepository.save(teamAllocate);
            }
            response.addProperty("message","Team Allocated Successfully");
            response.addProperty("responseStatus",HttpStatus.OK.value());
            return response;
        }
        response.addProperty("message","Team not found");
        response.addProperty("responseStatus",HttpStatus.NOT_FOUND.value());
        return response;
    }

    public Object getTeamAllocation(Map<String, String> requestParam, HttpServletRequest request) {
        JsonObject response=new JsonObject();
        Long teamId=Long.parseLong(requestParam.get("teamId"));
        Long teamLeaderId=Long.parseLong(requestParam.get("teamLeaderId"));
        Long[] members = null;
        List<TeamAllocate> teamAllocateList = teamAllocateRepository.findByTeamIdAndTeamLeaderIdAndStatus(teamId,teamLeaderId,true);
        if(teamAllocateList != null){
            JsonObject object = new JsonObject();
            object.addProperty("teamId", teamId);
            object.addProperty("teamLeaderId", teamLeaderId);
            members = new Long[teamAllocateList.size()];
            for (int i = 0; i < teamAllocateList.size(); i++) {
                members[i] = teamAllocateList.get(i).getMember().getId();
            }
            object.addProperty("members", Arrays.toString(members));
            response.add("data",object);
            response.addProperty("responseStatus",HttpStatus.OK.value());
        } else {
            response.addProperty("message", "Data not found");
            response.addProperty("responseStatus",HttpStatus.OK.value());
        }
        return response;
    }

    public JsonObject updateTeamAllocation(String requestParam, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Users users = jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(requestParam);
        Long inputteamId = json.get("teamId").getAsLong();
        Long inputteamLeader = json.get("teamLeaderId").getAsLong();
        Long inputdbteamLeaderId = json.get("dbteamLeaderId").getAsLong();
        List<TeamAllocate> teamAllocateList = null;
        Employee teamLeader = null;

        if (inputteamLeader.equals(inputdbteamLeaderId)) {
            System.out.println("inputteamLeader is equal to inputdbteamLeaderId");
            teamAllocateList=teamAllocateRepository.findByTeamIdAndTeamLeaderIdAndStatus(inputteamId, inputteamLeader,true);
            teamLeader = employeeRepository.findByIdAndStatus(inputteamLeader,true);
        } else {
            teamAllocateList=teamAllocateRepository.findByTeamIdAndTeamLeaderIdAndStatus(inputteamId, inputdbteamLeaderId,true);
            teamLeader = employeeRepository.findByIdAndStatus(inputdbteamLeaderId,true);
        }
        if(teamAllocateList != null){
            for(TeamAllocate teamAllocate : teamAllocateList){
                teamAllocate.setStatus(false);
                teamAllocate.setUpdatedAt(LocalDateTime.now());
                teamAllocate.setUpdatedBy(users.getId());
                teamAllocateRepository.save(teamAllocate);
            }
            JsonArray membersArray = json.getAsJsonArray("members");
            Long[] members = new Long[membersArray.size()];
            for (int i = 0; i < membersArray.size(); i++) {
                members[i] = membersArray.get(i).getAsLong();
            }
            Team team = teamRepository.findByIdAndStatus(inputteamId, true);
            for (int i = 0; i < members.length; i++) {
                TeamAllocate teamAllocate = new TeamAllocate();
                teamAllocate.setTeamLeader(teamLeader);
                teamAllocate.setTeam(team);
                Employee member = employeeRepository.findByIdAndStatus(members[i], true);
                teamAllocate.setMember(member);
                teamAllocate.setStatus(true);
                teamAllocate.setUpdatedAt(LocalDateTime.now());
                teamAllocate.setUpdatedBy(users.getId());
                TeamAllocate ta = teamAllocateRepository.save(teamAllocate);
            }
            response.addProperty("message","Team Allocation updated Successfully");
            response.addProperty("responseStatus",HttpStatus.OK.value());
        } else {
            response.addProperty("message","Data not found");
            response.addProperty("responseStatus",HttpStatus.NOT_FOUND.value());
        }

//        Team team = teamRepository.findByIdAndStatus(json.get("teamId").getAsLong(),true);
//        if(team != null) {
//            JsonArray membersArray = json.getAsJsonArray("members");
//            Long[] members = new Long[membersArray.size()];
//            for (int i = 0; i < membersArray.size(); i++) {
//                members[i] = membersArray.get(i).getAsLong();
//            }
//            for (int i = 0; i < members.length; i++) {
//                TeamAllocate teamAllocate = new TeamAllocate();
//                Employee teamLead = employeeRepository.findByIdAndStatus(json.get("teamLeaderId").getAsLong(),true);
//                teamAllocate.setTeamLeader(teamLead);
//                Employee member = employeeRepository.findByIdAndStatus(members[i], true);
//                teamAllocate.setMember(member);
//                teamAllocate.setStatus(true);
//                TeamAllocate ta = teamAllocateRepository.save(teamAllocate);
//            }
//            response.addProperty("message","Team Allocated Successfully");
//            response.addProperty("responseStatus",HttpStatus.OK.value());
//            return response;
//        }
//        response.addProperty("message","Team not found");
//        response.addProperty("responseStatus",HttpStatus.NOT_FOUND.value());
        return response;
    }

    public JsonObject listOfTeamAllocation(HttpServletRequest httpServletRequest){
        Users users=jwtTokenUtil.getUserDataFromToken(httpServletRequest.getHeader("Authorization").substring(7));
        JsonObject response=new JsonObject();
        JsonArray jsonArray=new JsonArray();
        try{
            if(users.getIsAdmin()){
                List<TeamAllocate> teamAllocateList=teamAllocateRepository.getTeamAllocationData(users.getCompany().getId(), users.getBranch().getId());
                for(TeamAllocate team:teamAllocateList){
                    JsonObject object=new JsonObject();
                    object.addProperty("teamId",team.getTeam().getId());
                    object.addProperty("teamName",team.getTeam().getTeamName());
                    object.addProperty("teamLeaderId",team.getTeamLeader().getId());
                    object.addProperty("teamLeaderName",team.getTeamLeader().getFullName());
                    jsonArray.add(object);
                }
                response.add("response",jsonArray);
                response.addProperty("responseStatus",HttpStatus.OK.value());
            }
        }catch (Exception e){
            response.addProperty("message","Failed to Load Data");
            response.addProperty("responseStatus",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object deleteTeamAllocation(Map<String,String> requestParam,HttpServletRequest request){
        Users users=jwtTokenUtil.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        ResponseMessage responseMessage=new ResponseMessage();
        List<TeamAllocate> teamAllocateList = teamAllocateRepository.findByTeamIdAndTeamLeaderIdAndStatus(Long.parseLong(requestParam.get("teamId")),Long.parseLong(requestParam.get("teamLeaderId")),true);
        if(teamAllocateList != null){
            try {
                for(TeamAllocate teamAllocate : teamAllocateList) {
                    teamAllocate.setStatus(false);
                    teamAllocate.setUpdatedAt(LocalDateTime.now());
                    teamAllocate.setUpdatedBy(users.getId());
                    teamAllocateRepository.save(teamAllocate);
                }
                responseMessage.setMessage("Team Deleted Successfully");
                responseMessage.setResponseStatus(HttpStatus.OK.value());
            } catch (Exception e) {
                System.out.println("Exception " + e.getMessage());
                responseMessage.setMessage("Failed to Delete Team");
                responseMessage.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } else {
            responseMessage.setMessage("Team Allocation data not found");
            responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
        }
        return responseMessage;
    }

    public Object findTeamByBranch(Map<String,String> requestParam,HttpServletRequest request){
        JsonObject response=new JsonObject();
        Long branchId=Long.parseLong(requestParam.get("branchId"));
        JsonArray array = new JsonArray();
        try {
            List<Team> teamList=teamRepository.findByBranchIdAndStatus(branchId,true);
            if(teamList != null){
                for(Team team : teamList) {
                    JsonObject object = new JsonObject();
                    object.addProperty("id", team.getId());
                    object.addProperty("teamName", team.getTeamName());
                    array.add(object);
                }
                response.add("response",array);
                response.addProperty("responseStatus",HttpStatus.OK.value());
            }else {
                response.addProperty("message","Failed to Load Data");
                response.addProperty("responseStatus",HttpStatus.NOT_FOUND.value());
            }
        }catch (Exception e){
            System.out.println("Exception" + e.getMessage());
            e.printStackTrace();
            response.addProperty("message","Failed to Load Data");
            response.addProperty("responseStatus",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Object getAttendanceMembersList(Map<String, String> requestParam, HttpServletRequest request) {
        Employee employee = jwtTokenUtil.getEmployeeDataFromToken(request.getHeader("Authorization").substring(7));
        JsonObject response=new JsonObject();
        JsonArray jsonArray=new JsonArray();
        Boolean attType = Boolean.parseBoolean(requestParam.get("attendanceType"));
        List<TeamAllocate> employeeList = null;
        JsonArray members = new JsonArray();
        try{
            if(attType){
                employeeList = teamAllocateRepository.findByTeamIdAndTeamLeaderIdAndStatus(Long.parseLong(requestParam.get("teamId")), employee.getId(), true);
                if(employeeList != null) {
                    for(TeamAllocate teamAllocate : employeeList){
                        JsonObject object = new JsonObject();
                        Attendance attendance = attendanceRepository.findAttendanceOfTeamMember(LocalDate.now().toString(), teamAllocate.getMember().getId());
                        if(attendance != null){
                            if(attendance.getCheckOutTime() != null){
                                object.addProperty("teamId", teamAllocate.getTeam().getId());
                                object.addProperty("employeeId", teamAllocate.getMember().getId());
                                object.addProperty("employeeName", teamAllocate.getMember().getFullName());
                                members.add(object);
                            }
                        } else {
                            object.addProperty("teamId", teamAllocate.getTeam().getId());
                            object.addProperty("employeeId", teamAllocate.getMember().getId());
                            object.addProperty("employeeName", teamAllocate.getMember().getFullName());
                            members.add(object);
                        }
                    }
                    response.add("members",members);
                    response.addProperty("responseStatus",HttpStatus.OK.value());
                } else {
                    response.addProperty("message","Team members not found");
                    response.addProperty("responseStatus",HttpStatus.NOT_FOUND.value());
                }
            } else {
                employeeList = teamAllocateRepository.findByTeamIdAndTeamLeaderIdAndStatus(Long.parseLong(requestParam.get("teamId")), employee.getId(),true);
                if(employeeList != null) {
                    for(TeamAllocate teamAllocate : employeeList){
                        JsonObject object = new JsonObject();
                        Attendance attendance = attendanceRepository.findAttendanceOfTeamMember(LocalDate.now().toString(), teamAllocate.getMember().getId());
                        if(attendance != null){
                            if(attendance.getCheckInTime() != null && attendance.getCheckOutTime() == null){
                                object.addProperty("teamId", teamAllocate.getTeam().getId());
                                object.addProperty("employeeId", teamAllocate.getMember().getId());
                                object.addProperty("employeeName", teamAllocate.getMember().getFullName());
                                members.add(object);
                            }
                        }
                    }
                    response.add("members",members);
                    response.addProperty("responseStatus",HttpStatus.OK.value());
                } else {
                    response.addProperty("message","Team members not found");
                    response.addProperty("responseStatus",HttpStatus.NOT_FOUND.value());
                }
            }
        }catch (Exception e){
            response.addProperty("message","Failed to Load Data");
            response.addProperty("responseStatus",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }
}
