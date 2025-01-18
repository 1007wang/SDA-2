package com.sda_2.Mappers;

import com.sda_2.Config.MapTypeHandler;
import com.sda_2.DTO.PopulationData;
import com.sda_2.DTO.QuarterPopulationData;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PopulationMapper {

    @Select("SELECT * FROM population_data1")
    @Results({
        @Result(property = "geography", column = "Geography"),
        @Result(property = "quarterData", column = "{resultSet=_parameter}", typeHandler = MapTypeHandler.class)
    })
    List<PopulationData> findAll();

    @Select("SELECT * FROM population_data1 WHERE Geography = #{geography}")
    @Results({
        @Result(property = "geography", column = "Geography"),
        @Result(property = "quarterData", column = "{resultSet=_parameter}", typeHandler = MapTypeHandler.class)
    })
    PopulationData findByGeography(@Param("geography") String geography);

    // 查询某一季度所有地区的总人口
    @SelectProvider(type = PopulationSqlProvider.class, method = "findTotalByQuarter")
    Long findTotalByQuarter(@Param("quarter") String quarter);

    // 根据季度查询各地区人口数据
    @SelectProvider(type = PopulationSqlProvider.class, method = "findByQuarter")
    List<QuarterPopulationData> findByQuarter(@Param("quarter") String quarter);
}
