package com.welink.commons.domain;

import java.util.List;

public class QuestionAndOptionsModel {
	public MikuMineQuestionsDO question;
	public List<MikuMineQoptionsDO> optionsList;
	
	
	public MikuMineQuestionsDO getQuestion() {
		return question;
	}

	public void setQuestion(MikuMineQuestionsDO question) {
		this.question = question;
	}

	public List<MikuMineQoptionsDO> getOptionsList() {
		return optionsList;
	}
	public void setOptionsList(List<MikuMineQoptionsDO> optionsList) {
		this.optionsList = optionsList;
	}

	@Override
	public String toString() {
		return "QuestionAndOptionsModel [question=" + question
				+ ", optionsList=" + optionsList + "]";
	}
	
	
}
