package db.mysql

databaseChangeLog(logicalFilePath: 'event_dead_letter.groovy') {
    changeSet(id: '2026-06-25-pa-create-event_dead_letter', author: 'platform-admin') {
        createTable(tableName: 'event_dead_letter', remarks: '死信事件索引') {
            column(name: 'id', type: 'BIGINT', autoIncrement: true, remarks: '主键') {
                constraints(primaryKey: true, primaryKeyName: 'pk_event_dead_letter', nullable: false)
            }
            column(name: 'event_id', type: 'VARCHAR(64)', remarks: '事件ID')
            column(name: 'event_type', type: 'VARCHAR(128)', remarks: '事件类型')
            column(name: 'origin_topic', type: 'VARCHAR(128)', remarks: '原始Topic') {
                constraints(nullable: false)
            }
            column(name: 'dlq_topic', type: 'VARCHAR(128)', remarks: 'DLQ Topic')
            column(name: 'consumer_group', type: 'VARCHAR(128)', remarks: '消费者组')
            column(name: 'source_service', type: 'VARCHAR(128)', remarks: '生产者服务')
            column(name: 'failed_service', type: 'VARCHAR(128)', remarks: '失败服务')
            column(name: 'failed_handler', type: 'VARCHAR(256)', remarks: '失败处理器')
            column(name: 'failed_reason', type: 'TEXT', remarks: '失败原因')
            column(name: 'payload', type: 'TEXT', remarks: '原始载荷')
            column(name: 'headers', type: 'TEXT', remarks: '请求头')
            column(name: 'retry_count', type: 'INT', defaultValueNumeric: 0, remarks: '重试次数')
            column(name: 'dead_time', type: 'DATETIME', remarks: '死信时间')
            column(name: 'replay_status', type: 'VARCHAR(32)', remarks: '重放状态')
            column(name: 'created_time', type: 'DATETIME', remarks: '创建时间')
            column(name: 'updated_time', type: 'DATETIME', remarks: '更新时间')
        }
        createIndex(tableName: 'event_dead_letter', indexName: 'idx_event_id', unique: false) {
            column(name: 'event_id')
        }
        createIndex(tableName: 'event_dead_letter', indexName: 'idx_event_type', unique: false) {
            column(name: 'event_type')
        }
        createIndex(tableName: 'event_dead_letter', indexName: 'idx_origin_topic', unique: false) {
            column(name: 'origin_topic')
        }
        createIndex(tableName: 'event_dead_letter', indexName: 'idx_consumer_group', unique: false) {
            column(name: 'consumer_group')
        }
        createIndex(tableName: 'event_dead_letter', indexName: 'idx_dead_time', unique: false) {
            column(name: 'dead_time')
        }
    }
}
