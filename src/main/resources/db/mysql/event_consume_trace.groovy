package db.mysql

databaseChangeLog(logicalFilePath: 'event_consume_trace.groovy') {
    changeSet(id: '2026-06-25-pa-create-event_consume_trace', author: 'platform-admin') {
        createTable(tableName: 'event_consume_trace', remarks: '事件消费轨迹') {
            column(name: 'id', type: 'BIGINT', autoIncrement: true, remarks: '主键') {
                constraints(primaryKey: true, primaryKeyName: 'pk_event_consume_trace', nullable: false)
            }
            column(name: 'event_id', type: 'VARCHAR(64)', remarks: '事件ID') {
                constraints(nullable: false)
            }
            column(name: 'event_type', type: 'VARCHAR(128)', remarks: '事件类型') {
                constraints(nullable: false)
            }
            column(name: 'topic', type: 'VARCHAR(128)', remarks: 'Topic') {
                constraints(nullable: false)
            }
            column(name: 'consumer_service', type: 'VARCHAR(128)', remarks: '消费者服务') {
                constraints(nullable: false)
            }
            column(name: 'consumer_group', type: 'VARCHAR(128)', remarks: '消费者组') {
                constraints(nullable: false)
            }
            column(name: 'handler', type: 'VARCHAR(256)', remarks: '处理器')
            column(name: 'consume_status', type: 'VARCHAR(32)', remarks: '消费状态') {
                constraints(nullable: false)
            }
            column(name: 'retry_count', type: 'INT', defaultValueNumeric: 0, remarks: '重试次数')
            column(name: 'last_error', type: 'TEXT', remarks: '最后错误信息')
            column(name: 'started_at', type: 'DATETIME', remarks: '开始时间')
            column(name: 'finished_at', type: 'DATETIME', remarks: '结束时间')
            column(name: 'created_time', type: 'DATETIME', remarks: '创建时间')
            column(name: 'updated_time', type: 'DATETIME', remarks: '更新时间')
        }
        createIndex(tableName: 'event_consume_trace', indexName: 'idx_event_id', unique: false) {
            column(name: 'event_id')
        }
        createIndex(tableName: 'event_consume_trace', indexName: 'idx_event_type', unique: false) {
            column(name: 'event_type')
        }
        createIndex(tableName: 'event_consume_trace', indexName: 'idx_consumer_group', unique: false) {
            column(name: 'consumer_group')
        }
        createIndex(tableName: 'event_consume_trace', indexName: 'idx_status', unique: false) {
            column(name: 'consume_status')
        }
        createIndex(tableName: 'event_consume_trace', indexName: 'idx_created_time', unique: false) {
            column(name: 'created_time')
        }
    }
}
