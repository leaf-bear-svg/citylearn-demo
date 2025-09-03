package com.citylearn.controller;

import com.citylearn.common.Result;
import com.citylearn.entity.Building;
import com.citylearn.param.PyFileParam;
import com.citylearn.service.BaseDataService;
import com.citylearn.vo.BuildingDataVO;
import com.citylearn.vo.KpisTransVO;
import com.citylearn.vo.KpisVO;
import com.citylearn.vo.PyFileVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author  chenglifu
 */
@RestController
@RequestMapping("/web/basedata")
public class BaseDataController {

    @Resource
    private BaseDataService baseDataService;

    @RequestMapping(value = "/getList",method = RequestMethod.GET)
    public Result<List<BuildingDataVO>> getList(@RequestParam(name = "buildingId")String buildingId,
                                                @RequestParam(name = "month",required = false)Integer month,
                                                @RequestParam(name = "hour",required = false)Integer hour,
                                                @RequestParam(name = "dayType",required = false)Integer dayType){
        List<BuildingDataVO> list= baseDataService.getList(buildingId,month,hour,dayType);
        return Result.success(list);
    }

    @RequestMapping(value = "/getKpis",method = RequestMethod.GET)
    public Result<List<KpisTransVO>> getKpis(@RequestParam(name = "agentType")String agentType){
        List<KpisTransVO> list= baseDataService.getKpis(agentType);
        return Result.success(list);
    }


    @RequestMapping(value = "/updateLearn", method = RequestMethod.GET)
    public Result<String> updateLearn(@RequestParam(name = "agentType") String agentType) {
        baseDataService.updateLearn(agentType);
        return Result.success();
    }

    @RequestMapping(value = "/getPyFileList",method = RequestMethod.GET)
    public Result<List<PyFileVO>> getPyFileList(){
        String createUser="admin";
        List<PyFileVO> list= baseDataService.getPyFileList(createUser);
        return Result.success(list);
    }

    @RequestMapping(value = "/getPyFile",method = RequestMethod.GET)
    public Result<String> getPyFile(@RequestParam(name = "id") String id){
        String content= baseDataService.getPyFile(id);
        return Result.success(content);
    }

    @RequestMapping(value = "/savePyFile",method = RequestMethod.POST)
    public Result<String> savePyFile(@RequestBody PyFileParam pyFileParam){
        try {
            baseDataService.savePyFile(pyFileParam);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        }
        return Result.success();
    }

    @RequestMapping(value = "/addPyFile",method = RequestMethod.POST)
    public Result<PyFileVO> addPyFile(){
        PyFileVO pyFileVO= null;
        try {
            pyFileVO = baseDataService.addPyFile();
            return Result.success(pyFileVO);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        }

    }

    @RequestMapping(value = "/deletePyFile",method = RequestMethod.POST)
    public Result<PyFileVO> deletePyFile(@RequestBody PyFileParam pyFileParam){
        try {
            baseDataService.deletePyFile(pyFileParam);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        }
        return Result.success();
    }
}