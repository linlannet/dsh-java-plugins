package net.linlan.dsh.mall.plugin;

import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class CancelOrderTool extends AbstractMallTool {
    CancelOrderTool(MallApiClient client) {
        super(client);
    }

    @Override
    public String name() {
        return "cancel_order";
    }

    @Override
    public String description() {
        return "取消一笔待支付的虚拟订单。仅状态为 CREATED（待支付）的订单可以取消，已支付或已取消的订单无法再次取消。";
    }

    @Override
    public Map<String, Object> parameters() {
        return objectSchema()
                .prop("customerId", stringSchema("商城上下文中的当前登录用户 ID"))
                .prop("orderNo", stringSchema("商城订单号，例如 MD20260909102500"))
                .required("customerId", "orderNo")
                .build();
    }

    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext ctx) {
        String customerId = str(args, "customerId", "").trim();
        if (customerId.isBlank()) {
            return fail("customerId 不能为空", "INVALID_ARGUMENT");
        }
        String orderNo = str(args, "orderNo", "").trim();
        if (orderNo.isBlank()) {
            return fail("orderNo 不能为空", "INVALID_ARGUMENT");
        }
        return callMallPost("/api/mall/orders/" + orderNo + "/cancel?customerId=" + customerId, "{}");
    }
}