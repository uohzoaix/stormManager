package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

/**
 * 
 * 功能说明： 主要是将文件内容读出来，一行一行
 * 
 * Spout类里面最重要的方法是nextTuple。 要么发射一个新的tuple到topology里面或者简单的返回如果已经没有新的tuple。
 * 要注意的是nextTuple方法不能阻塞，因为storm在同一个线程上面调用所有消息源spout的方法。
 * 另外两个比较重要的spout方法是ack和fail。 storm在检测到一个tuple被整个topology成功处理的时候调用ack，否则调用fail。
 * storm只对可靠的spout调用ack和fail。
 * 
 * @author 毛祥溢 Email:frank@maoxiangyi.cn 2013-8-26 下午6:05:46
 */
public class WordReader extends BaseRichSpout {

	private SpoutOutputCollector collector;
	private FileReader fileReader;
	private String filePath;
	private boolean completed = false;

	// storm在检测到一个tuple被整个topology成功处理的时候调用ack，否则调用fail。
	public void ack(Object msgId) {
		System.out.println("OK:" + msgId);
	}

	public void close() {
	}

	// storm在检测到一个tuple被整个topology成功处理的时候调用ack，否则调用fail。
	public void fail(Object msgId) {
		System.out.println("FAIL:" + msgId);
	}

	/*
	 * 在SpoutTracker类中被调用，每调用一次就可以向storm集群中发射一条数据（一个tuple元组），该方法会被不停的调用
	 */
	public void nextTuple() {
		if (completed) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			return;
		}
		String str;
		BufferedReader reader = new BufferedReader(fileReader);
		try {
			int i=0;
			while (true) {
				if(i>5000){
				}
				i++;
				System.out.println(i);
				str = reader.readLine();
				if (str == null) {
					reader = null;
					reader = new BufferedReader(new FileReader("bin/test/words.txt"));
					str = reader.readLine();
				}
				//System.out.println("WordReader类 读取到一行数据：" + str);
				this.collector.emit(new Values(str), str);
				//System.out.println("WordReader类 发射了一条数据：" + str);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error reading tuple", e);
		} finally {
			completed = true;
		}
	}

	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		try {
			this.fileReader = new FileReader(conf.get("wordsFile").toString());
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error reading file [" + conf.get("wordFile") + "]");
		}
		this.filePath = conf.get("wordsFile").toString();
		this.collector = collector;
	}

	/**
	 * 定义字段id，该id在简单模式下没有用处，但在按照字段分组的模式下有很大的用处。
	 * 该declarer变量有很大作用，我们还可以调用declarer.
	 * declareStream();来定义stramId，该id可以用来定义更加复杂的流拓扑结构
	 */
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("line"));
	}
}
