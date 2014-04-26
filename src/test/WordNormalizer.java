package test;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * 
 * 功能说明： 将一行文本切割成单词，并封装collector中发射出去
 * 
 * @author 毛祥溢 Email:frank@maoxiangyi.cn 2013-8-26 下午6:05:59
 */
public class WordNormalizer extends BaseBasicBolt {

	public void cleanup() {
		System.out.println("将一行文本切割成单词，并封装collector中发射出去 ---完毕！");
	}

	/**
	 * 接受的参数是WordReader发出的句子，即input的内容是句子 execute方法，将句子切割形成的单词发出
	 */
	public void execute(Tuple input, BasicOutputCollector collector) {
		String sentence = input.getString(0);
		String[] words = sentence.split(" ");
		//System.out.println("WordNormalizer类 收到一条数据，这条数据是：    " + sentence);
		for (String word : words) {
			word = word.trim();
			if (!word.isEmpty()) {
				word = word.toLowerCase();
				//System.out.println("WordNormalizer类 收到一条数据，这条数据是：    " + sentence
				//		+ "数据正在被切割，切割出来的单词是 " + word);
				collector.emit(new Values(word));
			}
		}
	}

	/**
	 * 定义字段id，该id在简单模式下没有用处，但在按照字段分组的模式下有很大的用处。
	 * 该declarer变量有很大作用，我们还可以调用declarer.
	 * declareStream();来定义stramId，该id可以用来定义更加复杂的流拓扑结构
	 */
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("word"));
	}
}
