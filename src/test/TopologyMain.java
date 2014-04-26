package test;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

/**
 * 功能说明： 设计一个topology，来实现对一个句子里面的单词出现的频率进行统计。 整个topology分为三个部分：
 * WordReader:数据源，负责发送单行文本记录（句子） WordNormalizer:负责将单行文本记录（句子）切分成单词
 * WordCounter:负责对单词的频率进行累加
 * 
 * @author 毛祥溢 Email:frank@maoxiangyi.cn 2013-8-26 下午5:59:06
 */
public class TopologyMain {

	/**
	 * @param args
	 *            文件路径
	 */
	public static void main(String[] args) throws Exception {
		// Storm框架支持多语言，在JAVA环境下创建一个拓扑，需要使用TopologyBuilder进行构建
		TopologyBuilder builder = new TopologyBuilder();
		/*
		 * WordReader类，主要是将文本内容读成一行一行的模式 消息源spout是Storm里面一个topology里面的消息生产者。
		 * 一般来说消息源会从一个外部源读取数据并且向topology里面发出消息：tuple。 Spout可以是可靠的也可以是不可靠的。
		 * 如果这个tuple没有被storm成功处理
		 * ，可靠的消息源spouts可以重新发射一个tuple，但是不可靠的消息源spouts一旦发出一个tuple就不能重发了。
		 * 
		 * 消息源可以发射多条消息流stream。多条消息流可以理解为多中类型的数据。
		 * 使用OutputFieldsDeclarer.declareStream来定义多个stream
		 * ，然后使用SpoutOutputCollector来发射指定的stream。
		 * 
		 * Spout类里面最重要的方法是nextTuple。要么发射一个新的tuple到topology里面或者简单的返回如果已经没有新的tuple。
		 * 要注意的是nextTuple方法不能阻塞，因为storm在同一个线程上面调用所有消息源spout的方法。
		 * 
		 * 另外两个比较重要的spout方法是ack和fail。storm在检测到一个tuple被整个topology成功处理的时候调用ack，
		 * 否则调用fail。storm只对可靠的spout调用ack和fail。
		 */
		builder.setSpout("word-reader", new WordReader());
		/*
		 * WordNormalizer类，主要是将一行一行的文本内容切割成单词
		 * 
		 * 所有的消息处理逻辑被封装在bolts里面。Bolts可以做很多事情：过滤，聚合，查询数据库等等。
		 * Bolts可以简单的做消息流的传递。复杂的消息流处理往往需要很多步骤，从而也就需要经过很多bolts。
		 * 比如算出一堆图片里面被转发最多的图片就至少需要两步： 第一步算出每个图片的转发数量。
		 * 第二步找出转发最多的前10个图片。（如果要把这个过程做得更具有扩展性那么可能需要更多的步骤）。
		 * 
		 * Bolts可以发射多条消息流，
		 * 使用OutputFieldsDeclarer.declareStream定义stream，使用OutputCollector
		 * .emit来选择要发射的stream。 Bolts的主要方法是execute,
		 * 它以一个tuple作为输入，bolts使用OutputCollector来发射tuple。
		 * bolts必须要为它处理的每一个tuple调用OutputCollector的ack方法
		 * ，以通知Storm这个tuple被处理完成了，从而通知这个tuple的发射者spouts。 一般的流程是：
		 * bolts处理一个输入tuple, 发射0个或者多个tuple,
		 * 然后调用ack通知storm自己已经处理过这个tuple了。storm提供了一个IBasicBolt会自动调用ack。
		 */
		builder.setBolt("word-normalizer", new WordNormalizer()).shuffleGrouping("word-reader");
		/*
		 * 上面的代码和下面的代码中都设定了数据分配的策略stream grouping
		 * 定义一个topology的其中一步是定义每个bolt接收什么样的流作为输入。stream
		 * grouping就是用来定义一个stream应该如果分配数据给bolts上面的多个tasks。 Storm里面有7种类型的stream
		 * grouping Shuffle Grouping: 随机分组，
		 * 随机派发stream里面的tuple，保证每个bolt接收到的tuple数目大致相同。 Fields Grouping：按字段分组，
		 * 比如按userid来分组， 具有同样userid的tuple会被分到相同的Bolts里的一个task，
		 * 而不同的userid则会被分配到不同的bolts里的task。 All
		 * Grouping：广播发送，对于每一个tuple，所有的bolts都会收到。 Global Grouping：全局分组，
		 * 这个tuple被分配到storm中的一个bolt的其中一个task。再具体一点就是分配给id值最低的那个task。 Non
		 * Grouping：不分组，这stream grouping个分组的意思是说stream不关心到底谁会收到它的tuple。
		 * 目前这种分组和Shuffle grouping是一样的效果，
		 * 有一点不同的是storm会把这个bolt放到这个bolt的订阅者同一个线程里面去执行。 Direct Grouping： 直接分组，
		 * 这是一种比较特别的分组方法，用这种分组意味着消息的发送者指定由消息接收者的哪个task处理这个消息。 只有被声明为Direct
		 * Stream的消息流可以声明这种分组方法。而且这种消息tuple必须使用emitDirect方法来发射。
		 * 消息处理者可以通过TopologyContext来获取处理它的消息的task的id
		 * （OutputCollector.emit方法也会返回task的id）。 Local or shuffle
		 * grouping：如果目标bolt有一个或者多个task在同一个工作进程中，tuple将会被随机发生给这些tasks。
		 * 否则，和普通的Shuffle Grouping行为一致。
		 */
		builder.setBolt("word-counter", new WordCounter(), 1).fieldsGrouping("word-normalizer",
				new Fields("word"));

		/*
		 * storm的运行有两种模式: 本地模式和分布式模式. 1) 本地模式： storm用一个进程里面的线程来模拟所有的spout和bolt.
		 * 本地模式对开发和测试来说比较有用。 你运行storm-starter里面的topology的时候它们就是以本地模式运行的，
		 * 你可以看到topology里面的每一个组件在发射什么消息。 2) 分布式模式：
		 * storm由一堆机器组成。当你提交topology给master的时候， 你同时也把topology的代码提交了。
		 * master负责分发你的代码并且负责给你的topolgoy分配工作进程。如果一个工作进程挂掉了，
		 * master节点会把认为重新分配到其它节点。 下面是以本地模式运行的代码:
		 * 
		 * Conf对象可以配置很多东西， 下面两个是最常见的： TOPOLOGY_WORKERS(setNumWorkers)
		 * 定义你希望集群分配多少个工作进程给你来执行这个topology.
		 * topology里面的每个组件会被需要线程来执行。每个组件到底用多少个线程是通过setBolt和setSpout来指定的。
		 * 这些线程都运行在工作进程里面. 每一个工作进程包含一些节点的一些工作线程。 比如， 如果你指定300个线程，60个进程，
		 * 那么每个工作进程里面要执行6个线程， 而这6个线程可能属于不同的组件(Spout, Bolt)。
		 * 你可以通过调整每个组件的并行度以及这些线程所在的进程数量来调整topology的性能。 TOPOLOGY_DEBUG(setDebug),
		 * 当它被设置成true的话， storm会记录下每个组件所发射的每条消息。 这在本地环境调试topology很有用，
		 * 但是在线上这么做的话会影响性能的。
		 */
		Config conf = new Config();
		conf.setDebug(false);
		conf.setNumWorkers(2);
		conf.put("wordsFile", "bin/test/words.txt");
		conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
		/*
		 * 定义一个LocalCluster对象来定义一个进程内的集群。提交topology给这个虚拟的集群和提交topology给分布式集群是一样的。
		 * 通过调用submitTopology方法来提交topology，
		 * 它接受三个参数：要运行的topology的名字，一个配置对象以及要运行的topology本身。
		 * topology的名字是用来唯一区别一个topology的，这样你然后可以用这个名字来杀死这个topology的。前面已经说过了，
		 * 你必须显式的杀掉一个topology， 否则它会一直运行。
		 */
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("wordCounterTopology", conf, builder.createTopology());
//		Thread.sleep(10000);
//		cluster.killTopology("wordCounterTopology");
//		cluster.shutdown();
	}

}
