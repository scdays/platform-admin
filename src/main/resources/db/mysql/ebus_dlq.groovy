package db.mysql

databaseChangeLog(logicalFilePath: 'ebus_dlq.groovy') {
    changeSet(id: '2026-06-25-pa-create-ebus_dlq', author: 'platform-admin') {
        createTable(tableName: 'ebus_dlq', remarks: 'EventBus 死信事件存储') {
            column(name: 'id', type: 'BIGINT', autoIncrement: true, remarks: '主键') {
                constraints(primaryKey: true, primaryKeyName: 'pk_ebus_dlq', nullable: false)
            }
            column(name: 'event_id', type: 'VARCHAR(64)', remarks: '事件ID') {
                constraints(nullable: false)
            }
            column(name: 'event_type', type: 'VARCHAR(128)', remarks: '事件类型') {
                constraints(nullable: false)
            }
            column(name: 'origin_topic', type: 'VARCHAR(128)', remarks: '原始Topic') {
                constraints(nullable: false)
            }
            column(name: 'consumer_group', type: 'VARCHAR(128)', remarks: '消费者组') {
                constraints(nullable: false)
            }
            column(name: 'source_service', type: 'VARCHAR(128)', remarks: '来源服务')
            column(name: 'failed_service', type: 'VARCHAR(128)', remarks: '失败服务')
            column(name: 'failed_handler', type: 'VARCHAR(256)', remarks: '失败处理器')
            column(name: 'error_message', type: 'TEXT', remarks: '失败原因')
            column(name: 'error_stack_digest', type: 'TEXT', remarks: '错误堆栈摘要')
            column(name: 'origin_message', type: 'LONGTEXT', remarks: '原始消息')
            column(name: 'dead_time', type: 'BIGINT', remarks: '进入死信时间（毫秒时间戳）')
            column(name: 'retry_count', type: 'INT', remarks: '重试次数')
            column(name: 'replay_status', type: 'VARCHAR(32)', remarks: '重放状态：PENDING / REPLAYED / FAILED')
        }
        createIndex(tableName: 'ebus_dlq', indexName: 'idx_event_id', unique: false) {
            column(name: 'event_id')
        }
        createIndex(tableName: 'ebus_dlq', indexName: 'idx_origin_topic_dead_time', unique: false) {
            column(name: 'origin_topic')
            column(name: 'dead_time')
        }
    }
}
