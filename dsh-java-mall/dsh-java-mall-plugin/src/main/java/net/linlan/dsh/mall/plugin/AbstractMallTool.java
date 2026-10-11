package net.linlan.dsh.mall.plugin;

import net.linlan.dsh.domain.model.entity.AbstractTool;
import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import java.util.concurrent.CompletableFuture;

abstract class AbstractMallTool extends AbstractTool {
    final MallApiClient client;

    AbstractMallTool(MallApiClient client) {
        this.client = client;
    }

    @Override
    public boolean isConcurrencySafe(Object args) {
        return true;
    }

    protected CompletableFuture<ToolExecutionResult> callMall(String path) {
        try {
            return ok(client.get(path));
        } catch (Throwable throwable) {
            return fail(throwable.getMessage(), "MALL_API_ERROR");
        }
    }

    protected CompletableFuture<ToolExecutionResult> callMallPost(String path, String jsonBody) {
        try {
            return ok(client.postJson(path, jsonBody));
        } catch (Throwable throwable) {
            return fail("POST " + path + " 失败: " + throwable.getMessage(), "MALL_API_ERROR");
        }
    }

    protected CompletableFuture<ToolExecutionResult> callMallPut(String path, String jsonBody) {
        try {
            return ok(client.putJson(path, jsonBody));
        } catch (Throwable throwable) {
            return fail("PUT " + path + " 失败: " + throwable.getMessage(), "MALL_API_ERROR");
        }
    }

    protected CompletableFuture<ToolExecutionResult> callMallDelete(String path) {
        try {
            return ok(client.delete(path));
        } catch (Throwable throwable) {
            return fail("DELETE " + path + " 失败: " + throwable.getMessage(), "MALL_API_ERROR");
        }
    }
}