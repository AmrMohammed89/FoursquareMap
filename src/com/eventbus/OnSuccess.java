package com.eventbus;

import com.model.FoursquareSearchModel.response;

;

public class OnSuccess {
	response success;

	public response getSuccess() {
		return success;
	}

	public void setSuccess(response success) {
		this.success = success;
	}

	public OnSuccess(response success) {
		this.success = success;
	}
}
