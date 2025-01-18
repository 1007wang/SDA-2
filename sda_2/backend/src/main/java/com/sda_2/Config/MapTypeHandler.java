package com.sda_2.Config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@MappedTypes(Map.class)
public class MapTypeHandler extends BaseTypeHandler<Map<String, Integer>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Integer> parameter, JdbcType jdbcType)
            throws SQLException {
        throw new UnsupportedOperationException("Setting Map parameters is not supported");
    }

    @Override
    public Map<String, Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        try {
            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            for (int i = 1; i <= columnCount; i++) {
                String colName = metaData.getColumnName(i);
                if (!colName.equalsIgnoreCase("Geography")) {
                    try {
                        int value = rs.getInt(colName);
                        if (!rs.wasNull()) {
                            result.put(colName, value);
                        }
                    } catch (SQLException e) {
                        // 如果这一列不能转换为 int，跳过它
                        continue;
                    }
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error processing result set", e);
        }
        return result;
    }

    @Override
    public Map<String, Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getNullableResult(rs, "");
    }

    @Override
    public Map<String, Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return new HashMap<>();
    }
} 