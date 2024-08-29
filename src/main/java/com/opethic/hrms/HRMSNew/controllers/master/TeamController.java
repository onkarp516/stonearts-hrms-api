package com.opethic.hrms.HRMSNew.controllers.master;

import com.google.gson.JsonObject;
import com.opethic.hrms.HRMSNew.services.master.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping(path="/createTeam")
    public Object createTeam(@RequestBody Map<String,String> requestParam, HttpServletRequest request){
        return teamService.createTeam(requestParam,request);
    }

    @GetMapping(path = "/listOfTeam")
    public Object listOfTeam(HttpServletRequest httpServletRequest){
        JsonObject res=teamService.listOfTeam(httpServletRequest);
        return res.toString();
    }

    @PostMapping(path = "/findTeam")
    public Object findTeam(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        return teamService.findTeam(requestParam,request).toString();
    }

    @PostMapping(path = "/findTeamByBranch")
    public Object findTeamByBranch(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        return teamService.findTeamByBranch(requestParam,request).toString();
    }

    @PostMapping(path = "/updateTeam")
    public Object updateTeam(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        return teamService.updateTeam(requestParam,request);
    }

    @PostMapping(path = "/deleteTeam")
    public Object deleteTeam(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        return teamService.deleteTeam(requestParam,request);
    }

    @PostMapping(path = "/create_team_allocation")
    public Object createTeamAllocation(@RequestBody String requestParam,HttpServletRequest request){
        return teamService.createTeamAllocation(requestParam,request).toString();
    }

    @PostMapping(path = "/getTeamAllocation")
    public Object getTeamAllocation(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        return teamService.getTeamAllocation(requestParam,request).toString();
    }
    @PostMapping(path = "/update_team_allocation")
    public Object updateTeamAllocation(@RequestBody String requestParam,HttpServletRequest request){
        return teamService.updateTeamAllocation(requestParam,request).toString();
    }

//    @PostMapping(path = "/team-allocation-update-teamlead")
//    public Object teamAllocationUpdateTeamlead(@RequestBody String requestParam,HttpServletRequest request){
//        return teamService.teamAllocationUpdateTeamlead(requestParam,request).toString();
//    }


    @GetMapping(path = "/listOfTeamAllocation")
    public Object listOfTeamAllocation(HttpServletRequest httpServletRequest){
        JsonObject res=teamService.listOfTeamAllocation(httpServletRequest);
        return res.toString();
    }

    @PostMapping(path = "/deleteTeamAllocation")
    public Object deleteTeamAllocation(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        return teamService.deleteTeamAllocation(requestParam,request);
    }

    @PostMapping(path = "/getAttendanceMembersList")
    public Object getAttendanceMembersList(@RequestBody Map<String,String> requestParam,HttpServletRequest request){
        return teamService.getAttendanceMembersList(requestParam,request).toString();
    }
}

