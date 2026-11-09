/**
 * Invocation 调用记录领域。
 * <p>
 * 职责：API 调用记录、限流命中、调用统计。
 * <p>
 * 消费 {@code InvocationRecordedEvent}，按 invocationId 幂等写入 api_invocation。
 *
 * @author asset-security
 */
package com.vtc.platformadmin.domain.invocation;