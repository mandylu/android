//liuweili@baixing.com
package com.baixing.network.api;

public class ApiError {
	private String errorCode;
	private String msg;
//	private String subCode;
//	private String subMsg;
	private String serverResponse;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

//	public String getSubCode() {
//		return subCode;
//	}
//
//	public void setSubCode(String subCode) {
//		this.subCode = subCode;
//	}
//
//	public String getSubMsg() {
//		return subMsg;
//	}
//
//	public void setSubMsg(String subMsg) {
//		this.subMsg = subMsg;
//	}
	
	public String getServerResponse() {
		return serverResponse;
	}

	public void setServerResponse(String serverResponse) {
		this.serverResponse = serverResponse;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("errorCode:").append(this.errorCode).append(" msg:")
				.append(this.msg).append(" serverResponse:").append(serverResponse);
		return builder.toString();
	}
}
