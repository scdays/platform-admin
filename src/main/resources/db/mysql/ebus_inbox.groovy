package db.mysql

databaseChangeLog(logicalFilePath: 'ebus_inbox.groovy') {
    changeSet(id: '2026-06-25-pa-create-ebus_inbox', author: 'platform-admin') {
        createTable(tableName: 'ebus_inbox', remarks: '事件消费幂等表') {
            column(name: 'id', type: 'BIGINT', autoIncrement: true, remarks: '主键') {
                constraints(primaryKey: true, primaryKeyName: 'pk_ebus_inbox', nullable: false)
            }
            column(name: 'event_id', type: 'VARCHAR(64)', remarks: '事件ID') {
                constraints(nullable: false)
            }
            column(name: 'event_type', type: 'VARCHAR(128)', remarks: '事件类型') {
                constraints(nullable: false)
            }
            column(name: 'consumer_group', type: 'VARCHAR(128)', remarks: '消费组') {
                constraints(nullable: false)
            }
            column(name: 'idempotent_key', type: 'VARCHAR(256)', remarks: '业务幂等键')
            column(name: 'status', type: 'VARCHAR(32)', remarks: '消费状态') {
                constraints(nullable: false)
            }
            column(name: 'consumed_time', type: 'BIGINT', remarks: '消费时间（毫秒时间戳）')
            column(name: 'created_time', type: 'BIGINT', remarks: '创建时间（毫秒时间戳）')
        }
        addUniqueConstraint(tableName: 'ebus_inbox', columnNames: 'event_id, consumer_group', constraintName: 'uk_ebus_inbox_event_id_group')
        addUniqueConstraint(tableName: 'ebus_inbox', columnNames: 'idempotent_key, consumer_group', constraintName: 'uk_ebus_inbox_idempotent_key_group')
        createIndex(tableName: 'ebus_inbox', indexName: 'idx_ebus_inbox_status', unique: false) {
            column(name: 'status')
        }
        createIndex(tableName: 'ebus_inbox', indexName: 'idx_ebus_inbox_created_time', unique: false) {
            column(name: 'created_time')
        }
    }
}
