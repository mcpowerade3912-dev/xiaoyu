-- 修复 order_item 表，添加 goods_id 列
-- 如果列已存在，会报错，可以忽略

ALTER TABLE order_item ADD COLUMN goods_id BIGINT AFTER order_no;

-- 验证列是否添加成功
DESCRIBE order_item;
