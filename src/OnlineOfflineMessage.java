/**
 * record是Java新推出一种类型，类似Kotlin的data类型
 * 在这里仅仅用于存上线/下线节点数据
 * @param node 用户节点
 */
public record OnlineOfflineMessage(Node node) {
}
