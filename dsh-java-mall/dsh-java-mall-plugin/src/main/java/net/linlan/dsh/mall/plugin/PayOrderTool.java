package net.linlan.dsh.mall.plugin;

import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class PayOrderTool extends AbstractMallTool {
    PayOrderTool(MallApiClient client) {
        super(client);
    }

    @Override
    public String name() {
        return "pay_order";
    }

    @Override
    public String description() {
        return "对已提交的虚拟订单执行支付操作。需提供订单号和支付方式。支持的支付方式：WECHAT（微信）、ALIPAY（支付宝）、BANK_CARD（银行卡）。支付成功后自动生成物流信息。";
    }

    @Override
    public Map<String, Object> parameters() {
        return objectSchema()
                .prop("customerId", stringSchema("商城上下文中的当前登录用户 ID"))
                .prop("orderNo", stringSchema("商城订单号，例如 MD20260909102500"))
                .prop("paymentMethod", stringSchema("支付方式：WECHAT、ALIPAY、BANK_CARD，留空则使用下单时选择的方式"))
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
        String paymentMethod = str(args, "paymentMethod", "").trim();

        String body = paymentMethod.isBlank()
                ? "{}"
                : "{\"paymentMethod\":\"" + paymentMethod + "\"}";
        return callMallPost("/api/mall/orders/" + orderNo + "/payment?customerId=" + customerId, body);
    }
}