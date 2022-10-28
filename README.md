# redis-lock

The most elegant Redis distributed lock implementation.

可能是最优雅的redis分布式锁实现吧，关于锁的实现可参考可重入锁，该分布式锁支持重入机制

## 准备工作

- 在pom中直接引入引入spring-data-redis依赖,也可自行扩展Jedis。

## 加锁模式

- **RedisLock.lock**：直到加锁成功。
- **RedisLock.tryLock**：尝试加锁。

## 使用优点

加锁在一个原子操作中完成，具有redis锁失效，以及本地解锁。 会有人有疑问这样和setNx 加上 失效时间有何区别，下面进行对比。

- redis-lock 支持锁重入，使用方式与 ReentrantLock 类似。
- setNx无法解决当前锁在分布式情况下被另外的客户端解锁的可能。

## 存在缺点

- client1 在Redis一个主节点获得了一个锁。
- 主节点挂了，而主从节点的写同步还没完成（异步复制）。
- 从节点被提升为主节点，client2 就有可能获得和 client1 相同的锁。