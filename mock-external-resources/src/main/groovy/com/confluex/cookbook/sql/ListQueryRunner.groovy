package com.confluex.cookbook.sql

import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

class ListQueryRunner extends QueryRunner {

    DataSource dataSource

    @Override
    Object query(Connection conn, String sql, ResultSetHandler rsh, Object[] params) throws SQLException {
        sql = parseArguments(sql, params)
        return super.query(conn, sql, rsh, params.flatten() as Object[])
    }

    protected String parseArguments(String sql, Object[] params) {
        def sb = new StringBuffer()
        def m = sql =~ /\?/
        params.eachWithIndex { p, i ->
            if (m.find(i)) {
                if (p instanceof List) {
                    m.appendReplacement(sb, createArgumentTokens(p.size()))
                }
            }
        }
        m.appendTail(sb)
        sb.toString()
    }

    String createArgumentTokens(Integer size) {
        def args = []
        size.times { args << "?" }
        return args.join(",")
    }

}
