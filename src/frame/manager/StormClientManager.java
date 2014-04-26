package frame.manager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift7.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.generated.TopologySummary;
import backtype.storm.utils.NimbusClient;
import backtype.storm.utils.Utils;
import frame.pojo.Topology;

public class StormClientManager {

	private static final Logger log = LoggerFactory.getLogger(StormClientManager.class);

	private NimbusClient nimbusClient;  

	public Map<String, Topology> topos = new HashMap<String, Topology>();

	public enum Status {
		ACTIVE(1, "ACTIVE"), INACTIVE(2, "INACTIVE"), REBALANCING(3, "REBALANCING"), KILLED(4,
				"KILLED");

		private static final Map<String, Status> byName = new HashMap<String, Status>();

		static {
			for (Status statu : EnumSet.allOf(Status.class)) {
				byName.put(statu.getName(), statu);
			}
		}

		public static Status findByType(int type) {
			switch (type) {
			case 1:
				return ACTIVE;
			case 2:
				return INACTIVE;
			case 3:
				return REBALANCING;
			case 4:
				return KILLED;
			default:
				return null;
			}
		}

		public static Status findByTypeOrThrow(int type) {
			Status status = findByType(type);
			if (status == null)
				throw new IllegalArgumentException("Type " + type + " doesn't exist!");
			return status;
		}

		public static Status findByName(String name) {
			return byName.get(name);
		}

		private Integer type;
		private String name;

		Status(Integer type, String name) {
			this.type = type;
			this.name = name;
		}

		public Integer getType() {
			return type;
		}

		public void setType(Integer type) {
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public void init() {
		log.info("get nimbusclient");
		nimbusClient = NimbusClient.getConfiguredClient(Utils.readDefaultConfig());
		if (nimbusClient == null) {
			log.error("nimbusClient is halt!please check the STORM PROCESS!");
		}
	}

	public Map<Status, List<Topology>> getTopologyStatus() {
		if(nimbusClient==null){
			nimbusClient = NimbusClient.getConfiguredClient(Utils.readDefaultConfig());
		}
		Map<Status, List<Topology>> topologyStatus = new HashMap<Status, List<Topology>>();
		try {
			List<TopologySummary> topologySummaries = nimbusClient.getClient().getClusterInfo()
					.get_topologies();
			if (!CollectionUtils.isEmpty(topologySummaries)) {
				topos.clear();
				for (TopologySummary summary : topologySummaries) {
					Status status = Status.findByName(summary.get_status());
					Topology topology = new Topology(status, summary.get_name(),
							summary.get_num_tasks(), summary.get_num_executors(),
							summary.get_num_workers(), summary.get_uptime_secs());
					topos.put(topology.getName(), topology);
					if (topologyStatus.containsKey(status)) {
						topologyStatus.get(status).add(topology);
					} else {
						List<Topology> topologies = new ArrayList<Topology>();
						topologies.add(topology);
						topologyStatus.put(status, topologies);
					}
				}
			}
		} catch (TException e) {
			log.error(e.getMessage());
		}
		return topologyStatus;
	}

	public NimbusClient getClient() {
		return nimbusClient;
	}

	public void shutdown() {
		if (nimbusClient != null)
			nimbusClient.close();
	}
}
