package db.mysql

databaseChangeLog(logicalFilePath: 'ebus_outbox.groovy') {
    changeSet(id: '2026-06-25-pa-create-ebus_outbox', author: 'platform-admin') {
        createTable(tableName: 'ebus_outbox', remarks: '出箱事件') {
            column(name: 'event_id', type: 'VARCHAR(64)', remarks: '事件ID') {
                constraints(primaryKey: true, primaryKeyName: 'pk_ebus_outbox', nullable: false)
            }
            column(name: 'event_type', type: 'VARCHAR(128)', remarks: '事件类型') {
                constraints(nullable: false)
            }
            column(name: 'topic', type: 'VARCHAR(128)', remarks: '目标Topic') {
                constraints(nullable: false)
            }
            column(name: 'partition_key', type: 'VARCHAR(128)', remarks: 'MQ分区键')
            column(name: 'idempotent_key', type: 'VARCHAR(256)', remarks: '业务幂等键')
            column(name: 'source_service', type: 'VARCHAR(128)', remarks: '来源服务')
            column(name: 'payload', type: 'LONGTEXT', remarks: '事件信封JSON')
            column(name: 'headers', type: 'LONGTEXT', remarks: '头信息JSON')
            column(name: 'status', type: 'VARCHAR(32)', remarks: '状态') {
                constraints(nullable: false)
            }
            column(name: 'retry_count', type: 'INT', defaultValueNumeric: 0, remarks: '重试次数')
            column(name: 'next_retry_time', type: 'DATETIME', remarks: '下次重试时间')
            column(name: 'last_error', type: 'TEXT', remarks: '最近一次失败信息')
            column(name: 'created_time', type: 'DATETIME', remarks: '创建时间') {
                constraints(nullable: false)
            }
            column(name: 'updated_time', type: 'DATETIME', remarks: '更新时间') {
                constraints(nullable: false)
            }
        }
        createIndex(tableName: 'ebus_outbox', indexName: 'idx_status', unique: false) {
            column(name: 'status')
        }
        createIndex(tableName: 'ebus_outbox', indexName: 'idx_created_time', unique: false) {
            column(name: 'created_time')
        }
    }
}
