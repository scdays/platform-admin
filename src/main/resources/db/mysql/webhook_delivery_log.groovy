package db.mysql

databaseChangeLog(logicalFilePath: 'webhook_delivery_log.groovy') {
    changeSet(id: '2026-06-25-pa-create-webhook_delivery_log', author: 'platform-admin') {
        createTable(tableName: 'webhook_delivery_log', remarks: 'Webhook 投递记录') {
            column(name: 'id', type: 'BIGINT', autoIncrement: true, remarks: '主键') {
                constraints(primaryKey: true, primaryKeyName: 'pk_webhook_delivery_log', nullable: false)
            }
            column(name: 'partner_id', type: 'VARCHAR(64)', remarks: '合作方ID') {
                constraints(nullable: false)
            }
            column(name: 'event_type', type: 'VARCHAR(128)', remarks: '事件类型') {
                constraints(nullable: false)
            }
            column(name: 'event_id', type: 'VARCHAR(64)', remarks: '事件ID') {
                constraints(nullable: false)
            }
            column(name: 'resource_type', type: 'VARCHAR(64)', remarks: '资源类型')
            column(name: 'resource_id', type: 'VARCHAR(128)', remarks: '资源ID')
            column(name: 'resource_ids_json', type: 'TEXT', remarks: '批量实例 vulInfoID 列表 JSON')
            column(name: 'trigger_source', type: 'VARCHAR(32)', remarks: 'FIRST_ATTEMPT / AUTO_RETRY / MANUAL_RETRY')
            column(name: 'payload_json', type: 'TEXT', remarks: '投递载荷 JSON')
            column(name: 'callback_url', type: 'VARCHAR(512)', remarks: '回调 URL')
            column(name: 'http_status', type: 'INT', remarks: 'HTTP 响应状态码')
            column(name: 'retry_count', type: 'INT', defaultValueNumeric: 0, remarks: '重试次数')
            column(name: 'status', type: 'VARCHAR(32)', remarks: '状态: PENDING/SUCCESS/FAILED/RETRY_WAIT/DEAD')
            column(name: 'created_at', type: 'DATETIME', remarks: '创建时间')
            column(name: 'next_retry_at', type: 'DATETIME', remarks: '下次重试时间')
            column(name: 'created_time', type: 'DATETIME', remarks: '创建时间')
            column(name: 'updated_time', type: 'DATETIME', remarks: '更新时间')
        }
        createIndex(tableName: 'webhook_delivery_log', indexName: 'idx_event_id', unique: false) {
            column(name: 'event_id')
        }
        createIndex(tableName: 'webhook_delivery_log', indexName: 'idx_partner_id', unique: false) {
            column(name: 'partner_id')
        }
        createIndex(tableName: 'webhook_delivery_log', indexName: 'idx_status', unique: false) {
            column(name: 'status')
        }
        createIndex(tableName: 'webhook_delivery_log', indexName: 'idx_next_retry_at', unique: false) {
            column(name: 'next_retry_at')
        }
        createIndex(tableName: 'webhook_delivery_log', indexName: 'idx_created_at', unique: false) {
            column(name: 'created_at')
        }
    }

    // 修正：建表 changeset 漏了 resource_ids_json / trigger_source 两列（PO/DO 均有，致 insert 报 Unknown column）。
    // 幂等：列已存在则 MARK_RAN 跳过，兼容「已执行旧建表脚本缺列」与「新环境建表已含列」两种库状态。
    changeSet(id: '2026-06-26-pa-webhook_delivery_log-add-governance-columns', author: 'platform-admin') {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'webhook_delivery_log', columnName: 'trigger_source')
            }
        }
        addColumn(tableName: 'webhook_delivery_log') {
            column(name: 'resource_ids_json', type: 'TEXT', remarks: '批量实例 vulInfoID 列表 JSON')
        }
        addColumn(tableName: 'webhook_delivery_log') {
            column(name: 'trigger_source', type: 'VARCHAR(32)', remarks: 'FIRST_ATTEMPT / AUTO_RETRY / MANUAL_RETRY')
        }
    }
}
