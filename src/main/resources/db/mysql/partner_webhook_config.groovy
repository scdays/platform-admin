package db.mysql

databaseChangeLog(logicalFilePath: 'partner_webhook_config.groovy') {
    changeSet(id: '2026-06-25-pa-create-partner_webhook_config', author: 'platform-admin') {
        createTable(tableName: 'partner_webhook_config', remarks: '合作方 Webhook 配置') {
            column(name: 'id', type: 'BIGINT', autoIncrement: true, remarks: '主键') {
                constraints(primaryKey: true, primaryKeyName: 'pk_partner_webhook_config', nullable: false)
            }
            column(name: 'partner_id', type: 'VARCHAR(64)', remarks: '合作方ID') {
                constraints(nullable: false)
            }
            column(name: 'callback_url', type: 'VARCHAR(512)', remarks: '回调 URL')
            column(name: 'webhook_secret', type: 'VARCHAR(256)', remarks: 'HMAC 签名密钥（明文存储）')
            column(name: 'webhook_secret_hash', type: 'VARCHAR(256)', remarks: 'HMAC 签名密钥哈希')
            column(name: 'downloadable_stages', type: 'VARCHAR(256)', remarks: '允许下载的 exportStage 逗号分隔')
            column(name: 'updated_at', type: 'DATETIME', remarks: '更新时间')
        }
        addUniqueConstraint(tableName: 'partner_webhook_config', columnNames: 'partner_id', constraintName: 'uk_partner_id')
    }
}
