package com.xiaoyu.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 数据库Schema修复器
 * 启动时检查并修复缺失的列
 */
@Component
public class DatabaseSchemaFixer implements CommandLineRunner {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        fixOrderItemTable();
    }

    private void fixOrderItemTable() {
        try {
            // 检查 order_item 表是否有 goods_id 列
            String checkSql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() " +
                    "AND TABLE_NAME = 'order_item' " +
                    "AND COLUMN_NAME = 'goods_id'";

            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            if (count != null && count == 0) {
                // 列不存在，添加它
                String alterSql = "ALTER TABLE order_item ADD COLUMN goods_id BIGINT AFTER order_no";
                jdbcTemplate.execute(alterSql);
                System.out.println("=== 已添加 order_item.goods_id 列 ===");
            } else {
                System.out.println("=== order_item.goods_id 列已存在 ===");
            }
        } catch (Exception e) {
            System.err.println("=== 修复 order_item 表失败: " + e.getMessage() + " ===");
        }
    }
}
