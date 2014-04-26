package frame.pojo;

import frame.manager.StormClientManager.Status;

public class Topology {

	private Status status;
	private String name;
	private Integer taskNum;
	private Integer excutorNum;
	private Integer workerNum;
	private Integer uptimeSeconds;

	public Topology(Status status, String name, Integer taskNum, Integer excutorNum,
			Integer workerNum, Integer uptimeSeconds) {
		super();
		this.status = status;
		this.name = name;
		this.taskNum = taskNum;
		this.excutorNum = excutorNum;
		this.workerNum = workerNum;
		this.uptimeSeconds = uptimeSeconds;
	}

	public Status getStatus() {
		return status;
	}

	public void setStauts(Status status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTaskNum() {
		return taskNum;
	}

	public void setTaskNum(Integer taskNum) {
		this.taskNum = taskNum;
	}

	public Integer getExcutorNum() {
		return excutorNum;
	}

	public void setExcutorNum(Integer excutorNum) {
		this.excutorNum = excutorNum;
	}

	public Integer getWorkerNum() {
		return workerNum;
	}

	public void setWorkerNum(Integer workerNum) {
		this.workerNum = workerNum;
	}

	public Integer getUptimeSeconds() {
		return uptimeSeconds;
	}

	public void setUptimeSeconds(Integer uptimeSeconds) {
		this.uptimeSeconds = uptimeSeconds;
	}

}
