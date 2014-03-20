package jpacker.proxy;

public interface ProxyFactory {
	public <T> T getProxyObject(Class<T> type);
}
