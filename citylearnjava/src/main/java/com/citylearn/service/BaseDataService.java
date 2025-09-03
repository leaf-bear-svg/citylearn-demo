package com.citylearn.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citylearn.dao.*;
import com.citylearn.entity.*;
import com.citylearn.param.PyFileParam;
import com.citylearn.vo.BuildingDataVO;
import com.citylearn.vo.KpisTransVO;
import com.citylearn.vo.KpisVO;
import com.citylearn.vo.PyFileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Your Name
 * @since 2025-06-08
 */
@Service
public class BaseDataService{

    @Resource
    private Building1Mapper building1Mapper;
    @Resource
    private Building2Mapper building2Mapper;
    @Resource
    private Building3Mapper building3Mapper;
    @Resource
    private CarbonIntensityMapper carbonIntensityMapper;
    @Resource
    private PricingMapper pricingMapper;
    @Resource
    private WeatherMapper weatherMapper;
    @Resource
    private KpisMapper kpisMapper;
    @Resource
    private PyFileMapper pyFileMapper;

    // 使用普通 Map，并通过 @PostConstruct 初始化
    private static Map<String, BaseMapper> BUILDING_MAPPER = new HashMap<>();

    @PostConstruct
    public void init() {
        BUILDING_MAPPER.put("building1", building1Mapper);
        BUILDING_MAPPER.put("building2", building2Mapper);
        BUILDING_MAPPER.put("building3", building3Mapper);
    }
    public List<BuildingDataVO> getList(String buildingId,Integer month,Integer hour,Integer dayType){
        List<?  extends Building> buildingList=null;
        if("building1".equals(buildingId)){
            buildingList=building1Mapper.selectList(new QueryWrapper<Building1>()
                    .lambda()
                    .eq(null!=month,Building1::getMonth,month)
                    .eq(null!=hour,Building1::getHour,hour)
                    .eq(null!=dayType,Building1::getDayType,dayType)
                    .orderByAsc(Building1::getId)
                    .last(null==month && null==hour && null==dayType,"limit 36"));
        }else if("building2".equals(buildingId)){
            buildingList=building2Mapper.selectList(new QueryWrapper<Building2>()
                    .lambda()
                    .eq(null!=month,Building2::getMonth,month)
                    .eq(null!=hour,Building2::getHour,hour)
                    .eq(null!=dayType,Building2::getDayType,dayType)
                    .orderByAsc(Building2::getId)
                    .last(null==month && null==hour && null==dayType,"limit 36"));
        }else if("building3".equals(buildingId)){
            buildingList=building3Mapper.selectList(new QueryWrapper<Building3>()
                    .lambda()
                    .eq(null!=month,Building3::getMonth,month)
                    .eq(null!=hour,Building3::getHour,hour)
                    .eq(null!=dayType,Building3::getDayType,dayType)
                    .orderByAsc(Building3::getId)
                    .last(null==month && null==hour && null==dayType,"limit 36"));
        }
        if(null==buildingList || buildingList.size()==0){
            return new ArrayList<>();
        }

        List<Integer> idList=buildingList.stream().map(Building::getId).collect(Collectors.toList());

        List<CarbonIntensity> carbonIntensityList=carbonIntensityMapper.selectList(new QueryWrapper<CarbonIntensity>()
                .lambda()
                .in(CarbonIntensity::getId,idList)
                .orderByAsc(CarbonIntensity::getId));

        List<Pricing> pricingList=pricingMapper.selectList(new QueryWrapper<Pricing>()
                .lambda()
                .in(Pricing::getId,idList)
                .orderByAsc(Pricing::getId));

        List<Weather> weatherList=weatherMapper.selectList(new QueryWrapper<Weather>()
                .lambda()
                .in(Weather::getId,idList)
                .orderByAsc(Weather::getId));


        List<BuildingDataVO> returnList=new ArrayList<>();
        for(int i=0;i<buildingList.size();i++){
            BuildingDataVO buildingDataVO=new BuildingDataVO();
            buildingDataVO.setBuildingId(buildingId);
            BeanUtils.copyProperties(buildingList.get(i),buildingDataVO);
            BeanUtils.copyProperties(carbonIntensityList.get(i),buildingDataVO);
            BeanUtils.copyProperties(pricingList.get(i),buildingDataVO);
            BeanUtils.copyProperties(weatherList.get(i),buildingDataVO);
            returnList.add(buildingDataVO);
        }


        return returnList;
    }


    private static final String PYTHON_PATH = "D:/Users/clfbe/anaconda3/envs/cl2/python.exe";



    private static  final  List<String> LINE_TITLE=new ArrayList<String>(){{
        add("allTimePeakAverage");
        add("annualNormalizedUnservedEnergyTotal");
        add("carbonEmissionsTotal");
        add("costTotal");
        add("dailyOneMinusLoadFactorAverage");
        add("dailyPeakAverage");
        add("discomfortColdDeltaAverage");
        add("discomfortColdDeltaMaximum");
        add("discomfortColdDeltaMinimum");
        add("discomfortColdProportion");
        add("discomfortHotDeltaAverage");
        add("discomfortHotDeltaMaximum");
        add("discomfortHotDeltaMinimum");
        add("discomfortHotProportion");
        add("discomfortProportion");
        add("electricityConsumptionTotal");
        add("monthlyOneMinusLoadFactorAverage");
        add("oneMinusThermalResilienceProportion");
        add("powerOutageNormalizedUnservedEnergyTotal");
        add("rampingAverage");
        add("zeroNetEnergy");
    }} ;

    private static  final  List<String> LINE_TXT_TITLE=new ArrayList<String>(){{
        add("全时段峰值平均值");
        add("年度标准化未满足能源总量");
        add("碳排放总量");
        add("总成本");
        add("日平均(1-负荷率)");
        add("日峰值平均值");
        add("低温不适温差平均值");
        add("低温不适温差最大值");
        add("低温不适温差最小值");
        add("低温不适温差比例");
        add("高温不适温差平均值");
        add("高温不适温差最大值");
        add("高温不适温差最小值");
        add("高温不适温差比例");
        add("不适比例");
        add("电量消耗总量");
        add("月平均(1-负荷率)");
        add("热弹性不足比例");
        add("停电未满足能源总量");
        add("负荷爬坡率");
        add("零净能耗达标率");


    }} ;
    public List<KpisTransVO> getKpis(String agentType) {
        List<Kpis> kpisList=kpisMapper.selectList(new QueryWrapper<Kpis>()
                .lambda()
                .eq(Kpis::getAgentType,agentType));

        if(null==kpisList || kpisList.size()==0){
            return new ArrayList<>();
        }

        JSONArray jsonArray=JSON.parseArray(JSON.toJSONString(kpisList));

        List<KpisTransVO> returnList=new ArrayList<>();
        for(int i=0;i<LINE_TITLE.size();i++){
            String title=LINE_TITLE.get(i);
            KpisTransVO kpisTransVO=new KpisTransVO();
            kpisTransVO.setLine1(LINE_TXT_TITLE.get(i));
            kpisTransVO.setLine2(jsonArray.getJSONObject(0).getBigDecimal(title));
            kpisTransVO.setLine3(jsonArray.getJSONObject(1).getBigDecimal(title));
            kpisTransVO.setLine4(jsonArray.getJSONObject(2).getBigDecimal(title));
            kpisTransVO.setLine5(jsonArray.getJSONObject(3).getBigDecimal(title));
            returnList.add(kpisTransVO);
        }

        return returnList;
    }

    public List<PyFileVO> getPyFileList(String createUser) {
        List<PyFile> list=pyFileMapper.getPyFileList(createUser);
        List<PyFileVO> returnList=JSON.parseArray(JSON.toJSONString(list),PyFileVO.class);
        return returnList;
    }

    private static final String basePath="D:\\citylearn-demo\\citylearnpy\\";

    public String getPyFile(String id) {
        PyFile pyFile = pyFileMapper.selectById(id);

        try {
            return new String(Files.readAllBytes(Paths.get(basePath+pyFile.getFileName())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void savePyFile(PyFileParam pyFileParam) throws Exception {
        PyFile pyFile = pyFileMapper.selectById(pyFileParam.getId());

        if(null!=pyFileParam.getFileName()){
            List<PyFile> list=pyFileMapper.selectList(new QueryWrapper<PyFile>().lambda()
                    .eq(PyFile::getFileName,pyFileParam.getFileName())
                    .ne(PyFile::getId,pyFile.getId()));
            if(null!=list && list.size()>0){
                throw new Exception("文件名已存在");
            }

            Path source = Paths.get(basePath+pyFile.getFileName());
            Path target = Paths.get(basePath+pyFileParam.getFileName());
            try {
                Files.move(source, target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            pyFile.setFileName(pyFileParam.getFileName());
            pyFileMapper.updateById(pyFile);

        }else if(null!=pyFileParam.getDescription()){
            pyFile.setDescription(pyFileParam.getDescription());
            pyFileMapper.updateById(pyFile);
        }else{
            Files.write(Paths.get(basePath+pyFile.getFileName()),pyFileParam.getCode().getBytes());
        }



    }

    public PyFileVO addPyFile() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date newDate = new Date();
        PyFile pyFile=new PyFile();
        pyFile.setId(sdf.format(newDate));
        pyFile.setFileName(pyFile.getId()+".py");
        pyFile.setDescription("无");
        pyFile.setCreateUser("admin");
        pyFile.setIfSystem(false);
        pyFile.setCreateTime(newDate);
        pyFileMapper.insert(pyFile);
        Files.createFile(Paths.get(basePath+pyFile.getFileName()));
        return JSON.parseObject(JSON.toJSONString(pyFile),PyFileVO.class);
    }

    public void deletePyFile(PyFileParam pyFileParam) {
        pyFileMapper.deleteById(pyFileParam.getId());
    }

    @FunctionalInterface
    interface BiConsumer<T1,T2>{
        void accept(T1 a, T2 b);
    }

    @FunctionalInterface
    interface TriConsumer<T1,T2,T3>{
        void accept(T1 a, T2 b,T3 c);
    }



    private static BigDecimal stringToBigDecimal(String value){
        if(null==value || "".equals(value) || "NaN".equals(value)){
            return null;
        }
        return new BigDecimal(value);
    }

    private static Map<String, BiConsumer<Kpis,String>> kpiConsumerMap = new HashMap<String, BiConsumer<Kpis,String>>(){{
        put("all_time_peak_average", (kpis,value) -> kpis.setAllTimePeakAverage(stringToBigDecimal(value)));
        put("annual_normalized_unserved_energy_total", (kpis,value) -> kpis.setAnnualNormalizedUnservedEnergyTotal(stringToBigDecimal(value)));
        put("carbon_emissions_total", (kpis,value) -> kpis.setCarbonEmissionsTotal(stringToBigDecimal(value)));
        put("cost_total", (kpis,value) -> kpis.setCostTotal(stringToBigDecimal(value)));
        put("daily_one_minus_load_factor_average", (kpis,value) -> kpis.setDailyOneMinusLoadFactorAverage(stringToBigDecimal(value)));
        put("daily_peak_average", (kpis,value) -> kpis.setDailyPeakAverage(stringToBigDecimal(value)));
        put("discomfort_cold_delta_average", (kpis,value) -> kpis.setDiscomfortColdDeltaAverage(stringToBigDecimal(value)));
        put("discomfort_cold_delta_maximum", (kpis,value) -> kpis.setDiscomfortColdDeltaMaximum(stringToBigDecimal(value)));
        put("discomfort_cold_delta_minimum", (kpis,value) -> kpis.setDiscomfortColdDeltaMinimum(stringToBigDecimal(value)));
        put("discomfort_cold_proportion", (kpis,value) -> kpis.setDiscomfortColdProportion(stringToBigDecimal(value)));
        put("discomfort_hot_delta_average", (kpis,value) -> kpis.setDiscomfortHotDeltaAverage(stringToBigDecimal(value)));
        put("discomfort_hot_delta_maximum", (kpis,value) -> kpis.setDiscomfortHotDeltaMaximum(stringToBigDecimal(value)));
        put("discomfort_hot_delta_minimum", (kpis,value) -> kpis.setDiscomfortHotDeltaMinimum(stringToBigDecimal(value)));
        put("discomfort_hot_proportion", (kpis,value) -> kpis.setDiscomfortHotProportion(stringToBigDecimal(value)));
        put("discomfort_proportion", (kpis,value) -> kpis.setDiscomfortProportion(stringToBigDecimal(value)));
        put("electricity_consumption_total", (kpis,value) -> kpis.setElectricityConsumptionTotal(stringToBigDecimal(value)));
        put("monthly_one_minus_load_factor_average", (kpis,value) -> kpis.setMonthlyOneMinusLoadFactorAverage(stringToBigDecimal(value)));
        put("one_minus_thermal_resilience_proportion", (kpis,value) -> kpis.setOneMinusThermalResilienceProportion(stringToBigDecimal(value)));
        put("power_outage_normalized_unserved_energy_total", (kpis,value) -> kpis.setPowerOutageNormalizedUnservedEnergyTotal(stringToBigDecimal(value)));
        put("ramping_average", (kpis,value) -> kpis.setRampingAverage(stringToBigDecimal(value)));
        put("zero_net_energy", (kpis,value) -> kpis.setZeroNetEnergy(stringToBigDecimal(value)));

    }};

    private static final TriConsumer<String,List<Kpis>,List<String>> kpiConsumer = (kpiName ,kpiList, valueList) -> {
        if(null!=kpiConsumerMap.get(kpiName)) {
            for(int i=0;i<4;i++) {
                kpiConsumerMap.get(kpiName).accept(kpiList.get(i), valueList.get(i));
            }
        }

    };

    private static final Map<String,String> scriptMap=new HashMap<String,String>(){{
        put("baseLine","NOCONTROL.py");
        put("diSac","DISAC.py");
        put("multi","Multi-agent.py");
        put("single","Single-agent.py");
    }};
    public void updateLearn(String agentType) {
        String baseScriptPath="D:/Users/clfbe/anaconda3/envs/cl2/Lib/site-packages/citylearn/apply/";

        String fileName=scriptMap.get(agentType);
        if(null==fileName){
            return;
        }
        String scriptPath=baseScriptPath+fileName;

        //D:\Users\clfbe\anaconda3\envs\cl2\Lib\site-packages\citylearn\apply\NOCONTROL.py
        String result=executePythonScript(scriptPath);

        List<Kpis> kpisList=new ArrayList<>();
        kpisList.add(new Kpis(){{setName("Building_1");setAgentType(agentType);}});
        kpisList.add(new Kpis(){{setName("Building_2");setAgentType(agentType);}});
        kpisList.add(new Kpis(){{setName("Building_3");setAgentType(agentType);}});
        kpisList.add(new Kpis(){{setName("District");setAgentType(agentType);}});
        try (BufferedReader reader = new BufferedReader(new StringReader(result))) {
            String line;
            int flag=0;
            while ((line = reader.readLine()) != null) {
                if("outputkpi".equals( line)){
                    flag=1;
                    continue;
                }
                if(flag==1){
                    line=line.replaceAll("\\s+", " ");
                    String[] params=line.split(" ");
                    System.out.println(JSON.toJSONString(params));
                    kpiConsumer.accept(params[0],kpisList, Stream.of(params).skip(1).collect(Collectors.toList()));
                }
                // 处理每一行
                //System.out.println("第 " + (++lineNumber) + " 行: " + line);
            }
            System.out.println(JSON.toJSONString(kpisList));
            kpisMapper.delete(new QueryWrapper<Kpis>()
                    .lambda().eq(Kpis::getAgentType,agentType));

            kpisMapper.insert(kpisList);
        } catch (IOException e) {
            throw new RuntimeException("读取字符串时发生异常", e);
        }
    }

    public String executePythonScript(String scriptPath) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_PATH, scriptPath);
            pb.redirectErrorStream(true); // 合并标准输出和错误输出
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor(); // 等待脚本执行完成
            if (exitCode != 0) {
                throw new RuntimeException("Python脚本执行失败，退出码：" + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("执行Python脚本时发生异常", e);
        }
        System.out.println(output.toString());
        try {
            Files.write(Paths.get("D:/output.txt"), output.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return output.toString().trim(); // 去除末尾换行符
    }
}
