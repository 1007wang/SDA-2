package com.sda_2.Mappers;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

import java.util.ArrayList;
import java.util.List;

public class PopulationSqlProvider {

    public String findTotalByQuarter(@Param("quarter") String quarter) {
        return new SQL() {{
            SELECT("SUM(`" + quarter + "`) as total");
            FROM("population_data1");
        }}.toString();
    }

    public String findByQuarter(@Param("quarter") String quarter) {
        return new SQL() {{
            SELECT("Geography as geography, `" + quarter + "` as population");
            FROM("population_data1");
        }}.toString();
    }

    public List<String> getAllQuarters() {
        List<String> quarters = new ArrayList<>();
        for (int year = 1991; year <= 2024; year++) {
            for (int q = 1; q <= 4; q++) {
                if (year == 1991 && q < 4) continue;
                if (year == 2024 && q > 2) break;
                quarters.add(String.format("Q%d %d", q, year));
            }
        }
        return quarters;
    }
}
