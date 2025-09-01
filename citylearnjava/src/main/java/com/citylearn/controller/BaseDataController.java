package com.citylearn.controller;

import com.citylearn.common.Result;
import com.citylearn.entity.Building;
import com.citylearn.service.BaseDataService;
import com.citylearn.vo.BuildingDataVO;
import com.citylearn.vo.KpisTransVO;
import com.citylearn.vo.KpisVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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


    @RequestMapping(value = "/updateLearn",method = RequestMethod.GET)
    public Result<String> updateLearn(@RequestParam(name = "agentType")String agentType){
        baseDataService.updateLearn(agentType);
        return Result.success();
    }
}