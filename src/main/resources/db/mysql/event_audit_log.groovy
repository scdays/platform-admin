package db.mysql

databaseChangeLog(logicalFilePath: 'event_audit_log.groovy') {
    changeSet(id: '2026-06-27-pa-create-event_audit_log', author: 'platform-admin') {
        createTable(tableName: 'event_audit_log', remarks: '事件中心操作审计日志') {
            column(name: 'id', type: 'BIGINT', autoIncrement: true, remarks: '主键') {
                constraints(primaryKey: true, primaryKeyName: 'pk_event_audit_log', nullable: false)
            }
            column(name: 'operator_id', type: 'VARCHAR(64)', remarks: '操作人ID')
            column(name: 'operator_name', type: 'VARCHAR(128)', remarks: '操作人名称')
            column(name: 'operation_type', type: 'VARCHAR(32)', remarks: '操作类型：PUBLISH/REPLAY') {
                constraints(nullable: false)
            }
            column(name: 'target_type', type: 'VARCHAR(32)', remarks: '目标类型：EVENT/DEAD_LETTER') {
                constraints(nullable: false)
            }
            column(name: 'target_id', type: 'VARCHAR(64)', remarks: '目标ID（eventId）') {
                constraints(nullable: false)
            }
            column(name: 'result', type: 'VARCHAR(16)', remarks: '操作结果：SUCCESS/FAILED')
            column(name: 'detail', type: 'TEXT', remarks: '操作详情JSON')
            column(name: 'client_ip', type: 'VARCHAR(64)', remarks: '客户端IP')
            column(name: 'created_time', type: 'DATETIME', remarks: '创建时间')
            column(name: 'updated_time', type: 'DATETIME', remarks: '更新时间')
        }
        createIndex(tableName: 'event_audit_log', indexName: 'idx_operator_id', unique: false) {
            column(name: 'operator_id')
        }
        createIndex(tableName: 'event_audit_log', indexName: 'idx_operation_type', unique: false) {
            column(name: 'operation_type')
        }
        createIndex(tableName: 'event_audit_log', indexName: 'idx_target_id', unique: false) {
            column(name: 'target_id')
        }
        createIndex(tableName: 'event_audit_log', indexName: 'idx_created_time', unique: false) {
            column(name: 'created_time')
        }
    }
}