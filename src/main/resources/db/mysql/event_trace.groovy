package db.mysql

databaseChangeLog(logicalFilePath: 'event_trace.groovy') {
    changeSet(id: '2026-06-25-pa-create-event_trace', author: 'platform-admin') {
        createTable(tableName: 'event_trace', remarks: '事件发布轨迹') {
            column(name: 'id', type: 'BIGINT', autoIncrement: true, remarks: '主键') {
                constraints(primaryKey: true, primaryKeyName: 'pk_event_trace', nullable: false)
            }
            column(name: 'event_id', type: 'VARCHAR(64)', remarks: '事件ID') {
                constraints(nullable: false)
            }
            column(name: 'event_type', type: 'VARCHAR(128)', remarks: '事件类型') {
                constraints(nullable: false)
            }
            column(name: 'event_version', type: 'VARCHAR(32)', remarks: '事件版本')
            column(name: 'topic', type: 'VARCHAR(128)', remarks: 'Topic') {
                constraints(nullable: false)
            }
            column(name: 'domain', type: 'VARCHAR(64)', remarks: '业务域')
            column(name: 'source_service', type: 'VARCHAR(128)', remarks: '生产者服务')
            column(name: 'tenant_type', type: 'VARCHAR(64)', remarks: '租户类型')
            column(name: 'tenant_id', type: 'VARCHAR(128)', remarks: '租户ID')
            column(name: 'subject_type', type: 'VARCHAR(64)', remarks: '事件主体类型')
            column(name: 'subject_id', type: 'VARCHAR(128)', remarks: '事件主体ID')
            column(name: 'trace_id', type: 'VARCHAR(128)', remarks: '链路追踪ID')
            column(name: 'idempotent_key', type: 'VARCHAR(256)', remarks: '业务幂等键')
            column(name: 'occurred_at', type: 'DATETIME', remarks: '事件发生时间')
            column(name: 'published_at', type: 'DATETIME', remarks: '发布时间')
            column(name: 'payload_digest', type: 'VARCHAR(128)', remarks: '载荷摘要')
            column(name: 'payload', type: 'LONGTEXT', remarks: '完整事件载荷JSON')
            column(name: 'status', type: 'VARCHAR(32)', remarks: '状态')
            column(name: 'created_time', type: 'DATETIME', remarks: '创建时间')
            column(name: 'updated_time', type: 'DATETIME', remarks: '更新时间')
        }
        addUniqueConstraint(tableName: 'event_trace', columnNames: 'event_id', constraintName: 'uk_event_id')
        createIndex(tableName: 'event_trace', indexName: 'idx_event_type', unique: false) {
            column(name: 'event_type')
        }
        createIndex(tableName: 'event_trace', indexName: 'idx_topic', unique: false) {
            column(name: 'topic')
        }
        createIndex(tableName: 'event_trace', indexName: 'idx_trace_id', unique: false) {
            column(name: 'trace_id')
        }
        createIndex(tableName: 'event_trace', indexName: 'idx_tenant', unique: false) {
            column(name: 'tenant_type')
            column(name: 'tenant_id')
        }
        createIndex(tableName: 'event_trace', indexName: 'idx_subject', unique: false) {
            column(name: 'subject_type')
            column(name: 'subject_id')
        }
        createIndex(tableName: 'event_trace', indexName: 'idx_created_time', unique: false) {
            column(name: 'created_time')
        }
    }
}
