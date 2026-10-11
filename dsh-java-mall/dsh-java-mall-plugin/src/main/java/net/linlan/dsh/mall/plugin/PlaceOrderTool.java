package net.linlan.dsh.mall.plugin;

import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class PlaceOrderTool extends AbstractMallTool {
    PlaceOrderTool(MallApiClient client) {
        super(client);
    }

    @Override
    public String name() {
        return "place_order";
    }

    @Override
    public String description() {
        return "根据购物车内容提交虚拟订单。调用前必须先通过 add_to_cart 加购商品。必须提供完整的收件人、手机号、收货地址和支付方式。支持的支付方式：WECHAT（微信）、ALIPAY（支付宝）、BANK_CARD（银行卡）。";
    }

    @Override
    public Map<String, Object> parameters() {
        return objectSchema()
                .prop("customerId", stringSchema("商城上下文中的当前登录用户 ID"))
                .prop("receiver", stringSchema("收件人姓名"))
                .prop("phone", stringSchema("收件手机号"))
                .prop("address", stringSchema("详细收货地址"))
                .prop("paymentMethod", stringSchema("支付方式：WECHAT、ALIPAY、BANK_CARD"))
                .prop("remark", stringSchema("订单备注，可选"))
                .required("customerId", "receiver", "phone", "address", "paymentMethod")
                .build();
    }

    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext ctx) {
        String customerId = str(args, "customerId", "").trim();
        if (customerId.isBlank()) {
            return fail("customerId 不能为空", "INVALID_ARGUMENT");
        }
        String receiver = str(args, "receiver", "").trim();
        if (receiver.isBlank()) {
            return fail("收件人不能为空", "INVALID_ARGUMENT");
        }
        String phone = str(args, "phone", "").trim();
        if (phone.isBlank()) {
            return fail("手机号不能为空", "INVALID_ARGUMENT");
        }
        String address = str(args, "address", "").trim();
        if (address.isBlank()) {
            return fail("收货地址不能为空", "INVALID_ARGUMENT");
        }
        String paymentMethod = str(args, "paymentMethod", "").trim();
        if (paymentMethod.isBlank()) {
            return fail("支付方式不能为空", "INVALID_ARGUMENT");
        }
        String remark = str(args, "remark", "").trim();

        String escapedReceiver = escapeJson(receiver);
        String escapedPhone = escapeJson(phone);
        String escapedAddress = escapeJson(address);
        String escapedRemark = escapeJson(remark);

        String body = "{"
                + "\"receiver\":\"" + escapedReceiver + "\","
                + "\"phone\":\"" + escapedPhone + "\","
                + "\"address\":\"" + escapedAddress + "\","
                + "\"paymentMethod\":\"" + paymentMethod + "\""
                + (remark.isBlank() ? "" : ",\"remark\":\"" + escapedRemark + "\"")
                + "}";
        return callMallPost("/api/mall/orders?customerId=" + customerId, body);
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}