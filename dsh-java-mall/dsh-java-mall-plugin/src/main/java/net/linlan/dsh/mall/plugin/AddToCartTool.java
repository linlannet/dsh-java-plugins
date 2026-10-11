package net.linlan.dsh.mall.plugin;

import net.linlan.dsh.domain.model.entity.ToolExecutionResult;
import net.linlan.dsh.domain.model.entity.ToolRunContext;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class AddToCartTool extends AbstractMallTool {
    AddToCartTool(MallApiClient client) {
        super(client);
    }

    @Override
    public String name() {
        return "add_to_cart";
    }

    @Override
    public String description() {
        return "将指定商品加入用户的虚拟购物车，支持指定数量（1-10 件）。加购前建议先通过 search_products 或 get_product 确认商品 ID 和库存。";
    }

    @Override
    public Map<String, Object> parameters() {
        return objectSchema()
                .prop("customerId", stringSchema("商城上下文中的当前登录用户 ID"))
                .prop("productId", stringSchema("商品 ID，例如 p001，必须先通过 search_products 或 get_product 获取"))
                .prop("quantity", stringSchema("购买数量，整数，范围 1-10，默认 1"))
                .required("customerId", "productId")
                .build();
    }

    @Override
    protected CompletableFuture<ToolExecutionResult> run(Map<String, Object> args, ToolRunContext ctx) {
        String customerId = str(args, "customerId", "").trim();
        if (customerId.isBlank()) {
            return fail("customerId 不能为空", "INVALID_ARGUMENT");
        }
        String productId = str(args, "productId", "").trim();
        if (productId.isBlank()) {
            return fail("productId 不能为空", "INVALID_ARGUMENT");
        }
        int quantity = parseQuantity(str(args, "quantity", "1"));
        if (quantity < 1 || quantity > 10) {
            return fail("数量必须在 1-10 之间", "INVALID_ARGUMENT");
        }
        String body = "{\"productId\":\"" + escapeJson(productId) + "\",\"quantity\":" + quantity + "}";
        return callMallPost("/api/mall/cart/items?customerId=" + customerId, body);
    }

    private static int parseQuantity(String raw) {
        try {
            int value = Integer.parseInt(raw.trim());
            return value;
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}